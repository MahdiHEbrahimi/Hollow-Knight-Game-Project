package com.mahdi.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mahdi.HollowKnightGame;
import com.mahdi.screen.BaseScreen;

public class ScreenManager {
    private static ScreenManager instance;

    private HollowKnightGame game;
    private BaseScreen targetScreen;
    private final SpriteBatch batch;
    private final Texture blackOverlay;

    private enum TransitionState {
        NONE, FADE_OUT, FADE_IN
    }

    private TransitionState state = TransitionState.NONE;

    private float blackScreenAlpha = 0f;

    private float currentDuration = 2.5f;

    private ScreenManager() {
        batch = new SpriteBatch();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackOverlay = new Texture(pixmap);
        pixmap.dispose();
    }

    public static ScreenManager getInstance() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    public void init(HollowKnightGame game) {
        this.game = game;
    }

    public void startWithFadeIn(BaseScreen firstScreen) {
        this.targetScreen = firstScreen;
        this.game.setScreen(firstScreen);

        this.currentDuration = 5.0f;
        this.state = TransitionState.FADE_IN;
        this.blackScreenAlpha = 1.0f;
    }

    public void performTransition(BaseScreen nextScreen) {
        if (state != TransitionState.NONE) return;

        this.targetScreen = nextScreen;
        this.currentDuration = 2.5f;
        this.state = TransitionState.FADE_OUT;
        this.blackScreenAlpha = 0f;
    }

    public void updateAndRender(float delta) {
        if (state == TransitionState.NONE)
            return;

        if (state == TransitionState.FADE_OUT) {

            blackScreenAlpha += delta / currentDuration;
            if (blackScreenAlpha >= 1.0f) {
                blackScreenAlpha = 1.0f;
                game.setScreen(targetScreen);
                state = TransitionState.FADE_IN;
            }
        } else if (state == TransitionState.FADE_IN) {

            blackScreenAlpha -= delta / currentDuration;
            if (blackScreenAlpha <= 0.0f) {
                blackScreenAlpha = 0.0f;
                state = TransitionState.NONE;
                targetScreen = null;
            }
        }

        batch.begin();
        batch.setColor(1f, 1f, 1f, blackScreenAlpha);
        batch.draw(blackOverlay, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);
        batch.end();
    }

    public void dispose() {
        if (blackOverlay != null)
            blackOverlay.dispose();
        if (batch != null)
            batch.dispose();
    }
}
