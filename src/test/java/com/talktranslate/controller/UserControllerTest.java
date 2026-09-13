package com.talktranslate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talktranslate.model.User;
import com.talktranslate.model.dto.UpdateLanguageRequest;
import com.talktranslate.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnSuggestedUsersWhenUserIdProvided() throws Exception {
        User u1 = User.builder()
                .id("usr_2")
                .username("sarah")
                .email("sarah@example.com")
                .fullName("Sarah Connor")
                .preferredLanguage("en")
                .online(true)
                .build();

        when(userService.getSuggestedUsers("usr_1")).thenReturn(List.of(u1));

        mockMvc.perform(get("/api/users/suggested")
                .param("userId", "usr_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("usr_2"))
                .andExpect(jsonPath("$[0].username").value("sarah"))
                .andExpect(jsonPath("$[0].email").value("sarah@example.com"))
                .andExpect(jsonPath("$[0].fullName").value("Sarah Connor"))
                .andExpect(jsonPath("$[0].preferredLanguage").value("en"))
                .andExpect(jsonPath("$[0].online").value(true));

        verify(userService, times(1)).getSuggestedUsers("usr_1");
    }

    @Test
    void shouldReturnSuggestedUsersWhenUserIdParamIsOmitted() throws Exception {
        User u1 = User.builder()
                .id("usr_2")
                .username("sarah")
                .build();

        when(userService.getSuggestedUsers(null)).thenReturn(List.of(u1));

        mockMvc.perform(get("/api/users/suggested"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("usr_2"))
                .andExpect(jsonPath("$[0].username").value("sarah"));

        verify(userService, times(1)).getSuggestedUsers(null);
    }

    @Test
    void shouldReturnEmptyListWhenNoSuggestedUsersFound() throws Exception {
        when(userService.getSuggestedUsers("usr_1")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users/suggested")
                .param("userId", "usr_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).getSuggestedUsers("usr_1");
    }

    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {
        User user = User.builder()
                .id("usr_1")
                .username("rahul")
                .email("rahul@example.com")
                .fullName("Rahul Sharma")
                .preferredLanguage("hi")
                .online(true)
                .build();

        when(userService.getUserById("usr_1")).thenReturn(user);

        mockMvc.perform(get("/api/users/usr_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr_1"))
                .andExpect(jsonPath("$.username").value("rahul"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"))
                .andExpect(jsonPath("$.fullName").value("Rahul Sharma"))
                .andExpect(jsonPath("$.preferredLanguage").value("hi"))
                .andExpect(jsonPath("$.online").value(true));

        verify(userService, times(1)).getUserById("usr_1");
    }

    @Test
    void shouldReturnNotFoundWhenUserNotFoundById() throws Exception {
        when(userService.getUserById("usr_999"))
                .thenThrow(new NoSuchElementException("User not found with ID: usr_999"));

        mockMvc.perform(get("/api/users/usr_999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with ID: usr_999"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService, times(1)).getUserById("usr_999");
    }

    @Test
    void shouldReturnNotFoundWithDefaultMessageWhenNoSuchElementExceptionMessageIsNull() throws Exception {
        when(userService.getUserById("usr_999"))
                .thenThrow(new NoSuchElementException((String) null));

        mockMvc.perform(get("/api/users/usr_999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService, times(1)).getUserById("usr_999");
    }

    @Test
    void shouldUpdateUserLanguageSuccessfully() throws Exception {
        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_1", "hi");
        User updatedUser = User.builder()
                .id("usr_1")
                .username("rahul")
                .email("rahul@example.com")
                .fullName("Rahul Sharma")
                .preferredLanguage("hi")
                .online(true)
                .build();

        when(userService.updateUserLanguage(any(UpdateLanguageRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/language")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr_1"))
                .andExpect(jsonPath("$.username").value("rahul"))
                .andExpect(jsonPath("$.email").value("rahul@example.com"))
                .andExpect(jsonPath("$.fullName").value("Rahul Sharma"))
                .andExpect(jsonPath("$.preferredLanguage").value("hi"));

        verify(userService, times(1)).updateUserLanguage(any(UpdateLanguageRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateLanguageThrowsIllegalArgumentException() throws Exception {
        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_1", "");

        when(userService.updateUserLanguage(any(UpdateLanguageRequest.class)))
                .thenThrow(new IllegalArgumentException("User ID and Language Code are required"));

        mockMvc.perform(put("/api/users/language")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("User ID and Language Code are required"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService, times(1)).updateUserLanguage(any(UpdateLanguageRequest.class));
    }

    @Test
    void shouldReturnBadRequestWithDefaultMessageWhenIllegalArgumentExceptionMessageIsNull() throws Exception {
        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_1", "invalid");

        when(userService.updateUserLanguage(any(UpdateLanguageRequest.class)))
                .thenThrow(new IllegalArgumentException((String) null));

        mockMvc.perform(put("/api/users/language")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService, times(1)).updateUserLanguage(any(UpdateLanguageRequest.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdateLanguageThrowsNoSuchElementException() throws Exception {
        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_999", "en");

        when(userService.updateUserLanguage(any(UpdateLanguageRequest.class)))
                .thenThrow(new NoSuchElementException("User not found with ID: usr_999"));

        mockMvc.perform(put("/api/users/language")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with ID: usr_999"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService, times(1)).updateUserLanguage(any(UpdateLanguageRequest.class));
    }

    @Test
    void shouldReturnNotFoundWithDefaultMessageWhenUpdateLanguageNoSuchElementExceptionMessageIsNull()
            throws Exception {
        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_999", "en");

        when(userService.updateUserLanguage(any(UpdateLanguageRequest.class)))
                .thenThrow(new NoSuchElementException((String) null));

        mockMvc.perform(put("/api/users/language")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Error"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService, times(1)).updateUserLanguage(any(UpdateLanguageRequest.class));
    }

    @Test
    void shouldReturnInternalServerErrorWhenUpdateLanguageFailsUnexpectedly() throws Exception {
        UpdateLanguageRequest request = new UpdateLanguageRequest("usr_1", "en");

        when(userService.updateUserLanguage(any(UpdateLanguageRequest.class)))
                .thenThrow(new RuntimeException("Database connectivity lost"));

        mockMvc.perform(put("/api/users/language")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected server error occurred: Database connectivity lost"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService, times(1)).updateUserLanguage(any(UpdateLanguageRequest.class));
    }

    @Test
    void shouldGetAllUsersSuccessfully() throws Exception {
        User u1 = User.builder().id("usr_1").username("rahul").fullName("Rahul Sharma").build();
        User u2 = User.builder().id("usr_2").username("sarah").fullName("Sarah Connor").build();

        when(userService.getAllUsers()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("usr_1"))
                .andExpect(jsonPath("$[0].username").value("rahul"))
                .andExpect(jsonPath("$[0].fullName").value("Rahul Sharma"))
                .andExpect(jsonPath("$[1].id").value("usr_2"))
                .andExpect(jsonPath("$[1].username").value("sarah"))
                .andExpect(jsonPath("$[1].fullName").value("Sarah Connor"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).getAllUsers();
    }
}
