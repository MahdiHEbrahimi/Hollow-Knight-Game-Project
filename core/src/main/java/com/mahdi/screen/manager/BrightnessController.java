package com.mahdi.screen.manager;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class BrightnessController extends Actor {
    private static BrightnessController instance;
    private Texture overlayTexture;
    
    // مقدار بین 1.0- تا 1.0+
    private float brightnessValue = 0f; 

    private BrightnessController() {
        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.WHITE);
        pix.fill();
        overlayTexture = new Texture(pix);
        pix.dispose();
    }

    public static BrightnessController getInstance() {
        if (instance == null) instance = new BrightnessController();
        return instance;
    }

    public void setBrightness(float value) {
        // محدود کردن بین 1- تا 1
        this.brightnessValue = Math.max(-1f, Math.min(value, 1f));
    }

    public float getBrightness() {
        return brightnessValue;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (brightnessValue == 0) return;

        // اگر مثبت باشد سفید، اگر منفی باشد سیاه
        Color color = (brightnessValue > 0) ? Color.WHITE : Color.BLACK;
        // آلفا معادل 0.5 در حالت بیشینه (قدر مطلقِ مقدار)
        float alpha = Math.abs(brightnessValue) * 0.5f;

        batch.setColor(color.r, color.g, color.b, alpha);
        batch.draw(overlayTexture, 0, 0, getStage().getWidth(), getStage().getHeight());
        batch.setColor(Color.WHITE);
    }
}