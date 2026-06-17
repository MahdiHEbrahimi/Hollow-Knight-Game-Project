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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.mahdi.screen.manager.CursorManager;
import com.mahdi.screen.manager.SoundManager;

public class MenuSlider extends Actor {
    private Texture trackTexture;
    private Texture fillTexture;
    private Texture knobTexture;
    private Texture markerTexture;

    // متغیرهای مربوط به آیکون تنظیم وضعیت
    private boolean hasIcon;
    private Texture iconOnTexture;
    private Texture iconOffTexture;
    private Rectangle iconBounds;
    private float iconSize = 72f;
    private ToggleBinding toggleBinding;
    private int preMuteValue = 50;

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
    private float padding = 15f;

    private SliderBinding binding;

    public MenuSlider(String label, BitmapFont font,
                      Texture trackTexture, Texture fillTexture,
                      Texture knobTexture, Texture markerTexture,
                      boolean hasIcon, Texture iconOnTexture, Texture iconOffTexture, // 🌟 اضافه شدن آیکون‌ها و بولین
                      SliderBinding binding, ToggleBinding toggleBinding) { // 🌟 اضافه شدن بایندینگ میوت
        this.labelText = label;
        this.font = font;
        this.trackTexture = trackTexture;
        this.fillTexture = fillTexture;
        this.knobTexture = knobTexture;
        this.markerTexture = markerTexture;
        
        this.hasIcon = hasIcon;
        this.iconOnTexture = iconOnTexture;
        this.iconOffTexture = iconOffTexture;
        this.binding = binding;
        this.toggleBinding = toggleBinding;
        
        this.iconBounds = new Rectangle();
        this.textLayout = new GlyphLayout();
        this.labelLayout = new GlyphLayout();
        if (labelText != null && !labelText.isEmpty()) {
            labelLayout.setText(font, labelText);
        }
        
        this.setSize(450, 110f);

        if (binding != null) {
            setValue(binding.get());
            if (value > 0) preMuteValue = value;
        }

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isHovered) {
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
                    SoundManager.getInstance().playSound(getClickSound());
                    
                    // بررسی کلیک روی آیکون
                    if (hasIcon && iconBounds.contains(x, y)) {
                        if (MenuSlider.this.toggleBinding != null) {
                            boolean currentState = MenuSlider.this.toggleBinding.get();
                            MenuSlider.this.toggleBinding.set(!currentState);
                            
                            if (!currentState) { // اگر میوت شد
                                if (value > 0) preMuteValue = value;
                                setValue(0);
                            } else { // اگر از میوت درآمد
                                setValue(preMuteValue > 0 ? preMuteValue : 50);
                            }
                        }
                    } else {
                        updateValueFromMouse(x);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                // هنگام درگ کردن، اگر روی آیکون نبودیم اسلایدر آپدیت شود
                if (!(hasIcon && iconBounds.contains(x, y))) {
                    updateValueFromMouse(x);
                }
            }
        });
    }

    private void updateValueFromMouse(float mouseX) {
        // محاسبه فضای اشغال شده توسط آیکون در صورت وجود
        float sliderLeftOffset = hasIcon ? (iconSize + 15f) : 0f;
        float usableWidth = getWidth() - padding * 2f - sliderLeftOffset;
        float knobSpace = knobWidth / 2f;
        
        float minX = padding + sliderLeftOffset + knobSpace;
        float maxX = padding + sliderLeftOffset + usableWidth - knobSpace;

        float clampedX = MathUtils.clamp(mouseX, minX, maxX);
        float percent = (clampedX - minX) / (maxX - minX);
        int newValue = MathUtils.round(percent * 100f);
        
        setValue(newValue);

        // آپدیت خودکار سیستم میوت اگر اسلایدر دستی روی صفر یا بیشتر از صفر رفت
        if (hasIcon && toggleBinding != null) {
            if (newValue == 0) {
                toggleBinding.set(true);
            } else {
                toggleBinding.set(false);
                preMuteValue = newValue;
            }
        }
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
        float trackCenterY = getY() + 40f;
        float sliderLeftOffset = hasIcon ? (iconSize + 15f) : 0f;

        // رسم برچسب (label) در بالای اسلایدر
        if (labelText != null && !labelText.isEmpty() && labelLayout != null) {
            font.setColor(1f, 1f, 1f, hoverAlpha);
            float labelX = getX() + (getWidth() - labelLayout.width) / 2f;
            float labelY = trackCenterY + trackHeight + 20f + labelLayout.height; 
            font.draw(batch, labelText, labelX, labelY);
        }

        // رسم آیکون سمت چپ (در صورت فعال بودن بولین)
        if (hasIcon) {
            // آپدیت محدوده کلیک آیکون (نسبت به خود اکتور)
            iconBounds.set(padding, 40f - iconSize / 2f, iconSize, iconSize);
            
            boolean isMuted = toggleBinding != null ? toggleBinding.get() : (value == 0);
            Texture currentIcon = isMuted ? iconOffTexture : iconOnTexture;
            
            if (currentIcon != null) {
                batch.setColor(1f, 1f, 1f, hoverAlpha);
                batch.draw(currentIcon, getX() + iconBounds.x, getY() + iconBounds.y, iconBounds.width, iconBounds.height);
            }
        }

        // محاسبه مختصات شروع بدنه اسلایدر
        float startX = getX() + padding + sliderLeftOffset;
        float trackW = getWidth() - padding * 2f - sliderLeftOffset;

        // رسم track
        if (trackTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            batch.draw(trackTexture, startX, trackCenterY - trackHeight / 2f, trackW, trackHeight);
        } else {
            batch.setColor(0.3f, 0.3f, 0.3f, hoverAlpha);
            drawDummy(batch, startX, trackCenterY - trackHeight / 2f, trackW, trackHeight);
        }

        // رسم fill
        float fillWidth = trackW * (value / 100f);
        if (fillTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            batch.draw(fillTexture, startX, trackCenterY - trackHeight / 2f, fillWidth, trackHeight);
        } else {
            batch.setColor(0.8f, 0.8f, 0.8f, hoverAlpha);
            drawDummy(batch, startX, trackCenterY - trackHeight / 2f, fillWidth, trackHeight);
        }

        // رسم knob
        float knobX = startX + fillWidth - knobWidth / 2f;
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
        // 🌟 انتقال عدد به ۱۵ پیکسل راست‌تر (مقدار 10f به 25f تغییر کرد)
        float textX = getX() + getWidth() + 25f; 
        float textY = trackCenterY + textLayout.height / 2f;
        font.draw(batch, valueText, textX, textY);

        // مارکرها هنگام هاور
        if (isHovered && markerTexture != null) {
            batch.setColor(1f, 1f, 1f, hoverAlpha);
            float markerW = 40f;
            float markerH = 30f;
            float markerY = trackCenterY - markerH / 2f;
            batch.draw(markerTexture, getX() - markerW - 5f, markerY, markerW, markerH);
            batch.draw(markerTexture, textX + textLayout.width + 10f, markerY, markerW, markerH,
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