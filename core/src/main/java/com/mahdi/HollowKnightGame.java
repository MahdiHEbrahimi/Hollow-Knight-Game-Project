package com.mahdi;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.mahdi.model.graphic.GraphicsQuality;
import com.mahdi.screen.MainMenuScreen;
import com.mahdi.screen.manager.CursorManager;
import com.mahdi.screen.manager.FontManager;

public class HollowKnightGame extends Game {

    private GraphicsQuality currentQuality = GraphicsQuality.Ultra_High;

    @Override
    public void create() {
        com.mahdi.screen.manager.ScreenManager.getInstance().init(this);

        updateGraphics(currentQuality);

        // Initialize the global hardware cursor system once at startup
        CursorManager.getInstance();

        com.mahdi.screen.manager.ScreenManager.getInstance().startWithFadeIn(new MainMenuScreen(this));
    }

    public void updateGraphics(GraphicsQuality quality) {
        this.currentQuality = quality;
        Gdx.graphics.setWindowedMode(quality.width, quality.height);
        Gdx.graphics.setVSync(quality.vSync);
        System.out
                .println("Graphics updated to: " + quality.name() + " (" + quality.width + "x" + quality.height + ")");
    }

    public GraphicsQuality getCurrentQuality() {
        return currentQuality;
    }

    @Override
    public void render() {
        // 1. Update background music systems
        com.mahdi.screen.manager.MusicManager.getInstance().update(Gdx.graphics.getDeltaTime());

        // 2. Render active screen (Gameplay / Menus)
        super.render();

        // 3. Render screen transitions and black fade overlays
        com.mahdi.screen.manager.ScreenManager.getInstance().updateAndRender(Gdx.graphics.getDeltaTime());

        // NOTE: No software cursor drawing here anymore! The OS handles it natively
        // now.
    }

    @Override
    public void dispose() {
        super.dispose();
        // 🌟 Dispose global managers to prevent memory leaks
        CursorManager.getInstance().dispose();
        FontManager.getInstance().dispose();
    }
}