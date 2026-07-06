package com.mahdi.model.enums;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public enum CheatCode {
    BOSS_ARENA_TELEPORT(
        Input.Keys.NUM_1,
        "Boss Arena Teleport",
        "Teleport to False Knight arena",
        false
    ),
    ANTIGRAVITY(
        Input.Keys.NUM_2,
        "Anti Gravity",
        "Noclip / Spectator mode (no gravity)",
        false
    ),
    EMERGENCY_HEAL(
        Input.Keys.NUM_3,
        "Emergency Heal",
        "Refill HP to full when health is empty",
        false
    ),
    SOUL_REFILL(
        Input.Keys.NUM_4,
        "Soul Refill",
        "Fill Soul Vessel completely",
        false
    ),
    GOD_MODE(
        Input.Keys.NUM_5,
        "God Mode",
        "God mode – no damage from any source",
        false
    ),
    NO_LIMIT(
        Input.Keys.NUM_6,
        "No Limit",
        "No limit for Dash and Double Jump",
        false
    );

    private final int key;
    private final String displayName;
    private final String description;
    private boolean active;

    CheatCode(int key, String displayName, String description, boolean activeDefault) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.active = activeDefault;
    }

    /** آیا کلید مربوطه همین حالا زده شده است (JustPressed) */
    public boolean isKeyJustPressed() {
        return Gdx.input.isKeyJustPressed(key);
    }

    /** وضعیت فعال بودن تقلب */
    public boolean isActive() {
        return active;
    }

    /** فعال/غیرفعال کردن تقلب */
    public void toggle() {
        active = !active;
    }

    /** نام تمیز برای نمایش در UI یا راهنما */
    public String getDisplayName() {
        return displayName;
    }

    /** توضیح انگلیسی کاربرد تقلب */
    public String getDescription() {
        return description;
    }

    /** کلید میانبر */
    public int getKey() {
        return key;
    }

    /** نام کلید به صورت رشته (برای نمایش) */
    public String getKeyName() {
        return Input.Keys.toString(key);
    }

    public void setFalse() {
        this.active = false;
    }
}
