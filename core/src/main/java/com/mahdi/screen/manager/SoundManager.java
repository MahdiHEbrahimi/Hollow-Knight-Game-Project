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

    // 🌟 متغیر جدید برای ذخیره ولوم اصلی افکت‌ها (پیش‌فرض 0.6)
    private float masterVolume = 1f;

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
     * 🌟 متد کمکی داخلی برای محاسبه ولوم بر اساس قانون توان دو
     * این متد تغییر اسلایدر را برای گوش انسان بسیار طبیعی‌تر می‌کند.
     */
    private float getCalculatedVolume() {
        if (isMuted) return 0f;
        return masterVolume * masterVolume;
    }

    /**
     * روش اول: پخش صدا بر اساس مسیر فایل (مدیریت خودکار کش)
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

        // 🌟 پاس دادن ولوم محاسبه‌شده به متد play
        sound.play(getCalculatedVolume());
    }

    /**
     * روش دوم: پخش مستقیم یک شیء Sound که از قبل لود شده است
     */
    public void playSound(Sound sound) {
        if (isMuted || sound == null)
            return;

        sound.play(getCalculatedVolume());
    }

    // 🌟 اضافه شدن متدهای مدیریت ولوم مشابه با MusicManager
    public void setVolume(float volume) {
        // محدود کردن مقدار ورودی بین 0.0 و 1.0 برای امنیت بیشتر
        this.masterVolume = Math.max(0f, Math.min(volume, 1f));
    }

    public float getVolume() {
        return masterVolume;
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
