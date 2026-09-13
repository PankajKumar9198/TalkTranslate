package com.talktranslate.model.dto;

import com.talktranslate.model.User;

import java.time.Instant;

/**
 * Data Transfer Object for User profile information without sensitive security fields (e.g. passwordHash).
 */
public class UserDto {

    private String id;
    private String username;
    private String email;
    private String fullName;
    private String preferredLanguage;
    private String avatarUrl;
    private String bio;
    private boolean online;
    private Instant createdAt;
    private Instant updatedAt;

    public UserDto() {
    }

    public UserDto(String id, String username, String email, String fullName, String preferredLanguage,
                   String avatarUrl, String bio, boolean online, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.preferredLanguage = preferredLanguage;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.online = online;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserDto fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPreferredLanguage(),
                user.getAvatarUrl(),
                user.getBio(),
                user.isOnline(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static UserDtoBuilder builder() {
        return new UserDtoBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class UserDtoBuilder {
        private String id;
        private String username;
        private String email;
        private String fullName;
        private String preferredLanguage;
        private String avatarUrl;
        private String bio;
        private boolean online;
        private Instant createdAt;
        private Instant updatedAt;

        public UserDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UserDtoBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserDtoBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserDtoBuilder preferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }

        public UserDtoBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public UserDtoBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public UserDtoBuilder online(boolean online) {
            this.online = online;
            return this;
        }

        public UserDtoBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserDtoBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserDto build() {
            return new UserDto(id, username, email, fullName, preferredLanguage, avatarUrl, bio, online, createdAt, updatedAt);
        }
    }
}
