package com.talktranslate.service;

import com.talktranslate.model.User;
import com.talktranslate.model.dto.UpdateLanguageRequest;
import com.talktranslate.repository.FriendshipRepository;
import com.talktranslate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @InjectMocks
    private UserService userService;

    private User userRahul;
    private User userSarah;
    private User userCarlos;
    private User userPriya;

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

        userCarlos = User.builder()
                .id("usr_carlos")
                .username("carlos_garcia")
                .email("carlos@test.com")
                .fullName("Carlos García")
                .preferredLanguage("es")
                .avatarUrl("https://img.example.com/carlos.jpg")
                .bio("Mobile Developer from Madrid")
                .online(true)
                .build();

        userPriya = User.builder()
                .id("usr_priya")
                .username("priya_singh")
                .email("priya@test.com")
                .fullName("Priya Singh")
                .preferredLanguage("hinglish")
                .avatarUrl("https://img.example.com/priya.jpg")
                .bio("Data Scientist from Mumbai")
                .online(true)
                .build();
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThatThrownBy(() -> userService.getUserById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID cannot be null");

        verifyNoInteractions(userRepository, friendshipRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {
        when(userRepository.findById("usr_non_existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById("usr_non_existent"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("User not found with ID: usr_non_existent");

        verify(userRepository, times(1)).findById("usr_non_existent");
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));

        User found = userService.getUserById("usr_rahul");

        assertAll("Validate User Details by ID",
                () -> assertThat(found).isNotNull(),
                () -> assertThat(found.getId()).isEqualTo("usr_rahul"),
                () -> assertThat(found.getUsername()).isEqualTo("rahul_sharma"),
                () -> assertThat(found.getEmail()).isEqualTo("rahul@test.com"),
                () -> assertThat(found.getFullName()).isEqualTo("Rahul Sharma"),
                () -> assertThat(found.getPreferredLanguage()).isEqualTo("hi"),
                () -> assertThat(found.getAvatarUrl()).isEqualTo("https://img.example.com/rahul.jpg"),
                () -> assertThat(found.getBio()).isEqualTo("Software Engineer from Delhi"),
                () -> assertThat(found.isOnline()).isTrue());

        verify(userRepository, times(1)).findById("usr_rahul");
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void shouldThrowExceptionWhenUsernameIsNull() {
        assertThatThrownBy(() -> userService.getUserByUsername(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be null");

        verifyNoInteractions(userRepository, friendshipRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        when(userRepository.findByUsername("unknown_user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("unknown_user"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("User not found with username: unknown_user");

        verify(userRepository, times(1)).findByUsername("unknown_user");
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void shouldGetUserByUsernameSuccessfully() {
        when(userRepository.findByUsername("rahul_sharma")).thenReturn(Optional.of(userRahul));

        User found = userService.getUserByUsername("rahul_sharma");

        assertAll("Validate User Details by Username",
                () -> assertThat(found).isNotNull(),
                () -> assertThat(found.getId()).isEqualTo("usr_rahul"),
                () -> assertThat(found.getUsername()).isEqualTo("rahul_sharma"),
                () -> assertThat(found.getEmail()).isEqualTo("rahul@test.com"),
                () -> assertThat(found.getFullName()).isEqualTo("Rahul Sharma"),
                () -> assertThat(found.getPreferredLanguage()).isEqualTo("hi"),
                () -> assertThat(found.getAvatarUrl()).isEqualTo("https://img.example.com/rahul.jpg"),
                () -> assertThat(found.getBio()).isEqualTo("Software Engineer from Delhi"),
                () -> assertThat(found.isOnline()).isTrue());

        verify(userRepository, times(1)).findByUsername("rahul_sharma");
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void shouldThrowExceptionWhenUpdateLanguageRequestIsNull() {
        assertThatThrownBy(() -> userService.updateUserLanguage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID and Language Code are required");

        verifyNoInteractions(userRepository, friendshipRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserIdInRequestIsNull() {
        UpdateLanguageRequest request = new UpdateLanguageRequest(null, "es");

        assertThatThrownBy(() -> userService.updateUserLanguage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID and Language Code are required");

        verifyNoInteractions(userRepository, friendshipRepository);
    }

    @Test
    void shouldThrowExceptionWhenLanguageCodeInRequestIsNull() {
        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_rahul", null);

        assertThatThrownBy(() -> userService.updateUserLanguage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID and Language Code are required");

        verifyNoInteractions(userRepository, friendshipRepository);
    }

    @Test
    void shouldThrowExceptionWhenBothUserIdAndLanguageCodeInRequestAreNull() {
        UpdateLanguageRequest request = new UpdateLanguageRequest(null, null);

        assertThatThrownBy(() -> userService.updateUserLanguage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID and Language Code are required");

        verifyNoInteractions(userRepository, friendshipRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringLanguageUpdate() {
        when(userRepository.findById("usr_non_existent")).thenReturn(Optional.empty());

        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_non_existent", "es");

        assertThatThrownBy(() -> userService.updateUserLanguage(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("User not found with ID: usr_non_existent");

        verify(userRepository, times(1)).findById("usr_non_existent");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void shouldUpdateUserLanguageSuccessfully() {
        when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_rahul", "es");
        User updated = userService.updateUserLanguage(request);

        assertThat(updated).isNotNull();
        assertThat(updated.getPreferredLanguage()).isEqualTo("es");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getPreferredLanguage()).isEqualTo("es");
        assertThat(captor.getValue().getId()).isEqualTo("usr_rahul");

        verify(userRepository, times(1)).findById("usr_rahul");
        verifyNoInteractions(friendshipRepository);
    }

    @ParameterizedTest(name = "Normalize raw language [{0}] to [{1}]")
    @CsvSource({
            "'  ES  ', es",
            "'HI', hi",
            "'  Hinglish  ', hinglish",
            "'FR', fr",
            "'  ja  ', ja"
    })
    void shouldTrimAndNormalizeLanguageCodeToLowerCase(String rawInput, String expectedLanguageCode) {
        when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_rahul", rawInput);
        User updated = userService.updateUserLanguage(request);

        assertThat(updated.getPreferredLanguage()).isEqualTo(expectedLanguageCode);
        verify(userRepository, times(1)).save(userRahul);
    }

    @Test
    void shouldReturnAllUsersWhenCurrentUserIdIsNull() {
        when(userRepository.findAll()).thenReturn(List.of(userRahul, userSarah, userCarlos, userPriya));

        List<User> result = userService.getSuggestedUsers(null);

        assertThat(result).hasSize(4).containsExactly(userRahul, userSarah, userCarlos, userPriya);
        verify(userRepository, times(1)).findAll();
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void shouldReturnSuggestedUsersExcludingSelfAndConnectedUsers() {
        when(friendshipRepository.findAllConnectedUserIds("usr_rahul"))
                .thenReturn(new ArrayList<>(List.of("usr_sarah")));
        when(userRepository.findAll()).thenReturn(List.of(userRahul, userSarah, userCarlos, userPriya));

        List<User> suggested = userService.getSuggestedUsers("usr_rahul");

        assertThat(suggested).hasSize(2).containsExactly(userCarlos, userPriya);
        verify(friendshipRepository, times(1)).findAllConnectedUserIds("usr_rahul");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnAllOtherUsersWhenUserHasNoConnections() {
        when(friendshipRepository.findAllConnectedUserIds("usr_rahul"))
                .thenReturn(new ArrayList<>());
        when(userRepository.findAll()).thenReturn(List.of(userRahul, userSarah, userCarlos));

        List<User> suggested = userService.getSuggestedUsers("usr_rahul");

        assertThat(suggested).hasSize(2).containsExactly(userSarah, userCarlos);
        verify(friendshipRepository, times(1)).findAllConnectedUserIds("usr_rahul");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenUserIsConnectedToAllUsers() {
        when(friendshipRepository.findAllConnectedUserIds("usr_rahul"))
                .thenReturn(new ArrayList<>(List.of("usr_sarah", "usr_carlos")));
        when(userRepository.findAll()).thenReturn(List.of(userRahul, userSarah, userCarlos));

        List<User> suggested = userService.getSuggestedUsers("usr_rahul");

        assertThat(suggested).isNotNull().isEmpty();
        verify(friendshipRepository, times(1)).findAllConnectedUserIds("usr_rahul");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenRepositoryIsEmptyInSuggestedUsers() {
        when(friendshipRepository.findAllConnectedUserIds("usr_rahul"))
                .thenReturn(new ArrayList<>());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> suggested = userService.getSuggestedUsers("usr_rahul");

        assertThat(suggested).isNotNull().isEmpty();
        verify(friendshipRepository, times(1)).findAllConnectedUserIds("usr_rahul");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenRepositoryHasOnlyCurrentUserInSuggestedUsers() {
        when(friendshipRepository.findAllConnectedUserIds("usr_rahul"))
                .thenReturn(new ArrayList<>());
        when(userRepository.findAll()).thenReturn(List.of(userRahul));

        List<User> suggested = userService.getSuggestedUsers("usr_rahul");

        assertThat(suggested).isNotNull().isEmpty();
        verify(friendshipRepository, times(1)).findAllConnectedUserIds("usr_rahul");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnAllUsersSuccessfully() {
        when(userRepository.findAll()).thenReturn(List.of(userRahul, userSarah, userCarlos));

        List<User> all = userService.getAllUsers();

        assertThat(all).hasSize(3).containsExactly(userRahul, userSarah, userCarlos);
        verify(userRepository, times(1)).findAll();
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExistInGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> all = userService.getAllUsers();

        assertThat(all).isNotNull().isEmpty();
        verify(userRepository, times(1)).findAll();
        verifyNoInteractions(friendshipRepository);
    }
}
