package org.patientview.api.controller;

import org.patientview.persistence.model.FhirObservation;
import org.patientview.api.model.ObservationSummary;
import org.patientview.api.model.UserResultCluster;
import org.patientview.api.service.ObservationService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
@RestController
public class ObservationController extends BaseController<ObservationController> {

    @Inject
    private ObservationService observationService;

    private static final String DEFAULT_SORT = "appliesDateTime";
    private static final String DEFAULT_SORT_DIRECTION = "DESC";

    @RequestMapping(value = "/user/{userId}/observations/{code}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirObservation>> getObservationsByCode(@PathVariable("userId") Long userId,
            @PathVariable("code") String code) throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(
            observationService.get(userId, code.toUpperCase(), DEFAULT_SORT, DEFAULT_SORT_DIRECTION, null),
            HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/observations/summary", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<ObservationSummary>> getObservationSummary(
            @PathVariable("userId") Long userId) throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(observationService.getObservationSummary(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/observations/resultclusters", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void addResultClusters(@PathVariable("userId") Long userId,
                                  @RequestBody List<UserResultCluster> userResultClusters)
            throws ResourceNotFoundException, FhirResourceException {
        observationService.addUserResultClusters(userId, userResultClusters);
    }
}
