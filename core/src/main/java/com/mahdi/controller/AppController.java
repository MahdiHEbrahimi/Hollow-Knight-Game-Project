package com.mahdi.controller;

import com.mahdi.model.status.AppStatus;
import com.mahdi.view.InputDTO;

public class AppController {
    static public void control(InputDTO input) {
        AppStatus.curreMenu.getController().control(input);
    }
}
