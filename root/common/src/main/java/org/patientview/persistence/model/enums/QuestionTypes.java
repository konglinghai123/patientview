package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
public enum QuestionTypes {
    SINGLE_SELECT("Single Select"),
    SINGLE_SELECT_RANGE("Single Select Range");

    private String name;
    QuestionTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
