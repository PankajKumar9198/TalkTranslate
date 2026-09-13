package com.talktranslate.model.dto;

import java.time.Instant;

public class FriendResponseDto {
    private String friendId;
    private String username;
    private String fullName;
    private String preferredLanguage;
    private String avatarUrl;
    private String bio;
    private boolean online;
    private String friendshipId;
    private Instant friendsSince;

    public FriendResponseDto() {
    }

    public FriendResponseDto(String friendId, String username, String fullName, String preferredLanguage,
                             String avatarUrl, String bio, boolean online, String friendshipId, Instant friendsSince) {
        this.friendId = friendId;
        this.username = username;
        this.fullName = fullName;
        this.preferredLanguage = preferredLanguage;
        this.avatarUrl = avatarUrl;
        this.bio = bio;
        this.online = online;
        this.friendshipId = friendshipId;
        this.friendsSince = friendsSince;
    }

    public static FriendResponseDtoBuilder builder() {
        return new FriendResponseDtoBuilder();
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(String friendshipId) {
        this.friendshipId = friendshipId;
    }

    public Instant getFriendsSince() {
        return friendsSince;
    }

    public void setFriendsSince(Instant friendsSince) {
        this.friendsSince = friendsSince;
    }

    public static class FriendResponseDtoBuilder {
        private String friendId;
        private String username;
        private String fullName;
        private String preferredLanguage;
        private String avatarUrl;
        private String bio;
        private boolean online;
        private String friendshipId;
        private Instant friendsSince;

        FriendResponseDtoBuilder() {
        }

        public FriendResponseDtoBuilder friendId(String friendId) {
            this.friendId = friendId;
            return this;
        }

        public FriendResponseDtoBuilder username(String username) {
            this.username = username;
            return this;
        }

        public FriendResponseDtoBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public FriendResponseDtoBuilder preferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }

        public FriendResponseDtoBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public FriendResponseDtoBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public FriendResponseDtoBuilder online(boolean online) {
            this.online = online;
            return this;
        }

        public FriendResponseDtoBuilder friendshipId(String friendshipId) {
            this.friendshipId = friendshipId;
            return this;
        }

        public FriendResponseDtoBuilder friendsSince(Instant friendsSince) {
            this.friendsSince = friendsSince;
            return this;
        }

        public FriendResponseDto build() {
            return new FriendResponseDto(friendId, username, fullName, preferredLanguage, avatarUrl, bio, online, friendshipId, friendsSince);
        }
    }
}
