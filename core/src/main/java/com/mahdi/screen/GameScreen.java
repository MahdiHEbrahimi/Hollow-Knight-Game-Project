package com.mahdi.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mahdi.model.enums.GameAction;
import com.mahdi.model.game.CheatManager;
import com.mahdi.model.game.GameHud;
import com.mahdi.model.game.GameEngine;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.BrightnessController;
import com.mahdi.screen.manager.MusicManager;
import com.mahdi.screen.manager.PanelManager;
import com.mahdi.screen.panels.PausePanel;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;

import java.util.Random;

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
        AppStatus.setScreen(this);
    }

    @Override
    public void show() {
        super.show(); // فعال‌سازی ورودی کیبورد روی استیج اصلی (BaseScreen)

        // ساخت Stage مستقل برای HUD با یک Viewport که همیشه ثابت می‌ماند
        hudStage = new Stage(new ExtendViewport(2560, 1440));
        gameHud = new GameHud();
        multiplexer.addProcessor(hudStage);
        hudStage.addActor(gameHud);
        hudStage.addActor(BrightnessController.getInstance());
    }

    public boolean isPaused = false;
    @Override
    protected void renderScreen(float delta) {
        CheatManager.update();
        if (isPaused && GameAction.PAUSE.isJustPressed()) {
            isPaused = false;
            PanelManager.getInstance().dispose();
        } else {
            if (GameAction.PAUSE.isJustPressed()) {
                isPaused = true;
                PanelManager.getInstance().initialize(hudStage);
                PanelManager.getInstance().performPanelTransition(new PausePanel(this));
            }
        }

        if (!isPaused){
            GameEngine gameEngine = AppStatus.getGameEngine();

            // 👇 خط زیر رو دقیقاً همین‌جا اضافه کن
            AnimatedTiledMapTile.updateAnimationBaseTime();

            gameEngine.update(Math.min(delta, 1 / 30f));
        }

        // دوربین نرم دنبال‌کننده
        updateCamera(gameEngine);
        // رسم دنیای بازی با دوربین خودش
        gameBatch.setProjectionMatrix(camera.combined);
        gameEngine.draw(camera, gameBatch);
    }

    // بعد از م دنیا و استیج اصلی، HUD ثابت را رسم می‌کنیم
    @Override
    public void render(float delta) {
        super.render(delta);

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


    // شعاع منطقه‌ی مرده: تا این فاصله دوربین اصلاً تکون نمی‌خوره (نه حتی یه‌ذره)
    private static final float DEAD_ZONE = 40f;

    // از لبه‌ی دد زون تا این‌قدر فاصله‌ی اضافه، سرعت به‌صورت درجه‌دو (t^2) از MIN_LERP به MAX_LERP می‌رسه
// می‌تونی به‌جای t*t از t*t*t یا (1 - Math.exp(-3f*t)) هم استفاده کنی برای شیب تندتر/نرم‌تر
    private static final float RAMP_RANGE = 500f;

    private static final float MIN_LERP = 0.05f;
    private static final float MAX_LERP = 0.35f; // ☀️ عمداً کمتر از ۱ که حتی تو سرعت بالا هم پرش ناگهانی نده

    // سقف قطعی: دوربین هیچ‌وقت بیشتر از این مقدار از هدف عقب نمی‌مونه (صرف‌نظر از این‌که بازیکن چقدر سریع حرکت کنه)
// این عدد باید از RAMP_RANGE بزرگ‌تر باشه، وگرنه همیشه فعال می‌مونه و نرمی رو از بین می‌بره
    private static final float MAX_LAG_DISTANCE = 650f;

    // ===================== شیک دوربین =====================
    private static final float SHAKE_DURATION = 0.4f;   // ☀️ طول لرزش؛ همینجا عوضش کن
    private static final float SHAKE_MAGNITUDE = 14f;    // ☀️ شدت لرزش (پیکسل)؛ همینجا عوضش کن
    private float shakeTimer = 0f;
    private final Random random = new Random(); // import java.util.Random;

    // موقعیت «منطقی/صاف‌شده»‌ی دوربین — جدا از camera.position چون شیک نباید روی محاسبه‌ی فاصله اثر بذاره
    private boolean cameraInitialized = false;
    private float smoothCamX, smoothCamY;

    /**
     * ☀️ صدا زدنش، فارغ از هر حالتی، یه لرزش کوتاه (۰.۴ ثانیه‌ای، نه یه فریم) به دوربین می‌ده.
     * مثال استفاده: وقتی باس گرزش رو می‌کوبه زمین -> activeCameraShake();
     */
    public void activeCameraShake() {
        shakeTimer = SHAKE_DURATION;
    }

    /**
     * ☀️ یه محور (X یا Y) رو با دد زون + رشد درجه‌دوی سرعت + سقف قطعیِ عقب‌موندن، به سمت target می‌بره.
     * توجه: از دلتا استفاده نمی‌شه؛ دقیقاً مثل کد قبلی، صرفاً بر پایه‌ی فاصله محاسبه می‌شه.
     */
    private float smoothAxis(float current, float target) {
        float diff = target - current;
        float absDiff = Math.abs(diff);

        // ☀️ دد زون واقعی: داخلش هیچ حرکتی انجام نمی‌شه
        if (absDiff <= DEAD_ZONE) {
            return current;
        }

        float rampDist = absDiff - DEAD_ZONE;
        float t = Math.min(Math.max(rampDist / RAMP_RANGE, 0f), 1f);
        float lerp = MIN_LERP + (MAX_LERP - MIN_LERP) * (t * t); // رشد درجه‌دو

        float moved = current + diff * lerp;

        // ☀️ سقف قطعی: اگه بعد از این گام هنوز خیلی عقبیم، مستقیم به فاصله‌ی مجاز برش می‌گردونیم
        float newDiff = target - moved;
        if (Math.abs(newDiff) > MAX_LAG_DISTANCE) {
            moved = target - Math.signum(newDiff) * MAX_LAG_DISTANCE;
        }

        return moved;
    }

    private void updateCamera(GameEngine gameEngine) {
        if (gameEngine.getPlayer() == null) return;

        if (isPaused) {
            com.badlogic.gdx.math.Vector2 position = gameEngine.getPlayer().getPosition();
            camera.position.set((float) position.x,position.y, 0f);
            camera.update();
            return;
        }

        com.badlogic.gdx.math.Vector2 eyeSight = gameEngine.getPlayer().getEyeSight();

        // ☀️ اولین فراخوانی: مستقیم روی بازیکن اسنپ کن که یه پرش اولیه‌ی زشت نداشته باشیم
        if (!cameraInitialized) {
            smoothCamX = eyeSight.x;
            smoothCamY = eyeSight.y ;
            cameraInitialized = true;
        }

        smoothCamX = smoothAxis(smoothCamX, eyeSight.x);
        smoothCamY = smoothAxis(smoothCamY, eyeSight.y);

        // ===================== شیک (جدا از موقعیت منطقی) =====================
        float shakeOffsetX = 0f, shakeOffsetY = 0f;
        if (shakeTimer > 0f) {
            // ☀️ تنها جایی که از دلتا استفاده می‌شه — چون شمارش یه تایمر بدون منبع زمانی ممکن نیست
            shakeTimer -= Gdx.graphics.getDeltaTime();
            float fade = Math.max(shakeTimer, 0f) / SHAKE_DURATION; // لرزش کم‌کم محو می‌شه
            shakeOffsetX = (random.nextFloat() * 2f - 1f) * SHAKE_MAGNITUDE * fade;
            shakeOffsetY = (random.nextFloat() * 2f - 1f) * SHAKE_MAGNITUDE * fade;
        }

        camera.position.set(smoothCamX + shakeOffsetX, smoothCamY + shakeOffsetY, 0);
        camera.update();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
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
