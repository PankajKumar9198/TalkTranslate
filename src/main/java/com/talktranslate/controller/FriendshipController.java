package com.talktranslate.controller;

import com.talktranslate.model.dto.FriendRequestDto;
import com.talktranslate.model.dto.FriendResponseDto;
import com.talktranslate.service.FriendshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Friendship management REST Controller.
 * Handles friend requests, acceptances, rejections, cancellations, and friend lists.
 * Exceptions are handled globally by {@link com.talktranslate.exception.GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    /**
     * Send friend request to target user
     * POST /api/friends/request/{targetUserId}?userId={requesterId}
     */
    @PostMapping("/request/{targetUserId}")
    public ResponseEntity<FriendRequestDto> sendFriendRequest(
            @PathVariable("targetUserId") String targetUserId,
            @RequestParam("userId") String requesterId) {
        FriendRequestDto dto = friendshipService.sendFriendRequest(requesterId, targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Accept an incoming friend request
     * POST /api/friends/accept/{requestId}?userId={currentUserId}
     */
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<FriendResponseDto> acceptFriendRequest(
            @PathVariable("requestId") String requestId,
            @RequestParam("userId") String currentUserId) {
        FriendResponseDto dto = friendshipService.acceptFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Reject an incoming friend request
     * POST /api/friends/reject/{requestId}?userId={currentUserId}
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Map<String, String>> rejectFriendRequest(
            @PathVariable("requestId") String requestId,
            @RequestParam("userId") String currentUserId) {
        friendshipService.rejectFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok(Map.of("message", "Friend request rejected successfully"));
    }

    /**
     * Cancel an outgoing sent request
     * DELETE /api/friends/cancel/{requestId}?userId={currentUserId}
     */
    @DeleteMapping("/cancel/{requestId}")
    public ResponseEntity<Map<String, String>> cancelFriendRequest(
            @PathVariable("requestId") String requestId,
            @RequestParam("userId") String currentUserId) {
        friendshipService.cancelFriendRequest(requestId, currentUserId);
        return ResponseEntity.ok(Map.of("message", "Friend request cancelled successfully"));
    }

    /**
     * List incoming pending friend requests
     * GET /api/friends/requests/incoming?userId={id}
     */
    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendRequestDto>> getIncomingRequests(@RequestParam("userId") String userId) {
        List<FriendRequestDto> requests = friendshipService.getIncomingRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * List outgoing pending friend requests
     * GET /api/friends/requests/outgoing?userId={id}
     */
    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendRequestDto>> getOutgoingRequests(@RequestParam("userId") String userId) {
        List<FriendRequestDto> requests = friendshipService.getOutgoingRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * List all accepted friends for a user
     * GET /api/friends?userId={id}
     */
    @GetMapping
    public ResponseEntity<List<FriendResponseDto>> getFriendsList(@RequestParam("userId") String userId) {
        List<FriendResponseDto> friends = friendshipService.getFriendsList(userId);
        return ResponseEntity.ok(friends);
    }
}
