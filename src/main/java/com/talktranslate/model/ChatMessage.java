package com.talktranslate.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MessageType type = MessageType.CHAT;

    @Column(nullable = false)
    private String senderId;

    private String senderName;

    @Column(length = 10)
    private String senderLang;

    private String recipientId;

    private String recipientName;

    @Column(length = 10)
    private String recipientLang;

    @Column(columnDefinition = "TEXT")
    private String originalText;

    @Column(columnDefinition = "TEXT")
    private String translatedText;

    @Column(length = 10)
    private String detectedLang;

    private String roomId;

    private boolean isRead = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    public ChatMessage() {
    }

    public ChatMessage(MessageType type, String senderName, String originalText) {
        this.type = type != null ? type : MessageType.CHAT;
        this.senderName = senderName;
        this.originalText = originalText;
    }

    public ChatMessage(String id, MessageType type, String senderId, String senderName, String senderLang,
                       String recipientId, String recipientName, String recipientLang, String originalText,
                       String translatedText, String detectedLang, String roomId, boolean isRead, Instant createdAt) {
        this.id = id;
        this.type = type != null ? type : MessageType.CHAT;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderLang = senderLang;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.recipientLang = recipientLang;
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.detectedLang = detectedLang;
        this.roomId = roomId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public static ChatMessageBuilder builder() {
        return new ChatMessageBuilder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type != null ? type : MessageType.CHAT;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderLang() {
        return senderLang;
    }

    public void setSenderLang(String senderLang) {
        this.senderLang = senderLang;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientLang() {
        return recipientLang;
    }

    public void setRecipientLang(String recipientLang) {
        this.recipientLang = recipientLang;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getDetectedLang() {
        return detectedLang;
    }

    public void setDetectedLang(String detectedLang) {
        this.detectedLang = detectedLang;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static class ChatMessageBuilder {
        private String id;
        private MessageType type = MessageType.CHAT;
        private String senderId;
        private String senderName;
        private String senderLang;
        private String recipientId;
        private String recipientName;
        private String recipientLang;
        private String originalText;
        private String translatedText;
        private String detectedLang;
        private String roomId;
        private boolean isRead = false;
        private Instant createdAt;

        ChatMessageBuilder() {
        }

        public ChatMessageBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ChatMessageBuilder type(MessageType type) {
            this.type = type;
            return this;
        }

        public ChatMessageBuilder senderId(String senderId) {
            this.senderId = senderId;
            return this;
        }

        public ChatMessageBuilder senderName(String senderName) {
            this.senderName = senderName;
            return this;
        }

        public ChatMessageBuilder senderLang(String senderLang) {
            this.senderLang = senderLang;
            return this;
        }

        public ChatMessageBuilder recipientId(String recipientId) {
            this.recipientId = recipientId;
            return this;
        }

        public ChatMessageBuilder recipientName(String recipientName) {
            this.recipientName = recipientName;
            return this;
        }

        public ChatMessageBuilder recipientLang(String recipientLang) {
            this.recipientLang = recipientLang;
            return this;
        }

        public ChatMessageBuilder originalText(String originalText) {
            this.originalText = originalText;
            return this;
        }

        public ChatMessageBuilder translatedText(String translatedText) {
            this.translatedText = translatedText;
            return this;
        }

        public ChatMessageBuilder detectedLang(String detectedLang) {
            this.detectedLang = detectedLang;
            return this;
        }

        public ChatMessageBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public ChatMessageBuilder isRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public ChatMessageBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ChatMessage build() {
            return new ChatMessage(id, type, senderId, senderName, senderLang, recipientId, recipientName, recipientLang,
                    originalText, translatedText, detectedLang, roomId, isRead, createdAt);
        }
    }
}
