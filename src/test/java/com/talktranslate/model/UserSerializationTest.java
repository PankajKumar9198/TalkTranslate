package com.talktranslate.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talktranslate.model.dto.UserDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldNeverSerializePasswordHashToJson() throws Exception {
        User user = User.builder()
                .id("usr_123")
                .username("rahul_sharma")
                .email("rahul@test.com")
                .passwordHash("$2a$10$e8wW0tV7f.superSecretHashBcryptString")
                .fullName("Rahul Sharma")
                .preferredLanguage("hi")
                .avatarUrl("https://example.com/avatar.png")
                .bio("Software Engineer")
                .online(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        String json = objectMapper.writeValueAsString(user);

        assertThat(json)
                .doesNotContain("passwordHash")
                .doesNotContain("superSecretHashBcryptString")
                .contains("\"id\":\"usr_123\"")
                .contains("\"username\":\"rahul_sharma\"")
                .contains("\"email\":\"rahul@test.com\"")
                .contains("\"fullName\":\"Rahul Sharma\"")
                .contains("\"preferredLanguage\":\"hi\"");
    }

    @Test
    void shouldMapUserToUserDtoProperly() {
        User user = User.builder()
                .id("usr_456")
                .username("sarah_jenkins")
                .email("sarah@test.com")
                .passwordHash("secret_password_hash")
                .fullName("Sarah Jenkins")
                .preferredLanguage("en")
                .avatarUrl("https://example.com/sarah.png")
                .bio("Product Designer")
                .online(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        UserDto dto = UserDto.fromUser(user);

        assertAll(
                () -> assertThat(dto).isNotNull(),
                () -> assertThat(dto.getId()).isEqualTo("usr_456"),
                () -> assertThat(dto.getUsername()).isEqualTo("sarah_jenkins"),
                () -> assertThat(dto.getEmail()).isEqualTo("sarah@test.com"),
                () -> assertThat(dto.getFullName()).isEqualTo("Sarah Jenkins"),
                () -> assertThat(dto.getPreferredLanguage()).isEqualTo("en"),
                () -> assertThat(dto.getAvatarUrl()).isEqualTo("https://example.com/sarah.png"),
                () -> assertThat(dto.getBio()).isEqualTo("Product Designer"),
                () -> assertThat(dto.isOnline()).isFalse()
        );
    }

    @Test
    void shouldReturnNullWhenMappingNullUserToDto() {
        assertThat(UserDto.fromUser(null)).isNull();
    }

    @Test
    void shouldInstantiateUserDtoWithBuilderAndGettersSetters() {
        UserDto dto = UserDto.builder()
                .id("usr_789")
                .username("carlos_garcia")
                .email("carlos@test.com")
                .fullName("Carlos Garcia")
                .preferredLanguage("es")
                .avatarUrl("https://example.com/carlos.png")
                .bio("Mobile Dev")
                .online(true)
                .build();

        dto.setUsername("carlos_updated");
        dto.setPreferredLanguage("fr");

        assertAll(
                () -> assertThat(dto.getId()).isEqualTo("usr_789"),
                () -> assertThat(dto.getUsername()).isEqualTo("carlos_updated"),
                () -> assertThat(dto.getPreferredLanguage()).isEqualTo("fr")
        );
    }
}
