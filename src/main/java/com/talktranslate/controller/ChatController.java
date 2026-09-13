package com.talktranslate.controller;

import com.talktranslate.model.ChatMessage;
import com.talktranslate.model.dto.UpdateLanguageRequest;
import com.talktranslate.service.ChatService;
import com.talktranslate.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService,
                          UserService userService,
                          SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles 1-on-1 friend-authorized chat messages with Principal authentication.
     * Automatically translates to recipient's language and delivers to recipient's queue.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            if (chatMessage != null) {
                chatMessage.setSenderId(principal.getName());
            }
        }
        sendMessage(chatMessage);
    }

    /**
     * Internal processor for chat message delivery.
     */
    public void sendMessage(ChatMessage chatMessage) {
        try {
            ChatMessage processed = chatService.processAndSaveMessage(chatMessage);

            logger.info("Delivering translated message to recipient [{}] in language [{}]: '{}'",
                    processed.getRecipientId(), processed.getRecipientLang(), processed.getTranslatedText());

            // Deliver to recipient's queue
            messagingTemplate.convertAndSendToUser(
                    processed.getRecipientId(),
                    "/queue/messages",
                    processed
            );

            // Echo to sender's queue for instant delivery receipt
            messagingTemplate.convertAndSendToUser(
                    processed.getSenderId(),
                    "/queue/messages",
                    processed
            );

        } catch (Exception e) {
            logger.error("Failed to process chat message: {}", e.getMessage());
            // Send error notification back to sender
            if (chatMessage != null && chatMessage.getSenderId() != null) {
                messagingTemplate.convertAndSendToUser(
                        chatMessage.getSenderId(),
                        "/queue/errors",
                        Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to process chat message")
                );
            }
        }
    }

    /**
     * Broadcasts typing indicators between chat partners with Principal context.
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> typingPayload, Principal principal) {
        handleTyping(typingPayload);
    }

    public void handleTyping(Map<String, Object> typingPayload) {
        if (typingPayload == null) {
            return;
        }
        String recipientId = (String) typingPayload.get("recipientId");
        if (recipientId != null) {
            messagingTemplate.convertAndSendToUser(recipientId, "/queue/typing", typingPayload);
        }
    }

    /**
     * Live Language Switch via WebSocket with Principal session binding.
     */
    @MessageMapping("/chat.changeLang")
    public void changeLanguage(@Payload UpdateLanguageRequest request, Principal principal) {
        if (principal != null && principal.getName() != null && !principal.getName().isBlank() && request != null) {
            request.setUserId(principal.getName());
        }
        changeLanguage(request);
    }

    public void changeLanguage(UpdateLanguageRequest request) {
        try {
            userService.updateUserLanguage(request);
            if (request != null) {
                logger.info("User {} switched preferred language live to {}", request.getUserId(), request.getLanguageCode());
            }
        } catch (Exception e) {
            logger.error("Failed to switch language via WebSocket: {}", e.getMessage());
        }
    }
}
