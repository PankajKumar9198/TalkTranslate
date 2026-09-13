package com.talktranslate.controller;

import com.talktranslate.model.ChatMessage;
import com.talktranslate.model.MessageType;
import com.talktranslate.model.dto.UpdateLanguageRequest;
import com.talktranslate.service.ChatService;
import com.talktranslate.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private UserService userService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatController chatController;

    @Test
    void shouldProcessAndDeliverMessageToBothRecipientAndSenderQueues() {
        ChatMessage input = ChatMessage.builder()
                .senderId("usr_1")
                .recipientId("usr_2")
                .originalText("Hello")
                .type(MessageType.CHAT)
                .build();

        ChatMessage processed = ChatMessage.builder()
                .senderId("usr_1")
                .recipientId("usr_2")
                .originalText("Hello")
                .translatedText("नमस्ते")
                .senderLang("en")
                .recipientLang("hi")
                .type(MessageType.CHAT)
                .build();

        when(chatService.processAndSaveMessage(input)).thenReturn(processed);

        chatController.sendMessage(input);

        verify(messagingTemplate).convertAndSendToUser("usr_2", "/queue/messages", processed);
        verify(messagingTemplate).convertAndSendToUser("usr_1", "/queue/messages", processed);
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void shouldSendErrorToSenderWhenChatProcessingThrowsExceptionWithDetail() {
        ChatMessage input = ChatMessage.builder()
                .senderId("usr_1")
                .recipientId("usr_2")
                .originalText("Hello")
                .build();

        when(chatService.processAndSaveMessage(input)).thenThrow(new IllegalStateException("Not friends"));

        chatController.sendMessage(input);

        verify(messagingTemplate).convertAndSendToUser(eq("usr_1"), eq("/queue/errors"),
                eq(Map.of("error", "Not friends")));
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void shouldSendDefaultErrorMessageToSenderWhenExceptionMessageIsNull() {
        ChatMessage input = ChatMessage.builder()
                .senderId("usr_1")
                .recipientId("usr_2")
                .originalText("Hello")
                .build();

        when(chatService.processAndSaveMessage(input)).thenThrow(new RuntimeException((String) null));

        chatController.sendMessage(input);

        verify(messagingTemplate).convertAndSendToUser(eq("usr_1"), eq("/queue/errors"),
                eq(Map.of("error", "Failed to process chat message")));
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void shouldNotSendErrorWhenChatMessageIsNull() {
        when(chatService.processAndSaveMessage(null))
                .thenThrow(new IllegalArgumentException("Message text cannot be empty"));

        assertDoesNotThrow(() -> chatController.sendMessage(null));

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void shouldNotSendErrorWhenSenderIdIsNullOnException() {
        ChatMessage input = ChatMessage.builder()
                .recipientId("usr_2")
                .originalText("Hello")
                .build();

        when(chatService.processAndSaveMessage(input))
                .thenThrow(new IllegalArgumentException("Sender ID and Recipient ID are required"));

        assertDoesNotThrow(() -> chatController.sendMessage(input));

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void shouldHandleMessagingTemplateExceptionGracefully() {
        ChatMessage input = ChatMessage.builder()
                .senderId("usr_1")
                .recipientId("usr_2")
                .originalText("Hello")
                .build();

        ChatMessage processed = ChatMessage.builder()
                .senderId("usr_1")
                .recipientId("usr_2")
                .originalText("Hello")
                .build();

        when(chatService.processAndSaveMessage(input)).thenReturn(processed);
        doThrow(new RuntimeException("Socket closed")).when(messagingTemplate)
                .convertAndSendToUser("usr_2", "/queue/messages", processed);

        assertDoesNotThrow(() -> chatController.sendMessage(input));

        verify(messagingTemplate).convertAndSendToUser(eq("usr_1"), eq("/queue/errors"),
                eq(Map.of("error", "Socket closed")));
    }

    @Test
    void shouldRouteTypingIndicatorToRecipientWhenTypingIsTrue() {
        Map<String, Object> payload = Map.of("senderId", "usr_1", "recipientId", "usr_2", "typing", true);

        chatController.handleTyping(payload);

        verify(messagingTemplate).convertAndSendToUser("usr_2", "/queue/typing", payload);
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void shouldRouteTypingIndicatorToRecipientWhenTypingIsFalse() {
        Map<String, Object> payload = Map.of("senderId", "usr_1", "recipientId", "usr_2", "typing", false);

        chatController.handleTyping(payload);

        verify(messagingTemplate).convertAndSendToUser("usr_2", "/queue/typing", payload);
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    void shouldIgnoreTypingWhenRecipientIdIsNull() {
        Map<String, Object> payload = Map.of("senderId", "usr_1", "typing", true);

        chatController.handleTyping(payload);

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void shouldIgnoreTypingWhenPayloadIsEmpty() {
        chatController.handleTyping(Collections.emptyMap());

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void shouldUpdateUserLanguageSuccessfully() {
        UpdateLanguageRequest req = new UpdateLanguageRequest("usr_1", "es");

        chatController.changeLanguage(req);

        verify(userService).updateUserLanguage(req);
    }

    @Test
    void shouldCatchAndLogExceptionWhenUpdateLanguageFails() {
        UpdateLanguageRequest req = new UpdateLanguageRequest("usr_invalid", "es");
        doThrow(new NoSuchElementException("User not found")).when(userService).updateUserLanguage(req);

        assertDoesNotThrow(() -> chatController.changeLanguage(req));

        verify(userService).updateUserLanguage(req);
    }

    @Test
    void shouldHandleNullRequestInChangeLanguageGracefully() {
        doThrow(new IllegalArgumentException("User ID and Language Code are required"))
                .when(userService).updateUserLanguage(null);

        assertDoesNotThrow(() -> chatController.changeLanguage(null));

        verify(userService).updateUserLanguage(null);
    }
}
