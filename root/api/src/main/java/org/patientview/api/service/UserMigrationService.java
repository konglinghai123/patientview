package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.persistence.model.UserMigration;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserMigration service, only used by migration to get migration status, see who has been migrated etc.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 04/11/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UserMigrationService {

    // used by migration
    UserMigration save(UserMigration userMigration);

    // used by migration
    List<UserMigration> getByStatus(MigrationStatus migrationStatus);

    // used by migration
    @RoleOnly
    List<Long> getPatientview1IdsByStatus(MigrationStatus migrationStatus);

    // used by migration
    UserMigration getByPatientview1Id(Long patientview1Id);

    // used by migration
    UserMigration getByPatientview2Id(Long patientview2Id);

    // used by migration
    @RoleOnly
    List<Long> getPatientview2IdsByStatus(MigrationStatus migrationStatus);
}
