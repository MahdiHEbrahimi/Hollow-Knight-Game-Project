package com.mahdi.model.game;

import java.util.ArrayList;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.mahdi.model.characters.Enemy;
import com.mahdi.model.characters.Player;
import com.mahdi.model.characters.enemis.Cockroach;
import com.mahdi.model.map.SolidBlock;
import com.mahdi.model.map.TiledMapHelper;

public class GameEngine {

    private final float spawnX = 641f;
    private final float spawnY = 801f;

    private final Player player;
    private final ArrayList<Enemy> enemies;

    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Array<SolidBlock> solidBlocks;

    public GameEngine() {
        TiledMapHelper mapHelper = new TiledMapHelper();
        this.tiledMap = mapHelper.loadMap("maps/untitled.tmx");
        this.solidBlocks = mapHelper.getSolidRectangles();
        this.mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        this.enemies = new ArrayList<>();

        float finalSpawnX = spawnX;
        float finalSpawnY = spawnY;

        MapLayer layer = tiledMap.getLayers().get("logical");
        if (layer != null) {
            int mapHeightInTiles = tiledMap.getProperties().get("height", Integer.class);
            int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
            float totalMapHeight = mapHeightInTiles * tileHeight;

            for (MapObject object : layer.getObjects()) {
                if ("SpawnPlayer".equals(object.getName()) && object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    finalSpawnX = rect.x;
                    finalSpawnY = totalMapHeight - rect.y - rect.height;
                    System.out.println("[GameEngine] Smart Spawn Point Found! X=" + finalSpawnX + " Y=" + finalSpawnY);
                    break;
                }
            }
        }

        this.player = new Player(finalSpawnX, finalSpawnY);
        spawnEnemiesFromMap();
    }

    private void spawnEnemiesFromMap() {
        MapLayer layer = tiledMap.getLayers().get("logical");
        if (layer == null) return;

        int mapHeightInTiles = tiledMap.getProperties().get("height", Integer.class);
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
        float totalMapHeight = mapHeightInTiles * tileHeight;

        for (MapObject object : layer.getObjects()) {
            String name = object.getName();
            if (name == null) continue;

            if (name.startsWith("Cockroach")) {
                // فقط اگر شیء مستطیلی باشد پردازش می‌کنیم
                if (!(object instanceof RectangleMapObject)) {
                    System.out.println("WARNING: Cockroach spawn point is not a rectangle, skipping.");
                    continue;
                }

                RectangleMapObject rectObj = (RectangleMapObject) object;
                Rectangle rect = rectObj.getRectangle();

                // تبدیل مختصات Tiled (پایین-چپ) به LibGDX (بالا-چپ)
                float spawnX = rect.x;
                float spawnY = totalMapHeight - rect.y - rect.height;

                Enemy cockroach = new Cockroach(this.spawnX + 200, this.spawnY, player);
                enemies.add(cockroach);
                System.out.println("Spawned Cockroach at: " + spawnX + ", " + spawnY);
            }
        }
    }

    public void update(float delta) {
        player.update(delta);
        handleMapCollisions(player, delta);

        // بررسی برخورد هیت‌باکس حمله شوالیه با دشمنان
        Rectangle playerAttackBox = player.getAttackHitbox();
        if (playerAttackBox != null) {
            for (Enemy e : enemies) {
                if (e.isAlive() && e instanceof Cockroach) {
                    if (playerAttackBox.overlaps(e.getBounds())) {
                        ((Cockroach) e).takeDamage(1);
//                        player.clearAttackHitbox();   // هر ضربه فقط یک آسیب
                        break;
                    }
                }
            }
        }

        // به‌روزرسانی دشمنان
        for (Enemy enemy : enemies) {
            enemy.update(delta);
        }
    }

    public void draw(OrthographicCamera camera, SpriteBatch batch) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.begin();
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                e.draw(batch);
            }
        }
        player.draw(batch);
        batch.end();
    }

    private void handleMapCollisions(Player p, float delta) {
        Rectangle playerBounds = p.getBounds();
        p.setGrounded(false);

        float sensorHeight = 2f;
        Rectangle footSensor = new Rectangle(
            playerBounds.x,
            playerBounds.y - sensorHeight,
            playerBounds.width,
            sensorHeight
        );

        for (SolidBlock block : solidBlocks) {
            if (block.isDeadly && playerBounds.overlaps(block.bounds)) {
                p.takeDamage(1);   // تغییر یافته از reduceHP به takeDamage
                continue;
            }

            if (p.getVelocity().y < 0 && playerBounds.overlaps(block.bounds)
                && playerBounds.y + playerBounds.height > block.bounds.y + block.bounds.height) {
                p.getPosition().y = block.bounds.y + block.bounds.height;
                p.getVelocity().y = 0;
                p.setGrounded(true);
            } else if (p.getVelocity().y > 0 && playerBounds.overlaps(block.bounds)
                && playerBounds.y < block.bounds.y) {
                p.getPosition().y = block.bounds.y - playerBounds.height;
                p.getVelocity().y = 0;
            }

            if (!p.isGrounded() && playerBounds.overlaps(block.bounds)) {
                if (p.getVelocity().x > 0 && playerBounds.x < block.bounds.x) {
                    p.getPosition().x = block.bounds.x - playerBounds.width;
                } else if (p.getVelocity().x < 0 && playerBounds.x > block.bounds.x) {
                    p.getPosition().x = block.bounds.x + block.bounds.width;
                }
            }

            if (footSensor.overlaps(block.bounds) && !block.isDeadly) {
                p.setGrounded(true);
            }
        }

        p.getBounds().setPosition(p.getPosition().x, p.getPosition().y);
    }

    public Player getPlayer() {
        return player;
    }

    public void dispose() {
        if (tiledMap != null) tiledMap.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (player != null) player.dispose();
        for (Enemy e : enemies) e.dispose();
    }
}
