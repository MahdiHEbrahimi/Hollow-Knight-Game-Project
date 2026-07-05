package com.mahdi.model.characters.enemies;

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

public class Mosquito extends Enemy {

    private final Player player;
    private static TextureAtlas atlas;

    private final Animation<TextureRegion> animIdle;
    private final Animation<TextureRegion> animAnticipate;
    private final Animation<TextureRegion> animAttack;
    private final Animation<TextureRegion> animDeath;
    private final Animation<TextureRegion> animTurn;
    private final Animation<TextureRegion> animTurn2;

    private enum MosquitoState { IDLE, TURN, ANTICIPATE, ATTACK, DEAD }
    private MosquitoState state = MosquitoState.IDLE;
    private float stateTime = 0f;

    private int facing = 1;
    private int pendingFacing = 1;

    private Vector2 targetPos = new Vector2();
    private final Vector2 spawnPos;

    private static final float DETECT_RANGE = 700f;
    private static final float ATTACK_SPEED = 800f;
    private static final float HOME_RADIUS = 10f;

    private static ShapeRenderer debugRenderer;

    public Mosquito(float x, float y, Player player) {
        super(x, y, 80, 80, ATTACK_SPEED, 400f, 2);
        this.player = player;
        this.hasGravity = false;
        this.spawnPos = new Vector2(x, y);

        if (atlas == null) {
            atlas = new TextureAtlas("enemies/Mosquito/Mosquito.atlas");
        }

        animIdle        = createAnimation("Idle",                0.1f, Animation.PlayMode.LOOP);
        animAnticipate  = createAnimation("Attack Anticipate",   0.08f, Animation.PlayMode.NORMAL);
        animAttack      = createAnimation("Attack",              0.08f, Animation.PlayMode.LOOP);
        animDeath       = createAnimation("Death",               0.1f, Animation.PlayMode.NORMAL);
        animTurn        = createAnimation("Turn",                0.08f, Animation.PlayMode.NORMAL);
        animTurn2       = createAnimation("Turn2",               0.08f, Animation.PlayMode.NORMAL);

        if (debugRenderer == null) {
            debugRenderer = new ShapeRenderer();
        }
    }

    private Animation<TextureRegion> createAnimation(String prefix, float dur, Animation.PlayMode mode) {
        Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(prefix);
        if (regions.size == 0) {
            System.err.println("WARNING: Animation " + prefix + " not found in Mosquito atlas!");
            return null;
        }
        return new Animation<>(dur, regions, mode);
    }

    @Override
    protected void updateCustomLogic(float delta) {
        if (state == MosquitoState.DEAD) return;
        stateTime += delta;

        switch (state) {
            case IDLE:
                idleUpdate();
                break;
            case TURN:
                Animation<TextureRegion> turnAnim = (pendingFacing == 1) ? animTurn2 : animTurn;
                if (turnAnim != null && turnAnim.isAnimationFinished(stateTime)) {
                    facing = pendingFacing;
                    state = MosquitoState.IDLE;
                    stateTime = 0f;
                }
                break;
            case ANTICIPATE:
                if (animAnticipate != null && animAnticipate.isAnimationFinished(stateTime)) {
                    startAttack();
                }
                break;
            case ATTACK:
                attackUpdate();
                break;
        }
    }

    private void updateFacingFromVelocity() {
        if (velocity.x > 0.1f) {
            if (facing != 1) {
                pendingFacing = 1;
                state = MosquitoState.TURN;
                stateTime = 0f;
            }
        } else if (velocity.x < -0.1f) {
            if (facing != -1) {
                pendingFacing = -1;
                state = MosquitoState.TURN;
                stateTime = 0f;
            }
        }
    }

    private void idleUpdate() {
        float distToPlayer = position.dst(player.getPosition());
        if (distToPlayer <= DETECT_RANGE) {
            targetPos.set(player.getPosition());
            velocity.setZero();
            state = MosquitoState.ANTICIPATE;
            stateTime = 0f;
            return;
        }

        float distToSpawn = position.dst(spawnPos);
        if (distToSpawn > HOME_RADIUS) {
            moveToPos(spawnPos, 0.4f);   // بدون delta
        } else {
            velocity.setZero();
        }

        updateFacingFromVelocity();
    }

    private void startAttack() {
        state = MosquitoState.ATTACK;
        velocity.set(0,0);
        stateTime = 0f;

        if (targetPos.x > position.x)
            facing = 1;
        else if (targetPos.x < position.x)
            facing = -1;

        moveToPos(targetPos, 1);   // شتاب اولیه
    }

    private void attackUpdate() {
        // اگر به نقطه هدف رسیدیم (با آستانه ۱ پیکسل)
        if (position.epsilonEquals(targetPos, 1f)) {
            onAttackFinished();
            return;
        }

        moveToPos(targetPos, 1);   // حرکت پیوسته
        setGrounded(false);

        Array<SolidBlock> blocks = AppStatus.getGameEngine().getSolidBlocks();
        for (SolidBlock b : blocks) {
            if (!b.isDeadly && bounds.overlaps(b.bounds)) {
                onAttackFinished();
                return;
            }
        }
    }

    // ☀️ متد جدید: پس از پایان حمله (رسیدن به هدف یا برخورد)
    private void onAttackFinished() {
        float distToPlayer = position.dst(player.getPosition());
        if (distToPlayer <= DETECT_RANGE) {
            // بازیکن هنوز در دید است → حمله بعدی را سریعاً آماده کن
            targetPos.set(player.getPosition());
            state = MosquitoState.ANTICIPATE;
            stateTime = 0f;
        } else {
            // بازیکن از دید خارج شده → برگرد به گشت‌زنی (بازگشت به خانه)
            state = MosquitoState.IDLE;
            stateTime = 0f;
        }
        velocity.setZero();
    }

    @Override
    public void die() {
        if (!isAlive) return;
        super.die();
        this.hasGravity = true;
        state = MosquitoState.DEAD;
    }

    @Override
    public Corpse getCorpse() {
        float scale = (facing == 1) ? -1f : 1f;
        return new Corpse(new Rectangle(bounds),
            new Vector2(velocity),
            animDeath,
            scale,
            0f, -10f);
    }

    @Override
    public void draw(Batch batch) {
        Animation<TextureRegion> anim = null;
        switch (state) {
            case IDLE:        anim = animIdle; break;
            case TURN:        anim = (pendingFacing == 1) ? animTurn2 : animTurn; break;
            case ANTICIPATE:  anim = animAnticipate; break;
            case ATTACK:      anim = animAttack; break;
            case DEAD:        return;
        }
        if (anim == null) return;

        TextureRegion frame = anim.getKeyFrame(stateTime, state != MosquitoState.ANTICIPATE && state != MosquitoState.TURN);
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        float drawX = bounds.x + (bounds.width - w) / 2f;
        float drawY = bounds.y + (bounds.height - h) / 2f;

        float scaleX = (facing == 1) ? -1 : 1;
        float originX = w / 2f;
        float originY = h / 2f;

        batch.draw(frame, drawX, drawY, originX, originY, w, h, scaleX, 1, 0);

        if (AppStatus.DEBUG) {
            batch.end();
            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);

            debugRenderer.setColor(Color.RED);
            debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

            debugRenderer.setColor(Color.ORANGE);
            float centerX = bounds.x + bounds.width / 2f;
            float centerY = bounds.y + bounds.height / 2f;
            debugRenderer.circle(centerX, centerY, DETECT_RANGE);

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
