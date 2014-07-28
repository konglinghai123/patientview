package org.patientview.api.service.impl;

import org.apache.commons.lang.SerializationUtils;
import org.patientview.api.service.CodeService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Class to control the crud operations of Codes.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Service
public class CodeServiceImpl implements CodeService {

    @Inject
    private CodeRepository codeRepository;
    @Inject
    private LinkRepository linkRepository;
    @Inject
    private UserRepository userRepository;

    public List<Code> getAllCodes() { return Util.iterableToList(codeRepository.findAll()); }

    public Code createCode(final Code code) {
        return codeRepository.save(code);
    }

    public Code getCode(final Long codeId) {
        return codeRepository.findOne(codeId);
    }

    public Code saveCode(final Code code) {
        return codeRepository.save(code);
    }

    public Code cloneCode(Long codeId) {
        Code entityCode = codeRepository.findOne(codeId);
        Code newCode = (Code)SerializationUtils.clone(entityCode);
        newCode.setId(null);
        return codeRepository.save(newCode);
    }

    public void deleteCode(final Long codeId) {
        codeRepository.delete(codeId);
    }

    public Link addLink(Long codeId, Link link) {
        Code entityCode = codeRepository.findOne(codeId);
        link.setCode(entityCode);
        link.setCreator(userRepository.findOne(1L));
        Link persistedLink = linkRepository.save(link);
        return persistedLink;
    }
}
