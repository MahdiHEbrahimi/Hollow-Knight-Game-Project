package com.mahdi.model.menus;

import com.mahdi.controller.menuControllers.MenuController;
import com.mahdi.screen.pannels.BasePanel;

public abstract class Menu {
    public abstract BasePanel getNewPanel();

    public abstract MenuController getController();
}
