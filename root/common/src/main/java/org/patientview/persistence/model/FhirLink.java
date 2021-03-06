package org.patientview.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 27/08/2014
 */
@Entity
@Table(name = "pv_fhir_link")
public class FhirLink extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "identifier_id")
    private Identifier identifier;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "version_id")
    private UUID versionId;

    @Column(name = "resource_type")
    private String resourceType;

    @OneToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @Column(name = "last_update_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated = new Date();

    @Column(name = "active")
    private Boolean active;

    // used when importing patients, to see if new patient for this GP and if GP letter table should be updated
    @Transient
    private boolean isNew;

    public FhirLink() { }

    // used when retrieving only essential data using repository queries
    public FhirLink(Long id, UUID resourceId, User user) {
        setId(id);
        setResourceId(resourceId);
        setUser(user);
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(final UUID resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final Identifier identifier) {
        this.identifier = identifier;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }


    public UUID getVersionId() {
        return versionId;
    }

    public void setVersionId(final UUID versionId) {
        this.versionId = versionId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }
}
