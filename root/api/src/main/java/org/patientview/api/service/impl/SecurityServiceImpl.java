package org.patientview.api.service.impl;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.SecurityService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Service to supply data based on a user's role and group
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Service
public class SecurityServiceImpl extends AbstractServiceImpl<SecurityServiceImpl> implements SecurityService {

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private RouteRepository routeRepository;

    // TODO Remove and introduce service
    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private UserRepository userRepository;

    public List<Role> getUserRoles(Long userId) {
        return Util.convertIterable(roleRepository.findValidRolesByUser(userId));
    }

    public Set<Route> getUserRoutes(Long userId) {
        User user = userRepository.findOne(userId);
        Set<Route> routes = new TreeSet<Route>(Util.convertIterable(routeRepository.findFeatureRoutesByUser(user)));
        routes.addAll(Util.convertIterable(routeRepository.findGroupRoutesByUser(user)));
        routes.addAll(Util.convertIterable(routeRepository.findRoleRoutesByUser(user)));
        return routes;

    }

    public List<Group> getGroupByUserAndRole(Long userId, Long roleId) {
        User user = userRepository.findOne(userId);
        Role role = roleRepository.findOne(roleId);
        return Util.convertIterable(groupRepository.findGroupByUserAndRole(user, role));
    }

    private List<org.patientview.api.model.Group> convertGroupsToTransportGroups(List<Group> groups) {
        List<org.patientview.api.model.Group> transportGroups = new ArrayList<>();

        for (Group group : groups) {
            org.patientview.api.model.Group newGroup = new org.patientview.api.model.Group();
            newGroup.setCode(group.getCode());
            newGroup.setId(group.getId());
            newGroup.setGroupType(group.getGroupType());
            newGroup.setGroupFeatures(group.getGroupFeatures());
            newGroup.setName(group.getName());
            newGroup.setVisible(group.getVisible());

            // only need parent groups in front end for headers
            for (Group parentGroup : group.getParentGroups()) {
                org.patientview.api.model.Group newParentGroup = new org.patientview.api.model.Group();
                newParentGroup.setCode(parentGroup.getCode());
                newParentGroup.setId(parentGroup.getId());
                newParentGroup.setName(parentGroup.getName());
                newGroup.getParentGroups().add(newParentGroup);
            }

            transportGroups.add(newGroup);
        }

        return transportGroups;
    }

    private List<Long> convertStringArrayToLongs(String[] strings) {
        final List<Long> longs = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(strings)) {
            for (String string : strings) {
                longs.add(Long.parseLong(string));
            }
        }
        return longs;
    }

    public Page<org.patientview.api.model.Group> getUserGroups(Long userId, GetParameters getParameters) {
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        String[] groupTypes = getParameters.getGroupTypes();
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

        List<Long> groupTypesList = convertStringArrayToLongs(groupTypes);

        if (StringUtils.isEmpty(filterText)) {
            filterText = "%%";
        } else {
            filterText = "%" + filterText.toUpperCase() + "%";
        }
        Page<Group> groupList;
        User user = userRepository.findOne(userId);

        if (doesListContainRole(roleRepository.findByUser(user), RoleName.GLOBAL_ADMIN)) {
            if (ArrayUtils.isNotEmpty(groupTypes)) {
                groupList = groupRepository.findAllByGroupType(filterText, groupTypesList, pageable);
            } else {
                groupList = groupRepository.findAll(filterText, pageable);
            }
        } else if (doesListContainRole(roleRepository.findByUser(user), RoleName.SPECIALTY_ADMIN)) {
            // if specialty admin get specialty group and all child groups
            if (ArrayUtils.isNotEmpty(groupTypes)) {
                groupList = groupRepository.findGroupAndChildGroupsByUserAndGroupType(filterText, groupTypesList,
                        user, pageable);
            } else {
                groupList = groupRepository.findGroupAndChildGroupsByUser(filterText, user, pageable);
            }
        }
        else {
            if (ArrayUtils.isNotEmpty(groupTypes)) {
                groupList = groupRepository.findGroupsByUserAndGroupTypeNoSpecialties(filterText, groupTypesList,
                        user, pageable);
            } else {
                groupList = groupRepository.findGroupsByUserNoSpecialties(filterText, user, pageable);
            }
        }

        if (groupList == null) {
            return new PageImpl<>(new ArrayList<org.patientview.api.model.Group>(), pageable, 0L);
        }

        // add parent and child groups
        List<Group> content = groupService.addParentAndChildGroups(groupList.getContent());

        // convert to lightweight transport objects, create Page and return
        List<org.patientview.api.model.Group> transportContent = convertGroupsToTransportGroups(content);

        return new PageImpl<>(transportContent, pageable, groupList.getTotalElements());
    }

    // TODO: this behaviour may need to be changed later to support cohorts and other parent type groups
    public Page<org.patientview.api.model.Group> getAllowedRelationshipGroups(Long userId) {

        boolean isGlobalAdmin = false;
        boolean isSpecialtyAdmin = false;
        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);

        List<Role> userRoles = getUserRoles(userId);
        for (Role role : userRoles) {
            if (role.getName().equals(RoleName.GLOBAL_ADMIN)) {
                isGlobalAdmin = true;
            }
            if (role.getName().equals(RoleName.SPECIALTY_ADMIN)) {
                isSpecialtyAdmin = true;
            }
        }

        if (isGlobalAdmin || isSpecialtyAdmin) {

            Page<Group> groupList = groupRepository.findAll("%%", new PageRequest(0, Integer.MAX_VALUE));

            // convert to lightweight transport objects, create Page and return
            List<org.patientview.api.model.Group> transportContent
                    = convertGroupsToTransportGroups(groupList.getContent());
            return new PageImpl<>(transportContent, pageable, groupList.getTotalElements());
        }

        return new PageImpl<>(new ArrayList<org.patientview.api.model.Group>(), pageable, 0L);
    }

    private boolean doesListContainRole(List<Role> roles, RoleName roleName) {
        for (Role role : roles) {
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }
}
