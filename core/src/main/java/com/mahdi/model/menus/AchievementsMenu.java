package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.AchievementsMenuController;
import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.screen.panels.AchievementsPanel;
import com.mahdi.screen.panels.BasePanel;

public class AchievementsMenu extends Menu {
    @Override
    public BasePanel getNewPanel() {
        return new AchievementsPanel();
    }

    @Override
    public MenuController getController() {
        return AchievementsMenuController.getInstance();
    }
}
