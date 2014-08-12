package org.patientview.api.service.impl;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.GroupService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.repository.ContactPointRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LocationRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Service
public class GroupServiceImpl extends AbstractServiceImpl<GroupServiceImpl> implements GroupService {

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private GroupFeatureRepository groupFeatureRepository;

    @Inject
    private LocationRepository locationRepository;

    @Inject
    private LinkRepository linkRepository;

    @Inject
    private ContactPointRepository contactPointRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupRelationshipRepository groupRelationshipRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private EntityManager entityManager;


    /**
     * Get all the groups and put the children and parents into the transient objects
     *
     * @return
     */
    public List<Group> findAll() {

        List<Group> groups = Util.iterableToList(groupRepository.findAll());

        return addParentAndChildGroups(groups);

    }

    public Group get(Long id) {
        return addSingleParentAndChildGroup(groupRepository.findOne(id));
    }

    public List<Group> findGroupByUser(User user) {
        List<Group> groups = Util.iterableToList(groupRepository.findGroupByUser(user));
        return addParentAndChildGroups(groups);
    }

    public List<Group> findGroupAndChildGroupsByUser(User user) {

        Lookup childRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "CHILD");
        Set<Group> groups = new HashSet<Group>();
        // get list of groups associated with user directly (user is member of group)
        groups.addAll(Util.iterableToList(groupRepository.findGroupByUser(user)));
        // for each group get list of children if present
        for (Group group : groups) {
            for (GroupRelationship groupRelationship : group.getGroupRelationships()) {
                if (groupRelationship.getRelationshipType() == RelationshipTypes.CHILD) {
                    groups.add(groupRelationship.getObjectGroup());
                }
            }
        }

        return addParentAndChildGroups(new ArrayList<Group>(groups));
    }

    public List<Group> findGroupByType(Long lookupId) {
        Lookup groupType = lookupRepository.findOne(lookupId);
        List<Group> groups = Util.iterableToList(groupRepository.findGroupByType(groupType));
        return addParentAndChildGroups(groups);
    }

    /**
     * @param group
     * @return
     */
    public Group save(Group group) throws ResourceNotFoundException {
        Group entityGroup = groupRepository.findOne(group.getId());

        if (entityGroup == null) {
            throw new ResourceNotFoundException(String.format("Could not find group %s", group.getId()));
        }

        entityGroup.setCode(group.getCode());
        entityGroup.setName(group.getName());
        entityGroup.setGroupType(group.getGroupType());
        entityGroup.setSftpUser(group.getSftpUser());
        entityGroup.setAddress1(group.getAddress1());
        entityGroup.setAddress2(group.getAddress2());
        entityGroup.setAddress3(group.getAddress3());
        entityGroup.setPostcode(group.getPostcode());
        entityGroup.setVisibleToJoin(group.getVisibleToJoin());
        return groupRepository.save(entityGroup);
    }

    /**
     * TODO remove links, relationships, locations, and features SPRINT 2
     *
     * @param group
     * @return
     * @throws javax.persistence.EntityExistsException
     */
    public Group add (Group group) throws EntityExistsException {
        Group newGroup;

        Set<Link> links;
        // get links and features, avoid persisting until group created successfully
        if (!CollectionUtils.isEmpty(group.getLinks())) {
            links = new HashSet<Link>(group.getLinks());
            group.getLinks().clear();
        } else {
            links = new HashSet<Link>();
        }

        Set<Location> locations;
        if (!CollectionUtils.isEmpty(group.getLocations())) {
            locations = new HashSet<Location>(group.getLocations());
            group.getLocations().clear();
        } else {
            locations = new HashSet<Location>();
        }

        Set<GroupFeature> groupFeatures;
        if (!CollectionUtils.isEmpty(group.getGroupFeatures())) {
            groupFeatures = new HashSet<GroupFeature>(group.getGroupFeatures());
            group.getGroupFeatures().clear();
        } else {
            groupFeatures = new HashSet<GroupFeature>();
        }

        Set<ContactPoint> contactPoints;
        if (!CollectionUtils.isEmpty(group.getContactPoints())) {
            contactPoints = new HashSet<ContactPoint>(group.getContactPoints());
            group.getContactPoints().clear();
        } else {
            contactPoints = new HashSet<ContactPoint>();
        }


        // save basic details
        try {
            newGroup = groupRepository.save(group);
        } catch (DataIntegrityViolationException dve) {
            LOG.debug("Group not created, duplicate: {}", dve.getCause());
            throw new EntityExistsException("Group already exists");
        }

        // Group Relationships
        saveGroupRelationships(newGroup);

        // save links
        for (Link link : links) {
            link.setGroup(newGroup);
            link = linkRepository.save(link);
            newGroup.getLinks().add(link);
        }

        // save locations
        for (Location location : locations) {
            location.setGroup(newGroup);
            location = locationRepository.save(location);
            newGroup.getLocations().add(location);
        }

        // save features
        for (GroupFeature groupFeature : groupFeatures) {
            GroupFeature tempGroupFeature = new GroupFeature();
            tempGroupFeature.setFeature(featureRepository.findOne(groupFeature.getFeature().getId()));
            tempGroupFeature.setGroup(newGroup);
            tempGroupFeature.setCreator(userRepository.findOne(1L));
            tempGroupFeature = groupFeatureRepository.save(tempGroupFeature);
            newGroup.getGroupFeatures().add(tempGroupFeature);
        }

        // save contact points
        for (ContactPoint contactPoint : contactPoints) {
            ContactPoint tempContactPoint = new ContactPoint();
            tempContactPoint.setGroup(newGroup);
            tempContactPoint.setCreator(userRepository.findOne(1L));
            tempContactPoint.setContactPointType(entityManager.find(ContactPointType.class,contactPoint.getContactPointType().getId()));
            tempContactPoint.setContent(contactPoint.getContent());
            tempContactPoint = contactPointRepository.save(tempContactPoint);
            newGroup.getContactPoints().add(tempContactPoint);
        }

        // return new group with parents/children for front end to avoid recursion
        return addSingleParentAndChildGroup(newGroup);
    }

    public GroupRole addGroupRole(Long userId, Long groupId, Long roleId) {
        GroupRole groupRole = new GroupRole();
        groupRole.setUser(userRepository.findOne(userId));
        groupRole.setGroup(groupRepository.findOne(groupId));
        groupRole.setRole(roleRepository.findOne(roleId));
        groupRole.setCreator(userRepository.findOne(1L));
        return groupRoleRepository.save(groupRole);
    }

    public void deleteGroupRole(Long userId, Long groupId, Long roleId) {
        groupRoleRepository.delete(groupRoleRepository.findByUserGroupRole(
                userRepository.findOne(userId), groupRepository.findOne(groupId), roleRepository.findOne(roleId)
        ));
    }

    private void saveGroupRelationships(Group group) {

        Lookup parentRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "PARENT");
        Lookup childRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "CHILD");

        // delete existing groups
        groupRelationshipRepository.deleteBySourceGroup(group);

        Group sourceGroup = groupRepository.findOne(group.getId());

        // Create a two way relationship; if a parent is a child, the inverse is also true
        if (!CollectionUtils.isEmpty(group.getParentGroups())) {
            for (Group parentGroup : group.getParentGroups()) {

                Group objectGroup = groupRepository.findOne(parentGroup.getId());
                createRelationship(sourceGroup, objectGroup, RelationshipTypes.PARENT);
                createRelationship(objectGroup, sourceGroup, RelationshipTypes.CHILD);
            }
        }
        if (!CollectionUtils.isEmpty(group.getChildGroups())) {
            for (Group childGroup : group.getChildGroups()) {

                Group objectGroup = groupRepository.findOne(childGroup.getId());
                createRelationship(sourceGroup, objectGroup, RelationshipTypes.CHILD);
                createRelationship(objectGroup, sourceGroup, RelationshipTypes.PARENT);
            }
        }
    }

    private GroupRelationship createRelationship(Group sourceGroup, Group objectGroup, RelationshipTypes relationshipType) {
        GroupRelationship groupRelationship = new GroupRelationship();
        groupRelationship.setSourceGroup(sourceGroup);
        groupRelationship.setObjectGroup(objectGroup);
        groupRelationship.setRelationshipType(relationshipType);
        return groupRelationshipRepository.save(groupRelationship);
    }

    private void deleteRelationship(Group sourceGroup, Group objectGroup, RelationshipTypes relationshipType) {
        groupRelationshipRepository.deleteBySourceObjectRelationshipType(sourceGroup, objectGroup, relationshipType);
    }

    private Group addSingleParentAndChildGroup(Group group) {
        // TODO Move this to PostConstruct sort out Transaction scope;

        Set<Group> parentGroups = new HashSet<Group>();
        Set<Group> childGroups = new HashSet<Group>();

        if (!CollectionUtils.isEmpty(group.getGroupRelationships())) {
            for (GroupRelationship groupRelationship : group.getGroupRelationships()) {

                if (groupRelationship.getRelationshipType() == RelationshipTypes.PARENT) {
                    Group detachedParentGroup = groupRelationship.getObjectGroup();
                    entityManager.detach(detachedParentGroup);
                    detachedParentGroup.setParentGroups(Collections.EMPTY_SET);
                    detachedParentGroup.setChildGroups(Collections.EMPTY_SET);
                    parentGroups.add(detachedParentGroup);
                }

                if (groupRelationship.getRelationshipType() == RelationshipTypes.CHILD) {
                    Group detachedChildGroup = groupRelationship.getObjectGroup();
                    entityManager.detach(detachedChildGroup);
                    detachedChildGroup.setParentGroups(Collections.EMPTY_SET);
                    detachedChildGroup.setChildGroups(Collections.EMPTY_SET);
                    childGroups.add(detachedChildGroup);
                }
            }
        }

        group.setParentGroups(parentGroups);
        group.setChildGroups(childGroups);
        return group;
    }

    // Attached the relationship of children groups and parents groups onto Transient objects
    private List<Group> addParentAndChildGroups(List<Group> groups) {

        for (Group group : groups) {
            addSingleParentAndChildGroup(group);
        }

        return groups;

    }

    public void addParentGroup(Long groupId, Long parentGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(parentGroupId);

        createRelationship(sourceGroup, objectGroup, RelationshipTypes.PARENT);
        createRelationship(objectGroup, sourceGroup, RelationshipTypes.CHILD);
    }

    public void deleteParentGroup(Long groupId, Long parentGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(parentGroupId);

        deleteRelationship(sourceGroup, objectGroup, RelationshipTypes.PARENT);
        deleteRelationship(objectGroup, sourceGroup, RelationshipTypes.CHILD);
    }

    public void addChildGroup(Long groupId, Long childGroupId) {
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(childGroupId);

        createRelationship(sourceGroup, objectGroup, RelationshipTypes.CHILD);
        createRelationship(objectGroup, sourceGroup, RelationshipTypes.PARENT);
    }

    public void deleteChildGroup(Long groupId, Long childGroupId) {
        Lookup parentRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "PARENT");
        Lookup childRelationshipType = lookupRepository.findByTypeAndValue(LookupTypes.RELATIONSHIP_TYPE, "CHILD");
        Group sourceGroup = groupRepository.findOne(groupId);
        Group objectGroup = groupRepository.findOne(childGroupId);

        deleteRelationship(sourceGroup, objectGroup, RelationshipTypes.CHILD);
        deleteRelationship(objectGroup, sourceGroup, RelationshipTypes.PARENT);
    }

    public Link addLink(final Long groupId, final Link link) {
        link.setGroup(groupRepository.findOne(groupId));
        link.setCreator(userRepository.findOne(1L));
        return linkRepository.save(link);
    }

    public ContactPoint addContactPoint(final Long groupId, final ContactPoint contactPoint) {
        contactPoint.setGroup(groupRepository.findOne(groupId));
        contactPoint.setCreator(userRepository.findOne(1L));
        contactPoint.setContactPointType(entityManager.find(ContactPointType.class, contactPoint.getContactPointType().getId()));
        return contactPointRepository.save(contactPoint);
    }

    public Location addLocation(final Long groupId, final Location location) {
        location.setGroup(groupRepository.findOne(groupId));
        location.setCreator(userRepository.findOne(1L));
        return locationRepository.save(location);
    }

    public void addFeature(Long groupId, Long featureId) {
        GroupFeature groupFeature = new GroupFeature();
        groupFeature.setFeature(featureRepository.findOne(featureId));
        groupFeature.setGroup(groupRepository.findOne(groupId));
        groupFeature.setCreator(userRepository.findOne(1L));
        groupFeatureRepository.save(groupFeature);
    }

    public void deleteFeature(Long groupId, Long featureId) {
        groupFeatureRepository.delete(groupFeatureRepository.findByGroupAndFeature(
                groupRepository.findOne(groupId), featureRepository.findOne(featureId)));
    }

    public List<Group> findChildren(Long groupId) throws ResourceNotFoundException {

        Group group = groupRepository.findOne(groupId);

        if (group == null) {
            throw new ResourceNotFoundException(String.format("The group id %s is not valid", groupId));
        }

        return Util.iterableToList(groupRepository.findChildren(group));

    }

    public void delete(Long id){
        LOG.info("Not Implemented");
    }
}
