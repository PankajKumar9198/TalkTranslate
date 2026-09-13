package com.talktranslate.service;

import com.talktranslate.model.Friendship;
import com.talktranslate.model.FriendshipStatus;
import com.talktranslate.model.User;
import com.talktranslate.model.dto.FriendRequestDto;
import com.talktranslate.model.dto.FriendResponseDto;
import com.talktranslate.repository.FriendshipRepository;
import com.talktranslate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

        @Mock
        private FriendshipRepository friendshipRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private SimpMessagingTemplate messagingTemplate;

        @InjectMocks
        private FriendshipService friendshipService;

        private User userRahul;
        private User userSarah;
        private Friendship pendingFriendship;
        private Friendship acceptedFriendship;

        @BeforeEach
        void setUp() {
                userRahul = User.builder()
                                .id("usr_rahul")
                                .username("rahul_sharma")
                                .email("rahul@test.com")
                                .fullName("Rahul Sharma")
                                .preferredLanguage("hi")
                                .avatarUrl("https://img.example.com/rahul.jpg")
                                .bio("Software Engineer from Delhi")
                                .online(true)
                                .build();

                userSarah = User.builder()
                                .id("usr_sarah")
                                .username("sarah_jenkins")
                                .email("sarah@test.com")
                                .fullName("Sarah Jenkins")
                                .preferredLanguage("en")
                                .avatarUrl("https://img.example.com/sarah.jpg")
                                .bio("Product Designer from London")
                                .online(false)
                                .build();

                pendingFriendship = Friendship.builder()
                                .id("req_100")
                                .requester(userRahul)
                                .addressee(userSarah)
                                .status(FriendshipStatus.PENDING)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();

                acceptedFriendship = Friendship.builder()
                                .id("f_200")
                                .requester(userRahul)
                                .addressee(userSarah)
                                .status(FriendshipStatus.ACCEPTED)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
        }

        @Test
        void shouldThrowExceptionWhenRequesterIdIsNullInSendFriendRequest() {
                assertThatThrownBy(() -> friendshipService.sendFriendRequest(null, "usr_sarah"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Requester ID and Addressee ID cannot be null");

                verifyNoInteractions(userRepository, friendshipRepository, messagingTemplate);
        }

        @Test
        void shouldThrowExceptionWhenAddresseeIdIsNullInSendFriendRequest() {
                assertThatThrownBy(() -> friendshipService.sendFriendRequest("usr_rahul", null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Requester ID and Addressee ID cannot be null");

                verifyNoInteractions(userRepository, friendshipRepository, messagingTemplate);
        }

        @Test
        void shouldThrowExceptionWhenBothRequesterAndAddresseeIdAreNullInSendFriendRequest() {
                assertThatThrownBy(() -> friendshipService.sendFriendRequest(null, null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Requester ID and Addressee ID cannot be null");

                verifyNoInteractions(userRepository, friendshipRepository, messagingTemplate);
        }

        @ParameterizedTest(name = "Reject self friend request with matching IDs: [{0}] and [{1}]")
        @ValueSource(strings = { "usr_rahul", "USR_RAHUL", "Usr_Rahul" })
        void shouldRejectFriendRequestWhenSendingToSelf(String addresseeId) {
                assertThatThrownBy(() -> friendshipService.sendFriendRequest("usr_rahul", addresseeId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Cannot send a friend request to yourself");

                verifyNoInteractions(userRepository, friendshipRepository, messagingTemplate);
        }

        @Test
        void shouldThrowExceptionWhenRequesterNotFoundInSendFriendRequest() {
                when(userRepository.findById("usr_non_existent")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> friendshipService.sendFriendRequest("usr_non_existent", "usr_sarah"))
                                .isInstanceOf(NoSuchElementException.class)
                                .hasMessage("Requester user not found");

                verify(userRepository, times(1)).findById("usr_non_existent");
                verify(userRepository, never()).findById("usr_sarah");
                verifyNoInteractions(friendshipRepository, messagingTemplate);
        }

        @Test
        void shouldThrowExceptionWhenTargetUserNotFoundInSendFriendRequest() {
                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_non_existent")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> friendshipService.sendFriendRequest("usr_rahul", "usr_non_existent"))
                                .isInstanceOf(NoSuchElementException.class)
                                .hasMessage("Target user not found");

                verify(userRepository, times(1)).findById("usr_rahul");
                verify(userRepository, times(1)).findById("usr_non_existent");
                verifyNoInteractions(friendshipRepository, messagingTemplate);
        }

        @Test
        void shouldSendFriendRequestSuccessfullyWhenNoPreviousFriendshipExists() {
                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findFriendshipBetween("usr_rahul", "usr_sarah")).thenReturn(Optional.empty());

                Friendship savedFriendship = Friendship.builder()
                                .id("req_100")
                                .requester(userRahul)
                                .addressee(userSarah)
                                .status(FriendshipStatus.PENDING)
                                .createdAt(Instant.now())
                                .build();

                when(friendshipRepository.save(any(Friendship.class))).thenReturn(savedFriendship);

                FriendRequestDto result = friendshipService.sendFriendRequest("usr_rahul", "usr_sarah");

                assertAll("Validate Created Friend Request DTO",
                                () -> assertThat(result).isNotNull(),
                                () -> assertThat(result.getRequestId()).isEqualTo("req_100"),
                                () -> assertThat(result.getRequesterId()).isEqualTo("usr_rahul"),
                                () -> assertThat(result.getRequesterUsername()).isEqualTo("rahul_sharma"),
                                () -> assertThat(result.getRequesterFullName()).isEqualTo("Rahul Sharma"),
                                () -> assertThat(result.getRequesterLanguage()).isEqualTo("hi"),
                                () -> assertThat(result.getRequesterAvatarUrl())
                                                .isEqualTo("https://img.example.com/rahul.jpg"),
                                () -> assertThat(result.getAddresseeId()).isEqualTo("usr_sarah"),
                                () -> assertThat(result.getAddresseeUsername()).isEqualTo("sarah_jenkins"),
                                () -> assertThat(result.getStatus()).isEqualTo(FriendshipStatus.PENDING),
                                () -> assertThat(result.getCreatedAt()).isNotNull());

                ArgumentCaptor<Friendship> friendshipCaptor = ArgumentCaptor.forClass(Friendship.class);
                verify(friendshipRepository, times(1)).save(friendshipCaptor.capture());
                Friendship captured = friendshipCaptor.getValue();
                assertThat(captured.getRequester()).isEqualTo(userRahul);
                assertThat(captured.getAddressee()).isEqualTo(userSarah);
                assertThat(captured.getStatus()).isEqualTo(FriendshipStatus.PENDING);

                verify(messagingTemplate, times(1)).convertAndSendToUser(
                                eq("usr_sarah"),
                                eq("/queue/notifications"),
                                any(FriendRequestDto.class));
        }

        @Test
        void shouldSendFriendRequestSuccessfullyEvenIfWebSocketNotificationFails() {
                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findFriendshipBetween("usr_rahul", "usr_sarah")).thenReturn(Optional.empty());

                Friendship savedFriendship = Friendship.builder()
                                .id("req_100")
                                .requester(userRahul)
                                .addressee(userSarah)
                                .status(FriendshipStatus.PENDING)
                                .build();

                when(friendshipRepository.save(any(Friendship.class))).thenReturn(savedFriendship);
                doThrow(new RuntimeException("WebSocket connection failed"))
                                .when(messagingTemplate).convertAndSendToUser(any(), any(), any());

                FriendRequestDto result = friendshipService.sendFriendRequest("usr_rahul", "usr_sarah");

                assertThat(result).isNotNull();
                assertThat(result.getRequestId()).isEqualTo("req_100");
                verify(friendshipRepository, times(1)).save(any(Friendship.class));
                verify(messagingTemplate, times(1)).convertAndSendToUser(any(), any(), any());
        }

        @Test
        void shouldPreventDuplicateFriendRequestWhenAlreadyPending() {
                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findFriendshipBetween("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(pendingFriendship));

                assertThatThrownBy(() -> friendshipService.sendFriendRequest("usr_rahul", "usr_sarah"))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("A friend request is already pending between you two");

                verify(friendshipRepository, never()).save(any(Friendship.class));
                verifyNoInteractions(messagingTemplate);
        }

        @Test
        void shouldPreventFriendRequestWhenAlreadyAcceptedFriends() {
                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findFriendshipBetween("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(acceptedFriendship));

                assertThatThrownBy(() -> friendshipService.sendFriendRequest("usr_rahul", "usr_sarah"))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("You are already friends with sarah_jenkins");

                verify(friendshipRepository, never()).save(any(Friendship.class));
                verifyNoInteractions(messagingTemplate);
        }

        @Test
        void shouldPreventFriendRequestWhenFriendshipIsBlocked() {
                Friendship blockedFriendship = Friendship.builder()
                                .id("req_blocked")
                                .requester(userRahul)
                                .addressee(userSarah)
                                .status(FriendshipStatus.BLOCKED)
                                .build();

                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findFriendshipBetween("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(blockedFriendship));

                assertThatThrownBy(() -> friendshipService.sendFriendRequest("usr_rahul", "usr_sarah"))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("Unable to send friend request to this user");

                verify(friendshipRepository, never()).save(any(Friendship.class));
                verifyNoInteractions(messagingTemplate);
        }

        @Test
        void shouldReopenFriendRequestAsPendingWhenPreviouslyRejected() {
                Friendship rejectedFriendship = Friendship.builder()
                                .id("req_rejected")
                                .requester(userSarah)
                                .addressee(userRahul)
                                .status(FriendshipStatus.REJECTED)
                                .build();

                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findFriendshipBetween("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(rejectedFriendship));
                when(friendshipRepository.save(any(Friendship.class))).thenAnswer(i -> i.getArgument(0));

                FriendRequestDto result = friendshipService.sendFriendRequest("usr_rahul", "usr_sarah");

                assertThat(result).isNotNull();
                assertThat(result.getRequestId()).isEqualTo("req_rejected");
                assertThat(result.getRequesterId()).isEqualTo("usr_rahul");
                assertThat(result.getAddresseeId()).isEqualTo("usr_sarah");
                assertThat(result.getStatus()).isEqualTo(FriendshipStatus.PENDING);

                assertThat(rejectedFriendship.getRequester()).isEqualTo(userRahul);
                assertThat(rejectedFriendship.getAddressee()).isEqualTo(userSarah);
                assertThat(rejectedFriendship.getStatus()).isEqualTo(FriendshipStatus.PENDING);

                verify(friendshipRepository, times(1)).save(rejectedFriendship);
                verify(messagingTemplate, times(1)).convertAndSendToUser(
                                eq("usr_sarah"),
                                eq("/queue/notifications"),
                                any(FriendRequestDto.class));
        }

        @Test
        void shouldThrowExceptionWhenRequestIdIsNullInAccept() {
                assertThatThrownBy(() -> friendshipService.acceptFriendRequest(null, "usr_sarah"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Request ID cannot be null");

                verifyNoInteractions(friendshipRepository, messagingTemplate);
        }

        @Test
        void shouldThrowExceptionWhenFriendRequestNotFoundInAccept() {
                when(friendshipRepository.findById("req_non_existent")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> friendshipService.acceptFriendRequest("req_non_existent", "usr_sarah"))
                                .isInstanceOf(NoSuchElementException.class)
                                .hasMessage("Friend request not found with ID: req_non_existent");

                verify(friendshipRepository, times(1)).findById("req_non_existent");
                verifyNoInteractions(messagingTemplate);
        }

        @Test
        void shouldThrowExceptionWhenNonRecipientTriesToAccept() {
                when(friendshipRepository.findById("req_100")).thenReturn(Optional.of(pendingFriendship));

                assertThatThrownBy(() -> friendshipService.acceptFriendRequest("req_100", "usr_rahul"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Only the recipient of a friend request can accept it");

                verify(friendshipRepository, never()).save(any(Friendship.class));
                verifyNoInteractions(messagingTemplate);
        }

        @Test
        void shouldThrowExceptionWhenThirdPartyUserTriesToAccept() {
                when(friendshipRepository.findById("req_100")).thenReturn(Optional.of(pendingFriendship));

                assertThatThrownBy(() -> friendshipService.acceptFriendRequest("req_100", "usr_priya"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Only the recipient of a friend request can accept it");

                verify(friendshipRepository, never()).save(any(Friendship.class));
                verifyNoInteractions(messagingTemplate);
        }

        @Test
        void shouldReturnDtoWithoutResavingWhenAlreadyAccepted() {
                when(friendshipRepository.findById("f_200")).thenReturn(Optional.of(acceptedFriendship));

                FriendResponseDto result = friendshipService.acceptFriendRequest("f_200", "usr_sarah");

                assertAll("Validate Idempotent Accepted Friendship DTO",
                                () -> assertThat(result).isNotNull(),
                                () -> assertThat(result.getFriendId()).isEqualTo("usr_rahul"),
                                () -> assertThat(result.getUsername()).isEqualTo("rahul_sharma"),
                                () -> assertThat(result.getFullName()).isEqualTo("Rahul Sharma"),
                                () -> assertThat(result.getPreferredLanguage()).isEqualTo("hi"),
                                () -> assertThat(result.getFriendshipId()).isEqualTo("f_200"));

                verify(friendshipRepository, never()).save(any(Friendship.class));
                verifyNoInteractions(messagingTemplate);
        }

        @Test
        void shouldAcceptFriendRequestSuccessfully() {
                when(friendshipRepository.findById("req_100")).thenReturn(Optional.of(pendingFriendship));
                when(friendshipRepository.save(any(Friendship.class))).thenAnswer(i -> i.getArgument(0));

                FriendResponseDto result = friendshipService.acceptFriendRequest("req_100", "usr_sarah");

                assertAll("Validate Accepted Friendship DTO and State",
                                () -> assertThat(result).isNotNull(),
                                () -> assertThat(result.getFriendId()).isEqualTo("usr_rahul"),
                                () -> assertThat(result.getUsername()).isEqualTo("rahul_sharma"),
                                () -> assertThat(result.getFullName()).isEqualTo("Rahul Sharma"),
                                () -> assertThat(result.getPreferredLanguage()).isEqualTo("hi"),
                                () -> assertThat(result.getAvatarUrl()).isEqualTo("https://img.example.com/rahul.jpg"),
                                () -> assertThat(result.getBio()).isEqualTo("Software Engineer from Delhi"),
                                () -> assertThat(result.isOnline()).isTrue(),
                                () -> assertThat(result.getFriendshipId()).isEqualTo("req_100"),
                                () -> assertThat(pendingFriendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED));

                verify(friendshipRepository, times(1)).save(pendingFriendship);
                verify(messagingTemplate, times(1)).convertAndSendToUser(
                                eq("usr_rahul"),
                                eq("/queue/notifications"),
                                any(FriendResponseDto.class));
        }

        @Test
        void shouldAcceptFriendRequestSuccessfullyEvenIfWebSocketNotificationFails() {
                when(friendshipRepository.findById("req_100")).thenReturn(Optional.of(pendingFriendship));
                when(friendshipRepository.save(any(Friendship.class))).thenAnswer(i -> i.getArgument(0));
                doThrow(new RuntimeException("WebSocket transmission error"))
                                .when(messagingTemplate).convertAndSendToUser(any(), any(), any());

                FriendResponseDto result = friendshipService.acceptFriendRequest("req_100", "usr_sarah");

                assertThat(result).isNotNull();
                assertThat(result.getFriendId()).isEqualTo("usr_rahul");
                assertThat(pendingFriendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
                verify(friendshipRepository, times(1)).save(pendingFriendship);
                verify(messagingTemplate, times(1)).convertAndSendToUser(any(), any(), any());
        }

        @Test
        void shouldThrowExceptionWhenRequestIdIsNullInReject() {
                assertThatThrownBy(() -> friendshipService.rejectFriendRequest(null, "usr_sarah"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Request ID cannot be null");

                verifyNoInteractions(friendshipRepository, messagingTemplate);
        }

        @Test
        void shouldThrowExceptionWhenFriendRequestNotFoundInReject() {
                when(friendshipRepository.findById("req_non_existent")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> friendshipService.rejectFriendRequest("req_non_existent", "usr_sarah"))
                                .isInstanceOf(NoSuchElementException.class)
                                .hasMessage("Friend request not found with ID: req_non_existent");

                verify(friendshipRepository, times(1)).findById("req_non_existent");
                verifyNoInteractions(messagingTemplate);
        }

        @Test
        void shouldThrowExceptionWhenNonRecipientTriesToReject() {
                when(friendshipRepository.findById("req_100")).thenReturn(Optional.of(pendingFriendship));

                assertThatThrownBy(() -> friendshipService.rejectFriendRequest("req_100", "usr_rahul"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Only the recipient of a friend request can reject it");

                verify(friendshipRepository, never()).save(any(Friendship.class));
        }

        @Test
        void shouldRejectFriendRequestSuccessfully() {
                when(friendshipRepository.findById("req_100")).thenReturn(Optional.of(pendingFriendship));

                friendshipService.rejectFriendRequest("req_100", "usr_sarah");

                assertThat(pendingFriendship.getStatus()).isEqualTo(FriendshipStatus.REJECTED);
                verify(friendshipRepository, times(1)).save(pendingFriendship);
        }

        @Test
        void shouldThrowExceptionWhenRequestIdIsNullInCancel() {
                assertThatThrownBy(() -> friendshipService.cancelFriendRequest(null, "usr_rahul"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Request ID cannot be null");

                verifyNoInteractions(friendshipRepository);
        }

        @Test
        void shouldThrowExceptionWhenFriendRequestNotFoundInCancel() {
                when(friendshipRepository.findById("req_non_existent")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> friendshipService.cancelFriendRequest("req_non_existent", "usr_rahul"))
                                .isInstanceOf(NoSuchElementException.class)
                                .hasMessage("Friend request not found with ID: req_non_existent");

                verify(friendshipRepository, times(1)).findById("req_non_existent");
        }

        @Test
        void shouldThrowExceptionWhenNonSenderTriesToCancel() {
                when(friendshipRepository.findById("req_100")).thenReturn(Optional.of(pendingFriendship));

                assertThatThrownBy(() -> friendshipService.cancelFriendRequest("req_100", "usr_sarah"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Only the sender of a friend request can cancel it");

                verify(friendshipRepository, never()).delete(any(Friendship.class));
        }

        @Test
        void shouldCancelFriendRequestSuccessfully() {
                when(friendshipRepository.findById("req_100")).thenReturn(Optional.of(pendingFriendship));

                friendshipService.cancelFriendRequest("req_100", "usr_rahul");

                verify(friendshipRepository, times(1)).delete(pendingFriendship);
        }

        @Test
        void shouldThrowExceptionWhenUserNotFoundInIncomingRequests() {
                when(userRepository.findById("usr_non_existent")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> friendshipService.getIncomingRequests("usr_non_existent"))
                                .isInstanceOf(NoSuchElementException.class)
                                .hasMessage("User not found");

                verify(userRepository, times(1)).findById("usr_non_existent");
                verify(friendshipRepository, never()).findAllByAddresseeAndStatus(any(), any());
        }

        @Test
        void shouldReturnIncomingPendingRequests() {
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findAllByAddresseeAndStatus(userSarah, FriendshipStatus.PENDING))
                                .thenReturn(List.of(pendingFriendship));

                List<FriendRequestDto> list = friendshipService.getIncomingRequests("usr_sarah");

                assertThat(list).hasSize(1);
                FriendRequestDto dto = list.get(0);
                assertThat(dto.getRequestId()).isEqualTo("req_100");
                assertThat(dto.getRequesterId()).isEqualTo("usr_rahul");
                assertThat(dto.getRequesterUsername()).isEqualTo("rahul_sharma");
                assertThat(dto.getAddresseeId()).isEqualTo("usr_sarah");
                assertThat(dto.getStatus()).isEqualTo(FriendshipStatus.PENDING);

                verify(userRepository, times(1)).findById("usr_sarah");
                verify(friendshipRepository, times(1)).findAllByAddresseeAndStatus(userSarah, FriendshipStatus.PENDING);
        }

        @Test
        void shouldReturnEmptyListWhenNoIncomingRequestsExist() {
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findAllByAddresseeAndStatus(userSarah, FriendshipStatus.PENDING))
                                .thenReturn(Collections.emptyList());

                List<FriendRequestDto> list = friendshipService.getIncomingRequests("usr_sarah");

                assertThat(list).isNotNull().isEmpty();
                verify(userRepository, times(1)).findById("usr_sarah");
                verify(friendshipRepository, times(1)).findAllByAddresseeAndStatus(userSarah, FriendshipStatus.PENDING);
        }

        @Test
        void shouldThrowExceptionWhenUserNotFoundInOutgoingRequests() {
                when(userRepository.findById("usr_non_existent")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> friendshipService.getOutgoingRequests("usr_non_existent"))
                                .isInstanceOf(NoSuchElementException.class)
                                .hasMessage("User not found");

                verify(userRepository, times(1)).findById("usr_non_existent");
                verify(friendshipRepository, never()).findAllByRequesterAndStatus(any(), any());
        }

        @Test
        void shouldReturnOutgoingPendingRequests() {
                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(friendshipRepository.findAllByRequesterAndStatus(userRahul, FriendshipStatus.PENDING))
                                .thenReturn(List.of(pendingFriendship));

                List<FriendRequestDto> list = friendshipService.getOutgoingRequests("usr_rahul");

                assertThat(list).hasSize(1);
                FriendRequestDto dto = list.get(0);
                assertThat(dto.getRequestId()).isEqualTo("req_100");
                assertThat(dto.getRequesterId()).isEqualTo("usr_rahul");
                assertThat(dto.getAddresseeId()).isEqualTo("usr_sarah");
                assertThat(dto.getAddresseeUsername()).isEqualTo("sarah_jenkins");
                assertThat(dto.getStatus()).isEqualTo(FriendshipStatus.PENDING);

                verify(userRepository, times(1)).findById("usr_rahul");
                verify(friendshipRepository, times(1)).findAllByRequesterAndStatus(userRahul, FriendshipStatus.PENDING);
        }

        @Test
        void shouldReturnEmptyListWhenNoOutgoingRequestsExist() {
                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(friendshipRepository.findAllByRequesterAndStatus(userRahul, FriendshipStatus.PENDING))
                                .thenReturn(Collections.emptyList());

                List<FriendRequestDto> list = friendshipService.getOutgoingRequests("usr_rahul");

                assertThat(list).isNotNull().isEmpty();
                verify(userRepository, times(1)).findById("usr_rahul");
                verify(friendshipRepository, times(1)).findAllByRequesterAndStatus(userRahul, FriendshipStatus.PENDING);
        }

        @Test
        void shouldReturnFriendsListWhenUserIsAddressee() {
                when(friendshipRepository.findAllAcceptedFriendships("usr_sarah"))
                                .thenReturn(List.of(acceptedFriendship));

                List<FriendResponseDto> list = friendshipService.getFriendsList("usr_sarah");

                assertThat(list).hasSize(1);
                FriendResponseDto dto = list.get(0);
                assertThat(dto.getFriendId()).isEqualTo("usr_rahul");
                assertThat(dto.getUsername()).isEqualTo("rahul_sharma");
                assertThat(dto.getFullName()).isEqualTo("Rahul Sharma");
                assertThat(dto.getPreferredLanguage()).isEqualTo("hi");
                assertThat(dto.getAvatarUrl()).isEqualTo("https://img.example.com/rahul.jpg");
                assertThat(dto.getBio()).isEqualTo("Software Engineer from Delhi");
                assertThat(dto.isOnline()).isTrue();
                assertThat(dto.getFriendshipId()).isEqualTo("f_200");

                verify(friendshipRepository, times(1)).findAllAcceptedFriendships("usr_sarah");
        }

        @Test
        void shouldReturnFriendsListWhenUserIsRequester() {
                when(friendshipRepository.findAllAcceptedFriendships("usr_rahul"))
                                .thenReturn(List.of(acceptedFriendship));

                List<FriendResponseDto> list = friendshipService.getFriendsList("usr_rahul");

                assertThat(list).hasSize(1);
                FriendResponseDto dto = list.get(0);
                assertThat(dto.getFriendId()).isEqualTo("usr_sarah");
                assertThat(dto.getUsername()).isEqualTo("sarah_jenkins");
                assertThat(dto.getFullName()).isEqualTo("Sarah Jenkins");
                assertThat(dto.getPreferredLanguage()).isEqualTo("en");
                assertThat(dto.getAvatarUrl()).isEqualTo("https://img.example.com/sarah.jpg");
                assertThat(dto.getBio()).isEqualTo("Product Designer from London");
                assertThat(dto.isOnline()).isFalse();
                assertThat(dto.getFriendshipId()).isEqualTo("f_200");

                verify(friendshipRepository, times(1)).findAllAcceptedFriendships("usr_rahul");
        }

        @Test
        void shouldReturnEmptyListWhenUserHasNoFriends() {
                when(friendshipRepository.findAllAcceptedFriendships("usr_rahul"))
                                .thenReturn(Collections.emptyList());

                List<FriendResponseDto> list = friendshipService.getFriendsList("usr_rahul");

                assertThat(list).isNotNull().isEmpty();
                verify(friendshipRepository, times(1)).findAllAcceptedFriendships("usr_rahul");
        }

        @Test
        void shouldReturnFalseWhenUserAIsNullInAreFriends() {
                boolean result = friendshipService.areFriends(null, "usr_sarah");

                assertThat(result).isFalse();
                verifyNoInteractions(friendshipRepository);
        }

        @Test
        void shouldReturnFalseWhenUserBIsNullInAreFriends() {
                boolean result = friendshipService.areFriends("usr_rahul", null);

                assertThat(result).isFalse();
                verifyNoInteractions(friendshipRepository);
        }

        @Test
        void shouldReturnFalseWhenBothUsersAreNullInAreFriends() {
                boolean result = friendshipService.areFriends(null, null);

                assertThat(result).isFalse();
                verifyNoInteractions(friendshipRepository);
        }

        @Test
        void shouldReturnTrueWhenAcceptedFriendshipExists() {
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(acceptedFriendship));

                boolean result = friendshipService.areFriends("usr_rahul", "usr_sarah");

                assertThat(result).isTrue();
                verify(friendshipRepository, times(1)).findAcceptedFriendship("usr_rahul", "usr_sarah");
        }

        @Test
        void shouldReturnFalseWhenNoAcceptedFriendshipExists() {
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.empty());

                boolean result = friendshipService.areFriends("usr_rahul", "usr_sarah");

                assertThat(result).isFalse();
                verify(friendshipRepository, times(1)).findAcceptedFriendship("usr_rahul", "usr_sarah");
        }
}
