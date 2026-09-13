package com.talktranslate.service;

import com.talktranslate.model.User;
import com.talktranslate.model.dto.UpdateLanguageRequest;
import com.talktranslate.repository.FriendshipRepository;
import com.talktranslate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service managing user profile retrieval, preferences updates, and friend discovery recommendations.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final TranslationService translationService;

    @Autowired
    public UserService(UserRepository userRepository,
                       FriendshipRepository friendshipRepository,
                       @Autowired(required = false) TranslationService translationService) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.translationService = translationService;
    }

    public UserService(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this(userRepository, friendshipRepository, null);
    }

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId the user ID
     * @return the User entity
     */
    public User getUserById(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User lookup failed for ID: {}", userId);
                    return new NoSuchElementException("User not found with ID: " + userId);
                });
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username the username
     * @return the User entity
     */
    public User getUserByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User lookup failed for username: {}", username);
                    return new NoSuchElementException("User not found with username: " + username);
                });
    }

    /**
     * Updates user's preferred native language live during chat with validation.
     *
     * @param request the language update request
     * @return the updated User entity
     */
    @Transactional
    public User updateUserLanguage(UpdateLanguageRequest request) {
        if (request == null || request.getUserId() == null || request.getLanguageCode() == null) {
            throw new IllegalArgumentException("User ID and Language Code are required");
        }

        String newLang = request.getLanguageCode().trim().toLowerCase();
        if (translationService != null && !translationService.isLanguageSupported(newLang)) {
            logger.warn("Attempt to set unsupported language code: '{}' for user ID: {}", request.getLanguageCode(), request.getUserId());
            throw new IllegalArgumentException("Unsupported language code: " + request.getLanguageCode());
        }

        User user = getUserById(request.getUserId());
        String oldLang = user.getPreferredLanguage();
        user.setPreferredLanguage(newLang);
        User updated = userRepository.save(user);
        logger.info("Updated preferred language for user '{}' from '{}' to '{}'", user.getUsername(), oldLang, newLang);
        return updated;
    }

    /**
     * Returns suggested users to connect with (excluding self and existing connections).
     *
     * @param currentUserId the ID of the current user
     * @return list of recommended users to connect with
     */
    public List<User> getSuggestedUsers(String currentUserId) {
        if (currentUserId == null) {
            return userRepository.findAll();
        }

        // Get IDs of users with whom a friendship/request already exists
        List<String> connectedUserIds = friendshipRepository.findAllConnectedUserIds(currentUserId);
        if (connectedUserIds == null) {
            connectedUserIds = new ArrayList<>();
        }
        connectedUserIds.add(currentUserId);

        final List<String> excludedIds = connectedUserIds;
        List<User> suggestions = userRepository.findAll().stream()
                .filter(u -> !excludedIds.contains(u.getId()))
                .collect(Collectors.toList());
        logger.debug("Found {} suggested users for user ID: {}", suggestions.size(), currentUserId);
        return suggestions;
    }

    /**
     * Returns all registered users in the system.
     *
     * @return list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
