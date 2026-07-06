package com.mahdi.model.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.mahdi.model.enums.Achievement;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.FontManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private int previousMaxHp = -1;
    private boolean isInitialized = false;

    // ---- اعلان‌های دستاورد ----
    private static final float NOTIFICATION_DURATION = 5.0f;
    private static final float FADE_DURATION = 2.0f;
    private static class Notification {
        String name;
        float timer;

        Notification(String name, float timer) {
            this.name = name;
            this.timer = timer;
        }
    }
    private final List<Notification> notifications = new ArrayList<>();
    private final Set<Achievement> notifiedAchievements = new HashSet<>();

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
        try {
            super.act(delta);
            hudStateTime += delta;
            shineStateTime += delta;

            if (AppStatus.getGameEngine().getPlayer() == null) return;

            // کپی اطلاعات از پلیر
            this.maxHP = AppStatus.getGameEngine().getPlayer().getMaxHp();
            this.currentHp = AppStatus.getGameEngine().getPlayer().getHp();
            this.currentGeo = AppStatus.getGameEngine().getPlayer().getGeo();
            this.currentSoul = AppStatus.getGameEngine().getPlayer().getSoul();

            // اگر maxHP تغییر کرده باشد (مثلاً با فعال شدن گاد مود)، آرایه‌ها را دوباره مقداردهی کن
            if (maxHP != previousMaxHp) {
                resizeHeartArrays(maxHP);
                previousMaxHp = maxHP;
                previousHp = currentHp;
                isInitialized = true;
            }

            // مقداردهی اولیه برای آرایه قلب‌ها در اولین فریم بازی
            if (!isInitialized && maxHP > 0) {
                heartStateTimes = new float[maxHP];
                heartVisualStates = new int[maxHP];
                for (int i = 0; i < maxHP; i++) {
                    heartVisualStates[i] = (i < currentHp) ? 3 : 0;
                }
                previousHp = currentHp;
                previousMaxHp = maxHP;
                isInitialized = true;
            }

            // آپدیت زمان داخلی انیمیشن هر قلب
            if (isInitialized) {
                for (int i = 0; i < maxHP; i++) {
                    heartStateTimes[i] += delta;
                }

                // الگوریتم هوشمند تشخیص صدمه (Damage) یا شفا (Heal)
                if (currentHp != previousHp) {
                    if (currentHp < previousHp) {
                        for (int i = currentHp; i < previousHp; i++) {
                            if (i >= 0 && i < maxHP) {
                                heartVisualStates[i] = 1;
                                heartStateTimes[i] = 0f;
                            }
                        }
                    } else {
                        for (int i = previousHp; i < currentHp; i++) {
                            if (i >= 0 && i < maxHP) {
                                heartVisualStates[i] = 2;
                                heartStateTimes[i] = 0f;
                            }
                        }
                    }
                    previousHp = currentHp;
                }

                for (int i = 0; i < maxHP; i++) {
                    if (heartVisualStates[i] == 1 && heartBreakAnim.isAnimationFinished(heartStateTimes[i])) {
                        heartVisualStates[i] = 0;
                    }
                    if (heartVisualStates[i] == 2 && heartRefillAnim.isAnimationFinished(heartStateTimes[i])) {
                        heartVisualStates[i] = 3;
                    }
                }
            }

            // ---- اعلان‌های دستاورد ----
            for (Achievement a : Achievement.values()) {
                if (a.isActive() && !notifiedAchievements.contains(a) && !a.seen()) {
                    a.setSeen(true);
                    notifications.add(new Notification(a.getDisplayName(), NOTIFICATION_DURATION));
                    notifiedAchievements.add(a);
                }
            }

            Iterator<Notification> it = notifications.iterator();
            while (it.hasNext()) {
                Notification n = it.next();
                n.timer -= delta;
                if (n.timer <= 0) {
                    it.remove();
                }
            }

        } catch (Exception e) {
            // نادیده گرفتن خطاهای احتمالی
        }
    }

    private void resizeHeartArrays(int newSize) {
        if (newSize <= 0) {
            isInitialized = false;
            return;
        }

        float[] newTimes = new float[newSize];
        int[] newStates = new int[newSize];

        for (int i = 0; i < newSize; i++) {
            if (i < currentHp) {
                newStates[i] = 3;   // پر ثابت/شاین
            } else {
                newStates[i] = 0;   // خالی
            }
            newTimes[i] = 0f;
        }

        heartStateTimes = newTimes;
        heartVisualStates = newStates;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        float yOffset = -60f;

        // ۱. نوار اصلی سلامت
        TextureRegion currentBarFrame = healthBarIntroAnim.getKeyFrame(hudStateTime, false);
        batch.draw(currentBarFrame, 40, 1300 + yOffset);

        // ۲. قلب‌ها
        if (isInitialized) {
            float startHeartX = 180f;
            float heartY = 1320f + yOffset;
            float spacingX = 65f;

            for (int i = 0; i < maxHP; i++) {
                float currentHeartX = startHeartX + (i * spacingX);
                TextureRegion heartRegionToDraw = emptyHeartFrame;

                switch (heartVisualStates[i]) {
                    case 0: heartRegionToDraw = emptyHeartFrame; break;
                    case 1: heartRegionToDraw = heartBreakAnim.getKeyFrame(heartStateTimes[i], false); break;
                    case 2: heartRegionToDraw = heartRefillAnim.getKeyFrame(heartStateTimes[i], false); break;
                    case 3: heartRegionToDraw = filledHeartFrame; break;
                }

                batch.draw(heartRegionToDraw, currentHeartX, heartY);

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

        // ۵. اعلان‌های دستاورد
        // ۵. اعلان‌های دستاورد
        float soulTextY = 1170 + yOffset;               // Y متن Soul
        float lineHeight = 85f;                         // فاصله‌ی بین دو اعلان (همان فاصله‌ی Geo تا Soul)
        float notificationBaseY = soulTextY - lineHeight; // قدیمی‌ترین اعلان اینجا قرار می‌گیرد

        int count = notifications.size();
// قدیمی‌ترین (index 0) را در baseY می‌گذاریم، جدیدترین‌ها پایین‌تر می‌روند
        for (int i = 0; i < count; i++) {
            Notification n = notifications.get(i);
            float y = notificationBaseY - i * lineHeight;   // i=0 قدیمی → بالا، i=1 جدید → پایین‌تر

            float alpha = (n.timer > FADE_DURATION) ? 1f : n.timer / FADE_DURATION;
            font.setColor(1f, 1f, 1f, alpha);
            font.draw(batch, "Achievement Unlocked: " + n.name, 60, y);
        }
        font.setColor(Color.WHITE);
    }
}
