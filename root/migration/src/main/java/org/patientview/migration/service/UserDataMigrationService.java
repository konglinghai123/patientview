package org.patientview.migration.service;

import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UserDataMigrationService {

    void migrate(String groupCode) throws JsonMigrationException;

    void bulkUserCreate(String unitCode1, String unitCode2, Long count, RoleName roleName);
}
