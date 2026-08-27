package com.mahdi.model.game;

import java.util.ArrayList;


import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.mahdi.model.characters.*;
import com.mahdi.model.characters.enemies.FalseKnight;
import com.mahdi.model.enums.Achievement;
import com.mahdi.model.map.SolidBlock;
import com.mahdi.model.map.TiledMapHelper;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.GameScreen;
import com.mahdi.screen.manager.MusicManager;
import com.mahdi.screen.manager.ScreenManager;
import com.mahdi.screen.manager.SoundManager;

public class GameEngine {

    private Player player;
    private float spawnX;
    private float spawnY;
    private final ArrayList<Enemy> enemies;
    private final ArrayList<BaseCharacter> NPCs = new ArrayList<>();
    private final ArrayList<Projectile> projectiles = new ArrayList<>();
    private final ArrayList<Corpse> corpses;
    private final ArrayList<Geo> geos;

    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Array<SolidBlock> solidBlocks;

    // ☀️ لایه‌های داینامیک جایگزین فیلدهای قبلی
    private final int[] bgLayers;
    private final int[] fgLayers;
    private final int mainIdx;
    private float engineTime = 0f;          // زمان سپری‌شدهٔ کلی
    private float lastTriggerTime = -2f;    // آخرین زمان فعال‌سازی یک تریگر
    private String mapPath;

    public GameEngine(String mapPath) {
        AppStatus.setGameStatus(this);
        TiledMapHelper mapHelper = new TiledMapHelper();
        this.tiledMap = mapHelper.loadMap(mapPath);
        this.solidBlocks = mapHelper.getSolidRectangles();
        this.mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        this.enemies = new ArrayList<>();
        this.corpses = new ArrayList<>();
        this.geos = new ArrayList<>();
        this.mapPath = mapPath;

        // ☀️ جمع‌آوری خودکار لایه‌های پس‌زمینه (bg_1 تا bg_10)
        IntArray bgIndices = new IntArray();
        for (int i = 1; i <= 10; i++) {
            int idx = tiledMap.getLayers().getIndex("bg_" + i);
            if (idx >= 0) bgIndices.add(idx);
        }
        bgLayers = bgIndices.toArray();

        mainIdx = tiledMap.getLayers().getIndex("main");

        // ☀️ جمع‌آوری خودکار لایه‌های پیش‌زمینه (fg_1 تا fg_10)
        IntArray fgIndices = new IntArray();
        for (int i = 1; i <= 10; i++) {
            int idx = tiledMap.getLayers().getIndex("fg_" + i);
            if (idx >= 0) fgIndices.add(idx);
        }
        fgLayers = fgIndices.toArray();

        spawnPlayerFromMap();
        spawnEnemiesFromMap();
        spawnNPCs();
    }

    private void spawnPlayerFromMap() {
        float finalSpawnX = 0;
        float finalSpawnY = 0;

        MapLayer layer = tiledMap.getLayers().get("logical");
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                if ("SpawnPlayer".equals(object.getName())) {
                    if (object instanceof PointMapObject point) {
                        finalSpawnX = point.getPoint().x;
                        finalSpawnY = point.getPoint().y;
                    }
                    System.out.println("[GameEngine] Smart Spawn Point Found! X=" + finalSpawnX + " Y=" + finalSpawnY);
                    spawnX = finalSpawnX;
                    spawnY = finalSpawnY;
                    break;
                }
            }
        }

        this.player = new Player(finalSpawnX, finalSpawnY);
    }

    private void spawnEnemiesFromMap() {
        float finalSpawnX = 0;
        float finalSpawnY = 0;

        MapLayer layer = tiledMap.getLayers().get("logical");
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                Enemy enemy = null;

                if (object instanceof PointMapObject point) {
                    finalSpawnX = point.getPoint().x;
                    finalSpawnY = point.getPoint().y;
                }

                for (EnemyType enemyType : EnemyType.values()) {
                    if (enemyType.getName().equals(object.getName())) {
                        enemy = enemyType.getInstance(finalSpawnX, finalSpawnY, this.player);
                        break;
                    }
                }

                if (enemy != null)
                    enemies.add(enemy);
            }
        }
    }

    private void spawnNPCs() {
        float finalSpawnX = 0;
        float finalSpawnY = 0;

        MapLayer layer = tiledMap.getLayers().get("logical");
        if (layer != null) {
            for (MapObject object : layer.getObjects()) {
                BaseCharacter npc = null;

                if (object instanceof PointMapObject point) {
                    finalSpawnX = point.getPoint().x;
                    finalSpawnY = point.getPoint().y;
                }

                for (NPCtype npcType : NPCtype.values()) {
                    if (npcType.getName().equals(object.getName())) {
                        npc = npcType.getInstance(finalSpawnX, finalSpawnY, player);
                        break;
                    }
                }

                if (npc != null)
                    NPCs.add(npc);
            }
        }
    }

    public void respawnPlayer() {
        player = new Player(spawnX, spawnY);
    }

    private void respawnPlayer(int id) {
        MapLayer layer = tiledMap.getLayers().get("logical");
        if (layer == null) return;

        int mapHeightTiles = tiledMap.getProperties().get("height", Integer.class);
        int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
        float mapTotalHeight = mapHeightTiles * tileHeight;

        for (MapObject obj : layer.getObjects()) {
            if (("respawn" + id).equals(obj.getName()) && obj instanceof PointMapObject) {
                PointMapObject point = (PointMapObject) obj;
                float x = point.getPoint().x;
                float y = point.getPoint().y;   // تبدیل Y

                player.getPosition().set(x, y);
                player.getBounds().setPosition(x, y);
                player.getVelocity().set(0, 0);

                break;
            }
        }
    }

    public void update(float delta) {
        engineTime += delta;
        player.update(delta);
        handleMapCollisions(player);

        handleGeos(delta);

        for (Corpse c : corpses)
            c.update(delta);

        for (BaseCharacter npc : NPCs) {
            npc.update(delta);
            handleMapCollisions(npc);
        }

        try {
            for (Projectile p : projectiles) {
                p.update(delta);
                if (p.damagesPlayer()) {
                    if (p.getBounds().overlaps(player.getBounds()))
                        player.takeDamage(1, null);
                    if (!p.isActive())
                        projectiles.remove(p);
                } else {
                    for (Enemy e : enemies) {
                        if (p.getBounds().overlaps(e.getBounds())) {
                            if (!(e instanceof FalseKnight)) {
                                e.takeDamage(3);
                                player.increaseSoul(11f);
                            } else {
                                e.takeDamage(3);

                                player.increaseSoul(11f);
                                projectiles.remove(p);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }


        Rectangle playerAttackBox = player.getAttackHitbox();
        if (playerAttackBox != null) {
            boolean isSuccessful = false;
            for (int i = 0; i < enemies.size(); i++) {
                Enemy e = enemies.get(i);
                if (e.isAlive()) {
                    if (playerAttackBox.overlaps(e.getBounds())) {
                        isSuccessful = true;
                        player.attackWasSuccessful(e);
                        if (!e.isAlive()) i--;
                    }
                }
            }
            if (isSuccessful)
                player.clearAttackHitBox();
        }

        for (Corpse corpse : corpses) {
            handleMapCollisions(corpse);
        }

        try {
            for (Enemy enemy : enemies) {
                enemy.update(delta);
                handleMapCollisions(enemy);
                if (enemy.getBounds().overlaps(player.getBounds()) && enemy.isAlive()) {
                    player.takeDamage(1, enemy);
                }
            }
        } catch (Exception e) {

        }
        if (enemies.isEmpty())
            Achievement.HUNTER.setActive(true);

        projectiles.removeIf(p -> !p.isActive());
    }

    // ☀️ متد اصلی مدیریت سکه‌ها
    private void handleGeos(float delta) {
        for (int i = geos.size() - 1; i >= 0; i--) {
            Geo g = geos.get(i);
            g.update(delta);
            applyGeoCollisions(g);

            if (player.getBounds().overlaps(g.getBounds())) {
                player.increaseGeo(1);
                geos.remove(i);
                continue;
            }

            if (!g.isAlive()) {
                geos.remove(i);
            }
        }
    }

    // ☀️ متد کمکی برای برخورد فیزیکی سکه‌ها با زمین
    private void applyGeoCollisions(Geo geo) {
        Rectangle geoBounds = geo.getBounds();
        boolean grounded = false;

        for (SolidBlock block : solidBlocks) {
            if (block.isDeadly) continue;

            if (geo.getVelocity().y < 0 && geoBounds.overlaps(block.bounds)
                && geoBounds.y + geoBounds.height > block.bounds.y + block.bounds.height) {
                geo.getPosition().y = block.bounds.y + block.bounds.height;
                geo.getVelocity().y = 0;
                grounded = true;
                break;
            }
        }

        if (grounded) {
            geo.getVelocity().x *= 0.85f;
            if (Math.abs(geo.getVelocity().x) < 1f) geo.getVelocity().x = 0;
        }

        geo.setGrounded(grounded);
        geoBounds.setPosition(geo.getPosition().x, geo.getPosition().y);
    }

    public void draw(OrthographicCamera camera, SpriteBatch batch) {
        mapRenderer.setView(camera);

        // ☀️ رسم تمام لایه‌های پس‌زمینه (اگر وجود داشته باشند)
        if (bgLayers.length > 0) mapRenderer.render(bgLayers);

        // ☀️ لایه اصلی
        if (mainIdx >= 0) mapRenderer.render(new int[]{mainIdx});

        // موجودات
        batch.begin();
        for (Corpse c : corpses) c.draw(batch);
        for (Enemy e : enemies) if (e.isAlive()) e.draw(batch);
        for (Geo g : geos) g.draw(batch);
        for (Projectile p : projectiles) p.draw(batch);
        for (BaseCharacter npc : NPCs) npc.draw(batch);
        player.draw(batch);
        batch.end();

        // ☀️ رسم تمام لایه‌های پیش‌زمینه (اگر وجود داشته باشند)
        if (fgLayers.length > 0) mapRenderer.render(fgLayers);
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

            // ───── بلوک‌های تریگر (غیر فیزیکی) ─────
            if ("musicChange".equals(block.type) || "screenChange".equals(block.type)) {
                // فقط برای بازیکن فعال شوند
                if (character instanceof Player && charBounds.overlaps(block.bounds)) {
                    if (engineTime - lastTriggerTime >= 0.001f) {
                        lastTriggerTime = engineTime;
                        if ("musicChange".equals(block.type)) {
                            onMusicTrigger(block.musicPath);
                        } else {
                            onScreenTrigger(block.mapPath, block.musicPath);
                        }
                    }
                }
                continue;   // ← بدون هیچ‌گونه برخورد فیزیکی، مستقیماً ادامه بده
            }

            // ═══════════ بلوک‌های فیزیکی (بدون تغییر) ═══════════
            if (block.isDeadly && charBounds.overlaps(block.bounds)) {
                if (character instanceof Player) {
                    if (((Player) character).getAttackHitbox() != null
                        && ((Player) character).getAttackHitbox().overlaps(block.bounds)) {
                        character.setThrown(new Vector2(0f, 5000f));
                        ((Player) character).setHasDoubleJump(true);
                    }
                    else {
                        ((Player) character).takeDamageFormGround();

                        if (block.respawnId >= 0)
                            respawnPlayer(block.respawnId);
                    }
                } else if (character instanceof Enemy) {
                    if (character.isAlive())
                        ((Enemy) character).takeDamage(1);
                    player.increaseSoul(11f);
                }
            }

            if ("wall".equals(block.type)) {
                boolean verticalResolved = false;

                if (character.getVelocity().y < 0 && charBounds.overlaps(block.bounds)
                    && charBounds.y + charBounds.height > block.bounds.y + block.bounds.height) {
                    character.getPosition().y = block.bounds.y + block.bounds.height;
                    character.getVelocity().y = 0;
                    character.setGrounded(true);
                    verticalResolved = true;
                } else if (character.getVelocity().y > 0 && charBounds.overlaps(block.bounds)
                    && charBounds.y < block.bounds.y) {
                    character.getPosition().y = block.bounds.y - charBounds.height;
                    character.getVelocity().y = 0;
                    verticalResolved = true;
                }

                if (!verticalResolved && charBounds.overlaps(block.bounds)) {
                    if (character.getVelocity().x > 0 && charBounds.x < block.bounds.x) {
                        character.getPosition().x = block.bounds.x - charBounds.width;
                    } else if (character.getVelocity().x < 0 && charBounds.x > block.bounds.x) {
                        character.getPosition().x = block.bounds.x + block.bounds.width;
                    }
                }
            } else if ("ground".equals(block.type)) {
                if (character.getVelocity().y < 0 && charBounds.overlaps(block.bounds)
                    && charBounds.y + charBounds.height > block.bounds.y + block.bounds.height) {
                    character.getPosition().y = block.bounds.y + block.bounds.height;
                    character.getVelocity().y = 0;
                    character.setGrounded(true);
                }
            }

            if (footSensor.overlaps(block.bounds) && !block.isDeadly) {
                if ("wall".equals(block.type)) {
                    if (character.getBounds().y >= block.bounds.y + block.bounds.height - 0.5f) {
                        character.setGrounded(true);
                    }
                } else {
                    character.setGrounded(true);
                }
            }
        }

        character.getBounds().setPosition(character.getPosition().x, character.getPosition().y);
    }

    private void onMusicTrigger(String musicPath) {
        MusicManager.getInstance().playMusic(musicPath);
    }

    private void onScreenTrigger(String mapPath, String musicPath) {
        ScreenManager.getInstance().performTransition(() -> new GameScreen(mapPath, musicPath));
    }

    public void enemyIsDead(Enemy enemy) {
        Player.increaseKill();
        SoundManager.getInstance().playSFX("SFX/uumuu_helper_slash.mp3");
        Vector2 position = enemy.getPosition();
        Corpse corpse = enemy.getCorpse();
        enemy.die();
        enemies.remove(enemy);
        corpse.setThrown(new Vector2(0f, 800f));
        corpses.add(corpse);
        for (int i = 0; i < 3; i++) {
            geos.add(new Geo(position.x, position.y));
        }
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }

    public String getMapPath() {
        return mapPath;
    }

    public void addProjectile(Projectile projectile) {
        projectiles.add(projectile);
    }

    public Array<SolidBlock> getSolidBlocks() {
        return solidBlocks;
    }

    public void dispose() {
        if (tiledMap != null) tiledMap.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        for (Enemy e : enemies) e.dispose();
        for (Corpse c : corpses) c.dispose();
    }
}
