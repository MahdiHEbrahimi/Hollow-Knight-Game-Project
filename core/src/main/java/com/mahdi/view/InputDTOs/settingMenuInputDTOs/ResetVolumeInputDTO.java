package com.mahdi.view.InputDTOs.settingMenuInputDTOs;

import com.mahdi.screen.ui.MenuSlider;

public record ResetVolumeInputDTO (MenuSlider musicSlider, MenuSlider sfxSlider, MenuSlider brightnessSlider) implements SettingMenuInputDTO {}
