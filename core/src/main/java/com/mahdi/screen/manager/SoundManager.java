package com.mahdi.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;

public class SoundManager {
    private static SoundManager instance;

    // کش داخلی برای متدهایی که با آدرس فایل صدا زده می‌شوند
    private final ObjectMap<String, Sound> soundCache;

    // وضعیت مرکزی میوت بودن افکت‌های صوتی
    private boolean isMuted = false;

    private SoundManager() {
        soundCache = new ObjectMap<>();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * روش اول: پخش صدا بر اساس مسیر فایل (مدیریت خودکار کش)
     * برای دکمه‌هایی که نمی‌خواهی دستی شیء Sound را برایشان بسازی و Dispose کنی
     * عالی است.
     */
    public void playSFX(String filePath) {
        if (isMuted)
            return;

        Sound sound = soundCache.get(filePath);

        if (sound == null) {
            try {
                sound = Gdx.audio.newSound(Gdx.files.internal(filePath));
                soundCache.put(filePath, sound);
            } catch (Exception e) {
                Gdx.app.error("SoundManager", "Error loading sound file: " + filePath, e);
                return;
            }
        }

        sound.play();
    }

    /**
     * روش دوم: پخش مستقیم یک شیء Sound که از قبل لود شده است
     * این متد بدون دخالت در کش، صدا را دریافت کرده و با رعایت شرط میوت بودن بازی،
     * آن را پخش می‌کند.
     * 
     * @param sound شیء صوتی لود شده در کلاس دکمه یا اسکرین
     */
    public void playSound(Sound sound) {
        // اگر سیستم میوت بود یا شیء صدا به هر دلیلی نال بود، پخش نکن
        if (isMuted || sound == null)
            return;

        sound.play();
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }

    public boolean getMute() {
        return isMuted;
    }

    /**
     * آزادسازی حافظه فقط برای صداهایی که توسط خود منیجر کش شده بودند
     */
    public void dispose() {
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();
        instance = null;
    }
}