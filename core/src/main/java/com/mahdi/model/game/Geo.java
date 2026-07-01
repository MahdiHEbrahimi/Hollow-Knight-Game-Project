package com.mahdi.model.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mahdi.model.status.AppStatus;

public class Geo {
    private static Texture texture;
    private final Vector2 position;
    private final Vector2 velocity;
    private final Rectangle bounds;
    private boolean alive = true;
    private float lifeTime = 15.0f;
    private static final float GRAVITY = -500f;
    private static final float MAX_SPEED = 200;
    private boolean grounded;

    public Geo(float x, float y) {
        if (texture == null) texture = new Texture(Gdx.files.internal("global/geo.png"));
        this.position = new Vector2(x, y);
        this.velocity = new Vector2();
        float angle = MathUtils.random(0f, 360f);
        float speed = MathUtils.random(40f, MAX_SPEED);
        this.velocity.set(MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed + MAX_SPEED);
        this.bounds = new Rectangle(x, y, 60, 60);
    }

    public void update(float delta) {
        if (!alive) return;
        lifeTime -= delta;
        if (lifeTime <= 0f) { alive = false; return; }
        velocity.y += GRAVITY * delta;
        if (velocity.y < -600f) velocity.y = -600f;   // محدودیت سقوط

        if (velocity.x < 1f) velocity.x = 0;
        else velocity.x *= 0.99f;

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        bounds.setPosition(position.x, position.y);
    }

    // ☀️ یک ShapeRenderer استاتیک برای تمام سکه‌ها
    private static ShapeRenderer debugRenderer;

    public void draw(Batch batch) {
        if (!alive || texture == null) return;

        float w = bounds.width, h = bounds.height;
        batch.draw(texture, position.x, position.y, w, h);

        // ☀️ رسم مستطیل زرد توخالی برای دیباگ
        if (AppStatus.DEBUG) {   // یا هر شرط دیباگ دلخواه
            batch.end();
            if (debugRenderer == null) {
                debugRenderer = new ShapeRenderer();
            }
            debugRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            debugRenderer.begin(ShapeRenderer.ShapeType.Line);
            debugRenderer.setColor(Color.YELLOW);
            debugRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            debugRenderer.end();
            batch.begin();
        }
    }

    public boolean isAlive() { return alive; }
    public Rectangle getBounds() { return bounds; }
    public Vector2 getPosition() { return position; }
    public Vector2 getVelocity() { return velocity; }
    public void setGrounded(boolean g) { this.grounded = g; }

    public static void loadTexture(String path) {
        if (texture == null) texture = new Texture(Gdx.files.internal(path));
    }
    public static void disposeTexture() {
        if (texture != null) { texture.dispose(); texture = null; }
    }
}
