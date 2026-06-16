package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.controller.menuControllers.SettingMenuController;
import com.mahdi.screen.pannels.BasePanel;
import com.mahdi.screen.pannels.SettingsPanel;

public class SettingMenu extends Menu {
    @Override
    public BasePanel getNewPanel() {
        return new SettingsPanel();
    }

    @Override
    public MenuController getController() {
        return SettingMenuController.getInstance();
    }
}