package org.patientview.api.util;

import org.junit.Assert;
import org.junit.Test;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.StatisticType;
import org.patientview.test.util.TestUtils;
import org.patientview.util.Util;
import org.slf4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApiUtilTest {

    final static Logger LOG = org.slf4j.LoggerFactory.getLogger(ApiUtilTest.class);

    User creator;

    @org.junit.Before
    public void init() {
        creator = TestUtils.createUser("testCreator");
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
        List<Group> groupList = Util.convertIterable(groups);
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
            Group group = TestUtils.createGroup("testGroup");
            ((Set<Group>) groups).add(group);
        }

        List<Group> groupList = Util.convertIterable(groups);
        Assert.assertTrue("We now have an array list", groupList instanceof ArrayList);
        Assert.assertTrue("We have 10 results in our list", groupList.size() == sizeOfList);
    }

    // Create a list containing all the GroupStatistic types
    private List<GroupStatistic> createGroupStatistics(Group group) {
        List<GroupStatistic> groupStatistics = new ArrayList<>();

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.STATISTIC_TYPE);


        for (StatisticType statisticType : StatisticType.values()) {
            Lookup lookup = TestUtils.createLookup(lookupType, statisticType.name());
            GroupStatistic groupStatistic = TestUtils.createGroupStatistics(group, BigInteger.TEN, lookup);
            groupStatistics.add(groupStatistic);

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
        Group group = TestUtils.createGroup("testGroup");
        User user = TestUtils.createUser("testUser");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole( role, group, user);
        grantedAuthorities.add(groupRole);

        role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        groupRole = TestUtils.createGroupRole(role, group, user);
        grantedAuthorities.add(groupRole);

        role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        groupRole = TestUtils.createGroupRole(role, group, user);
        grantedAuthorities.add(groupRole);

        role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        groupRole = TestUtils.createGroupRole(role, group, user);
        grantedAuthorities.add(groupRole);

        List<Role> roles = ApiUtil.convertAuthorities(grantedAuthorities);

        Assert.assertTrue("The list is not empty", !CollectionUtils.isEmpty(roles));

    }

    /**
     * Test: Does the List of Roles contain the role
     */
    @Test
    public void testDoesRoleContain() {
        List<Role> roles = new ArrayList<>();
        roles.add(TestUtils.createRole(RoleName.PATIENT));
        roles.add(TestUtils.createRole(RoleName.UNIT_ADMIN));

        TestUtils.authenticateTest(TestUtils.createUser("testUser"), RoleName.PATIENT, RoleName.UNIT_ADMIN);

        Assert.assertFalse("The list does not contain the following role",
                ApiUtil.currentUserHasRole(RoleName.SPECIALTY_ADMIN));
        Assert.assertTrue("The list does not contain the following role", ApiUtil.currentUserHasRole(RoleName.PATIENT));

    }
}