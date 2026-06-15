package com.mahdi.model.menus;

import com.mahdi.screen.BaseScreen;

public abstract class Menu {
    public abstract BaseScreen getScreen();

    public abstract Object getController();
}
