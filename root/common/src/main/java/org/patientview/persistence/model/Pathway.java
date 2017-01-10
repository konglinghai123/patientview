package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.PathwayTypes;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;

/**
 * Pathway entity model represents pathway for the User eg Donorview
 */
@Entity
@Table(name = "pv_pathway")
public class Pathway extends AuditModel {

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "pathway_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PathwayTypes pathwayType;

    @OneToMany(mappedBy = "pathway", cascade = {CascadeType.REMOVE, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Stage> stages;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PathwayTypes getPathwayType() {
        return pathwayType;
    }

    public void setPathwayType(PathwayTypes pathwayType) {
        this.pathwayType = pathwayType;
    }


    public Set<Stage> getStages() {
        return stages;
    }

    public void setStages(Set<Stage> stages) {
        this.stages = stages;
    }
}
