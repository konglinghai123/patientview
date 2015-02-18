package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.BaseUser;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Conversation;
import org.patientview.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

/**
 * Conversation service, for CRUD operations related to Conversations and Messages.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ConversationService extends CrudService<Conversation> {

    /**
     * Create a new conversation, including recipients and associated Message.
     * @param userId ID of User creating Conversation
     * @param conversation Conversation object containing all required properties and first Message content
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    void addConversation(Long userId, Conversation conversation)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Add a Message to an existing Conversation.
     * @param conversationId ID of Conversation to add Message to
     * @param message Message object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    void addMessage(Long conversationId, org.patientview.api.model.Message message)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Add a read receipt for a Message given the Message and User IDs.
     * @param messageId ID of Message to add read receipt for
     * @param userId ID of User who has read the Message
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    void addMessageReadReceipt(Long messageId, Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    void deleteUserFromConversations(User user);

    /**
     * Get a Page of Conversation objects given a User (who is a member of the Conversations) and Pageable containing
     * pagination settings.
     * @param userId ID of User to retrieve Conversations for
     * @param pageable Pageable object containing pagination options
     * @return Page of Conversations
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    Page<org.patientview.api.model.Conversation> findByUserId(Long userId, Pageable pageable)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Conversation, including Messages given a Conversation ID.
     * @param conversationId ID of Conversation to retrieve
     * @return Conversation object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    org.patientview.api.model.Conversation findByConversationId(Long conversationId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a list of potential message recipients, mapped by User role. Used in UI by user when creating a new
     * Conversation to populate the drop-down select of available recipients after a Group is selected.
     * Note: not currently used due to speed concerns when rendering large lists client-side in ie8.
     * @param userId ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return Object containing Lists of BaseUser organised by Role
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    HashMap<String, List<BaseUser>> getRecipients(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Fast method of returning available Conversation recipients when a User has selected a Group in the UI.
     * Note: returns HTML as a String to avoid performance issues in ie8
     * @param userId ID of User retrieving available Conversation recipients
     * @param groupId ID of Group to find available Conversation recipients for
     * @return HTML String for drop-down select
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    String getRecipientsFast(Long userId, Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get the number of unread Messages (those with no read receipt) for a User.
     * @param userId ID of User to find number of unread messages for
     * @return Long containing number of unread messages
     * @throws ResourceNotFoundException
     */
    @UserOnly
    Long getUnreadConversationCount(Long userId) throws ResourceNotFoundException;
}
