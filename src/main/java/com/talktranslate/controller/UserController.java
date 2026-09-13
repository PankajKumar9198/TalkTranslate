package com.talktranslate.controller;

import com.talktranslate.model.User;
import com.talktranslate.model.dto.UpdateLanguageRequest;
import com.talktranslate.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User management and profile REST Controller.
 * Exceptions are handled globally by {@link com.talktranslate.exception.GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get suggested users to connect with (Instagram/FB style)
     * GET /api/users/suggested?userId={id}
     */
    @GetMapping("/suggested")
    public ResponseEntity<List<User>> getSuggestedUsers(@RequestParam(value = "userId", required = false) String userId) {
        return ResponseEntity.ok(userService.getSuggestedUsers(userId));
    }

    /**
     * Get user profile by ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable("userId") String userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Update preferred language dynamically / live during conversation
     * PUT /api/users/language
     */
    @PutMapping("/language")
    public ResponseEntity<User> updateUserLanguage(@RequestBody UpdateLanguageRequest request) {
        User updated = userService.updateUserLanguage(request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get all users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
