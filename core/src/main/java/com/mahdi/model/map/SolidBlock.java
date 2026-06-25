package com.mahdi.model.map;

import com.badlogic.gdx.math.Rectangle;

public class SolidBlock {
    public Rectangle bounds;
    public boolean isDeadly;
    public final String type;

    public SolidBlock(float x, float y, float width, float height, boolean isDeadly, String type) {
        this.bounds = new Rectangle(x, y, width, height);
        this.isDeadly = isDeadly;
        this.type = type;
    }
}
