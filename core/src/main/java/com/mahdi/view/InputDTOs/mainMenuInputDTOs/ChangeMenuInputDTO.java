package com.mahdi.view.InputDTOs.mainMenuInputDTOs;

import com.mahdi.model.enums.MenuType;

public record ChangeMenuInputDTO(MenuType target) implements MainMenuInputDTO {}