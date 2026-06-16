package com.mahdi.screen.manager;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.mahdi.screen.pannels.BasePanel;

public class PanelManager {
    private static PanelManager instance;

    private Stage stage;
    private BasePanel currentPanel;

    private boolean isTransitioning = false;

    private PanelManager() {}

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

        // اگر انتقالی در جریان است، درخواست را نادیده بگیر (یا می‌توان صف گذاشت)
        if (isTransitioning) {
            return;
        }

        isTransitioning = true;

        if (currentPanel != null) {
            // Fade Out پنل فعلی
            currentPanel.addAction(Actions.sequence(
                Actions.fadeOut(2f),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        // حذف از Stage
                        currentPanel.remove();
                        // آزادسازی منابع پنل قدیمی
                        currentPanel.dispose();
                        currentPanel = null;

                        // اضافه کردن پنل جدید با Fade In
                        addNewPanelWithFadeIn(newPanel);
                        isTransitioning = false;
                        return true;
                    }
                }
            ));
        } else {
            addNewPanelWithFadeIn(newPanel);
            isTransitioning = false;
        }
    }

    private void addNewPanelWithFadeIn(BasePanel panel) {
        panel.getColor().a = 0f;           // شفاف کامل
        stage.addActor(panel);
        panel.addAction(Actions.fadeIn(2f));
        currentPanel = panel;
    }

    /**
     * پاک‌سازی نهایی در صورت نیاز.
     */
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