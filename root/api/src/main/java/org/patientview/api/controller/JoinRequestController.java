package org.patientview.api.controller;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
public class JoinRequestController extends BaseController<JoinRequestController> {

    @Inject
    private JoinRequestService joinRequestService;

    @RequestMapping(value = "/joinrequest/statuses", method = RequestMethod.GET)
    @ResponseBody
    public List<JoinRequestStatus> getStatuses() {
        return CollectionUtils.arrayToList(JoinRequestStatus.values());
    }

    @RequestMapping(value = "/user/{userId}/joinrequests", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<JoinRequest>> get(@PathVariable("userId") Long userId,
                                                 @RequestParam(value = "statuses", required = false)
                                                            List<String> statuses)
            throws ResourceNotFoundException {
        if (CollectionUtils.isEmpty(statuses)) {
            return new ResponseEntity(joinRequestService.get(userId), HttpStatus.OK);
        }

        return new ResponseEntity(joinRequestService.getByStatuses(userId, convertList(statuses)), HttpStatus.OK);

    }

    @RequestMapping(value = "/joinrequest", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> save(@RequestBody JoinRequest joinRequest)
        throws ResourceNotFoundException{
        joinRequestService.save(joinRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/joinrequests/count", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<BigInteger> getSubmittedJoinRequestCount(@PathVariable("userId") Long userId) {
        try {
            LOG.debug("Request has been received for conversations of userId : {}", userId);
            return new ResponseEntity<>(joinRequestService.getCount(userId), HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private List<JoinRequestStatus> convertList(List<String> values) {
        List<JoinRequestStatus> statuses = new ArrayList<>();
        for (String value : values) {
            value = value.replaceAll("\\[", "");
            value = value.replaceAll("\\]", "");
            value = value.replaceAll("\"", "");
            statuses.add(JoinRequestStatus.valueOf(value));
        }
        return statuses;
    }

}
