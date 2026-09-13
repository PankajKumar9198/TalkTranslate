package com.talktranslate;

import com.talktranslate.model.ChatMessage;
import com.talktranslate.model.Friendship;
import com.talktranslate.model.FriendshipStatus;
import com.talktranslate.model.MessageType;
import com.talktranslate.model.User;
import com.talktranslate.model.dto.AuthResponse;
import com.talktranslate.model.dto.LoginRequest;
import com.talktranslate.model.dto.SignUpRequest;
import com.talktranslate.repository.ChatMessageRepository;
import com.talktranslate.repository.FriendshipRepository;
import com.talktranslate.repository.UserRepository;
import com.talktranslate.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * End-to-end integration tests running against in-memory H2 SQL database.
 * Validates real database queries, constraint handling, JPQL queries, and
 * authentication.
 */
@SpringBootTest
@Transactional
class TalkTranslateIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private AuthService authService;

    private User userAlice;
    private User userBob;
    private User userCharlie;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        friendshipRepository.deleteAll();
        userRepository.deleteAll();

        userAlice = userRepository.save(User.builder()
                .username("alice_wonder")
                .email("alice@example.com")
                .passwordHash("$2a$10$encryptedHashAlice")
                .fullName("Alice Wonderland")
                .preferredLanguage("en")
                .online(true)
                .build());

        userBob = userRepository.save(User.builder()
                .username("bob_builder")
                .email("bob@example.com")
                .passwordHash("$2a$10$encryptedHashBob")
                .fullName("Bob Builder")
                .preferredLanguage("es")
                .online(true)
                .build());

        userCharlie = userRepository.save(User.builder()
                .username("charlie_chap")
                .email("charlie@example.com")
                .passwordHash("$2a$10$encryptedHashCharlie")
                .fullName("Charlie Chaplin")
                .preferredLanguage("fr")
                .online(false)
                .build());
    }

    @Test
    void shouldFindUserByIdentifierAndUsernameOrEmailInH2() {
        Optional<User> byUsername = userRepository.findByIdentifier("alice_wonder");
        Optional<User> byEmail = userRepository.findByIdentifier("alice@example.com");
        Optional<User> byBoth = userRepository.findByUsernameOrEmail("bob_builder", "bob@example.com");

        assertAll(
                () -> assertThat(byUsername).isPresent(),
                () -> assertThat(byUsername.get().getId()).isEqualTo(userAlice.getId()),
                () -> assertThat(byEmail).isPresent(),
                () -> assertThat(byEmail.get().getId()).isEqualTo(userAlice.getId()),
                () -> assertThat(byBoth).isPresent(),
                () -> assertThat(byBoth.get().getId()).isEqualTo(userBob.getId()));
    }

    @Test
    void shouldFindSuggestedUsersUsingDatabaseSubqueryInH2() {
        // Create friendship between Alice and Bob
        Friendship friendship = new Friendship();
        friendship.setRequester(userAlice);
        friendship.setAddressee(userBob);
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        // Suggested users for Alice should be Charlie only (Bob is connected, Alice is
        // self)
        List<User> suggestionsForAlice = userRepository.findSuggestedUsers(userAlice.getId());

        assertThat(suggestionsForAlice)
                .hasSize(1)
                .extracting(User::getUsername)
                .containsExactly("charlie_chap");
    }

    @Test
    void shouldPersistAndRetrievePaginatedChatHistoryInH2() {
        // Save 5 messages between Alice and Bob
        for (int i = 1; i <= 5; i++) {
            ChatMessage msg = new ChatMessage();
            msg.setSenderId(userAlice.getId());
            msg.setRecipientId(userBob.getId());
            msg.setSenderName(userAlice.getFullName());
            msg.setRecipientName(userBob.getFullName());
            msg.setOriginalText("Message " + i);
            msg.setTranslatedText("Mensaje " + i);
            msg.setSenderLang("en");
            msg.setRecipientLang("es");
            msg.setType(MessageType.CHAT);
            msg.setCreatedAt(Instant.now().plusMillis(i * 100));
            chatMessageRepository.save(msg);
        }

        // Test unpaginated query
        List<ChatMessage> allMessages = chatMessageRepository.findChatHistoryBetween(userAlice.getId(),
                userBob.getId());
        assertThat(allMessages).hasSize(5);

        // Test paginated query (page 0, size 2)
        Page<ChatMessage> page0 = chatMessageRepository.findChatHistoryBetween(userAlice.getId(), userBob.getId(),
                PageRequest.of(0, 2));
        assertThat(page0.getContent()).hasSize(2);
        assertThat(page0.getTotalElements()).isEqualTo(5);
        assertThat(page0.getTotalPages()).isEqualTo(3);

        // Test paginated query (page 1, size 2)
        Page<ChatMessage> page1 = chatMessageRepository.findChatHistoryBetween(userAlice.getId(), userBob.getId(),
                PageRequest.of(1, 2));
        assertThat(page1.getContent()).hasSize(2);

        // Test paginated query (page 2, size 2)
        Page<ChatMessage> page2 = chatMessageRepository.findChatHistoryBetween(userAlice.getId(), userBob.getId(),
                PageRequest.of(2, 2));
        assertThat(page2.getContent()).hasSize(1);
    }

    @Test
    void shouldPerformFullSignUpAndLoginFlowInH2() {
        SignUpRequest signUpRequest = new SignUpRequest("david_copper", "david@test.com", "Password123!",
                "David Copperfield");
        AuthResponse signUpResponse = authService.signUp(signUpRequest);

        assertThat(signUpResponse).isNotNull();
        assertThat(signUpResponse.getToken()).isNotBlank();
        assertThat(signUpResponse.getUsername()).isEqualTo("david_copper");

        LoginRequest loginRequest = new LoginRequest("david_copper", "Password123!");
        AuthResponse loginResponse = authService.login(loginRequest);

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getToken()).isNotBlank();
        assertThat(loginResponse.getEmail()).isEqualTo("david@test.com");

        User currentUser = authService.getCurrentUser("Bearer " + loginResponse.getToken());
        assertThat(currentUser).isNotNull();
        assertThat(currentUser.getUsername()).isEqualTo("david_copper");
    }
}
