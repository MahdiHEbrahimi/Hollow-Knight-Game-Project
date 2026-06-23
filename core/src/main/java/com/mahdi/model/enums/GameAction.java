package com.mahdi.model.enums;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;

public enum GameAction {
    MOVE_LEFT(Keys.A),
    MOVE_RIGHT(Keys.D),
    MOVE_UP(Keys.W),
    MOVE_DOWN(Keys.S),

    JUMP(Keys.SPACE),
    DASH(Keys.J),
    ATTACK(Keys.K),
    FOCUS(Keys.F),

    PAUSE(Keys.ESCAPE),
    INVENTORY(Keys.I);

    private final int defaultKey;

    private int currentKey;

    GameAction(int defaultKey) {
        this.defaultKey = defaultKey;
        this.currentKey = defaultKey;
    }

    public boolean isPressed() {
        return Gdx.input.isKeyPressed(currentKey);
    }

    public boolean isJustPressed() {
        return Gdx.input.isKeyJustPressed(currentKey);
    }

    public void setKey(int newKey) {
        this.currentKey = newKey;
    }

    public int getKey() {
        return currentKey;
    }


    public String getKeyName() {
        return Input.Keys.toString(currentKey);
    }

    public void resetToDefault() {
        this.currentKey = this.defaultKey;
    }

    public static void resetAllToDefault() {
        for (GameAction action : GameAction.values()) {
            action.resetToDefault();
        }
    }
}
