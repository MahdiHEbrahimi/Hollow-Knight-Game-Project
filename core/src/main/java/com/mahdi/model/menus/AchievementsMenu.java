package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.AchievementsMenuController;
import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.screen.pannels.BasePanel;

public class AchievementsMenu extends Menu {
    @Override
    public BasePanel getNewPanel() {
        return null;
    }

    @Override
    public MenuController getController() {
        return AchievementsMenuController.getInstance();
    }
}