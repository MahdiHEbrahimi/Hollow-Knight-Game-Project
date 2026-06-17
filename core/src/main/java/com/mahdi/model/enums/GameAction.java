package com.mahdi.model.enums;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;

public enum GameAction {
    // --- اکشن‌های حرکتی ---
    MOVE_LEFT(Keys.A),
    MOVE_RIGHT(Keys.D),
    MOVE_UP(Keys.W),
    MOVE_DOWN(Keys.S),

    // --- مکانیک‌های اصلی شوالیه ---
    JUMP(Keys.SPACE),
    DASH(Keys.SHIFT_LEFT),
    ATTACK(Keys.K),  // ضربه با شمشیر (Nail)
    FOCUS(Keys.F),   // تمرکز برای هیل کردن با روح

    // --- اکشن‌های رابط کاربری (UI) ---
    PAUSE(Keys.ESCAPE),
    INVENTORY(Keys.I);

    // کلید پیش‌فرض و ثابت (غیر قابل تغییر) برای دکمه Reset
    private final int defaultKey;

    // کلید فعلی بازی که در تنظیمات تغییر می‌کند
    private int currentKey;

    GameAction(int defaultKey) {
        this.defaultKey = defaultKey;
        this.currentKey = defaultKey;
    }

    /**
     * روش Polling: آیا در این فریم دست بازیکن روی کلید این اکشن هست؟
     */
    public boolean isPressed() {
        return Gdx.input.isKeyPressed(currentKey);
    }

    /**
     * روش Trigger: آیا بازیکن دقیقاً در همین فریم کلید این اکشن را فشار داده است؟
     */
    public boolean isJustPressed() {
        return Gdx.input.isKeyJustPressed(currentKey);
    }

    /**
     * تغییر کلید این اکشن در منوی تنظیمات
     */
    public void setKey(int newKey) {
        this.currentKey = newKey;
    }

    /**
     * دریافت کد عددی کلید فعلی
     */
    public int getKey() {
        return currentKey;
    }

    /**
     * دریافت نام متنی و خوانای کلید فعلی (مثلاً برای نمایش روی دکمه‌های منو)
     * خروجی مثلاً: "A", "SPACE", "LEFT SHIFT"
     */
    public String getKeyName() {
        return Input.Keys.toString(currentKey);
    }

    /**
     * بازگرداندن همین یک کلید به حالت پیش‌فرض
     */
    public void resetToDefault() {
        this.currentKey = this.defaultKey;
    }

    // ==========================================
    // متدهای استاتیک (Static Methods) برای کل بازی
    // ==========================================

    /**
     * 🌟 ریست کردن تمام کلیدهای بازی به صورت یکجا به حالت کارخانه
     * کاربرد: برای دکمه "Reset All" در منوی تنظیمات
     */
    public static void resetAllToDefault() {
        for (GameAction action : GameAction.values()) {
            action.resetToDefault();
        }
    }
}
