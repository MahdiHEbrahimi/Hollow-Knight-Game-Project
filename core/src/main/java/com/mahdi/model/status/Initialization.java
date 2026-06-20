package com.mahdi.model.status;

import com.mahdi.HollowKnightGame;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.graphic.GraphicsQuality;

public class Initialization {

    public static void init(HollowKnightGame game) {
        AppStatus.hollowKnightGame = game;
        AppStatus.curreMenu = MenuType.MAIN_MENU;
        AppStatus.setQuality(GraphicsQuality.Ultra_High);
    }
}