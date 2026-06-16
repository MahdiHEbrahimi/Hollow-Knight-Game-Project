package com.mahdi.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.mahdi.screen.manager.CursorManager;
import com.mahdi.screen.manager.SoundManager; // 🌟 اضافه شدن امپورت منیجر صدا

public class MenuSlider extends Actor {
    private Texture trackTexture;
    private Texture fillTexture;
    private Texture knobTexture;
    private Texture markerTexture;

    private int value;
    private String valueText = "";
    private BitmapFont font;
    private GlyphLayout textLayout;

    private String labelText;
    private GlyphLayout labelLayout;

    private boolean isHovered = false;
    private float hoverAlpha = 0.5f;
    private static final float TARGET_HOVER_ALPHA = 1.0f;
    private static final float FADE_SPEED = 5f;

    private static Sound hoverSound;
    private static Sound clickSound;

    private float trackHeight = 20f;
    private float knobWidth = 30f;
    private float knobHeight = 40f;
    private float padding = 10f;

    private SliderBinding binding;

    public MenuSlider(String label, BitmapFont font,
                      Texture trackTexture, Texture fillTexture,
                      Texture knobTexture, Texture markerTexture,
                      SliderBinding binding) {
        this.labelText = label;
        this.font = font;
        this.trackTexture = trackTexture;
        this.fillTexture = fillTexture;
        this.knobTexture = knobTexture;
        this.markerTexture = markerTexture;
        this.binding = binding;

        this.textLayout = new GlyphLayout();
        this.labelLayout = new GlyphLayout();
        if (labelText != null && !labelText.isEmpty()) {
            labelLayout.setText(font, labelText);
        }
        
        this.setSize(400f, 100f);

        if (binding != null) {
            setValue(binding.get());
        }

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isHovered) {
                    // getHoverSound().play();
                    // 🌟 پخش افکت صوتی هوور از طریق کانال اختصاصی مجهز به سیستم میوت
                    SoundManager.getInstance().playSound(getHoverSound());
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
                if (button == 0) {
                    // getClickSound().play();
                    // 🌟 پخش افکت صوتی کلیک با رعایت تغییرات زنده وضعیت سایلنت در تنظیمات
                    SoundManager.getInstance().playSound(getClickSound());
                    updateValueFromMouse(x);
                    return true;
                }
                return false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                updateValueFromMouse(x);
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

    public void setValue(int newValue) {
        this.value = MathUtils.clamp(newValue, 0, 100);
        this.valueText = String.valueOf(value);
        if (textLayout != null && font != null) {
            textLayout.setText(font, valueText);
        }

        if (binding != null) {
            binding.set(value);
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
        float trackCenterY = getY() + 30f;

        // رسم برچسب (label) در بالای اسلایدر
        if (labelText != null && !labelText.isEmpty() && labelLayout != null) {
            font.setColor(1f, 1f, 1f, hoverAlpha);
            float labelX = getX() + (getWidth() - labelLayout.width) / 2f;
            float labelY = trackCenterY + trackHeight + 15f + labelLayout.height; 
            font.draw(batch, labelText, labelX, labelY);
        }

        // رسم track
        if (trackTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            batch.draw(trackTexture, getX() + padding, trackCenterY - trackHeight / 2f,
                    getWidth() - padding * 2f, trackHeight);
        } else {
            batch.setColor(0.3f, 0.3f, 0.3f, hoverAlpha);
            drawDummy(batch, getX() + padding, trackCenterY - trackHeight / 2f,
                    getWidth() - padding * 2f, trackHeight);
        }

        // رسم fill
        float fillWidth = (getWidth() - padding * 2f) * (value / 100f);
        if (fillTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            batch.draw(fillTexture, getX() + padding, trackCenterY - trackHeight / 2f,
                    fillWidth, trackHeight);
        } else {
            batch.setColor(0.8f, 0.8f, 0.8f, hoverAlpha);
            drawDummy(batch, getX() + padding, trackCenterY - trackHeight / 2f,
                    fillWidth, trackHeight);
        }

        // رسم knob
        float knobX = getX() + padding + fillWidth - knobWidth / 2f;
        float knobY = trackCenterY - knobHeight / 2f;
        if (knobTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            batch.draw(knobTexture, knobX, knobY, knobWidth, knobHeight);
        } else {
            batch.setColor(0.9f, 0.9f, 0.9f, hoverAlpha);
            drawDummy(batch, knobX, knobY, knobWidth, knobHeight);
        }

        // رسم عدد
        font.setColor(1f, 1f, 1f, hoverAlpha);
        float textX = getX() + getWidth() + 10f;
        float textY = trackCenterY + textLayout.height / 2f;
        font.draw(batch, valueText, textX, textY);

        // مارکرها هنگام هاور
        if (isHovered && markerTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            float markerW = 40f;
            float markerH = 30f;
            float markerY = trackCenterY - markerH / 2f;
            batch.draw(markerTexture, getX() - markerW - 5f, markerY, markerW, markerH);
            batch.draw(markerTexture, getX() + getWidth() + 5f, markerY, markerW, markerH,
                    0, 0, markerTexture.getWidth(), markerTexture.getHeight(), true, false);
        }

        batch.setColor(Color.WHITE);
    }

    private static Texture dummyTexture;
    private static void drawDummy(Batch batch, float x, float y, float w, float h) {
        if (dummyTexture == null) {
            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fill();
            dummyTexture = new Texture(pix);
            pix.dispose();
        }
        batch.draw(dummyTexture, x, y, w, h);
    }

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