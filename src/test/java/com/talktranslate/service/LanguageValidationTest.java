package com.talktranslate.service;

import com.talktranslate.model.User;
import com.talktranslate.model.dto.UpdateLanguageRequest;
import com.talktranslate.repository.FriendshipRepository;
import com.talktranslate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageValidationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    private TranslationService translationService;
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        translationService = new TranslationService();
        userService = new UserService(userRepository, friendshipRepository, translationService);

        testUser = User.builder()
                .id("usr_test")
                .username("test_user")
                .email("test@example.com")
                .preferredLanguage("en")
                .build();
    }

    @Test
    void shouldVerifyTranslationServiceIsLanguageSupported() {
        assertThat(translationService.isLanguageSupported("en")).isTrue();
        assertThat(translationService.isLanguageSupported("hi")).isTrue();
        assertThat(translationService.isLanguageSupported("hinglish")).isTrue();
        assertThat(translationService.isLanguageSupported("es")).isTrue();
        assertThat(translationService.isLanguageSupported("FR")).isTrue();
        assertThat(translationService.isLanguageSupported("ja")).isTrue();

        assertThat(translationService.isLanguageSupported("invalid_code")).isFalse();
        assertThat(translationService.isLanguageSupported("xyz")).isFalse();
        assertThat(translationService.isLanguageSupported("")).isFalse();
        assertThat(translationService.isLanguageSupported(null)).isFalse();
    }

    @Test
    void shouldAcceptValidLanguageOnUpdate() {
        when(userRepository.findById("usr_test")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_test", "es");
        User updated = userService.updateUserLanguage(request);

        assertThat(updated.getPreferredLanguage()).isEqualTo("es");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void shouldRejectUnsupportedLanguageOnUpdate() {
        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_test", "klingon");

        assertThatThrownBy(() -> userService.updateUserLanguage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported language code: klingon");

        verify(userRepository, never()).save(any(User.class));
    }
}
