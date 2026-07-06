package com.mahdi.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MusicManager {
    private static MusicManager instance;

    private Music currentMusic;
    private Music previousMusic; // Kept safely for cross-fading to prevent memory leaks

    private boolean isMuted = false;
    private float masterVolume = 0.35f; // The volume value set by user
    private String currentTrackName = "";

    // Fade State Tracking Flags
    private enum FadeState {
        NONE, FADE_IN, CROSS_FADE
    }

    private FadeState fadeState = FadeState.NONE;

    private float fadeTimer = 0f;
    private float fadeInDuration = 3.0f; // 3 seconds for clean start
    private float crossFadeDuration = 3.0f; // 1.5 seconds for transitioning tracks
    private float prevMusicVolumeStart = 0f;

    private MusicManager() {
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void update(float delta) {
        if (fadeState == FadeState.NONE)
            return;

        fadeTimer += delta;

        if (fadeState == FadeState.FADE_IN) {
            float progress = fadeTimer / fadeInDuration;
            if (progress >= 1.0f) {
                progress = 1.0f;
                fadeState = FadeState.NONE; // Transition finished
            }

            if (currentMusic != null) {
                // Linear volume calculation relative to the target master volume
                float targetVol = progress * masterVolume;
                currentMusic.setVolume(isMuted ? 0f : targetVol);
            }

        } else if (fadeState == FadeState.CROSS_FADE) {
            float progress = fadeTimer / crossFadeDuration;
            if (progress >= 1.0f) {
                progress = 1.0f;
                fadeState = FadeState.NONE;

                // End of transition: Safely clean up previous track memory
                safelyDisposePrevious();
            }

            // 1. Fade Out Previous Music
            if (previousMusic != null) {
                float prevVol = (1.0f - progress) * prevMusicVolumeStart;
                previousMusic.setVolume(isMuted ? 0f : prevVol);
            }

            // 2. Fade In Current Music
            if (currentMusic != null) {
                float targetVol = progress * masterVolume;
                currentMusic.setVolume(isMuted ? 0f : targetVol);
            }
        }
    }

    /**
     * Unified music trigger method. Handles both clean fade-in and non-blocking
     * cross-fading.
     */
    public void playMusic(String fileName) {
        // If the same track is requested and already active, leave it be
        if (currentTrackName.equals(fileName) && currentMusic != null) {
            if (!currentMusic.isPlaying()) {
                currentMusic.play();
            }
            return;
        }

        // Check if we are transitioning or starting fresh
        if (currentMusic != null) {
            // SCENARIO 2: Cross-Fade transition (1.5 seconds)
            safelyDisposePrevious(); // Ensure older tracks are flushed out completely

            previousMusic = currentMusic;
            // Capture the exact volume it left off at to decrease smoothly
            prevMusicVolumeStart = previousMusic.getVolume();

            fadeState = FadeState.CROSS_FADE;
        } else {
            // SCENARIO 1: Fresh Fade-In (3.0 seconds)
            fadeState = FadeState.FADE_IN;
        }

        try {
            currentTrackName = fileName;
            currentMusic = Gdx.audio.newMusic(Gdx.files.internal(fileName));
            currentMusic.setLooping(true);

            // Start from absolute silence
            currentMusic.setVolume(0f);
            currentMusic.play();

            fadeTimer = 0f; // Reset timeline tracker
        } catch (Exception e) {
            Gdx.app.error("MusicManager", "Error playing music file: " + fileName, e);
            fadeState = FadeState.NONE;
        }
    }

    /**
     * Explicit method to change the target sound volume manually (e.g., slider
     * adjustments)
     */
    public void setVolume(float volume) {
        this.masterVolume = Math.max(0f, Math.min(volume, 1f)); // Bounds clamp between 0.0 and 1.0

        // If no active fade animation is running, lock instantly to target volume
        if (fadeState == FadeState.NONE && currentMusic != null) {
            currentMusic.setVolume(isMuted ? 0f : masterVolume);
        }
    }

    public float getVolume() {
        return masterVolume;
    }

    /**
     * Toggles absolute global mute without stopping track timers in background
     */
    public void setMuted(boolean mute) {
        this.isMuted = mute;

        // Dynamically shift current active components
        if (currentMusic != null) {
            if (fadeState == FadeState.NONE) {
                currentMusic.setVolume(mute ? 0f : masterVolume);
            } else {
                // If mid-fade, force zero instantly to honor the mute button
                if (mute)
                    currentMusic.setVolume(0f);
            }
        }

        if (previousMusic != null && mute) {
            previousMusic.setVolume(0f);
        }
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void stopMusic() {
        fadeState = FadeState.NONE;
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
        safelyDisposePrevious();
        currentTrackName = "";
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
        if (previousMusic != null && previousMusic.isPlaying()) {
            previousMusic.pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
        if (previousMusic != null && !previousMusic.isPlaying()) {
            previousMusic.play();
        }
    }

    private void safelyDisposePrevious() {
        if (previousMusic != null) {
            previousMusic.stop();
            previousMusic.dispose();
            previousMusic = null;
        }
    }

    public void dispose() {
        stopMusic();
    }

    public boolean getMute() {
        return isMuted;
    }
}
