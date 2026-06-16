package com.mahdi.controller.menuControllers;

import com.mahdi.view.InputDTO;

public class PauseMenuController extends MenuController {
    private static PauseMenuController instance;

    private PauseMenuController() {}

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new PauseMenuController();
        }
        return instance;
    }

    @Override
    public void control(InputDTO input) {
        // TODO: پردازش ورودی‌های منوی توقف
        System.out.println("[PauseMenuController] Processing pause input: " + input.toString());
    }
}