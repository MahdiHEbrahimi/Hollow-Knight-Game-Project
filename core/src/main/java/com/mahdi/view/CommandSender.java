package com.mahdi.view;

import com.mahdi.controller.AppController;

public class CommandSender {
    public void send(InputDTO input) {
        AppController.control(input);
    }
}
