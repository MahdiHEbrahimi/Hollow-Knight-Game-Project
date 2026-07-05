package com.mahdi.model.characters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mahdi.model.status.AppStatus;

public class Corpse extends BaseCharacter {

    private final Animation<TextureRegion> deathAnim;
    private float stateTime;
    private boolean animationFinished;
    private final float flipScale;
    // ☀️ افست‌های دلخواه برای تنظیم موقعیت اسپرایت (هر دشمن مقدار خود را می‌دهد)
    private final float offsetX;
    private final float offsetY;
    private float flipScaleY = 1f;

    /**
     * @param enemyBounds  مستطیل فیزیکی دشمن در لحظهٔ مرگ
     * @param initialVel   سرعت اولیه (مثلاً بردار پرتاب)
     * @param deathAnim    انیمیشن مرگ
     * @param flipScale    ۱ = راست، -۱ = چپ
     * @param offsetX      جابجایی افقی (پیکسل) – مثبت به راست
     * @param offsetY      جابجایی عمودی (پیکسل) – مثبت به بالا
     */

    public Corpse(Rectangle enemyBounds, Vector2 initialVel, Animation<TextureRegion> deathAnim,
                  float flipScale,float flipScaleY, float offsetX, float offsetY){
        this( enemyBounds,  initialVel,  deathAnim,
         flipScale,  offsetX,  offsetY);
        this.flipScaleY = flipScaleY;
    }

    public Corpse(Rectangle enemyBounds, Vector2 initialVel, Animation<TextureRegion> deathAnim,
                  float flipScale, float offsetX, float offsetY) {
        super(enemyBounds.x, enemyBounds.y, enemyBounds.width, enemyBounds.height,
            0f, 0f, 0);
        this.deathAnim = deathAnim;
        this.velocity.set(initialVel);
        this.flipScale = flipScale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.stateTime = 0f;
        this.animationFinished = false;
        this.isAlive = true;
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


    private static ShapeRenderer debugRenderer;

    @Override
    public void draw(Batch batch) {
        TextureRegion frame;
        if (!animationFinished) {
            frame = deathAnim.getKeyFrame(stateTime, false);
        } else {
            frame = deathAnim.getKeyFrame(deathAnim.getAnimationDuration(), false);
        }

        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();

        // ☀️ اسپرایت با کف مستطیل تراز می‌شود (bounds.y) و بعد offsetY اعمال می‌شود
        float drawX = bounds.x + (bounds.width - w) / 2f + offsetX;
        float drawY = bounds.y + offsetY;   // کف مستطیل + افست عمودی

        // ☀️ مبدأ چرخش پایین اسپرایت است تا هنگام flip افقی جابجا نشود
        float originX = w / 2f;
        float originY = 0f;

        batch.draw(frame, drawX, drawY, originX, originY, w, h, flipScale, flipScaleY, 0);
        if (AppStatus.DEBUG){
            batch.end();
            if (debugRenderer == null) {
                debugRenderer = new ShapeRenderer();
            }
            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);
            debugRenderer.setColor(Color.GREEN);
            debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            debugRenderer.end();
            batch.begin();
        }
    }

    public boolean isCompletelyStopped() {
        return animationFinished && Math.abs(velocity.x) < 1f && isGrounded();
    }

    @Override
    public void die() {
        // در Corpse معمولاً die دوباره صدا زده نمی‌شود
    }
}
