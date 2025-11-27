package com.myproject.caro_game.models;

import java.util.UUID;

public class Player extends BaseEntity {
    private String name;
    private char symbol; // 'X' or 'O'

    public Player() {
        this.setId(UUID.randomUUID());
        
    }

    public Player(String name, char symbol) {
        this.setId(UUID.randomUUID());
        this.name = name;
        this.symbol = symbol;
    }

    public String getUserId() {
        return this.getId().toString();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }
}