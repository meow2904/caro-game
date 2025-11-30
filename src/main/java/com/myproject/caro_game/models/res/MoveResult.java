package com.myproject.caro_game.models.res;

public class MoveResult {
    private boolean success;
    private String winnerUserId;

    public MoveResult() {}

    public MoveResult(boolean success, String winnerUserId) {
        this.success = success;
        this.winnerUserId = winnerUserId;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getWinnerUserId() { return winnerUserId; }
    public void setWinnerUserId(String winnerUserId) { this.winnerUserId = winnerUserId; }
}
