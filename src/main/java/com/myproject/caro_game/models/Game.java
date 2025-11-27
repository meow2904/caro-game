package com.myproject.caro_game.models;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private Board board = new Board();
    private final List<Player> players = new ArrayList<>();
    private Player currentPlayer;

    public Board getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
        if (players.size() < 2) {
            players.add(player);
            if (currentPlayer == null)
                currentPlayer = player;
        }
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void switchPlayer() {
        if (players.size() < 2) return;
        currentPlayer = (currentPlayer == players.get(0)) ? players.get(1) : players.get(0);
    }

}
