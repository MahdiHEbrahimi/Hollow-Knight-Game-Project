package com.mahdi.model.characters.enemis;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mahdi.model.characters.Corpse;
import com.mahdi.model.characters.Enemy;
import com.mahdi.model.characters.Player;
import com.mahdi.model.status.AppStatus;

public class Crawled extends Enemy {

    private final Player player;

    // اطلس اشتراکی (static)
    private static TextureAtlas atlas;

    private final Animation<TextureRegion> animWalk;
    private final Animation<TextureRegion> animTurn;
    private final Animation<TextureRegion> Death;   // انیمیشن مرگ (Death Air + Death Land ترکیبی)

    private enum EnemyState { WALK, TURN, DEAD }
    private EnemyState state = EnemyState.WALK;
    private float stateTime = 0f;
    private int currentDirection = 1; // 1 = راست، -1 = چپ

    // رندرر دیباگ برای نمایش مستطیل زرد
    private static ShapeRenderer debugRenderer;

    public Crawled(float x, float y, Player player) {
        super(x, y, 120, 80, 1600f, 900f, 3);
        this.player = player;

        // بارگذاری اشتراکی اطلس – فقط یک بار
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
                moveToPos(player.getPosition(), delta);
                float dx = player.getPosition().x - position.x;
                int desiredDirection = (dx > 0) ? 1 : -1;
                if (desiredDirection != currentDirection) {
                    currentDirection = desiredDirection;
                    if (animTurn != null) {
                        state = EnemyState.TURN;
                        stateTime = 0f;
                    }
                }
                break;

            case TURN:
                if (animTurn != null && animTurn.isAnimationFinished(stateTime)) {
                    state = EnemyState.WALK;
                    stateTime = 0f;
                }
                break;
        }
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

        // فلیپ افقی بر اساس جهت حرکت
        float scaleX = (currentDirection == 1) ? -1 : 1;
        float originX = w / 2f;
        float originY = h / 2f;

        batch.draw(frame,
            drawX, drawY,
            originX, originY,
            w, h,
            scaleX, 1, 0);

        // رسم مستطیل زرد توخالی برای دیباگ
        if (AppStatus.DEBUG){
            batch.end();
            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);
            debugRenderer.setColor(Color.YELLOW);
            debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            debugRenderer.end();
            batch.begin();
        }
    }

    /**
     * جسدی متناسب با جهت فعلی دشمن برمی‌گرداند.
     * انیمیشن Death (ترکیبی از Death Air و Death Land) را با scaleX مناسب به Corpse می‌دهد.
     */
    @Override
    public Corpse getCorpse() {
        float scale = (currentDirection == 1) ? -1f : 1f;
        // ☀️ افست‌ها: 0 افقی، -10 پیکسل به سمت پایین (بسته به ظاهر فریم‌های مرگ)
        return new Corpse(new com.badlogic.gdx.math.Rectangle(bounds),
            new Vector2(velocity),
            Death,
            scale,
            0f,   // offsetX
            -10f); // offsetY
    }

    @Override
    public void die() {
        if (!isAlive) return;
        super.die();
        state = EnemyState.DEAD;
    }

    /**
     * پاک‌سازی اطلس اشتراکی.
     */
    public static void disposeAtlas() {
        if (atlas != null) {
            atlas.dispose();
            atlas = null;
        }
    }
}
