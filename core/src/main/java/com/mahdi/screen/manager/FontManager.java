package com.mahdi.screen.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class FontManager {
    private static FontManager instance;

    // فونت‌های انگلیسی
    private BitmapFont englishMenuFont;
    private BitmapFont englishTitleFont;

    // فونت‌های فارسی
    private BitmapFont persianMenuFont;
    private BitmapFont persianTitleFont;

    // کاراکترهای فارسی برای نمایش صحیح (بسیار مهم)
    private static final String PERSIAN_CHARS = FreeTypeFontGenerator.DEFAULT_CHARS + "ابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهیيکآأإؤئ";

    private FontManager() {
        loadEnglishFonts();
        loadPersianFonts();
    }

    private void loadEnglishFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/TrajanPro-Regular.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        
        // تنظیمات برای منو (English)
        parameter.size = 54;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        englishMenuFont = generator.generateFont(parameter);

        // تنظیمات برای تایتل (English)
        parameter.size = 72;
        englishTitleFont = generator.generateFont(parameter);

        generator.dispose();
    }

    private void loadPersianFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Vazir.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        
        // اضافه کردن کاراکترهای فارسی به پارامترها
        parameter.characters = PERSIAN_CHARS; 
        
        // تنظیمات برای منو (Persian)
        parameter.size = 54;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        persianMenuFont = generator.generateFont(parameter);

        // تنظیمات برای تایتل (Persian)
        parameter.size = 72;
        persianTitleFont = generator.generateFont(parameter);

        generator.dispose();
    }

    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    // متدهای دریافت برای انگلیسی
    public BitmapFont getEnglishMenuFont() { return englishMenuFont; }
    public BitmapFont getEnglishTitleFont() { return englishTitleFont; }

    // متدهای دریافت برای فارسی
    public BitmapFont getPersianMenuFont() { return persianMenuFont; }
    public BitmapFont getPersianTitleFont() { return persianTitleFont; }

    public void dispose() {
        if (englishMenuFont != null) englishMenuFont.dispose();
        if (englishTitleFont != null) englishTitleFont.dispose();
        if (persianMenuFont != null) persianMenuFont.dispose();
        if (persianTitleFont != null) persianTitleFont.dispose();
    }
}