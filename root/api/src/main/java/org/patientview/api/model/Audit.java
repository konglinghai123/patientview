package org.patientview.api.model;

import org.patientview.persistence.model.BaseModel;

import java.util.Date;

/**
 * Audit, represents logs of User, importer and other actions.
 * Created by jamesr@solidstategroup.com
 * Created on 12/11/2014
 */
public class Audit extends BaseModel {

    private String auditActions;
    private Long sourceObjectId;
    private String sourceObjectType;
    private String preValue;
    private String postValue;
    private Long actorId;
    private Date creationDate;
    private String username;

    // importer
    private String identifier;
    private String information;
    private String xml;
    private BaseGroup group;

    // new for transport object
    private User actor;
    private User sourceObjectUser;
    private BaseGroup sourceObjectGroup;

    public Audit() {
    }

    public Audit(org.patientview.persistence.model.Audit audit) {
        if (audit.getAuditActions() != null) {
            this.auditActions = audit.getAuditActions().getName();
        }
        this.sourceObjectId = audit.getSourceObjectId();
        if (audit.getSourceObjectType() != null) {
            this.sourceObjectType = audit.getSourceObjectType().getName();
        }
        this.preValue = audit.getPreValue();
        this.postValue = audit.getPostValue();
        this.actorId = audit.getActorId();
        this.creationDate = audit.getCreationDate();

        // importer
        this.identifier = audit.getIdentifier();
        this.information = audit.getInformation();
        this.xml = audit.getXml();

        if (audit.getGroup() != null) {
            this.setGroup(new BaseGroup(audit.getGroup()));
        }
    }

    public String getAuditActions() {
        return auditActions;
    }

    public void setAuditActions(final String auditActions) {
        this.auditActions = auditActions;
    }

    public Long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final Long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    public String getSourceObjectType() {
        return sourceObjectType;
    }

    public void setSourceObjectType(final String sourceObjectType) {
        this.sourceObjectType = sourceObjectType;
    }

    public String getPreValue() {
        return preValue;
    }

    public void setPreValue(final String preValue) {
        this.preValue = preValue;
    }

    public String getPostValue() {
        return postValue;
    }

    public void setPostValue(final String postValue) {
        this.postValue = postValue;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(final Long actorId) {
        this.actorId = actorId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public User getSourceObjectUser() {
        return sourceObjectUser;
    }

    public void setSourceObjectUser(User sourceObjectUser) {
        this.sourceObjectUser = sourceObjectUser;
    }

    public BaseGroup getSourceObjectGroup() {
        return sourceObjectGroup;
    }

    public void setSourceObjectGroup(BaseGroup sourceObjectGroup) {
        this.sourceObjectGroup = sourceObjectGroup;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public BaseGroup getGroup() {
        return group;
    }

    public void setGroup(BaseGroup group) {
        this.group = group;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
