package com.mahdi.screen.pannels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import java.util.ArrayList;

public abstract class BasePanel extends Table {

    // --- کلاس کمکی برای نگهداری اطلاعات یک تصویر ---
    public static class Art {
        public Texture texture;
        public float x, y, width, height;

        public Art(Texture texture, float x, float y, float width, float height) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private final ArrayList<Art> arts = new ArrayList<>();

    /**
     * اضافه کردن یک Art به پنل.
     */
    protected void addArt(Texture texture, float x, float y, float width, float height) {
        arts.add(new Art(texture, x, y, width, height));
    }


    public void dispose() {
        for (Art art : arts) {
            if (art.texture != null) {
                art.texture.dispose();
            }
        }
        arts.clear();

        super.clear();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // ۱. رسم تمام Artهای پنل (مثل لوگو)
        for (Art art : arts) {
            if (art.texture != null) {
                batch.setColor(1f, 1f, 1f, parentAlpha);
                batch.draw(art.texture, art.x, art.y, art.width, art.height);
            }
        }

        // ۲. رسم بچه‌ها (دکمه‌ها و هر چیزی که با addActor یا add به این Table اضافه شده)
        super.draw(batch, parentAlpha);
    }
}