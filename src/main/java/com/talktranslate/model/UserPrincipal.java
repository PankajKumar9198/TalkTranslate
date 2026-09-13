package com.talktranslate.model;

import java.security.Principal;
import java.util.Objects;

/**
 * Principal representing an authenticated TalkTranslate user.
 * The principal name maps to the unique user ID for STOMP user queue destination routing.
 */
public class UserPrincipal implements Principal {

    private final String userId;
    private final String username;

    public UserPrincipal(String userId, String username) {
        this.userId = userId != null ? userId : "";
        this.username = username != null ? username : "";
    }

    @Override
    public String getName() {
        return userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(userId, that.userId) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
