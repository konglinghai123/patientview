package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.MigrationUser;

import javax.persistence.EntityExistsException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/11/2014
 */
public interface MigrationService {

    @RoleOnly
    Long migrateUser(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, MigrationException;

    @RoleOnly
    void migrateObservations(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, MigrationException;
}
