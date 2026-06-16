package com.mahdi.model.status;

import com.mahdi.model.enums.MenuType;
import com.mahdi.screen.manager.MusicManager;
import com.mahdi.screen.manager.PanelManager;
import com.mahdi.screen.manager.SoundManager;

public class AppStatus {
    public static MenuType curreMenu;

    public static void ChangeMenuAndPanel(MenuType target) {
        curreMenu = target;
        PanelManager.getInstance().performPanelTransition(target.getNewPanel());
    }

    public static int getVolume() {
        return (int) MusicManager.getInstance().getVolume() * 100;
    }

    public static void setVolume(int amount) {
        MusicManager.getInstance().setVolume(amount / 100);
    }

    public void setMutedSFX(boolean muted) {
        SoundManager.getInstance().setMuted(muted);
    }

    public boolean getMuteSFX() {
        return SoundManager.getInstance().getMute();
    }

    public void setMutedMusic(boolean muted) {
        MusicManager.getInstance().setMuted(muted);
    }

    public boolean getMuteMusic() {
        return MusicManager.getInstance().getMute();
    }
}
