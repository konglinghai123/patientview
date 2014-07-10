package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Entity
@Table(name = "pv_group")
public class Group extends AuditModel {

    @Column(name = "group_name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "fhir_resource_id")
    private UUID fhirResourceId;

    @Column(name = "visible")
    private Boolean visible;

    @OneToOne
    @JoinColumn(name = "type_id")
    private Lookup groupType;

    @OneToMany(mappedBy = "group")
    private Set<GroupFeature> groupFeatures;

    @OneToMany(mappedBy = "group")
    private Set<GroupRole> groupRoles;

    @OneToMany(mappedBy = "group")
    private Set<Route> routes;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
    private Set<Link> links;

    @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private Set<Location> locations;

    @Transient
    private Set<Group> parentGroups;

    @Transient
    private Set<Group> childGroups;

    @OneToMany(mappedBy = "sourceGroup", fetch = FetchType.EAGER)
    private Set<GroupRelationship> groupRelationships;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public UUID getFhirResourceId() {
        return fhirResourceId;
    }

    public void setFhirResourceId(final UUID fhirResourceId) {
        this.fhirResourceId = fhirResourceId;
    }

    public Lookup getGroupType() {
        return groupType;
    }

    public void setGroupType(final Lookup groupType) {
        this.groupType = groupType;
    }

    @JsonIgnore
    public Set<GroupFeature> getGroupFeatures() {
        return groupFeatures;
    }

    public void setGroupFeatures(final Set<GroupFeature> groupFeatures) {
        this.groupFeatures = groupFeatures;
    }

    @JsonIgnore
    public Set<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(final Set<Route> routes) {
        this.routes = routes;
    }

    public Set<Link> getLinks() {
        return links;
    }

    public void setLinks(Set<Link> links) {
        this.links = links;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    @JsonIgnore
    public Set<GroupRole> getGroupRoles() {
        return groupRoles;
    }

    public void setGroupRoles(final Set<GroupRole> groupRoles) {
        this.groupRoles = groupRoles;
    }

    public Set<Group> getParentGroups() {
        return parentGroups;
    }

    public void setParentGroups(Set<Group> parentGroups) {
        this.parentGroups = parentGroups;
    }

    public Set<Group> getChildGroups() {
        return childGroups;
    }

    public void setChildGroups(Set<Group> childGroups) {
        this.childGroups = childGroups;
    }

    @JsonIgnore
    public Set<GroupRelationship> getGroupRelationships() {
        return groupRelationships;
    }

    public void setGroupRelationships(final Set<GroupRelationship> groupRelationships) {
        this.groupRelationships = groupRelationships;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(final Boolean visible) {
        this.visible = visible;
    }
}
