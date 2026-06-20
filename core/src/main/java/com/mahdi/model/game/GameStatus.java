package com.mahdi.model.game;

import java.util.ArrayList;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.mahdi.model.characters.Enemy;
import com.mahdi.model.characters.Player;
import com.mahdi.model.map.SolidBlock;
import com.mahdi.model.map.TiledMapHelper;

public class GameStatus {

    // مکان اولیه اسپان شوالیه روی مپ (می‌توانی متناسب با مپ تغییرش دهی)
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
        // مسیر فایل مپ شما (اسم مپ طبق عکسی که فرستادی newMap.tmx یا همان newMap است)
        this.tiledMap = mapHelper.loadMap("maps/newMap.tmx"); 
        
        // ۲. گرفتن تمام مستطیل‌های لایه فیزیک (logical)
        this.solidBlocks = mapHelper.getSolidRectangles();

        // ۳. ساخت رندرر اختصاصی نقشه
        this.mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        // ۴. مقداردهی اولیه کاراکترها
        this.player = new Player(spawnX, spawnY);
        this.enemies = new ArrayList<>();
    }

    public void update(float delta) {
        // ۱. آپدیت منطق داخلی و ورودی‌های کیبورد پلier
        player.update(delta);

        // ۲. 🌟 حل برخورد فیزیکی پلیر با مستطیل‌های لایه صلب مپ (AABB Map Collision)
        handleMapCollisions(player, delta);

        // ۳. آپدیت دشمنان (در آینده)
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

        // فرض اولیه بر این است که روی هواست، مگر اینکه خلافش ثابت شود
        p.setGrounded(false);

        for (SolidBlock block : solidBlocks) {
            // بررسی تداخل کادر پلیر با مستطیل صلب مپ
            if (playerBounds.overlaps(block.bounds)) {
                
                // الف) اگر تایل مرگبار بود (تیغ یا اسید کریستالی)
                if (block.isDeadly) {
                    p.die();
                    p.setHp(0); // خون شوالیه صفر شود
                    continue; 
                }

                // ب) حل برخورد فیزیکی براساس جهت سرعت (برخورد از بالا یا پایین)
                // اگر در حال سقوط بود و به مانع خورد -> فرود روی زمین
                if (p.getVelocity().y < 0 && playerBounds.y + playerBounds.height > block.bounds.y + block.bounds.height) {
                    p.getPosition().y = block.bounds.y + block.bounds.height;
                    p.getVelocity().y = 0;
                    p.setGrounded(true);
                } 
                // اگر در حال صعود بود و سرش به سقف خورد
                else if (p.getVelocity().y > 0 && playerBounds.y < block.bounds.y) {
                    p.getPosition().y = block.bounds.y - playerBounds.height;
                    p.getVelocity().y = 0;
                }
                
                // تصحیح افقی پوزیشن کادر (برخورد با دیوارهای چپ و راست)
                if (p.getVelocity().x > 0 && playerBounds.x < block.bounds.x) {
                    p.getPosition().x = block.bounds.x - playerBounds.width;
                } else if (p.getVelocity().x < 0 && playerBounds.x > block.bounds.x) {
                    p.getPosition().x = block.bounds.x + block.bounds.width;
                }
                
                // همگام‌سازی کادر برخورد با پوزیشن جدید تصحیح شده
                p.getBounds().setPosition(p.getPosition().x, p.getPosition().y);
            }
        }
    }

    public Player getPlayer() { return player; }

    // متد آزادسازی منابع سنگین نقشه از حافظه کارت گرافیک
    public void dispose() {
        if (tiledMap != null) tiledMap.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (player != null) player.dispose();
    }
}