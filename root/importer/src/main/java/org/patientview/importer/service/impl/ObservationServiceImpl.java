package org.patientview.importer.service.impl;

import generated.Patientview;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.ObservationsBuilder;
import org.patientview.importer.model.BasicObservation;
import org.patientview.importer.model.DateRange;
import org.patientview.persistence.model.FhirDatabaseObservation;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.ObservationService;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Service
public class ObservationServiceImpl extends AbstractServiceImpl<ObservationService> implements ObservationService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    @Named("fhir")
    private BasicDataSource dataSource;

    /**
     * Creates all of the FHIR observation records from the Patientview object. Links then to the PatientReference
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        LOG.info("Starting Observation Process");
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        ObservationsBuilder observationsBuilder = new ObservationsBuilder(data, patientReference);
        observationsBuilder.build();

        LOG.info("Getting Existing Observations");
        List<BasicObservation> observations = getBasicObservationBySubjectId(fhirLink.getResourceId());

        // delete existing observations
        LOG.info("Deleting Existing Observations");

        for (BasicObservation observation : observations) {
            String code = observation.getCode();
            UUID uuid = observation.getLogicalId();
            Date applies = observation.getApplies();

            // only delete test result observations between date range (not BLOOD_GROUP, DIAGNOSTIC_RESULT etc)
            if (!Util.isInEnum(code, NonTestObservationTypes.class)) {

                Patientview.Patient.Testdetails.Test.Daterange daterange
                        = observationsBuilder.getDateRanges().get(code);

                // between dates in <test><daterange>
                if (daterange != null) {
                    DateRange convertedDateRange = new DateRange(daterange);

                    if (applies.after(convertedDateRange.getStart()) && applies.before(convertedDateRange.getEnd())) {
                        fhirResource.delete(uuid, ResourceType.Observation);
                    }
                }
            }

            // if observation is NonTestObservationType.BLOOD_GROUP, PTPULSE, DPPULSE then delete
            if (code.equals(NonTestObservationTypes.BLOOD_GROUP.toString())
                    || code.equals(NonTestObservationTypes.PTPULSE.toString())
                    || code.equals(NonTestObservationTypes.DPPULSE.toString())) {
                fhirResource.delete(uuid, ResourceType.Observation);
            }
        }

        int count = 0;

        // old slow method (calls fhir_create)
        /*LOG.info("Creating New Observations");
        for (Observation observation : observationsBuilder.getObservations()) {
            LOG.trace("Creating... observation " + count);
            try {
                // only add observations within daterange or those without a daterange (non test observation type)
                Patientview.Patient.Testdetails.Test.Daterange daterange
                        = observationsBuilder.getDateRanges().get(observation.getIdentifier()
                            .getValueSimple().toUpperCase());

                if (daterange != null) {
                    DateRange convertedDateRange = new DateRange(daterange);
                    Date applies = convertDateTime((DateTime) observation.getApplies());

                    if (applies.after(convertedDateRange.getStart()) && applies.before(convertedDateRange.getEnd())) {
                        fhirResource.create(observation);
                    }
                } else {
                    fhirResource.create(observation);
                }
            } catch (FhirResourceException e) {
                LOG.error("Unable to build observation {} " + e.getCause());
            }
            LOG.trace("Finished creating observation " + count++);
        }
        LOG.info("Processed {} of {} observations", observationsBuilder.getSuccess(), observationsBuilder.getCount());*/

        LOG.info("Creating New Observations");
        List<FhirDatabaseObservation> fhirDatabaseObservations = new ArrayList<>();

        for (Observation observation : observationsBuilder.getObservations()) {
            LOG.trace("Creating... observation " + count);
            try {
                // only add observations within daterange or those without a daterange (non test observation type)
                Patientview.Patient.Testdetails.Test.Daterange daterange
                        = observationsBuilder.getDateRanges().get(observation.getIdentifier()
                            .getValueSimple().toUpperCase());

                if (daterange != null) {
                    DateRange convertedDateRange = new DateRange(daterange);
                    Date applies = convertDateTime((DateTime) observation.getApplies());

                    if (applies.after(convertedDateRange.getStart()) && applies.before(convertedDateRange.getEnd())) {
                        //fhirResource.create(observation);
                        fhirDatabaseObservations.add(
                                new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation)));
                    }
                } else {
                    //fhirResource.create(observation);
                    fhirDatabaseObservations.add(
                            new FhirDatabaseObservation(fhirResource.marshallFhirRecord(observation)));
                }
            } catch (FhirResourceException e) {
                LOG.error("Unable to build observation {} " + e.getCause());
            }
            LOG.trace("Finished creating observation " + count++);
        }

        // now have collection, manually insert using native SQL
        if (!CollectionUtils.isEmpty(fhirDatabaseObservations)) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO observation (logical_id, version_id, resource_type, published, updated, content) VALUES ");

            for (int i = 0; i < fhirDatabaseObservations.size() ; i++) {
                FhirDatabaseObservation obs = fhirDatabaseObservations.get(i);
                sb.append("(");
                sb.append("'").append(obs.getLogicalId().toString()).append("','");
                sb.append(obs.getVersionId().toString()).append("','");
                sb.append(obs.getResourceType()).append("','");
                sb.append(obs.getPublished().toString()).append("','");
                sb.append(obs.getUpdated().toString()).append("','");
                sb.append(obs.getContent());
                sb.append("')");
                if (i != (fhirDatabaseObservations.size() - 1)) {
                    sb.append(",");
                }
            }
            fhirResource.executeSQL(sb.toString());
        }

        LOG.info("Processed {} of {} observations", observationsBuilder.getSuccess(), observationsBuilder.getCount());
    }

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("observation", subjectId)) {
            fhirResource.delete(uuid, ResourceType.Observation);
        }
    }

    private Date convertDateTime(DateTime dateTime) {
        DateAndTime dateAndTime = dateTime.getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateAndTime.getYear(), dateAndTime.getMonth() - 1, dateAndTime.getDay(),
                dateAndTime.getHour(), dateAndTime.getMinute());
        return calendar.getTime();
    }

    private List<BasicObservation> getBasicObservationBySubjectId(final UUID subjectId)
            throws FhirResourceException {

        // build query
        StringBuilder query = new StringBuilder();
        query.append("SELECT logical_id, content->'appliesDateTime', content->'name'->'text' ");
        query.append("FROM observation ");
        query.append("WHERE   content ->> 'subject' = '{\"display\": \"");
        query.append(subjectId);
        query.append("\", \"reference\": \"uuid\"}' ");

        // execute and return map of logical ids and applies
        try {
            Connection connection = dataSource.getConnection();
            java.sql.Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(query.toString());
            List<BasicObservation> observations = new ArrayList<>();

            while ((results.next())) {

                // remove timezone and parse date
                try {
                    String codeString = results.getString(3).replace("\"", "");

                    // ignore DIAGNOSTIC_RESULT
                    if(!codeString.equals(NonTestObservationTypes.DIAGNOSTIC_RESULT.toString())) {

                        Date applies = null;

                        if (StringUtils.isNotEmpty(results.getString(2))) {
                            String dateString = results.getString(2).replace("\"", "");
                            XMLGregorianCalendar xmlDate
                                    = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
                            applies = xmlDate.toGregorianCalendar().getTime();
                        }

                        observations.add(new BasicObservation(
                                UUID.fromString(results.getString(1)), applies, codeString));
                    }

                } catch (DatatypeConfigurationException e) {
                    LOG.error(e.getMessage());
                }
            }

            connection.close();
            return observations;
        } catch (SQLException e) {
            throw new FhirResourceException(e);
        }
    }
}


