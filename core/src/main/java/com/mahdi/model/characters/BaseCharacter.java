package com.mahdi.model.characters;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mahdi.model.status.AppStatus;

public abstract class BaseCharacter {

    // =======================================================
    // 🌟 ثوابت فیزیکی جهانی و غیرقابل تغییر مپ (Global Constants)
    // =======================================================
    protected static final float GRAVITY = -2800f; // شتاب جاذبه زمین
    protected static final float TERMINAL_VELOCITY_Y = -1000f; // حداکثر سرعت سقوط مجاز
    protected static float FRICTION = 0.85f; // اصطکاک افقی (کاهش سرعت در زمان توقف)

    // ویژگی‌های فیزیکی نمونه
    protected Vector2 position;
    protected Vector2 velocity = new Vector2();
    protected Rectangle bounds;

    private boolean isGrounded;
    protected boolean isAlive;
    protected boolean hasGravity;
    protected boolean isMoving; // 🌟 پرچم نشان‌دهنده اینکه کاراکتر در این فریم قصد حرکت دارد یا نه
    protected float throwTimeRemaining = 0f;
    protected Vector2 throwVelocity;
    protected static final float THROW_SPEED = 1400f;   // سرعت پرتاب (پیکسل در ثانیه)

    // =======================================================
    // 🌟 ویژگی‌های فیزیکی قابل تنظیم از طریق کانتراکتور فرزند
    // =======================================================
    protected final float maxXSpeed; // حداکثر سرعت افقی مجاز این کاراکتر
    protected final float acceleration; // شتاب حرکت افقی این کاراکتر
    protected int hp;

    // جهت حرکت فعلی: 1 = راست، 1- = چپ، 0 = بدون حرکت دستی
    protected float totalTime = 0f;   // زمان سپری‌شده از شروع بازی (یا منطق شخصیت)
    protected float lastAttackTime = -1f;

    public BaseCharacter(float x, float y, float width, float height, float maxXSpeed, float acceleration, int hp) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.bounds = new Rectangle(x, y, width, height);
        this.isAlive = true;
        this.isGrounded = false;
        this.hasGravity = true;
        this.isMoving = false;

        // مقداردهی ویژگی‌های اختصاصی هر کاراکتر از طریق کانتراکتور
        this.maxXSpeed = maxXSpeed;
        this.acceleration = acceleration;
        this.hp = hp;
    }

    /**
     * 🌟 متد عمومی حرکت برای استفاده کیبورد پلیر یا تصمیم‌گیری هوش مصنوعی (AI)
     *
     * @param direction : 1 برای راست، 1- برای چپ، 0 برای ایستادن
     */
    public void move(int direction, float delta) {
        velocity.x += direction * acceleration * delta;
        this.isMoving = (direction != 0);
    }

    public void move(int direction, float delta, float c) {
        if (c > 1f)
            c = 1f;
        if (c < 0)
            c = 0;

        velocity.x += direction * acceleration * delta * c;
        this.isMoving = (direction != 0);
    }

    /**
     * چرخه فیزیک اصلی موتور بازی
     */
    public final void update(float delta) {
        if (!isAlive)
            return;

        updateThrown(delta);
        totalTime += delta;
        // ۱. اعمال جاذبه و مهار آن توسط سرعت حد عمودی
        if (hasGravity && !isGrounded) {
            velocity.y += GRAVITY * delta;
            if (velocity.y < TERMINAL_VELOCITY_Y) {
                velocity.y = TERMINAL_VELOCITY_Y;
            }
        }

        // ۲. صدا زدن منطق فرزند (جهت مقداردهی متد move یا پرش)
        updateCustomLogic(delta);

        // ۳. 🌟 اعمال شتاب افقی بر اساس جهتِ دستور داده شده (پاس داده شده به متد move)
        if (!isMoving) {
            // اگر دکمه رها شده یا AI متوقف شده، اصطکاک فعال می‌شود تا سُر خورده و بایستد
            velocity.x *= FRICTION;
            // برای جلوگیری از محاسبات اعشاری بی‌نهایت کوچک نزدیک به صفر
            if (Math.abs(velocity.x) < 1f) {
                velocity.x = 0;
            }
        }

        // ۴. مهار کردن سرعت افقی توسط سرعت حد اختصاصی (maxXSpeed)
        if (velocity.x > maxXSpeed) {
            velocity.x = maxXSpeed;
        } else if (velocity.x < -maxXSpeed) {
            velocity.x = -maxXSpeed;
        }

        // ۵. اعمال نهایی تغییرات روی پوزیشن
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // ۶. تنظیم کادر برخورد بر اساس پوزیشن جدید
        bounds.setPosition(position.x, position.y);
    }

    protected abstract void updateCustomLogic(float delta);

    public abstract void draw(Batch batch);

    public void moveToPos(Vector2 target, float coefficent) {
        Vector2 diff = target.cpy().sub(this.position);   // بردار از خودمان به سمت هدف
        if (coefficent > 1) coefficent = 1;
        moveToDirection(diff, coefficent);
    }

    public void moveToPosNoJump(Vector2 target) {
        Vector2 diff = target.cpy().sub(this.position);
        diff.y = 0f;
        moveToDirection(diff, 1);
    }

    public void moveToDirection(Vector2 diffVector, float coefficent) {
        float ax = Math.abs(diffVector.x);
        float ay = Math.abs(diffVector.y);
        float maxAbs = Math.max(ax, ay);
        if (maxAbs < 0.0001f) return;   // از تقسیم بر صفر جلوگیری
        // نرمال‌سازی: تقسیم همه مؤلفه‌ها بر بزرگترین قدرمطلق
        float normX = diffVector.x / maxAbs;
        float normY = diffVector.y / maxAbs;

        // اعمال شتاب در جهت نرمال‌شده
        velocity.x = normX * maxXSpeed * coefficent;
        velocity.y = normY * maxXSpeed * coefficent;

    }

    abstract void die();

//    public void die() {
//        // todo
//        this.isAlive = false;
//        this.hasGravity = true;
//        this.velocity.x = 0;
//        this.isMoving = false;
//    }

    public void setThrown(Vector2 v) {
        this.throwTimeRemaining = 0.2f;
        throwVelocity = v;
    }

    public void updateThrown(float delta) {
        if (throwTimeRemaining <= 0f || throwVelocity == null) return;

        throwTimeRemaining -= delta;
        float timeDependedMultiplier = throwTimeRemaining * 5;
        // حرکت مستقیم (بدون شتاب / جاذبه) در جهت پرتاب
        position.x += throwVelocity.x * delta * timeDependedMultiplier;
        position.y += throwVelocity.y * delta * timeDependedMultiplier;

        // پس از پایان زمان، velocity اصلی را صفر می‌کنیم (اختیاری)
        if (throwTimeRemaining <= 0f) {
            throwTimeRemaining = 0;
            throwVelocity = null;
        }
    }

    public void dispose() {

    }

    // --- گترها و سترها ---
    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
        if (!hasGravity) grounded = false;
    }

    public int getHp() {
        return hp;
    }

}
