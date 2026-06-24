package com.mahdi.model.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Corpse extends BaseCharacter {

    private final Animation<TextureRegion> deathAnim;
    private float stateTime;
    private boolean animationFinished;


    /**
     * @param enemyBounds   مستطیل فیزیکی دشمن در لحظهٔ مرگ
     * @param initialVel    سرعت اولیه (مثلاً بردار پرتاب)
     * @param deathAnim     انیمیشن مرگ (PlayMode.NORMAL)
     */
    public Corpse(Rectangle enemyBounds, Vector2 initialVel, Animation<TextureRegion> deathAnim) {
        // ابعاد فیزیکی دقیقاً مثل دشمن
        super(enemyBounds.x, enemyBounds.y,
            enemyBounds.width, enemyBounds.height,
            0f, 0f, 0);  // maxSpeed و شتاب صفر – فیزیک توسط initialVel تأمین می‌شود

        this.deathAnim = deathAnim;
        this.velocity.set(initialVel);   // سرعت پرتاب / باقی‌ماندهٔ دشمن
        this.stateTime = 0f;
        this.animationFinished = false;
        this.isAlive = true;             // فیزیک (جاذبه، برخورد) اجرا شود
        this.hasGravity = true;
        this.isMoving = false;
    }

    @Override
    protected void updateCustomLogic(float delta) {
        stateTime += delta;
        if (deathAnim.isAnimationFinished(stateTime)) {
            animationFinished = true;
        }
    }

    @Override
    public void draw(Batch batch) {
        TextureRegion frame;
        if (!animationFinished) {
            frame = deathAnim.getKeyFrame(stateTime, false);
        } else {
            frame = deathAnim.getKeyFrame(deathAnim.getAnimationDuration(), false);
        }

        // مرکز مستطیل فیزیکی (bounds)
        float centerX = bounds.x + bounds.width / 2f;
        float centerY = bounds.y + bounds.height / 2f;
        // ابعاد واقعی فریم
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        // گوشهٔ چپ-پایین اسپرایت برای آنکه مرکز آن روی مرکز bounds بیفتد
        float drawX = centerX - w / 2f;
        float drawY = centerY - h / 2f;

        batch.draw(frame, drawX, drawY, w, h);
    }

    public boolean isCompletelyStopped() {
        return animationFinished && Math.abs(velocity.x) < 1f && isGrounded;
    }

    @Override
    public void die() {
        //todo
    }
}
