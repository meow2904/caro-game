package com.myproject.caro_game.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.caro_game.models.dto.CreateZoomDto;
import com.myproject.caro_game.models.dto.JoinZoomDto;
import com.myproject.caro_game.models.res.ErrorResponse;
import com.myproject.caro_game.models.res.UserRoomResponse;
import com.myproject.caro_game.services.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/boardState")
    public ResponseEntity<?> getBoardState(@RequestParam("zoomId") String zoomId) {
        try {
            var game = gameService.getGame(zoomId);
            if (game == null) {
                throw new Exception("Room not found");
            }

            // If you win, reset the board, currentPlayer and winner.
            if (game.getWinner() != null) {
                game.resetGame();
            }
            var board = game.getBoard();
            var moves = board.getMoves();
            var moveList = new java.util.ArrayList<java.util.Map<String, Object>>();
            for (var entry : moves.entrySet()) {
                var point = entry.getKey();
                var symbol = entry.getValue();
                var move = new java.util.HashMap<String, Object>();
                move.put("x", point.x());
                move.put("y", point.y());
                move.put("symbol", symbol);
                moveList.add(move);
            }
            var resp = new java.util.HashMap<String, Object>();
            resp.put("moves", moveList);
            resp.put("currentPlayer", game.getCurrentPlayer() != null ? game.getCurrentPlayer().getUserId() : null);
            resp.put("players", game.getPlayers());
            resp.put("winner", game.getWinner());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            var err = new ErrorResponse();
            err.setType("BOARD_STATE_ERROR");
            err.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }

    @PostMapping("/createZoom")
    public UserRoomResponse createZoom(@RequestBody CreateZoomDto request) {
        return gameService.createRoom(request.getPlayerName());
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinZoom(@RequestBody JoinZoomDto request) {
        try {
            UserRoomResponse resp = gameService.joinToRoom(request.getZoomId(), request.getPlayerName(), request.getUserId());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            ErrorResponse err = new ErrorResponse();
            err.setType("JOIN_ERROR");
            err.setZoomId(request.getZoomId());
            err.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }

    @GetMapping("/checkZoom")
    public ResponseEntity<?> checkZoom(@RequestParam("zoomId") String zoomId) {
        try {
            boolean exists = gameService.getGame(zoomId) != null;
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("exists", exists);
            resp.put("zoomId", zoomId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            ErrorResponse err = new ErrorResponse();
            err.setType("CHECK_ERROR");
            err.setZoomId(zoomId);
            err.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @PostMapping("/resetGame")
    public ResponseEntity<?> resetGame(@RequestBody java.util.Map<String, String> request) {
        try {
            String zoomId = request.get("zoomId");
            if (zoomId == null || zoomId.isEmpty()) {
                throw new Exception("zoomId is required");
            }
            gameService.resetGame(zoomId);
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("success", true);
            resp.put("zoomId", zoomId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            ErrorResponse err = new ErrorResponse();
            err.setType("RESET_ERROR");
            err.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }
    }
}
