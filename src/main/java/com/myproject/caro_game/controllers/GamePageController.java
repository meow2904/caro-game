package com.myproject.caro_game.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GamePageController {

    @GetMapping("/game/{zoomId}")
    public String gamePage(Model model) {
        return "game"; 
    }
}
