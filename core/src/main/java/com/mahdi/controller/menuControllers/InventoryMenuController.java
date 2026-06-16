package com.mahdi.controller.menuControllers;

import com.mahdi.view.InputDTO;

public class InventoryMenuController extends MenuController {
    private static InventoryMenuController instance;

    private InventoryMenuController() {}

    public static MenuController getInstance() {
        if (instance == null) {
            instance = new InventoryMenuController();
        }
        return instance;
    }

    @Override
    public void control(InputDTO input) {
        // TODO: پردازش ورودی‌های منوی Inventory
        System.out.println("[InventoryMenuController] Processing inventory input: " + input.toString());
    }
}