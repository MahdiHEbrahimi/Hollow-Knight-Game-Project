package com.mahdi.model.characters.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
import com.mahdi.model.map.SolidBlock;
import com.mahdi.model.status.AppStatus;

public class HornheadHusk extends Enemy {

    private final Player player;
    private static TextureAtlas atlas;

    // انیمیشن‌ها
    private final Animation<TextureRegion> animIdle;
    private final Animation<TextureRegion> animWalk;
    private final Animation<TextureRegion> animTurn;
    private final Animation<TextureRegion> animAttackAnticipate;
    private final Animation<TextureRegion> animAttackLunge;
    private final Animation<TextureRegion> animAttackCooldown;
    private final Animation<TextureRegion> animDeathLand;

    private enum HuskState {
        PATROL_WALK, PATROL_IDLE, TURN,
        ATTACK_ANTICIPATE, ATTACK_LUNGE, ATTACK_COOLDOWN,
        DEAD
    }
    private HuskState state = HuskState.PATROL_IDLE;
    private float stateTime = 0f;

    // جهت (۱ = راست، -۱ = چپ) – فقط بر اساس حرکت افقی تنظیم می‌شود
    private int facing = 1;
    final float mScale = 1.5f;

    // محدوده‌های گشت‌زنی (بر اساس مسافت)
    private float patrolStartX;
    private static final float PATROL_DISTANCE = 1000;   // مسافت راه رفتن قبل از چرخش
    private static final float IDLE_DURATION = 1.2f;     // زمان استراحت بعد از راه رفتن (ثانیه)

    // سرعت‌ها
    private static final float PATROL_SPEED = 120f;
    private static final float CHARGE_SPEED = 600f;

    // سیستم دید
    private static final float VISION_LENGTH = 700f;
    private static final float MAX_CHARGE_DISTANCE = VISION_LENGTH * 1.5f; // حداکثر مسافت شارژ

    // موقعیت شروع شارژ (برای محدود کردن مسافت)
    private float lungeStartX;

    // دیباگ
    private static ShapeRenderer debugRenderer;

    public HornheadHusk(float x, float y, Player player) {
        super(x, y, 200, 200, CHARGE_SPEED, 1800f, 3);
        this.player = player;
        this.hasGravity = true;

        if (atlas == null) {
            atlas = new TextureAtlas("enemies/Husk_Hornhead/Husk_Hornhead.atlas");
        }

        animIdle            = createAnimation("Idle",                0.15f, Animation.PlayMode.LOOP);
        animWalk            = createAnimation("Walk",                0.1f,  Animation.PlayMode.LOOP);
        animTurn            = createAnimation("Turn",                0.1f,  Animation.PlayMode.NORMAL);
        animAttackAnticipate = createAnimation("Attack Anticipate",  0.08f, Animation.PlayMode.NORMAL);
        animAttackLunge     = createAnimation("Attack Lunge",        0.06f, Animation.PlayMode.LOOP);
        animAttackCooldown  = createAnimation("Attack Cooldown",     0.5f,  Animation.PlayMode.NORMAL);
        animDeathLand       = createAnimation("Death Land",          0.1f,  Animation.PlayMode.NORMAL);

        if (debugRenderer == null) {
            debugRenderer = new ShapeRenderer();
        }
    }

    private Animation<TextureRegion> createAnimation(String prefix, float dur, Animation.PlayMode mode) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(prefix);
        if (regions.size == 0) {
            System.err.println("WARNING: Animation " + prefix + " not found in HornheadHusk atlas!");
            return null;
        }
        return new Animation<>(dur, regions, mode);
    }

    @Override
    protected void updateCustomLogic(float delta) {
        if (state == HuskState.DEAD) return;
        shieldTime += delta;
        stateTime += delta;

        switch (state) {
            case PATROL_WALK:
                updatePatrolWalk();
                break;
            case PATROL_IDLE:
                updatePatrolIdle(delta);
                break;
            case TURN:
                updateTurn();
                break;
            case ATTACK_ANTICIPATE:
                if (animAttackAnticipate != null && animAttackAnticipate.isAnimationFinished(stateTime)) {
                    startLunge();
                }
                break;
            case ATTACK_LUNGE:
                updateLunge();
                break;
            case ATTACK_COOLDOWN:
                if (animAttackCooldown != null && animAttackCooldown.isAnimationFinished(stateTime)) {
                    returnToPatrol();
                }
                break;
        }
    }

    // ===================== گشت‌زنی (راه رفتن) =====================
    private void enterPatrolWalk() {
        state = HuskState.PATROL_WALK;
        stateTime = 0f;
        patrolStartX = position.x;
        // سرعت ثابت برای گشت‌زنی
        velocity.x = facing * PATROL_SPEED;
        isMoving = true;   // اصطکاک غیرفعال
    }

    private void updatePatrolWalk() {
        // حفظ سرعت ثابت (ممکن است برخورد فیزیکی آن را صفر کرده باشد)
        velocity.x = facing * PATROL_SPEED;
        isMoving = true;

        // بررسی مسافت طی‌شده
        float dist = Math.abs(position.x - patrolStartX);
        if (dist >= PATROL_DISTANCE) {
            // چرخش (تغییر جهت)
            startTurn();
            return;
        }

        // بررسی موانع (دیوار / پرتگاه) – با سنسورهای ساده
        if (isWallAhead() || !isGroundAhead()) {
            startTurn();
            return;
        }

        // اگر بازیکن در دید باشد، حمله آغاز شود
        checkVision();
    }

    // ===================== استراحت (Idle) =====================
    private void updatePatrolIdle(float delta) {
        velocity.x = 0;
        isMoving = false;

        idleTimer -= delta;   // باید فیلد idleTimer داشته باشیم. بیایید ساده کنیم:
        // می‌توانیم از stateTime استفاده کنیم (stateTime از ابتدای Idle شمرده می‌شود)
        if (stateTime >= IDLE_DURATION) {
            // بعد از استراحت، بچرخ و دوباره راه برو
            startTurn();
        }

        checkVision();
    }
    // توجه: باید یک فیلد idleTimer یا استفاده از stateTime
    // من stateTime را جایگزین idleTimer می‌کنم.
    private float idleTimer = 0f; // حذف می‌کنیم و از stateTime استفاده می‌کنیم.
    // در ورود به PATROL_IDLE، stateTime = 0f می‌شود. بنابراین شرط stateTime >= IDLE_DURATION کافی است.

    // ===================== چرخش (Turn) =====================
    private void startTurn() {
        state = HuskState.TURN;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
    }

    private void updateTurn() {
        if (animTurn != null && animTurn.isAnimationFinished(stateTime)) {
            facing = -facing;   // جهت را برعکس کن
            enterPatrolWalk();  // دوباره راه برو
        }
    }

    // ===================== حمله =====================
    private void startAnticipate() {
        state = HuskState.ATTACK_ANTICIPATE;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
    }

    private void startLunge() {
        state = HuskState.ATTACK_LUNGE;
        stateTime = 0f;
        lungeStartX = position.x;
        // جهت به سمت بازیکن در لحظه شروع
        float dir = (player.getPosition().x > position.x) ? 1f : -1f;
        facing = (int) dir;
        velocity.x = facing * CHARGE_SPEED;
        velocity.y = 0f;
        isMoving = true;
    }

    private void updateLunge() {
        // سرعت شارژ را حفظ کن (ممکن است برخوردها تغییرش دهند)
//        velocity.x = facing * CHARGE_SPEED;
        isMoving = true;
        move(facing, Gdx.graphics.getDeltaTime(), 0.2f);

        // ۱. برخورد با دیوار (سرعت افقی تقریباً صفر شود)
        if (Math.abs(velocity.x) < 1f && isGrounded()) {
            endLunge();
            return;
        }

        // ۲. رسیدن به لبه (پرتگاه)
        if (isGrounded() && !isGroundAhead()) {
            endLunge();
            return;
        }

        // ۳. محدودیت مسافت شارژ
        if (Math.abs(position.x - lungeStartX) >= MAX_CHARGE_DISTANCE) {
            endLunge();
            return;
        }
    }

    private void endLunge() {
        state = HuskState.ATTACK_COOLDOWN;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
    }

    private void returnToPatrol() {
        state = HuskState.PATROL_IDLE;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
    }

    // ===================== سنسورهای محیطی =====================
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

    private void checkVision() {
        Rectangle visionRect = new Rectangle(
            (facing == 1) ? bounds.x + bounds.width : bounds.x - VISION_LENGTH,
            bounds.y,
            VISION_LENGTH,
            bounds.height
        );
        if (player.getBounds().overlaps(visionRect)) {
            startAnticipate();
        }
    }

    // ===================== مرگ و جسد =====================
    @Override
    public void die() {
        if (!isAlive) return;
        super.die();
        state = HuskState.DEAD;
    }

    @Override
    public Corpse getCorpse() {
        float scale = (facing == 1) ? -1f : 1f;
        return new Corpse(new Rectangle(bounds),
            new Vector2(velocity),
            animDeathLand,
            scale * mScale * 1.2f,mScale,
            0f, -10f);
    }

    // ===================== رسم =====================
    @Override
    public void draw(Batch batch) {
        Animation<TextureRegion> anim = null;
        switch (state) {
            case PATROL_WALK:      anim = animWalk; break;
            case PATROL_IDLE:      anim = animIdle; break;
            case TURN:             anim = animTurn; break;
            case ATTACK_ANTICIPATE: anim = animAttackAnticipate; break;
            case ATTACK_LUNGE:     anim = animAttackLunge; break;
            case ATTACK_COOLDOWN:  anim = animAttackCooldown; break;
            case DEAD:             return;
        }
        if (anim == null) return;

        TextureRegion frame = anim.getKeyFrame(stateTime, false);
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();

// ☀️ ابعاد نهایی پس از اعمال مقیاس
        float finalW = w * mScale;
        float finalH = h * mScale;

// ☀️ موقعیت گوشهٔ چپ-پایین اسپرایت به‌گونه‌ای که مرکز آن روی مرکز bounds بیفتد
        float drawX = bounds.x + (bounds.width - finalW) / 2f;
        float drawY = bounds.y + (bounds.height - finalH) / 2f + 60;

        float scaleX = (facing == 1) ? -1 : 1;
        float originX = finalW / 2f;
        float originY = finalH / 2f;

// ☀️ رسم با ابعاد نهایی و scaleX خالص (بدون mScale)
        batch.draw(frame, drawX, drawY, originX, originY, finalW, finalH, scaleX, 1f, 0);

        // دیباگ
        if (AppStatus.DEBUG) {
            batch.end();
            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);

            debugRenderer.setColor(Color.RED);
            debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

            // مستطیل دید
            debugRenderer.setColor(Color.BLUE);
            float vx = (facing == 1) ? bounds.x + bounds.width : bounds.x - VISION_LENGTH;
            debugRenderer.rect(vx, bounds.y, VISION_LENGTH, bounds.height);

            // محدوده گشت‌زنی (اختیاری)
            debugRenderer.setColor(Color.GREEN);
            debugRenderer.line(patrolStartX, bounds.y, patrolStartX, bounds.y + bounds.height);

            debugRenderer.end();
            batch.begin();
        }
    }

    public static void disposeAtlas() {
        if (atlas != null) {
            atlas.dispose();
            atlas = null;
        }
    }
}
