package com.mahdi.controller.menuControllers;

import com.mahdi.view.InputDTO;

public class KeyBoardMenuController extends MenuController {

    static private KeyBoardMenuController instance;

    private KeyBoardMenuController() {
    }

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new KeyBoardMenuController();
        }
        return instance;
    }


    @Override
    public void control(InputDTO input) {
        // TODO: پردازش ورودی‌های منوی Inventory
    }
}
