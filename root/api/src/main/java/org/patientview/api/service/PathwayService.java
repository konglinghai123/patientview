package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.Pathway;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pathway service used for managing pathway for patients.
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface PathwayService {

    /**
     * Update Pathway for a User
     *
     * @param userId  an ID of User to update the Pathway for
     * @param pathway a Pathway containing updated properties
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN,
            RoleName.SPECIALTY_ADMIN, RoleName.GLOBAL_ADMIN, RoleName.GP_ADMIN})
    void updatePathway(Long userId, Pathway pathway)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Pathway fo a User, given the PathwayTypes type of Pathway.
     *
     * @param userId      an ID of User to retrieve Alerts for
     * @param pathwayType a Type of the Pathway, currently only PathwayTypes.DONORPATHWAY
     * @return a Pathway of a type PathwayTypes
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN,
            RoleName.SPECIALTY_ADMIN, RoleName.GLOBAL_ADMIN, RoleName.GP_ADMIN, RoleName.PATIENT})
    Pathway getPathway(Long userId, PathwayTypes pathwayType)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Initialise a Pathway for given user.
     *
     * @param user ID of User to change the Alert preferences for
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = {RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN,
            RoleName.SPECIALTY_ADMIN, RoleName.GLOBAL_ADMIN, RoleName.GP_ADMIN})
    void setupPathway(User user) throws ResourceNotFoundException;
}
