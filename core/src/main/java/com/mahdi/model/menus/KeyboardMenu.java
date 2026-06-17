package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.KeyBoardMenuController;
import com.mahdi.controller.menuControllers.MainMenuController;
import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.screen.manager.FontManager;
import com.mahdi.screen.panels.BasePanel;
import com.mahdi.screen.panels.KeyboardPanel;
import com.mahdi.screen.panels.MainMenuPanel;

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
