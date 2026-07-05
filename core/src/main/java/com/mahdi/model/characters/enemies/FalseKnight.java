package com.mahdi.model.characters.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mahdi.model.characters.Corpse;
import com.mahdi.model.characters.Enemy;
import com.mahdi.model.characters.Player;
import com.mahdi.model.characters.Projectile;
import com.mahdi.model.map.SolidBlock;
import com.mahdi.model.status.AppStatus;

import java.util.Random;

/**
 * ☀️ باس شوالیه‌ی دروغین (False Knight)
 * پیاده‌سازی FSM دو فازی طبق مستند طراحی:
 * - انتخاب حرکت بر اساس فاصله + رندوم + ضد اسپم
 * - زنجیره‌ی استان در ۵۰٪ جون (DeathFall -> DeathHit -> DeathLand -> Stun -> Recover -> Phase2)
 * - نوار جون تناسبی بالای سر که با تغییر maxHp خراب نمی‌شود
 */
public class FalseKnight extends Enemy {

    private final Player player;
    private static TextureAtlas atlas;
    private final Random random = new Random();

    // ===================== انیمیشن‌ها (طبق اسم دقیق ریجن‌ها در اطلس) =====================
    private final Animation<TextureRegion> animAttackAntic;
    private final Animation<TextureRegion> animAttack;
    private final Animation<TextureRegion> animAttackRecover;
    private final Animation<TextureRegion> animBody;
    private final Animation<TextureRegion> animDeathFall;
    private final Animation<TextureRegion> animDeathHit;
    private final Animation<TextureRegion> animDeathLand;
    private final Animation<TextureRegion> animIdle;
    private final Animation<TextureRegion> animJumpAttackHit;
    private final Animation<TextureRegion> animJumpAttack;
    private final Animation<TextureRegion> animJump;
    private final Animation<TextureRegion> animLand;
    private final Animation<TextureRegion> animRunAntic;
    private final Animation<TextureRegion> animRun;
    private final Animation<TextureRegion> animShockwave;
    private final Animation<TextureRegion> animStunRecover;
    private final Animation<TextureRegion> animTurn;

    // ===================== ماشین وضعیت =====================
    private enum FKState {
        IDLE, TURN,
        ATTACK_ANTIC, ATTACK, ATTACK_RECOVER,
        RUN_ANTIC, RUN,
        LEAP_AIR, LEAP_LAND,           // پرش هجومی / مگا اسلم (فاز۲)
        JUMP_BACK_AIR, JUMP_BACK_LAND, // پرش دفاعی
        DEATH_FALL, DEATH_HIT, DEATH_LAND,
        STUN_BODY, STUN_RECOVER,
        DEAD
    }

    private enum MoveType { SLAM, RUN_CHARGE, LEAP_OFFENSIVE, LEAP_DEFENSIVE, MEGA_SLAM }

    private FKState state = FKState.IDLE;
    private float stateTime = 0f;

    private int facing = 1;
    final float mScale = 2.2f;   // ☀️ فرض: چون باندز بزرگ‌تر شده، اسپرایت هم بزرگ‌تر رسم می‌شود
    private static final float SPRITE_Y_LIFT = 335f; // ☀️ عکس ۲۰۰ پیکسل بالاتر از مرکز باندز چاپ می‌شود

    // ===================== HP / فاز =====================
    private final int maxHp;
    private boolean phaseTwo = false;

    // ===================== استان =====================
    private int stunHitsTaken = 0;
    private static final int STUN_HITS_REQUIRED = 2;

    // ===================== ری‌اکشن به ضربات پیاپی (پرش دفاعی) =====================
    private int recentHitCount = 0;
    private float recentHitWindow = 0f;
    private static final float RECENT_HIT_WINDOW_DURATION = 3f;
    private static final int RECENT_HIT_THRESHOLD = 2;

    // ===================== مقیاس‌گذاری فاز دوم =====================
    private float animSpeedMult = 1f;   // سرعت پخش انیمیشن‌ها
    private float moveSpeedMult = 1f;   // سرعت حرکت افقی
    private float aiCooldownMult = 1f;  // ضریب کوتاه شدن فاصله تصمیم‌گیری AI

    // ===================== فاصله‌ها برای تصمیم‌گیری (متناسب با باندز ۸۰۰×۶۰۰) =====================
    private static final float CLOSE_RANGE = 600f;
    private static final float MID_RANGE = 1400f;

    // ===================== زمان‌بندی پیش‌فرض (اگر انیمیشن null بود) =====================
    private static final float FALLBACK_ATTACK_ANTIC = 0.5f;
    private static final float FALLBACK_ATTACK = 0.3f;
    private static final float FALLBACK_ATTACK_RECOVER = 0.4f;
    private static final float FALLBACK_RUN_ANTIC = 0.25f;
    private static final float FALLBACK_TURN = 0.2f;
    private static final float FALLBACK_DEATH_FALL = 0.4f;
    private static final float FALLBACK_DEATH_HIT = 0.3f;
    private static final float FALLBACK_DEATH_LAND = 1.0f;
    private static final float FALLBACK_STUN_RECOVER = 0.8f;
    private static final float FALLBACK_LEAP_LAND = 0.3f;
    private static final float FALLBACK_JUMP_BACK_LAND = 0.3f;

    // ===================== سرعت‌ها (چند برابر شده تا با باندز بزرگ‌تر و مسافت پرش بیشتر هم‌خونی داشته باشد) =====================
    private static final float divider = 1.5f;
    private static final float RUN_SPEED = 1150f;
    private static final float LEAP_H_SPEED = 1300f / divider;
    private static final float LEAP_V_SPEED = 2700f / divider;
    private static final float MEGA_SLAM_V_SPEED = 3800f / divider;   // پرش بلندتر عمودی برای مگا اسلم
    private static final float JUMP_BACK_H_SPEED = 1050f / divider;
    private static final float JUMP_BACK_V_SPEED = 2100f / divider;

    // ===================== کول‌داون تصمیم‌گیری =====================
    private static final float BASE_DECISION_COOLDOWN = 0.6f;
    private float decisionCooldown = 0.4f;

    // ===================== ضد اسپم (صف دو حرکت آخر) =====================
    private final MoveType[] lastMoves = new MoveType[2];

    // ===================== دمیج =====================
    private static final int SLAM_DAMAGE = 1;
    private static final int MEGA_SLAM_DAMAGE = SLAM_DAMAGE * 2;
    private static final int LEAP_DAMAGE = 1;
    private static final float SHOCKWAVE_SPEED = 2000f; // ☀️ تقریب سرعت ثابت به‌جای شتاب واقعی (کلاس Projectile سرعت ثابت دارد)
    private static final float SHOCKWAVE_LIFETIME = 4f;   // ☀️ طبق درخواست: ۴ ثانیه عمر
    private static final float HIT_LIFETIME = 0.2f;
    private static final float DEATH_IMPACT_LIFETIME = 1f;

    private boolean isMegaSlamActive = false;
    private boolean impactDamageDealt = false; // جلوگیری از چند بار دمیج زدن در یک حمله

    private static ShapeRenderer debugRenderer;
    private static ShapeRenderer fxRenderer;

    // ===================== نوار جون =====================
    private static final float HP_BAR_WIDTH = 260f;
    private static final float HP_BAR_HEIGHT = 16f;
    private static final float HP_BAR_Y_OFFSET = 50f;

    public FalseKnight(float x, float y, Player player, int maxHp) {
        // ☀️ باندز برخورد: عرض ۸۰۰، ارتفاع ۶۰۰
        super(x, y, 800, 600, RUN_SPEED, 3200f, maxHp);
        this.player = player;
        this.maxHp = maxHp;
        this.hasGravity = true;

        if (atlas == null) {
            // ☀️ مسیر رو مطابق ساختار پروژه‌تون تنظیم کنید
            atlas = new TextureAtlas("bosses/FalseKnight/False_knight.atlas");
        }

        animAttackAntic    = createAnimation("Attack Antic",      0.07f, Animation.PlayMode.NORMAL);
        animAttack         = createAnimation("Attack",            0.08f, Animation.PlayMode.NORMAL);
        animAttackRecover  = createAnimation("Attack Recover_00", 0.08f, Animation.PlayMode.NORMAL);
        animBody           = createAnimation("Body",              0.2f,  Animation.PlayMode.LOOP);
        animDeathFall      = createAnimation("DeathFall_00",      0.12f, Animation.PlayMode.NORMAL);
        animDeathHit       = createAnimation("DeathHit_00",       0.1f,  Animation.PlayMode.NORMAL);
        animDeathLand      = createAnimation("DeathLand_",        0.09f, Animation.PlayMode.NORMAL);
        animIdle           = createAnimation("Idle",              0.15f, Animation.PlayMode.LOOP);
        animJumpAttackHit  = createAnimation("Jump Attack Hit 3", 0.1f,  Animation.PlayMode.NORMAL);
        animJumpAttack     = createAnimation("Jump Attack",       0.07f, Animation.PlayMode.LOOP);
        animJump           = createAnimation("Jump",               0.08f, Animation.PlayMode.LOOP);
        animLand           = createAnimation("Land",               0.08f, Animation.PlayMode.NORMAL);
        animRunAntic       = createAnimation("Run Antic",          0.1f,  Animation.PlayMode.NORMAL);
        animRun            = createAnimation("Run",                0.07f, Animation.PlayMode.LOOP);
        animShockwave      = createAnimation("Shockwave",          0.05f, Animation.PlayMode.NORMAL);
        animStunRecover    = createAnimation("Stun Recover_00",    0.12f, Animation.PlayMode.NORMAL);
        animTurn           = createAnimation("Turn",               0.1f,  Animation.PlayMode.NORMAL);

        if (debugRenderer == null) debugRenderer = new ShapeRenderer();
        if (fxRenderer == null) fxRenderer = new ShapeRenderer();
    }

    private Animation<TextureRegion> createAnimation(String prefix, float dur, Animation.PlayMode mode) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(prefix);
        if (regions.size == 0) {
            System.err.println("WARNING: Animation '" + prefix + "' not found in FalseKnight atlas!");
            return null;
        }
        return new Animation<>(dur, regions, mode);
    }

    // =======================================================================
    // 🌟 حلقه اصلی منطق
    // =======================================================================
    @Override
    protected void updateCustomLogic(float delta) {
        if (state == FKState.DEAD) return;

        stateTime += delta * animSpeedMult;
        updateRecentHitWindow(delta);

        switch (state) {
            case IDLE:              updateIdle(delta); break;
            case TURN:               updateTurn(); break;
            case ATTACK_ANTIC:       updateAttackAntic(); break;
            case ATTACK:             updateAttack(); break;
            case ATTACK_RECOVER:     updateAttackRecover(); break;
            case RUN_ANTIC:          updateRunAntic(); break;
            case RUN:                updateRun(); break;
            case LEAP_AIR:           updateLeapAir(); break;
            case LEAP_LAND:          updateLeapLand(); break;
            case JUMP_BACK_AIR:      updateJumpBackAir(); break;
            case JUMP_BACK_LAND:     updateJumpBackLand(); break;
            case DEATH_FALL:         updateDeathFall(); break;
            case DEATH_HIT:          updateDeathHit(); break;
            case DEATH_LAND:         updateDeathLand(); break;
            case STUN_BODY:          /* منتظر ضربات پلیر، منطق در takeDamage */ break;
            case STUN_RECOVER:       updateStunRecover(); break;
        }
    }

    private void updateRecentHitWindow(float delta) {
        if (recentHitCount > 0) {
            recentHitWindow -= delta;
            if (recentHitWindow <= 0f) {
                recentHitCount = 0;
            }
        }
    }

    // =======================================================================
    // 🌟 IDLE و تصمیم‌گیری AI
    // =======================================================================
    private void updateIdle(float delta) {
        velocity.x = 0;
        isMoving = false;

        // ☀️ اگر پشت‌سرهم ضربه خورده، اول یه پرش دفاعی بزن (ری‌اکشن، مقدم بر انتخاب عادی)
        if (recentHitCount >= RECENT_HIT_THRESHOLD) {
            recentHitCount = 0;
            startLeapDefensive();
            return;
        }

        // ☀️ اگر جهت باس با جهت پلیر همخوانی نداره، اول بچرخ (ترانزیشن تمیز از طریق انیمیشن Turn)
        boolean playerOnRight = player.getPosition().x > position.x;
        if ((playerOnRight && facing < 0) || (!playerOnRight && facing > 0)) {
            startTurn();
            return;
        }

        decisionCooldown -= delta;
        if (decisionCooldown <= 0f) {
            decideNextMove();
        }
    }

    private void decideNextMove() {
        float xDist = Math.abs(player.getPosition().x - position.x);

        float wSlam, wRun, wLeap, wMega = 0f;

        if (xDist < CLOSE_RANGE) {
            wSlam = 70f; wLeap = 15f; wRun = 5f;
            if (phaseTwo) wMega = 35f;
        } else if (xDist < MID_RANGE) {
            wSlam = 20f; wLeap = 45f; wRun = 35f;
            if (phaseTwo) wMega = 15f;
        } else {
            wSlam = 5f; wLeap = 35f; wRun = 60f;
            if (phaseTwo) wMega = 5f;
        }

        // ☀️ فاکتور تصادفی پیوسته
        wSlam = Math.max(0f, wSlam + (random.nextFloat() * 30f - 15f));
        wRun  = Math.max(0f, wRun  + (random.nextFloat() * 30f - 15f));
        wLeap = Math.max(0f, wLeap + (random.nextFloat() * 30f - 15f));
        if (phaseTwo) wMega = Math.max(0f, wMega + (random.nextFloat() * 20f - 10f));

        // ☀️ ضد اسپم: اگر یه حرکت دو بار متوالی تکرار شده، وزنش رو صفر کن
        if (isRepeatedTwice(MoveType.SLAM)) wSlam = 0f;
        if (isRepeatedTwice(MoveType.RUN_CHARGE)) wRun = 0f;
        if (isRepeatedTwice(MoveType.LEAP_OFFENSIVE)) wLeap = 0f;
        if (isRepeatedTwice(MoveType.MEGA_SLAM)) wMega = 0f;

        float total = wSlam + wRun + wLeap + wMega;
        MoveType chosen;
        if (total <= 0.001f) {
            // ☀️ همه صفر شدن (حالت نادر)؛ یه پیش‌فرض امن انتخاب کن
            chosen = MoveType.SLAM;
        } else {
            float r = random.nextFloat() * total;
            if (r < wSlam) {
                chosen = MoveType.SLAM;
            } else if (r < wSlam + wRun) {
                chosen = MoveType.RUN_CHARGE;
            } else if (r < wSlam + wRun + wLeap) {
                chosen = MoveType.LEAP_OFFENSIVE;
            } else {
                chosen = MoveType.MEGA_SLAM;
            }
        }

        pushMoveHistory(chosen);
        executeMove(chosen);
    }

    private boolean isRepeatedTwice(MoveType type) {
        return lastMoves[0] == type && lastMoves[1] == type;
    }

    private void pushMoveHistory(MoveType move) {
        lastMoves[0] = lastMoves[1];
        lastMoves[1] = move;
    }

    private void executeMove(MoveType move) {
        switch (move) {
            case SLAM:            startAttackAntic(); break;
            case RUN_CHARGE:      startRunAntic(); break;
            case LEAP_OFFENSIVE:  startLeapOffensive(); break;
            case LEAP_DEFENSIVE:  startLeapDefensive(); break;
            case MEGA_SLAM:       startMegaSlam(); break;
        }
    }

    private void returnToIdleAndDecideLater() {
        state = FKState.IDLE;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
        decisionCooldown = (BASE_DECISION_COOLDOWN + random.nextFloat() * 0.4f) * aiCooldownMult;
    }

    // =======================================================================
    // 🌟 چرخش (Turn)
    // =======================================================================
    private void startTurn() {
        state = FKState.TURN;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
    }

    private void updateTurn() {
        if (isAnimFinished(animTurn, FALLBACK_TURN)) {
            facing = -facing;
            state = FKState.IDLE;
            stateTime = 0f;
            // ☀️ توجه: decisionCooldown دست‌نخورده می‌مونه تا مستقیم تصمیم‌گیری ادامه پیدا کنه
        }
    }

    // =======================================================================
    // 🌟 کوبیدن گرز (Slam Mace)
    // =======================================================================
    private void startAttackAntic() {
        state = FKState.ATTACK_ANTIC;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
        impactDamageDealt = false;
    }

    private void updateAttackAntic() {
        if (isAnimFinished(animAttackAntic, FALLBACK_ATTACK_ANTIC)) {
            state = FKState.ATTACK;
            stateTime = 0f;
            impactDamageDealt = false;
        }
    }

    private void updateAttack() {
        // ☀️ نقطه‌ی برخورد گرز (تقریباً نیمه‌ی انیمیشن ضربه)
        float impactWindowStart = getAnimDuration(animAttack, FALLBACK_ATTACK) * 0.4f;
        if (!impactDamageDealt && stateTime >= impactWindowStart) {
            spawnMaceHitbox(SLAM_DAMAGE);
            impactDamageDealt = true;
        }
        if (isAnimFinished(animAttack, FALLBACK_ATTACK)) {
            state = FKState.ATTACK_RECOVER;
            stateTime = 0f;
        }
    }

    private void updateAttackRecover() {
        if (isAnimFinished(animAttackRecover, FALLBACK_ATTACK_RECOVER)) {
            returnToIdleAndDecideLater();
        }
    }

    private static final float HITBOX_Y_LIFT = 10f;

    private void spawnMaceHitbox(int damage) {
        float hitW = 320;
        float hitH = 200f;
        float hitX = (facing == 1) ? bounds.x + bounds.width : bounds.x - hitW;
        float hitY = bounds.y + HITBOX_Y_LIFT;
        Rectangle hitBounds = new Rectangle(hitX, hitY, hitW, hitH);

        // ☀️ استفاده از Projectile به عنوان هیت‌باکس ثابت (سرعت صفر) طبق معماری موجود پروژه
        Animation<TextureRegion> visual = (animAttack != null) ? animAttack : animIdle;
        Projectile hit = new Projectile(hitBounds, 0f, 0f, HIT_LIFETIME, visual, true, 0f, 0f);
        AppStatus.getGameEngine().addProjectile(hit);
        // توجه: damage در حال حاضر ثابت (۱ واحد استاندارد) اعمال می‌شود؛
        // اگر Projectile فیلد damage نداشت، این مقدار را در سیستم برخورد مرکزی هندل کنید.
    }

    // =======================================================================
    // 🌟 دویدن / هجوم (Run Charge)
    // =======================================================================
    private void startRunAntic() {
        state = FKState.RUN_ANTIC;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
    }

    private void updateRunAntic() {
        if (isAnimFinished(animRunAntic, FALLBACK_RUN_ANTIC)) {
            state = FKState.RUN;
            stateTime = 0f;
            isMoving = true;
        }
    }

    private void updateRun() {
        velocity.x = facing * RUN_SPEED * moveSpeedMult;
        isMoving = true;

        float xDist = Math.abs(player.getPosition().x - position.x);

        if (isWallAhead()) {
            startTurn();
            return;
        }
        if (!isGroundAhead()) {
            startTurn();
            return;
        }
        // ☀️ وقتی به فاصله‌ی نزدیک رسید، مستقیم زنجیره بشه به کوبیدن گرز (کمبوی طبیعی)
        if (xDist < CLOSE_RANGE) {
            startAttackAntic();
        }
    }

    // =======================================================================
    // 🌟 پرش هجومی / مگا اسلم (Leap Offensive / Mega Slam)
    // =======================================================================
    private void startLeapOffensive() {
        isMegaSlamActive = false;
        state = FKState.LEAP_AIR;
        stateTime = 0f;
        impactDamageDealt = false;

        facing = (player.getPosition().x > position.x) ? 1 : -1;
        velocity.x = facing * LEAP_H_SPEED * moveSpeedMult;
        velocity.y = LEAP_V_SPEED;
        isMoving = true;
    }

    private void startMegaSlam() {
        isMegaSlamActive = true;
        state = FKState.LEAP_AIR;
        stateTime = 0f;
        impactDamageDealt = false;

        facing = (player.getPosition().x > position.x) ? 1 : -1;
        // ☀️ مگا اسلم بیشتر عمودیه؛ افقی فقط یه کورس کوچیک به سمت پلیر
        velocity.x = facing * (LEAP_H_SPEED * 0.4f) * moveSpeedMult;
        velocity.y = MEGA_SLAM_V_SPEED;
        isMoving = true;
    }

    private void updateLeapAir() {
        isMoving = true;
        // ☀️ جاذبه توسط BaseCharacter.update به‌صورت خودکار روی velocity.y اعمال می‌شود
        if (isGrounded() && velocity.y <= 0f) {
            state = FKState.LEAP_LAND;
            stateTime = 0f;
            velocity.x = 0;
            isMoving = false;
            impactDamageDealt = false;
        }
    }

    private void updateLeapLand() {
        if (!impactDamageDealt) {
            if (isMegaSlamActive) {
                spawnMaceHitbox(MEGA_SLAM_DAMAGE);
                spawnShockwaves();
            } else {
                spawnMaceHitbox(LEAP_DAMAGE);
            }
            impactDamageDealt = true;
        }

        Animation<TextureRegion> landAnim = animJumpAttackHit;
        if (isAnimFinished(landAnim, FALLBACK_LEAP_LAND)) {
            isMegaSlamActive = false;
            returnToIdleAndDecideLater();
        }
    }

    private void spawnShockwaves() {
        Animation<TextureRegion> wave = (animShockwave != null) ? animShockwave : animIdle;
        if (wave == null) return;

        // ☀️ عرض/ارتفاع موج دقیقاً از خود فریم آرت گرفته می‌شود، نه یه عدد دلخواه ثابت
        TextureRegion sampleFrame = wave.getKeyFrame(0f);
        float waveW = sampleFrame.getRegionWidth();
        float waveH = sampleFrame.getRegionHeight();
        float waveY = bounds.y + HITBOX_Y_LIFT;

        Rectangle rightBounds = new Rectangle(bounds.x + bounds.width, waveY, waveW, waveH);
        Projectile waveRight = new Projectile(rightBounds, SHOCKWAVE_SPEED, 0f, SHOCKWAVE_LIFETIME, wave, true, 0f, 0f);
        AppStatus.getGameEngine().addProjectile(waveRight);

        Rectangle leftBounds = new Rectangle(bounds.x - waveW, waveY, waveW, waveH);
        Projectile waveLeft = new Projectile(leftBounds, -SHOCKWAVE_SPEED, 0f, SHOCKWAVE_LIFETIME, wave, true, 0f, 0f, true);
        AppStatus.getGameEngine().addProjectile(waveLeft);
    }

    // =======================================================================
    // 🌟 پرش دفاعی (Leap Defensive)
    // =======================================================================
    private void startLeapDefensive() {
        state = FKState.JUMP_BACK_AIR;
        stateTime = 0f;

        // ☀️ جهت مخالف پلیر (عقب‌نشینی)
        int awayDir = (player.getPosition().x > position.x) ? -1 : 1;
        facing = -awayDir; // باس رو به پلیر می‌مونه ولی به عقب می‌پره
        velocity.x = awayDir * JUMP_BACK_H_SPEED * moveSpeedMult;
        velocity.y = JUMP_BACK_V_SPEED;
        isMoving = true;
    }

    private void updateJumpBackAir() {
        isMoving = true;
        if (isGrounded() && velocity.y <= 0f) {
            state = FKState.JUMP_BACK_LAND;
            stateTime = 0f;
            velocity.x = 0;
            isMoving = false;
        }
    }

    private void updateJumpBackLand() {
        if (isAnimFinished(animLand, FALLBACK_JUMP_BACK_LAND)) {
            returnToIdleAndDecideLater();
        }
    }

    // =======================================================================
    // 🌟 مکانیزم استان (Stun Chain) و دریافت دمیج
    // =======================================================================
    @Override
    public void takeDamage(int damage) {
        if (!isAlive) return;

        // ☀️ در حالت استان، ضربات hp را کم نمی‌کنند؛ فقط شمارش می‌شوند
        if (state == FKState.STUN_BODY) {
            registerStunHit();
            return;
        }

        // ☀️ در حین زنجیره‌ی سقوط (DeathFall/Hit/Land) آسیب‌پذیر نیست
        if (state == FKState.DEATH_FALL || state == FKState.DEATH_HIT || state == FKState.DEATH_LAND
            || state == FKState.STUN_RECOVER) {
            return;
        }

        hp -= damage;

        // ☀️ ردیابی ضربات پیاپی برای تحریک پرش دفاعی
        recentHitCount++;
        recentHitWindow = RECENT_HIT_WINDOW_DURATION;

        if (hp <= 0) {
            AppStatus.getGameEngine().enemyIsDead(this);
            return;
        }

        // ☀️ رسیدن به ۵۰٪ فقط یک‌بار (در فاز اول) باعث شروع زنجیره‌ی استان می‌شود
        if (!phaseTwo && hp <= maxHp / 2) {
            startCollapseSequence();
        }
    }

    private void startCollapseSequence() {
        state = FKState.DEATH_FALL;
        stateTime = 0f;
        velocity.x = 0;
        velocity.y = 0;
        isMoving = false;
    }

    private void updateDeathFall() {
        if (isAnimFinished(animDeathFall, FALLBACK_DEATH_FALL)) {
            state = FKState.DEATH_HIT;
            stateTime = 0f;
        }
    }

    private void updateDeathHit() {
        if (isAnimFinished(animDeathHit, FALLBACK_DEATH_HIT)) {
            spawnDeathImpactEffect(); // ☀️ لحظه‌ای که ضربه کاملاً به زمین خورده: پرتابه‌ی ثابت جلوی باس
            state = FKState.DEATH_LAND;
            stateTime = 0f;
        }
    }

    /**
     * ☀️ وقتی انیمیشن DeathHit کامل تموم شد (یعنی زره واقعاً به زمین خورده)،
     * یه پرتابه‌ی ثابت (بدون حرکت) جلوی باس روی زمین ظاهر می‌شود.
     */
    private void spawnDeathImpactEffect() {
        Animation<TextureRegion> visual = (animDeathHit != null) ? animDeathHit : animIdle;
        if (visual == null) return;

        TextureRegion sampleFrame = visual.getKeyFrame(0f);
        float impactW = sampleFrame.getRegionWidth();
        float impactH = sampleFrame.getRegionHeight();

        float impactX = (facing == 1) ? bounds.x + bounds.width : bounds.x - impactW;
        float impactY = bounds.y + HITBOX_Y_LIFT;
        Rectangle impactBounds = new Rectangle(impactX, impactY, impactW, impactH);

//        Projectile impact = new Projectile(impactBounds, 0f, 0f, DEATH_IMPACT_LIFETIME, visual, true, 0f, 0f);
//        AppStatus.getGameEngine().addProjectile(impact);
    }

    private void updateDeathLand() {
        if (isAnimFinished(animDeathLand, FALLBACK_DEATH_LAND)) {
            state = FKState.STUN_BODY;
            stateTime = 0f;
            stunHitsTaken = 0;
        }
    }

    private void registerStunHit() {
        stunHitsTaken++;
        if (stunHitsTaken >= STUN_HITS_REQUIRED) {
            state = FKState.STUN_RECOVER;
            stateTime = 0f;
        }
    }

    private void updateStunRecover() {
        if (isAnimFinished(animStunRecover, FALLBACK_STUN_RECOVER)) {
            beginPhaseTwo();
        }
    }

    private void beginPhaseTwo() {
        phaseTwo = true;
        animSpeedMult = 1.4f;
        moveSpeedMult = 1.35f;
        aiCooldownMult = 0.6f;

        lastMoves[0] = null;
        lastMoves[1] = null;

        returnToIdleAndDecideLater();
    }

    // =======================================================================
    // 🌟 سنسورهای محیطی (دیوار / پرتگاه)
    // =======================================================================
    private boolean isWallAhead() {
        float sensorX = (facing == 1) ? bounds.x + bounds.width : bounds.x - 2f;
        Rectangle sensor = new Rectangle(sensorX, bounds.y + 2f, 2f, bounds.height - 4f);
        Array<SolidBlock> blocks = AppStatus.getGameEngine().getSolidBlocks();
        for (SolidBlock b : blocks) {
            if (!b.isDeadly && "wall".equals(b.type) && sensor.overlaps(b.bounds)) {
                return true;
            }
        }
        return false;
    }

    private boolean isGroundAhead() {
        float sensorX = (facing == 1) ? bounds.x + bounds.width : bounds.x - 4f;
        Rectangle ledgeSensor = new Rectangle(sensorX, bounds.y - 4f, 4f, 4f);
        Array<SolidBlock> blocks = AppStatus.getGameEngine().getSolidBlocks();
        for (SolidBlock b : blocks) {
            if (!b.isDeadly && ledgeSensor.overlaps(b.bounds)) {
                return true;
            }
        }
        return false;
    }

    // =======================================================================
    // 🌟 توابع کمکی انیمیشن
    // =======================================================================
    private boolean isAnimFinished(Animation<TextureRegion> anim, float fallbackDuration) {
        if (anim == null) return stateTime >= fallbackDuration;
        return anim.isAnimationFinished(stateTime);
    }

    private float getAnimDuration(Animation<TextureRegion> anim, float fallbackDuration) {
        if (anim == null) return fallbackDuration;
        return anim.getAnimationDuration();
    }

    // =======================================================================
    // 🌟 مرگ نهایی و جسد
    // =======================================================================
    @Override
    public void die() {
        if (!isAlive) return;
        super.die();
        state = FKState.DEAD;
    }

    @Override
    public Corpse getCorpse() {
        float scale = (facing == 1) ? -1f : 1f;
        return new Corpse(new Rectangle(bounds),
            new Vector2(velocity),
            animDeathLand,
            scale * mScale,
            0f, -10f);
    }

    // =======================================================================
    // 🌟 رسم
    // =======================================================================
    @Override
    public void draw(Batch batch) {
        if (state == FKState.DEAD) return;

        Animation<TextureRegion> anim = null;
        switch (state) {
            case IDLE:            anim = animIdle; break;
            case TURN:            anim = animTurn; break;
            case ATTACK_ANTIC:    anim = animAttackAntic; break;
            case ATTACK:          anim = animAttack; break;
            case ATTACK_RECOVER:  anim = animAttackRecover; break;
            case RUN_ANTIC:       anim = animRunAntic; break;
            case RUN:             anim = animRun; break;
            case LEAP_AIR:        anim = animJumpAttack; break;
            case LEAP_LAND:       anim = animJumpAttackHit; break;
            case JUMP_BACK_AIR:   anim = animJump; break;
            case JUMP_BACK_LAND:  anim = animLand; break;
            case DEATH_FALL:      anim = animDeathFall; break;
            case DEATH_HIT:       anim = animDeathHit; break;
            case DEATH_LAND:      anim = animDeathLand; break;
            case STUN_BODY:       anim = animBody; break;
            case STUN_RECOVER:    anim = animStunRecover; break;
        }
        if (anim == null) anim = animIdle;
        if (anim == null) return;

        TextureRegion frame = anim.getKeyFrame(stateTime, false);
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();

        float finalW = w * mScale;
        float finalH = h * mScale;

        // ☀️ سنتر کامل اسپرایت داخل باندز ۸۰۰×۶۰۰، بعلاوه‌ی ۲۰۰ پیکسل بالاتر طبق درخواست
        float drawX = bounds.x + (bounds.width - finalW) / 2f;
        float drawY = bounds.y + (bounds.height - finalH) / 2f + SPRITE_Y_LIFT;

        float scaleX = (facing == 1) ? -1 : 1;
        float originX = finalW / 2f;
        float originY = finalH / 2f;

        batch.draw(frame, drawX, drawY, originX, originY, finalW, finalH, scaleX, 1f, 0);

        drawHealthBar(batch);

        if (AppStatus.DEBUG) {
            drawDebug(batch);
        }
    }

    /**
     * ☀️ دیباگ کامل: باندز اصلی، رنج نزدیک/متوسط، سنسورهای دیوار و پرتگاه،
     * پیش‌نمایش هیت‌باکس گرز، و جهت فیس فعلی.
     * توجه: پرتابه‌ها (Projectile) خودشان مستقل و همیشه هیت‌باکس‌شان را رسم می‌کنند
     * (در متد Projectile.draw)، پس اینجا فقط مربوط به خودِ باس است.
     */
    private void drawDebug(Batch batch) {
        batch.end();
        debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);

        // باندز اصلی برخورد باس
        debugRenderer.setColor(Color.RED);
        debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        // مرکز واقعی + محل رسم اسپرایت (برای دیدن افست ۲۰۰ پیکسلی)
        debugRenderer.setColor(Color.MAGENTA);
        float centerX = bounds.x + bounds.width / 2f;
        float centerY = bounds.y + bounds.height / 2f;
        debugRenderer.line(centerX - 20, centerY, centerX + 20, centerY);
        debugRenderer.line(centerX, centerY - 20, centerX, centerY + 20);
        debugRenderer.line(centerX, centerY, centerX, centerY + SPRITE_Y_LIFT);

        // رنج نزدیک (زرد) و متوسط (نارنجی) در جهت فیس فعلی
        float rangeY = bounds.y;
        float rangeH = bounds.height;
        float closeX = (facing == 1) ? bounds.x + bounds.width : bounds.x - CLOSE_RANGE;
        debugRenderer.setColor(Color.YELLOW);
        debugRenderer.rect(closeX, rangeY, CLOSE_RANGE, rangeH);

        float midX = (facing == 1) ? bounds.x + bounds.width + CLOSE_RANGE : bounds.x - MID_RANGE;
        float midW = MID_RANGE - CLOSE_RANGE;
        debugRenderer.setColor(Color.ORANGE);
        debugRenderer.rect(midX, rangeY, midW, rangeH);

        // سنسور دیوار (آبی) و سنسور پرتگاه (فیروزه‌ای)
        float wallSensorX = (facing == 1) ? bounds.x + bounds.width : bounds.x - 2f;
        debugRenderer.setColor(Color.BLUE);
        debugRenderer.rect(wallSensorX, bounds.y + 2f, 2f, bounds.height - 4f);

        float ledgeSensorX = (facing == 1) ? bounds.x + bounds.width : bounds.x - 4f;
        debugRenderer.setColor(Color.CYAN);
        debugRenderer.rect(ledgeSensorX, bounds.y - 4f, 4f, 4f);

        // پیش‌نمایش هیت‌باکس گرز (سبز) — همون محلی که spawnMaceHitbox توش پرتابه می‌سازه
        float hitW = 320f, hitH = 200f;
        float hitX = (facing == 1) ? bounds.x + bounds.width : bounds.x - hitW;
        float hitY = bounds.y + HITBOX_Y_LIFT;
        debugRenderer.setColor(Color.GREEN);
        debugRenderer.rect(hitX, hitY, hitW, hitH);

        // جهت فیس فعلی (خط بلند سفید)
        debugRenderer.setColor(Color.WHITE);
        float facingLineEndX = centerX + facing * 80f;
        debugRenderer.line(centerX, centerY, facingLineEndX, centerY);

        debugRenderer.end();
        batch.begin();
    }

    /**
     * ☀️ نوار جون بالای سر — کاملاً تناسبی.
     * عرض پر شده = HP_BAR_WIDTH * (hp / maxHp)
     * تعداد خط‌های تفکیک هم بر اساس maxHp دوباره محاسبه می‌شود،
     * پس با تغییر maxHp (مثلاً از ۱۲ به ۲۰) هیچ‌چیزی نمی‌شکند یا اسکیل غلط نمی‌گیرد.
     */
    private void drawHealthBar(Batch batch) {
        float barX = bounds.x + bounds.width / 2f - HP_BAR_WIDTH / 2f;
        float barY = bounds.y + bounds.height + HP_BAR_Y_OFFSET;

        float hpRatio = Math.max(0f, Math.min(1f, hp / (float) maxHp));
        float fillWidth = HP_BAR_WIDTH * hpRatio;

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        fxRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        // پس‌زمینه (تیره)
        fxRenderer.begin(ShapeRenderer.ShapeType.Filled);
        fxRenderer.setColor(0.1f, 0.1f, 0.1f, 0.8f);
        fxRenderer.rect(barX, barY, HP_BAR_WIDTH, HP_BAR_HEIGHT);

        // پرشدگی (رنگ بر اساس فاز)
        if (phaseTwo) {
            fxRenderer.setColor(1f, 0.35f, 0.2f, 0.95f); // قرمز/نارنجی برای فاز دوم
        } else {
            fxRenderer.setColor(0.6f, 0.95f, 0.4f, 0.95f); // سبز برای فاز اول
        }
        fxRenderer.rect(barX, barY, fillWidth, HP_BAR_HEIGHT);
        fxRenderer.end();

        // خط دور
        fxRenderer.begin(ShapeRenderer.ShapeType.Line);
        fxRenderer.setColor(Color.WHITE);
        fxRenderer.rect(barX, barY, HP_BAR_WIDTH, HP_BAR_HEIGHT);

        // ☀️ خط‌های تفکیک تناسبی — تعدادشون از maxHp میاد، پس همیشه درست چیده می‌شن
        fxRenderer.setColor(1f, 1f, 1f, 0.5f);
        for (int i = 1; i < maxHp; i++) {
            float segX = barX + (HP_BAR_WIDTH / (float) maxHp) * i;
            fxRenderer.line(segX, barY, segX, barY + HP_BAR_HEIGHT);
        }
        fxRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
    }

    public static void disposeAtlas() {
        if (atlas != null) {
            atlas.dispose();
            atlas = null;
        }
    }
}
