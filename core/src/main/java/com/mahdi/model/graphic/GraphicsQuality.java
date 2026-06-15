package com.mahdi.model.graphic;

    // Global enum defining preset graphic qualities
    public enum GraphicsQuality {
        LOW(800, 450, false),
        MEDIUM(1280, 720, true),
        HIGH(1920, 1080, true),
        Ultra_High(2560, 1440, true);

        public final int width;
        public final int height;
        public final boolean vSync;

        GraphicsQuality(int width, int height, boolean vSync) {
            this.width = width;
            this.height = height;
            this.vSync = vSync;
        }
    }