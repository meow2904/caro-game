package com.myproject.caro_game.models.res;

public class MoveResponse {
    private String type = "MOVE";
    private int x;
    private int y;
    private char symbol;
    private String userId;
    private String zoomId;

    public MoveResponse() {}

    public MoveResponse(int x, int y, char symbol, String userId, String zoomId) {
        this.x = x;
        this.y = y;
        this.symbol = symbol;
        this.userId = userId;
        this.zoomId = zoomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getZoomId() {
        return zoomId;
    }

    public void setZoomId(String zoomId) {
        this.zoomId = zoomId;
    }
}
