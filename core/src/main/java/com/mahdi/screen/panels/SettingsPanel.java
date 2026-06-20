package com.mahdi.screen.panels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.mahdi.HollowKnightGame;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.ui.MenuButton;
import com.mahdi.screen.ui.MenuSlider;
import com.mahdi.screen.ui.SliderBinding;
import com.mahdi.screen.ui.ToggleBinding;
import com.mahdi.view.CommandSender;
import com.mahdi.view.InputDTOs.settingMenuInputDTOs.QualityChangeInputDTO;
import com.mahdi.view.InputDTOs.settingMenuInputDTOs.ResetVolumeInputDTO;

public class SettingsPanel extends BasePanel {

    public Texture soundOnTex;
    public Texture soundOffTex;

    private final MenuSlider musicSlider;
    private final MenuSlider sfxSlider;
    private final MenuSlider brightnessSlider;

    private final MenuButton qualityButton;

    public SettingsPanel() {
        this.setFillParent(true);
        this.align(Align.center);

        // عنوان
        Texture titleTexture = new Texture(Gdx.files.internal("SettingsMenu/SettingTittle.png"));
        addArt(titleTexture, 670f, 870f, 1220f, 520f);
        this.padTop(400f);

        float spacing = 40f;

        soundOnTex = new Texture(Gdx.files.internal("global/sound/sound_on.png"));
        soundOffTex = new Texture(Gdx.files.internal("global/sound/sound_off.png"));

        // ==========================================
        // ۱. اسلایدر حجم موسیقی (Music Volume)
        // ==========================================
        musicSlider = new MenuSlider("Music Volume",
                FontManager.getInstance().getEnglishMenuFont(),
                null, null, null,
                new Texture("global/button_marker.png"),
                true, // 🌟 hasIcon = true
                soundOnTex,
                soundOffTex,
                new SliderBinding() {
                    @Override
                    public int get() {
                        return AppStatus.getMusicVolume();
                    }

                    @Override
                    public void set(int value) {
                        AppStatus.setMusicVolume(value);
                    }
                },
                new ToggleBinding() {
                    @Override
                    public boolean get() {
                        return AppStatus.getMuteMusic();
                    }

                    @Override
                    public void set(boolean value) {
                        AppStatus.setMutedMusic(value);
                    }
                });
        this.add(musicSlider).padBottom(spacing - 20f).row();

        // ==========================================
        // ۲. اسلایدر حجم افکت‌ها (SFX Volume)
        // ==========================================
        sfxSlider = new MenuSlider("SFX Volume",
                FontManager.getInstance().getEnglishMenuFont(),
                null, null, null,
                new Texture("global/button_marker.png"),
                true, // 🌟 hasIcon = true
                soundOnTex,
                soundOffTex,
                new SliderBinding() {
                    @Override
                    public int get() {
                        return AppStatus.getSFXVolume();
                    }

                    @Override
                    public void set(int value) {
                        AppStatus.setSFXVolume(value);
                    }
                },
                new ToggleBinding() {
                    @Override
                    public boolean get() {
                        return AppStatus.getMuteSFX();
                    }

                    @Override
                    public void set(boolean value) {
                        AppStatus.setMutedSFX(value);
                    }
                });

        this.add(sfxSlider).padBottom(spacing - 20f).row();

        // ==========================================
        // ۳. اسلایدر روشنایی (Brightness)
        // ==========================================

        brightnessSlider = new MenuSlider("Brightness",
                FontManager.getInstance().getEnglishMenuFont(),
                null, null, null,
                new Texture("global/button_marker.png"),
                false, // 🌟 hasIcon = false (دکمه را مخفی کن)
                null,
                null,
                new SliderBinding() {
                    @Override
                    public int get() {
                        return AppStatus.getBrightness();
                    }

                    @Override
                    public void set(int value) {
                        AppStatus.setBrightness(value);
                    }
                },
                null // 🌟 نیازی به ToggleBinding نیست
        );

        this.add(brightnessSlider).padBottom(spacing - 20f).row();

        // ==========================================
        // ۴. دکمه‌های تنظیمات
        // ==========================================
        qualityButton = new MenuButton(
                "Quality: " + ((HollowKnightGame) Gdx.app.getApplicationListener()).getCurrentQuality().name(),
                FontManager.getInstance().getEnglishMenuFont(),
                this::onQualityChange);

        this.add(qualityButton).padBottom(spacing).row();

        this.add(new MenuButton("RESET SOUNDS",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onResetSounds)).padBottom(spacing).row();

        this.add(new MenuButton("KEYBOARD",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onKeyboard)).padBottom(spacing).row();

        // this.add(new MenuButton("RESET CONTROLS",
        // FontManager.getInstance().getEnglishMenuFont(),
        // this::onResetControls)).padBottom(spacing).row();

        this.add(new MenuButton("LANGUAGE",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onLanguage)).padBottom(spacing).row();

        // بازگشت
        this.add(new MenuButton("BACK",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onBack)).row();
    }

    private void onQualityChange() {
        CommandSender.send(new QualityChangeInputDTO(qualityButton));
    }

    private void onResetSounds() {
        CommandSender.send(new ResetVolumeInputDTO(musicSlider, sfxSlider, brightnessSlider));
    }

    private void onKeyboard() {
        AppStatus.ChangeMenuAndPanel(MenuType.KEYBOARD);
    }

    private void onLanguage() {
        System.out.println("[Settings] Language change.");
    }

    private void onBack() {
        AppStatus.ChangeMenuAndPanel(MenuType.MAIN_MENU);
    }

    @Override
    public void dispose() {
        // جلوگیری از مموری لیک تکستچرهای آیکون صدا
        if (soundOnTex != null)
            soundOnTex.dispose();
        if (soundOffTex != null)
            soundOffTex.dispose();
        super.dispose();
    }
}
