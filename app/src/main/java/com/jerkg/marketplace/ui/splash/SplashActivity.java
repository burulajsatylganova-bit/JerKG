// Файл: app/src/main/java/com/jerkg/marketplace/ui/splash/SplashActivity.java
package com.jerkg.marketplace.ui.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.jerkg.marketplace.JerKGApp;
import com.jerkg.marketplace.databinding.ActivitySplashBinding;
import com.jerkg.marketplace.ui.auth.LoginActivity;

/**
 * SplashActivity — первый экран при запуске.
 *
 * Логика:
 * - Если язык ещё не выбран → показываем выбор языка (RU / KG)
 * - Если язык уже выбран → сразу переходим на LoginActivity
 *   (LoginActivity сам проверит, авторизован ли пользователь)
 */
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Проверяем, выбран ли язык
        SharedPreferences prefs = getSharedPreferences(JerKGApp.PREFS_NAME, MODE_PRIVATE);
        boolean languageChosen = prefs.contains(JerKGApp.KEY_LANGUAGE);

        if (languageChosen) {
            // Язык уже выбран — сразу переходим
            proceedToLogin();
        } else {
            // Первый запуск — показываем выбор языка
            showLanguageSelection();
        }
    }

    private void showLanguageSelection() {
        binding.layoutLanguage.setVisibility(View.VISIBLE);

        // Кнопка русского языка
        binding.btnRussian.setOnClickListener(v -> {
            JerKGApp.getInstance().setLanguage(JerKGApp.LANG_RU);
            proceedToLogin();
        });

        // Кнопка кыргызского языка
        binding.btnKyrgyz.setOnClickListener(v -> {
            JerKGApp.getInstance().setLanguage(JerKGApp.LANG_KY);
            proceedToLogin();
        });
    }

    private void proceedToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
