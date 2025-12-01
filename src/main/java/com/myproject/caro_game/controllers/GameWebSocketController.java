package com.myproject.caro_game.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.myproject.caro_game.models.Game;
import com.myproject.caro_game.models.Move;
import com.myproject.caro_game.models.Player;
import com.myproject.caro_game.models.dto.StartRequest;
import com.myproject.caro_game.models.res.ErrorResponse;
import com.myproject.caro_game.models.res.MoveResponse;
import com.myproject.caro_game.models.res.MoveResult;
import com.myproject.caro_game.models.res.ResetResponse;
import com.myproject.caro_game.models.res.StartResponse;
import com.myproject.caro_game.models.res.WinResponse;
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
        err.setMessage("Test message from server");
        messagingTemplate.convertAndSend("/queue/" + request.getUserId() + "/message", err);
    }

    @MessageMapping("/move")
    public void handleMove(@Payload Move move) {
        try {
            System.out.println("Received move: " + move);
            String zoomId = move.zoomId();

            // Validate move and check win
            MoveResult result = gameService.placeMove(zoomId, move);
            if (!result.isSuccess()) {
                ErrorResponse err = new ErrorResponse();
                err.setType("ERROR");
                err.setZoomId(zoomId);
                err.setMessage("Nước đi không hợp lệ");
                messagingTemplate.convertAndSend("/queue/" + move.userId() + "/message", err);
                return;
            }

            // Determine symbol for broadcasting (look up player)
            Game game = gameService.getGame(zoomId);
            Player player = game.getPlayers().stream()
                    .filter(p -> p.getUserId().equals(move.userId()))
                    .findFirst()
                    .orElse(null);
            char symbol = (player != null) ? player.getSymbol() : 'X';

            // Broadcast move to all players in room
            MoveResponse moveResponse = new MoveResponse(move.x(), move.y(), symbol, move.userId(), zoomId);
            messagingTemplate.convertAndSend("/topic/room." + zoomId, moveResponse);
            System.out.println("Move broadcasted to room: " + zoomId);

            // If there is a winner, broadcast WIN
            if (result.getWinnerUserId() != null) {
                WinResponse win = new WinResponse(zoomId, result.getWinnerUserId());
                messagingTemplate.convertAndSend("/topic/room." + zoomId, win);
                System.out.println("WIN broadcasted for room: " + zoomId + ", winner: " + result.getWinnerUserId());
            }

        } catch (Exception e) {
            ErrorResponse err = new ErrorResponse();
            err.setType("ERROR");
            err.setZoomId(move.zoomId());
            err.setMessage(e.getMessage());
            messagingTemplate.convertAndSend("/queue/" + move.userId() + "/message", err);
        }
    }

    @MessageMapping("/start")
    public void handleStart(@Payload StartRequest request) {

        try {
            String zoomId = request.getZoomId();
            String userId = request.getUserId();
            gameService.startGame(zoomId, userId, request.isFirstTurn());

            // Notify all players in the room that the game has started and who the current player is
            StartResponse response = new StartResponse();
            response.setType("START");
            response.setZoomId(zoomId);

            //Room creator id
            //Determine who created the room before or after
            response.setUserId(userId);
            response.setFirstTurn(request.isFirstTurn());
            response.setMessage("Game start!");
            messagingTemplate.convertAndSend("/topic/room." + zoomId, response);

            // messagingTemplate.convertAndSend(
            //         "/queue/" + request.getUserId() + "/message",
            //         response);

        } catch (Exception e) {
            ErrorResponse err = new ErrorResponse();
            err.setType("ERROR");
            err.setZoomId(request.getZoomId());
            err.setMessage(e.getMessage());
            messagingTemplate.convertAndSend(
                    "/queue/" + request.getUserId() + "/message",
                    err);
        }
    }

    @MessageMapping("/reset")
    public void handleReset(@Payload java.util.Map<String, String> request) {
        try {
            String zoomId = request.get("zoomId");
            if (zoomId == null || zoomId.isEmpty()) {
                throw new Exception("zoomId is required");
            }

            // Reset game on server side
            gameService.resetGame(zoomId);

            // Broadcast RESET message to all players in room
            ResetResponse resetResponse = new ResetResponse(null, zoomId, "Game has been reset");
            resetResponse.setType("RESET");
            messagingTemplate.convertAndSend("/topic/room." + zoomId, (Object) resetResponse);
            System.out.println("RESET broadcasted for room: " + zoomId);

        } catch (Exception e) {
            System.err.println("Error handling reset: " + e.getMessage());
        }
    }

}
