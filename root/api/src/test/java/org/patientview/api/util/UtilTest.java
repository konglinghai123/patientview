package org.patientview.api.util;

import org.junit.Assert;
import org.junit.Test;
import org.patientview.api.controller.model.GroupStatisticTO;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.StatisticType;
import org.patientview.test.util.TestUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UtilTest {

    User creator;

    @org.junit.Before
    public void init() {
        creator = TestUtils.createUser(1L, "testCreator");
    }


    /**
     * Test: Converting an empty Iterable to an ArrayList
     * Fail: This does not work/ does not return an ArrayList
     *
     * @throws Exception
     */
    @Test
    public void testIterableToList_emptyResult() throws Exception {
        Iterable<Group> groups = new HashSet<>();
        List<Group> groupList = Util.iterableToList(groups);
        Assert.assertTrue("We now have an array list", groupList instanceof ArrayList);
    }

    /**
     * Test: Create a Iterable with results and convert the result to a ArrayList
     * Fail: Not all the result are returned
     *
     * @throws Exception
     */
    @Test
    public void testIterableToList_notNullResult() throws Exception {
        Iterable<Group> groups = new HashSet<>();

        long sizeOfList = 10;

        for (long l = 1; l <= sizeOfList; l++) {
            Group group = TestUtils.createGroup(l, "testGroup", creator);
            ((Set<Group>) groups).add(group);
        }

        List<Group> groupList = Util.iterableToList(groups);
        Assert.assertTrue("We now have an array list", groupList instanceof ArrayList);
        Assert.assertTrue("We have 10 results in our list", groupList.size() == sizeOfList);
    }

    public void testGetRoles() throws Exception {

    }

    public void testGetRolesFromAnnotation() throws Exception {

    }

    /**
     * Test: Convert the statistics into viewed bean
     * Fail: The values are not populated correctly
     *
     */
    @Test
    public void convertStatisticListToModelObject() {
        Group testGroup = TestUtils.createGroup(1L, "testGroup", creator);
        List<GroupStatistic> groupStatistics = createGroupStatistics(testGroup);
        Collection<GroupStatisticTO> groupStatisticTOs = Util.convertGroupStatistics(groupStatistics);

        // Get the only statistic
        GroupStatisticTO groupStatisticTO = (GroupStatisticTO) groupStatisticTOs.toArray()[0];

        // Make sure the count is set to ten for all the properties
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfPatients() == BigInteger.TEN);
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfUniqueLogons() == BigInteger.TEN);
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfImportFails() == BigInteger.TEN);
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfImportLoads() == BigInteger.TEN);
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfLogons() == BigInteger.TEN);
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfUniqueLogons() == BigInteger.TEN);
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfPasswordChanges() == BigInteger.TEN);
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfAccountLocks() == BigInteger.TEN);
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfPatientViews() == BigInteger.TEN);
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfPatientRemoves() == BigInteger.TEN);
        Assert.assertTrue("The count should be 10", groupStatisticTO.getCountOfPatientDeletes() == BigInteger.TEN);


    }

    // Create a list containing all the GroupStatistic types
    private List<GroupStatistic> createGroupStatistics(Group group) {
        List<GroupStatistic> groupStatistics = new ArrayList<>();

        LookupType lookupType = TestUtils.createLookupType(2L, LookupTypes.STATISTIC_TYPE, creator);

        int i = 0;
        for (StatisticType statisticType : StatisticType.values()) {
            Lookup lookup = TestUtils.createLookup(Long.valueOf(i),lookupType, statisticType.name(), creator);
            GroupStatistic groupStatistic = TestUtils.createGroupStatistics(group, BigInteger.TEN, lookup);
            groupStatistics.add(groupStatistic);
            i++;
        }

        return groupStatistics;

    }

    /**
     * Test: The conversion of roles to GrantedAuthorities
     * Fail: Anything but a list of roles from a list of GrantAuthorities
     */
    @Test
    public void testConvertAuthorities() {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(TestUtils.createRole(1L, RoleName.GLOBAL_ADMIN, creator));
        grantedAuthorities.add(TestUtils.createRole(1L, RoleName.UNIT_ADMIN, creator));
        grantedAuthorities.add(TestUtils.createRole(1L, RoleName.SPECIALTY_ADMIN, creator));

        List<Role> roles = Util.convertAuthorities(grantedAuthorities);

        Assert.assertTrue("The list is not empty", !CollectionUtils.isEmpty(roles));

    }

    /**
     * Test: Does the List of Roles contain the role
     */
    public void testDoesRoleContain() {
        List<Role> roles = new ArrayList<>();
        roles.add(TestUtils.createRole(1L, RoleName.PATIENT, creator));
        roles.add(TestUtils.createRole(1L, RoleName.UNIT_ADMIN, creator));

        Assert.assertFalse("The list does not contain the following role", Util.doesContainRole(roles, RoleName.SPECIALTY_ADMIN));
        Assert.assertTrue("The list does not contain the following role", Util.doesContainRole(roles, RoleName.PATIENT));

    }
}