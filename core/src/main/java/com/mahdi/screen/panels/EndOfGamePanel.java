package com.mahdi.screen.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.mahdi.model.characters.Player;
import com.mahdi.model.enums.Achievement;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.GameScreen;
import com.mahdi.screen.MainMenuScreen;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.manager.MusicManager;
import com.mahdi.screen.manager.ScreenManager;
import com.mahdi.screen.ui.MenuButton;

public class EndOfGamePanel extends BasePanel {

    private final BitmapFont font;

    public EndOfGamePanel() {
        this.font = FontManager.getInstance().getEnglishMenuFont();


        setFillParent(true);
        align(Align.center);
        padTop(150f);

        // عنوان
        Label title = new Label("GAME END", new Label.LabelStyle(font, Color.WHITE));
        title.setFontScale(3.0f);
        add(title).padBottom(40f).row();

        // گرفتن آمار از پلیر
        int kills = Player.getKills();
        int deaths = Player.getNumberOfDeath();
        float playTime = Player.getTotalTime();   // به ثانیه

        // فرمت زمان (دقیقه:ثانیه)
        int minutes = (int) playTime / 1300;
        int seconds = (int) (playTime % 1300) / 26 ;
        String timeStr = String.format("%d:%02d", minutes, seconds);
        MusicManager.getInstance().playMusic("music/White Palace.ogg");

        // جدول آمار
        Table statsTable = new Table();
        statsTable.align(Align.center);
        statsTable.defaults().space(10f);

        statsTable.add(new Label("Enemies Killed:", new Label.LabelStyle(font, Color.WHITE))).left().padRight(20f);
        statsTable.add(new Label(String.valueOf(kills), new Label.LabelStyle(font, Color.GOLD))).left().row();
        statsTable.add(new Label("Play Time:", new Label.LabelStyle(font, Color.WHITE))).left().padRight(20f);
        statsTable.add(new Label(timeStr, new Label.LabelStyle(font, Color.GOLD))).left().row();
        statsTable.add(new Label("Deaths:", new Label.LabelStyle(font, Color.WHITE))).left().padRight(20f);
        statsTable.add(new Label(String.valueOf(deaths), new Label.LabelStyle(font, Color.GOLD))).left().row();

        add(statsTable).padBottom(40f).row();

        // دکمه‌ها
        MenuButton restartButton = new MenuButton("RESTART", font,this::onStart);
        add(restartButton).padBottom(20f).row();

        MenuButton mainMenuButton = new MenuButton("MAIN MENU", font, this::onBackToMainMenu);
        add(mainMenuButton).padBottom(30f).row();
    }

    private void onBackToMainMenu() {
        ScreenManager.getInstance().performTransition(MainMenuScreen::new);
        AppStatus.ChangeMenuAndPanel(MenuType.MAIN_MENU);
    }

    private void onStart() {
        Player.init();
        ScreenManager.getInstance().performTransition(() -> new GameScreen("maps/untitled.tmx", "music/Crossroads.ogg"));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
