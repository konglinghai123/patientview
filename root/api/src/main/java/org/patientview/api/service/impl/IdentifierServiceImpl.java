package org.patientview.api.service.impl;

import org.patientview.api.model.UserIdentifier;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.persistence.model.GroupRole;
import org.patientview.api.service.IdentifierService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.NonUniqueResultException;

import static java.lang.Integer.parseInt;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
@Service
public class IdentifierServiceImpl extends AbstractServiceImpl<IdentifierServiceImpl> implements IdentifierService {

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private UserRepository userRepository;

    private static final String GENERIC_GROUP_CODE = "GENERIC";
    private static final int NHS_NUMBER_START = 4;
    private static final int NHS_NUMBER_END = 9;
    private static final int CHI_NUMBER_START = 0;
    private static final int CHI_NUMBER_END = 3;
    private static final int NHS_NUMBER_LENGTH = 10;
    private static final int NHS_NUMBER_MODULUS = 11;
    private static final int NHS_NUMBER_MODULUS_OFFSET = 11;

    public Identifier get(final Long identifierId) throws ResourceNotFoundException, ResourceForbiddenException {

        Identifier identifier = identifierRepository.findOne(identifierId);

        if (identifier == null) {
            throw new ResourceNotFoundException("Identifier does not exist");
        }

        if (!isMemberOfCurrentUsersGroups(identifier.getUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        return identifier;
    }

    public void delete(final Long identifierId) throws ResourceNotFoundException, ResourceForbiddenException {

        Identifier identifier = identifierRepository.findOne(identifierId);

        if (identifier == null) {
            throw new ResourceNotFoundException("Identifier does not exist");
        }

        if (!isMemberOfCurrentUsersGroups(identifier.getUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        identifierRepository.delete(identifierId);
    }

    public void save(Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {

        Identifier entityIdentifier = identifierRepository.findOne(identifier.getId());
        if (entityIdentifier == null) {
            throw new ResourceNotFoundException("Identifier does not exist");
        }

        Identifier existingIdentifier = identifierRepository.findByValue(identifier.getIdentifier());

        if (existingIdentifier != null && !existingIdentifier.equals(entityIdentifier)) {
            throw new EntityExistsException("Cannot save Identifier, another Identifier with the same "
                    + "value already exists");
        }

        if (!isMemberOfCurrentUsersGroups(entityIdentifier.getUser())) {
            throw new ResourceForbiddenException("Forbidden");
        }

        entityIdentifier.setIdentifier(identifier.getIdentifier());
        entityIdentifier.setIdentifierType(identifier.getIdentifierType());
        identifierRepository.save(entityIdentifier);
    }

    public Identifier add(Long userId, Identifier identifier)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        // check Identifier doesn't already exist for another user
        Identifier entityIdentifier = identifierRepository.findByValue(identifier.getIdentifier());

        if (entityIdentifier != null) {
            if (!(user.getId().equals(entityIdentifier.getUser().getId()))) {
                throw new EntityExistsException("Identifier already exists for another patient");
            }
        }

        if (!isMemberOfCurrentUsersGroups(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        identifier.setCreator(getCurrentUser());
        user.getIdentifiers().add(identifier);
        identifier.setUser(user);
        return identifierRepository.save(identifier);
    }

    public Identifier getIdentifierByValue(String identifierValue) throws ResourceNotFoundException {
        Identifier identifier = identifierRepository.findByValue(identifierValue);
        if (identifier == null) {
            throw new ResourceNotFoundException(String.format("Could not find identifier with value %s",
                identifierValue));
        }
        return identifier;
    }

    public void validate(UserIdentifier userIdentifier)
        throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException, ResourceInvalidException {

        Long userId = userIdentifier.getUserId();
        Identifier identifier = userIdentifier.getIdentifier();
        boolean dummy = userIdentifier.isDummy();

        if (userId != null) {
            User user = userRepository.findOne(userIdentifier.getUserId());
            if (user == null) {
                throw new ResourceNotFoundException("Could not find user");
            }

            if (!isMemberOfCurrentUsersGroups(user)) {
                throw new ResourceForbiddenException("Forbidden");
            }
        }

        // check Identifier doesn't already exist (should only ever be one result returned)
        try {
            Identifier entityIdentifier = identifierRepository.findByValue(identifier.getIdentifier());

            if (entityIdentifier != null) {
                throw new EntityExistsException("Identifier already exists");
            }
        } catch (NonUniqueResultException nure) {
            throw new EntityExistsException("Identifier already exists");
        }

        if (!dummy) {
            try {
                isValidIdentifier(identifier);
            } catch (ResourceInvalidException rie) {
                throw new ResourceInvalidException("Invalid " + identifier.getIdentifierType().getDescription()
                        + " Identifier. " + rie.getMessage() + ".");
            }
        }
    }

    private void isValidIdentifier(Identifier identifier) throws ResourceInvalidException {

        String value = identifier.getIdentifier();

        if (identifier.getIdentifierType() == null) {
            throw new ResourceInvalidException("Invalid type");
        }

        if (identifier.getIdentifierType().getValue() == null) {
            throw new ResourceInvalidException("Invalid type");
        }

        // NHS Number
        if (identifier.getIdentifierType().getValue().equals(IdentifierTypes.NHS_NUMBER.toString())) {
            try {
                Long firstDigit = Long.parseLong(String.valueOf(value.charAt(0)));

                // should have correct first digit
                if (firstDigit < NHS_NUMBER_START || firstDigit > NHS_NUMBER_END) {
                    throw new ResourceInvalidException(
                            "Should start with digit between " + NHS_NUMBER_START + " or " + NHS_NUMBER_END);
                }

                // should be 10 characters
                if (value.length() != NHS_NUMBER_LENGTH) {
                    throw new ResourceInvalidException("Incorrect length");
                }

                // should be numeric and pass checksum
                if (!isChecksumValid(value)) {
                    throw new ResourceInvalidException("Failed Checksum");
                }

            } catch (NumberFormatException e) {
                throw new ResourceInvalidException("Incorrect character");
            }
        }

        // CHI Number
        if (identifier.getIdentifierType().getValue().equals(IdentifierTypes.CHI_NUMBER.toString())) {
            try {
                Long firstDigit = Long.parseLong(String.valueOf(value.charAt(0)));

                // should have correct first digit
                if (firstDigit < CHI_NUMBER_START || firstDigit > CHI_NUMBER_END) {
                    throw new ResourceInvalidException(
                            "Should start with digit between " + CHI_NUMBER_START + " or " + CHI_NUMBER_END);
                }

                // should be 10 characters
                if (value.length() != NHS_NUMBER_LENGTH) {
                    throw new ResourceInvalidException("Incorrect length");
                }

            } catch (NumberFormatException e) {
                throw new ResourceInvalidException("Incorrect character");
            }
        }
    }

    private boolean isChecksumValid(String nhsNumber) {
        /**
         * Generate the checksum using modulus 11 algorithm
         */
        int checksum = 0;

        try {
            // Multiply each of the first 9 digits by 10-character position (where the left character is in position 0)
            for (int i = 0; i < NHS_NUMBER_LENGTH - 1; i++) {
                int value = parseInt(nhsNumber.charAt(i) + "") * (NHS_NUMBER_LENGTH - i);
                checksum += value;
            }

            //(modulus 11)
            checksum = NHS_NUMBER_MODULUS_OFFSET - checksum % NHS_NUMBER_MODULUS;

            if (checksum == NHS_NUMBER_MODULUS_OFFSET) {
                checksum = 0;
            }

            // Does checksum match the 10th digit?
            return checksum == parseInt(String.valueOf(nhsNumber.charAt(NHS_NUMBER_LENGTH - 1)));

        } catch (NumberFormatException e) {
            return false; // nhsNumber contains letters
        }
    }

    private boolean isMemberOfCurrentUsersGroups(User user) {
        for (GroupRole groupRole : user.getGroupRoles()) {
            if (!groupRole.getGroup().getCode().toUpperCase().equals(GENERIC_GROUP_CODE)) {
                if (isCurrentUserMemberOfGroup(groupRole.getGroup())) {
                    return true;
                }
            }
        }

        return false;
    }
}
