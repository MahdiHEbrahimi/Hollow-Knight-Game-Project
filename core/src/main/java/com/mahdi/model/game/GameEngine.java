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
import com.mahdi.model.characters.BaseCharacter;
import com.mahdi.model.characters.Corpse;
import com.mahdi.model.characters.Enemy;
import com.mahdi.model.characters.Player;
import com.mahdi.model.characters.enemis.Crawled;
import com.mahdi.model.map.SolidBlock;
import com.mahdi.model.map.TiledMapHelper;

public class GameEngine {

    private final float spawnX = 641f;
    private final float spawnY = 801f;

    private final Player player;
    private final ArrayList<Enemy> enemies;
    private final ArrayList<Corpse> corpses;

    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Array<SolidBlock> solidBlocks;

    public GameEngine() {
        TiledMapHelper mapHelper = new TiledMapHelper();
        this.tiledMap = mapHelper.loadMap("maps/untitled.tmx");
        this.solidBlocks = mapHelper.getSolidRectangles();
        this.mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        this.enemies = new ArrayList<>();
        this.corpses = new ArrayList<>();

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
        this.enemies.add(new Crawled(1141 + 300, this.spawnY, this.player));
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
                if (!(object instanceof RectangleMapObject)) {
                    System.out.println("WARNING: Cockroach spawn point is not a rectangle, skipping.");
                    continue;
                }

                RectangleMapObject rectObj = (RectangleMapObject) object;
                Rectangle rect = rectObj.getRectangle();

                float spawnX = rect.x;
                float spawnY = totalMapHeight - rect.y - rect.height;

                // از مختصات واقعی استفاده کن، نه مقادیر ثابت
                Enemy cockroach = new Crawled(this.spawnX + 200, this.spawnY, player);
                //todo
                enemies.add(cockroach);
                System.out.println("Spawned Cockroach at: " + spawnX + ", " + spawnY);
            }
        }
    }

    public void update(float delta) {
        player.update(delta);
        handleMapCollisions(player);

        for (Corpse c : corpses) {
            c.update(delta);
        }

        // بررسی برخورد هیت‌باکس حمله شوالیه با دشمنان
        Rectangle playerAttackBox = player.getAttackHitbox();
        if (playerAttackBox != null) {
            for (Enemy e : enemies) {
                if (e.isAlive()) {
                    if (playerAttackBox.overlaps(e.getBounds())) {
                        player.attackWasSuccessful(e);   // هر ضربه فقط یک آسیب
                        break;
                    }
                }
            }
        }

        for (Corpse corpse : corpses){
            handleMapCollisions(corpse);
        }

        // به‌روزرسانی دشمنان
        for (Enemy enemy : enemies) {
            enemy.update(delta);
            handleMapCollisions(enemy);
            if (enemy.getBounds().overlaps(player.getBounds()) && enemy.isAlive()){
                player.takeDamage(1, enemy);
            }
        }
    }

    public void draw(OrthographicCamera camera, SpriteBatch batch) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.begin();
        for(Corpse c : corpses){
            c.draw(batch);
        }
        for (Enemy e : enemies) {
            if (e.isAlive()) {
                e.draw(batch);
            }
        }
        player.draw(batch);
        batch.end();
    }

    private void handleMapCollisions(BaseCharacter character) {
        Rectangle charBounds = character.getBounds();
        character.setGrounded(false);

        float sensorHeight = 2f;
        Rectangle footSensor = new Rectangle(
            charBounds.x,
            charBounds.y - sensorHeight,
            charBounds.width,
            sensorHeight
        );

        for (SolidBlock block : solidBlocks) {
            // برخورد با تیغ‌های مرگبار: فقط بازیکن آسیب ببیند
            if (block.isDeadly && charBounds.overlaps(block.bounds)) {
                if (character instanceof Player) {
                    ((Player) character).takeDamageFormGround();
                }
                continue;
            }

            // فرود از بالا
            if (character.getVelocity().y < 0 && charBounds.overlaps(block.bounds)
                && charBounds.y + charBounds.height > block.bounds.y + block.bounds.height) {
                character.getPosition().y = block.bounds.y + block.bounds.height;
                character.getVelocity().y = 0;
                character.setGrounded(true);
            }
            // برخورد از پایین (سقف)
            else if (character.getVelocity().y > 0 && charBounds.overlaps(block.bounds)
                && charBounds.y < block.bounds.y) {
                character.getPosition().y = block.bounds.y - charBounds.height;
                character.getVelocity().y = 0;
            }

            // برخورد افقی با دیوارها (فقط وقتی روی زمین نیست)
            if (!character.isGrounded() && charBounds.overlaps(block.bounds)) {
                if (character.getVelocity().x > 0 && charBounds.x < block.bounds.x) {
                    character.getPosition().x = block.bounds.x - charBounds.width;
                } else if (character.getVelocity().x < 0 && charBounds.x > block.bounds.x) {
                    character.getPosition().x = block.bounds.x + block.bounds.width;
                }
            }

            // حسگر پا برای چسبیدن به زمین
            if (footSensor.overlaps(block.bounds) && !block.isDeadly) {
                character.setGrounded(true);
            }
        }

        character.getBounds().setPosition(character.getPosition().x, character.getPosition().y);
    }

    public void enemyIsDead (Enemy enemy) {
        Corpse corpse = enemy.getCorpse();
        enemy.die();
        enemies.remove(enemy);
        corpses.add(corpse);
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
