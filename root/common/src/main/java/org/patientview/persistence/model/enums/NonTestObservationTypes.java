package org.patientview.persistence.model.enums;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/08/2014
 *
 * Observation types used to reference non test/result Observation records e.g. blood group, smoking history
 * Testcode.java contains other test codes generated from patientview.xsd e.g. ciclosporin, weight
 */
public enum NonTestObservationTypes {
    BLOOD_GROUP("Blood Group"),
    PTPULSE("Foot Checkup: ptpulse"),
    DPPULSE("Foot Checkup: dppulse"),
    MGRADE("Eye Checkup: mgrade"),
    RGRADE("Eye Checkup: rgrade"),
    VA("Eye Checkup: va"),
    IBD_DISEASE_EXTENT("IBD Disease Extent");

    private String name;
    NonTestObservationTypes(String name) { this.name = name; }
    public String getName() { return this.name; }
    public String getId() { return this.name(); }
}
