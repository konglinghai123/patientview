package org.patientview.api.service;

import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupService {

    @GroupMemberOnly
    Group findOne(Long id);

    List<Group> findAll();

    List<Group> findGroupByUser(User user);

    List<Group> findGroupAndChildGroupsByUser(User user);

    List<Group> findGroupByType(Long lookupId);

    Group save(Group group);

    Group create(Group group);

    GroupRole addGroupRole(Long userId, Long groupId, Long roleId);

    void addParentGroup(Long groupId, Long parentGroupId);

}
