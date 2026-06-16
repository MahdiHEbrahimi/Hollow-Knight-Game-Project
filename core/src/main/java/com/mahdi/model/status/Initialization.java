package com.mahdi.model.status;

import com.mahdi.model.enums.MenuType;

public class Initialization {

    public static void init() {
        AppStatus.curreMenu = MenuType.MAIN_MENU;
    }
}