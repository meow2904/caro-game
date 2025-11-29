package com.myproject.caro_game.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Message broker configuration
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages sent from client
        config.setApplicationDestinationPrefixes("/app");

        // Prefix cho user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint
        registry.addEndpoint("/ws")
                .addInterceptors(new HttpHandshakeInterceptor())
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Support fallback if browser does not support WebSocket
    }
    
}
