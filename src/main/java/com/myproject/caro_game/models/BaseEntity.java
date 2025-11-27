package com.myproject.caro_game.models;

import java.util.UUID;

public abstract class BaseEntity {
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
