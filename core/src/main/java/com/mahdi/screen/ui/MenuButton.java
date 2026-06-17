package com.mahdi.screen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mahdi.screen.manager.CursorManager;
import com.mahdi.screen.manager.SoundManager;

public class MenuButton extends Actor {
    private String text;
    private BitmapFont font;
    private Texture markerTexture;
    private Runnable clickAction;
    private GlyphLayout textLayout;

    private boolean isHovered = false;
    private float currentAlpha = 0.5f;
    private final float TARGET_HOVER_ALPHA = 1.0f;
    private final float FADE_SPEED = 5f;
    private final float PADDING = 25f;

    private static Texture defaultMarker;
    private static Sound hoverSound;
    private static Sound clickSound;

    public MenuButton(String text, BitmapFont font, Runnable clickAction) {
        this(text, font, getDefaultMarker(), clickAction);
    }

    public MenuButton(String text, BitmapFont font, Texture markerTexture, Runnable clickAction) {
        this.text = text;
        this.font = font;
        this.markerTexture = markerTexture;
        this.clickAction = clickAction;

        this.textLayout = new GlyphLayout(font, text);
        float dynamicWidth = textLayout.width + 150f;
        setSize(dynamicWidth, 50);

        addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isHovered) {
                    // getHoverSound().play();
                    // 🌟 افکت هاور با رعایت وضعیت سیستم میوت از طریق متد playSound پخش می‌شود
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
            public void clicked(InputEvent event, float x, float y) {
                // getClickSound().play();
                // 🌟 افکت کلیک با رعایت وضعیت سیستم میوت از طریق متد playSound پخش می‌شود
                SoundManager.getInstance().playSound(getClickSound());

                if (MenuButton.this.clickAction != null) {
                    MenuButton.this.clickAction.run();
                }
            }
        });
    }

    private static Texture getDefaultMarker() {
        if (defaultMarker == null) {
            defaultMarker = new Texture(Gdx.files.internal("global/button_marker.png"));
        }
        return defaultMarker;
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

    public static void disposeDefault() {
        if (defaultMarker != null) {
            defaultMarker.dispose();
            defaultMarker = null;
        }
        if (hoverSound != null) {
            hoverSound.dispose();
            hoverSound = null;
        }
        if (clickSound != null) {
            clickSound.dispose();
            clickSound = null;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float targetAlpha = isHovered ? TARGET_HOVER_ALPHA : 0.5f;
        currentAlpha += (targetAlpha - currentAlpha) * FADE_SPEED * delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        font.setColor(1f, 1f, 1f, currentAlpha);

        float textX = getX() + (getWidth() - textLayout.width) / 2f;
        float textY = getY() + (getHeight() + textLayout.height) / 2f;

        font.draw(batch, text, textX, textY);

        if (isHovered && markerTexture != null) {
            batch.setColor(1f, 1f, 1f, currentAlpha);

            float markerWidth = 70f;
            float markerHeight = 44f;
            float dynamicPadding = PADDING + 10f;
            float markerY = getY() + (getHeight() - markerHeight) / 2f;

            batch.draw(markerTexture,
                    textX - dynamicPadding - markerWidth,
                    markerY,
                    markerWidth,
                    markerHeight);

            batch.draw(markerTexture,
                    textX + textLayout.width + dynamicPadding,
                    markerY,
                    markerWidth,
                    markerHeight,
                    0, 0,
                    markerTexture.getWidth(), markerTexture.getHeight(),
                    true, false);

            batch.setColor(Color.WHITE);
        }
    }

    //Dynamic botton
    public void setText(String newText) {
        this.text = newText;
        this.textLayout.setText(font, text);
        float dynamicWidth = textLayout.width + 150f;
        setSize(dynamicWidth, getHeight());
    }
    
}