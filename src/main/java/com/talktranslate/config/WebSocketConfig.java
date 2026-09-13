package com.talktranslate.config;

import com.talktranslate.model.UserPrincipal;
import com.talktranslate.service.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    private final JwtTokenService jwtTokenService;

    public WebSocketConfig(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket connection endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Inbound message destination prefix
        registry.setApplicationDestinationPrefixes("/app");

        // Message broker prefixes for topics and user queues
        registry.enableSimpleBroker("/topic", "/queue", "/user");

        // Point-to-point user destination prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader == null || authHeader.isBlank()) {
                        authHeader = accessor.getFirstNativeHeader("token");
                    }

                    if (authHeader != null && !authHeader.isBlank()) {
                        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7).trim() : authHeader.trim();
                        if (jwtTokenService.validateToken(token)) {
                            String userId = jwtTokenService.extractUserId(token);
                            String username = jwtTokenService.extractUsername(token);
                            if (userId != null) {
                                UserPrincipal userPrincipal = new UserPrincipal(userId, username);
                                if (accessor.isMutable()) {
                                    accessor.setUser(userPrincipal);
                                    logger.info("STOMP session authenticated for user: {} (ID: {})", username, userId);
                                } else {
                                    StompHeaderAccessor mutableAccessor = StompHeaderAccessor.wrap(message);
                                    mutableAccessor.setUser(userPrincipal);
                                    logger.info("STOMP session authenticated for user: {} (ID: {})", username, userId);
                                    return org.springframework.messaging.support.MessageBuilder.createMessage(message.getPayload(), mutableAccessor.getMessageHeaders());
                                }
                            }
                        } else {
                            logger.warn("STOMP CONNECT token validation failed");
                        }
                    }
                }
                return message;
            }
        });
    }
}
