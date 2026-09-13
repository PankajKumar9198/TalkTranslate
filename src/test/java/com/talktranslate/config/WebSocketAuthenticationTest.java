package com.talktranslate.config;

import com.talktranslate.model.User;
import com.talktranslate.model.UserPrincipal;
import com.talktranslate.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

class WebSocketAuthenticationTest {

    private JwtTokenService jwtTokenService;
    private WebSocketConfig webSocketConfig;
    private ChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService("SecretKeyForWebSocketAuthTesting123456", 3600000);
        webSocketConfig = new WebSocketConfig(jwtTokenService);

        List<ChannelInterceptor> interceptors = new ArrayList<>();
        ChannelRegistration registration = new ChannelRegistration() {
            @Override
            public ChannelRegistration interceptors(ChannelInterceptor... channelInterceptors) {
                interceptors.addAll(List.of(channelInterceptors));
                return this;
            }
        };

        webSocketConfig.configureClientInboundChannel(registration);
        assertThat(interceptors).isNotEmpty();
        interceptor = interceptors.get(0);
    }

    @Test
    void shouldAuthenticateStompConnectFrameWithBearerToken() {
        User user = User.builder()
                .id("usr_100")
                .username("priya_singh")
                .email("priya@test.com")
                .fullName("Priya Singh")
                .build();

        String token = jwtTokenService.generateToken(user);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer " + token);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        MessageChannel channel = mock(MessageChannel.class);
        Message<?> result = interceptor.preSend(message, channel);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo("usr_100");
        assertThat(((UserPrincipal) resultAccessor.getUser()).getUsername()).isEqualTo("priya_singh");
    }

    @Test
    void shouldAuthenticateStompConnectFrameWithTokenHeader() {
        User user = User.builder()
                .id("usr_200")
                .username("carlos_g")
                .email("carlos@test.com")
                .fullName("Carlos Garcia")
                .build();

        String token = jwtTokenService.generateToken(user);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("token", token);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        MessageChannel channel = mock(MessageChannel.class);
        Message<?> result = interceptor.preSend(message, channel);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNotNull();
        assertThat(resultAccessor.getUser().getName()).isEqualTo("usr_200");
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer invalid_token_xyz");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        MessageChannel channel = mock(MessageChannel.class);
        Message<?> result = interceptor.preSend(message, channel);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNull();
    }

    @Test
    void shouldNotAuthenticateWhenHeaderIsMissing() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        MessageChannel channel = mock(MessageChannel.class);
        Message<?> result = interceptor.preSend(message, channel);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);
        assertThat(resultAccessor.getUser()).isNull();
    }

    @Test
    void shouldVerifyUserPrincipalProperties() {
        UserPrincipal principal1 = new UserPrincipal("u1", "user1");
        UserPrincipal principal2 = new UserPrincipal("u1", "user1");
        UserPrincipal principal3 = new UserPrincipal("u2", "user2");

        assertAll(
                () -> assertThat(principal1.getName()).isEqualTo("u1"),
                () -> assertThat(principal1.getUserId()).isEqualTo("u1"),
                () -> assertThat(principal1.getUsername()).isEqualTo("user1"),
                () -> assertThat(principal1).isEqualTo(principal2),
                () -> assertThat(principal1).isNotEqualTo(principal3),
                () -> assertThat(principal1.hashCode()).isEqualTo(principal2.hashCode()),
                () -> assertThat(principal1.toString()).contains("u1").contains("user1")
        );
    }
}
