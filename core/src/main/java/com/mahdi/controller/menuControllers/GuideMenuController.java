package com.mahdi.controller.menuControllers;

import com.mahdi.view.InputDTO;

public class GuideMenuController extends MenuController {
    private static GuideMenuController instance;

    private GuideMenuController() {}

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new GuideMenuController();
        }
        return instance;
    }

    @Override
    public void control(InputDTO input) {
        // TODO: پردازش ورودی‌های منوی راهنما
        System.out.println("[GuideMenuController] Processing guide input: " + input.toString());
    }
}