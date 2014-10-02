package org.patientview.api.model;

import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Decimal;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Property;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.BaseModel;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public class FhirObservation extends BaseModel{

    private Date applies;
    private String name;
    private String value;
    private String comments;
    private Group group;
    private String temporaryUuid;

    public FhirObservation() {
    }

    // if converting from actual Observation
    public FhirObservation(Observation observation) throws FhirResourceException {

        if (observation.getName() != null) {
            setTemporaryUuid(UUID.randomUUID().toString());
            setName(observation.getName().getTextSimple());
            setComments(observation.getCommentsSimple());

            try {
                if (observation.getValue() != null) {
                    Property valueProperty = observation.getValue().getChildByName("value");

                    if (valueProperty.getTypeCode().equals("decimal")) {
                        Decimal element = (Decimal)valueProperty.getValues().get(0);
                        setValue(element.getStringValue());
                    }
                }
            } catch (NumberFormatException nfe) {
                throw new FhirResourceException("Cannot convert FHIR observation, missing Value");
            }

            if (observation.getApplies() != null) {
                DateTime applies = (DateTime) observation.getApplies();
                DateAndTime date = applies.getValue();
                setApplies(new Date(new GregorianCalendar(date.getYear(), date.getMonth() - 1,
                        date.getDay(), date.getHour(), date.getMinute(), date.getSecond()).getTimeInMillis()));
            }
        } else {
            throw new FhirResourceException("Cannot convert FHIR observation, missing Name");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getApplies() {
        return applies;
    }

    public void setApplies(Date applies) {
        this.applies = applies;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getTemporaryUuid() {
        return temporaryUuid;
    }

    public void setTemporaryUuid(String temporaryUuid) {
        this.temporaryUuid = temporaryUuid;
    }
}
