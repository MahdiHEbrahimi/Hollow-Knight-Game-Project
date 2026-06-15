package com.mahdi.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mahdi.HollowKnightGame;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.ui.MenuButton;
import java.util.ArrayList;

public class MainMenuScreen extends BaseScreen {
    private Stage stage;
    private Texture backgroundTexture;
    private Texture titleTexture; // Big prominent title logo asset
    private SpriteBatch batch;
    private BitmapFont font;

    private Texture blurParticleTexture;
    private ArrayList<VoidParticle> particles;
    private final int PARTICLE_COUNT = 50;
    private final float MAX_HEIGHT_ZONE = 1200f;

    public MainMenuScreen(HollowKnightGame game) {
        super(game);
        this.stage = new Stage(this.viewport);
        this.batch = (SpriteBatch) stage.getBatch();

        // Redirect input management to the stage actors so click and hover behaviors respond
        Gdx.input.setInputProcessor(stage);

        // Load background assets and cinematic main logo
        backgroundTexture = new Texture(Gdx.files.internal("MainMenu/MainMenu_BackGround.png"));
        titleTexture = new Texture(Gdx.files.internal("MainMenu/vheart_title.png"));

        // Initialize a clean font engine and scale appropriately for 1440p target space
        font = FontManager.getInstance().getEnglishMenuFont();

        // Generate particle visual textures locally
        createBlurryParticleTexture();

        // Fire background music track sequence
        com.mahdi.screen.manager.MusicManager.getInstance().playMusic("MainMenu/MainMenu_BackGround.ogg");

        // Spawn dynamic button cluster using the clean default constructor
        createMenuButtons();

        // Populate atmospheric physics particles
        particles = new ArrayList<>();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new VoidParticle());
            particles.get(i).y = MathUtils.random(0f, MAX_HEIGHT_ZONE);
            if (particles.get(i).y > 850f) {
                particles.get(i).triggerFadeState();
            }
        }
    }

    private void createMenuButtons() {
        // تنظیم موقعیت شروع و فاصله برای چیدمان عمودی تمیز ۵ دکمه
        float startY = 560f;  
        float spacing = 100f; 

        // ۱. دکمه شروع بازی
        MenuButton startBtn = new MenuButton("START GAME", font, this::onStartGamePressed);
        startBtn.setPosition(2560f / 2f - startBtn.getWidth() / 2f, startY);
        stage.addActor(startBtn);

        // ۲. دکمه تنظیمات
        MenuButton settingsBtn = new MenuButton("SETTINGS", font, this::onSettingsPressed);
        settingsBtn.setPosition(2560f / 2f - settingsBtn.getWidth() / 2f, startY - spacing);
        stage.addActor(settingsBtn);

        // ۳. دکمه راهنما
        MenuButton guideBtn = new MenuButton("GUIDE", font, this::onGuidePressed);
        guideBtn.setPosition(2560f / 2f - guideBtn.getWidth() / 2f, startY - (spacing * 2));
        stage.addActor(guideBtn);

        // ۴. دکمه دستاوردها
        MenuButton achievementsBtn = new MenuButton("ACHIEVEMENTS", font, this::onAchievementsPressed);
        achievementsBtn.setPosition(2560f / 2f - achievementsBtn.getWidth() / 2f, startY - (spacing * 3));
        stage.addActor(achievementsBtn);

        // ۵. دکمه خروج از بازی
        MenuButton quitBtn = new MenuButton("QUIT GAME", font, this::onQuitGamePressed);
        quitBtn.setPosition(2560f / 2f - quitBtn.getWidth() / 2f, startY - (spacing * 4));
        stage.addActor(quitBtn);
    }

    // کا‌لبک‌های اختصاصی دکمه‌ها همراه با TODO برای توسعه بعدی شما

    private void onStartGamePressed() {
        System.out.println("[Navigation] Game Start triggered.");
        // TODO: منطق شروع بازی یا تغییر اسکرین به حالت گیم‌پلی را اینجا بنویسید
        // ScreenManager.getInstance().performTransition(new GameplayScreen(game));
    }

    private void onSettingsPressed() {
        System.out.println("[Navigation] Settings Menu triggered.");
        // TODO: منطق باز کردن منو یا اسکرین تنظیمات (صدا و گرافیک) را اینجا بنویسید
    }

    private void onGuidePressed() {
        System.out.println("[Navigation] Guide Menu triggered.");
        // TODO: منطق باز کردن بخش راهنما یا منوی چگونگی بازی را اینجا بنویسید
    }

    private void onAchievementsPressed() {
        System.out.println("[Navigation] Achievements Menu triggered.");
        // TODO: منطق نمایش لیست دستاوردهای پلیر را اینجا بنویسید
    }

    private void onQuitGamePressed() {
        System.out.println("[System] Gracefully closing application runtime. Farewell.");
        // TODO: در صورت نیاز به سیو کردن داده‌ها قبل از خروج، کدهای آن را اینجا بنویسید
        Gdx.app.exit();
    }

    private void createBlurryParticleTexture() {
        int textureSize = 64;
        Pixmap pixmap = new Pixmap(textureSize, textureSize, Pixmap.Format.RGBA8888);
        float center = textureSize / 2f;
        float maxRadius = textureSize / 2f;

        for (int x = 0; x < textureSize; x++) {
            for (int y = 0; y < textureSize; y++) {
                float dx = center - x;
                float dy = center - y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < maxRadius) {
                    float alphaFactor = 1.0f - (distance / maxRadius);
                    alphaFactor = alphaFactor * alphaFactor;
                    pixmap.setColor(new Color(0f, 0f, 0f, alphaFactor * 0.8f));
                    pixmap.drawPixel(x, y);
                }
            }
        }
        blurParticleTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    protected void renderScreen(float delta) {
        // Phase 1: Draw underlying environment layouts and effects layers
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 2560, 1440);

        // 🌟 بزرگ‌تر کردن آرت اصلی سر صفحه (لوگو)
        // ابعاد از 900x380 به 1220x520 افزایش یافت تا جلوه بهتری در فضای 1440p داشته باشد
        // فرمول تراز وسط افقی: (1220 / 2) - (2560 / 2) = 670
        batch.draw(titleTexture, 670f, 820f, 1220f, 520f);

        for (VoidParticle p : particles) {
            p.update(delta);
            batch.setColor(1f, 1f, 1f, p.alpha);
            batch.draw(blurParticleTexture, p.x, p.y, p.size, p.size);
        }
        batch.setColor(Color.WHITE);
        batch.end();

        // Phase 2: Update matrices and execute UI rendering pipeline components
        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void hide() {
        dispose();
    }

    private class VoidParticle {
        float x, y;
        float currentSpeedY;
        float initialSpeedY;
        float size;
        float alpha;

        boolean isFading;
        float fadeProgress;
        private final float FADE_DURATION = 2.0f;

        public VoidParticle() {
            resetPosition();
        }

        public void resetPosition() {
            this.x = MathUtils.random(0f, 2560f);
            this.y = MathUtils.random(-40f, 10f);
            this.initialSpeedY = MathUtils.random(90f, 170f);
            this.currentSpeedY = this.initialSpeedY;
            this.size = MathUtils.random(12f, 42f);
            this.alpha = 1.0f;
            this.isFading = false;
            this.fadeProgress = 0f;
        }

        public void triggerFadeState() {
            this.isFading = true;
            this.fadeProgress = MathUtils.random(0f, 0.6f);
        }

        public void update(float delta) {
            y += currentSpeedY * delta;

            if (!isFading && y >= 850f) {
                isFading = true;
            }

            if (isFading) {
                fadeProgress += delta / FADE_DURATION;
                if (fadeProgress > 1.0f)
                    fadeProgress = 1.0f;

                alpha = 1.0f - fadeProgress;
                currentSpeedY = MathUtils.lerp(initialSpeedY, initialSpeedY * 0.25f, fadeProgress);
            }

            if (alpha <= 0.001f || currentSpeedY <= 0.1f || y >= MAX_HEIGHT_ZONE) {
                resetPosition();
            }
        }
    }

    @Override
    public void dispose() {
        if (stage != null)
            stage.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
        if (titleTexture != null)
            titleTexture.dispose();
        if (blurParticleTexture != null)
            blurParticleTexture.dispose();
        
    }
}