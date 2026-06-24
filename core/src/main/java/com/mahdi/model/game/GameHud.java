package com.mahdi.model.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.FontManager;

public class GameHud extends Group {

    private final BitmapFont font;
    private TextureAtlas hudAtlas;

    // انیمیشن‌های کلی HUD
    private Animation<TextureRegion> healthBarIntroAnim;
    private Animation<TextureRegion> geoIntroAnim;

    // انیمیشن‌های وضعیت هر قلب
    private Animation<TextureRegion> heartBreakAnim;
    private Animation<TextureRegion> heartRefillAnim;
    private Animation<TextureRegion> heartShineAnim;

    // تصاویر ثابت
    private TextureRegion filledHeartFrame;
    private TextureRegion emptyHeartFrame;

    // تایمرها
    private float hudStateTime = 0f;
    private float shineStateTime = 0f;

    // آرایه‌ای برای ذخیره وضعیت انیمیشن و زمان مستقل هر قلب
    private float[] heartStateTimes;
    private int[] heartVisualStates; // 0: ثابت خالی، 1: در حال شکستن، 2: در حال پر شدن، 3: ثابت پر/شاین

    // مقادیر محلی کش‌شده
    private int currentHp;
    private int currentGeo;
    private float currentSoul;
    private int maxHP;

    // متغیرهای کمکی برای تشخیص تغییرات HP در فریم قبلی
    private int previousHp = -1;
    private boolean isInitialized = false;

    public GameHud() {
        this.font = FontManager.getInstance().getEnglishMenuFont();

        // ۱. بارگذاری اطلس HUD از فولدر assets/HUD
        hudAtlas = new TextureAtlas(Gdx.files.internal("HUD/HUDAtlas.atlas"));

        // ۲. ساخت انیمیشن‌های شروع بازی (کند با زمان فریم 0.15 ثانیه)
        healthBarIntroAnim = new Animation<>(0.15f, hudAtlas.findRegions("HealthBar"), Animation.PlayMode.NORMAL);
        geoIntroAnim = new Animation<>(0.15f, hudAtlas.findRegions("Geo"), Animation.PlayMode.NORMAL);

        // ۳. ساخت انیمیشن‌های عملکردی قلب‌ها
        heartBreakAnim = new Animation<>(0.06f, hudAtlas.findRegions("BreakHealth"), Animation.PlayMode.NORMAL);
        heartRefillAnim = new Animation<>(0.06f, hudAtlas.findRegions("HealthRefill"), Animation.PlayMode.NORMAL);
        heartShineAnim = new Animation<>(0.10f, hudAtlas.findRegions("FilledHealthShine"), Animation.PlayMode.LOOP);

        // ۴. تصاویر ثابت قلب
        filledHeartFrame = hudAtlas.findRegion("FilledHealth");
        emptyHeartFrame = hudAtlas.findRegion("EmptyHealth");
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        hudStateTime += delta;
        shineStateTime += delta;

        if (AppStatus.getGameEngine().getPlayer() == null) return;

        // کپی اطلاعات از پلیر
        this.currentHp = AppStatus.getGameEngine().getPlayer().getHp();
        this.currentGeo = AppStatus.getGameEngine().getPlayer().getGeo();
        this.currentSoul = AppStatus.getGameEngine().getPlayer().getSoul();
        this.maxHP = AppStatus.getGameEngine().getPlayer().getMaxHp();

        // مقداردهی اولیه برای آرایه قلب‌ها در اولین فریم بازی
        if (!isInitialized && maxHP > 0) {
            heartStateTimes = new float[maxHP];
            heartVisualStates = new int[maxHP];
            for (int i = 0; i < maxHP; i++) {
                heartVisualStates[i] = (i < currentHp) ? 3 : 0; // اگر پر بود برود روی شاین، وگرنه خالی ثابت
            }
            previousHp = currentHp;
            isInitialized = true;
        }

        // آپدیت زمان داخلی انیمیشن هر قلب
        if (isInitialized) {
            for (int i = 0; i < maxHP; i++) {
                heartStateTimes[i] += delta;
            }

            // 🎯 الگوریتم هوشمند تشخیص صدمه (Damage) یا شفا (Heal)
            if (currentHp != previousHp) {
                if (currentHp < previousHp) {
                    // پلیر آسیب دیده -> قلب‌هایی که از دست رفته‌اند انیمیشن Break بگیرند
                    for (int i = currentHp; i < previousHp; i++) {
                        if (i >= 0 && i < maxHP) {
                            heartVisualStates[i] = 1; // وضعیت شکستن
                            heartStateTimes[i] = 0f;  // ریست تایمر انیمیشن قلب
                        }
                    }
                } else {
                    // پلیر شفا یافته -> قلب‌های جدید انیمیشن Refill بگیرند
                    for (int i = previousHp; i < currentHp; i++) {
                        if (i >= 0 && i < maxHP) {
                            heartVisualStates[i] = 2; // وضعیت پر شدن
                            heartStateTimes[i] = 0f;  // ریست تایمر انیمیشن قلب
                        }
                    }
                }
                previousHp = currentHp;
            }

            // چک کردن به پایان رسیدن انیمیشن‌های موقت (Break و Refill)
            for (int i = 0; i < maxHP; i++) {
                if (heartVisualStates[i] == 1 && heartBreakAnim.isAnimationFinished(heartStateTimes[i])) {
                    heartVisualStates[i] = 0; // تبدیل به خالی ثابت پس از پایان انیمیشن شکستن
                }
                if (heartVisualStates[i] == 2 && heartRefillAnim.isAnimationFinished(heartStateTimes[i])) {
                    heartVisualStates[i] = 3; // تبدیل به پر ثابت/شاین پس از پایان انیمیشن پر شدن
                }
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        // 🌟 آفست عمودی برای پایین آوردن کل HUD
        float yOffset = -60f; // هر چقدر نیاز داری تغییر بده

        // ۱. نوار اصلی سلامت (health bar intro)
        TextureRegion currentBarFrame = healthBarIntroAnim.getKeyFrame(hudStateTime, false);
        batch.draw(currentBarFrame, 40, 1300 + yOffset);

        // ۲. قلب‌ها (داینامیک بر اساس maxHP)
        if (isInitialized) {
            float startHeartX = 180f;
            float heartY = 1320f + yOffset;   // پایین‌تر
            float spacingX = 65f;

            for (int i = 0; i < maxHP; i++) {
                float currentHeartX = startHeartX + (i * spacingX);
                TextureRegion heartRegionToDraw = emptyHeartFrame;

                switch (heartVisualStates[i]) {
                    case 0: // خالی
                        heartRegionToDraw = emptyHeartFrame;
                        break;
                    case 1: // در حال شکستن (آسیب)
                        heartRegionToDraw = heartBreakAnim.getKeyFrame(heartStateTimes[i], false);
                        break;
                    case 2: // در حال پر شدن (شفا)
                        heartRegionToDraw = heartRefillAnim.getKeyFrame(heartStateTimes[i], false);
                        break;
                    case 3: // پر ثابت
                        heartRegionToDraw = filledHeartFrame;
                        break;
                }

                // رسم قلب اصلی
                batch.draw(heartRegionToDraw, currentHeartX, heartY);

                // اگر قلب پر است، درخشش اضافی رندر شود
                if (heartVisualStates[i] == 3) {
                    TextureRegion shineFrame = heartShineAnim.getKeyFrame(shineStateTime, true);
                    batch.draw(shineFrame, currentHeartX, heartY);
                }
            }
        }

        // ۳. آیکون سکه (GEO)
        TextureRegion currentGeoIcon = geoIntroAnim.getKeyFrame(hudStateTime, false);
        batch.draw(currentGeoIcon, 50, 1220 + yOffset);

        // ۴. متن‌ها با فونت سفید
        font.setColor(Color.WHITE);
        font.draw(batch, "" + currentGeo, 130, 1255 + yOffset);
        font.draw(batch, "SOUL: " + (int) currentSoul + "%", 60, 1170 + yOffset);
    }
//    @Override
//    public void dispose() {
//        if (hudAtlas != null) hudAtlas.dispose();
//    }
}
