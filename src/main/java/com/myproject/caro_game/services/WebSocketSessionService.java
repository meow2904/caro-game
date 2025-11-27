package com.myproject.caro_game.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class WebSocketSessionService  {
    // zoomId -> (userId -> sessionId)
    private final Map<String, Map<String, String>> roomSessions = new HashMap<>();

    // khi user connect
    public void registerSession(String zoomId, String userId, String sessionId) {
        roomSessions.putIfAbsent(zoomId, new HashMap<>());
        roomSessions.get(zoomId).put(userId, sessionId);
    }

    // khi user disconnect
    public void removeSession(String sessionId) {
        roomSessions.forEach((zoomId, map) -> {
            map.entrySet().removeIf(entry -> entry.getValue().equals(sessionId));
        });
    }

    public Map<String, String> getRoomSessions(String zoomId) {
        return roomSessions.getOrDefault(zoomId, new HashMap<>());
    }
}
