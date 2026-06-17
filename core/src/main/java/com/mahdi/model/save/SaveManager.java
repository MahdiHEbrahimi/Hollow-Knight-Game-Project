package com.mahdi.model.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SaveManager {
    private static final String PREFS_NAME = "HollowKnight_SaveSlots";
    private static Preferences prefs;

    public static Preferences getPrefs() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences(PREFS_NAME);
        }
        return prefs;
    }

    // آیا این اسلات قبلاً بازی در آن ذخیره شده است؟
    public static boolean isSlotFileExists(int slotNumber) {
        return getPrefs().contains("slot_" + slotNumber + "_exists");
    }

    // شروع یک بازی جدید در یک اسلات مشخص
    public static void createNewGame(int slotNumber) {
        getPrefs().putBoolean("slot_" + slotNumber + "_exists", true);
        getPrefs().putInteger("slot_" + slotNumber + "_hp", 5); // هلت اولیه شوالیه
        getPrefs().putInteger("slot_" + slotNumber + "_geo", 0); // پول اولیه
        getPrefs().putString("slot_" + slotNumber + "_map", "Forgotten_Crossroads");
        getPrefs().flush(); // ذخیره روی هارد
    }

    // گرفتن اطلاعات یک اسلات برای نشان دادن در منو
    public static String getSlotInfo(int slotNumber) {
        if (!isSlotFileExists(slotNumber)) {
            return "Empty Slot (Click to Start New Game)";
        }
        int hp = getPrefs().getInteger("slot_" + slotNumber + "_hp", 5);
        int geo = getPrefs().getInteger("slot_" + slotNumber + "_geo", 0);
        return "Slot " + slotNumber + " [ HP: " + hp + " | Geo: " + geo + " ]";
    }

    public static void deleteSlot(int slotNumber) {
        getPrefs().remove("slot_" + slotNumber + "_exists");
        getPrefs().remove("slot_" + slotNumber + "_hp");
        getPrefs().remove("slot_" + slotNumber + "_geo");
        getPrefs().remove("slot_" + slotNumber + "_map");
        getPrefs().flush();
    }

}