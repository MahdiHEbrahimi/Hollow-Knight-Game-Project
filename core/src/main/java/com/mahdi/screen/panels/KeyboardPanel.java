package com.mahdi.screen.panels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.mahdi.model.enums.GameAction;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.manager.PanelManager;
import com.mahdi.screen.ui.MenuButton;

import java.util.HashMap;

public class KeyboardPanel extends BasePanel {

    private final BitmapFont font;

    private final HashMap<GameAction, MenuButton> keyButtons = new HashMap<>();

    private GameAction listeningAction = null;
    private boolean toPauseMenu = false;

    public KeyboardPanel(boolean toPauseMenu) {
        BitmapFont font = FontManager.getInstance().getEnglishMenuFont();
        this(font);
        this.toPauseMenu = toPauseMenu;
    }

    public KeyboardPanel(BitmapFont font) {
        this.font = font;

        // تنظیمات جدول Scene2D
        this.setFillParent(true);
        this.center();

        // چیدمان دکمه‌های تنظیمات کیبورد
        buildKeyboardMenu();
    }

    private void buildKeyboardMenu() {
        this.clearChildren();
        keyButtons.clear();

        // ساخت یک ردیف مجزا برای هر کدام از اکشن‌های بازی
        for (final GameAction action : GameAction.values()) {
            String labelText = action.name() + ": " + action.getKeyName();

            final MenuButton btn = new MenuButton(labelText, font, new Runnable() {
                @Override
                public void run() {
                    // اگر سیستم در حال گوش دادن به کلید دیگری نیست، این اکشن را آماده تغییر کن
                    if (listeningAction == null) {
                        listeningAction = action;
                        keyButtons.get(action).setText(action.name() + ": ...");
                    }
                }
            });

            keyButtons.put(action, btn);
            this.add(btn).pad(10f).center();
            this.row();
        }

        // دکمه ریست کردن کل کیبورد به حالت پیش‌فرض
        MenuButton resetBtn = new MenuButton("RESET ALL KEYS", font, new Runnable() {
            @Override
            public void run() {
                GameAction.resetAllToDefault();
                updateAllKeyTexts();
            }
        });
        this.add(resetBtn).padTop(30f).center();

        this.add(new MenuButton("BACK",
            FontManager.getInstance().getEnglishMenuFont(),
            this::onBack)).row();

    }

    private void onBack() {
        AppStatus.ChangeMenuAndPanel(MenuType.SETTINGS);
        PanelManager.getInstance().performPanelTransition(new SettingsPanel(toPauseMenu));
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (listeningAction != null) {
            for (int keyCode = 0; keyCode < 256; keyCode++) {
                if (Gdx.input.isKeyJustPressed(keyCode)) {

                    if (keyCode == Input.Keys.ESCAPE && listeningAction != GameAction.PAUSE) {
                        updateKeyText(listeningAction);
                        listeningAction = null;
                        break;
                    }

                    listeningAction.setKey(keyCode);

                    updateKeyText(listeningAction);

                    listeningAction = null;
                    break;
                }
            }
        }
    }

    private void updateKeyText(GameAction action) {
        MenuButton btn = keyButtons.get(action);
        if (btn != null) {
            btn.setText(action.name() + ": " + action.getKeyName());
        }
    }


    private void updateAllKeyTexts() {
        for (GameAction action : GameAction.values()) {
            updateKeyText(action);
        }
    }
}
