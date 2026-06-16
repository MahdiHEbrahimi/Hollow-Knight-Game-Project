package com.mahdi.controller.menuControllers;

import com.mahdi.view.InputDTO;

public class AchievementsMenuController extends MenuController {
    private static AchievementsMenuController instance;

    private AchievementsMenuController() {}

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new AchievementsMenuController();
        }
        return instance;
    }

    @Override
    public void control(InputDTO input) {
        // TODO: پردازش ورودی‌های منوی دستاوردها
        System.out.println("[AchievementsMenuController] Processing achievements input: " + input.toString());
    }
}