package com.talktranslate.config;

import com.talktranslate.model.UserPrincipal;
import com.talktranslate.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Principal;

/**
 * Filter that intercepts incoming HTTP requests, validates JWT Bearer tokens,
 * and attaches authenticated UserPrincipal to the request context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    public static final String AUTHENTICATED_USER_ATTR = "authenticatedUser";

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(@Autowired(required = false) JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (jwtTokenService != null) {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                if (jwtTokenService.validateToken(token)) {
                    String userId = jwtTokenService.extractUserId(token);
                    String username = jwtTokenService.extractUsername(token);

                    if (userId != null) {
                        UserPrincipal principal = new UserPrincipal(userId, username);
                        request.setAttribute(AUTHENTICATED_USER_ATTR, principal);

                        HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                            @Override
                            public Principal getUserPrincipal() {
                                return principal;
                            }

                            @Override
                            public String getRemoteUser() {
                                return username;
                            }
                        };

                        logger.debug("Authenticated request from user ID: {}, username: {}", userId, username);
                        filterChain.doFilter(wrappedRequest, response);
                        return;
                    }
                } else {
                    logger.debug("Invalid or expired JWT token in Authorization header");
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
