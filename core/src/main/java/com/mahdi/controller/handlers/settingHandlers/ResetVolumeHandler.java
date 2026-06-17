package com.mahdi.controller.handlers.settingHandlers;

import com.mahdi.model.status.AppStatus;
import com.mahdi.view.InputDTOs.settingMenuInputDTOs.ResetVolumeInputDTO;

public class ResetVolumeHandler {
    public static void handle(ResetVolumeInputDTO input) {
        AppStatus.setMusicVolume(50);
        AppStatus.setSFXVolume(50);
        AppStatus.setBrightness(50);
        AppStatus.setMutedSFX(false);
        AppStatus.setMutedMusic(false);
        if (input.musicSlider() != null) {
            input.musicSlider().setValue(AppStatus.getMusicVolume());
        }
        if (input.sfxSlider() != null) {
            input.sfxSlider().setValue(AppStatus.getSFXVolume());
        }
        if (input.brightnessSlider() != null) {
            input.brightnessSlider().setValue(AppStatus.getBrightness());
        }
    }
}
