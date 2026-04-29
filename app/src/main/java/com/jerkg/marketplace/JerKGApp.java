package com.jerkg.marketplace;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;


public class JerKGApp extends Application {

    private static JerKGApp instance;

    public static final String PREFS_NAME = "JerKGPrefs";
    public static final String KEY_LANGUAGE = "language";
    public static final String LANG_RU = "ru";
    public static final String LANG_KY = "ky";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static JerKGApp getInstance() {
        return instance;
    }


    public String getSelectedLanguage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANG_RU);
    }

    public void setLanguage(String lang) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, lang).apply();
    }
}
