package com.talktranslate.controller;

import com.talktranslate.model.FriendshipStatus;
import com.talktranslate.model.dto.FriendRequestDto;
import com.talktranslate.model.dto.FriendResponseDto;
import com.talktranslate.service.FriendshipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendshipController.class)
class FriendshipControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private FriendshipService friendshipService;

        @Test
        void shouldSendFriendRequestSuccessfully() throws Exception {
                FriendRequestDto response = FriendRequestDto.builder()
                                .requestId("req_1")
                                .requesterId("usr_1")
                                .requesterUsername("alice")
                                .requesterFullName("Alice Wonderland")
                                .requesterLanguage("en")
                                .requesterAvatarUrl("http://avatar.com/alice.png")
                                .addresseeId("usr_2")
                                .addresseeUsername("bob")
                                .status(FriendshipStatus.PENDING)
                                .createdAt(Instant.parse("2026-09-11T00:00:00Z"))
                                .build();

                when(friendshipService.sendFriendRequest("usr_1", "usr_2")).thenReturn(response);

                mockMvc.perform(post("/api/friends/request/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.requestId").value("req_1"))
                                .andExpect(jsonPath("$.requesterId").value("usr_1"))
                                .andExpect(jsonPath("$.requesterUsername").value("alice"))
                                .andExpect(jsonPath("$.requesterFullName").value("Alice Wonderland"))
                                .andExpect(jsonPath("$.requesterLanguage").value("en"))
                                .andExpect(jsonPath("$.requesterAvatarUrl").value("http://avatar.com/alice.png"))
                                .andExpect(jsonPath("$.addresseeId").value("usr_2"))
                                .andExpect(jsonPath("$.addresseeUsername").value("bob"))
                                .andExpect(jsonPath("$.status").value("PENDING"));

                verify(friendshipService, times(1)).sendFriendRequest("usr_1", "usr_2");
        }

        @Test
        void shouldReturnBadRequestWhenSendFriendRequestThrowsIllegalArgumentException() throws Exception {
                when(friendshipService.sendFriendRequest("usr_1", "usr_1"))
                                .thenThrow(new IllegalArgumentException("Cannot send a friend request to yourself"));

                mockMvc.perform(post("/api/friends/request/usr_1")
                                .param("userId", "usr_1"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message").value("Cannot send a friend request to yourself"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).sendFriendRequest("usr_1", "usr_1");
        }

        @Test
        void shouldReturnBadRequestWithDefaultMessageWhenSendFriendRequestIllegalArgumentExceptionMessageIsNull()
                        throws Exception {
                when(friendshipService.sendFriendRequest("usr_1", "usr_2"))
                                .thenThrow(new IllegalArgumentException((String) null));

                mockMvc.perform(post("/api/friends/request/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message").value("Error"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).sendFriendRequest("usr_1", "usr_2");
        }

        @Test
        void shouldReturnConflictWhenSendFriendRequestThrowsIllegalStateException() throws Exception {
                when(friendshipService.sendFriendRequest("usr_1", "usr_2"))
                                .thenThrow(new IllegalStateException("You are already friends with bob"));

                mockMvc.perform(post("/api/friends/request/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.error").value("Conflict"))
                                .andExpect(jsonPath("$.message").value("You are already friends with bob"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).sendFriendRequest("usr_1", "usr_2");
        }

        @Test
        void shouldReturnConflictWithDefaultMessageWhenSendFriendRequestIllegalStateExceptionMessageIsNull()
                        throws Exception {
                when(friendshipService.sendFriendRequest("usr_1", "usr_2"))
                                .thenThrow(new IllegalStateException((String) null));

                mockMvc.perform(post("/api/friends/request/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.error").value("Conflict"))
                                .andExpect(jsonPath("$.message").value("Error"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).sendFriendRequest("usr_1", "usr_2");
        }

        @Test
        void shouldReturnNotFoundWhenSendFriendRequestThrowsNoSuchElementException() throws Exception {
                when(friendshipService.sendFriendRequest("usr_1", "usr_unknown"))
                                .thenThrow(new NoSuchElementException("Target user not found"));

                mockMvc.perform(post("/api/friends/request/usr_unknown")
                                .param("userId", "usr_1"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("Target user not found"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).sendFriendRequest("usr_1", "usr_unknown");
        }

        @Test
        void shouldReturnNotFoundWithDefaultMessageWhenSendFriendRequestNoSuchElementExceptionMessageIsNull()
                        throws Exception {
                when(friendshipService.sendFriendRequest("usr_1", "usr_unknown"))
                                .thenThrow(new NoSuchElementException((String) null));

                mockMvc.perform(post("/api/friends/request/usr_unknown")
                                .param("userId", "usr_1"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("Error"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).sendFriendRequest("usr_1", "usr_unknown");
        }

        @Test
        void shouldAcceptFriendRequestSuccessfully() throws Exception {
                FriendResponseDto response = FriendResponseDto.builder()
                                .friendshipId("f_1")
                                .friendId("usr_1")
                                .username("user_one")
                                .fullName("User One")
                                .preferredLanguage("en")
                                .avatarUrl("http://avatar.com/one.png")
                                .bio("Hello world")
                                .online(true)
                                .friendsSince(Instant.parse("2026-09-11T00:00:00Z"))
                                .build();

                when(friendshipService.acceptFriendRequest("req_1", "usr_2")).thenReturn(response);

                mockMvc.perform(post("/api/friends/accept/req_1")
                                .param("userId", "usr_2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.friendshipId").value("f_1"))
                                .andExpect(jsonPath("$.friendId").value("usr_1"))
                                .andExpect(jsonPath("$.username").value("user_one"))
                                .andExpect(jsonPath("$.fullName").value("User One"))
                                .andExpect(jsonPath("$.preferredLanguage").value("en"))
                                .andExpect(jsonPath("$.online").value(true));

                verify(friendshipService, times(1)).acceptFriendRequest("req_1", "usr_2");
        }

        @Test
        void shouldReturnBadRequestWhenAcceptFriendRequestThrowsIllegalArgumentException() throws Exception {
                when(friendshipService.acceptFriendRequest("req_1", "usr_3"))
                                .thenThrow(new IllegalArgumentException("Only the recipient can accept"));

                mockMvc.perform(post("/api/friends/accept/req_1")
                                .param("userId", "usr_3"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message").value("Only the recipient can accept"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).acceptFriendRequest("req_1", "usr_3");
        }

        @Test
        void shouldReturnBadRequestWithDefaultMessageWhenAcceptFriendRequestIllegalArgumentExceptionMessageIsNull()
                        throws Exception {
                when(friendshipService.acceptFriendRequest("req_1", "usr_3"))
                                .thenThrow(new IllegalArgumentException((String) null));

                mockMvc.perform(post("/api/friends/accept/req_1")
                                .param("userId", "usr_3"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message").value("Error"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).acceptFriendRequest("req_1", "usr_3");
        }

        @Test
        void shouldReturnNotFoundWhenAcceptFriendRequestThrowsNoSuchElementException() throws Exception {
                when(friendshipService.acceptFriendRequest("req_invalid", "usr_2"))
                                .thenThrow(new NoSuchElementException("Friend request not found"));

                mockMvc.perform(post("/api/friends/accept/req_invalid")
                                .param("userId", "usr_2"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("Friend request not found"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).acceptFriendRequest("req_invalid", "usr_2");
        }

        @Test
        void shouldReturnNotFoundWithDefaultMessageWhenAcceptFriendRequestNoSuchElementExceptionMessageIsNull()
                        throws Exception {
                when(friendshipService.acceptFriendRequest("req_invalid", "usr_2"))
                                .thenThrow(new NoSuchElementException((String) null));

                mockMvc.perform(post("/api/friends/accept/req_invalid")
                                .param("userId", "usr_2"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("Error"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).acceptFriendRequest("req_invalid", "usr_2");
        }

        @Test
        void shouldRejectFriendRequestSuccessfully() throws Exception {
                doNothing().when(friendshipService).rejectFriendRequest("req_1", "usr_2");

                mockMvc.perform(post("/api/friends/reject/req_1")
                                .param("userId", "usr_2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Friend request rejected successfully"));

                verify(friendshipService, times(1)).rejectFriendRequest("req_1", "usr_2");
        }

        @Test
        void shouldReturnBadRequestWhenRejectFriendRequestThrowsIllegalArgumentException() throws Exception {
                doThrow(new IllegalArgumentException("Only recipient can reject"))
                                .when(friendshipService).rejectFriendRequest("req_1", "usr_3");

                mockMvc.perform(post("/api/friends/reject/req_1")
                                .param("userId", "usr_3"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message").value("Only recipient can reject"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).rejectFriendRequest("req_1", "usr_3");
        }

        @Test
        void shouldReturnNotFoundWhenRejectFriendRequestThrowsNoSuchElementException() throws Exception {
                doThrow(new NoSuchElementException("Friend request not found"))
                                .when(friendshipService).rejectFriendRequest("req_invalid", "usr_2");

                mockMvc.perform(post("/api/friends/reject/req_invalid")
                                .param("userId", "usr_2"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("Friend request not found"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).rejectFriendRequest("req_invalid", "usr_2");
        }

        @Test
        void shouldCancelFriendRequestSuccessfully() throws Exception {
                doNothing().when(friendshipService).cancelFriendRequest("req_1", "usr_1");

                mockMvc.perform(delete("/api/friends/cancel/req_1")
                                .param("userId", "usr_1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Friend request cancelled successfully"));

                verify(friendshipService, times(1)).cancelFriendRequest("req_1", "usr_1");
        }

        @Test
        void shouldReturnBadRequestWhenCancelFriendRequestThrowsIllegalArgumentException() throws Exception {
                doThrow(new IllegalArgumentException("Only requester can cancel"))
                                .when(friendshipService).cancelFriendRequest("req_1", "usr_2");

                mockMvc.perform(delete("/api/friends/cancel/req_1")
                                .param("userId", "usr_2"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message").value("Only requester can cancel"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).cancelFriendRequest("req_1", "usr_2");
        }

        @Test
        void shouldReturnNotFoundWhenCancelFriendRequestThrowsNoSuchElementException() throws Exception {
                doThrow(new NoSuchElementException("Friend request not found"))
                                .when(friendshipService).cancelFriendRequest("req_invalid", "usr_1");

                mockMvc.perform(delete("/api/friends/cancel/req_invalid")
                                .param("userId", "usr_1"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("Friend request not found"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).cancelFriendRequest("req_invalid", "usr_1");
        }

        @Test
        void shouldGetIncomingRequestsSuccessfully() throws Exception {
                FriendRequestDto req1 = FriendRequestDto.builder()
                                .requestId("req_1")
                                .requesterId("usr_1")
                                .requesterUsername("alice")
                                .addresseeId("usr_2")
                                .status(FriendshipStatus.PENDING)
                                .build();

                when(friendshipService.getIncomingRequests("usr_2")).thenReturn(List.of(req1));

                mockMvc.perform(get("/api/friends/requests/incoming")
                                .param("userId", "usr_2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].requestId").value("req_1"))
                                .andExpect(jsonPath("$[0].requesterUsername").value("alice"));

                verify(friendshipService, times(1)).getIncomingRequests("usr_2");
        }

        @Test
        void shouldReturnEmptyListWhenNoIncomingRequests() throws Exception {
                when(friendshipService.getIncomingRequests("usr_2")).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/api/friends/requests/incoming")
                                .param("userId", "usr_2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(0));

                verify(friendshipService, times(1)).getIncomingRequests("usr_2");
        }

        @Test
        void shouldReturnNotFoundWhenGetIncomingRequestsThrowsNoSuchElementException() throws Exception {
                when(friendshipService.getIncomingRequests("usr_unknown"))
                                .thenThrow(new NoSuchElementException("User not found"));

                mockMvc.perform(get("/api/friends/requests/incoming")
                                .param("userId", "usr_unknown"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("User not found"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).getIncomingRequests("usr_unknown");
        }

        @Test
        void shouldGetOutgoingRequestsSuccessfully() throws Exception {
                FriendRequestDto req1 = FriendRequestDto.builder()
                                .requestId("req_1")
                                .requesterId("usr_1")
                                .addresseeId("usr_2")
                                .addresseeUsername("bob")
                                .status(FriendshipStatus.PENDING)
                                .build();

                when(friendshipService.getOutgoingRequests("usr_1")).thenReturn(List.of(req1));

                mockMvc.perform(get("/api/friends/requests/outgoing")
                                .param("userId", "usr_1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].requestId").value("req_1"))
                                .andExpect(jsonPath("$[0].addresseeUsername").value("bob"));

                verify(friendshipService, times(1)).getOutgoingRequests("usr_1");
        }

        @Test
        void shouldReturnEmptyListWhenNoOutgoingRequests() throws Exception {
                when(friendshipService.getOutgoingRequests("usr_1")).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/api/friends/requests/outgoing")
                                .param("userId", "usr_1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(0));

                verify(friendshipService, times(1)).getOutgoingRequests("usr_1");
        }

        @Test
        void shouldReturnNotFoundWhenGetOutgoingRequestsThrowsNoSuchElementException() throws Exception {
                when(friendshipService.getOutgoingRequests("usr_unknown"))
                                .thenThrow(new NoSuchElementException("User not found"));

                mockMvc.perform(get("/api/friends/requests/outgoing")
                                .param("userId", "usr_unknown"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("User not found"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).getOutgoingRequests("usr_unknown");
        }

        @Test
        void shouldGetFriendsListSuccessfully() throws Exception {
                FriendResponseDto f1 = FriendResponseDto.builder()
                                .friendshipId("f_1")
                                .friendId("usr_2")
                                .username("user_two")
                                .fullName("User Two")
                                .preferredLanguage("hi")
                                .online(true)
                                .build();

                when(friendshipService.getFriendsList("usr_1")).thenReturn(List.of(f1));

                mockMvc.perform(get("/api/friends")
                                .param("userId", "usr_1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(1))
                                .andExpect(jsonPath("$[0].friendshipId").value("f_1"))
                                .andExpect(jsonPath("$[0].friendId").value("usr_2"))
                                .andExpect(jsonPath("$[0].username").value("user_two"))
                                .andExpect(jsonPath("$[0].fullName").value("User Two"))
                                .andExpect(jsonPath("$[0].preferredLanguage").value("hi"))
                                .andExpect(jsonPath("$[0].online").value(true));

                verify(friendshipService, times(1)).getFriendsList("usr_1");
        }

        @Test
        void shouldReturnEmptyListWhenNoFriends() throws Exception {
                when(friendshipService.getFriendsList("usr_1")).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/api/friends")
                                .param("userId", "usr_1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(0));

                verify(friendshipService, times(1)).getFriendsList("usr_1");
        }

        @Test
        void shouldReturnNotFoundWhenGetFriendsListThrowsNoSuchElementException() throws Exception {
                when(friendshipService.getFriendsList("usr_unknown"))
                                .thenThrow(new NoSuchElementException("User not found"));

                mockMvc.perform(get("/api/friends")
                                .param("userId", "usr_unknown"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.error").value("Not Found"))
                                .andExpect(jsonPath("$.message").value("User not found"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(friendshipService, times(1)).getFriendsList("usr_unknown");
        }
}
