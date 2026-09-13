package com.talktranslate.controller;

import com.talktranslate.model.ChatMessage;
import com.talktranslate.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Chat history REST Controller.
 * Provides endpoints for retrieving mutual friend chat history with optional pagination support.
 * Exceptions are handled globally by {@link com.talktranslate.exception.GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    private final ChatService chatService;

    public ChatHistoryController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Load translated chat history between two mutual friends
     * GET /api/chat/history/{friendId}?userId={currentUserId}&page={page}&size={size}
     */
    @GetMapping("/history/{friendId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable("friendId") String friendId,
            @RequestParam("userId") String currentUserId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        List<ChatMessage> history;
        if (page != null && size != null) {
            history = chatService.getChatHistory(currentUserId, friendId, page, size);
        } else {
            history = chatService.getChatHistory(currentUserId, friendId);
        }
        return ResponseEntity.ok(history);
    }
}
