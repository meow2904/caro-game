package com.myproject.caro_game.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.core.env.Environment;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final Environment env;

    public WebSocketConfig(Environment env) {
        this.env = env;
    }
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
        String allowed = env.getProperty("ALLOWED_ORIGINS");
        if (allowed == null || allowed.isBlank()) {
            allowed = env.getProperty("app.allowed-origins", "*");
        }
        String[] patterns = parseAllowedOrigins(allowed);

        registry.addEndpoint("/ws")
                .addInterceptors(new HttpHandshakeInterceptor())
                .setAllowedOriginPatterns(patterns)
                .withSockJS(); // Support fallback if browser does not support WebSocket
    }
    
    private String[] parseAllowedOrigins(String value) {
        if (value == null) return new String[]{"*"};
        // allow comma-separated list, trim whitespace, ignore empty entries
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }
    
}
