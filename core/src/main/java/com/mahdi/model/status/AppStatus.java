package com.mahdi.model.status;

import com.mahdi.HollowKnightGame;
import com.mahdi.model.enums.MenuType;
import com.mahdi.model.game.GameEngine;
import com.mahdi.model.graphic.GraphicsQuality;
import com.mahdi.screen.BaseScreen;
import com.mahdi.screen.manager.BrightnessController;
import com.mahdi.screen.manager.MusicManager;
import com.mahdi.screen.manager.PanelManager;
import com.mahdi.screen.manager.SoundManager;

public class AppStatus {
    public static boolean DEBUG = false ;
    public static HollowKnightGame hollowKnightGame;
    public static MenuType curreMenu;
    private static GameEngine game;
    public static GraphicsQuality Quality = GraphicsQuality.Ultra_High;
    public static BaseScreen screen;

    public  static void setGameStatus(GameEngine game) {
        AppStatus.game = game;
    }

    public static GameEngine getGameEngine() {
        return game;
    }

    public static void setHollowKnightGame(HollowKnightGame hollowKnightGame) {
        AppStatus.hollowKnightGame = hollowKnightGame;
    }

    public static HollowKnightGame getHollowKnightGame() {
        return hollowKnightGame;
    }

    public static GraphicsQuality getQuality() {
        return Quality;
    }

    public static void setQuality(GraphicsQuality quality) {
        Quality = quality;
        hollowKnightGame.updateGraphics(quality);
    }

    public static void setCurreMenu(MenuType curreMenu) {
        AppStatus.curreMenu = curreMenu;
    }

    public static void ChangeMenuAndPanel(MenuType target) {
        curreMenu = target;
        PanelManager.getInstance().performPanelTransition(target.getNewPanel());
    }


    public static int getMusicVolume() {
        return (int) (MusicManager.getInstance().getVolume() * 100);
    }

    public static void setMusicVolume(int amount) {
        MusicManager.getInstance().setVolume(amount / 100f);
    }

    public static void setMutedMusic(boolean muted) {
        MusicManager.getInstance().setMuted(muted);
    }

    public static boolean getMuteMusic() {
        return MusicManager.getInstance().getMute();
    }

    public static int getSFXVolume() {
        return (int) (SoundManager.getInstance().getVolume() * 100);
    }

    public static void setSFXVolume(int amount) {
        SoundManager.getInstance().setVolume(amount / 100f);
    }

    public static void setMutedSFX(boolean muted) {
        SoundManager.getInstance().setMuted(muted);
    }

    public static boolean getMuteSFX() {
        return SoundManager.getInstance().getMute();
    }

    public static int getBrightness() {
        return (int) ((BrightnessController.getInstance().getBrightness() + 1f) * 50f);
    }

    public static void setScreen(BaseScreen screen) {
        AppStatus.screen = screen;
    }

    public static BaseScreen getScreen() {
        return screen;
    }

    public static void setBrightness(int value) {
        BrightnessController.getInstance().setBrightness((value / 50f) - 1f);
    }
}
