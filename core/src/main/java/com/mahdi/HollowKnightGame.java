package com.mahdi;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.mahdi.model.graphic.GraphicsQuality;
import com.mahdi.screen.MainMenuScreen;
import com.mahdi.screen.manager.*;

public class HollowKnightGame extends Game {

    private GraphicsQuality currentQuality = GraphicsQuality.Ultra_High;

    @Override
    public void create() {
        com.mahdi.model.status.Initialization.init(this);

        com.mahdi.screen.manager.ScreenManager.getInstance().init(this);

        CursorManager.getInstance();

        com.mahdi.screen.manager.ScreenManager.getInstance().startWithFadeIn(new MainMenuScreen());
    }

    @Override
    public void render() {
        com.mahdi.screen.manager.MusicManager.getInstance().update(Gdx.graphics.getDeltaTime());

        super.render();

        com.mahdi.screen.manager.ScreenManager.getInstance().updateAndRender(Gdx.graphics.getDeltaTime());

    }

    @Override
    public void dispose() {
        super.dispose();
        CursorManager.getInstance().dispose();
        FontManager.getInstance().dispose();
        ScreenManager.getInstance().dispose();
        BrightnessController.getInstance().dispose();
        MusicManager.getInstance().dispose();

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

}
