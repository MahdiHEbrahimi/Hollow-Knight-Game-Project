package com.mahdi.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mahdi.model.game.GameHud;
import com.mahdi.model.game.GameEngine;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.MusicManager;

public class GameScreen extends BaseScreen {

    private final SpriteBatch gameBatch;
    private GameHud gameHud;
    private Stage hudStage;   // Stage ثابت برای HUD
    private final GameEngine gameEngine;
    private final String mapPath;
    private final String musicPath;

    public GameScreen(String mapPath, String musicPath) {
        super();
        this.gameBatch = new SpriteBatch();

        this.mapPath = mapPath;
        this.musicPath = musicPath;

        MusicManager.getInstance().playMusic(musicPath);
        gameEngine = new GameEngine(mapPath);
        AppStatus.setGameStatus(gameEngine);
    }

    @Override
    public void show() {
        super.show(); // فعال‌سازی ورودی کیبورد روی استیج اصلی (BaseScreen)

        // ساخت Stage مستقل برای HUD با یک Viewport که همیشه ثابت می‌ماند
        hudStage = new Stage(new ExtendViewport(2560, 1440));
        gameHud = new GameHud();
        hudStage.addActor(gameHud);
    }

    @Override
    protected void renderScreen(float delta) {
        GameEngine gameEngine = AppStatus.getGameEngine();
        gameEngine.update(Math.min(delta, 1 / 30f));

        // دوربین نرم دنبال‌کننده
        updateCamera(gameEngine);

        // رسم دنیای بازی با دوربین خودش
        gameBatch.setProjectionMatrix(camera.combined);
        gameEngine.draw(camera, gameBatch);
    }

    // بعد از رسم دنیا و استیج اصلی، HUD ثابت را رسم می‌کنیم
    @Override
    public void render(float delta) {
        super.render(delta);   // همه چیز را طبق BaseScreen اجرا می‌کند (renderScreen + stage.draw)

        // حالا HUD همیشه بالای همه چیز، ثابت روی صفحه
        if (hudStage != null) {
            hudStage.act(Math.min(delta, 1 / 30f));
            hudStage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (hudStage != null) {
            hudStage.getViewport().update(width, height, true);
        }
    }

    // ========== دوربین ==========
    private static final float MIN_CRITICAL_DIST = 50f;
    private static final float MAX_CRITICAL_DIST = 400f;
    private static final float MIN_LERP = 0.08f;
    private static final float MAX_LERP = 1.00f;

    private void updateCamera(GameEngine gameEngine) {
        if (gameEngine.getPlayer() == null) return;

        float currentX = camera.position.x;
        float currentY = camera.position.y;

        float targetX = gameEngine.getPlayer().getEyeSight().x;
        float targetY = gameEngine.getPlayer().getEyeSight().y;

        float diffX = targetX - currentX;
        float diffY = targetY - currentY;

        float absDiffX = Math.abs(diffX);
        float absDiffY = Math.abs(diffY);

        float lerpX = MIN_LERP;
        if (absDiffX > MIN_CRITICAL_DIST) {
            float tX = (absDiffX - MIN_CRITICAL_DIST) / (MAX_CRITICAL_DIST - MIN_CRITICAL_DIST);
            tX = Math.min(Math.max(tX, 0f), 1f);
            lerpX = MIN_LERP + (MAX_LERP - MIN_LERP) * tX;
        }

        float lerpY = MIN_LERP;
        if (absDiffY > MIN_CRITICAL_DIST) {
            float tY = (absDiffY - MIN_CRITICAL_DIST) / (MAX_CRITICAL_DIST - MIN_CRITICAL_DIST);
            tY = Math.min(Math.max(tY, 0f), 1f);
            lerpY = MIN_LERP + (MAX_LERP - MIN_LERP) * tY;
        }

        float newX = currentX + diffX * lerpX;
        float newY = currentY + diffY * lerpY;

        camera.position.set(newX, newY, 0);
        camera.update();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (gameBatch != null) {
            gameBatch.dispose();
        }
        if (hudStage != null) {
            hudStage.dispose();
        }
        if (gameEngine != null)
            gameEngine.dispose();
        System.out.println("[GameScreen] Game resources disposed cleanly.");
    }
}
