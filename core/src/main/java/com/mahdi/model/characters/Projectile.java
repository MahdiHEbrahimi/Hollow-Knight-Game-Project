package com.mahdi.model.characters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Projectile {

    private final Animation<TextureRegion> animation;
    private float stateTime;
    private final Vector2 position;         // مرکز پرتابه (برای رسم و جابه‌جایی)
    private final Vector2 velocity;
    private float lifetime;
    private boolean active;
    private final boolean damagesPlayer;    // true = به بازیکن آسیب می‌زند
    private boolean flip = false;

    private final Rectangle bounds;         // مستطیل برخورد (collision box)
    private final float animWidth, animHeight;  // ابعاد واقعی فریم انیمیشن (اگر انیمیشن null باشد، صفر است)
    private final float offsetX, offsetY;       // افست‌های دلخواه برای رسم

    private static ShapeRenderer debugRenderer; // مشترک بین همه پرتابه‌ها

    /**
     * @param bounds        مستطیل برخورد (برای تشخیص برخورد با دیگران)
     * @param vx            سرعت افقی اولیه
     * @param vy            سرعت عمودی اولیه
     * @param lifetime      طول عمر (ثانیه)
     * @param anim          انیمیشن پرتابه — ☀️ می‌تواند null باشد؛ در این صورت هیچ عکسی رسم نمی‌شود
     * @param damagesPlayer اگر true باشد، به بازیکن آسیب می‌زند
     * @param offsetX       جابجایی افقی رسم نسبت به مرکز (پیکسل)
     * @param offsetY       جابجایی عمودی رسم نسبت به مرکز (پیکسل)
     */

    public Projectile(Rectangle bounds, float vx, float vy, float lifetime,
                      Animation<TextureRegion> anim, boolean damagesPlayer,
                      float offsetX, float offsetY, boolean flip) {

        this(bounds, vx, vy, lifetime,
            anim, damagesPlayer,
            offsetX, offsetY);

        this.flip = flip;
    }

    public Projectile(Rectangle bounds, float vx, float vy, float lifetime,
                      Animation<TextureRegion> anim, boolean damagesPlayer,
                      float offsetX, float offsetY) {
        this.animation = anim;
        this.velocity = new Vector2(vx, vy);
        this.lifetime = lifetime;
        this.damagesPlayer = damagesPlayer;
        this.active = true;
        this.stateTime = 0f;

        // کپی از مستطیل برخورد
        this.bounds = new Rectangle(bounds);
        // مرکز پرتابه را در وسط مستطیل قرار می‌دهیم
        this.position = new Vector2(bounds.x + bounds.width / 2f, bounds.y + bounds.height / 2f);

        // ☀️ ابعاد واقعی فریم اول (برای رسم) — فقط اگر انیمیشن داده شده باشد.
        // اگر anim نال باشد، اصلاً به آن دست نمی‌زنیم (نه getKeyFrame، نه هیچ‌چیز دیگر)
        // و ابعاد رو صفر می‌ذاریم؛ چون draw() هم در این حالت هیچی از انیمیشن رسم نمی‌کند.
        if (anim != null) {
            TextureRegion firstFrame = anim.getKeyFrame(0);
            this.animWidth = firstFrame.getRegionWidth();
            this.animHeight = firstFrame.getRegionHeight();
        } else {
            this.animWidth = 0f;
            this.animHeight = 0f;
        }

        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void update(float delta) {
        if (!active) return;
        stateTime += delta;
        lifetime -= delta;
        if (lifetime <= 0f) {
            active = false;
            return;
        }

        // حرکت مرکز
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // به‌روزرسانی مستطیل برخورد (حفظ مرکز)
        bounds.setCenter(position.x, position.y);
    }

    public void draw(Batch batch) {
        if (!active) return;

        // رسم مستطیل دیباگ (بدون تغییر)
        if (debugRenderer == null) {
            debugRenderer = new ShapeRenderer();
        }
        batch.end();
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.CYAN);
        debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        debugRenderer.end();
        batch.begin();

        // ☀️ اگر انیمیشنی داده نشده (null)، دقیقاً طبق درخواست هیچ عکسی رسم نمی‌شود
        if (animation == null) return;

        TextureRegion frame = animation.getKeyFrame(stateTime, false);

        float drawX = position.x - animWidth / 2f + offsetX;
        float drawY = position.y - animHeight / 2f + offsetY;

        // ☀️ اعمال flip افقی در صورت نیاز
        if (flip) {
            float originX = animWidth / 2f;
            float originY = animHeight / 2f;
            batch.draw(frame, drawX, drawY, originX, originY, animWidth, animHeight, -1f, 1f, 0);
        } else {
            batch.draw(frame, drawX, drawY, animWidth, animHeight);
        }

    }

    // --- Getter ها ---
    public boolean isActive() {
        return active;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean damagesPlayer() {
        return damagesPlayer;
    }

    public Vector2 getPosition() {
        return position;
    }

    // پاک‌سازی
    public static void disposeDebugRenderer() {
        if (debugRenderer != null) {
            debugRenderer.dispose();
            debugRenderer = null;
        }
    }
}
