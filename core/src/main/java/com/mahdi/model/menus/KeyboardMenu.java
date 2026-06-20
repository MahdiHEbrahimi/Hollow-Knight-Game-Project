package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.KeyBoardMenuController;
import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.panels.BasePanel;
import com.mahdi.screen.panels.KeyboardPanel;

public class KeyboardMenu extends Menu {
        @Override
    public BasePanel getNewPanel() {
        return new KeyboardPanel(FontManager.getInstance().getEnglishMenuFont());
    }

    @Override
    public MenuController getController() {
        return KeyBoardMenuController.getInstance();
    }
}
