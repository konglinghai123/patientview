package org.patientview.importer.builder;

import generated.Patientview;
import generated.Patientview.Patient.Letterdetails.Letter;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/09/2014
 */
public class DocumentReferenceBuilder {

    private final Logger LOG = LoggerFactory.getLogger(DocumentReferenceBuilder.class);

    private ResourceReference resourceReference;
    private Patientview data;
    private List<DocumentReference> documentReferences;
    private int success = 0;
    private int count = 0;
    private Alert alert;

    public DocumentReferenceBuilder(Patientview results, ResourceReference resourceReference) {
        this.data = results;
        this.resourceReference = resourceReference;
        documentReferences = new ArrayList<>();
    }

    // Normally and invalid data would fail the whole XML
    public List<DocumentReference> build() {

        if (data.getPatient().getLetterdetails() != null) {
            for (Patientview.Patient.Letterdetails.Letter letter : data.getPatient().getLetterdetails().getLetter()) {
                try {
                    documentReferences.add(createDocumentReference(letter));

                    if (alert == null) {
                        alert = new Alert();
                    }

                    if (alert.getLatestDate() == null) {
                        alert.setLatestDate(letter.getLetterdate().toGregorianCalendar().getTime());
                        alert.setLatestValue(letter.getLettertype());
                        alert.setEmailAlertSent(false);
                        alert.setWebAlertViewed(false);
                        alert.setUpdated(true);
                    } else {
                        if (alert.getLatestDate().getTime()
                                < letter.getLetterdate().toGregorianCalendar().getTime().getTime()) {
                            alert.setLatestDate(letter.getLetterdate().toGregorianCalendar().getTime());
                            alert.setLatestValue(letter.getLettertype());
                            alert.setEmailAlertSent(false);
                            alert.setWebAlertViewed(false);
                            alert.setUpdated(true);
                        }
                    }

                    success++;
                } catch (FhirResourceException e) {
                    LOG.error("Invalid data in XML: " + e.getMessage());
                }
                count++;
            }
        }

        return documentReferences;
    }

    private DocumentReference createDocumentReference(Letter letter) throws FhirResourceException{
        DocumentReference documentReference = new DocumentReference();
        documentReference.setStatusSimple(DocumentReference.DocumentReferenceStatus.current);
        documentReference.setSubject(resourceReference);

        if (StringUtils.isNotEmpty(letter.getLettertype())) {
            CodeableConcept type = new CodeableConcept();
            type.setTextSimple(CommonUtils.cleanSql(letter.getLettertype()));
            documentReference.setType(type);
        }

        if (StringUtils.isNotEmpty(letter.getLettercontent())) {
            documentReference.setDescriptionSimple(CommonUtils.cleanSql(letter.getLettercontent()));
        }

        if (letter.getLetterdate() != null) {
            try {
                DateAndTime dateAndTime = new DateAndTime(letter.getLetterdate().toGregorianCalendar().getTime());
                DateTime date = new DateTime();
                date.setValue(dateAndTime);
                documentReference.setCreated(date);
            } catch (NullPointerException npe) {
                throw new FhirResourceException("Letter timestamp is incorrectly formatted");
            }
        }

        return documentReference;
    }

    public int getSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }
}
