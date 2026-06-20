package com.mahdi.model.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.FontManager;

public class GameHud extends Group {

    private final BitmapFont font;

    // مقادیر محلی کش‌شده برای استفاده در متد رسم
    private int currentHp;
    private int currentGeo;
    private float currentSoul;

    public GameHud() {
        this.font = FontManager.getInstance().getEnglishMenuFont();
    }

    /**
     * متد act هر فریم قبل از رسم صدا زده می‌شود؛
     * حالا خیلی سریع و مستقیم اطلاعات را از روی پوینتر کپی می‌کند.
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        

            this.currentHp = AppStatus.getGameStatus().getPlayer().getHp();
            this.currentGeo = AppStatus.getGameStatus().getPlayer().getGeo();
            this.currentSoul = AppStatus.getGameStatus().getPlayer().getSoul();
        
    }

    /**
     * متد رسم HUD؛ چون روی استیج اصلی است، کاملاً روی صفحه ثابت می‌ماند
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        

        // تغییر رنگ فونت به سفید برای خوانایی بهتر در تم تاریک هالو نایت
        font.setColor(Color.WHITE);
        
        // چاپ تستی اطلاعات در گوشه بالا سمت چپ مانیتور فرضی (ویوپورت 2560x1440)
        font.draw(batch, "HP: " + currentHp, 60, 1400);
        font.draw(batch, "GEO: " + currentGeo, 60, 1340);
        font.draw(batch, "SOUL: " + (int)currentSoul + "%", 60, 1280);
    }
}