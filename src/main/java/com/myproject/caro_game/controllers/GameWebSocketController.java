package com.myproject.caro_game.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.myproject.caro_game.models.Game;
import com.myproject.caro_game.models.Player;
import com.myproject.caro_game.models.dto.StartRequest;
import com.myproject.caro_game.models.res.ErrorResponse;
import com.myproject.caro_game.models.res.StartResponse;
import com.myproject.caro_game.services.GameService;

@Controller
public class GameWebSocketController {

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/test")
    public void test(@Payload StartRequest request) {
        System.out.println("Received test message: " + request);

        ErrorResponse err = new ErrorResponse();
            err.setType("ERROR");
            err.setZoomId(request.getZoomId());
            err.setMessage("aaaaaaaaaa");

            messagingTemplate.convertAndSend("/user/" + request.getUserId() + "/queue/errors", err);
            // messagingTemplate.convertAndSendToUser( request.getUserId(), "/queue/errors", err);
    }

    @MessageMapping("/start")
    public void handleStart(@Payload StartRequest request) {

        try {
            String zoomId = request.getZoomId();
            String userId = request.getUserId();

            gameService.startGame(zoomId, userId, request.isFirstTurn());

            Game game = gameService.getGame(zoomId);
            Player current = game.getCurrentPlayer();

            StartResponse response = new StartResponse();
            response.setType("START");
            response.setZoomId(zoomId);
            response.setUserId(current.getUserId());
            response.setMessage("Game bắt đầu!");

            messagingTemplate.convertAndSend("/topic/room." + zoomId, response);

        } catch (Exception e) {
            ErrorResponse err = new ErrorResponse();
            err.setType("ERROR");
            err.setZoomId(request.getZoomId());
            err.setMessage(e.getMessage());
            messagingTemplate.convertAndSendToUser(
                    request.getUserId(),
                    "/queue/errors",
                    err);
        }
    }

}
