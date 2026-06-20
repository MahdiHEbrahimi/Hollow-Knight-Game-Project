package com.mahdi.screen;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mahdi.model.game.GameHud;
import com.mahdi.model.game.GameStatus;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.MusicManager;

public class GameScreen extends BaseScreen {

    private final SpriteBatch gameBatch;
    private GameHud gameHud;
    // ۱. تعریف ضریب نرمی حرکت دوربین (هر چه کوچکتر باشد، دوربین نرم‌تر و کندتر
    // تعقیب می‌کند)
    private final float LERP_FACTOR = 1f; // عددی بین 0 تا 1

    public GameScreen() {
        super(); // ساخت دوربین، ویوپورت و استیج در کلاس مادر
        this.gameBatch = new SpriteBatch();

        MusicManager.getInstance().playMusic("MainMenu/MainMenu_BackGround.ogg");
        AppStatus.setGameStatus(new GameStatus());
    }

    @Override
    public void show() {
        super.show(); // فعال‌سازی ورودی کیبورد روی استیج در کلاس مادر

        gameHud = new GameHud();
        super.stage.addActor(gameHud);
    }

    @Override
    protected void renderScreen(float delta) {
        delta = Math.min(delta, 1/600f);
        GameStatus gameStatus = AppStatus.getGameStatus();
        gameStatus.update(Math.min(delta, 1 / 30f));

        // ۲. صدا زدن متد مدیریت هوشمند و نرم دوربین
        updateCamera(gameStatus);

        gameBatch.setProjectionMatrix(camera.combined);

        gameStatus.draw(camera, gameBatch);

    }

    /**
     * مدیریت حرکت نرم دوربین (Lerp) به همراه پشتیبانی از محورهای X و Y
     */
    private void updateCamera(GameStatus gameStatus) {
        if (gameStatus.getPlayer() == null)
            return;

        // موقعیت فعلی دوربین
        float currentX = camera.position.x;
        float currentY = camera.position.y;

        // موقعیت مقصد (جایی که شوالیه قرار دارد)
        float targetX = gameStatus.getPlayer().getPosition().x;
        float targetY = gameStatus.getPlayer().getPosition().y;

        // 🌟 اعمال فرمول Lerp برای جابجایی نرم
        // دوربین در هر فریم 10% (LERP_FACTOR) از فاصله باقی‌مانده تا بازیکن را طی
        // می‌کند
        float newX = currentX + (targetX - currentX) * LERP_FACTOR;
        float newY = currentY + (targetY - currentY) * LERP_FACTOR;

        // اعمال پوزیشن‌های نرم جدید روی دوربین
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
        System.out.println("[GameScreen] Game resources disposed cleanly.");
    }
}
