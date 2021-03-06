package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ApiConditionService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.List;

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
    private ApiConditionService apiConditionService;

    /**
     * Add multiple diagnoses (Conditions) to your own FHIR record of type DIAGNOSIS_PATIENT_ENTERED
     * @param userId User ID of current User
     * @param codes List of String code of diagnoses
     * @throws ResourceNotFoundException
     * @throws EntityExistsException
     * @throws FhirResourceException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/diagnosis/patiententered", method = RequestMethod.POST)
    public void addMultiplePatientEntered(@PathVariable("userId") Long userId, @RequestBody List<String> codes)
            throws ResourceNotFoundException, EntityExistsException, FhirResourceException, ResourceForbiddenException {
        apiConditionService.patientAddConditions(userId, codes);
    }

    /**
     * Add a diagnosis (Condition) to your own FHIR record of type DIAGNOSIS_PATIENT_ENTERED
     * @param userId User ID of current User
     * @param code String code of diagnosis
     * @throws FhirResourceException
     * @throws ResourceForbiddenException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/diagnosis/{code}/patiententered", method = RequestMethod.POST)
    public void addPatientEntered(@PathVariable("userId") Long userId, @PathVariable("code") String code)
            throws ResourceNotFoundException, EntityExistsException, FhirResourceException, ResourceForbiddenException {
        apiConditionService.patientAddCondition(userId, code);
    }

    /**
     * Add a diagnosis (Condition) to a patient, used by staff to add DIAGNOSIS_STAFF_ENTERED Condition to a patient
     * @param userId Long User ID of patient to add diagnosis to
     * @param code String code of diagnosis to add
     * @throws ResourceNotFoundException
     * @throws EntityExistsException
     * @throws FhirResourceException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/diagnosis/{code}/staffentered", method = RequestMethod.POST)
    public void addStaffEntered(@PathVariable("userId") Long userId, @PathVariable("code") String code)
            throws ResourceNotFoundException, EntityExistsException, FhirResourceException, ResourceForbiddenException {
        apiConditionService.staffAddCondition(userId, code);
    }

    /**
     * Get patient entered Conditions for a patient if present
     * @param userId Long User ID of patient to get patient entered Conditions for
     * @return List of patient entered Conditions
     * @throws FhirResourceException
     * @throws ResourceForbiddenException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/diagnosis/patiententered", method = RequestMethod.GET)
    public ResponseEntity<List<FhirCondition>> getPatientEntered(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, EntityExistsException, FhirResourceException, ResourceForbiddenException {
        return new ResponseEntity<>(
            apiConditionService.getUserEntered(userId, DiagnosisTypes.DIAGNOSIS_PATIENT_ENTERED, false), HttpStatus.OK);
    }

    /**
     * Get staff entered Conditions for a patient if present
     * @param userId Long User ID of patient to get staff entered Conditions for
     * @return List of staff entered Conditions
     * @throws FhirResourceException
     * @throws ResourceForbiddenException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/diagnosis/staffentered", method = RequestMethod.GET)
    public ResponseEntity<List<FhirCondition>> getStaffEntered(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, EntityExistsException, FhirResourceException, ResourceForbiddenException {
        return new ResponseEntity<>(
            apiConditionService.getUserEntered(userId, DiagnosisTypes.DIAGNOSIS_STAFF_ENTERED, false), HttpStatus.OK);
    }

    /**
     * Remove a diagnosis (Condition) from your own FHIR record of type DIAGNOSIS_PATIENT_ENTERED
     * @param userId User ID of current User
     * @param code String code of diagnosis
     * @throws FhirResourceException
     * @throws ResourceForbiddenException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/diagnosis/{code}/patiententered", method = RequestMethod.DELETE)
    public void removePatientEntered(@PathVariable("userId") Long userId, @PathVariable("code") String code)
            throws ResourceNotFoundException, EntityExistsException, FhirResourceException, ResourceForbiddenException {
        apiConditionService.patientRemoveCondition(userId, code);
    }

    /**
     * Set the status of all a User's staff entered Conditions to "refuted", equivalent to deleting
     * @param userId User ID of user to set staff entered Conditions status to "refuted"
     * @throws Exception
     */
    @RequestMapping(value = "/user/{userId}/diagnosis/staffentered", method = RequestMethod.DELETE)
    public void removeStaffEntered(@PathVariable("userId") Long userId) throws Exception {
        apiConditionService.staffRemoveCondition(userId);
    }
}
