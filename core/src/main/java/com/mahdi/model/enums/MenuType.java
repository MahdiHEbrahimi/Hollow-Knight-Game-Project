package com.mahdi.model.enums;

import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.model.menus.*;
import com.mahdi.screen.BaseScreen;
import com.mahdi.screen.panels.BasePanel;

public enum MenuType {
    KEYBOARD(new KeyboardMenu()),
    MAIN_MENU(new MainMenu()),
    START_GAME(new StartGameMenu()),
    SETTINGS(new SettingMenu()),
    GUIDE(new GuideMenu()),
    ACHIEVEMENTS(new AchievementsMenu()),
    PAUSE(new PauseMenu()),
    INVENTORY(new InventoryMenu());

    public final Menu menu;

    MenuType(Menu menu) {
        this.menu = menu;
    }

    public BasePanel getNewPanel() {
        return this.menu.getNewPanel();
    }

    public MenuController getController() {
        return this.menu.getController();
    }
}