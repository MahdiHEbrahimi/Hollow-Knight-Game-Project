package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.controller.menuControllers.PauseMenuController;
import com.mahdi.screen.panels.BasePanel;

public class PauseMenu extends Menu {
    @Override
    public BasePanel getNewPanel() {
        return null;
    }

    @Override
    public MenuController getController() {
        return PauseMenuController.getInstance();
    }
}