package com.myproject.caro_game.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.myproject.caro_game.models.Game;
import com.myproject.caro_game.models.Player;
import com.myproject.caro_game.models.dto.StartRequest;
import com.myproject.caro_game.models.dto.StartResponse;
import com.myproject.caro_game.services.GameService;

public class GameWebSocketController {

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Xử lý khi game bắt đầu
     * Client gửi đến: /app/start
     */
    @MessageMapping("/start")
    public void handleStart(@Payload StartRequest request) {
        String zoomId = request.getZoomId();
        String userId = request.getUserId();

        boolean ok = gameService.startGame(zoomId, userId, request.isFirstTurn());
        if (!ok) {
            // gửi lỗi cho host nếu muốn
            return;
        }

        Game game = gameService.getGame(zoomId);
        Player current = game.getCurrentPlayer();

        StartResponse response = new StartResponse();
        response.setType("START");
        response.setZoomId(zoomId);
        response.setUserId(current.getUserId());  // người bắt đầu
        response.setMessage("Game bắt đầu!");

        messagingTemplate.convertAndSend("/topic/room." + zoomId, response);
    }

}
