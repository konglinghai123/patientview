package org.patientview.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface DocumentReferenceService {

    void add(Patientview data, FhirLink fhirLink) throws FhirResourceException, SQLException;

    void add(FhirDocumentReference fhirDocumentReference, FhirLink fhirLink) throws FhirResourceException;
}
