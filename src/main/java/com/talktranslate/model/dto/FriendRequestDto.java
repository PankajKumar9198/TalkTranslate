package com.talktranslate.model.dto;

import com.talktranslate.model.FriendshipStatus;
import java.time.Instant;

public class FriendRequestDto {
    private String requestId;
    private String requesterId;
    private String requesterUsername;
    private String requesterFullName;
    private String requesterLanguage;
    private String requesterAvatarUrl;
    private String addresseeId;
    private String addresseeUsername;
    private FriendshipStatus status;
    private Instant createdAt;

    public FriendRequestDto() {
    }

    public FriendRequestDto(String requestId, String requesterId, String requesterUsername, String requesterFullName,
                            String requesterLanguage, String requesterAvatarUrl, String addresseeId,
                            String addresseeUsername, FriendshipStatus status, Instant createdAt) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.requesterUsername = requesterUsername;
        this.requesterFullName = requesterFullName;
        this.requesterLanguage = requesterLanguage;
        this.requesterAvatarUrl = requesterAvatarUrl;
        this.addresseeId = addresseeId;
        this.addresseeUsername = addresseeUsername;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static FriendRequestDtoBuilder builder() {
        return new FriendRequestDtoBuilder();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public String getRequesterFullName() {
        return requesterFullName;
    }

    public void setRequesterFullName(String requesterFullName) {
        this.requesterFullName = requesterFullName;
    }

    public String getRequesterLanguage() {
        return requesterLanguage;
    }

    public void setRequesterLanguage(String requesterLanguage) {
        this.requesterLanguage = requesterLanguage;
    }

    public String getRequesterAvatarUrl() {
        return requesterAvatarUrl;
    }

    public void setRequesterAvatarUrl(String requesterAvatarUrl) {
        this.requesterAvatarUrl = requesterAvatarUrl;
    }

    public String getAddresseeId() {
        return addresseeId;
    }

    public void setAddresseeId(String addresseeId) {
        this.addresseeId = addresseeId;
    }

    public String getAddresseeUsername() {
        return addresseeUsername;
    }

    public void setAddresseeUsername(String addresseeUsername) {
        this.addresseeUsername = addresseeUsername;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static class FriendRequestDtoBuilder {
        private String requestId;
        private String requesterId;
        private String requesterUsername;
        private String requesterFullName;
        private String requesterLanguage;
        private String requesterAvatarUrl;
        private String addresseeId;
        private String addresseeUsername;
        private FriendshipStatus status;
        private Instant createdAt;

        FriendRequestDtoBuilder() {
        }

        public FriendRequestDtoBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public FriendRequestDtoBuilder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public FriendRequestDtoBuilder requesterUsername(String requesterUsername) {
            this.requesterUsername = requesterUsername;
            return this;
        }

        public FriendRequestDtoBuilder requesterFullName(String requesterFullName) {
            this.requesterFullName = requesterFullName;
            return this;
        }

        public FriendRequestDtoBuilder requesterLanguage(String requesterLanguage) {
            this.requesterLanguage = requesterLanguage;
            return this;
        }

        public FriendRequestDtoBuilder requesterAvatarUrl(String requesterAvatarUrl) {
            this.requesterAvatarUrl = requesterAvatarUrl;
            return this;
        }

        public FriendRequestDtoBuilder addresseeId(String addresseeId) {
            this.addresseeId = addresseeId;
            return this;
        }

        public FriendRequestDtoBuilder addresseeUsername(String addresseeUsername) {
            this.addresseeUsername = addresseeUsername;
            return this;
        }

        public FriendRequestDtoBuilder status(FriendshipStatus status) {
            this.status = status;
            return this;
        }

        public FriendRequestDtoBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FriendRequestDto build() {
            return new FriendRequestDto(requestId, requesterId, requesterUsername, requesterFullName,
                    requesterLanguage, requesterAvatarUrl, addresseeId, addresseeUsername, status, createdAt);
        }
    }
}
