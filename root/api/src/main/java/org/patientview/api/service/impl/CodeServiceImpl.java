package org.patientview.api.service.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.CodeService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Class to control the crud operations of Codes.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Service
public class CodeServiceImpl extends AbstractServiceImpl<CodeServiceImpl> implements CodeService {

    @Inject
    private CodeRepository codeRepository;
    @Inject
    private LinkRepository linkRepository;
    @Inject
    private Properties properties;
    @Inject
    private UserRepository userRepository;

    @Override
    public Code add(final Code code) throws EntityExistsException {
        Code newCode;

        Set<Link> links;
        // get links and features, avoid persisting until code created successfully
        if (!CollectionUtils.isEmpty(code.getLinks())) {
            links = new HashSet<>(code.getLinks());
            code.getLinks().clear();
        } else {
            links = new HashSet<>();
        }

        // save basic details, checking if identical code already exists
        if (codeExists(code)) {
            LOG.debug("Code not created, Code already exists with these details");
            throw new EntityExistsException("Code already exists with these details");
        }
        newCode = codeRepository.save(code);

        // save links
        for (Link link : links) {
            link.setCode(newCode);
            link = linkRepository.save(link);
            newCode.getLinks().add(link);
        }

        return newCode;
    }

    @Override
    public Code cloneCode(final Long codeId) {
        // clone original
        Code entityCode = codeRepository.findOne(codeId);
        Code newCode = (Code) SerializationUtils.clone(entityCode);
        newCode.setCode(newCode.getCode() + "_new");

        // set up links
        newCode.setLinks(new HashSet<Link>());
        for (Link link : entityCode.getLinks()) {
            Link newLink = new Link();
            newLink.setLink(link.getLink());
            newLink.setName(link.getName());
            newLink.setDisplayOrder(link.getDisplayOrder());
            newLink.setCode(newCode);
            newLink.setLinkType(link.getLinkType());
            newLink.setCreator(userRepository.findOne(1L));
            newCode.getLinks().add(newLink);
        }
        newCode.setId(null);
        return codeRepository.save(newCode);
    }

    private boolean codeExists(Code code) {
        return codeRepository.findOneByCode(code.getCode()) != null;
    }

    @Override
    public void delete(final Long codeId) {
        codeRepository.delete(codeId);
    }

    @Override
    public List<Code> findAllByCodeAndType(String code, Lookup codeType) {
        return codeRepository.findAllByCodeAndType(code, codeType);
    }

    @Override
    public Code get(final Long codeId) throws ResourceNotFoundException {
        Code code = codeRepository.findOne(codeId);
        if (code == null) {
            throw new ResourceNotFoundException("Code does not exist");
        }
        return code;
    }

    @Override
    public Page<Code> getAllCodes(GetParameters getParameters) {

        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        String[] codeTypes = getParameters.getCodeTypes();
        String[] standardTypes = getParameters.getStandardTypes();
        String filterText = getParameters.getFilterText();

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortDirection)) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection.equals("DESC")) {
                direction = Sort.Direction.DESC;
            }

            pageable = new PageRequest(pageConverted, sizeConverted, new Sort(new Sort.Order(direction, sortField)));
        } else {
            pageable = new PageRequest(pageConverted, sizeConverted);
        }

        List<Long> codeTypesList = convertStringArrayToLongs(codeTypes);
        List<Long> standardTypesList = convertStringArrayToLongs(standardTypes);

        if (StringUtils.isEmpty(filterText)) {
            filterText = "%%";
        } else {
            filterText = "%" + filterText.trim().toUpperCase() + "%";
        }

        if (ArrayUtils.isNotEmpty(codeTypes) && ArrayUtils.isNotEmpty(standardTypes)) {
            return codeRepository.findAllByCodeAndStandardTypesFiltered(filterText, codeTypesList,
                    standardTypesList, pageable);
        }
        if (ArrayUtils.isNotEmpty(codeTypes)) {
            return codeRepository.findAllByCodeTypesFiltered(filterText, codeTypesList, pageable);
        }
        if (ArrayUtils.isNotEmpty(standardTypes)) {
            return codeRepository.findAllByStandardTypesFiltered(filterText, standardTypesList, pageable);
        }

        return codeRepository.findAllFiltered(filterText, pageable);
    }

    @Override
    public List<Code> getPatientManagementDiagnoses() {
        List<Code> codes = new ArrayList<>();

        // todo: make generic, currently hardcoded
        String diagnosesCodeString = properties.getProperty("patient.management.diagnoses.codes");

        if (StringUtils.isNotEmpty(diagnosesCodeString)) {
            List<String> diagnosesCodeArr = Arrays.asList(diagnosesCodeString.split(","));
            if (!CollectionUtils.isEmpty(diagnosesCodeArr)) {
                for (String code : diagnosesCodeArr) {
                    Code codeEntity = codeRepository.findOneByCode(code);
                    if (codeEntity != null) {
                        codes.add(codeEntity);
                    }
                }
            }


        }

        return codes;
    }

    public Code save(final Code code) throws ResourceNotFoundException, EntityExistsException {
        Code entityCode = codeRepository.findOne(code.getId());
        if (entityCode == null) {
            throw new ResourceNotFoundException("Code does not exist");
        }

        // check if another code with this code exists
        Code existingCode = codeRepository.findOneByCode(code.getCode());
        if (codeExists(code) && !(existingCode.getId().equals(code.getId()))) {
            throw new EntityExistsException("Code already exists with this code");
        }

        entityCode.setCode(code.getCode());
        entityCode.setDescription(code.getDescription());
        entityCode.setCodeType(code.getCodeType());
        entityCode.setStandardType(code.getStandardType());
        return codeRepository.save(entityCode);
    }
}
