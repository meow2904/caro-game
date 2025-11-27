package com.myproject.caro_game.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GamePageController {

    @GetMapping("/game/{zoomId}")
    public String gamePage(@PathVariable String zoomId, Model model) {
        model.addAttribute("zoomId", zoomId);
        return "game"; 
    }
}
