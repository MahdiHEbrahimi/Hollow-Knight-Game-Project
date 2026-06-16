package com.mahdi.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.mahdi.screen.manager.CursorManager;

public class MenuSlider extends Actor {
    // ----- ظاهر -----
    private Texture trackTexture;   // نوار پس‌زمینه (خالی)
    private Texture fillTexture;    // بخش پر شده
    private Texture knobTexture;    // دکمه‌ی کشویی
    private Texture markerTexture;  // مارکرهای تزیینی (مثل دکمه‌ها)

    // ----- مقدار -----
    private int value = 50;         // 0 تا 100
    private String valueText = "50";
    private BitmapFont font;
    private GlyphLayout textLayout;

    // ----- حالت هاور / صدا -----
    private boolean isHovered = false;
    private float hoverAlpha = 0.5f;
    private static final float TARGET_HOVER_ALPHA = 1.0f;
    private static final float FADE_SPEED = 5f;

    private static Sound hoverSound;
    private static Sound clickSound; // برای زمان شروع drag می‌توان استفاده کرد

    // ----- ابعاد داخلی -----
    private float trackHeight = 20f;
    private float knobWidth = 30f;
    private float knobHeight = 40f;
    private float padding = 10f;

    // ----- callback -----
    private ValueChangeListener listener;

    // اینترفیس ساده برای اطلاع‌رسانی تغییر مقدار
    public interface ValueChangeListener {
        void onValueChanged(int newValue);
    }

    public MenuSlider(BitmapFont font) {
        this(font, null, null, null, null, null);
    }

    public MenuSlider(BitmapFont font,
                      Texture trackTexture,
                      Texture fillTexture,
                      Texture knobTexture,
                      Texture markerTexture,
                      ValueChangeListener listener) {
        this.font = font;
        this.trackTexture = trackTexture;
        this.fillTexture = fillTexture;
        this.knobTexture = knobTexture;
        this.markerTexture = markerTexture;
        this.listener = listener;

        // اندازه پیش‌فرض (بعداً با setSize قابل تغییر است)
        setSize(400f, 60f);

        textLayout = new GlyphLayout();

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isHovered) {
                    getHoverSound().play();
                }
                isHovered = true;
                CursorManager.getInstance().setPointerMode(true);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                isHovered = false;
                CursorManager.getInstance().setPointerMode(false);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == 0) { // دکمه چپ موس
                    getClickSound().play();
                    updateValueFromMouse(x);
                    return true;
                }
                return false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                updateValueFromMouse(x);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                // می‌توان اینجا اقدام خاصی انجام داد
            }
        });
    }

    private void updateValueFromMouse(float mouseX) {
        float usableWidth = getWidth() - padding * 2f;
        float knobSpace = knobWidth / 2f;
        float minX = padding + knobSpace;
        float maxX = padding + usableWidth - knobSpace;

        float clampedX = MathUtils.clamp(mouseX, minX, maxX);
        float percent = (clampedX - minX) / (maxX - minX);
        setValue(MathUtils.round(percent * 100f));
    }

    /**
     * مقدار را تنظیم و callback را صدا می‌زند.
     */
    public void setValue(int newValue) {
        this.value = MathUtils.clamp(newValue, 0, 100);
        this.valueText = String.valueOf(value);
        textLayout.setText(font, valueText);

        if (listener != null) {
            listener.onValueChanged(value);
        }
    }

    public int getValue() {
        return value;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float targetAlpha = isHovered ? TARGET_HOVER_ALPHA : 0.5f;
        hoverAlpha += (targetAlpha - hoverAlpha) * FADE_SPEED * delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // ۱. رسم نوار خالی (track)
        if (trackTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            batch.draw(trackTexture, getX() + padding, getY() + getHeight() / 2f - trackHeight / 2f,
                    getWidth() - padding * 2f, trackHeight);
        } else {
            // رسم یک مستطیل ساده
            batch.setColor(0.3f, 0.3f, 0.3f, hoverAlpha);
            batch.draw(getTextureDummy(), getX() + padding, getY() + getHeight() / 2f - trackHeight / 2f,
                    getWidth() - padding * 2f, trackHeight);
        }

        // ۲. رسم بخش پر شده
        float fillWidth = (getWidth() - padding * 2f) * (value / 100f);
        if (fillTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            batch.draw(fillTexture, getX() + padding, getY() + getHeight() / 2f - trackHeight / 2f,
                    fillWidth, trackHeight);
        } else {
            batch.setColor(0.8f, 0.8f, 0.8f, hoverAlpha);
            batch.draw(getTextureDummy(), getX() + padding, getY() + getHeight() / 2f - trackHeight / 2f,
                    fillWidth, trackHeight);
        }

        // ۳. رسم دکمه‌ی کشویی (knob)
        float knobX = getX() + padding + fillWidth - knobWidth / 2f;
        float knobY = getY() + getHeight() / 2f - knobHeight / 2f;
        if (knobTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            batch.draw(knobTexture, knobX, knobY, knobWidth, knobHeight);
        } else {
            batch.setColor(0.9f, 0.9f, 0.9f, hoverAlpha);
            batch.draw(getTextureDummy(), knobX, knobY, knobWidth, knobHeight);
        }

        // ۴. نمایش عدد
        font.setColor(1f, 1f, 1f, hoverAlpha);
        float textX = getX() + getWidth() + 10f; // سمت راست اسلایدر
        float textY = getY() + getHeight() / 2f + textLayout.height / 2f;
        font.draw(batch, valueText, textX, textY);

        // ۵. مارکرهای تزیینی (مانند MenuButton) هنگام هاور
        if (isHovered && markerTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            float markerW = 40f;
            float markerH = 30f;
            float markerY = getY() + getHeight() / 2f - markerH / 2f;
            // مارکر سمت چپ
            batch.draw(markerTexture, getX() - markerW - 5f, markerY, markerW, markerH);
            // مارکر سمت راست
            batch.draw(markerTexture, getX() + getWidth() + 5f, markerY, markerW, markerH,
                    0, 0, markerTexture.getWidth(), markerTexture.getHeight(), true, false);
        }

        batch.setColor(Color.WHITE);
    }

    // کمکی: یک تکسچر ۱×۱ سفید برای رسم اشکال ساده
    private static Texture dummyTexture;
    private static Texture getTextureDummy() {
        if (dummyTexture == null) {
            com.badlogic.gdx.graphics.Pixmap pix = new com.badlogic.gdx.graphics.Pixmap(1, 1,
                    com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fill();
            dummyTexture = new Texture(pix);
            pix.dispose();
        }
        return dummyTexture;
    }

    // ---------- مدیریت صداها (مشابه MenuButton) ----------
    private static Sound getHoverSound() {
        if (hoverSound == null) {
            hoverSound = Gdx.audio.newSound(Gdx.files.internal("global/BottomSelection.mp3"));
        }
        return hoverSound;
    }

    private static Sound getClickSound() {
        if (clickSound == null) {
            clickSound = Gdx.audio.newSound(Gdx.files.internal("global/BottomClicked.mp3"));
        }
        return clickSound;
    }

    public static void disposeStatic() {
        if (hoverSound != null) { hoverSound.dispose(); hoverSound = null; }
        if (clickSound != null) { clickSound.dispose(); clickSound = null; }
        if (dummyTexture != null) { dummyTexture.dispose(); dummyTexture = null; }
    }
}