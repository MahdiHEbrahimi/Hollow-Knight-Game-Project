package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.InventoryMenuController;
import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.screen.pannels.BasePanel;

public class InventoryMenu extends Menu {
    @Override
    public BasePanel getNewPanel() {
        return null;
    }

    @Override
    public MenuController getController() {
        return InventoryMenuController.getInstance();
    }
}