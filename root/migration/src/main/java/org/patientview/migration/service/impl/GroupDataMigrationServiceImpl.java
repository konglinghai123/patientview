package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.patientview.migration.service.GroupDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.model.Unit;
import org.patientview.patientview.logging.AddLog;
import org.patientview.patientview.model.PatientCount;
import org.patientview.patientview.model.UnitStat;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupStatisticLookupValues;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.patientview.repository.FeatureDao;
import org.patientview.repository.PatientCountDao;
import org.patientview.repository.UnitDao;
import org.patientview.service.UnitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Service
public class GroupDataMigrationServiceImpl implements GroupDataMigrationService {

    private static final Logger LOG = LoggerFactory.getLogger(GroupDataMigrationServiceImpl.class);

    @Inject
    private UnitDao unitDao;

    @Inject
    private UnitManager unitManager;

    @Inject
    private FeatureDao featureDao;

    @Inject
    private PatientCountDao patientCountDao;

    private List<Group> groups;
    private List<Lookup> lookups;
    private List<Feature> features;

    private @Value("${migration.username}") String migrationUsername;
    private @Value("${migration.password}") String migrationPassword;
    private @Value("${patientview.api.url}") String patientviewApiUrl;
    private @Value("${jdbc.url}") String jdbcUrl;
    private @Value("${jdbc.username}") String jdbcUsername;
    private @Value("${jdbc.password}") String jdbcPassword;

    private void init() throws JsonMigrationException {
        try {
            JsonUtil.setPatientviewApiUrl(patientviewApiUrl);
            JsonUtil.token = JsonUtil.authenticate(migrationUsername, migrationPassword);
            lookups = JsonUtil.getStaticDataLookups(JsonUtil.pvUrl + "/lookup");
            features = JsonUtil.getStaticDataFeatures(JsonUtil.pvUrl + "/feature");
            groups = JsonUtil.getGroups(JsonUtil.pvUrl + "/group");
        } catch (JsonMigrationException e) {
            LOG.error("Could not authenticate {} ", e.getCause());
            throw new JsonMigrationException(e.getMessage());
        } catch (JsonMigrationExistsException e) {
            LOG.error("Could not authenticate {} ", e.getCause());
        }
    }

    public void createGroups() throws JsonMigrationException {

        init();

        for (Unit unit : unitDao.getAll(false)) {

            LOG.info(unit.getUnitcode() + ": starting group");

            // only copy units of the right sourcetype
            if (unit.getSourceType().equalsIgnoreCase("renalunit")
                    || unit.getSourceType().equalsIgnoreCase("diabetesunit")
                    || unit.getSourceType().equalsIgnoreCase("radargroup")) {

                // get group features (added individually with API call)
                Set<Feature> unitFeatures = getUnitFeatures(unit);

                // Create the unit
                Group group = createGroup(unit);

                // set group contact points (added to Group object)
                group.setContactPoints(createGroupContactPoints(unit));

                // set group additional locations (added to Group object)
                group.setLocations(createGroupLocations(unit));

                // call API to create unit, get the id
                Long groupId = callApiCreateGroup(group);

                if (groupId != null) {
                    group = callApiGetGroup(groupId);

                    // Create the features
                    if (CollectionUtils.isNotEmpty(unitFeatures)) {
                        for (Feature feature : unitFeatures) {
                            if (group != null) {
                                callApiCreateGroupFeature(group, feature);
                            }
                        }
                    }

                    // Assign a specialty (will likely give HTTP conflict as probably already exists)
                    Group parentGroup = getGroupParent(unit);
                    if (parentGroup != null && group != null) {
                        callApiCreateParentGroup(group, parentGroup);
                    } else {
                        LOG.error(unit.getUnitcode() + ": Unable to find parent group");
                    }
                }

                LOG.info(unit.getUnitcode() + ": finished group");
            }
        }
    }

    public void createStatistics() throws JsonMigrationException {
        init();

        Map<String, Lookup> statisticTypeMap = new HashMap<String, Lookup>();

        statisticTypeMap.put(AddLog.ADMIN_ADD,
                getLookupByName(GroupStatisticLookupValues.ADMIN_GROUP_ROLE_ADD_COUNT.toString()));
        statisticTypeMap.put(AddLog.EMAIL_CHANGED,
                getLookupByName(GroupStatisticLookupValues.EMAIL_CHANGED_COUNT.toString()));
        statisticTypeMap.put(AddLog.EMAIL_VERIFY,
                getLookupByName(GroupStatisticLookupValues.EMAIL_VERIFY_COUNT.toString()));
        statisticTypeMap.put(AddLog.LOGGED_ON,
                getLookupByName(GroupStatisticLookupValues.LOGGED_ON_COUNT.toString()));
        statisticTypeMap.put(AddLog.PASSWORD_CHANGE,
                getLookupByName(GroupStatisticLookupValues.PASSWORD_CHANGE_COUNT.toString()));
        statisticTypeMap.put(AddLog.PASSWORD_LOCKED,
                getLookupByName(GroupStatisticLookupValues.ACCOUNT_LOCKED_COUNT.toString()));
        statisticTypeMap.put(AddLog.PASSWORD_RESET,
                getLookupByName(GroupStatisticLookupValues.PASSWORD_RESET_COUNT.toString()));
        statisticTypeMap.put(AddLog.PASSWORD_RESET_FORGOTTEN,
                getLookupByName(GroupStatisticLookupValues.PASSWORD_RESET_FORGOTTEN_COUNT.toString()));
        statisticTypeMap.put(AddLog.PASSWORD_UNLOCKED,
                getLookupByName(GroupStatisticLookupValues.ACCOUNT_UNLOCKED_COUNT.toString()));
        statisticTypeMap.put(AddLog.PATIENT_ADD,
                getLookupByName(GroupStatisticLookupValues.PATIENT_GROUP_ROLE_ADD_COUNT.toString()));
        statisticTypeMap.put(AddLog.PATIENT_DATA_FAIL,
                getLookupByName(GroupStatisticLookupValues.PATIENT_DATA_FAIL_COUNT.toString()));
        statisticTypeMap.put(AddLog.PATIENT_DELETE,
                getLookupByName(GroupStatisticLookupValues.PATIENT_GROUP_ROLE_DELETE_COUNT.toString()));
        statisticTypeMap.put(AddLog.PATIENT_VIEW,
                getLookupByName(GroupStatisticLookupValues.PATIENT_VIEW_COUNT.toString()));
        statisticTypeMap.put("unique data load",
                getLookupByName(GroupStatisticLookupValues.UNIQUE_PATIENT_DATA_SUCCESS_COUNT.toString()));
        statisticTypeMap.put("unique logon",
                getLookupByName(GroupStatisticLookupValues.UNIQUE_LOGGED_ON_COUNT.toString()));

        // restricted group list, testing only
        List<Group> groupsToAdd = new ArrayList<Group>();
        Group groupToAdd = getGroupByCode("RRBBV");
        
        if (groupToAdd != null) {
            groupsToAdd.add(groupToAdd);
        }
        
        if(CollectionUtils.isEmpty(groupsToAdd)) {
            LOG.info("No groups found, not migrating statistics");
        }
        
        for (Group group : groupsToAdd) {
            if (!group.getCode().equals("Generic")) {
                List<GroupStatistic> statistics = new ArrayList<GroupStatistic>();
                List<UnitStat> unitStats = unitManager.getUnitStatsForUnit(group.getCode());

                if (CollectionUtils.isNotEmpty(unitStats)) {
                    for (UnitStat unitStat : unitStats) {
                        GroupStatistic groupStatistic = new GroupStatistic();
                        groupStatistic.setGroup(group);
                        groupStatistic.setStatisticPeriod(StatisticPeriod.MONTH);
                        groupStatistic.setValue(BigInteger.valueOf(unitStat.getCount()));

                        int year = Integer.parseInt(unitStat.getYearmonth().split("-")[0]);
                        int month = Integer.parseInt(unitStat.getYearmonth().split("-")[1]);

                        Calendar startDate = Calendar.getInstance();
                        startDate.set(Calendar.YEAR, year);
                        startDate.set(Calendar.MONTH, month - 1);  // zero based
                        startDate.set(Calendar.DAY_OF_MONTH, 1);
                        startDate.set(Calendar.HOUR_OF_DAY, 0);
                        startDate.set(Calendar.MINUTE, 0);
                        startDate.set(Calendar.SECOND, 0);
                        startDate.set(Calendar.MILLISECOND, 0);
                        groupStatistic.setStartDate(startDate.getTime());

                        if (month > Calendar.DECEMBER) {
                            year++;
                            month = Calendar.JANUARY;
                        }

                        Calendar endDate = Calendar.getInstance();
                        endDate.set(Calendar.YEAR, year);
                        endDate.set(Calendar.MONTH, month);  // zero based
                        endDate.set(Calendar.DAY_OF_MONTH, 1);
                        endDate.set(Calendar.HOUR_OF_DAY, 0);
                        endDate.set(Calendar.MINUTE, 0);
                        endDate.set(Calendar.SECOND, 0);
                        endDate.set(Calendar.MILLISECOND, 0);
                        groupStatistic.setEndDate(endDate.getTime());

                        if (statisticTypeMap.get(unitStat.getAction()) != null) {
                            groupStatistic.setStatisticType(statisticTypeMap.get(unitStat.getAction()));
                            statistics.add(groupStatistic);
                        }
                    }
                } else {
                    LOG.info(group.getCode() + ": group statistics empty");
                }

                List<PatientCount> patientCounts = patientCountDao.get(group.getCode(), "patient");

                if (CollectionUtils.isNotEmpty(patientCounts)) {
                    for (PatientCount patientCount : patientCounts) {
                        GroupStatistic groupStatistic = new GroupStatistic();
                        groupStatistic.setGroup(group);
                        groupStatistic.setStatisticPeriod(StatisticPeriod.MONTH);
                        groupStatistic.setValue(BigInteger.valueOf(patientCount.getCount()));
                        groupStatistic.setStatisticType(
                                getLookupByName(GroupStatisticLookupValues.PATIENT_COUNT.toString()));

                        int year = patientCount.getDatestamp().get(Calendar.YEAR);
                        int month = patientCount.getDatestamp().get(Calendar.MONTH);

                        Calendar startDate = Calendar.getInstance();
                        startDate.set(Calendar.YEAR, year);
                        startDate.set(Calendar.MONTH, month - 1);  // zero based
                        startDate.set(Calendar.DAY_OF_MONTH, 1);
                        startDate.set(Calendar.HOUR_OF_DAY, 0);
                        startDate.set(Calendar.MINUTE, 0);
                        startDate.set(Calendar.SECOND, 0);
                        startDate.set(Calendar.MILLISECOND, 0);
                        groupStatistic.setStartDate(startDate.getTime());

                        if (month > Calendar.DECEMBER) {
                            year++;
                            month = Calendar.JANUARY;
                        }

                        Calendar endDate = Calendar.getInstance();
                        endDate.set(Calendar.YEAR, year);
                        endDate.set(Calendar.MONTH, month);  // zero based
                        endDate.set(Calendar.DAY_OF_MONTH, 1);
                        endDate.set(Calendar.HOUR_OF_DAY, 0);
                        endDate.set(Calendar.MINUTE, 0);
                        endDate.set(Calendar.SECOND, 0);
                        endDate.set(Calendar.MILLISECOND, 0);
                        groupStatistic.setEndDate(endDate.getTime());

                        statistics.add(groupStatistic);
                    }
                } else {
                    LOG.info(group.getCode() + ": patient statistics empty");
                }

                if (CollectionUtils.isNotEmpty(statistics)) {
                    callApiCreateGroupStatistics(group, statistics);
                } else {
                    LOG.info(group.getCode() + ": statistics empty, not migrating");
                }
            }
        }
        
        LOG.info("Group statistics finished");
    }

    @Override
    public int getGroupCount(boolean nativeQuery) {
        if (!nativeQuery) {
            return unitDao.getAll(false).size();
        } else {
            Connection connection = null;
            String sql = "SELECT count(1) FROM unit";

            try {
                DataSource dataSource = new DriverManagerDataSource(jdbcUrl, jdbcUsername, jdbcPassword);
                connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery(sql);

                if (results.next()) {
                    return results.getInt(1);
                }
            } catch (SQLException se) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException se2) {
                        LOG.error(se2.getMessage());
                    }
                }
            }
        }

        return -1;
    }

    private void callApiCreateGroupStatistics(Group group, List<GroupStatistic> statistics) {
        String url = JsonUtil.pvUrl + "/group/" + group.getId() + "/migratestatistics/";

        try {
            JsonUtil.jsonRequest(url, null, statistics, HttpPost.class, false);
            LOG.info("Success: migrated statistics for " + group.getCode());
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to migrate statistics: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to migrate statistics: ", jee.getMessage());
        } catch (Exception e) {
            LOG.error("Unable to migrate statistics: ", e.getMessage());
        }
    }

    private void callApiCreateParentGroup(Group group, Group parentGroup) {
        String featureUrl = JsonUtil.pvUrl + "/group/" + group.getId() + "/parent/" + parentGroup.getId();

        try {
            JsonUtil.jsonRequest(featureUrl, GroupRole.class, null, HttpPut.class, true);
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to parent group: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to parent group: ", jee.getMessage());
        } catch (Exception e) {
            LOG.error("Unable to parent group: ", e.getMessage());
        }
    }

    private GroupFeature callApiCreateGroupFeature(Group group, Feature feature) {

        String featureUrl = JsonUtil.pvUrl + "/group/" + group.getId() + "/features/" + feature.getId();

        try {
            return JsonUtil.jsonRequest(featureUrl, GroupFeature.class, null, HttpPut.class, true);
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create group feature: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("Could not create group feature for {}", group.getName());
        } catch (Exception jee) {
            LOG.info("Could not create group feature for {}", group.getName());
        }

        LOG.info("Success: feature created for group");

        return null;
    }

    private Long callApiCreateGroup(Group group) {
        Long newGroupId = null;
        try {
            newGroupId = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group", Long.class, group, HttpPost.class, true);
            LOG.info("Created group");
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create group: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("Group {} already exists", group.getName());
        }

        return newGroupId;
    }

    private Group callApiGetGroup(Long groupId) {
        Group newGroup = null;
        try {
            newGroup = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group/" + groupId, Group.class, null, HttpGet.class, true);
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to get group: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to get group: ", jee.getMessage());
        }

        return newGroup;
    }

    private ContactPointType callApiGetType(String type) {
        ContactPointType newLink = null;
        String url = JsonUtil.pvUrl + "/contactpoint/type/" + type;
        try {
            newLink = JsonUtil.jsonRequest(url, ContactPointType.class, null , HttpGet.class, true);
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to get contact point: ", jme.getMessage());
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to get contact point: ", jee.getMessage());
        }

        return newLink;
    }

    private void sendDummyUnit() {

        // Export a dummy group to test hibernate until one works
        // FIXME need hibernate to read the table indexes on startup
        Group group = new Group();
        group.setName("TEST_GROUP");
        group.setCode("TEST");
        group.setGroupType(getLookupByName("UNIT"));

        int i = 0;
        while (i < 10) {
            try {
                group = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group", Group.class, group, HttpPost.class, true);
                // Delete the test group once we have successfully created one
                group = JsonUtil.jsonRequest(JsonUtil.pvUrl + "/group/" + group.getId(), null, null, HttpDelete.class, true);
                break;
            } catch (JsonMigrationException jme) {
                LOG.trace("Unable to create group: ", jme.getMessage());
            } catch (JsonMigrationExistsException jee) {
                LOG.trace("Group {} already exists", group.getName());
            }
            i++;
        }
    }

    public Set<Feature> getUnitFeatures(Unit unit) {
        Set<Feature> unitFeatures = new HashSet<Feature>();

        /*if (unit.isSharedThoughtEnabled()) {
            unitFeatures.add(getFeatureByName(FeatureType.SHARING_THOUGHTS.toString()));
        }*/

        if (unit.isFeedbackEnabled()) {
            unitFeatures.add(getFeatureByName(FeatureType.FEEDBACK.toString()));
        }

        if (featureDao.getUnitsForFeature("messaging").contains(unit)) {
            unitFeatures.add(getFeatureByName(FeatureType.MESSAGING.toString()));
        }

        if (unit.isEcrEnabled()) {
            unitFeatures.add(getFeatureByName(FeatureType.GP_MEDICATION.toString()));
        }

        return  unitFeatures;
    }

    private Lookup getLookupByName(String value) {
        for (Lookup lookup : lookups) {
            if (lookup.getValue().equalsIgnoreCase(value)) {
                return  lookup;
            }
        }
        return null;
    }

    private Group getGroupByCode(String code) {
        for (Group group : groups) {
            if (group.getCode().equalsIgnoreCase(code)) {
                return group;
            }
        }
        return null;
    }

    private Group getGroupByName(String name) {
        for (Group group : groups) {
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }
        return null;
    }

    private Feature getFeatureByName(String value) {
        for (Feature feature : features) {
            if (feature.getName().equalsIgnoreCase(value)) {
                return feature;
            }
        }
        return null;
    }

    private Set<ContactPoint> createGroupContactPoints(Unit unit) {
        Set<ContactPoint> contactPoints = new HashSet<ContactPoint>();

        // unit.uniturl
        if (StringUtils.isNotEmpty(unit.getUniturl())) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.UNIT_WEB_ADDRESS.toString()));
            contactPoint.setContent(unit.getUniturl());
            contactPoints.add(contactPoint);
        }

        // unit.trusturl
        if (StringUtils.isNotEmpty(unit.getTrusturl())) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.TRUST_WEB_ADDRESS.toString()));
            contactPoint.setContent(unit.getTrusturl());
            contactPoints.add(contactPoint);
        }

        // unit.renaladminname
        if (StringUtils.isNotEmpty(unit.getRenaladminname())) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.PV_ADMIN_NAME.toString()));
            contactPoint.setContent(unit.getRenaladminname());
            contactPoints.add(contactPoint);
        }

        // unit.renaladminphone
        if (StringUtils.isNotEmpty(unit.getRenaladminphone())) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.PV_ADMIN_PHONE.toString()));
            contactPoint.setContent(unit.getRenaladminphone());
            contactPoints.add(contactPoint);
        }

        // unit.renaladminemail
        if (StringUtils.isNotEmpty(unit.getRenaladminemail())) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.PV_ADMIN_EMAIL.toString()));
            contactPoint.setContent(unit.getRenaladminemail());
            contactPoints.add(contactPoint);
        }

        // unit.unitenquiriesphone
        if (StringUtils.isNotEmpty(unit.getUnitenquiriesphone())) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.UNIT_ENQUIRIES_PHONE.toString()));
            contactPoint.setContent(unit.getUnitenquiriesphone());
            contactPoints.add(contactPoint);
        }

        // unit.unitenquiriesemail
        if (StringUtils.isNotEmpty(unit.getUnitenquiriesemail())) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.UNIT_ENQUIRIES_EMAIL.toString()));
            contactPoint.setContent(unit.getUnitenquiriesemail());
            contactPoints.add(contactPoint);
        }

        // unit.appointmentphone
        if (StringUtils.isNotEmpty(unit.getAppointmentphone())) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.APPOINTMENT_PHONE.toString()));
            contactPoint.setContent(unit.getAppointmentphone());
            contactPoints.add(contactPoint);
        }

        // unit.appointmentemail
        if (StringUtils.isNotEmpty(unit.getAppointmentemail())) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.APPOINTMENT_EMAIL.toString()));
            contactPoint.setContent(unit.getAppointmentemail());
            contactPoints.add(contactPoint);
        }

        // unit.outofhours
        if (StringUtils.isNotEmpty(unit.getOutofhours())) {
            ContactPoint contactPoint = new ContactPoint();
            contactPoint.setContactPointType(callApiGetType(ContactPointTypes.OUT_OF_HOURS_INFO.toString()));
            contactPoint.setContent(unit.getOutofhours());
            contactPoints.add(contactPoint);
        }

        return contactPoints;
    }

    private Set<Location> createGroupLocations(Unit unit) {
        Set<Location> locations = new HashSet<Location>();

        String haemoUnit = "Haemodialysis Unit";
        String peritoUnit = "Peritoneal Dialysis Location";

        // unit.peritonealdialysisphone
        if (StringUtils.isNotEmpty(unit.getPeritonealdialysisphone())
                || StringUtils.isNotEmpty(unit.getPeritonealdialysisemail())) {

            Location location = new Location();
            location.setLabel(peritoUnit);
            location.setName("");
            if (StringUtils.isNotEmpty(unit.getPeritonealdialysisphone())) {
                location.setPhone(unit.getPeritonealdialysisphone());
            }
            if (StringUtils.isNotEmpty(unit.getPeritonealdialysisemail())) {
                location.setEmail(unit.getPeritonealdialysisemail());
            }
            locations.add(location);
        }

        // unit.haemodialysisunit 1
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname1())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname1());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone1())) {
                location.setPhone(unit.getHaemodialysisunitphone1());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation1())) {
                location.setAddress(unit.getHaemodialysisunitlocation1());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl1())) {
                location.setWeb(unit.getHaemodialysisuniturl1());
            }
            locations.add(location);
        }

        // unit.haemodialysisunit 2
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname2())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname2());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone2())) {
                location.setPhone(unit.getHaemodialysisunitphone2());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation2())) {
                location.setAddress(unit.getHaemodialysisunitlocation2());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl2())) {
                location.setWeb(unit.getHaemodialysisuniturl2());
            }
            locations.add(location);
        }

        // unit.haemodialysisunit 3
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname3())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname3());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone3())) {
                location.setPhone(unit.getHaemodialysisunitphone3());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation3())) {
                location.setAddress(unit.getHaemodialysisunitlocation3());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl3())) {
                location.setWeb(unit.getHaemodialysisuniturl3());
            }
            locations.add(location);
        }

        // unit.haemodialysisunit 4
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname4())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname4());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone4())) {
                location.setPhone(unit.getHaemodialysisunitphone4());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation4())) {
                location.setAddress(unit.getHaemodialysisunitlocation4());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl4())) {
                location.setWeb(unit.getHaemodialysisuniturl4());
            }
            locations.add(location);
        }
        
        // unit.haemodialysisunit 5
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname5())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname5());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone5())) {
                location.setPhone(unit.getHaemodialysisunitphone5());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation5())) {
                location.setAddress(unit.getHaemodialysisunitlocation5());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl5())) {
                location.setWeb(unit.getHaemodialysisuniturl5());
            }
            locations.add(location);
        }
        
        // unit.haemodialysisunit 6
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname6())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname6());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone6())) {
                location.setPhone(unit.getHaemodialysisunitphone6());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation6())) {
                location.setAddress(unit.getHaemodialysisunitlocation6());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl6())) {
                location.setWeb(unit.getHaemodialysisuniturl6());
            }
            locations.add(location);
        }
        
        // unit.haemodialysisunit 7
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname7())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname7());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone7())) {
                location.setPhone(unit.getHaemodialysisunitphone7());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation7())) {
                location.setAddress(unit.getHaemodialysisunitlocation7());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl7())) {
                location.setWeb(unit.getHaemodialysisuniturl7());
            }
            locations.add(location);
        }
        
        // unit.haemodialysisunit 8
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname8())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname8());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone8())) {
                location.setPhone(unit.getHaemodialysisunitphone8());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation8())) {
                location.setAddress(unit.getHaemodialysisunitlocation8());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl8())) {
                location.setWeb(unit.getHaemodialysisuniturl8());
            }
            locations.add(location);
        }
        
        // unit.haemodialysisunit 9
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname9())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname9());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone9())) {
                location.setPhone(unit.getHaemodialysisunitphone9());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation9())) {
                location.setAddress(unit.getHaemodialysisunitlocation9());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl9())) {
                location.setWeb(unit.getHaemodialysisuniturl9());
            }
            locations.add(location);
        }
        
        // unit.haemodialysisunit 3
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname3())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname3());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone3())) {
                location.setPhone(unit.getHaemodialysisunitphone3());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation3())) {
                location.setAddress(unit.getHaemodialysisunitlocation3());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl3())) {
                location.setWeb(unit.getHaemodialysisuniturl3());
            }
            locations.add(location);
        }
        
        // unit.haemodialysisunit 10
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname10())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname10());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone10())) {
                location.setPhone(unit.getHaemodialysisunitphone10());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation10())) {
                location.setAddress(unit.getHaemodialysisunitlocation10());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl10())) {
                location.setWeb(unit.getHaemodialysisuniturl10());
            }
            locations.add(location);
        }
        
        // unit.haemodialysisunit 11
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname11())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname11());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone11())) {
                location.setPhone(unit.getHaemodialysisunitphone11());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation11())) {
                location.setAddress(unit.getHaemodialysisunitlocation11());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl11())) {
                location.setWeb(unit.getHaemodialysisuniturl11());
            }
            locations.add(location);
        }
        
        // unit.haemodialysisunit 12
        if (StringUtils.isNotEmpty(unit.getHaemodialysisunitname12())) {
            Location location = new Location();
            location.setLabel(haemoUnit);
            location.setName(unit.getHaemodialysisunitname12());
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitphone12())) {
                location.setPhone(unit.getHaemodialysisunitphone12());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisunitlocation12())) {
                location.setAddress(unit.getHaemodialysisunitlocation12());
            }
            if (StringUtils.isNotEmpty(unit.getHaemodialysisuniturl12())) {
                location.setWeb(unit.getHaemodialysisuniturl12());
            }
            locations.add(location);
        }

        return locations;
    }

    private Group createGroup(Unit unit) {
        Group group = new Group();

        // basic details
        group.setName(unit.getName());
        group.setShortName(unit.getShortname());
        group.setCode(unit.getUnitcode().toUpperCase());
        group.setVisibleToJoin(unit.isVisible());
        group.setVisible(true);

        // group type
        if (unit.getSourceType().equalsIgnoreCase("renalunit")
                || unit.getSourceType().equalsIgnoreCase("diabetesunit")) {
            group.setGroupType(getLookupByName(GroupTypes.UNIT.toString()));
        } else if (unit.getSourceType().equalsIgnoreCase("radargroup")) {
            group.setGroupType(getLookupByName(GroupTypes.DISEASE_GROUP.toString()));
        }

        // address
        if (StringUtils.isNotEmpty(unit.getAddress1())) {
            group.setAddress1(unit.getAddress1());
        }
        if (StringUtils.isNotEmpty(unit.getAddress2())) {
            group.setAddress2(unit.getAddress2());
        }
        if (StringUtils.isNotEmpty(unit.getAddress3())) {
            group.setAddress3(unit.getAddress3());
        }
        if (StringUtils.isNotEmpty(unit.getPostcode())) {
            group.setPostcode(unit.getPostcode());
        }

        return group;
    }

    private Group getGroupParent(Unit unit) {
        if (unit.getSpecialty().getContext().equalsIgnoreCase("renal")) {
            return getGroupByName("RENAL");
        }
        if (unit.getSpecialty().getContext().equalsIgnoreCase("diabetes")) {
            return getGroupByName("DIABETES");
        }

        return null;
    }
}
