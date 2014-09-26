package org.patientview.importer.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.persistence.exception.FhirResourceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface EncounterService {

    public void add(Patientview data, ResourceReference patientReference, ResourceReference groupReference);

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException;
}
