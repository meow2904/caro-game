package com.myproject.caro_game.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.myproject.caro_game.models.Player;
import com.myproject.caro_game.models.res.ErrorResponse;
import com.myproject.caro_game.services.GameService;

@Component
public class WebSocketEventListener {
    
    @Autowired
    private GameService gameService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // When the client connects
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        String userId = (String) sha.getSessionAttributes().get("userId");
        String zoomId = (String) sha.getSessionAttributes().get("zoomId");
        System.out.println(" ===> User " + userId + " connected to room " + zoomId);
    }

    // When the client disconnect
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        
        System.out.println(" ===>  Session " + sessionId + " disconnected (userId: " + userId + ")");
        
        if (userId == null) {
            return;
        }
        
        // Handle player disconnect from game
        Map<String, Object> disconnectInfo = gameService.handlePlayerDisconnect(userId);
        
        if (disconnectInfo != null) {
            String zoomId = (String) disconnectInfo.get("zoomId");
            Player opponent = (Player) disconnectInfo.get("opponent");
            boolean gameDeleted = (boolean) disconnectInfo.get("gameDeleted");
            
            System.out.println(" ===> Player " + userId + " removed from room " + zoomId);
            
            if (gameDeleted) {
                System.out.println(" ===> Game room " + zoomId + " deleted (no players left)");
            } else if (opponent != null) {
                // Notify opponent that the other player disconnected
                ErrorResponse disconnectMsg = new ErrorResponse();
                disconnectMsg.setType("OPPONENT_DISCONNECTED");
                disconnectMsg.setZoomId(zoomId);
                disconnectMsg.setMessage("Opponent has disconnected from the game");
                messagingTemplate.convertAndSend("/queue/" + opponent.getUserId() + "/message", disconnectMsg);
                System.out.println(" ===> Notified opponent " + opponent.getUserId() + " about disconnect");
            }
        }
    }
}

