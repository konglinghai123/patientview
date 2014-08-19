package org.patientview.api.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateUtils;
import org.patientview.api.controller.model.Email;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Service
public class UserServiceImpl extends AbstractServiceImpl<UserServiceImpl> implements UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserFeatureRepository userFeatureRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private EmailService emailService;

    @Inject
    private EntityManager entityManager;

    @Inject
    private Properties properties;

    public User add(User user) {

        User newUser;

        if (getByUsername(user.getUsername()) != null) {
            throw new EntityExistsException("User already exists (username)");
        }

        if (getByEmail(user.getEmail()) != null) {
            throw new EntityExistsException("User already exists (email)");
        }

        user.setCreator(userRepository.findOne(1L));
        newUser = userRepository.save(user);
        Long userId = newUser.getId();
        LOG.info("New user with id: {}", user.getId());

        if (!CollectionUtils.isEmpty(user.getGroupRoles())) {

            for (GroupRole groupRole : user.getGroupRoles()) {

                groupRole.setGroup(groupRepository.findOne(groupRole.getGroup().getId()));
                groupRole.setRole(roleRepository.findOne(groupRole.getRole().getId()));
                groupRole.setUser(userRepository.findOne(userId));
                groupRole.setCreator(userRepository.findOne(1L));
                groupRoleRepository.save(groupRole);
            }
        }

        entityManager.flush();

        if (!CollectionUtils.isEmpty(user.getUserFeatures())) {

            for (UserFeature userFeature : user.getUserFeatures()) {
                userFeature.setFeature(featureRepository.findOne(userFeature.getFeature().getId()));
                userFeature.setUser(userRepository.findOne(userId));
                userFeature.setCreator(userRepository.findOne(1L));
                userFeatureRepository.save(userFeature);
            }
        }

        entityManager.flush();

        // TODO remove into a separate call
        if (!CollectionUtils.isEmpty(user.getIdentifiers())) {

            for (Identifier identifier : user.getIdentifiers()) {
                identifier.setId(null);
                identifier.setUser(userRepository.findOne(userId));
                identifier.setCreator(userRepository.findOne(1L));
                identifierRepository.save(identifier);
            }
        }

        entityManager.flush();

        user.setId(newUser.getId());
        // Everyone should change their password at login
        user.setChangePassword(Boolean.TRUE);
        // Everyone should be in the generic group.
        addUserToGenericGroup(newUser);

        return userRepository.save(user);

    }

    // We do this so early one gets the generic group
    private void addUserToGenericGroup(User user) {
        // TODO Sprint 2 make these value configurable
        Role role = roleRepository.findOne(7L);
        Group group = groupRepository.findOne(1L);

        GroupRole groupRole = new GroupRole();
        groupRole.setUser(user);
        groupRole.setGroup(group);
        groupRole.setCreator(userRepository.findOne(1L));
        groupRole.setRole(role);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);
    }

    /**
     * This persists the User map with GroupRoles and UserFeatures. The static
     * data objects are detached so have to be become managed again without updating the objects.
     *
     * @param user
     * @return
     */
    public User createUserWithPasswordEncryption(User user) {
        user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
        return add(user);
    }

    //Migration Only
    public User createUserNoEncryption(User user) {
        return add(user);
    }

    public User get(Long userId) {
        return userRepository.findOne(userId);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) throws ResourceNotFoundException {
        User entityUser = findUser(user.getId());
        entityUser.setForename(user.getForename());
        entityUser.setSurname(user.getSurname());
        entityUser.setUsername(user.getUsername());
        entityUser.setEmail(user.getEmail());
        entityUser.setEmailVerified(user.getEmailVerified());
        entityUser.setLocked(user.getLocked());
        entityUser.setDummy(user.getDummy());
        return userRepository.save(entityUser);
    }

    /**
     * Get users based on a list of groups and role types
     *
     * @param groupIds
     * @param roleIds
     * @return
     */
    public List<User> getUsersByGroupsAndRoles(List<Long> groupIds, List<Long> roleIds) {
        return Util.iterableToList(userRepository.findByGroupsAndRoles(groupIds, roleIds));
    }

    public void delete(Long userId) {
        userRepository.delete(userId);
    }

    public List<Feature> getUserFeatures(Long userId) throws ResourceNotFoundException {
        User user = findUser(userId);
        return Util.iterableToList(Util.iterableToList(featureRepository.findByUser(user)));
    }

    /**
     * Reset the flag so the user will not be prompted to change the password again
     *
     * @param userId
     * @param password
     * @return
     */
    public User changePassword(Long userId, String password) throws ResourceNotFoundException {
        User user = findUser(userId);
        user.setChangePassword(Boolean.FALSE);
        user.setPassword(DigestUtils.sha256Hex(password));
        return userRepository.save(user);
    }

    /**
     * On a password reset the user should change on login
     *
     * @param userId
     * @param password
     * @return
     */
    public User resetPassword(Long userId, String password) throws ResourceNotFoundException {
        User user = findUser(userId);
        user.setChangePassword(Boolean.TRUE);
        user.setPassword(DigestUtils.sha256Hex(password));
        return userRepository.save(user);
    }

    /**
     * Send a email to the user email address to verify have access to the email account
     *
     * @param userId
     * @return
     */
    public Boolean sendVerificationEmail(Long userId) {
        User user = userRepository.getOne(userId);
        Email email = new Email();
        email.setSender(properties.getProperty("smtp.sender"));
        email.setSubject("PatientView - Please verify your account");
        email.setRecipients(new String[]{user.getEmail()});

        StringBuilder sb = new StringBuilder();
        sb.append("Please visit http://www.patientview.org/#/verify?userId=");
        sb.append(user.getId());
        sb.append("&verificationCode=");
        sb.append(user.getVerificationCode());
        sb.append(" to validate your account.");
        email.setBody(sb.toString());
        return emailService.sendEmail(email);
    }

    public Boolean verify(Long userId, String verificationCode) throws ResourceNotFoundException {
        User user = findUser(userId);
        if (user.getVerificationCode().equals(verificationCode)) {
            user.setEmailVerified(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Identifier addIdentifier(Long userId, Identifier identifier) throws ResourceNotFoundException {
        User user = findUser(userId);
        identifier.setCreator(userRepository.findOne(1L));
        user.getIdentifiers().add(identifier);
        identifier.setUser(user);

        return identifierRepository.save(identifier);
    }

    public void addFeature(Long userId, Long featureId) {
        UserFeature userFeature = new UserFeature();
        userFeature.setFeature(featureRepository.findOne(featureId));
        userFeature.setUser(userRepository.findOne(userId));
        userFeature.setCreator(userRepository.findOne(1L));
        userFeatureRepository.save(userFeature);
    }

    public void deleteFeature(Long userId, Long featureId) {
        userFeatureRepository.delete(userFeatureRepository.findByUserAndFeature(
                userRepository.findOne(userId), featureRepository.findOne(featureId)));
    }

    private User findUser(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException(String.format("Could not find user %s", userId));
        }
        return user;
    }
}
