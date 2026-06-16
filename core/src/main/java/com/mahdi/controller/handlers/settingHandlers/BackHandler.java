package com.mahdi.controller.handlers.settingHandlers;

import com.mahdi.model.enums.MenuType;
import com.mahdi.model.status.AppStatus;
import com.mahdi.view.InputDTOs.settingMenuInputDTOs.BackInputDTO;

public class BackHandler {
    public static void handle (BackInputDTO input) {
        AppStatus.ChangeMenuAndPanel(MenuType.MAIN_MENU);
    }
}
