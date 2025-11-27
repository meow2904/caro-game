package com.myproject.caro_game.models;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private final Map<Point, Character> moves = new HashMap<>();

    public boolean placeMove(Point point, char symbol) {
        if (moves.containsKey(point)) return false;
        moves.put(point, symbol);
        return true;
    }

    public Character getSymbolAt(int x, int y) {
        return moves.getOrDefault(new Point(x, y), null);
    }

    public Map<Point, Character> getMoves() { return moves; }
}