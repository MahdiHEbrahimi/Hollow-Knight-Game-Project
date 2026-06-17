package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.controller.menuControllers.StartGameMenuController;
import com.mahdi.screen.panels.BasePanel;
import com.mahdi.screen.panels.StartGamePanel;

public class StartGameMenu extends Menu {
    @Override
    public BasePanel getNewPanel() {
        return new StartGamePanel();
    }

    @Override
    public MenuController getController() {
        return StartGameMenuController.getInstance();
    }
}