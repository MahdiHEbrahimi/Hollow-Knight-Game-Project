package com.mahdi.screen.pannels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.manager.MusicManager;
import com.mahdi.screen.ui.MenuButton;
import com.mahdi.screen.ui.MenuSlider;
import com.mahdi.screen.ui.SliderBinding;

public class SettingsPanel extends BasePanel {
    int brightness = 50;

    public SettingsPanel() {
        this.setFillParent(true);
        this.align(Align.center);

        // عنوان
        Texture titleTexture = new Texture(Gdx.files.internal("SettingsMenu/SettingTittle.png"));
        addArt(titleTexture, 670f, 870f, 1220f, 520f);
        this.padTop(400f);

        float spacing = 40f;

        // اسلایدر حجم موسیقی
        this.add(new MenuSlider("Music Volume",
                FontManager.getInstance().getEnglishMenuFont(),
                null, null, null,
                new Texture("global/button_marker.png"),
                new SliderBinding() {
                    @Override
                    public int get() {
                        return (int) (MusicManager.getInstance().getVolume() * 100);
                    }

                    @Override
                    public void set(int value) {
                        MusicManager.getInstance().setVolume(value / 100f);
                    }
                })).padBottom(spacing - 20f).row();

        // دکمه‌های صدا و کنترل
        this.add(new MenuButton("MUTE MUSIC",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onMuteMusic)).padBottom(spacing).row();

        this.add(new MenuButton("MUTE SFX",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onMuteSfx)).padBottom(spacing).row();

        this.add(new MenuButton("RESET SOUNDS",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onResetSounds)).padBottom(spacing).row();

        this.add(new MenuButton("CHANGE CONTROLS",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onChangeControls)).padBottom(spacing).row();

        this.add(new MenuButton("RESET CONTROLS",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onResetControls)).padBottom(spacing).row();

        // اسلایدر روشنایی
        this.add(new MenuSlider("Brightness Volume",
                FontManager.getInstance().getEnglishMenuFont(),
                null, null, null,
                new Texture("global/button_marker.png"),
                new SliderBinding() {
                    @Override
                    public int get() {
                        // return AppStatus.getVolume();
                        return brightness;
                    }

                    @Override
                    public void set(int value) {
                        // AppStatus.setVolume(value);
                        brightness = value;
                        System.out.println("[Settings] Brightness set to: " + value);
                    }
                })).padBottom(spacing - 20f).row();

        this.add(new MenuButton("LANGUAGE",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onLanguage)).padBottom(spacing).row();

        // بازگشت
        this.add(new MenuButton("BACK",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onBack)).row();
    }

    private void onMuteMusic() {
        System.out.println("[Settings] Mute Music toggled.");
        // TODO: قطع/وصل موسیقی
    }

    private void onMuteSfx() {
        System.out.println("[Settings] Mute SFX toggled.");
        // TODO: قطع/وصل صداهای افکت
    }

    private void onResetSounds() {
        System.out.println("[Settings] Reset Sounds to default.");
        // TODO: بازنشانی صداها
    }

    private void onChangeControls() {
        System.out.println("[Settings] Change Controls requested.");
        // TODO: باز کردن صفحه تغییر کنترل‌ها
    }

    private void onResetControls() {
        System.out.println("[Settings] Reset Controls to default.");
        // TODO: بازنشانی کنترل‌ها
    }

    private void onLanguage() {
        System.out.println("[Settings] Language change.");
        // TODO: تغییر زبان
    }

    private void onBack() {
        AppStatus.ChangeMenuAndPanel(MenuType.MAIN_MENU);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}