package org.patientview.api.controller;

import org.patientview.api.service.ContactPointService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
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

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
public class ContactPointController extends BaseController<ContactPointController> {

    @Inject
    private ContactPointService contactPointService;

    @RequestMapping(value = "/contactpoint/{contactPointId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("contactPointId") Long contactPointId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        contactPointService.delete(contactPointId);
    }

    @RequestMapping(value = "/contactpoint", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody ContactPoint contactPoint)
            throws ResourceNotFoundException, ResourceForbiddenException {
        contactPointService.save(contactPoint);
    }

    @RequestMapping(value = "/group/{groupId}/contactpoints", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ContactPoint> add(@PathVariable("groupId") Long groupId,
                                                        @RequestBody ContactPoint contactPoint)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(contactPointService.add(groupId, contactPoint), HttpStatus.CREATED);
    }

    /*
    // used by migration
    @RequestMapping(value = "/contactpoint/type/{type}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ContactPointType> getContactPointType(@PathVariable(value = "type") String type)
            throws ResourceInvalidException {
        return new ResponseEntity<>(contactPointService.getContactPointType(type), HttpStatus.OK);
    }
    */
}
