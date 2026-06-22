package com.mahdi.model.game;

import com.badlogic.gdx.math.MathUtils;

public class RisingParticle {
    public float x, y;
    public float speedY;
    public float size;
    public float alpha;
    public boolean alive;

    private float initialSpeedY;
    private float fadeTimer;
    private static final float LIFE_DURATION = 2.5f;  // محو شدن کامل در ۲.۵ ثانیه

    public RisingParticle() {
        alive = false;
    }

    /**
     * فعال‌سازی ذره در نقطه‌ای مشخص (پای بازیکن)
     */
    public void spawn(float baseX, float baseY) {
        this.x = baseX + MathUtils.random(-15f, 15f);   // پخش افقی
        this.y = baseY + MathUtils.random(-4f, 8f);     // کمی بالاتر از زمین
        this.initialSpeedY = MathUtils.random(25f, 55f); // سرعت اولیه صعود (آهسته)
        this.speedY = initialSpeedY;
        this.size = MathUtils.random(12f, 44f);          // اندازه‌های متفاوت
        this.alpha = 1f;
        this.fadeTimer = 0f;
        this.alive = true;
    }

    public void update(float delta) {
        if (!alive) return;

        y += speedY * delta;
        fadeTimer += delta;

        float progress = fadeTimer / LIFE_DURATION;
        if (progress > 1f) progress = 1f;

        // محو شدن تدریجی
        alpha = 1f - progress;

        // کاهش سرعت هرچه بیشتر محو می‌شود (تا نزدیک صفر)
        speedY = MathUtils.lerp(initialSpeedY, initialSpeedY * 0.1f, progress);

        if (progress >= 1f) {
            alive = false;
        }
    }
}
