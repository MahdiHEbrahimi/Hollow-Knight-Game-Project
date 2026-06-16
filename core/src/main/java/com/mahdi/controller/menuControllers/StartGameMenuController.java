package com.mahdi.controller.menuControllers;

import com.mahdi.view.InputDTO;

public class StartGameMenuController extends MenuController {
    private static StartGameMenuController instance;

    private StartGameMenuController() {}

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new StartGameMenuController();
        }
        return instance;
    }

    @Override
    public void control(InputDTO input) {
        // TODO: پردازش ورودی‌های منوی شروع بازی
        System.out.println("[StartGameMenuController] Processing start game input: " + input.toString());
    }
}