package com.talktranslate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 10)
    private String preferredLanguage = "en"; // Dynamic: defaults to "en", switched live

    @Column(length = 255)
    private String avatarUrl;

    @Column(length = 500)
    private String bio;

    private boolean online = true;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public User() {
    }

    public User(String id, String username, String email, String passwordHash, String fullName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.preferredLanguage = "en";
        this.online = true;
    }

    public User(String id, String username, String email, String passwordHash, String fullName,
                String preferredLanguage, String avatarUrl, String bio, boolean online,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.preferredLanguage = preferredLanguage != null ? preferredLanguage : "en";
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.online = online;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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
        this.preferredLanguage = preferredLanguage != null ? preferredLanguage : "en";
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

    public static class UserBuilder {
        private String id;
        private String username;
        private String email;
        private String passwordHash;
        private String fullName;
        private String preferredLanguage = "en";
        private String avatarUrl;
        private String bio;
        private boolean online = true;
        private Instant createdAt;
        private Instant updatedAt;

        UserBuilder() {
        }

        public UserBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public UserBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserBuilder preferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }

        public UserBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public UserBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public UserBuilder online(boolean online) {
            this.online = online;
            return this;
        }

        public UserBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public User build() {
            return new User(id, username, email, passwordHash, fullName, preferredLanguage, avatarUrl, bio, online, createdAt, updatedAt);
        }
    }
}
