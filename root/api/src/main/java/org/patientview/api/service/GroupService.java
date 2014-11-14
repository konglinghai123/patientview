package org.patientview.api.service;

import org.patientview.api.annotation.AuditTrail;
import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.UnitRequest;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupService {

    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Group get(Long id) throws ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    List<Group> findAll();

    List<org.patientview.api.model.Group> findAllPublic();

    Group findByCode(String code);

    List<Group> findGroupByUser(User user);

    @AuditTrail(value = AuditActions.EDIT_GROUP, objectType = Group.class)
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(Group group) throws ResourceNotFoundException, EntityExistsException, ResourceForbiddenException;

    @AuditTrail(value = AuditActions.CREATE_GROUP, objectType = Group.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Long add(Group group);

    @AuditTrail(value = AuditActions.ADD_PARENT_GROUP, objectType = Group.class)
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addParentGroup(Long groupId, Long parentGroupId);

    @AuditTrail(value = AuditActions.REMOVE_PARENT_GROUP, objectType = Group.class)
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteParentGroup(Long groupId, Long parentGroupId);

    List<Group> findChildren(Long groupId) throws ResourceNotFoundException;

    @AuditTrail(value = AuditActions.ADD_CHILD_GROUP, objectType = Group.class)
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addChildGroup(Long groupId, Long childGroupId);

    @AuditTrail(value = AuditActions.REMOVE_CHILD_GROUP, objectType = Group.class)
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteChildGroup(Long groupId, Long childGroupId);

    @AuditTrail(value = AuditActions.ADD_FEATURE, objectType = Group.class)
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addFeature(Long groupId, Long featureId);

    @AuditTrail(value = AuditActions.REMOVE_FEATURE, objectType = Group.class)
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteFeature(Long groupId, Long featureId);

    void passwordRequest(Long groupId, UnitRequest unitRequest) throws ResourceNotFoundException;

    List<Group> addParentAndChildGroups(List<Group> groups);

    List<UUID> getOrganizationLogicalUuidsByCode(final String code) throws FhirResourceException;

    UUID addOrganization(Group group) throws FhirResourceException;
}
