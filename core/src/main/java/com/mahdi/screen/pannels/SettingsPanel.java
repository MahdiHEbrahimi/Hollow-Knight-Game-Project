package com.mahdi.screen.pannels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.manager.PanelManager;
import com.mahdi.screen.ui.MenuButton;
import com.mahdi.view.CommandSender;
import com.mahdi.view.InputDTOs.settingMenuInputDTOs.BackInputDTO;

public class SettingsPanel extends BasePanel {

    public SettingsPanel() {
        // پوشش کل صفحه و وسط‌چین کردن محتوا
        this.setFillParent(true);
        this.align(Align.center);

        // بارگذاری و افزودن آرت عنوان تنظیمات
        Texture titleTexture = new Texture(Gdx.files.internal("SettingsMenu/SettingTittle.png"));
        addArt(titleTexture, 670f, 870f, 1220f, 520f); // ابعاد و موقعیت مشابه MainMenu

        // فاصله از بالا برای دیده شدن لوگو
        this.padTop(400f);

        float spacing = 40f;

        // دکمه‌های تنظیمات با متدهای خالی داخلی
        this.add(new MenuButton("MUSIC VOLUME",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onMusicVolume)).padBottom(spacing).row();

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

        this.add(new MenuButton("BRIGHTNESS",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onBrightness)).padBottom(spacing).row();

        this.add(new MenuButton("LANGUAGE",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onLanguage)).padBottom(spacing).row();

        // دکمه بازگشت به منوی اصلی
        this.add(new MenuButton("BACK",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onBack)).row();
    }

    // ========== متدهای خالی (فعلاً فقط چاپ و TODO) ==========

    private void onMusicVolume() {
        System.out.println("[Settings] Music Volume pressed.");
        // TODO: پیاده‌سازی تغییر حجم صدا
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

    private void onBrightness() {
        System.out.println("[Settings] Brightness adjustment.");
        // TODO: تغییر روشنایی
    }

    private void onLanguage() {
        System.out.println("[Settings] Language change.");
        // TODO: تغییر زبان
    }

    private void onBack() {
        // CommandSender.send(new BackInputDTO());
        AppStatus.ChangeMenuAndPanel(MenuType.MAIN_MENU);
    }

    @Override
    public void dispose() {
        super.dispose(); // تکسچرهای Art را پاک می‌کند
    }
}