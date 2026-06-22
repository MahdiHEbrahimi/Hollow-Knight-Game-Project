package com.mahdi.model.characters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mahdi.model.enums.GameAction;
import com.mahdi.model.characters.enums.State;
import com.mahdi.model.game.RisingParticle;

import java.util.ArrayList;
import java.util.HashMap;

public class Player extends BaseCharacter {

    // ========== ویژگی‌های بازیکن ==========
    private int hp = 5;
    private int maxHp = 10;
    private int geo = 20;
    private float soul = 54;
    private float maxSoul = 100;

    private State currentState;
    private State previousState = State.IDLE; // برای تشخیص پایان انیمیشن‌های یک‌باره

    // ========== پارامترهای فیزیکی ==========
    private static final float JUMP_FORCE = 1350f;
    private static final float JUMP_RELEASE_DAMPING = 0.4f;
    private static final float DASH_SPEED = 12000f;
    private static final float DASH_DURATION = 0.3f;

    // ========== انیمیشن ==========
    private final TextureAtlas atlas;
    private HashMap<State, Animation<TextureRegion>> animations;
    private float stateTime = 0f;

    // جهت نگاه (true = راست)
    private boolean facingRight = false;

    // ابعاد رسم
    private float renderWidth, renderHeight;
    private final ShapeRenderer debugRenderer;

    // ========== قفل انیمیشن و کنترل‌های موقت ==========
    private boolean isFixAnimationActive = false;
    private float fixStateTimer = 0f;        // برای مدت زمان سپری‌شده در انیمیشن قفل
    private boolean hasDoubleJump = true;    // فعال بودن پرش دوم
    private float lastAttackTime = -1f;      // زمان آخرین حمله برای زنجیره‌ی کمبو
    private float dashTimer = 0f;
    private boolean isDashing = false;

    // ========== حسگرهای محیطی (توسط GameStatus پر می‌شوند) ==========
    private boolean touchingWallLeft = false;
    private boolean touchingWallRight = false;

    public Player(float x, float y) {
        super(x + 500, y, 80, 120, 700f, 2000f);

        this.currentState = State.IDLE;
        this.debugRenderer = new ShapeRenderer();

        // بارگذاری اطلس
        atlas = new TextureAtlas("Knight_Animations/knight.atlas");
        animations = new HashMap<>();

        // ایجاد بافت دود (دایره‌ی محو) – دقیقاً مثل MainMenuScreen
        smokeTexture = createSmokeTexture();
        for (int i = 0; i < MAX_SMOKE; i++) {
            smokeParticles.add(new RisingParticle());
        }
        glowTexture = createGlowTexture();

        // نگاشت انیمیشن‌ها با PlayMode صحیح بر اساس راهنمای انتقال
        loadAnimation(State.IDLE, "Idle", 0.15f, Animation.PlayMode.LOOP);
        loadAnimation(State.RUNNING, "Run", 0.08f, Animation.PlayMode.LOOP);
        loadAnimation(State.SLOW_WALK, "Walk", 0.10f, Animation.PlayMode.LOOP);
        loadAnimation(State.RUN_TO_IDLE, "Run To Idle", 0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.LOOK_UP, "LookUp", 0.10f, Animation.PlayMode.LOOP);
        loadAnimation(State.LOOK_DOWN, "LookDown", 0.10f, Animation.PlayMode.LOOP);
        loadAnimation(State.JUMPING, "Airborne", 0.10f, Animation.PlayMode.NORMAL); // قفل روی آخرین فریم
        loadAnimation(State.DOUBLE_JUMP, "Double Jump", 0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.FALLING, "Fall", 0.10f, Animation.PlayMode.LOOP);
        loadAnimation(State.LANDING, "Landing", 0.05f, Animation.PlayMode.NORMAL);
        loadAnimation(State.SLASH, "Slash", 0.04f, Animation.PlayMode.NORMAL);
        loadAnimation(State.SLASH_ALT, "SlashAlt", 0.04f, Animation.PlayMode.NORMAL);
        loadAnimation(State.UP_SLASH, "UpSlash", 0.04f, Animation.PlayMode.NORMAL);
        loadAnimation(State.DOWN_SLASH, "DownSlash", 0.04f, Animation.PlayMode.NORMAL);
        loadAnimation(State.DASH, "Dash", 0.03f, Animation.PlayMode.NORMAL);
        loadAnimation(State.WALL_SLIDE, "Wall Slide", 0.10f, Animation.PlayMode.LOOP);
        loadAnimation(State.WALL_JUMP, "Walljump", 0.10f, Animation.PlayMode.NORMAL);
        loadAnimation(State.FOCUS_START, "Focus Start", 0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.FOCUS, "Focus", 0.10f, Animation.PlayMode.LOOP);
        loadAnimation(State.FOCUS_GET, "Focus Get", 0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.FOCUS_END, "Focus End", 0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.FIREBALL_CAST, "Fireball Cast", 0.08f, Animation.PlayMode.NORMAL);
        loadAnimation(State.HURT, "Idle Hurt", 0.08f, Animation.PlayMode.LOOP); // در هنگام آسیب لوپ می‌شود
        loadAnimation(State.SCREAM, "Scream", 0.10f, Animation.PlayMode.NORMAL);
        loadAnimation(State.DEATH, "Death", 0.10f, Animation.PlayMode.NORMAL);

        // محاسبه ابعاد رسم
        Array<AtlasRegion> idleRegions = atlas.findRegions("Idle");
        if (idleRegions.size > 0) {
            float rawW = idleRegions.first().getRegionWidth();
            float rawH = idleRegions.first().getRegionHeight();
            float sizeMultiplier = 1.5f;
            float scale = (bounds.height / rawH) * sizeMultiplier;
            renderWidth = rawW * scale;
            renderHeight = rawH * scale;
        } else {
            renderWidth = 80;
            renderHeight = 120;
        }
    }

    private void loadAnimation(State state, String regionName, float frameDuration, Animation.PlayMode playMode) {
        Array<AtlasRegion> regions = atlas.findRegions(regionName);
        if (regions.size == 0) {
            regions = atlas.findRegions("Idle");
            System.out.println("WARNING: Animation missing for " + regionName + " - using Idle");
        }
        animations.put(state, new Animation<TextureRegion>(frameDuration, regions, playMode));
    }

    // ========== متدهای عمومی برای GameStatus ==========
    public void setTouchingWallLeft(boolean touching) {
        this.touchingWallLeft = touching;
    }

    public void setTouchingWallRight(boolean touching) {
        this.touchingWallRight = touching;
    }

    public boolean isTouchingWallLeft() {
        return touchingWallLeft;
    }

    public boolean isTouchingWallRight() {
        return touchingWallRight;
    }

    @Override
    protected void updateCustomLogic(float delta) {
        stateTime += delta;

        // اگر در انیمیشن قفل‌شده هستیم و تمام نشده، سایر ورودی‌ها را نادیده بگیر
        if (isFixAnimationActive) {
            Animation<TextureRegion> anim = animations.get(currentState);
            if (anim != null && anim.isAnimationFinished(stateTime)) {
                isFixAnimationActive = false;
                onFixAnimationFinished(); // انتقال پس از پایان انیمیشن
            } else {
                // در حین انیمیشن، فقط جاذبه اعمال شود (اگر در هوا)
                applyGravity(delta);
                return;
            }
        }

        // ========== مدیریت Dash (در اولویت بالاتر) ==========
        if (GameAction.DASH.isJustPressed() && !isFixAnimationActive) {
            startDash();
            return;
        }

        // ========== مدیریت Attack ==========
        if (GameAction.ATTACK.isJustPressed() && !isFixAnimationActive) {
            startAttack();
            return;
        }

        // ========== مدیریت Focus ==========
        if (GameAction.FOCUS.isPressed() && isGrounded && !isFixAnimationActive) {
            startFocus();
            return;
        }
        // اگر دکمه Focus رها شده باشد و در حالت Focus هستیم
        if (!GameAction.FOCUS.isPressed() && (currentState == State.FOCUS_START || currentState == State.FOCUS || currentState == State.FOCUS_GET)) {
            endFocus();
            return;
        }

        // ========== مدیریت پرش (روی زمین یا دیوار) ==========
        if (GameAction.JUMP.isJustPressed()) {
            if (isGrounded) {
                velocity.y = JUMP_FORCE;
                isGrounded = false;
                currentState = State.JUMPING;
                stateTime = 0f;
            } else if (hasDoubleJump && !isGrounded && currentState != State.WALL_SLIDE) {
                // پرش دوبل در هوا
                velocity.y = JUMP_FORCE * 1.8f; // کمی ضعیف‌تر
                hasDoubleJump = false;
                currentState = State.DOUBLE_JUMP;
                stateTime = 0f;
                isFixAnimationActive = true;   // ← اضافه شود
            } else if (currentState == State.WALL_SLIDE && !isGrounded) {
                // پرش از دیوار
                float wallJumpX = (facingRight ? -1 : 1) * 400f;
                velocity.x = wallJumpX;
                velocity.y = JUMP_FORCE * 0.8f;
                isGrounded = false;
                currentState = State.WALL_JUMP;
                stateTime = 0f;
                isFixAnimationActive = true;
            }
        }

        // پرش متغیر
        if (!GameAction.JUMP.isPressed() && velocity.y > 0 && (currentState == State.JUMPING || currentState == State.DOUBLE_JUMP)) {
            velocity.y *= JUMP_RELEASE_DAMPING;
        }

        // ورودی حرکت افقی (فقط زمانی که حالت عادی باشد)
        int direction = 0;
        if (GameAction.MOVE_LEFT.isPressed()) direction = -1;
        if (GameAction.MOVE_RIGHT.isPressed()) direction = GameAction.MOVE_LEFT.isPressed() ? 0 : 1;
        move(direction);

        // تشخیص دیوار (برای Wall Slide)
        boolean isTouchingWall = (touchingWallLeft && direction == -1) || (touchingWallRight && direction == 1);
        boolean isFallingDown = velocity.y < 0 && !isGrounded;

        // ماشین حالت اصلی
        if (!isFixAnimationActive) {
            if (!isAlive) {
                currentState = State.DEATH;
            } else if (!isGrounded) {
                if (isTouchingWall && isFallingDown && !GameAction.DASH.isPressed()) {
                    // سر خوردن روی دیوار
                    currentState = State.WALL_SLIDE;
                    velocity.y = Math.max(velocity.y, -200f); // سرعت سقوط کم
                    hasDoubleJump = true; // پرش دوبل هنگام دیوار دوباره شارژ می‌شود
                } else if (currentState != State.WALL_SLIDE && currentState != State.LANDING) {
                    if (velocity.y > 0) {
                        currentState = State.JUMPING;
                    } else {
                        currentState = State.FALLING;
                    }
                }
            } else {
                // روی زمین
                hasDoubleJump = true; // شارژ پرش دوبل
                if (previousState == State.FALLING || previousState == State.WALL_SLIDE) {
                    // فرود آمدن
                    currentState = State.LANDING;
                    stateTime = 0f;
                } else if (Math.abs(velocity.x) > 0.1f) {
                    currentState = State.RUNNING;
                } else {
                    currentState = State.IDLE;
                }

                // نگاه به بالا / پایین (فقط در حالت ایستاده یا دویدن)
                if (currentState == State.IDLE || currentState == State.RUNNING) {
                    if (GameAction.MOVE_UP.isPressed()) currentState = State.LOOK_UP;
                    else if (GameAction.MOVE_DOWN.isPressed()) currentState = State.LOOK_DOWN;
                }
            }

            // به‌روزرسانی جهت
            if (velocity.x > 0) facingRight = true;
            else if (velocity.x < 0) facingRight = false;

            previousState = currentState;
        }
        // به‌روزرسانی همه ذرات دود
        for (RisingParticle p : smokeParticles) {
            p.update(delta);
        }

// تولید ذره جدید فقط وقتی حرکت می‌کند یا در هواست
        if (isAlive) {
            smokeSpawnTimer += delta;
            if (smokeSpawnTimer >= SMOKE_INTERVAL) {
                smokeSpawnTimer -= SMOKE_INTERVAL;
                // تولید ۳ ذره در هر نوبت از نقاط تصادفی بدنه
                int spawned = 0;
                for (RisingParticle p : smokeParticles) {
                    if (!p.alive) {
                        float randX = bounds.x + MathUtils.random(0f, bounds.width);
                        float randY = bounds.y + MathUtils.random(0f, bounds.height);
                        p.spawn(randX, randY);
                        spawned++;
                        if (spawned >= 3) break;  // تعداد ذرات در هر نوبت
                    }
                }
            }
        }
    }


    // داخل کلاس Player
    private final ArrayList<RisingParticle> smokeParticles = new ArrayList<>();
    private Texture smokeTexture;                // بافت دایره محو
    private float smokeSpawnTimer = 0f;
    private static final float SMOKE_INTERVAL = 0.08f;  // هر چند ثانیه یک ذره
    private static final int MAX_SMOKE = 300;

    private Texture createSmokeTexture() {
        int texSize = 64;
        Pixmap pixmap = new Pixmap(texSize, texSize, Pixmap.Format.RGBA8888);
        float center = texSize / 2f;
        float maxRadius = texSize / 2f;

        for (int px = 0; px < texSize; px++) {
            for (int py = 0; py < texSize; py++) {
                float dx = center - px;
                float dy = center - py;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < maxRadius) {
                    float factor = 1f - (dist / maxRadius);
                    factor = factor * factor; // نرمی بیشتر در لبه‌ها
                    pixmap.setColor(1f, 1f, 1f, factor * 0.85f);
                    pixmap.drawPixel(px, py);
                }
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }


    private Texture glowTexture;

    private Texture createGlowTexture() {
        int size = 1024;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float center = size / 2f;
        float maxRadius = size / 2f;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float dx = center - x;
                float dy = center - y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float alpha = 1f - (dist / maxRadius);
                if (alpha < 0) alpha = 0;
                // نرم‌تر شدن محو
                alpha = alpha * alpha * 0.6f; // شدت نور ملایم
                pixmap.setColor(1f, 0.95f, 0.75f, alpha); // سفید گرم
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void applyGravity(float delta) {
        if (hasGravity && !isGrounded) {
            velocity.y += GRAVITY * delta;
            if (velocity.y < TERMINAL_VELOCITY_Y) velocity.y = TERMINAL_VELOCITY_Y;
        }
    }

    // ========== رویدادهای شروع/پایان انیمیشن‌های قفل‌شده ==========
    private void startDash() {
        currentState = State.DASH;
        stateTime = 0f;
        isFixAnimationActive = true;
        isDashing = true;
        dashTimer = 5f;
        velocity.y = 0; // در هنگام دش جاذبه تعلیق می‌شود (اختیاری)
        // سرعت افقی را تنظیم کن
        velocity.x = (facingRight ? 1 : -1) * DASH_SPEED;
    }

    private void startAttack() {
        // زنجیره‌ی کمبو
        if (currentState == State.SLASH && (stateTime < 0.5f || lastAttackTime > 0 && (stateTime - lastAttackTime) < 0.5f)) {
            currentState = State.SLASH_ALT;
        } else {
            currentState = State.SLASH;
        }
        stateTime = 0f;
        isFixAnimationActive = true;
        lastAttackTime = stateTime; // فعلاً صفر می‌شود، بعداً می‌توان timer جداگانه داشت
        velocity.x = 0; // توقف حرکت
    }

    private void startFocus() {
        if (currentState != State.FOCUS_START && currentState != State.FOCUS) {
            currentState = State.FOCUS_START;
            stateTime = 0f;
            isFixAnimationActive = true;
        }
    }

    private void endFocus() {
        if (currentState == State.FOCUS || currentState == State.FOCUS_START) {
            currentState = State.FOCUS_END;
            stateTime = 0f;
            isFixAnimationActive = true;
        }
    }

    /**
     * پس از پایان یک انیمیشن یک‌باره (NORMAL) صدا زده می‌شود.
     */
    private void onFixAnimationFinished() {
        switch (currentState) {
            case LANDING:
                currentState = State.IDLE;
                break;
            case DASH:
                isDashing = false;
                currentState = isGrounded ? State.IDLE : State.FALLING;
                break;
            case SLASH:
            case SLASH_ALT:
            case UP_SLASH:
            case DOWN_SLASH:
            case FIREBALL_CAST:
            case SCREAM:
            case DOUBLE_JUMP:
                currentState = isGrounded ? State.IDLE : State.FALLING;
                break;
            case FOCUS_START:
                currentState = State.FOCUS; // وارد فاز تلقین (لوپ)
                stateTime = 0f; // ریست تایمر برای انیمیشن لوپ
                isFixAnimationActive = true; // همچنان قفل است تا دکمه رها شود
                break;
            case FOCUS_GET:
                currentState = State.FOCUS;
                stateTime = 0f;
                isFixAnimationActive = true;
                break;
            case FOCUS_END:
                currentState = State.IDLE;
                isFixAnimationActive = false;
                break;
            case WALL_JUMP:
                currentState = State.FALLING;
                break;
            default:
                currentState = State.IDLE;
                break;
        }
        stateTime = 0f;
    }

    @Override
    public void draw(Batch batch) {
        Animation<TextureRegion> anim = animations.get(currentState);
        if (anim == null) return;

        TextureRegion frame = anim.getKeyFrame(stateTime);
        float drawX = (bounds.x + bounds.width / 2f) - (renderWidth / 2f);
        float drawY = bounds.y;

        // ⚡ ۱. رسم هاله نورانی (Additive Blending برای درخشش)
        float centerX = bounds.x + bounds.width / 2f;
        float centerY = bounds.y + bounds.height / 2f;
        float glowRadius = 500f;
        float glowDiameter = glowRadius * 2f;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE); // حالت جمع‌شونده
        batch.draw(glowTexture,
            centerX - glowRadius, centerY - glowRadius,
            glowDiameter, glowDiameter);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // بازگشت به حالت عادی

        // ۲. رسم ذرات دود مشکی (با شفافیت)
        for (RisingParticle p : smokeParticles) {
            if (p.alive) {
                batch.setColor(0f, 0f, 0f, p.alpha);  // مشکی
                float half = p.size / 2f;
                batch.draw(smokeTexture, p.x - half, p.y - half, p.size, p.size);
            }
        }
        batch.setColor(Color.WHITE); // برگرداندن رنگ

        // ۳. رسم خود شوالیه با اسپرایت
        boolean flip = facingRight;
        float scaleX = flip ? -1 : 1;
        float originX = renderWidth / 2f;
        float originY = renderHeight / 2f;

        batch.draw(frame,
            drawX, drawY,
            originX, originY,
            renderWidth, renderHeight,
            scaleX, 1, 0);

        // ۴. مستطیل دیباگ (دور کادر فیزیکی)
        batch.end();
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.GREEN);
        debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        debugRenderer.end();
        batch.begin();
    }
    // ========== Getter & Setter ==========
    private final float EYE_SIGHT = 300f;

    public Vector2 getEyeSight() {
        float x = this.position.x;
        float y = this.position.y;
        if (GameAction.MOVE_UP.isPressed()) {
            y += EYE_SIGHT;
        }
        if (GameAction.MOVE_DOWN.isPressed()) {
            y -= EYE_SIGHT;
        }
        return new Vector2(x, y);
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getGeo() {
        return geo;
    }

    public void setGeo(int geo) {
        this.geo = geo;
    }

    public float getSoul() {
        return soul;
    }

    public void setSoul(float soul) {
        this.soul = soul;
    }

    public float getMaxSoul() {
        return maxSoul;
    }

    public void setMaxSoul(float maxSoul) {
        this.maxSoul = maxSoul;
    }

    public State getCurrentState() {
        return currentState;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public boolean isFixAnimationActive() {
        return isFixAnimationActive;
    }

    @Override
    public void dispose() {
        if (glowTexture != null) glowTexture.dispose();
        if (atlas != null) atlas.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
        if (smokeTexture != null) smokeTexture.dispose();
    }
}
