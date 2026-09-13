package com.talktranslate.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requester_id", "addressee_id"})
})
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public Friendship() {
    }

    public Friendship(String id, User requester, User addressee, FriendshipStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.requester = requester;
        this.addressee = addressee;
        this.status = status != null ? status : FriendshipStatus.PENDING;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FriendshipBuilder builder() {
        return new FriendshipBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getAddressee() {
        return addressee;
    }

    public void setAddressee(User addressee) {
        this.addressee = addressee;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status != null ? status : FriendshipStatus.PENDING;
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

    public static class FriendshipBuilder {
        private String id;
        private User requester;
        private User addressee;
        private FriendshipStatus status = FriendshipStatus.PENDING;
        private Instant createdAt;
        private Instant updatedAt;

        FriendshipBuilder() {
        }

        public FriendshipBuilder id(String id) {
            this.id = id;
            return this;
        }

        public FriendshipBuilder requester(User requester) {
            this.requester = requester;
            return this;
        }

        public FriendshipBuilder addressee(User addressee) {
            this.addressee = addressee;
            return this;
        }

        public FriendshipBuilder status(FriendshipStatus status) {
            this.status = status;
            return this;
        }

        public FriendshipBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FriendshipBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Friendship build() {
            return new Friendship(id, requester, addressee, status, createdAt, updatedAt);
        }
    }
}
