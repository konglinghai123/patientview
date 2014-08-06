package org.patientview;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.enums.Roles;
import org.patientview.migration.service.AdminDataMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;


/**
 * Created by james@solidstategroup.com
 * Created on 04/06/2014
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:source-repository.xml")
public class AdminDataMigrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AdminDataMigrationTest.class);

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    /**
     * Order(1) Migrates all the static data like groups, specialities
     *
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(false)
    public void testStaticDataMigrationFeatures()  throws Exception {
        LOG.info("Starting migration");
        adminDataMigrationService.migrate();
        Assert.assertNotNull("This group should not be null", adminDataMigrationService.getLookupByName("UNIT"));
        Assert.assertNotNull("This feature should not be null", adminDataMigrationService.getFeatureByName("SHARING_THOUGHTS"));
        Assert.assertNotNull("This feature should not be null", adminDataMigrationService.getRoleByName(Roles.PATIENT));
        Assert.assertNotNull("This feature should not be null", adminDataMigrationService.getRoleByName(Roles.UNIT_ADMIN));
        Assert.assertNotNull("This feature should not be null", adminDataMigrationService.getRoleByName(Roles.STAFF_ADMIN));
    }


}
