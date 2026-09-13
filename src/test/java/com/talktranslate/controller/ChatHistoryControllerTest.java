package com.talktranslate.controller;

import com.talktranslate.model.ChatMessage;
import com.talktranslate.model.MessageType;
import com.talktranslate.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatHistoryController.class)
class ChatHistoryControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ChatService chatService;

        @Test
        void shouldReturnChatHistorySuccessfully() throws Exception {
                ChatMessage m1 = ChatMessage.builder()
                                .id("msg_1")
                                .type(MessageType.CHAT)
                                .senderId("usr_1")
                                .senderName("Alice")
                                .senderLang("en")
                                .recipientId("usr_2")
                                .recipientName("Bob")
                                .recipientLang("hi")
                                .originalText("Hello Bob")
                                .translatedText("नमस्ते बॉब")
                                .detectedLang("en")
                                .roomId("room_123")
                                .isRead(true)
                                .createdAt(Instant.parse("2026-09-11T00:00:00Z"))
                                .build();

                ChatMessage m2 = ChatMessage.builder()
                                .id("msg_2")
                                .type(MessageType.CHAT)
                                .senderId("usr_2")
                                .senderName("Bob")
                                .senderLang("hi")
                                .recipientId("usr_1")
                                .recipientName("Alice")
                                .recipientLang("en")
                                .originalText("नमस्ते एलिस")
                                .translatedText("Hello Alice")
                                .detectedLang("hi")
                                .roomId("room_123")
                                .isRead(true)
                                .createdAt(Instant.parse("2026-09-11T00:01:00Z"))
                                .build();

                when(chatService.getChatHistory("usr_1", "usr_2")).thenReturn(List.of(m1, m2));

                mockMvc.perform(get("/api/chat/history/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].id").value("msg_1"))
                                .andExpect(jsonPath("$[0].senderId").value("usr_1"))
                                .andExpect(jsonPath("$[0].senderName").value("Alice"))
                                .andExpect(jsonPath("$[0].recipientId").value("usr_2"))
                                .andExpect(jsonPath("$[0].recipientName").value("Bob"))
                                .andExpect(jsonPath("$[0].originalText").value("Hello Bob"))
                                .andExpect(jsonPath("$[0].translatedText").value("नमस्ते बॉब"))
                                .andExpect(jsonPath("$[0].senderLang").value("en"))
                                .andExpect(jsonPath("$[0].recipientLang").value("hi"))
                                .andExpect(jsonPath("$[0].detectedLang").value("en"))
                                .andExpect(jsonPath("$[0].type").value("CHAT"))
                                .andExpect(jsonPath("$[0].read").value(true))
                                .andExpect(jsonPath("$[1].id").value("msg_2"))
                                .andExpect(jsonPath("$[1].senderId").value("usr_2"))
                                .andExpect(jsonPath("$[1].originalText").value("नमस्ते एलिस"))
                                .andExpect(jsonPath("$[1].translatedText").value("Hello Alice"));

                verify(chatService, times(1)).getChatHistory("usr_1", "usr_2");
        }

        @Test
        void shouldReturnEmptyListWhenNoChatHistoryExists() throws Exception {
                when(chatService.getChatHistory("usr_1", "usr_2")).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/api/chat/history/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(0));

                verify(chatService, times(1)).getChatHistory("usr_1", "usr_2");
        }

        @Test
        void shouldReturnBadRequestWhenChatServiceThrowsIllegalArgumentException() throws Exception {
                when(chatService.getChatHistory("usr_1", "usr_2"))
                                .thenThrow(new IllegalArgumentException("User IDs cannot be null"));

                mockMvc.perform(get("/api/chat/history/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message").value("User IDs cannot be null"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(chatService, times(1)).getChatHistory("usr_1", "usr_2");
        }

        @Test
        void shouldReturnBadRequestWithDefaultMessageWhenIllegalArgumentExceptionMessageIsNull() throws Exception {
                when(chatService.getChatHistory("usr_1", "usr_2"))
                                .thenThrow(new IllegalArgumentException((String) null));

                mockMvc.perform(get("/api/chat/history/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error").value("Bad Request"))
                                .andExpect(jsonPath("$.message").value("Error"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(chatService, times(1)).getChatHistory("usr_1", "usr_2");
        }

        @Test
        void shouldRejectRequestWhenUserIdParamIsMissing() throws Exception {
                mockMvc.perform(get("/api/chat/history/usr_2"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.message", org.hamcrest.Matchers
                                                .containsString("Required request parameter 'userId'")));

                verify(chatService, never()).getChatHistory(anyString(), anyString());
        }

        @Test
        void shouldReturnConflictWhenNotMutualFriends() throws Exception {
                when(chatService.getChatHistory("usr_1", "usr_2"))
                                .thenThrow(new IllegalStateException("You are not mutual friends"));

                mockMvc.perform(get("/api/chat/history/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.error").value("Conflict"))
                                .andExpect(jsonPath("$.message").value("You are not mutual friends"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(chatService, times(1)).getChatHistory("usr_1", "usr_2");
        }

        @Test
        void shouldReturnConflictWithDefaultMessageWhenIllegalStateExceptionMessageIsNull() throws Exception {
                when(chatService.getChatHistory("usr_1", "usr_2"))
                                .thenThrow(new IllegalStateException((String) null));

                mockMvc.perform(get("/api/chat/history/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.error").value("Conflict"))
                                .andExpect(jsonPath("$.message").value("Error"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(chatService, times(1)).getChatHistory("usr_1", "usr_2");
        }

        @Test
        void shouldReturnInternalServerErrorWhenChatServiceFailsUnexpectedly() throws Exception {
                when(chatService.getChatHistory("usr_1", "usr_2"))
                                .thenThrow(new RuntimeException("Database connection timeout"));

                mockMvc.perform(get("/api/chat/history/usr_2")
                                .param("userId", "usr_1"))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath("$.status").value(500))
                                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                                .andExpect(jsonPath("$.message").value("An unexpected server error occurred: Database connection timeout"))
                                .andExpect(jsonPath("$.timestamp").exists());

                verify(chatService, times(1)).getChatHistory("usr_1", "usr_2");
        }
}
