package com.mahdi.controller.menuControllers;

import com.mahdi.controller.handlers.settingHandlers.BackHandler;
import com.mahdi.controller.handlers.settingHandlers.QualityChangeHandler;
import com.mahdi.controller.handlers.settingHandlers.ResetVolumeHandler;
import com.mahdi.view.InputDTO;
import com.mahdi.view.InputDTOs.settingMenuInputDTOs.BackInputDTO;
import com.mahdi.view.InputDTOs.settingMenuInputDTOs.QualityChangeInputDTO;
import com.mahdi.view.InputDTOs.settingMenuInputDTOs.ResetVolumeInputDTO;

public class SettingMenuController extends MenuController {
    private static SettingMenuController instance;

    private SettingMenuController() {
    }

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new SettingMenuController();
        }
        return instance;
    }

    @Override
    public void control(InputDTO input) {
        if (input instanceof BackInputDTO) {
            BackHandler.handle((BackInputDTO) input);
        } else if (input instanceof ResetVolumeInputDTO) {
            ResetVolumeHandler.handle((ResetVolumeInputDTO) input);
        } else if (input instanceof QualityChangeInputDTO) {
            QualityChangeHandler.handle((QualityChangeInputDTO) input);
        }
    }
}