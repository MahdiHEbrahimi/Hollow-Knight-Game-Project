package com.mahdi.model.characters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.mahdi.model.enums.GameAction;
import com.mahdi.model.characters.enums.State;

import java.util.HashMap;

public class Player extends BaseCharacter {

    // ========== ویژگی‌های بازیکن ==========
    private int hp = 5;
    private int maxHp = 10;
    private int geo = 20;
    private float soul = 54;
    private float maxSoul = 100;

    private State currentState;

    // ========== پارامترهای فیزیکی ==========
    private static final float JUMP_FORCE = 1500f;
    private static final float JUMP_RELEASE_DAMPING = 0.4f;

    // ========== انیمیشن ==========
    private final TextureAtlas atlas;
    private HashMap<State, Animation<TextureRegion>> animations;
    private float stateTime = 0f;

    // جهت نگاه (true = راست، پیش‌فرض اسپرایت‌ها رو به چپ است)
    private boolean facingRight = false;

    // ابعاد رسم اسپرایت (مقیاس‌بندی شده بر اساس ارتفاع مستطیل فیزیکی)
    private float renderWidth, renderHeight;

    // رندرر خطی برای نمایش کادر برخورد
    private final ShapeRenderer debugRenderer;

    public Player(float x, float y) {
        // مستطیل فیزیکی: 80x120 (محور برخورد)
        super(x + 500, y, 80, 120, 700f, 2000f);

        this.currentState = State.IDLE;
        this.debugRenderer = new ShapeRenderer();

        // بارگذاری اطلس
        atlas = new TextureAtlas("Knight_Animations/knight.atlas");
        animations = new HashMap<>();

        // ثبت تمام انیمیشن‌ها (کاملاً هماهنگ با تصاویر اطلس)
        loadAnimation(State.IDLE,          "Idle",          0.15f, Animation.PlayMode.LOOP);
        loadAnimation(State.RUNNING,       "Run",           0.08f, Animation.PlayMode.LOOP);
        loadAnimation(State.RUN_TO_IDLE,   "Run To Idle",   0.08f, Animation.PlayMode.NORMAL); // هماهنگ با Run To Idle_000
        loadAnimation(State.LOOK_UP,       "LookUp",        0.1f,  Animation.PlayMode.LOOP);
        loadAnimation(State.LOOK_DOWN,     "LookDown",      0.1f,  Animation.PlayMode.LOOP);
        loadAnimation(State.JUMPING,       "Airborne",      0.1f,  Animation.PlayMode.NORMAL); // هماهنگ با Airborne_000
        loadAnimation(State.FALLING,       "Fall",          0.1f,  Animation.PlayMode.LOOP);   // 🌟 اصلاح شد: هماهنگ با Fall_000
        loadAnimation(State.LANDING,       "Landing",       0.05f, Animation.PlayMode.NORMAL);
        loadAnimation(State.SLASH,         "Slash",         0.04f, Animation.PlayMode.NORMAL);
        loadAnimation(State.UP_SLASH,      "UpSlash",       0.04f, Animation.PlayMode.NORMAL);
        loadAnimation(State.DOWN_SLASH,    "DownSlash",     0.04f, Animation.PlayMode.NORMAL);
        loadAnimation(State.DASH,          "Dash",          0.03f, Animation.PlayMode.NORMAL);
        loadAnimation(State.WALL_SLIDE,    "Wall Slide",    0.1f,  Animation.PlayMode.LOOP);   // هماهنگ با Wall Slide_000
        loadAnimation(State.WALL_JUMP,     "Walljump",      0.1f,  Animation.PlayMode.NORMAL); // هماهنگ با Walljump_000
        loadAnimation(State.FOCUS,         "Focus",         0.1f,  Animation.PlayMode.LOOP);   // ⚠️ حواست باشد فایل‌های Focus چند مدل هستند (Focus_000، Focus Get، Focus Start). این نام فقط سری Focus_000 را لود می‌کند.
        loadAnimation(State.FIREBALL_CAST, "Fireball Cast", 0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.HURT,          "Idle Hurt",     0.08f, Animation.PlayMode.NORMAL); // هماهنگ با Idle Hurt_000
        loadAnimation(State.SCREAM,        "Scream",        0.1f,  Animation.PlayMode.NORMAL);
        loadAnimation(State.DEATH,          "Death",         0.1f,  Animation.PlayMode.NORMAL);
        loadAnimation(State.DOUBLE_JUMP,   "Double Jump",   0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.FOCUS_START,   "Focus Start",   0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.FOCUS_END,     "Focus End",     0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.FOCUS_GET,     "Focus Get",     0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.SLASH_ALT,     "SlashAlt",      0.04f, Animation.PlayMode.NORMAL);
        loadAnimation(State.SLOW_WALK,     "Walk",      0.04f, Animation.PlayMode.NORMAL);


        // محاسبه ابعاد رسم بر اساس فریم Idle و بزرگ‌نمایی دلخواه
        Array<AtlasRegion> idleRegions = atlas.findRegions("Idle");
        if (idleRegions.size > 0) {
            float rawW = idleRegions.first().getRegionWidth();   // 349
            float rawH = idleRegions.first().getRegionHeight();  // 186
            float sizeMultiplier = 1.5f; // مقیاس اضافی برای بزرگ‌تر دیده شدن
            float scale = (bounds.height / rawH) * sizeMultiplier;
            renderWidth = rawW * scale;
            renderHeight = rawH * scale;
        } else {
            renderWidth = 80;
            renderHeight = 120;
        }
    }

    /**
     * یک انیمیشن را از اطلس بارگذاری کرده و در جدول ذخیره می‌کند.
     * اگر منطقه‌ای با نام داده شده یافت نشد، از انیمیشن Idle استفاده می‌شود.
     */
    private void loadAnimation(State state, String regionName, float frameDuration, Animation.PlayMode playMode) {
        Array<AtlasRegion> regions = atlas.findRegions(regionName);
        if (regions.size == 0) {
            regions = atlas.findRegions("Idle");
            System.out.println("WARNING: Animation missing for " + regionName + " - using Idle");
        }
        animations.put(state, new Animation<TextureRegion>(frameDuration, regions, playMode));
    }

    @Override
    protected void updateCustomLogic(float delta) {
        stateTime += delta;

        // ورودی حرکت افقی
        int direction = 0;
        if (GameAction.MOVE_LEFT.isPressed()) {
            direction = -1;
        }
        if (GameAction.MOVE_RIGHT.isPressed()) {
            direction = GameAction.MOVE_LEFT.isPressed() ? 0 : 1;
        }
        move(direction);

        // پرش
        if (GameAction.JUMP.isJustPressed() && isGrounded) {
            velocity.y = JUMP_FORCE;
            isGrounded = false;
        }
        if (!GameAction.JUMP.isPressed() && velocity.y > 0) {
            velocity.y *= JUMP_RELEASE_DAMPING;
        }

        // تعیین جهت نگاه
        if (velocity.x > 0) {
            facingRight = true;
        } else if (velocity.x < 0) {
            facingRight = false;
        }

        // ماشین وضعیت
        if (!isAlive) {
            currentState = State.DEATH;
        } else if (!isGrounded) {
            if (velocity.y > 0) {
                currentState = State.JUMPING;
            } else {
                currentState = State.FALLING;
            }
        } else {
            if (Math.abs(velocity.x) > 0.1f) {
                currentState = State.RUNNING;
            } else {
                currentState = State.IDLE;
            }
        }
    }

    @Override
    public void draw(Batch batch) {
        Animation<TextureRegion> anim = animations.get(currentState);
        if (anim == null) return;

        TextureRegion frame = anim.getKeyFrame(stateTime);

        // اسپرایت به‌گونه‌ای رسم می‌شود که مرکز آن روی مرکز مستطیل فیزیکی قرار گیرد
        // و پایین آن مماس با کف مستطیل (bounds.y) باشد.
        float drawX = (bounds.x + bounds.width / 2f) - (renderWidth / 2f);
        float drawY = bounds.y;

        boolean flip = facingRight;
        float scaleX = flip ? -1 : 1;
        float originX = renderWidth / 2f;
        float originY = renderHeight / 2f;

        batch.draw(frame,
            drawX, drawY,
            originX, originY,
            renderWidth, renderHeight,
            scaleX, 1, 0);

        // مستطیل سبز برای نمایش کادر برخورد
        batch.end();
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.GREEN);
        debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        debugRenderer.end();
        batch.begin();
    }

    // ========== Getter & Setter ==========
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

    public State getCurrentState() { return currentState; }
    public boolean isFacingRight() { return facingRight; }

    @Override
    public void dispose() {
        if (atlas != null) atlas.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
    }
}
