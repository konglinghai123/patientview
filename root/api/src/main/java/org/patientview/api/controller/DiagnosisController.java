package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ConditionService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;

/**
 * RESTful interface for adding diagnoses (Conditions).
 *
 * Created by jamesr@solidstategroup.com
 * Created on 03/12/2015
 */
@RestController
@ExcludeFromApiDoc
public class DiagnosisController extends BaseController<DiagnosisController> {

    @Inject
    private ConditionService conditionService;

    /**
     * Add a diagnosis to a patient, used by staff to add DIAGNOSIS_STAFF_ENTERED Condition to a patient
     * @param userId Long User ID of patient to add diagnosis to
     * @param code String code of diagnosis to add
     * @throws ResourceNotFoundException
     * @throws EntityExistsException
     * @throws FhirResourceException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/diagnosis/{code}", method = RequestMethod.POST)
    public void add(@PathVariable("userId") Long userId, @PathVariable("code") String code)
            throws ResourceNotFoundException, EntityExistsException, FhirResourceException, ResourceForbiddenException {
        conditionService.staffAddCondition(userId, code);
    }
}