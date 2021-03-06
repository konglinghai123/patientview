package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.ScoreSeverity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by james@solidstategroup.com
 * Created on 17/06/2015
 */
@Entity
@Table(name = "pv_survey_response_score")
public class SurveyResponseScore extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "survey_response_id")
    private SurveyResponse surveyResponse;

    @Column(name = "score", columnDefinition="numeric", precision=19, scale=2)
    private Double score;

    @Column(name = "severity")
    @Enumerated(EnumType.STRING)
    private ScoreSeverity severity;

    @Column(name = "type")
    private String type;

    @Column(name = "level")
    private String level;

    public SurveyResponseScore() {}

    public SurveyResponseScore(SurveyResponse surveyResponse, String type,
                               Double score, ScoreSeverity severity) {
        this.surveyResponse = surveyResponse;
        this.type = type;
        this.score = score;
        this.severity = severity;
    }

    @JsonIgnore
    public SurveyResponse getSurveyResponse() {
        return surveyResponse;
    }

    public void setSurveyResponse(SurveyResponse surveyResponse) {
        this.surveyResponse = surveyResponse;
    }

    public ScoreSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ScoreSeverity severity) {
        this.severity = severity;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
