package org.patientview.api.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.patientview.api.service.GroupService;
import org.patientview.api.util.ApiUtil;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 25/07/2014
 *
 * http://stackoverflow.com/questions/3271659/use-enum-type-as-a-value-parameter-for-rolesallowed-annotation
 *
 * Responsible for security resource via annotations.
 *
 */
@Aspect
@Configurable
public final class SecurityAspect {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAspect.class);

    private static SecurityAspect instance;

    @Inject
    private GroupService groupService;

    @Inject
    private GroupRepository groupRepository;

    private SecurityAspect() {
        LOG.info("Security Aspect Initialised");
    }

    @PostConstruct
    public void init() {
        if (groupService == null || groupRepository == null) {
            throw new IllegalStateException("Injection failed for aspect");
        } else {
            LOG.info("Security aspect started correctly");
        }
    }

    /**
     * Check if current User has a certain Role.
     * @param joinPoint Join point of aspect, defined by @RoleOnly annotation on service method
     * @throws ResourceForbiddenException
     */
    @Before("@annotation(org.patientview.api.annotation.RoleOnly)")
    public void checkHasRole(JoinPoint joinPoint) throws ResourceForbiddenException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Cannot validate when security has not been initialised
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("The request must be authenticated");
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Cannot validate without the principal
        if (user == null) {
            throw new SecurityException("The user must be authenticated");
        }

        RoleName[] roles = ApiUtil.getRoles(joinPoint);

        if (roles != null && (ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN) || ApiUtil.currentUserHasRole(roles))) {
            LOG.debug("User has passed role validation");
        } else {
            throw new ResourceForbiddenException("The user does not have the required role");
        }

        LOG.debug("PointCut");
    }

    /**
     * Check if current User is a member of a certain Group.
     * @param joinPoint Join point of aspect, defined by @GroupMemberOnly annotation on service method
     * @throws ResourceForbiddenException
     */
    @Before("@annotation(org.patientview.api.annotation.GroupMemberOnly)")
    public void checkGroupMembership(JoinPoint joinPoint) throws ResourceForbiddenException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Cannot validate when security has not been initialised
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceForbiddenException("The request must be authenticated");
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Cannot validate without the principal
        if (user == null) {
            throw new ResourceForbiddenException("The user must be authenticated");
        }

        // Refactor later into two different PointCuts - one for groupIds being passed, another for whole groups
        Long groupId = getId(joinPoint);
        //LOG.info("groupId: " + groupId);

        if (groupId == null) {
            Group group = getGroup(joinPoint);
            //LOG.info("group null?: " + (group == null));
            if (group != null) {
                groupId = group.getId();
            }
        }

        if (groupId == null) {
            LOG.error("Cannot validate against a group that does not exist");
            return;
        }

        RoleName[] roles = ApiUtil.getRoles(joinPoint);

        if (ApiUtil.currentUserHasRole(RoleName.GLOBAL_ADMIN)
                || ApiUtil.doesContainChildGroupAndRole(groupId, RoleName.SPECIALTY_ADMIN)
                || ApiUtil.doesContainGroupAndRole(groupId, roles)) {
            LOG.debug("User has passed group validation");
        } else {
            throw new ResourceForbiddenException("Failed group validation");
        }

        LOG.debug("PointCut");
    }

    /**
     * Check if User being retrieved or modified is the current User, typically used for service methods where only the
     * current user has permission to modify or view their own details.
     * @param joinPoint Join point of aspect, defined by @GroupMemberOnly annotation on service method
     * @throws ResourceForbiddenException
     */
    @Before("@annotation(org.patientview.api.annotation.UserOnly)")
    public void checkUser(JoinPoint joinPoint) throws ResourceForbiddenException {
        Long requestId = getId(joinPoint);
        Long userId = ApiUtil.getUser().getId();
        if (!requestId.equals(userId)) {
            LOG.debug("User with id: {id} should not be accessing the resource for user id {}", userId, requestId);
            throw new ResourceForbiddenException("You have to be logged in as the requested user");
        }

    }

    /**
     * Get ID from join point parameters, currently used to retrieve Group or User ID and assumes the annotation is
     * applied to a method with a Group or User ID.
     * @param joinPoint Join point of aspect, typically on service method
     * @return Long ID of Group or User from join point parameters
     */
    // TODO the next two methods can be fixed up with annotations on the parameters
    private Long getId(JoinPoint joinPoint) {
        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof Long) {
                return (Long) argument;
            }
        }
        return null;
    }

    /**
     * Get the Group as retrieved from join point. Assuming we apply the annotation to a method with a Group.
     * @param joinPoint Join point of aspect
     * @return Group retrieved from join point parameters
     */
    private Group getGroup(JoinPoint joinPoint) {
        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof Group) {
                return (Group) argument;
            }
        }
        return null;
    }

    public static SecurityAspect aspectOf() {
        if (instance == null) {
            instance = new SecurityAspect();
        }
        return instance;
    }
}
