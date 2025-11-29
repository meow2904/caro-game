package com.myproject.caro_game.models.res;

import lombok.Data;
@Data
public class UserRoomResponse {
    private String zoomId;
    private String userId;
    private String role;

    public UserRoomResponse(String zoomId, String userId, String role) {
        this.zoomId = zoomId;
        this.userId = userId;
        this.role = role;
    }
}
