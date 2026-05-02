
package com.aimak.marketplace.ui.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.aimak.marketplace.AimakApp;
import com.aimak.marketplace.databinding.ActivitySplashBinding;
import com.aimak.marketplace.ui.auth.LoginActivity;

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
        SharedPreferences prefs = getSharedPreferences(AimakApp.PREFS_NAME, MODE_PRIVATE);
        boolean languageChosen = prefs.contains(AimakApp.KEY_LANGUAGE);

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
            AimakApp.getInstance().setLanguage(AimakApp.LANG_RU);
            proceedToLogin();
        });

        // Кнопка кыргызского языка
        binding.btnKyrgyz.setOnClickListener(v -> {
            AimakApp.getInstance().setLanguage(AimakApp.LANG_KY);
            proceedToLogin();
        });
    }

    private void proceedToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
