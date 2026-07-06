package com.mahdi.model.map;

import com.badlogic.gdx.math.Rectangle;

public class SolidBlock {
    public Rectangle bounds;
    public boolean isDeadly;
    public String type;
    public int respawnId = -1;   // ☀️ شماره ری‌اسپاون (پیش‌فرض -۱ یعنی ندارد)

    public SolidBlock(float x, float y, float width, float height, boolean isDeadly, String type, int respawnId) {
        this.bounds = new Rectangle(x, y, width, height);
        this.isDeadly = isDeadly;
        this.type = type;
        this.respawnId = respawnId;
    }
}
