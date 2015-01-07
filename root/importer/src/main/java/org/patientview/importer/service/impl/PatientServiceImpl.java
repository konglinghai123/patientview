package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.builder.PatientBuilder;
import org.patientview.persistence.model.User;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.PatientService;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Service
public class PatientServiceImpl extends AbstractServiceImpl<PatientServiceImpl> implements PatientService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private GroupRepository groupRepository;

    private String nhsno;

    /**
     * We link fhir records against NHS Number, Unit and User.
     *
     * @param patient
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @Override
    public FhirLink add(final Patientview patient, final ResourceReference practitionerReference)
            throws ResourceNotFoundException, FhirResourceException {

        this.nhsno = patient.getPatient().getPersonaldetails().getNhsno();
        LOG.info(nhsno + ": Starting Patient Data Process");

        // Find the identifier which the patient is linked to.
        Identifier identifier = matchPatientByIdentifierValue(patient);

        // Find the group that is importing the data
        Group group = groupRepository.findByCode(patient.getCentredetails().getCentrecode());

        // Find the link between the existing User, Unit and the FHIR Record
        FhirLink fhirLink = retrieveLink(group, identifier);

        // create Fhir patient from XML
        PatientBuilder patientBuilder = new PatientBuilder(patient, practitionerReference);
        Patient newFhirPatient = patientBuilder.build();
        UUID versionId;

        if (fhirLink != null) {
            // link to FHIR exists, update patient
            JSONObject jsonObject
                    = fhirResource.updateFhirObject(newFhirPatient, fhirLink.getResourceId(), fhirLink.getVersionId());
            versionId = Util.getVersionId(jsonObject);

            // update link
            fhirLink.setVersionId(versionId);
            fhirLink.setUpdated(new Date());
            fhirLinkRepository.save(fhirLink);
        } else {
            // Create a new Fhir record and add the link to the User and Unit
            JSONObject jsonObject = create(newFhirPatient);
            fhirLink = addLink(identifier, group, jsonObject);
        }

        // update User date of birth
        if (patient.getPatient().getPersonaldetails().getDateofbirth() != null) {
            User entityUser = userRepository.findOne(fhirLink.getUser().getId());
            entityUser.setDateOfBirth(
                    patient.getPatient().getPersonaldetails().getDateofbirth().toGregorianCalendar().getTime());
            userRepository.save(entityUser);
        }

        LOG.info(nhsno + ": Processed Patient Data");
        return fhirLink;
    }

    public List<FhirLink> getInactivePatientFhirLinksByGroup(Patientview patientview) throws ResourceNotFoundException {
        Identifier identifier = matchPatientByIdentifierValue(patientview);
        Group group = groupRepository.findByCode(patientview.getCentredetails().getCentrecode());

        if (group == null) {
            throw new ResourceNotFoundException(
                    nhsno + ": Group not found in PatientView database from imported <centrecode>");
        }

        return fhirLinkRepository.findInActiveByUserAndGroup(identifier.getUser(), group);
    }

    public void deleteByResourceId(UUID resourceId) throws FhirResourceException, SQLException {
        fhirResource.delete(resourceId, ResourceType.Patient);
    }

    public void deleteFhirLink(FhirLink fhirlink) throws ResourceNotFoundException {
        FhirLink entityFhirLink = fhirLinkRepository.findOne(fhirlink.getId());

        if (entityFhirLink == null) {
            throw new ResourceNotFoundException(nhsno + ": FhirLink not found");
        }

        fhirLinkRepository.delete(entityFhirLink);
    }

    private JSONObject create(Patient patient) throws FhirResourceException {
        try {
            return fhirResource.create(patient);
        } catch (Exception e) {
            LOG.error(nhsno + ": Could not build patient resource", e);
            throw new FhirResourceException(e.getMessage());
        }
    }

    public Identifier matchPatientByIdentifierValue(Patientview patientview) throws ResourceNotFoundException {

        // should only ever be one
        List<Identifier> identifiers = identifierRepository.findByValue(
                patientview.getPatient().getPersonaldetails().getNhsno());

        if (CollectionUtils.isEmpty(identifiers)) {
            throw new ResourceNotFoundException(nhsno + ": The Identifier value is not linked with PatientView");
        }

        return identifiers.get(0);
    }

    // return most recent as ordered by created DESC
    private FhirLink retrieveLink(Group group, Identifier identifier) {
        List<FhirLink> fhirLinks = fhirLinkRepository.findByUserAndGroupAndIdentifier(
                identifier.getUser(), group, identifier);

        if (!fhirLinks.isEmpty()) {
            return fhirLinks.get(0);
        } else {
            return null;
        }
    }

    private FhirLink addLink(Identifier identifier, Group group, JSONObject bundle) {
        if (CollectionUtils.isEmpty(identifier.getUser().getFhirLinks())) {
            identifier.getUser().setFhirLinks(new HashSet<FhirLink>());
        }

        Date now = new Date();

        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(identifier.getUser());
        fhirLink.setIdentifier(identifier);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(Util.getResourceId(bundle));
        fhirLink.setVersionId((Util.getVersionId(bundle)));
        fhirLink.setResourceType(ResourceType.Patient.name());
        fhirLink.setActive(true);
        fhirLink.setCreated(now);
        fhirLink.setUpdated(now);

        identifier.getUser().getFhirLinks().add(fhirLink);
        userRepository.save(identifier.getUser());

        return fhirLink;
    }
}
