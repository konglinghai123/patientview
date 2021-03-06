package org.patientview.persistence.repository;

import org.patientview.persistence.model.UserMigration;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/11/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface UserMigrationRepository extends CrudRepository<UserMigration, Long> {

    @Query("SELECT um FROM UserMigration um WHERE um.status = :status")
    List<UserMigration> findByStatus(@Param("status") MigrationStatus status);

    // modified for IBD, specific id
    @Query("SELECT um.patientview1UserId FROM UserMigration um WHERE um.status = :status AND um.id > 7281771")
    List<Long> findPatientview1IdsByStatus(@Param("status") MigrationStatus status);

    @Query("SELECT um FROM UserMigration um WHERE um.status <> :status")
    List<UserMigration> findByNotStatus(@Param("status") MigrationStatus status);

    @Query("SELECT um FROM UserMigration um WHERE um.patientview1UserId = :patientview1Id")
    UserMigration getByPatientview1Id(@Param("patientview1Id") Long patientview1Id);

    @Query("SELECT um FROM UserMigration um WHERE um.patientview2UserId = :patientview2Id")
    UserMigration getByPatientview2Id(@Param("patientview2Id") Long patientview2Id);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserMigration WHERE patientview2UserId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT um.patientview2UserId FROM UserMigration um WHERE um.status = :migrationStatus")
    List<Long> findPatientview2IdsByStatus(@Param("migrationStatus") MigrationStatus migrationStatus);
}
