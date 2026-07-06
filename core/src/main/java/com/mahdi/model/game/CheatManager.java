package com.mahdi.model.game;

import com.mahdi.model.enums.Achievement;
import com.mahdi.model.enums.CheatCode;

/**
 * مدیریت و اجرای کدهای تقلب.
 * در هر فریم از حلقهٔ اصلی بازی (مثلاً GameScreen) صدا زده شود.
 */
public class CheatManager {

    /**
     * وضعیت همهٔ کدهای تقلب را بررسی می‌کند.
     * اگر کلید مربوط به یک تقلب فشرده شده باشد، آن را فعال/غیرفعال می‌کند.
     */
    public static void update() {
        for (CheatCode cheat : CheatCode.values()) {
            if (cheat.isKeyJustPressed()) {
                Achievement.CHEATER.setActive(true);
                cheat.toggle();
                System.out.println("[CHEAT] " + cheat.getDisplayName() + " : " + (cheat.isActive() ? "ON" : "OFF"));
            }
        }
    }
}
