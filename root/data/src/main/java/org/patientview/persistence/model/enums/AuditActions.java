package org.patientview.persistence.model.enums;

/**
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
public enum AuditActions {

    CREATE("Create"), EDIT("Edit"), CHANGE_PASSWORD("Change Password"), DELETE("Delete User"),
    RESET_PASSWORD("Reset Password"), VERIFY_EMAIL("Verify Email"), LOGON_SUCCESS("Logon Success"),
    LOGON_FAIL("Logon failed"), LOGOFF("log off");

    private String name;
    AuditActions(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }

}
