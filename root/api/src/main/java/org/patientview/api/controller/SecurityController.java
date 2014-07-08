package org.patientview.api.controller;

import org.patientview.api.service.SecurityService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * This controller is associated with the calls
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@RestController
public class SecurityController extends BaseController {

    private final static Logger LOG = LoggerFactory.getLogger(SecurityController.class);

    @Inject
    private SecurityService securityService;

    @RequestMapping(value = "/security/user/{userId}/roles", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Role>> getSecurityRolesByUser(@PathVariable("userId") Long userId) {
        return new ResponseEntity<List<Role>>(securityService.getUserRoles(userId), HttpStatus.OK);
    }


    @RequestMapping(value = "/security/user/{userId}/role/{roleId}/groups", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Group> getSecurityGroupsByUserAndRole(@PathVariable("userId") Long userId,
                                               @PathVariable("roleId") Long roleId) {

        return securityService.getGroupByUserAndRole(userId, roleId);
    }

    @RequestMapping(value = "/security/user/{userId}/routes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Set<Route>> getUserRoutes(@PathVariable("userId") Long userId,
                                                     UriComponentsBuilder uriComponentsBuilder) {

        LOG.trace("Request has been received for userId : {}", userId);

        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<Set<Route>>(securityService.getUserRoutes(userId), HttpStatus.OK);

    }

    @RequestMapping(value = "/security/user/{userId}/news", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<NewsItem>> getUserNews(@PathVariable("userId") Long userId,
                                                     UriComponentsBuilder uriComponentsBuilder) {

        LOG.trace("Request has been received for userId : {}", userId);

        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<List<NewsItem>>(securityService.getNewsByUser(userId), HttpStatus.OK);

    }

    @RequestMapping(value = "/security/user/{userId}/groups", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Group>> getUserGroups(@PathVariable("userId") Long userId,
                                                      UriComponentsBuilder uriComponentsBuilder) {

        LOG.trace("Request has been received for userId : {}", userId);

        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<List<Group>>(securityService.getUserGroups(userId), HttpStatus.OK);

    }

}
