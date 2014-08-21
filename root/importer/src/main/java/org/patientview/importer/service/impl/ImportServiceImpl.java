package org.patientview.importer.service.impl;

import com.rabbitmq.client.Channel;
import generated.Patientview;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.service.ImportService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Service
public class ImportServiceImpl extends AbstractServiceImpl<ImportServiceImpl> implements ImportService {

    private final static String QUEUE_NAME = "patient_import";

    @Inject
    private Channel channel;

    public ImportServiceImpl() {

    }

    @Override
    public void importRecord(final Patientview patientview) throws ImportResourceException {

        StringWriter stringWriter = new StringWriter();

        try {
            JAXBContext context = JAXBContext.newInstance(Patientview.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(patientview, stringWriter);
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall patientview record");
        }

        try {
            channel.basicPublish("", QUEUE_NAME, true, null, stringWriter.toString().getBytes());
        } catch (IOException e) {
            throw new ImportResourceException("Unable to send message onto queue");
        }
        LOG.info("Successfully sent record to be processed for NHS number {}", patientview.getPatient().getPersonaldetails().getNhsno());

    }


}
