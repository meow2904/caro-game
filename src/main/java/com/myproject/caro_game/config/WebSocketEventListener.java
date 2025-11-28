package com.myproject.caro_game.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.myproject.caro_game.services.WebSocketSessionService;

@Component
public class WebSocketEventListener {
    
    @Autowired
    private WebSocketSessionService sessionService;

    // Khi client connect
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();

        String userId = (String) sha.getSessionAttributes().get("userId");
        String zoomId = (String) sha.getSessionAttributes().get("zoomId");
        if (zoomId != null && userId != null) {
            sessionService.registerSession(zoomId, userId, sessionId);
            System.out.println("User " + userId + " connected to room " + zoomId);
        }
    }

    // Khi client disconnect
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        if (sessionId != null) {
            sessionService.removeSession(sessionId);
            System.out.println("Session " + sessionId + " disconnected");
        }
    }
}
