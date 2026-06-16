package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.controller.menuControllers.StartGameMenuController;
import com.mahdi.screen.pannels.BasePanel;

public class StartGameMenu extends Menu {
    @Override
    public BasePanel getNewPanel() {
        return null;
    }

    @Override
    public MenuController getController() {
        return StartGameMenuController.getInstance();
    }
}