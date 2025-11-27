package com.myproject.caro_game.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.caro_game.models.dto.CreateZoomDto;
import com.myproject.caro_game.services.GameService;

@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/createZoom")
    public String createZoom(@RequestBody CreateZoomDto request) {
        return gameService.createRoom(request.getPlayerName());
    }
}
