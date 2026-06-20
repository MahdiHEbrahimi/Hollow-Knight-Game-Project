package com.mahdi.model.characters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mahdi.model.enums.GameAction;

public class Player extends BaseCharacter {

    private int hp;
    private int maxHp;
    private int geo; // سکه‌ها
    private float soul; // مقدار روح از 0 تا 100
    private float maxSoul;

    // تعریف دقیق استیت‌های شوالیه
    public enum State { IDLE, WALKING, JUMPING, FALLING, DEAD }
    
    private State currentState;

    private static final float JUMP_FORCE = 850f;
    private static final float JUMP_RELEASE_DAMPING = 0.4f; 

    // رندرر تستی برای کشیدن مستطیل رنگی روی صفحه
    private final ShapeRenderer stateDebugRenderer;

    public Player(float x, float y) {
        // فیکس کردن سرعت مکس روی 350 و شتاب روی 2000 برای تست اولیه حرکت نرم
        super(x, y, 40, 60, 35000f, 2000f); 
        this.currentState = State.IDLE;
        this.stateDebugRenderer = new ShapeRenderer();
    }

    @Override
    protected void updateCustomLogic(float delta) {
        // ۱. مدیریت حرکت افقی بر اساس ورودی‌ها
        int direction = 0;  
        if (GameAction.MOVE_LEFT.isPressed()) {
            direction = -1;
        }
        if (GameAction.MOVE_RIGHT.isPressed()) {
            direction = GameAction.MOVE_LEFT.isPressed() ? 0 : 1;
        }
        
        // فرستادن جهت به متد حرکت کلاس پایه جهت اعمال شتاب
        move(direction);

        // ۲. منطق پرش اولیه
        if (GameAction.JUMP.isJustPressed() && isGrounded) {
            velocity.y = JUMP_FORCE;
            isGrounded = false;
        }

        // ۳. منطق ارتفاع پرش متغیر (Variable Jump Height)
        if (!GameAction.JUMP.isPressed() && velocity.y > 0) {
            velocity.y *= JUMP_RELEASE_DAMPING;
        }

        // ۴. 🌟 ماشین وضعیت اتوماتیک (Automatic State Machine)
        // بر اساس برآیند نیروهای فیزیکی کلاس پایه، استیت گرافیکی بازیکن را مشخص می‌کنیم
        if (!isAlive) {
            currentState = State.DEAD;
        } else if (!isGrounded) {
            // اگر روی زمین نیست، سرعت رو به بالا یعنی در حال پرش، رو به پایین یعنی سقوط
            if (velocity.y > 0) {
                currentState = State.JUMPING;
            } else {
                currentState = State.FALLING;
            }
        } else {
            // اگر روی زمین است، سرعت افقی مخالف صفر یعنی راه رفتن، صفر یعنی ایستاده
            if (velocity.x != 0) {
                currentState = State.WALKING;
            } else {
                currentState = State.IDLE;
            }
        }
    }

    /**
     * پیاده‌سازی متد ابسترکتِ رسم با استفاده از مستطیل‌های رنگی دیباگ متناسب با استیت فعلی
     */
    @Override
    public void draw(Batch batch) {
        // ۱. ابتدا بچ اصلی بازی را موقتاً می‌بندیم تا رندرر اشکال هندسی بتواند کارش را انجام دهد
        batch.end();

        stateDebugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        stateDebugRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // ۲. 🌟 سوییچ کردن رنگ مستطیل بر اساس استیت فیزیکی لحظه‌ای
        switch (currentState) {
            case IDLE:
                stateDebugRenderer.setColor(Color.RED);       // ایستاده = قرمز ثابت
                break;
            case WALKING:
                stateDebugRenderer.setColor(Color.ORANGE);    // در حال دویدن = نارنجی
                break;
            case JUMPING:
                stateDebugRenderer.setColor(Color.YELLOW);    // اوج گرفتن در پرش = زرد
                break;
            case FALLING:
                stateDebugRenderer.setColor(Color.BLUE);      // جاذبه و سقوط رو به پایین = آبی
                break;
            case DEAD:
                stateDebugRenderer.setColor(Color.GRAY);      // مرگ کاراکتر = خاکستری
                break;
        }

        // ۳. رسم مستطیل پر شده بر اساس کادر فیزیکی (bounds) که کلاس پایه جابجایش می‌کند
        stateDebugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        stateDebugRenderer.end();

        // ۴. باز کردن مجدد بچ اصلی بازی برای اینکه سیستم رندر بقیه المان‌ها مختل نشود
        batch.begin();
    }

    public State getCurrentState() {
        return currentState;
    }

    public void dispose() {
        if (stateDebugRenderer != null) {
            stateDebugRenderer.dispose();
        }
    }


    // --- گترها و سترهای تک‌خطی (Getters & Setters) ---
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getGeo() { return geo; }
    public void setGeo(int geo) { this.geo = geo; }

    public float getSoul() { return soul; }
    public void setSoul(float soul) { this.soul = soul; }

    public float getMaxSoul() { return maxSoul; }
    public void setMaxSoul(float maxSoul) { this.maxSoul = maxSoul; }
}