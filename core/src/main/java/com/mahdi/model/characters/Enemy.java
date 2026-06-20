package com.mahdi.model.characters;

public abstract class Enemy extends BaseCharacter {

    protected Enemy (float x, float y, float width, float height, float maxXSpeed, float acceleration) {
        super(x, y, width, height, maxXSpeed, acceleration);
    }
    
}
