package com.talktranslate.service;

import com.talktranslate.model.Friendship;
import com.talktranslate.model.FriendshipStatus;
import com.talktranslate.model.User;
import com.talktranslate.model.dto.FriendRequestDto;
import com.talktranslate.model.dto.FriendResponseDto;
import com.talktranslate.repository.FriendshipRepository;
import com.talktranslate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing user connections, friend requests lifecycle, and mutual friendships.
 */
@Service
@Transactional(readOnly = true)
public class FriendshipService {

    private static final Logger logger = LoggerFactory.getLogger(FriendshipService.class);

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public FriendshipService(FriendshipRepository friendshipRepository,
                             UserRepository userRepository,
                             SimpMessagingTemplate messagingTemplate) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends a friend request to a target user.
     *
     * @param requesterId the ID of user initiating the request
     * @param addresseeId the ID of target user
     * @return the created or updated friend request DTO
     */
    @Transactional
    public FriendRequestDto sendFriendRequest(String requesterId, String addresseeId) {
        if (requesterId == null || addresseeId == null) {
            throw new IllegalArgumentException("Requester ID and Addressee ID cannot be null");
        }
        if (requesterId.equalsIgnoreCase(addresseeId)) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NoSuchElementException("Requester user not found"));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new NoSuchElementException("Target user not found"));

        Optional<Friendship> existingOpt = friendshipRepository.findFriendshipBetween(requesterId, addresseeId);
        Friendship saved;
        if (existingOpt.isPresent()) {
            Friendship existing = existingOpt.get();
            if (existing.getStatus() == FriendshipStatus.ACCEPTED) {
                logger.warn("Friend request failed: User {} is already friends with {}", requesterId, addressee.getUsername());
                throw new IllegalStateException("You are already friends with " + addressee.getUsername());
            }
            if (existing.getStatus() == FriendshipStatus.PENDING) {
                logger.warn("Friend request failed: Pending request already exists between {} and {}", requesterId, addresseeId);
                throw new IllegalStateException("A friend request is already pending between you two");
            }
            if (existing.getStatus() == FriendshipStatus.BLOCKED) {
                logger.warn("Friend request blocked: Relationship is blocked between {} and {}", requesterId, addresseeId);
                throw new IllegalStateException("Unable to send friend request to this user");
            }
            // If previously rejected, re-open as PENDING
            existing.setRequester(requester);
            existing.setAddressee(addressee);
            existing.setStatus(FriendshipStatus.PENDING);
            saved = friendshipRepository.save(existing);
        } else {
            Friendship friendship = Friendship.builder()
                    .requester(requester)
                    .addressee(addressee)
                    .status(FriendshipStatus.PENDING)
                    .build();
            saved = friendshipRepository.save(friendship);
        }

        logger.info("Friend request sent from {} to {}", requesterId, addresseeId);
        FriendRequestDto dto = toFriendRequestDto(saved);
        try {
            messagingTemplate.convertAndSendToUser(addresseeId, "/queue/notifications", dto);
        } catch (Exception e) {
            logger.debug("Failed to deliver real-time friend request notification to user {}: {}", addresseeId, e.getMessage());
        }
        return dto;
    }

    /**
     * Accepts an incoming friend request.
     *
     * @param requestId the friendship ID
     * @param currentUserId the ID of the user accepting the request
     * @return the friend response DTO
     */
    @Transactional
    public FriendResponseDto acceptFriendRequest(String requestId, String currentUserId) {
        Friendship friendship = getFriendshipOrThrow(requestId);

        if (!friendship.getAddressee().getId().equalsIgnoreCase(currentUserId)) {
            logger.warn("Unauthorized attempt to accept friend request {} by user {}", requestId, currentUserId);
            throw new IllegalArgumentException("Only the recipient of a friend request can accept it");
        }

        if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
            return toFriendResponseDto(friendship, currentUserId);
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship saved = friendshipRepository.save(friendship);
        logger.info("Friend request {} accepted by user {}", requestId, currentUserId);
        FriendResponseDto dto = toFriendResponseDto(saved, currentUserId);

        try {
            // Notify the original requester that their friend request was accepted
            messagingTemplate.convertAndSendToUser(
                    friendship.getRequester().getId(),
                    "/queue/notifications",
                    dto
            );
        } catch (Exception e) {
            logger.debug("Failed to deliver friend acceptance notification to user {}: {}", friendship.getRequester().getId(), e.getMessage());
        }
        return dto;
    }

    /**
     * Rejects an incoming friend request.
     *
     * @param requestId the friendship ID
     * @param currentUserId the ID of the user rejecting the request
     */
    @Transactional
    public void rejectFriendRequest(String requestId, String currentUserId) {
        Friendship friendship = getFriendshipOrThrow(requestId);

        if (!friendship.getAddressee().getId().equalsIgnoreCase(currentUserId)) {
            logger.warn("Unauthorized attempt to reject friend request {} by user {}", requestId, currentUserId);
            throw new IllegalArgumentException("Only the recipient of a friend request can reject it");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);
        logger.info("Friend request {} rejected by user {}", requestId, currentUserId);
    }

    /**
     * Cancels an outgoing pending friend request.
     *
     * @param requestId the friendship ID
     * @param currentUserId the ID of the user cancelling their sent request
     */
    @Transactional
    public void cancelFriendRequest(String requestId, String currentUserId) {
        Friendship friendship = getFriendshipOrThrow(requestId);

        if (!friendship.getRequester().getId().equalsIgnoreCase(currentUserId)) {
            logger.warn("Unauthorized attempt to cancel friend request {} by user {}", requestId, currentUserId);
            throw new IllegalArgumentException("Only the sender of a friend request can cancel it");
        }

        friendshipRepository.delete(friendship);
        logger.info("Friend request {} cancelled by sender {}", requestId, currentUserId);
    }

    /**
     * Lists incoming pending friend requests for a user.
     *
     * @param userId the user ID
     * @return list of pending friend requests received by the user
     */
    public List<FriendRequestDto> getIncomingRequests(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return friendshipRepository.findAllByAddresseeAndStatus(user, FriendshipStatus.PENDING).stream()
                .map(this::toFriendRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Lists outgoing pending friend requests sent by a user.
     *
     * @param userId the user ID
     * @return list of pending friend requests sent by the user
     */
    public List<FriendRequestDto> getOutgoingRequests(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return friendshipRepository.findAllByRequesterAndStatus(user, FriendshipStatus.PENDING).stream()
                .map(this::toFriendRequestDto)
                .collect(Collectors.toList());
    }

    /**
     * Lists all accepted friends for a user.
     *
     * @param userId the user ID
     * @return list of active friends for the user
     */
    public List<FriendResponseDto> getFriendsList(String userId) {
        return friendshipRepository.findAllAcceptedFriendships(userId).stream()
                .map(f -> toFriendResponseDto(f, userId))
                .collect(Collectors.toList());
    }

    /**
     * Checks if two users are mutual friends.
     *
     * @param userA first user ID
     * @param userB second user ID
     * @return true if an accepted friendship exists between them
     */
    public boolean areFriends(String userA, String userB) {
        if (userA == null || userB == null) return false;
        return friendshipRepository.findAcceptedFriendship(userA, userB).isPresent();
    }

    private Friendship getFriendshipOrThrow(String requestId) {
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID cannot be null");
        }
        return friendshipRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Friend request not found with ID: " + requestId));
    }

    private FriendRequestDto toFriendRequestDto(Friendship f) {
        return FriendRequestDto.builder()
                .requestId(f.getId())
                .requesterId(f.getRequester().getId())
                .requesterUsername(f.getRequester().getUsername())
                .requesterFullName(f.getRequester().getFullName())
                .requesterLanguage(f.getRequester().getPreferredLanguage())
                .requesterAvatarUrl(f.getRequester().getAvatarUrl())
                .addresseeId(f.getAddressee().getId())
                .addresseeUsername(f.getAddressee().getUsername())
                .status(f.getStatus())
                .createdAt(f.getCreatedAt())
                .build();
    }

    private FriendResponseDto toFriendResponseDto(Friendship f, String currentUserId) {
        User friend = f.getRequester().getId().equalsIgnoreCase(currentUserId) ? f.getAddressee() : f.getRequester();
        return FriendResponseDto.builder()
                .friendId(friend.getId())
                .username(friend.getUsername())
                .fullName(friend.getFullName())
                .preferredLanguage(friend.getPreferredLanguage())
                .avatarUrl(friend.getAvatarUrl())
                .bio(friend.getBio())
                .online(friend.isOnline())
                .friendshipId(f.getId())
                .friendsSince(f.getUpdatedAt())
                .build();
    }
}
