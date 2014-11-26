package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
public enum AuditActions {

    // group
    CREATE_GROUP("Create Group"),
    EDIT_GROUP("Edit Group"),

    // user group roles
    ADD_GROUP_ROLE("Add Group Role"),
    REMOVE_GROUP_ROLE("Remove Group Role"),
    REMOVE_GROUP_ROLES("Remove all Groups and Roles"),

    // user and group
    ADD_FEATURE("Add Feature"),
    REMOVE_FEATURE("Remove Feature"),

    // user
    CREATE_USER("Create User"),
    EDIT_USER("Edit User"),
    RESET_PASSWORD("Reset Password"),
    VERIFY_EMAIL("Verify Email"),
    LOGON_SUCCESS("Log on"),
    CHANGE_PASSWORD("Change Password"),
    DELETE_USER("Delete User"),
    LOGON_FAIL("Log on failed"),
    LOGOFF("Log off"),
    VIEW_USER("View User"),

    // importer
    PATIENT_DATA_SUCCESS("Patient data imported"),
    PATIENT_DATA_VALIDATE_FAIL("Patient data failed validation"),
    PATIENT_DATA_FAIL("Patient data failed import"),

    // ECS (GP Medication)
    GET_PATIENT_IDENTIFIERS_ECS("Get Patient Identifiers (ECS)");

    // not used
    //VIEW("View"),
    //MAP("Map");

    private String name;
    AuditActions(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
