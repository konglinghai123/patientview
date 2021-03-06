package org.patientview.api.model;

/**
 * IdValue, a helper object used when a patient enters their own results using UserResultClusters.
 * Created by jamesr@solidstategroup.com
 * Created on 01/09/2014
 */
public class IdValue {

    private Long id;
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
