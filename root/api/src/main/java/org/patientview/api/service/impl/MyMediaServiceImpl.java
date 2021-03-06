package org.patientview.api.service.impl;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.im4java.core.IM4JavaException;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.scale.AWTUtil;
import org.patientview.api.model.BaseUser;
import org.patientview.api.service.MyMediaService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.MediaUserSpaceLimitException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.MyMedia;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.MediaTypes;
import org.patientview.persistence.repository.MyMediaRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to control the crud operations of the News.
 * <p>
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class MyMediaServiceImpl extends AbstractServiceImpl<MyMediaServiceImpl> implements MyMediaService {

    @Autowired
    private MyMediaRepository myMediaRepository;


    @Autowired
    private UserRepository userRepository;


    @Override
    public org.patientview.api.model.MyMedia save(Long userId, MyMedia myMedia) throws ResourceNotFoundException,
            ResourceForbiddenException,
            IOException, IM4JavaException, InterruptedException, JCodecException, MediaUserSpaceLimitException {
        User currentUser = userRepository.findOne(userId);
        myMedia.setCreator(currentUser);


        Long userSpaceSize = myMediaRepository.getUserTotal(currentUser, false);

        if (userSpaceSize != null && userSpaceSize >= 50) {
            throw new MediaUserSpaceLimitException("You currently have " + userSpaceSize + "mb of files." +
                    "Please delete some files to make space for new media.");
        }

        if (myMedia.getData() != null) {
            byte[] decodedString = Base64.decodeBase64(new String(myMedia.getData()).getBytes("UTF-8"));
            myMedia.setContent(decodedString);

            if (myMedia.getType().equals(MediaTypes.IMAGE)) {
                myMedia.setThumbnailContent(this.getPreviewImage(myMedia, 200));
            }
            //TODO this bit is too slow. Need to backgroun it and get mov working
//            else if (myMedia.getType().equals(MediaTypes.VIDEO)) {
//                //TODO need to create the video thumbnail here.
//                //TODO This will need to use either xuggler or ffmpg to convert mov files to mp4
//                myMedia.setThumbnailContent(getVideoThumbnail(myMedia));
//            }

        }

        return createMyMediaDto(myMediaRepository.save(myMedia));
    }

    @Override
    public org.patientview.api.model.MyMedia get(long id) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException {
        return createMyMediaDto(myMediaRepository.findOne(id));
    }

    @Override
    public void delete(Long myMediaId) throws ResourceNotFoundException, ResourceForbiddenException,
            UnsupportedEncodingException {
        MyMedia myMedia = myMediaRepository.findOne(myMediaId);
        if (ApiUtil.getCurrentUser().getId().equals(myMedia.getCreator().getId())) {
            //Because media can be shared across multiple conversations etc, we just remove the content
            //From the db so we can display an error
            myMedia.setDeleted(true);
            myMedia.setContent(null);
            myMedia.setThumbnailContent(null);
            myMedia.setLocalPath(null);
            myMediaRepository.save(myMedia);
        } else {
            throw new ResourceForbiddenException("Unauthorised");
        }
    }

    @Override
    public PageImpl<org.patientview.api.model.MyMedia> getAllForUser(Long userId, GetParameters getParameters) throws
            ResourceNotFoundException, ResourceForbiddenException, UnsupportedEncodingException {
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        PageRequest pageable = createPageRequest(pageConverted, sizeConverted, sortField, sortDirection);

        Page<List<MyMedia>> media =
                myMediaRepository.getByCreator(userRepository.findOne(userId), false, pageable);

        List<org.patientview.api.model.MyMedia> mediaToReturn = new ArrayList();

        for (int i = 0; i < media.getContent().size(); i++) {
            mediaToReturn.add(this.createMyMediaDto((MyMedia) media.getContent().get(i)));
        }

        return new PageImpl<>(mediaToReturn, pageable, media.getTotalElements());
    }

    @Override
    public byte[] getPreviewImage(MyMedia myMedia, int height) throws IOException, IM4JavaException,
            InterruptedException {
        // convert byte array back to BufferedImage
        InputStream in = new ByteArrayInputStream(myMedia.getContent());
        BufferedImage image = ImageIO.read(in);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(scale(image), "jpg", baos);
        return baos.toByteArray();
    }

    @Override
    public org.patientview.api.model.MyMedia createMyMediaDto(MyMedia myMedia) {
        return new org.patientview.api.model.MyMedia(myMedia);
    }

    private byte[] getVideoThumbnail(MyMedia myMedia) throws IOException, JCodecException {
        String[] localPath = myMedia.getLocalPath().split("/");
        String fileExtension = localPath[localPath.length - 1].split("\\.")[1];
        if (fileExtension.equals("mp4") || fileExtension.equals("mpeg4")) {
            String inputFileName = String.format("%d-%d", new Date().getTime(), ApiUtil.getCurrentUser().getId());
            File temp = File.createTempFile(inputFileName, "." + fileExtension);
            FileUtils.writeByteArrayToFile(temp, myMedia.getContent());

            int frameNumber = 2;
            org.jcodec.common.model.Picture picture = FrameGrab.getFrameFromFile(temp, frameNumber);

            //for JDK (jcodec-javase)
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);

            temp.delete();

            return baos.toByteArray();
        } else {
            return null;
        }
    }

    private BufferedImage scale(BufferedImage originalBufferedImage) {
        int thumbnailWidth = 150;

        int widthToScale, heightToScale;
        if (originalBufferedImage.getWidth() > originalBufferedImage.getHeight()) {

            heightToScale = (int) (1.1 * thumbnailWidth);
            widthToScale = (int) ((heightToScale * 1.0) / originalBufferedImage.getHeight()
                    * originalBufferedImage.getWidth());

        } else {
            widthToScale = (int) (1.1 * thumbnailWidth);
            heightToScale = (int) ((widthToScale * 1.0) / originalBufferedImage.getWidth()
                    * originalBufferedImage.getHeight());
        }

        BufferedImage resizedImage = new BufferedImage(widthToScale,
                heightToScale, originalBufferedImage.getType());
        Graphics2D g = resizedImage.createGraphics();

        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(originalBufferedImage, 0, 0, widthToScale, heightToScale, null);
        g.dispose();

        return resizedImage;
    }

//
//    private byte[] transcodeVideo(MyMedia myMedia) throws IOException {
//        String[] localPath = myMedia.getLocalPath().split("/");
//        String fileExtension = localPath[localPath.length - 1].split("\\.")[1];
//
//        String inputFileName = String.format("%d-%d", new Date().getTime(), ApiUtil.getCurrentUser().getId());
//        File temp = File.createTempFile(inputFileName, "." + fileExtension);
//        File outputTemp = File.createTempFile(inputFileName, ".mp4");
//
//        FileUtils.writeByteArrayToFile(temp, myMedia.getContent());
//        Long st = System.currentTimeMillis();
//
//        // create a media reader
//        IMediaReader mediaReader = ToolFactory.makeReader(temp.getPath());
//
//        // create a media writer
//        IMediaWriter mediaWriter = ToolFactory.makeWriter(outputTemp.getPath(), mediaReader);
//
//        // add a writer to the reader, to create the output file
//        mediaReader.addListener(mediaWriter);
//
//        // read and decode packets from the source file and
//        // and dispatch decoded audio and video to the writer
//        while (mediaReader.readPacket() == null) ;
//
//        Long end = System.currentTimeMillis();
//        System.out.println("Time Taken In Milli Seconds: " + (end - st));
//        byte[] toReturn = Files.readAllBytes(outputTemp.toPath());
//
//        temp.delete();
//        outputTemp.delete();
//
//        return toReturn;
//    }
//
//
//    private byte[] ffmpg(MyMedia myMedia) throws IOException {
//        String[] localPath = myMedia.getLocalPath().split("/");
//        String fileExtension = localPath[localPath.length - 1].split("\\.")[1];
//
//        String inputFileName = String.format("%d-%d", new Date().getTime(), ApiUtil.getCurrentUser().getId());
//        File temp = File.createTempFile(inputFileName, "." + fileExtension);
//        File outputTemp = File.createTempFile(inputFileName, ".mp4");
//
//        FileUtils.writeByteArrayToFile(temp, myMedia.getContent());
//
//
//        FFmpegBuilder builder =
//                new FFmpegBuilder()
//                        .addInput(temp.getPath())
//                        .addOutput(outputTemp.getPath())
//                        .setVideoFrameRate(FFmpeg.FPS_24)
//                        .done();
//
//
//        //FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
//
//        // Run a one-pass encode
//        //executor.createJob(builder).run();
//
//
//        byte[] toReturn = Files.readAllBytes(outputTemp.toPath());
//
//        temp.delete();
//        outputTemp.delete();
//
//        return toReturn;
//    }


}
