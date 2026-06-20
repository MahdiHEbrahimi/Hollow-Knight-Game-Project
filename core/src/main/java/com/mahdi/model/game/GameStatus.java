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
import com.mahdi.model.map.SolidBlock;
import com.mahdi.model.map.TiledMapHelper;

public class GameStatus {

    // مختصات پیش‌فرض عددی شما (در صورت پیدا نشدن آبجکت اسپان استفاده می‌شود)
    private final float spawnX = 641f;
    private final float spawnY = 801f;

    private final Player player;
    private final ArrayList<Enemy> enemies;

    // ابزارهای مدیریت و رندر نقشه تایل
    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Array<SolidBlock> solidBlocks;

    public GameStatus() {
        // ۱. لود کردن مپ با استفاده از هلپری که TA داده است
        TiledMapHelper mapHelper = new TiledMapHelper();
        this.tiledMap = mapHelper.loadMap("maps/untitled.tmx");

        // ۲. گرفتن تمام مستطیل‌های لایه فیزیک (logical)
        this.solidBlocks = mapHelper.getSolidRectangles();

        // ۳. ساخت رندرر اختصاصی نقشه
        this.mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        this.enemies = new ArrayList<>();

        // ۴. 🌟 منطق هوشمند پیدا کردن آبجکت SpawnPlayer از لایه لوجیکال مپ
        float finalSpawnX = spawnX;
        float finalSpawnY = spawnY;

        MapLayer layer = tiledMap.getLayers().get("logical");
        if (layer != null) {
            // به دست آوردن ارتفاع کل مپ به پیکسل جهت تصحیح احتمال معکوس بودن محور Y
            int mapHeightInTiles = tiledMap.getProperties().get("height", Integer.class);
            int tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);
            float totalMapHeight = mapHeightInTiles * tileHeight;

            for (MapObject object : layer.getObjects()) {
                if ("SpawnPlayer".equals(object.getName()) && object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    finalSpawnX = rect.x;

                    // 🌟 فرمول اصلاح محور Y بر اساس مبدا مختصات LibGDX
                    finalSpawnY = totalMapHeight - rect.y - rect.height;

                    System.out.println("[GameStatus] Smart Spawn Point Found! X=" + finalSpawnX + " Y=" + finalSpawnY);
                    break;
                }
            }
        }

        // ۵. مقداردهی نهایی پلیر با پوزیشن تصحیح شده مپ
        this.player = new Player(finalSpawnX, finalSpawnY);
    }

    public void update(float delta) {

        // ۱. آپدیت منطق داخلی و ورودی‌های کیبورد پلیر
        player.update(delta);

        // ۲. حل برخورد فیزیکی پلیر با مستطیل‌های لایه صلب مپ
        handleMapCollisions(player, delta);

        // ۳. آپدیت دشمنان
        for (Enemy enemy : enemies) {
            enemy.update(delta);
        }
    }

    public void draw(OrthographicCamera camera, SpriteBatch batch) {
        // ۲. تنظیم دوربین روی رندرر نقشه و رسم لایه‌های گرافیکی مپ Crystal Peak
        mapRenderer.setView(camera);
        mapRenderer.render();

        // ۳. باز کردن مجدد بچ اصلی برای رسم مستطیل‌های دیباگ و کاراکترها
        batch.begin();

        // ۴. رسم کاراکترها (مستطیل‌های استیت پلیر و دشمنان)
        for (Enemy e : enemies) {
            e.draw(batch);
        }
        player.draw(batch);
        batch.end();
    }

    /**
     * 🌟 مغز متفکر برخورد فیزیکی شوالیه با لایه صلب و مناطق مرگبار مپ
     */
private void handleMapCollisions(Player p, float delta) {
    Rectangle playerBounds = p.getBounds();
    p.setGrounded(false);

    // 🌟 حسگر پا: یک مستطیل بسیار کوتاه درست زیر بدنه اصلی
    float sensorHeight = 2f;
    Rectangle footSensor = new Rectangle(
        playerBounds.x,
        playerBounds.y - sensorHeight,
        playerBounds.width,
        sensorHeight
    );

    for (SolidBlock block : solidBlocks) {
        // --- بلوک‌های مرگبار ---
        if (block.isDeadly && playerBounds.overlaps(block.bounds)) {
            p.die();
            p.setHp(0);
            continue;
        }

        // --- برخورد عمودی از بالا (فرود) ---
        if (p.getVelocity().y < 0 && playerBounds.overlaps(block.bounds)
                && playerBounds.y + playerBounds.height > block.bounds.y + block.bounds.height) {
            p.getPosition().y = block.bounds.y + block.bounds.height;
            p.getVelocity().y = 0;
            p.setGrounded(true);
        }
        // --- برخورد عمودی از پایین (برخورد سر به سقف) ---
        else if (p.getVelocity().y > 0 && playerBounds.overlaps(block.bounds)
                && playerBounds.y < block.bounds.y) {
            p.getPosition().y = block.bounds.y - playerBounds.height;
            p.getVelocity().y = 0;
        }

        // --- تصحیح افقی (فقط وقتی که روی این بلوک زمین‌گیر نشده‌ایم) ---
        if (!p.isGrounded() && playerBounds.overlaps(block.bounds)) {
            if (p.getVelocity().x > 0 && playerBounds.x < block.bounds.x) {
                p.getPosition().x = block.bounds.x - playerBounds.width;
            } else if (p.getVelocity().x < 0 && playerBounds.x > block.bounds.x) {
                p.getPosition().x = block.bounds.x + block.bounds.width;
            }
        }

        // 🌟 چک حسگر پا (برای ماندگاری grounded)
        if (footSensor.overlaps(block.bounds) && !block.isDeadly) {
            p.setGrounded(true);
        }
    }

    // همگام‌سازی نهایی کادر با موقعیت تصحیح‌شده
    p.getBounds().setPosition(p.getPosition().x, p.getPosition().y);
}
    public Player getPlayer() {
        return player;
    }

    public void dispose() {
        if (tiledMap != null)
            tiledMap.dispose();
        if (mapRenderer != null)
            mapRenderer.dispose();
        if (player != null)
            player.dispose();
    }
}