package org.patientview.persistence.model.enums;

/**
 * Restricted list of features used when searching for conversation recipients by patients
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public enum PatientMessagingFeatureType {

    MESSAGING("Messaging"),
    DEFAULT_MESSAGING_CONTACT("Default Messaging Contact");

    private String name;
    PatientMessagingFeatureType(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
