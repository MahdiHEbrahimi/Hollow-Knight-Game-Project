package com.mahdi.model.characters;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mahdi.model.characters.enums.State;

public abstract class BaseCharacter {

    // --- وضعیت‌های کاراکتر (State Machine) ---
    protected State currentState;
    protected State previousState;
    protected float stateTime;

    // --- موقعیت و فیزیک ---
    protected Vector2 position;
    protected Vector2 velocity;
    protected Rectangle boundingBox; // مستطیل برخورد برای تشخیص تصادف با دیوار و زمین

    protected float maxSpeed = 400f;
    protected float acceleration = 2000f;
    protected float deceleration = 2500f;
    protected float gravity = -2000f;
    protected float terminalVelocity = 1500f; // سقف سرعت سقوط آزاد

    // --- پرچم‌ها (Flags) ---
    protected boolean isFacingLeft;
    protected boolean isGrounded;
    protected boolean isAlive;

    // --- پرچم‌های درخواست حرکت (جهت جداسازی منطق کیبورد/هوش مصنوعی از فیزیک) ---
    protected boolean moveLeftFlag;
    protected boolean moveRightFlag;

    public BaseCharacter(float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        // ابعاد فرضی مستطیل برخورد (در کلاس‌های فرزند مقدار دقیق‌تر می‌گیرد)
        this.boundingBox = new Rectangle(startX, startY, 50, 80);

        this.currentState = State.IDLE;
        this.previousState = State.IDLE;
        this.stateTime = 0f;
        this.isAlive = true;
        this.isFacingLeft = false;
        this.isGrounded = false;

        loadAnimations(); // صدا زدن متد لود انیمیشن‌ها در لحظه تولد کاراکتر
    }


    protected abstract void loadAnimations();

    // برگرداندن فریم فعلی انیمیشن بر اساس استیت (برای رندر شدن)
    protected abstract TextureRegion getCurrentFrame();

    // ==========================================
    // چرخه حیات (آپدیت و رسم)
    // ==========================================

    public void update(float delta) {
        if (!isAlive) return; // اگر کاراکتر مرده است، این منطق دیگر اجرا نمی‌شود

        stateTime += delta;

        applyPhysics(delta);
        updateBoundingBox();
        updateState();
    }

    private void applyPhysics(float delta) {
        // ۱. اعمال شتاب، اصطکاک و لغزش در راستای افقی
        if (moveRightFlag) {
            velocity.x += acceleration * delta;
            if (velocity.x > maxSpeed) velocity.x = maxSpeed;
            isFacingLeft = false;
        } else if (moveLeftFlag) {
            velocity.x -= acceleration * delta;
            if (velocity.x < -maxSpeed) velocity.x = -maxSpeed;
            isFacingLeft = true;
        } else {
            // اعمال ترمز و اصطکاک وقتی دکمه‌ای فشرده نیست
            if (velocity.x > 0) {
                velocity.x -= deceleration * delta;
                if (velocity.x < 0) velocity.x = 0;
            } else if (velocity.x < 0) {
                velocity.x += deceleration * delta;
                if (velocity.x > 0) velocity.x = 0;
            }
        }

        // ۲. اعمال جاذبه در راستای عمودی
        if (!isGrounded) {
            velocity.y += gravity * delta;
            // جلوگیری از بی‌نهایت شدن سرعت سقوط
            if (velocity.y < -terminalVelocity) {
                velocity.y = -terminalVelocity;
            }
        }

        // ۳. جابجایی نهایی بر اساس سرعت محاسبه‌شده
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // ریست کردن درخواست‌های حرکتی برای فریم بعدی
        moveLeftFlag = false;
        moveRightFlag = false;
    }

    private void updateBoundingBox() {
        // مستطیل برخورد همیشه باید همراه با کاراکتر جابجا شود
        boundingBox.setPosition(position.x, position.y);
    }

    private void updateState() {
        previousState = currentState;

        // منطق تشخیص انیمیشن بر اساس سرعت و وضعیت
        if (velocity.y > 0) {
            currentState = State.JUMPING;
        } else if (velocity.y < 0 && !isGrounded) {
            currentState = State.FALLING;
        } else if (velocity.x != 0) {
            currentState = State.WALKING;
        } else {
            currentState = State.IDLE;
        }

        // صفر کردن تایمر انیمیشن هنگام تغییر وضعیت (تا انیمیشن جدید از فریم اول پخش شود)
        if (currentState != previousState) {
            stateTime = 0f;
        }
    }

    public void draw(Batch batch) {
        TextureRegion frame = getCurrentFrame();
        if (frame != null) {
            // چرخاندن تصویر کاراکتر اگر مسیر حرکتش به سمت چپ باشد
            if (isFacingLeft && !frame.isFlipX()) {
                frame.flip(true, false);
            } else if (!isFacingLeft && frame.isFlipX()) {
                frame.flip(true, false);
            }
            batch.draw(frame, position.x, position.y);
        }
    }

    // ==========================================
    // متدهای کنترلی (برای استفاده توسط کلاس Player یا هوش مصنوعی)
    // ==========================================

    public void moveLeft() { this.moveLeftFlag = true; }
    public void moveRight() { this.moveRightFlag = true; }
    public void setGrounded(boolean grounded) { this.isGrounded = grounded; }

    public void die() { this.isAlive = false; }
    public boolean isAlive() { return isAlive; }
    public Vector2 getPosition() { return position; }
    public Rectangle getBoundingBox() { return boundingBox; }
}
