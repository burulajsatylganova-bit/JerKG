package com.aimak.marketplace;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class AimakApp extends Application {

    private static AimakApp instance;

    public static final String PREFS_NAME = "JerKGPrefs";
    public static final String KEY_LANGUAGE = "language";
    public static final String LANG_RU = "ru";
    public static final String LANG_KY = "ky";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        applyLocale(this);
        // Восстанавливаем тёмную тему после перезапуска
        boolean isDark = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                isDark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                        : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
    }

    public static AimakApp getInstance() { return instance; }

    public String getSelectedLanguage() {
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, LANG_RU);
    }

    public void setLanguage(String lang) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_LANGUAGE, lang).apply();
    }

    /**
     * Применяет язык к контексту.
     * Вызывается из Activity.attachBaseContext и из Application.onCreate.
     */
    public static void applyLocale(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(
                    PREFS_NAME, Context.MODE_PRIVATE);
            String lang = prefs.getString(KEY_LANGUAGE, LANG_RU);

            Locale locale = new Locale(lang);
            Locale.setDefault(locale);

            Configuration config = new Configuration(
                    context.getResources().getConfiguration());
            config.setLocale(locale);
            context.getResources().updateConfiguration(
                    config, context.getResources().getDisplayMetrics());
        } catch (Exception e) {
            // Безопасный fallback — не крашимся если контекст ещё не готов
            e.printStackTrace();
        }
    }

    // НЕ переопределяем attachBaseContext в Application —
    // это вызывало краш до инициализации SharedPreferences
}
