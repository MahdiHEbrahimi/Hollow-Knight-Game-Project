package com.mahdi.model.status;

import com.mahdi.model.enums.MenuType;
import com.mahdi.screen.manager.PanelManager;

public class AppStatus {
    public static MenuType curreMenu;


    public static void ChangeMenuAndPanel (MenuType target) {
        curreMenu = target;
        PanelManager.getInstance().performPanelTransition(target.getNewPanel());
    }
}
