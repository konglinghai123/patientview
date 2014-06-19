package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Entity
@Table(name = "pv_feature")
public class Feature extends RangeModel {

    @Column(name = "feature_name")
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "feature")
    private Set<Route> routes;

    @JsonIgnore
    @OneToMany(mappedBy = "feature")
    private Set<UserFeature> userFeatures;

    @JsonIgnore
    @OneToMany(mappedBy = "feature")
    private Set<GroupFeature> groupFeatures;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Set<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(final Set<Route> routes) {
        this.routes = routes;
    }

    public Set<UserFeature> getUserFeatures() {
        return userFeatures;
    }

    public void setUserFeatures(final Set<UserFeature> userFeatures) {
        this.userFeatures = userFeatures;
    }

    public Set<GroupFeature> getGroupFeatures() {
        return groupFeatures;
    }

    public void setGroupFeatures(final Set<GroupFeature> groupFeatures) {
        this.groupFeatures = groupFeatures;
    }
}
