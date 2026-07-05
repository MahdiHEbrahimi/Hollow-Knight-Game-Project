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

public class CrystalGuardian extends Enemy {

    private final Player player;
    private static TextureAtlas atlas;

    // انیمیشن‌ها
    private final Animation<TextureRegion> animIdle;
    private final Animation<TextureRegion> animTurn;
    private final Animation<TextureRegion> animLaserCircle;
    private final Animation<TextureRegion> animShoot;
    private final Animation<TextureRegion> animRun;
    private final Animation<TextureRegion> animEvade;
    private final Animation<TextureRegion> animDeathLand;
    private final Animation<TextureRegion> animLaserBeam;

    private enum GuardianState {
        PATROL_WALK, PATROL_IDLE, TURN,
        LASER_CIRCLE, SHOOT,
        DEAD
    }
    private GuardianState state = GuardianState.PATROL_IDLE;
    private float stateTime = 0f;

    private int facing = 1;                 // ۱ = راست، -۱ = چپ
    final float mScale = 1.5f;

    // ===================== گشت‌زنی (مثل HornheadHusk) =====================
    private float patrolStartX;
    private static final float PATROL_DISTANCE = 1000f;   // مسافت راه رفتن قبل از چرخش
    private static final float IDLE_DURATION = 1.2f;      // زمان استراحت بین راه رفتن‌ها
    private static final float PATROL_SPEED = 120f;

    // ===================== دید =====================
    private static final float VISION_LENGTH = 800f;
    private static final float VISION_HEIGHT = 300f;   // ☀️ کمی بلندتر تا پریدن بازیکن هم پوشش دهد

    // ===================== حمله (لیزر) =====================
    private static final float LASER_CIRCLE_DURATION = 1.8f;
    private static final float SHOOT_DURATION = 0.5f;
    private static final float LASER_LIFETIME = 2f;
    private static final float ATTACK_COOLDOWN = 1.5f;
    private float cooldownTimer = 0f;

    // ===================== گوی شارژ (Charge Orb) =====================
    private static final float ORB_MAX_RADIUS = 18f;
    private static final float ORB_MIN_RADIUS = 6f;

    private static ShapeRenderer debugRenderer;
    private static ShapeRenderer fxRenderer;

    public CrystalGuardian(float x, float y, Player player) {
        super(x, y, 200, 200, 400f, 800f, 4);
        this.player = player;
        this.hasGravity = true;

        if (atlas == null) {
            atlas = new TextureAtlas("enemies/CrystalGuardian/CrystalGuardian.atlas");
        }

        animIdle        = createAnimation("Idle",          0.15f, Animation.PlayMode.LOOP);
        animTurn        = createAnimation("Turn",          0.1f,  Animation.PlayMode.NORMAL);
        animLaserCircle = createAnimation("LaserCircle",   0.08f, Animation.PlayMode.LOOP);
        animShoot       = createAnimation("Shoot",         0.06f, Animation.PlayMode.NORMAL);
        animRun         = createAnimation("Run",           0.08f, Animation.PlayMode.LOOP);
        animEvade       = createAnimation("Evade",         0.06f, Animation.PlayMode.NORMAL);
        animDeathLand   = createAnimation("Death Land",    0.1f,  Animation.PlayMode.NORMAL);
        animLaserBeam   = createAnimation("CrystalLaser",  0.05f, Animation.PlayMode.LOOP);

        if (debugRenderer == null) {
            debugRenderer = new ShapeRenderer();
        }
        if (fxRenderer == null) {
            fxRenderer = new ShapeRenderer();
        }
    }

    private Animation<TextureRegion> createAnimation(String prefix, float dur, Animation.PlayMode mode) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(prefix);
        if (regions.size == 0) {
            System.err.println("WARNING: Animation " + prefix + " not found in CrystalGuardian atlas!");
            return null;
        }
        return new Animation<>(dur, regions, mode);
    }

    @Override
    protected void updateCustomLogic(float delta) {
        if (state == GuardianState.DEAD) return;
        stateTime += delta;
        cooldownTimer -= delta;

        switch (state) {
            case PATROL_WALK:
                updatePatrolWalk();
                break;
            case PATROL_IDLE:
                updatePatrolIdle();
                break;
            case TURN:
                updateTurn();
                break;
            case LASER_CIRCLE:
                if (stateTime >= LASER_CIRCLE_DURATION) {
                    startShoot();
                }
                break;
            case SHOOT:
                if (animShoot == null || animShoot.isAnimationFinished(stateTime) || stateTime >= SHOOT_DURATION) {
                    spawnLaser();
                    endAttack();
                }
                break;
        }
    }

    // ===================== گشت‌زنی (راه رفتن) =====================
    private void enterPatrolWalk() {
        state = GuardianState.PATROL_WALK;
        stateTime = 0f;
        patrolStartX = position.x;
        velocity.x = facing * PATROL_SPEED;
        isMoving = true;
    }

    private void updatePatrolWalk() {
        velocity.x = facing * PATROL_SPEED;
        isMoving = true;

        float dist = Math.abs(position.x - patrolStartX);
        if (dist >= PATROL_DISTANCE) {
            startTurn();
            return;
        }

        if (isWallAhead() || !isGroundAhead()) {
            startTurn();
            return;
        }

        checkVision();
    }

    // ===================== استراحت (Idle) =====================
    private void updatePatrolIdle() {
        velocity.x = 0;
        isMoving = false;

        if (stateTime >= IDLE_DURATION) {
            startTurn();
        }

        checkVision();
    }

    // ===================== چرخش (Turn) =====================
    private void startTurn() {
        state = GuardianState.TURN;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
    }

    private void updateTurn() {
        if (animTurn == null || animTurn.isAnimationFinished(stateTime)) {
            facing = -facing;
            enterPatrolWalk();
        }
    }

    // ===================== دید =====================
    private boolean isPlayerInVision() {
        Rectangle visionRect = new Rectangle(
            (facing == 1) ? bounds.x + bounds.width : bounds.x - VISION_LENGTH,
            bounds.y - (VISION_HEIGHT - bounds.height) / 2f,
            VISION_LENGTH,
            VISION_HEIGHT
        );
        return player.getBounds().overlaps(visionRect);
    }

    private void checkVision() {
        if (cooldownTimer > 0f) return;
        if (isPlayerInVision()) {
            startLaserCircle();
        }
    }

    // ===================== حمله (لیزر) =====================
    private void startLaserCircle() {
        state = GuardianState.LASER_CIRCLE;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
    }

    private void startShoot() {
        state = GuardianState.SHOOT;
        stateTime = 0f;
        velocity.x = 0;
        isMoving = false;
    }

    private void spawnLaser() {
        float shootX = bounds.x + bounds.width / 2f + (facing * 970f);
        float shootY = bounds.y + bounds.height * 0.8f;

        // ابعاد فریم اول انیمیشن لیزر
        TextureRegion firstFrame = animLaserBeam.getKeyFrame(0);
        float beamW = firstFrame.getRegionWidth();
        float beamH = firstFrame.getRegionHeight();

        // مستطیل برخورد دقیقاً به اندازه انیمیشن و در مرکز نقطه شلیک
        Rectangle laserHitbox = new Rectangle(shootX - beamW/2f, shootY - beamH/2f, beamW, beamH);

        // پرتابه ثابت (سرعت صفر) بدون افست اضافی
        Projectile laser = new Projectile(laserHitbox, 0f, 0f, LASER_LIFETIME, animLaserBeam, true, 0f, 0f);

        AppStatus.getGameEngine().addProjectile(laser);
    }

    private void endAttack() {
        // ☀️ اگر بازیکن هنوز توی دیدشه، دوباره شلیک کن
        if (isPlayerInVision()) {
            startLaserCircle();
            return;
        }
        // ☀️ در غیر این صورت، مسافت گشت‌زنی ریست بشه و به همون جهت (سمت پلیر) ادامه بده
        cooldownTimer = ATTACK_COOLDOWN;
        enterPatrolWalk();
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

    // ===================== مرگ و جسد =====================
    @Override
    public void die() {
        if (!isAlive) return;
        super.die();
        state = GuardianState.DEAD;
    }

    @Override
    public Corpse getCorpse() {
        float scale = (facing == 1) ? -1f : 1f;
        return new Corpse(new Rectangle(bounds),
            new Vector2(velocity),
            animDeathLand,
            scale * mScale,
            -30f * mScale, 0);
    }

    // ===================== رسم =====================
    @Override
    public void draw(Batch batch) {
        Animation<TextureRegion> anim = null;
        switch (state) {
            case PATROL_WALK:   anim = animRun; break;
            case PATROL_IDLE:   anim = animIdle; break;
            case TURN:          anim = animTurn; break;
            case LASER_CIRCLE:  anim = animEvade; break;
            case SHOOT:         anim = animShoot; break;
            case DEAD:          return;
        }
        if (anim == null) {
            anim = animIdle;
        }
        if (anim == null) return;

        TextureRegion frame = anim.getKeyFrame(stateTime, false);
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();

        float finalW = w * mScale;
        float finalH = h * mScale;

        float drawX = bounds.x + (bounds.width - finalW) / 2f;
        float drawY = bounds.y + (bounds.height - finalH) / 2f + 60;

        float scaleX = (facing == 1) ? -1 : 1;
        float originX = finalW / 2f;
        float originY = finalH / 2f;

        batch.draw(frame, drawX, drawY, originX, originY, finalW, finalH, scaleX, 1f, 0);

        // ☀️ گوی نورانی بالای شاخ، در حین شارژ و شلیک
        if (state == GuardianState.LASER_CIRCLE || state == GuardianState.SHOOT) {
            drawChargeOrb(batch);
        }

        if (AppStatus.DEBUG) {
            batch.end();
            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);

            debugRenderer.setColor(Color.RED);
            debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

            debugRenderer.setColor(Color.BLUE);
            float vx = (facing == 1) ? bounds.x + bounds.width : bounds.x - VISION_LENGTH;
            debugRenderer.rect(vx, bounds.y - (VISION_HEIGHT - bounds.height) / 2f, VISION_LENGTH, VISION_HEIGHT);

            debugRenderer.setColor(Color.GREEN);
            debugRenderer.line(patrolStartX, bounds.y, patrolStartX, bounds.y + bounds.height);

            debugRenderer.end();
            batch.begin();
        }
    }

    private void drawChargeOrb(Batch batch) {
        // ☀️ پیشرفت شارژ: در حالت شلیک همیشه پُر و بزرگ‌ترین اندازه
        float progress;
        if (state == GuardianState.SHOOT) {
            progress = 1f;
        } else {
            progress = Math.min(stateTime / LASER_CIRCLE_DURATION, 1f);
        }
        float radius = ORB_MIN_RADIUS + (ORB_MAX_RADIUS - ORB_MIN_RADIUS) * progress;

        // ☀️ بالای شاخ و کمی جلوتر از دشمن (همون نقطه‌ای که پرتابه ازش خارج میشه)
        float orbX = bounds.x + bounds.width / 2f + (facing * 60f);
        float orbY = bounds.y + bounds.height * 0.85f;

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        fxRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        fxRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // هالهٔ بیرونی کم‌رنگ
        fxRenderer.setColor(1f, 0.45f, 0.7f, 0.35f * progress + 0.15f);
        fxRenderer.circle(orbX, orbY, radius * 1.6f);

        // هستهٔ نورانی داخلی
        fxRenderer.setColor(1f, 0.6f, 0.85f, 0.6f + 0.4f * progress);
        fxRenderer.circle(orbX, orbY, radius);

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
