package com.mahdi.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.mahdi.HollowKnightGame;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.BrightnessController;

public abstract class BaseScreen implements Screen {
    protected final HollowKnightGame game;
    protected OrthographicCamera camera;
    protected FillViewport viewport;

    protected Stage stage;

    protected static final float VIRTUAL_WIDTH = 2560;
    protected static final float VIRTUAL_HEIGHT = 1440;

    public BaseScreen() {
        this.game = AppStatus.getGame();

        camera = new OrthographicCamera();
        viewport = new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);

        stage = new Stage(viewport);
        stage.addActor(BrightnessController.getInstance());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        int currentWidth = Gdx.graphics.getWidth();
        int currentHeight = Gdx.graphics.getHeight();

        Gdx.gl.glViewport(0, 0, currentWidth, currentHeight);
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (viewport.getScreenWidth() != currentWidth || viewport.getScreenHeight() != currentHeight) {
            viewport.update(currentWidth, currentHeight, true);
        }

        viewport.apply(true);
        camera.update();

        renderScreen(delta);

        if (stage != null) {
            stage.act(Math.min(delta, 1 / 30f));
            BrightnessController.getInstance().toFront();
            stage.draw();
        }
    }

    protected abstract void renderScreen(float delta);

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            System.out.println("[BaseScreen] Stage disposed safely.");
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
}
