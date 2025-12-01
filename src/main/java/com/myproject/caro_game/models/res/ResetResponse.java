package com.myproject.caro_game.models.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetResponse {
    private String type = "RESET";
    private String zoomId;
    private String message = "Game has been reset";
}
