package com.myproject.caro_game.models.res;


import lombok.Data;

@Data
public class StartResponse {
    private String type;
    private String zoomId;
    private String userId;
    private String message;
}
