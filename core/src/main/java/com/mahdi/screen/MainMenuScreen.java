package com.mahdi.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mahdi.HollowKnightGame;
import com.mahdi.screen.manager.MusicManager;
import com.mahdi.screen.manager.PanelManager;
import com.mahdi.screen.pannels.MainMenuPanel;

import java.util.ArrayList;

public class MainMenuScreen extends BaseScreen {
    private Stage stage;
    private Texture backgroundTexture;
    private SpriteBatch batch;

    private Texture blurParticleTexture;
    private ArrayList<VoidParticle> particles;
    private final int PARTICLE_COUNT = 50;
    private final float MAX_HEIGHT_ZONE = 1200f;

    public MainMenuScreen(HollowKnightGame game) {
        super(game);
        this.stage = new Stage(this.viewport);
        this.batch = (SpriteBatch) stage.getBatch();

        Gdx.input.setInputProcessor(stage);

        backgroundTexture = new Texture(Gdx.files.internal("MainMenu/MainMenu_BackGround.png"));

        createBlurryParticleTexture();

        MusicManager.getInstance().playMusic("MainMenu/MainMenu_BackGround.ogg");

        particles = new ArrayList<>();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new VoidParticle());
            particles.get(i).y = MathUtils.random(0f, MAX_HEIGHT_ZONE);
            if (particles.get(i).y > 850f) {
                particles.get(i).triggerFadeState();
            }
        }

        PanelManager.getInstance().initialize(stage);

        PanelManager.getInstance().performPanelTransition(new MainMenuPanel());
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
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 2560, 1440);

        for (VoidParticle p : particles) {
            p.update(delta);
            batch.setColor(1f, 1f, 1f, p.alpha);
            batch.draw(blurParticleTexture, p.x, p.y, p.size, p.size);
        }
        batch.setColor(Color.WHITE);
        batch.end();

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
        if (stage != null) {
            stage.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (blurParticleTexture != null) {
            blurParticleTexture.dispose();
        }
    }
}