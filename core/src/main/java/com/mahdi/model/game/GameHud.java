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
    private TextureAtlas soulAtlas;

    private Animation<TextureRegion> healthBarIntroAnim;
    private Animation<TextureRegion> geoIntroAnim;

    // انیمیشن‌های وضعیت هر قلب
    private Animation<TextureRegion> heartBreakAnim;
    private Animation<TextureRegion> heartRefillAnim;
    private Animation<TextureRegion> heartShineAnim;

    // تصاویر ثابت قلب
    private TextureRegion filledHeartFrame;
    private TextureRegion emptyHeartFrame;

    // --- Soul Tank ---
    private TextureRegion emptyTankFrame;
    private Animation<TextureRegion> idle33Anim, idle66Anim, idle99Anim;
    private Animation<TextureRegion> fillTo33Anim, fillTo66Anim, fillTo99Anim;

    private enum SoulState { EMPTY, LEVEL33, LEVEL66, LEVEL99, FILLING }
    private SoulState currentSoulState = SoulState.EMPTY;
    private SoulState previousSoulLevel = SoulState.EMPTY;
    private float soulAnimTime = 0f;
    private int previousLevelIndex = 0;        // 0..3
    private int fillTargetIndex = 0;           // سطحی که fill به سمت آن می‌رود

    // ☀️ تنظیمات موقعیت و اندازه تانک روح (CSS-like)
    private static final float SOUL_TANK_X = 5f;       // left
    private static final float SOUL_TANK_Y_OFFSET = 1260f;  // top (بدون yOffset)
    private static final float SOUL_TANK_SCALE = 2.65f;  // مقیاس (۱ = اندازه اصلی)
    private static final float SOUL_TANK_SIZE = 80f;    // اندازه پایه
    // -----------------

    // تایمرها
    private float hudStateTime = 0f;
    private float shineStateTime = 0f;

    private float[] heartStateTimes;
    private int[] heartVisualStates;

    private int currentHp;
    private int currentGeo;
    private float currentSoul;
    private int maxHP;

    private int previousHp = -1;
    private int previousMaxHp = -1;
    private boolean isInitialized = false;

    // ---- اعلان‌های دستاورد ----
    private static final float NOTIFICATION_DURATION = 5.0f;
    private static final float FADE_DURATION = 2.0f;
    private static class Notification {
        String name; float timer;
        Notification(String name, float timer) { this.name = name; this.timer = timer; }
    }
    private final List<Notification> notifications = new ArrayList<>();
    private final Set<Achievement> notifiedAchievements = new HashSet<>();

    public GameHud() {
        this.font = FontManager.getInstance().getEnglishMenuFont();

        // ۱. اطلس اصلی HUD
        hudAtlas = new TextureAtlas(Gdx.files.internal("HUD/HUDAtlas.atlas"));
        healthBarIntroAnim = new Animation<>(0.15f, hudAtlas.findRegions("HealthBar"), Animation.PlayMode.NORMAL);
        geoIntroAnim      = new Animation<>(0.15f, hudAtlas.findRegions("Geo"), Animation.PlayMode.NORMAL);
        heartBreakAnim    = new Animation<>(0.06f, hudAtlas.findRegions("BreakHealth"), Animation.PlayMode.NORMAL);
        heartRefillAnim   = new Animation<>(0.06f, hudAtlas.findRegions("HealthRefill"), Animation.PlayMode.NORMAL);
        heartShineAnim    = new Animation<>(0.10f, hudAtlas.findRegions("FilledHealthShine"), Animation.PlayMode.LOOP);
        filledHeartFrame  = hudAtlas.findRegion("FilledHealth");
        emptyHeartFrame   = hudAtlas.findRegion("EmptyHealth");

        // ۲. اطلس SoulTank
        soulAtlas = new TextureAtlas(Gdx.files.internal("HUD/SoulTank.atlas"));
        emptyTankFrame = soulAtlas.findRegion("empty");
        idle33Anim  = new Animation<>(0.15f, soulAtlas.findRegions("33"), Animation.PlayMode.LOOP);
        idle66Anim  = new Animation<>(0.15f, soulAtlas.findRegions("66"), Animation.PlayMode.LOOP);
        idle99Anim  = new Animation<>(0.15f, soulAtlas.findRegions("99"), Animation.PlayMode.LOOP);
        fillTo33Anim = new Animation<>(0.1f, soulAtlas.findRegions("fillto33"), Animation.PlayMode.NORMAL);
        fillTo66Anim = new Animation<>(0.1f, soulAtlas.findRegions("fillto66"), Animation.PlayMode.NORMAL);
        fillTo99Anim = new Animation<>(0.1f, soulAtlas.findRegions("fillto99"), Animation.PlayMode.NORMAL);
    }

    @Override
    public void act(float delta) {
        try {
            super.act(delta);
            hudStateTime += delta;
            shineStateTime += delta;

            if (AppStatus.getGameEngine().getPlayer() == null) return;

            this.maxHP = AppStatus.getGameEngine().getPlayer().getMaxHp();
            this.currentHp = AppStatus.getGameEngine().getPlayer().getHp();
            this.currentGeo = AppStatus.getGameEngine().getPlayer().getGeo();
            this.currentSoul = AppStatus.getGameEngine().getPlayer().getSoul();

            // ===== منطق جدید Soul Tank =====
            int newLevelIndex = getSoulLevelIndex(currentSoul);

            if (currentSoulState == SoulState.FILLING) {
                // اگر در حین fill سطح هدف تغییر کرد، قطع کن و به وضعیت واقعی برو
                if (newLevelIndex != fillTargetIndex) {
                    snapToLevel(newLevelIndex);
                } else {
                    soulAnimTime += delta;
                    Animation<TextureRegion> activeFill = getFillAnimation(previousSoulLevel);
                    if (activeFill != null && activeFill.isAnimationFinished(soulAnimTime)) {
                        // fill تمام شد
                        snapToLevel(fillTargetIndex);
                    }
                }
            } else {
                // حالت عادی (EMPTY, LEVEL33, LEVEL66, LEVEL99)
                if (newLevelIndex > previousLevelIndex) {
                    // افزایش: شروع fill
                    startSoulFill(newLevelIndex);
                } else if (newLevelIndex < previousLevelIndex) {
                    // کاهش: مستقیم snap
                    snapToLevel(newLevelIndex);
                } else {
                    // همان سطح: فقط تایمر idle جلو برود
                    soulAnimTime += delta;
                }
            }
            // =================================

            // مدیریت maxHP و قلب‌ها (بدون تغییر)
            if (maxHP != previousMaxHp) {
                resizeHeartArrays(maxHP);
                previousMaxHp = maxHP;
                previousHp = currentHp;
                isInitialized = true;
            }

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

            if (isInitialized) {
                for (int i = 0; i < maxHP; i++) heartStateTimes[i] += delta;

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
                    if (heartVisualStates[i] == 1 && heartBreakAnim.isAnimationFinished(heartStateTimes[i]))
                        heartVisualStates[i] = 0;
                    if (heartVisualStates[i] == 2 && heartRefillAnim.isAnimationFinished(heartStateTimes[i]))
                        heartVisualStates[i] = 3;
                }
            }

            // اعلان‌ها (بدون تغییر)
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
                if (n.timer <= 0) it.remove();
            }

        } catch (Exception e) { /* نادیده */ }
    }

    // -------------------- Soul Helpers --------------------
    private int getSoulLevelIndex(float soul) {
        if (soul >= 99f) return 3;
        if (soul >= 66f) return 2;
        if (soul >= 33f) return 1;
        return 0;
    }

    private SoulState indexToState(int idx) {
        switch (idx) {
            case 1:  return SoulState.LEVEL33;
            case 2:  return SoulState.LEVEL66;
            case 3:  return SoulState.LEVEL99;
            default: return SoulState.EMPTY;
        }
    }

    private void startSoulFill(int targetIndex) {
        previousSoulLevel = indexToState(previousLevelIndex);
        currentSoulState = SoulState.FILLING;
        soulAnimTime = 0f;
        fillTargetIndex = targetIndex;
        // previousLevelIndex بعداً به‌روز می‌شود
    }

    private void snapToLevel(int newIndex) {
        currentSoulState = indexToState(newIndex);
        previousSoulLevel = currentSoulState;
        previousLevelIndex = newIndex;
        soulAnimTime = 0f;
        fillTargetIndex = newIndex;
    }

    private Animation<TextureRegion> getFillAnimation(SoulState from) {
        switch (from) {
            case EMPTY:   return fillTo33Anim;
            case LEVEL33: return fillTo66Anim;
            case LEVEL66: return fillTo99Anim;
            default:      return null;
        }
    }

    private TextureRegion getCurrentSoulFrame() {
        switch (currentSoulState) {
            case EMPTY:   return emptyTankFrame;
            case LEVEL33: return idle33Anim.getKeyFrame(soulAnimTime, true);
            case LEVEL66: return idle66Anim.getKeyFrame(soulAnimTime, true);
            case LEVEL99: return idle99Anim.getKeyFrame(soulAnimTime, true);
            case FILLING: {
                Animation<TextureRegion> fill = getFillAnimation(previousSoulLevel);
                return (fill != null) ? fill.getKeyFrame(soulAnimTime, false) : emptyTankFrame;
            }
        }
        return emptyTankFrame;
    }
    // -------------------------------------------------------

    private void resizeHeartArrays(int newSize) {
        if (newSize <= 0) { isInitialized = false; return; }
        float[] newTimes = new float[newSize];
        int[] newStates = new int[newSize];
        for (int i = 0; i < newSize; i++) {
            newStates[i] = (i < currentHp) ? 3 : 0;
            newTimes[i] = 0f;
        }
        heartStateTimes = newTimes;
        heartVisualStates = newStates;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        float yOffset = -60f;

        // ۱. نوار سلامت
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
        batch.draw(currentGeoIcon, 32, 1204 + yOffset);
        font.setColor(Color.WHITE);
        font.draw(batch, "" + currentGeo, 130, 1255 + yOffset);

        // ۴. Soul Tank با قابلیت تنظیم موقعیت و اندازه
        TextureRegion soulFrame = getCurrentSoulFrame();
        if (soulFrame != null) {
            float tankW = SOUL_TANK_SIZE * SOUL_TANK_SCALE;
            float tankH = SOUL_TANK_SIZE * SOUL_TANK_SCALE;
            float tankX = SOUL_TANK_X;
            float tankY = SOUL_TANK_Y_OFFSET + yOffset;
            batch.draw(soulFrame, tankX, tankY, tankW, tankH);
        }

        // اگر دیباگ فعال باشد، عدد روح را نیز نمایش بده
        if (AppStatus.DEBUG) {
            font.setColor(Color.WHITE);
            font.draw(batch, "SOUL: " + (int)currentSoul + "%", 150, 1215 + yOffset);
        }

        // ۵. اعلان‌های دستاورد
        float soulTextY = 1170 + yOffset;
        float lineHeight = 85f;
        float notificationBaseY = soulTextY - lineHeight;
        int count = notifications.size();
        for (int i = 0; i < count; i++) {
            Notification n = notifications.get(i);
            float y = notificationBaseY - i * lineHeight;
            float alpha = (n.timer > FADE_DURATION) ? 1f : n.timer / FADE_DURATION;
            font.setColor(1f, 1f, 1f, alpha);
            font.draw(batch, "Achievement Unlocked: " + n.name, 60, y);
        }
        font.setColor(Color.WHITE);
    }

//    @Override
//    public void dispose() {
//        super.dispose();
//        if (hudAtlas != null) hudAtlas.dispose();
//        if (soulAtlas != null) soulAtlas.dispose();
//    }
}
