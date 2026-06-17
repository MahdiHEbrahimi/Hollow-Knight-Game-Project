package com.mahdi.screen.pannels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.ui.MenuButton;

public class MainMenuPanel extends BasePanel {

    public MainMenuPanel() {
        // کل صفحه را پوشش دهد و محتوا را وسط‌چین کند
        this.setFillParent(true);
        this.align(Align.center);

        // بارگذاری لوگو به‌عنوان Art
        Texture titleTexture = new Texture(Gdx.files.internal("MainMenu/vheart_title.png"));
        addArt(titleTexture, 670f, 820f, 1220f, 520f);

        // فضای خالی در بالا برای دیده شدن لوگو
        this.padTop(600f);

        float spacing = 80f; // فاصله‌ی عمودی دکمه‌ها

        // دکمه‌ها با ارجاع مستقیم به متدهای داخلی خود پنل
        this.add(new MenuButton("START GAME",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onStartGame)).padBottom(spacing).row();

        this.add(new MenuButton("SETTINGS",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onSettings)).padBottom(spacing).row();

        this.add(new MenuButton("GUIDE",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onGuide)).padBottom(spacing).row();

        this.add(new MenuButton("ACHIEVEMENTS",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onAchievements)).padBottom(spacing).row();

        this.add(new MenuButton("QUIT GAME",
                FontManager.getInstance().getEnglishMenuFont(),
                this::onQuit)).row();
    }

    // ========== رفتار هر دکمه در خود پنل ==========

    private void onStartGame() {
        // com.mahdi.view.CommandSender.send(new ChangeMenuInputDTO(MenuType.START_GAME));
        AppStatus.ChangeMenuAndPanel(MenuType.START_GAME);
    }

    private void onSettings() {
        // com.mahdi.view.CommandSender.send(new ChangeMenuInputDTO(MenuType.SETTINGS));
        AppStatus.ChangeMenuAndPanel(MenuType.SETTINGS);
    }

    private void onGuide() {
        // com.mahdi.view.CommandSender.send(new ChangeMenuInputDTO(MenuType.GUIDE));
        AppStatus.ChangeMenuAndPanel(MenuType.GUIDE);
    }

    private void onAchievements() {
        // com.mahdi.view.CommandSender.send(new ChangeMenuInputDTO(MenuType.ACHIEVEMENTS));
        AppStatus.ChangeMenuAndPanel(MenuType.ACHIEVEMENTS);
    }

    private void onQuit() {
        Gdx.app.exit();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}