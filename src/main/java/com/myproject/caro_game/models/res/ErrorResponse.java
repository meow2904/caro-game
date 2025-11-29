package com.myproject.caro_game.models.res;


import lombok.Data;
@Data
public class ErrorResponse {
    private String type;
    private String zoomId;
    private String message;
}
