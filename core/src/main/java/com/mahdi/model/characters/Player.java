package com.mahdi.model.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mahdi.model.enums.GameAction;
import com.mahdi.model.characters.enums.State;
import com.mahdi.model.game.RisingParticle;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.GameScreen;
import com.mahdi.screen.manager.SoundManager;

import java.util.ArrayList;
import java.util.HashMap;

public class Player extends BaseCharacter {

    // ========== ویژگی‌های بازیکن ==========
    private int maxHp = 20;
    private int geo = 20;
    private float soul = 67;
    private float maxSoul = 100;

    private State currentState;
    private State previousState = State.IDLE; // برای تشخیص پایان انیمیشن‌های یک‌باره

    // ========== پارامترهای فیزیکی ==========
    private static final float JUMP_FORCE = 1350f;
    private static final float JUMP_RELEASE_DAMPING = 0.4f;
    private static final float DASH_SPEED = 1400f;
    private static final float DASH_DURATION = 0.3f;

    // ========== انیمیشن ==========
    private static TextureAtlas atlas;
    private HashMap<State, Animation<TextureRegion>> animations;
    private float stateTime = 0f;

    // جهت نگاه (true = راست)
    private boolean facingRight = true;

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
    private float focusActiveTime = 0f;

    // ===================== دش هوایی (سقف تعداد) =====================
    // ☀️ طبق درخواست: بیشتر از این تعداد، بدون برخورد با زمین نمی‌شه دش زد
    private static final int MAX_AIR_DASHES = 2;
    private int airDashCount = 0;

    // ===================== دیوار (پنجه‌ی مانتیس: Wall Slide / Wall Jump) =====================
    private static final float WALL_SLIDE_MAX_FALL_SPEED = -200f; // ☀️ سرعت خیلی کم سر خوردن روی دیوار
    private static final float WALL_JUMP_PUSH_SPEED = 400f;
    private static final float WALL_JUMP_UP_FORCE = JUMP_FORCE * 0.8f;

    // ========== حسگرهای محیطی (توسط GameStatus پر می‌شوند) ==========
    private boolean touchingWallLeft = false;
    private boolean touchingWallRight = false;
    //==== texture for arts
    private static TextureAtlas vfxAtlas;
    private HashMap<String, Animation<TextureRegion>> vfxAnimations;
    private String currentEffect = null;        // افکت در حال پخش
    private float effectTimer = 0f;
    private boolean effectActive = false;
    private boolean effectFlip = false;         // برای افکت‌های ضربه (باید جهت‌دار باشند)
    Music focusSFX = Gdx.audio.newMusic(Gdx.files.internal("SFX/focus_health_charging.wav"));
    Sound dashSound = Gdx.audio.newSound(Gdx.files.internal("SFX/hero_dash.wav"));
    Sound attackSound = Gdx.audio.newSound(Gdx.files.internal("SFX/hero_damage.mp3"));
    Sound healSound = Gdx.audio.newSound(Gdx.files.internal("SFX/focus_health_heal.wav"));

    private Rectangle attackHitbox = null;  // مستطیل هیت‌باکس موقت (برای دیباگ + برخورد)

    // ===================== ابیلیتی‌ها (اسپل‌ها) =====================
    // ☀️ فرض: این دو تا باید به enum GameAction اضافه بشن (مثل ATTACK/DASH/FOCUS)
    //   SPELL_UP      -> انفجار جادویی رو به بالا (۳ ضربه‌ی سریع)
    //   SPELL_FORWARD -> پرتابه‌ی جادویی افقی (پیرسینگ)
    private static final float UPWARD_BLAST_TOTAL_DURATION = 0.6f; // ☀️ با طول انیمیشن Scream هماهنگش کن
    private static final int UPWARD_BLAST_HIT_COUNT = 3;
    private static final float UPWARD_BLAST_PULSE_WIDTH = 0.06f;   // هر پنجره‌ی هیت‌باکس چقدر باز بمونه
    private float abilityElapsed = 0f;
    private int abilityHitsFired = 0;

    private static final float FIREBALL_SPEED = 900f;
    private static final float FIREBALL_LIFETIME = 2.5f;
    private static final float FIREBALL_SPAWN_WINDOW = 0.3f; // ☀️ نسبت به شروع کست؛ با طول Fireball Cast هماهنگش کن
    private boolean fireballSpawned = false;

    public Player(float x, float y) {
        super(x, y, 80, 120, 700f, 2000f, 20);

        this.currentState = State.IDLE;
        this.debugRenderer = new ShapeRenderer();

        if (atlas == null)
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
        loadAnimation(State.FOCUS, "Focus", 0.10f, Animation.PlayMode.LOOP_PINGPONG);
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
            float sizeMultiplier = 2f; // ضریب سایز برای آرت انیمیشن
            float scale = (bounds.height / rawH) * sizeMultiplier;
            renderWidth = rawW * scale;
            renderHeight = rawH * scale;
        } else {
            renderWidth = 80;
            renderHeight = 120;
        }

        if (vfxAtlas == null)
            vfxAtlas = new TextureAtlas("Knight_Animations/vfx.atlas");
        vfxAnimations = new HashMap<>();

        // انفجارها
        loadVfxAnimation("Blast", "Blast", 0.08f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("Dash Effect", "Dash Effect", 0.03f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("SlashEffect", "SlashEffect", 0.04f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("SlashEffectAlt", "SlashEffectAlt", 0.04f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("UpSlashEffect", "UpSlashEffect", 0.04f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("DownSlashEffect", "DownSlashEffect", 0.04f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("LaserCircle", "LaserCircle", 0.08f, Animation.PlayMode.LOOP);  // برای Focus
        loadVfxAnimation("Quake Blast", "Quake Blast", 0.06f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("Shockwave", "Shockwave", 0.06f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("Shockwave Spurt", "Shockwave Spurt", 0.05f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("SoulScream", "SoulScream", 0.07f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("ShadowScream", "ShadowScream", 0.07f, Animation.PlayMode.NORMAL);
        loadVfxAnimation("SoulBall", "SoulBall", 0.2f, Animation.PlayMode.NORMAL);


    }

    private void loadVfxAnimation(String name, String regionPrefix, float frameDuration, Animation.PlayMode playMode) {
        Array<AtlasRegion> regions = vfxAtlas.findRegions(regionPrefix);
        if (regions.size == 0) {
            System.out.println("WARNING: VFX missing for " + regionPrefix);
            return;
        }
        vfxAnimations.put(name, new Animation<>(frameDuration, regions, playMode));
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

    // =======================================================================
    // 🌟 حلقه اصلی منطق
    // =======================================================================
    @Override
    protected void updateCustomLogic(float delta) {
        stateTime += delta;                     // پیشبرد تایمر انیمیشن
        lastTimeKnightGotDamaged += delta;

        handleFixedAnimation();                 // بررسی پایان انیمیشن‌های قفل‌شده

        // ☀️ فوکوس نباید وسط یه دش شروع بشه، وگرنه فیزیک/تایمر دش هنگ می‌کنه
        if (!isDashing && handleFocus(delta)) return;

        int direction = 0;

        // ☀️ موقع فوکوس یا حین یه ابیلیتی (SCREAM/FIREBALL_CAST)، همه‌چیز باید قفل باشه
        boolean inputBlocked = isFocusActive() || isFullyLockedByAbility();

        if (!inputBlocked) {
            boolean dashing = handleDash(delta); // ☀️ حالا برای کل مدت دش true برمی‌گردونه، نه فقط فریم اول
            if (!dashing) {
                if (!handleAbilityInput()) {         // ☀️ جدید: شروع اسپل‌ها
                    if (!handleAttack()) {           // اگر حمله شروع شد، بقیه این فریم اجرا نشه
                        handleJump();                // مدیریت پرش
                        handleVariableJump();        // کاهش سرعت پرش با رها کردن دکمه
                        direction = handleMovementInput(delta);
                    }
                }
            }
        } else if (isDashing) {
            // ☀️ احتیاطی: اگه هم‌زمان قفل (مثلاً فوکوس نگه داشته شده) و در حال دش بودیم،
            // فیزیک/تایمر دش باید همچنان تا آخر پیش بره وگرنه isDashing برای همیشه true می‌مونه
            handleDash(delta);
        }

        boolean isTouchingWall = (touchingWallLeft && direction == -1) || (touchingWallRight && direction == 1);
        boolean isFallingDown = velocity.y < 0 && !isGrounded();

        updateStateMachine(direction, isTouchingWall, isFallingDown); // ماشین حالت اصلی

        updateSmokeParticles(delta);            // به‌روزرسانی و تولید ذرات دود
        updateVFX(delta);                       // به‌روزرسانی افکت‌های بصری
        updateAbilities(delta);                 // ☀️ منطق زمان‌بندی ابیلیتی‌ها (۳ ضربه بالای سر / لحظه‌ی شلیک فایربال)
    }

// ======================= زیرمتدهای خصوصی =========================

    /**
     * بررسی می‌کند که آیا انیمیشن قفل‌شده (یک‌باره) به پایان رسیده یا خیر.
     * در صورت پایان، قفل را برمی‌دارد و رویداد onFixAnimationFinished را صدا می‌زند.
     */
    private void handleFixedAnimation() {
        if (!isFixAnimationActive) return;

        Animation<TextureRegion> anim = animations.get(currentState);
        if (anim != null && anim.isAnimationFinished(stateTime)) {
            isFixAnimationActive = false;
            onFixAnimationFinished();
        }
    }

    /** ☀️ حین کست ابیلیتی‌ها (SCREAM / FIREBALL_CAST)، هیچ ورودی دیگه‌ای پردازش نشه */
    private boolean isFullyLockedByAbility() {
        return isFixAnimationActive && (currentState == State.SCREAM || currentState == State.FIREBALL_CAST);
    }

    /**
     * مدیریت حالت فوکوس (Focus):
     * - اگر روی زمین دکمه فوکوس فشرده شود و در انیمیشن قفل نباشیم، شروع فوکوس.
     * - اگر دکمه رها شود و در یکی از مراحل فوکوس باشیم، پایان فوکوس.
     *
     * @return true اگر یکی از شاخه‌های فوکوس اجرا شد (برای return از متد اصلی)
     */

    private boolean handleFocus(float delta) {
        if (isFocusActive()) {
            this.velocity.x = 0;
            focusActiveTime += delta;
            if (focusActiveTime > 1.5f && soul >= 33 && hp < maxHp) {
                focusActiveTime = 0;
                reduceSoul(33f);
                increaseHP(1);
                SoundManager.getInstance().playSound(healSound);
            }
        } else {
            focusActiveTime = 0;
        }
        if (GameAction.FOCUS.isPressed() && isGrounded() && !isFixAnimationActive) {
            startFocus();
            return true;
        }
        if (!GameAction.FOCUS.isPressed() && (currentState == State.FOCUS_START || currentState == State.FOCUS || currentState == State.FOCUS_GET)) {
            endFocus();
            return true;
        }
        return false;
    }

    /**
     * مدیریت دش (Dash):
     * - اگر دکمه دش تازه فشرده شود (و سقف دش هوایی رد نشده)، startDash را صدا می‌زند.
     * - در تمام مدتی که isDashing فعاله، جابه‌جایی/تایمر را به‌روز می‌کند و true برمی‌گرداند
     *   تا هیچ اکشن دیگری (حمله/پرش/حرکت) وسط دش اجرا نشود.
     *
     * @return true اگر در حال حاضر در حال دش هستیم (برای مسدود کردن بقیه‌ی ورودی‌ها)
     */
    private boolean handleDash(float delta) {
        if (GameAction.DASH.isJustPressed() && !isDashing && canDash()) {
            startDash();
        }

        if (isDashing) {
            this.position.x += ((facingRight ? 1 : -1) * DASH_SPEED) * delta;
            applyNoGravity(delta);
            dashTimer -= delta;
            if (dashTimer <= 0f) {
                isDashing = false;
            }
            return true;
        }
        return false;
    }

    /** ☀️ سقف تعداد دش بدون برخورد با زمین */
    private boolean canDash() {
        return isGrounded() || airDashCount < MAX_AIR_DASHES;
    }

    /**
     * مدیریت حمله (Attack):
     * اگر دکمه حمله تازه فشرده شود، startAttack صدا زده می‌شود.
     *
     * @return true اگر حمله شروع شد (برای return از متد اصلی)
     */
    private boolean handleAttack() {
        if (GameAction.ATTACK.isJustPressed()) {
            // اگر در یک انیمیشن زمینی قفل شده باشیم، حمله را رد می‌کنیم
            if (isFixAnimationActive
                && currentState != State.JUMPING
                && currentState != State.DOUBLE_JUMP
                && currentState != State.FALLING) {
                return false;
            }
            startAttack();
            return true;
        }
        return false;
    }

    /**
     * ☀️ مدیریت ابیلیتی‌ها (اسپل‌ها):
     * - SPELL_UP      -> انفجار جادویی رو به بالا (۳ ضربه)
     * - SPELL_FORWARD -> پرتابه‌ی جادویی افقی
     *
     * @return true اگر یکی از ابیلیتی‌ها شروع شد
     */
    private boolean handleAbilityInput() {
        if (isFixAnimationActive) return false; // نباید یه انیمیشن قفل‌شده‌ی دیگه رو کنسل کنه

        if (GameAction.SPELL_UP.isJustPressed()) {
            startUpwardBlast();
            return true;
        }
        if (GameAction.SPELL_FORWARD.isJustPressed()) {
            startForwardFireball();
            return true;
        }
        return false;
    }

    /**
     * مدیریت پرش (Jump):
     * - پرش از زمین
     * - پرش دوبل در هوا
     * - پرش از دیوار (جهت بر اساس سمتی که به دیوار چسبیده، نه facingRight)
     */
    private void handleJump() {
        if (!GameAction.JUMP.isJustPressed()) return;

        if (isGrounded()) {
            velocity.y = JUMP_FORCE;
            setGrounded(false);
            currentState = State.JUMPING;
            stateTime = 0f;
        } else if (hasDoubleJump && !isGrounded() && currentState != State.WALL_SLIDE) {
            velocity.y = JUMP_FORCE * 1f; // پرش دوم کمی ضعیف‌تر
            hasDoubleJump = false;
            currentState = State.DOUBLE_JUMP;
            stateTime = 0f;
            isFixAnimationActive = true;
        } else if (currentState == State.WALL_SLIDE && !isGrounded()) {
            // ☀️ باگ قبلی: جهت از facingRight می‌اومد؛ الان دقیقاً بر اساس سمت دیواره
            int pushDir = touchingWallLeft ? 1 : (touchingWallRight ? -1 : (facingRight ? -1 : 1));
            velocity.x = pushDir * WALL_JUMP_PUSH_SPEED;
            velocity.y = WALL_JUMP_UP_FORCE;
            facingRight = pushDir > 0; // بعد از پرش، رو به همون طرفی نگاه کنه که پرتاب شده
            setGrounded(false);
            currentState = State.WALL_JUMP;
            stateTime = 0f;
            isFixAnimationActive = true;
        }
    }

    /**
     * پرش متغیر: اگر دکمه پرش رها شود و در حال بالا رفتن باشیم، سرعت عمودی کاهش می‌یابد.
     */
    private void handleVariableJump() {
        if (!GameAction.JUMP.isPressed() && velocity.y > 0 &&
            (currentState == State.JUMPING || currentState == State.DOUBLE_JUMP)) {
            velocity.y *= JUMP_RELEASE_DAMPING;
        }
    }

    /**
     * دریافت ورودی حرکت افقی و اعمال آن از طریق متد move.
     *
     * @return جهت حرکت (1 = راست، -1 = چپ، 0 = ساکن)
     */
    private int handleMovementInput(float delta) {
        int direction = 0;
        if (GameAction.MOVE_LEFT.isPressed()) direction = -1;
        if (GameAction.MOVE_RIGHT.isPressed()) direction = (GameAction.MOVE_LEFT.isPressed() ? 0 : 1);
        move(direction, delta);
        return direction;
    }

    /**
     * ماشین حالت اصلی (State Machine):
     * - بررسی مرگ، هوا، زمین و سرخوردن روی دیوار
     * - به‌روزرسانی جهت و حالت پیشین
     */
    private void updateStateMachine(int direction, boolean isTouchingWall, boolean isFallingDown) {
        if (isFixAnimationActive) return; // در حین انیمیشن قفل، حالت را تغییر نده

        if (!isAlive) {
            currentState = State.DEATH;
            return;
        }

        if (!isGrounded()) {
            // در هوا
            if (isTouchingWall && isFallingDown && !isDashing) {
                currentState = State.WALL_SLIDE;
                velocity.y = Math.max(velocity.y, WALL_SLIDE_MAX_FALL_SPEED); // سر خوردن با سرعت خیلی کم
                hasDoubleJump = true; // شارژ پرش دوبل
            } else if (currentState != State.LANDING) {
                // ☀️ باگ قبلی: اینجا "currentState != State.WALL_SLIDE" هم شرط بود که باعث می‌شد
                // یه‌بار وارد WALL_SLIDE که می‌شدیم، رها کردن دکمه یا برگشتن جهت هیچ‌وقت ازش خارجمون نکنه.
                currentState = (velocity.y > 0) ? State.JUMPING : State.FALLING;
            }
        } else {
            // روی زمین
            hasDoubleJump = true; // شارژ پرش دوبل
            airDashCount = 0;     // ☀️ لمس زمین، شمارنده‌ی دش هوایی رو ریست می‌کنه
            if (previousState == State.FALLING || previousState == State.WALL_SLIDE) {
                currentState = State.LANDING;
                stateTime = 0f;
            } else if (Math.abs(velocity.x) > 0.1f) {
                currentState = State.RUNNING;
            } else {
                currentState = State.IDLE;
            }

            // نگاه به بالا/پایین در حالت ایستاده یا دویدن
            if (currentState == State.IDLE || currentState == State.RUNNING) {
                if (GameAction.MOVE_UP.isPressed()) currentState = State.LOOK_UP;
                else if (GameAction.MOVE_DOWN.isPressed()) currentState = State.LOOK_DOWN;
            }
        }

        // به‌روزرسانی جهت نگاه
        if (velocity.x > 0) facingRight = true;
        else if (velocity.x < 0) facingRight = false;

        previousState = currentState;
    }

    /**
     * به‌روزرسانی ذرات دود و تولید ذرات جدید با فاصله زمانی مشخص.
     */
    private void updateSmokeParticles(float delta) {
        // به‌روزرسانی ذرات موجود
        for (RisingParticle p : smokeParticles) {
            p.update(delta);
        }

        // تولید ذرات جدید در صورت زنده بودن و سپری شدن زمان کافی
        if (!isAlive) return;
        smokeSpawnTimer += delta;
        if (smokeSpawnTimer >= SMOKE_INTERVAL) {
            smokeSpawnTimer -= SMOKE_INTERVAL;
            int spawned = 0;
            for (RisingParticle p : smokeParticles) {
                if (!p.alive) {
                    float randX = bounds.x + MathUtils.random(0f, bounds.width);
                    float randY = bounds.y + MathUtils.random(0f, bounds.height);
                    p.spawn(randX, randY);
                    spawned++;
                    if (spawned >= 3) break;
                }
            }
        }
    }

    /**
     * به‌روزرسانی افکت VFX جاری:
     * - پیشبرد تایمر افکت
     * - در صورت پایان انیمیشن NORMAL، افکت را غیرفعال می‌کند.
     */
    private void updateVFX(float delta) {
        if (!effectActive || currentEffect == null) return;
        Animation<TextureRegion> effectAnim = vfxAnimations.get(currentEffect);
        if (effectAnim != null) {
            effectTimer += delta;
            if (effectAnim.getPlayMode() == Animation.PlayMode.NORMAL && effectAnim.isAnimationFinished(effectTimer)) {
                effectActive = false;
                currentEffect = null;
            }
        }
    }

    /**
     * ☀️ زمان‌بندی داخلیِ ابیلیتی‌ها:
     * - SCREAM: هیت‌باکس رو در ۳ پنجره‌ی کوتاه، پخش‌شده در طول UPWARD_BLAST_TOTAL_DURATION، پالس می‌زنه.
     * - FIREBALL_CAST: دقیقاً یه‌بار، در لحظه‌ی FIREBALL_SPAWN_WINDOW، پرتابه رو می‌سازه.
     */
    private void updateAbilities(float delta) {
        if (currentState == State.SCREAM && isFixAnimationActive) {
            updateUpwardBlastTicks(delta);
        } else if (currentState == State.FIREBALL_CAST && isFixAnimationActive) {
            updateFireballCast();
        }
    }

    private void updateUpwardBlastTicks(float delta) {
        abilityElapsed += delta;

        float tickInterval = UPWARD_BLAST_TOTAL_DURATION / UPWARD_BLAST_HIT_COUNT;
        int shouldHaveFired = Math.min(UPWARD_BLAST_HIT_COUNT, 1 + (int) (abilityElapsed / tickInterval));

        if (shouldHaveFired > abilityHitsFired) {
            abilityHitsFired = shouldHaveFired;
            pulseUpwardBlastHitbox();
        } else if (attackHitbox != null && (abilityElapsed % tickInterval) > UPWARD_BLAST_PULSE_WIDTH) {
            // ☀️ بین دو ضربه، هیت‌باکس خاموش بشه که یه ضربه چند بار حساب نشه
            clearAttackHitBox();
        }
    }

    private void updateFireballCast() {
        if (!fireballSpawned && stateTime >= FIREBALL_SPAWN_WINDOW) {
            spawnFireballProjectile();
            fireballSpawned = true;
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

    private void applyNoGravity(float delta) {
        if (hasGravity && !isGrounded()) {
            velocity.y -= GRAVITY * delta;
            if (velocity.y < TERMINAL_VELOCITY_Y) velocity.y = TERMINAL_VELOCITY_Y;
        }
    }


    private  float lastTimeKnightGotDamaged = 0;

    public void takeDamageFormGround(){
        if (!isAlive || lastTimeKnightGotDamaged < 2.0f) return;
        lastTimeKnightGotDamaged = 0;
        hp--;
        ((GameScreen)AppStatus.getScreen()).activeCameraShake();
        setThrown(new Vector2(0f , 4.5f * THROW_SPEED));
        if (hp <= 0) {
            die();
        }
    }

    public void takeDamage(int damage, Enemy enemy) {
        if (!isAlive || lastTimeKnightGotDamaged < 2.0f) return;
        hp -= damage;
        ((GameScreen)AppStatus.getScreen()).activeCameraShake();
        lastTimeKnightGotDamaged = 0;

        if (enemy != null){
            if (enemy.getPosition().x + enemy.getBounds().width / 2f < this.getPosition().x + this.getBounds().width / 2f) {
                this.setThrown(new Vector2(THROW_SPEED, 0f));
                enemy.setThrown(new Vector2(-THROW_SPEED, 0f));
            } else {
                this.setThrown(new Vector2(-THROW_SPEED, 0f));
                enemy.setThrown(new Vector2(THROW_SPEED, 0f));
            }
        }
        if (hp <= 0) {
            die();
        }
    }

    public boolean isFocusActive() {
        return GameAction.FOCUS.isPressed();
    }

    private void triggerEffect(String effectName, boolean flip) {
        this.currentEffect = effectName;
        this.effectActive = true;
        this.effectTimer = 0f;
        this.effectFlip = flip;
    }

    // ========== رویدادهای شروع/پایان انیمیشن‌های قفل‌شده ==========
    private void startDash() {
        SoundManager.getInstance().playSound(dashSound);
        currentState = State.DASH;
        stateTime = 0f;
        isFixAnimationActive = true;
        isDashing = true;
        dashTimer = DASH_DURATION; // ☀️ باگ قبلی: اینجا 2f (دو ثانیه!) هاردکد شده بود
        velocity.y = 0; // در هنگام دش جاذبه تعلیق می‌شود (اختیاری)
        // سرعت افقی را تنظیم کن
        velocity.x = (facingRight ? 1 : -1) * DASH_SPEED;
        if (!isGrounded()) {
            airDashCount++;
        }
        triggerEffect("Dash Effect", facingRight);
    }


    private boolean lastWasAltSlash = false;

    private void startAttack() {
        SoundManager.getInstance().playSound(attackSound);
        float now = totalTime; // زمان مطلق فعلی
        boolean up = GameAction.MOVE_UP.isPressed();
        boolean down = GameAction.MOVE_DOWN.isPressed();

        if (up) {
            currentState = State.UP_SLASH;
            triggerEffect("UpSlashEffect", !facingRight);
        } else if (down) {
            currentState = State.DOWN_SLASH;
            triggerEffect("DownSlashEffect", !facingRight);
        } else {
            // اگر کمتر از ۱.۵ ثانیه از آخرین حمله گذشته بود، اسلش جایگزین
            if (lastAttackTime >= 0 && (now - lastAttackTime) < 1.5f && !lastWasAltSlash) {
                currentState = State.SLASH_ALT;
                lastWasAltSlash = true;
                triggerEffect("SlashEffectAlt", !facingRight);
            } else {
                lastWasAltSlash = false;
                currentState = State.SLASH;
                triggerEffect("SlashEffect", !facingRight);
            }
        }

        lastAttackTime = now;  // زمان این حمله را به‌خاطر بسپار
        setAttackHitbox(currentState);
        stateTime = 0f;
        isFixAnimationActive = true;
        velocity.x = 0;
    }

    public void attackWasSuccessful(Enemy enemy) {
        switch (currentState) {
            case DOWN_SLASH:
                setThrown(new Vector2(0, 4 * THROW_SPEED));
//                enemy.setThrown(new Vector2(0, -THROW_SPEED));
                break;
            case UP_SLASH:
//                setThrown(new Vector2(0, -THROW_SPEED));
                enemy.setThrown(new Vector2(0, THROW_SPEED));
                break;
            case SLASH:
            case SLASH_ALT:
                if (facingRight) {
                    setThrown(new Vector2(-THROW_SPEED, 0));
                    enemy.setThrown(new Vector2(THROW_SPEED, 0));
                }
                else {
                    setThrown(new Vector2(THROW_SPEED, 0));
                    enemy.setThrown(new Vector2(-THROW_SPEED, 0));
                }
                break;
            case SCREAM:
                // ☀️ ضربه‌ی بالای سر: یه پرتاب کوچیک رو به بالا
                enemy.setThrown(new Vector2(0, THROW_SPEED * 0.5f));
                break;
        }

        enemy.takeDamage(1);
    }

    public void clearAttackHitBox(){
        attackHitbox = null;
    }

    private void setAttackHitbox(State slashState) {
        float hitboxMultiplayer = 1.5f;
        float hitboxWidth = 150f * hitboxMultiplayer;   // طول اسلش
        float hitboxHeight = bounds.height * hitboxMultiplayer; // 120 پیکسل (قد شوالیه)


        switch (slashState) {
            case UP_SLASH:
                // بالای سر، وسط عرض
                attackHitbox = new Rectangle(
                    bounds.x + (bounds.width - hitboxWidth) / 2f,
                    bounds.y + bounds.height,          // از بالای کاراکتر شروع می‌شه
                    hitboxWidth,
                    hitboxHeight
                );
                break;
            case DOWN_SLASH:
                // پایین کاراکتر (زمین)
                attackHitbox = new Rectangle(
                    bounds.x + (bounds.width - hitboxWidth) / 2f,
                    bounds.y - hitboxHeight - 100,           // از پایین کاراکتر به سمت پایین
                    hitboxWidth,
                    hitboxHeight + 100
                );
                break;
            default: // SLASH / SLASH_ALT
                // وسط بدن، برعکس جهت فعلی
                attackHitbox = new Rectangle(
                    !facingRight ? bounds.x - hitboxWidth : bounds.x + bounds.width,
                    bounds.y,                         // هم‌سطح پایین کاراکتر
                    hitboxWidth,
                    hitboxHeight
                );
                break;
        }
    }

    public Rectangle getAttackHitbox() {
        return attackHitbox;
    }

    // =======================================================================
    // 🌟 ابیلیتی ۱: انفجار جادویی رو به بالا (۳ ضربه‌ی سریع)
    // =======================================================================
    private void startUpwardBlast() {
        currentState = State.SCREAM;
        stateTime = 0f;
        isFixAnimationActive = true;
        velocity.x = 0;
        triggerEffect("SoulScream", facingRight);
       ((GameScreen)AppStatus.getScreen()).activeCameraShake();

        abilityElapsed = 0f;
        abilityHitsFired = 0;
        pulseUpwardBlastHitbox(); // اولین ضربه بلافاصله فعال بشه
    }

    /** ☀️ یه پنجره‌ی هیت کوتاه دقیقاً بالای سر بازیکن باز می‌کنه؛ از همون قرارداد attackHitbox/attackWasSuccessful استفاده می‌کنه */
    private void pulseUpwardBlastHitbox() {
        float hitW = bounds.width * 2f;
        float hitH = 220f;
        attackHitbox = new Rectangle(
            bounds.x + (bounds.width - hitW) / 2f,
            bounds.y + bounds.height,
            hitW,
            hitH
        );
    }

    // =======================================================================
    // 🌟 ابیلیتی ۲: پرتابه‌ی جادویی افقی (پیرسینگ)
    // =======================================================================
    private void startForwardFireball() {
        currentState = State.FIREBALL_CAST;
        stateTime = 0f;
        isFixAnimationActive = true;
        velocity.x = 0;
        fireballSpawned = false;
        triggerEffect("Blast", facingRight);
        ((GameScreen)AppStatus.getScreen()).activeCameraShake();
    }

    private void spawnFireballProjectile() {
        Animation<TextureRegion> visual = vfxAnimations.get("SoulBall");
        if (visual == null) return;

        float projW = 70f, projH = 50f;
        float px = facingRight ? (bounds.x + bounds.width) : (bounds.x - projW);
        float py = bounds.y + bounds.height * 0.5f - projH / 2f;
        Rectangle projBounds = new Rectangle(px, py, projW, projH);

        float vx = facingRight ? FIREBALL_SPEED : -FIREBALL_SPEED;

        // ☀️ فرض: damagesPlayer=false یعنی این پرتابه به دشمن‌ها آسیب می‌زنه
        // (متقارن با پرتابه‌های دشمن که damagesPlayer=true هستن). حتماً با GameEngine چک کنید.
        Projectile fireball = new Projectile(projBounds, vx, 0f, FIREBALL_LIFETIME, visual, false, 0f, 0f, !facingRight);
        AppStatus.getGameEngine().addProjectile(fireball);
    }

    private void startFocus() {
        focusSFX.play();
        if (currentState != State.FOCUS_START && currentState != State.FOCUS) {
            currentState = State.FOCUS_START;
            stateTime = 0f;
            this.velocity.x = 0f;
            isFixAnimationActive = true;
            triggerEffect("LaserCircle", facingRight); // افکت دایره‌ای دور شوالیه
        }
    }

    private void endFocus() {
        if (currentState == State.FOCUS || currentState == State.FOCUS_START) {
            focusSFX.stop();
            currentState = State.FOCUS_END;
            stateTime = 0f;
            isFixAnimationActive = true;
            currentEffect = null;
            effectActive = false;
        }
    }

    /**
     * پس از پایان یک انیمیشن یک‌باره (NORMAL) صدا زده می‌شود.
     */
    private void onFixAnimationFinished() {
        attackHitbox = null;   // هیت‌باکس دیباگ/برخورد حذف بشه
        switch (currentState) {
            case LANDING:
                currentState = State.IDLE;
                break;
            case DASH:
                isDashing = false;
                currentState = isGrounded() ? State.IDLE : State.FALLING;
                break;
            case SLASH:
            case SLASH_ALT:
            case UP_SLASH:
            case DOWN_SLASH:
            case FIREBALL_CAST:
            case SCREAM:
            case DOUBLE_JUMP:
                currentState = isGrounded() ? State.IDLE : State.FALLING;
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

    // درون کلاس Player

    @Override
    public void draw(Batch batch) {
        TextureRegion frame = getCurrentFrame();
        if (frame == null) return;

        drawGlow(batch);
        drawBlackParticles(batch);
        drawVFX(batch);
        drawKnight(batch, frame);
        drawDebug(batch);
    }

    private TextureRegion getCurrentFrame() {
        Animation<TextureRegion> anim = animations.get(currentState);
        if (anim == null) return null;
        return anim.getKeyFrame(stateTime);
    }

    private void drawGlow(Batch batch) {
        float centerX = bounds.x + bounds.width / 2f;
        float centerY = bounds.y + bounds.height / 2f;
        float glowRadius = 500f;
        float glowDiameter = glowRadius * 2f;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        batch.draw(glowTexture, centerX - glowRadius, centerY - glowRadius, glowDiameter, glowDiameter);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void drawBlackParticles(Batch batch) {
        for (RisingParticle p : smokeParticles) {
            if (p.alive) {
                batch.setColor(0f, 0f, 0f, p.alpha);
                float half = p.size / 2f;
                batch.draw(smokeTexture, p.x - half, p.y - half, p.size, p.size);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void drawKnight(Batch batch, TextureRegion frame) {
        float drawX = (bounds.x + bounds.width / 2f) - (renderWidth / 2f);
        float drawY = bounds.y;

        boolean flip = facingRight;
        float scaleX = flip ? -1 : 1;
        float originX = renderWidth / 2f;
        float originY = renderHeight / 2f;

        // ☀️ تنظیم شفافیت در دورهٔ ایمنی (کمتر از ۲ ثانیه)
        float alpha = 1f;
        if (lastTimeKnightGotDamaged < 2.0f) {
            alpha = 0.3f + 0.7f * (float)Math.abs(Math.sin(lastTimeKnightGotDamaged * 12));
        }
        batch.setColor(1f, 1f, 1f, alpha);

        batch.draw(frame, drawX, drawY, originX, originY, renderWidth, renderHeight, scaleX, 1, 0);

        batch.setColor(Color.WHITE);
    }

    private void drawVFX(Batch batch) {
        if (!effectActive || currentEffect == null) return;

        Animation<TextureRegion> effectAnim = vfxAnimations.get(currentEffect);
        if (effectAnim == null) return;

        TextureRegion effectFrame = effectAnim.getKeyFrame(effectTimer, false);
        float w = effectFrame.getRegionWidth();
        float h = effectFrame.getRegionHeight();
        float scaleFactorX = 1.5f;
        float scaleFactorY = 1.5f;

        float offsetX = 0f;
        float offsetY = 0f;

        // تنظیم آفست برای هر افکت خاص
        switch (currentEffect) {
            case "SlashEffect":
                offsetX = 10f * (facingRight ? 1 : -1);
                offsetY = 40f;
                break;
            case "SlashEffectAlt":
                offsetX = 5f * (facingRight ? 1 : -1);
                offsetY = 25f;
                break;
            case "UpSlashEffect":
                offsetY = 55f;
                break;
            case "DownSlashEffect":
                offsetY = -45f;
                break;
            case "Dash Effect":
                offsetX = -230f * (facingRight ? 1 : -1);
                offsetY = 0f;
                break;
            case "LaserCircle":
                offsetX = -50f * (facingRight ? 1 : -1);
                offsetY = 30f;
                scaleFactorY = scaleFactorX = 2.0f;
                break;
            case "SoulScream":
            case "ShadowScream":
                offsetY = bounds.height + 40f; // ☀️ دقیقاً بالای سر
                scaleFactorY = scaleFactorX = 2.0f;
                break;
            case "Blast":
            default:
                offsetX = 120f * (facingRight ? 1 : -1);
                break;
        }

        float drawW = w * scaleFactorX;
        float drawH = h * scaleFactorY;
        float ex = bounds.x + bounds.width / 2f - drawW / 2f + offsetX;
        float ey = bounds.y + bounds.height / 2f - drawH / 2f + offsetY;
        float sX = effectFlip ? 1 : -1;

        batch.draw(effectFrame, ex, ey, drawW / 2f, drawH / 2f, drawW, drawH, sX, 1, 0);
    }

    private void drawDebug(Batch batch) {
        if (!AppStatus.DEBUG)return;
        batch.end();
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);

        debugRenderer.setColor(Color.GREEN);
        debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        if (attackHitbox != null) {
            debugRenderer.setColor(Color.RED);
            debugRenderer.rect(attackHitbox.x, attackHitbox.y, attackHitbox.width, attackHitbox.height);
        }

        debugRenderer.end();
        batch.begin();
    }

    public void die() {
        // todo
        this.isAlive = false;
        this.hasGravity = true;
        this.velocity.x = 0;
        this.isMoving = false;
    }

    // ========== Getter & Setter ==========
    private final float EYE_SIGHT = 450f;

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


    public void increaseHP(int amount) {
        hp += amount;
        if (hp > maxHp) hp = maxHp;
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

    public void increaseGeo(int geo){
        this.geo += geo;
    }

    public float getSoul() {
        return soul;
    }

    public void increaseSoul(float amount) {
        soul += amount;
        if (soul > 99) soul = 99;
    }

    public void reduceSoul(float amount) {
        soul -= amount;
        if (soul < 0) soul = 0;
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
        if (atlas != null) {
            atlas.dispose();
            atlas = null;
        }
        if (vfxAtlas != null) {
            vfxAtlas.dispose();
            vfxAtlas = null;
        }
        if (debugRenderer != null) debugRenderer.dispose();
        if (smokeTexture != null) smokeTexture.dispose();
    }
}
