package com.mahdi.screen.panels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.save.SaveManager;
import com.mahdi.model.status.AppStatus;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.ui.MenuButton;

public class StartGamePanel extends BasePanel {
    private MenuButton slot1Button, slot2Button, slot3Button, slot4Button;
    private MenuButton remove1Button, remove2Button, remove3Button, remove4Button;

    public StartGamePanel() {
        this.setFillParent(true);
        this.align(Align.center);

        // عنوان (موقتاً از لوگوی اصلی استفاده می‌کنیم، بعداً می‌توان تعویض کرد)
        Texture titleTexture = new Texture(Gdx.files.internal("MainMenu/vheart_title.png"));
        addArt(titleTexture, 670f, 820f, 1220f, 520f);
        this.padTop(600f);

        // جدول داخلی برای چیدمان اسلات‌ها و دکمه‌های Remove
        Table contentTable = new Table();
        contentTable.align(Align.center);

        float spacing = 30f;

        // ردیف اسلات ۱
        slot1Button = new MenuButton(SaveManager.getSlotInfo(1),
            FontManager.getInstance().getEnglishMenuFont(),
            () -> onSlotClicked(1));
        remove1Button = new MenuButton("REMOVE",
            FontManager.getInstance().getEnglishMenuFont(),
            () -> onRemoveSlot(1));
        remove1Button.setVisible(SaveManager.isSlotFileExists(1)); // فقط برای اسلات پر
        contentTable.add(slot1Button).width(600f).padRight(500f);
        contentTable.add(remove1Button).width(100f).padBottom(spacing).row();

        // ردیف اسلات ۲
        slot2Button = new MenuButton(SaveManager.getSlotInfo(2),
            FontManager.getInstance().getEnglishMenuFont(),
            () -> onSlotClicked(2));
        remove2Button = new MenuButton("REMOVE",
            FontManager.getInstance().getEnglishMenuFont(),
            () -> onRemoveSlot(2));
        remove2Button.setVisible(SaveManager.isSlotFileExists(2));
        contentTable.add(slot2Button).width(600f).padRight(500f);
        contentTable.add(remove2Button).width(100f).padBottom(spacing).row();

        // ردیف اسلات ۳
        slot3Button = new MenuButton(SaveManager.getSlotInfo(3),
            FontManager.getInstance().getEnglishMenuFont(),
            () -> onSlotClicked(3));
        remove3Button = new MenuButton("REMOVE",
            FontManager.getInstance().getEnglishMenuFont(),
            () -> onRemoveSlot(3));
        remove3Button.setVisible(SaveManager.isSlotFileExists(3));
        contentTable.add(slot3Button).width(600f).padRight(500f);
        contentTable.add(remove3Button).width(100f).padBottom(spacing).row();

        // ردیف اسلات ۴
        slot4Button = new MenuButton(SaveManager.getSlotInfo(4),
            FontManager.getInstance().getEnglishMenuFont(),
            () -> onSlotClicked(4));
        remove4Button = new MenuButton("REMOVE",
            FontManager.getInstance().getEnglishMenuFont(),
            () -> onRemoveSlot(4));
        remove4Button.setVisible(SaveManager.isSlotFileExists(4));
        contentTable.add(slot4Button).width(600f).padRight(500f);
        contentTable.add(remove4Button).width(100f).padBottom(spacing).row();

        this.add(new MenuButton("Start",
            FontManager.getInstance().getEnglishMenuFont(),
            this::onStart)).padTop(50f).row();

        // افزودن جدول محتوا به پنل اصلی
        this.add(contentTable).padTop(200f).row();

        // دکمه بازگشت
        this.add(new MenuButton("BACK",
            FontManager.getInstance().getEnglishMenuFont(),
            this::onBack)).padTop(50f).row();
    }

    private void onSlotClicked(int slot) {
        if (SaveManager.isSlotFileExists(slot)) {
            // اسلات پر → بارگذاری بازی
            System.out.println("[StartGame] Loading game from slot " + slot);
            // TODO: ScreenManager.getInstance().performTransition(new GameplayScreen(game, slot));
        } else {
            // اسلات خالی → ایجاد بازی جدید
            SaveManager.createNewGame(slot);
            System.out.println("[StartGame] New game created in slot " + slot);
            updateSlotVisual(slot);
        }
    }

    private void onRemoveSlot(int slot) {
        SaveManager.deleteSlot(slot);
        System.out.println("[StartGame] Slot " + slot + " deleted.");
        updateSlotVisual(slot);
    }

    private void onStart() {
        //todo
    }


    private void updateSlotVisual(int slot) {
        boolean exists = SaveManager.isSlotFileExists(slot);
        switch (slot) {
            case 1:
                slot1Button.setText(SaveManager.getSlotInfo(1));
                remove1Button.setVisible(exists);
                break;
            case 2:
                slot2Button.setText(SaveManager.getSlotInfo(2));
                remove2Button.setVisible(exists);
                break;
            case 3:
                slot3Button.setText(SaveManager.getSlotInfo(3));
                remove3Button.setVisible(exists);
                break;
            case 4:
                slot4Button.setText(SaveManager.getSlotInfo(4));
                remove4Button.setVisible(exists);
                break;
        }
    }

    private void onBack() {
        AppStatus.ChangeMenuAndPanel(MenuType.MAIN_MENU);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
