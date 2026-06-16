package com.mahdi.controller.handlers.mainMenuHandlers;


import com.mahdi.model.status.AppStatus;
import com.mahdi.view.InputDTOs.mainMenuInputDTOs.ChangeMenuInputDTO;

public class ChangeMenuHandler {
    public static void handle (ChangeMenuInputDTO input) {
        AppStatus.ChangeMenuAndPanel(input.target());
    }
}
