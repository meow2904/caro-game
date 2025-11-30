package com.myproject.caro_game.models.res;

public class WinResponse {
    private String type = "WIN";
    private String zoomId;
    private String winner;

    public WinResponse() {}

    public WinResponse(String zoomId, String winner) {
        this.zoomId = zoomId;
        this.winner = winner;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getZoomId() { return zoomId; }
    public void setZoomId(String zoomId) { this.zoomId = zoomId; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
}
