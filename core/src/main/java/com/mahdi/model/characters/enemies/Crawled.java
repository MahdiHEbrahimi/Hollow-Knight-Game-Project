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

public class Crawled extends Enemy {

    private final Player player;

    private static TextureAtlas atlas;

    private final Animation<TextureRegion> animWalk;
    private final Animation<TextureRegion> animTurn;
    private final Animation<TextureRegion> Death;

    private enum EnemyState { WALK, TURN, DEAD }
    private EnemyState state = EnemyState.WALK;
    private float stateTime = 0f;
    private int currentDirection = 1; // 1 = راست، -1 = چپ

    // ☀️ محدودهٔ گشت‌زنی
    private final float spawnX;
    private static final float PATROL_RANGE = 1000f;

    private static ShapeRenderer debugRenderer;

    public Crawled(float x, float y, Player player) {
        super(x, y, 120, 80, 200f, 460f, 3);
        this.player = player;
        this.spawnX = x;   // ☀️ ذخیره‌سازی نقطهٔ اسپاون

        if (atlas == null) {
            atlas = new TextureAtlas("enemies/Crawled/Crawled.atlas");
        }

        animWalk = createAnimation("Walk",  0.1f, Animation.PlayMode.LOOP);
        animTurn = createAnimation("turn",  0.08f, Animation.PlayMode.NORMAL);
        Death    = createAnimation("Death", 0.1f, Animation.PlayMode.NORMAL);

        if (debugRenderer == null) {
            debugRenderer = new ShapeRenderer();
        }
    }

    private Animation<TextureRegion> createAnimation(String regionName, float frameDuration, Animation.PlayMode mode) {
        com.badlogic.gdx.utils.Array<TextureAtlas.AtlasRegion> regions = atlas.findRegions(regionName);
        if (regions.size == 0) {
            System.err.println("WARNING: Animation " + regionName + " not found in Crawled atlas!");
            return null;
        }
        return new Animation<TextureRegion>(frameDuration, regions, mode);
    }

    @Override
    public void updateCustomLogic(float delta) {
        if (state == EnemyState.DEAD) return;
        stateTime += delta;

        switch (state) {
            case WALK:
                // ☀️ منطق گشت‌زنی
                int desiredDirection = calculatePatrolDirection();
                if (desiredDirection != currentDirection) {
                    currentDirection = desiredDirection;
                    if (animTurn != null) {
                        state = EnemyState.TURN;
                        stateTime = 0f;
                    }
                }
                move(desiredDirection, delta);
                break;

            case TURN:
                if (animTurn != null && animTurn.isAnimationFinished(stateTime)) {
                    state = EnemyState.WALK;
                    stateTime = 0f;
                }
                break;
        }
    }

    /**
     * ☀️ محاسبهٔ جهت حرکت بر اساس محدودهٔ گشت‌زنی، پرتگاه و دیوار.
     */
    private int calculatePatrolDirection() {
        // اگر از محدوده خارج شدیم، به سمت مرکز برگرد
        float distFromSpawn = position.x - spawnX;
        if (distFromSpawn > PATROL_RANGE) return -1;
        if (distFromSpawn < -PATROL_RANGE) return 1;

        // فقط در صورت چسبیدن به زمین، سنسورهای لبه و دیوار بررسی شوند
        if (isGrounded()) {
            Array<SolidBlock> blocks = AppStatus.getGameEngine().getSolidBlocks();
            float sensorOffsetX = (currentDirection == 1) ? bounds.width : -2f;
            float sensorX = bounds.x + sensorOffsetX;

            // سنسور پرتگاه (زیر پای جلویی)
            Rectangle edgeSensor = new Rectangle(sensorX, bounds.y - 4f, 2f, 2f);
            boolean groundAhead = false;
            for (SolidBlock b : blocks) {
                if (!b.isDeadly && edgeSensor.overlaps(b.bounds)) {
                    groundAhead = true;
                    break;
                }
            }
            if (!groundAhead) return -currentDirection;   // پرتگاه → برگرد

            // سنسور دیوار (جلوی بدن)
            Rectangle wallSensor = new Rectangle(sensorX, bounds.y, 2f, bounds.height);
            for (SolidBlock b : blocks) {
                if ("wall".equals(b.type) && wallSensor.overlaps(b.bounds)) {
                    return -currentDirection;             // دیوار → برگرد
                }
            }
        }

        return currentDirection;   // همان جهت قبلی را حفظ کن
    }

    @Override
    public void draw(Batch batch) {
        Animation<TextureRegion> anim = null;
        switch (state) {
            case WALK: anim = animWalk; break;
            case TURN: anim = animTurn; break;
            case DEAD: return;
        }
        if (anim == null) return;

        TextureRegion frame = anim.getKeyFrame(stateTime, state != EnemyState.TURN);
        float w = frame.getRegionWidth();
        float h = frame.getRegionHeight();
        float drawX = bounds.x + (bounds.width - w) / 2f;
        float drawY = bounds.y + (bounds.height - h) / 2f + 28f;

        float scaleX = (currentDirection == 1) ? -1 : 1;
        float originX = w / 2f;
        float originY = h / 2f;

        batch.draw(frame,
            drawX, drawY,
            originX, originY,
            w, h,
            scaleX, 1, 0);

        if (AppStatus.DEBUG) {
            batch.end();
            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);
            debugRenderer.setColor(Color.YELLOW);
            debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            debugRenderer.end();
            batch.begin();
        }
    }

    @Override
    public Corpse getCorpse() {
        float scale = (currentDirection == 1) ? -1f : 1f;
        return new Corpse(new com.badlogic.gdx.math.Rectangle(bounds),
            new Vector2(velocity),
            Death,
            scale,
            0f,
            -10f);
    }

    @Override
    public void die() {
        if (!isAlive) return;
        super.die();
        state = EnemyState.DEAD;
    }

    public static void disposeAtlas() {
        if (atlas != null) {
            atlas.dispose();
            atlas = null;
        }
    }
}
