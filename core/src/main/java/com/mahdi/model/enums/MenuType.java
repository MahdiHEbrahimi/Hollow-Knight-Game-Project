package com.mahdi.model.enums;

import com.mahdi.model.menus.Menu;
import com.mahdi.model.menus.MainMenu;
import com.mahdi.screen.BaseScreen;

public enum MenuType {
    MAIN_MENU(new MainMenu());

    public final Menu menu;

    MenuType(Menu menu) {
        this.menu = menu;
    }

    public BaseScreen getScreen() {
        return this.menu.getScreen();
    }

    public Object getController() {
        return this.menu.getController();
    }
}