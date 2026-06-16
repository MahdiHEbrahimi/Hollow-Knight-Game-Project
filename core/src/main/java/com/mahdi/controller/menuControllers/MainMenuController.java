package com.mahdi.controller.menuControllers;

import com.mahdi.controller.handlers.mainMenuHandlers.ChangeMenuHandler;
import com.mahdi.view.InputDTO;
import com.mahdi.view.InputDTOs.mainMenuInputDTOs.*;

public class MainMenuController extends MenuController {
    private static MainMenuController instance;

    private MainMenuController() {
    }

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new MainMenuController();
        }
        return instance;
    }

    @Override
    public void control(InputDTO input) {
        if (input instanceof ChangeMenuInputDTO) {
            ChangeMenuHandler.handle((ChangeMenuInputDTO) input);
        }
    }
}