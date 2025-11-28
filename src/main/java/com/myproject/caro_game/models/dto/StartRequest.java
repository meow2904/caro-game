package com.myproject.caro_game.models.dto;

import lombok.Data;

@Data
public class StartRequest {
    private String zoomId;
    private String userId;
    private boolean firstTurn;
}
