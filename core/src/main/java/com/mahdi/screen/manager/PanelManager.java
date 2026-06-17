package com.mahdi.screen.manager;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mahdi.screen.pannels.BasePanel;

public class PanelManager {
    private static PanelManager instance;

    private Stage stage;
    private BasePanel currentPanel;

    private PanelManager() {
    }

    public static PanelManager getInstance() {
        if (instance == null) {
            instance = new PanelManager();
        }
        return instance;
    }

    public void initialize(Stage stage) {
        this.stage = stage;
    }

    public void performPanelTransition(BasePanel newPanel) {
        if (stage == null) {
            throw new IllegalStateException("PanelManager not initialized with a Stage. Call initialize() first.");
        }

        // حذف و پاکسازی فوری پنل قبلی
        if (currentPanel != null) {
            currentPanel.remove();
            currentPanel.dispose();
            currentPanel = null;
        }

        // افزودن فوری پنل جدید
        currentPanel = newPanel;
        currentPanel.getColor().a = 1f;
        stage.addActor(currentPanel);
    }

    public void dispose() {
        if (currentPanel != null) {
            currentPanel.remove();
            currentPanel.dispose();
            currentPanel = null;
        }
        stage = null;
        instance = null;
    }
}
