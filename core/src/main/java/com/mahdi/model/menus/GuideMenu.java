package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.GuideMenuController;
import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.screen.pannels.BasePanel;

public class GuideMenu extends Menu {
    @Override
    public BasePanel getNewPanel() {
        return null;
    }

    @Override
    public MenuController getController() {
        return GuideMenuController.getInstance();
    }
}