package org.patientview.persistence.repository;

import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface RoleRepository extends CrudRepository<Role, Long> {

    @Query("SELECT r " +
           "FROM Role r " +
           "WHERE r.level <= (SELECT MAX(r.level) FROM User u JOIN u.groupRoles gr JOIN gr.role r WHERE u.id = :userId)" +
           "AND  r.visible = true")
    public List<Role> findValidRolesByUser(@Param("userId") Long userId);

    @Query("SELECT r " +
           "FROM   Role r " +
           "WHERE  r.roleType.value = :roleType " +
           "AND    r.visible = true")
    public List<Role> findByRoleType(@Param("roleType") RoleType roleType);

    @Query("SELECT gr.role FROM User u JOIN u.groupRoles gr WHERE u = :user AND gr.role.visible = true")
    public List<Role> findByUser(@Param("user") User user);
}
