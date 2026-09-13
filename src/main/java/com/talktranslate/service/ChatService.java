package com.talktranslate.service;

import com.talktranslate.model.ChatMessage;
import com.talktranslate.model.MessageType;
import com.talktranslate.model.TranslationResponse;
import com.talktranslate.model.User;
import com.talktranslate.repository.ChatMessageRepository;
import com.talktranslate.repository.FriendshipRepository;
import com.talktranslate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service managing chat messaging, language detection, dynamic translation, resilience fallbacks, and chat history.
 */
@Service
@Transactional(readOnly = true)
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final TranslationService translationService;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       UserRepository userRepository,
                       FriendshipRepository friendshipRepository,
                       TranslationService translationService) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.translationService = translationService;
    }

    /**
     * Processes an incoming 1-on-1 chat message:
     * 1. Verifies mutual friendship between sender and recipient.
     * 2. Detects sender language.
     * 3. Dynamically translates the message into the recipient's preferred language with fail-safe fallback.
     * 4. Persists the message to the database.
     *
     * @param message the chat message to process
     * @return the persisted chat message with translation details
     */
    @Transactional
    public ChatMessage processAndSaveMessage(ChatMessage message) {
        if (message == null || message.getOriginalText() == null || message.getOriginalText().trim().isEmpty()) {
            throw new IllegalArgumentException("Message text cannot be empty");
        }

        String senderId = message.getSenderId();
        String recipientId = message.getRecipientId();

        if (senderId == null || recipientId == null) {
            throw new IllegalArgumentException("Sender ID and Recipient ID are required");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NoSuchElementException("Sender not found with ID: " + senderId));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new NoSuchElementException("Recipient not found with ID: " + recipientId));

        // Verify mutual friendship
        boolean areFriends = friendshipRepository.findAcceptedFriendship(senderId, recipientId).isPresent();
        if (!areFriends) {
            logger.warn("Message delivery blocked: Users {} and {} are not mutual friends", senderId, recipientId);
            throw new IllegalStateException("Cannot send message: You and " + recipient.getUsername() + " are not mutual friends");
        }

        // Detect or use sender language
        String senderLang = message.getSenderLang();
        if (senderLang == null || senderLang.isBlank() || "auto".equalsIgnoreCase(senderLang)) {
            senderLang = translationService.detectLanguage(message.getOriginalText());
        }

        // Recipient's current dynamic preferred language
        String recipientLang = recipient.getPreferredLanguage();

        // Translate with resilience fallback
        TranslationResponse translation;
        try {
            translation = translationService.translate(
                    message.getOriginalText(),
                    senderLang,
                    recipientLang
            );
        } catch (Exception e) {
            logger.warn("Translation service encountered error: {}. Falling back to original text.", e.getMessage());
            translation = new TranslationResponse(message.getOriginalText(), message.getOriginalText(), senderLang, recipientLang, senderLang, false);
        }

        // Populate fields
        message.setSenderName(sender.getFullName());
        message.setRecipientName(recipient.getFullName());
        message.setSenderLang(senderLang);
        message.setRecipientLang(recipientLang);
        message.setDetectedLang(translation != null ? translation.getDetectedLang() : senderLang);
        message.setTranslatedText(translation != null ? translation.getTranslatedText() : message.getOriginalText());
        message.setType(MessageType.CHAT);

        // Save message history
        ChatMessage saved = chatMessageRepository.save(message);
        logger.debug("Message processed and saved with ID: {} from {} to {}", saved.getId(), senderId, recipientId);
        return saved;
    }

    /**
     * Loads complete chat history between two mutual friends.
     *
     * @param userA first user's ID
     * @param userB second user's ID
     * @return list of chat messages exchanged between userA and userB
     */
    public List<ChatMessage> getChatHistory(String userA, String userB) {
        if (userA == null || userB == null) {
            throw new IllegalArgumentException("User IDs cannot be null");
        }

        boolean areFriends = friendshipRepository.findAcceptedFriendship(userA, userB).isPresent();
        if (!areFriends) {
            logger.warn("Chat history access denied: Users {} and {} are not mutual friends", userA, userB);
            throw new IllegalStateException("Users are not mutual friends");
        }

        return chatMessageRepository.findChatHistoryBetween(userA, userB);
    }

    /**
     * Loads paginated chat history between two mutual friends.
     *
     * @param userA first user's ID
     * @param userB second user's ID
     * @param page page index (0-based)
     * @param size page size
     * @return paginated list of chat messages exchanged between userA and userB
     */
    public List<ChatMessage> getChatHistory(String userA, String userB, int page, int size) {
        if (userA == null || userB == null) {
            throw new IllegalArgumentException("User IDs cannot be null");
        }
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters: page must be >= 0 and size must be > 0");
        }

        boolean areFriends = friendshipRepository.findAcceptedFriendship(userA, userB).isPresent();
        if (!areFriends) {
            logger.warn("Chat history access denied: Users {} and {} are not mutual friends", userA, userB);
            throw new IllegalStateException("Users are not mutual friends");
        }

        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findChatHistoryBetween(userA, userB, pageable).getContent();
    }
}
