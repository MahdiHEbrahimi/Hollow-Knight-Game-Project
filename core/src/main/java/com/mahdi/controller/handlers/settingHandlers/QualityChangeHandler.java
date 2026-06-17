package com.mahdi.controller.handlers.settingHandlers;

import com.mahdi.model.graphic.GraphicsQuality;
import com.mahdi.model.status.AppStatus;
import com.mahdi.view.InputDTOs.settingMenuInputDTOs.QualityChangeInputDTO;

public class QualityChangeHandler {
    public static void handle(QualityChangeInputDTO input) {
        switch (AppStatus.Quality) {
            case LOW:
                AppStatus.setQuality(GraphicsQuality.MEDIUM);

                break;
            case MEDIUM:
                AppStatus.setQuality(GraphicsQuality.HIGH);
                break;
            case HIGH:
                AppStatus.setQuality(GraphicsQuality.Ultra_High);
                break;
            case Ultra_High:
                AppStatus.setQuality(GraphicsQuality.LOW);
                break;

        }
        input.qualityButton().setText("Quality: " + AppStatus.getQuality().name());
    }
}
