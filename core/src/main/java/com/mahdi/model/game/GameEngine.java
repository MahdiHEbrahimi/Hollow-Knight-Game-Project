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
    import com.badlogic.gdx.utils.Array;
    import com.mahdi.model.characters.*;
    import com.mahdi.model.characters.enemies.Crawled;
    import com.mahdi.model.map.SolidBlock;
    import com.mahdi.model.map.TiledMapHelper;

    public class GameEngine {


        private Player player;
        private final ArrayList<Enemy> enemies;
        private final ArrayList<Corpse> corpses;

        private final TiledMap tiledMap;
        private final OrthogonalTiledMapRenderer mapRenderer;
        private final Array<SolidBlock> solidBlocks;
        private final String mapPath;

        private final int mainIdx, fg1Idx;

        public GameEngine(String mapPath) {
            this.mapPath = mapPath;
            TiledMapHelper mapHelper = new TiledMapHelper();
            this.tiledMap = mapHelper.loadMap(this.mapPath);
            this.solidBlocks = mapHelper.getSolidRectangles();
            this.mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
            this.enemies = new ArrayList<>();
            this.corpses = new ArrayList<>();

//            bg1bjIdx = tiledMap.getLayers().getIndex("bg_1");
            mainIdx  = tiledMap.getLayers().getIndex("main");
            fg1Idx   = tiledMap.getLayers().getIndex("fg_1");

            spawnPlayerFromMap();
            spawnEnemiesFromMap();
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

        public void update(float delta) {
            player.update(delta);
            handleMapCollisions(player);

            for (Corpse c : corpses) {
                c.update(delta);
            }

            // بررسی برخورد هیت‌باکس حمله شوالیه با دشمنان
            Rectangle playerAttackBox = player.getAttackHitbox();
            if (playerAttackBox != null) {
                boolean isSuccessful = false;
                for (int i = 0; i < enemies.size(); i ++) {
                    Enemy e = enemies.get(i);
                    if (e.isAlive()) {
                        if (playerAttackBox.overlaps(e.getBounds())) {
                            isSuccessful = true;
                            player.attackWasSuccessful(e);   // هر ضربه فقط یک آسیب
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

            // به‌روزرسانی دشمنان
            for (Enemy enemy : enemies) {
                enemy.update(delta);
                handleMapCollisions(enemy);
                if (enemy.getBounds().overlaps(player.getBounds()) && enemy.isAlive()) {
                    player.takeDamage(1, enemy);
                }
            }
        }

        public void draw(OrthographicCamera camera, SpriteBatch batch) {
            mapRenderer.setView(camera);

            // لایه‌های پشت
//            if (bgIdx    >= 0) mapRenderer.render(new int[]{bgIdx});
//            if (bgObjIdx >= 0) mapRenderer.render(new int[]{bgObjIdx});
            if (mainIdx  >= 0) mapRenderer.render(new int[]{mainIdx});

            // موجودات
            batch.begin();
            for (Corpse c : corpses) c.draw(batch);
            for (Enemy e : enemies) if (e.isAlive()) e.draw(batch);
            player.draw(batch);
            batch.end();

            // لایه‌های جلو
            if (fg1Idx   >= 0) mapRenderer.render(new int[]{fg1Idx});
//            if (fg2Idx   >= 0) mapRenderer.render(new int[]{fg2Idx});
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
                // ۱. تیغ‌های مرگبار (فقط بازیکن آسیب ببیند)
                if (block.isDeadly && charBounds.overlaps(block.bounds)) {
                    if (character instanceof Player) {
                        ((Player) character).takeDamageFormGround();
                    }
                    continue;   // تیغ را مانند دیوار هم در نظر نگیریم
                }

                // ۲. برخورد عمودی (فرود / سقف)
                boolean verticalResolved = false;
                if (character.getVelocity().y < 0 && charBounds.overlaps(block.bounds)
                    && charBounds.y + charBounds.height > block.bounds.y + block.bounds.height) {
                    // فرود از بالا
                    character.getPosition().y = block.bounds.y + block.bounds.height;
                    character.getVelocity().y = 0;
                    character.setGrounded(true);
                    verticalResolved = true;
                } else if (character.getVelocity().y > 0 && charBounds.overlaps(block.bounds)
                    && charBounds.y < block.bounds.y) {
                    // برخورد به سقف
                    character.getPosition().y = block.bounds.y - charBounds.height;
                    character.getVelocity().y = 0;
                    verticalResolved = true;
                }

                // ۳. برخورد افقی (همیشه، چه روی زمین چه در هوا)
                //    فقط در صورتی انجام شود که در همین فریم مشکل عمودی نداشته‌ایم
                if (!verticalResolved && charBounds.overlaps(block.bounds)) {
                    if (character.getVelocity().x > 0 && charBounds.x < block.bounds.x) {
                        character.getPosition().x = block.bounds.x - charBounds.width;
                    } else if (character.getVelocity().x < 0 && charBounds.x > block.bounds.x) {
                        character.getPosition().x = block.bounds.x + block.bounds.width;
                    }
                }

                // ۴. حسگر پا
                if (footSensor.overlaps(block.bounds) && !block.isDeadly) {
                    character.setGrounded(true);
                }
            }

            character.getBounds().setPosition(character.getPosition().x, character.getPosition().y);
        }
        public void enemyIsDead(Enemy enemy) {
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
    //        if (player != null) player.dispose();
            for (Enemy e : enemies) e.dispose();
            for (Corpse c : corpses) c.dispose();
        }
    }
