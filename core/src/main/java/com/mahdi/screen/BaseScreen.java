package com.mahdi.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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
    // 🌟 اضافه کردن مالتی‌پلکسر برای مدیریت چند منبع ورودی به صورت همزمان
    protected InputMultiplexer multiplexer;

    protected static final float VIRTUAL_WIDTH = 2560;
    protected static final float VIRTUAL_HEIGHT = 1440;

    public BaseScreen() {
        this.game = AppStatus.getHollowKnightGame();

        camera = new OrthographicCamera();
        viewport = new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);

        stage = new Stage(viewport);
        stage.addActor(BrightnessController.getInstance());

        // 🌟 مقداردهی اولیه مالتی‌پلکسر و اضافه کردن استیج به عنوان پردازنده‌ پیش‌فرض
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
    }

    @Override
    public void show() {
        // 🌟 به جای استیج، کل مالتی‌پلکسر را به موتور بازی معرفی می‌کنیم
        Gdx.input.setInputProcessor(multiplexer);
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
        // 🌟 پاک کردن پردازنده ورودی هنگام تغییر اسکرین
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) {
            // ابتدا پردازنده‌ ورودی را بردار تا استیج درگیر رویداد جدیدی نشود
            if (Gdx.input.getInputProcessor() == multiplexer || Gdx.input.getInputProcessor() == stage) {
                Gdx.input.setInputProcessor(null);
            }

            stage.dispose();
            stage = null; // 🌟 بسیار مهم: نال کردن جلوی دیسپوز دوباره در فراخوانی‌های بعدی را می‌گیرد
            System.out.println("[BaseScreen] Stage disposed safely.");
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
