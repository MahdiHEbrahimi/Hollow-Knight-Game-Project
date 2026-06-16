package com.mahdi.controller.menuControllers;

import com.mahdi.controller.handlers.settingHandlers.BackHandler;
import com.mahdi.view.InputDTO;
import com.mahdi.view.InputDTOs.settingMenuInputDTOs.BackInputDTO;

public class SettingMenuController extends MenuController {
    private static SettingMenuController instance;

    private SettingMenuController() {}

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new SettingMenuController();
        }
        return instance;
    }

    @Override
    public void control(InputDTO input) {
        if (input instanceof BackInputDTO) {
            BackHandler.handle((BackInputDTO)input);
        }
    }
}