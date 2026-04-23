// Файл: app/src/main/java/com/jerkg/marketplace/ui/auth/LoginActivity.java
package com.jerkg.marketplace.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.jerkg.marketplace.databinding.ActivityLoginBinding;
import com.jerkg.marketplace.ui.main.MainActivity;

/**
 * LoginActivity — экран авторизации по номеру телефона.
 *
 * Логика (2 шага):
 * 1. Пользователь вводит номер → нажимает "Получить код" → приходит SMS
 * 2. Пользователь вводит 6-значный код → нажимает "Войти" → переход на главный экран
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding вместо findViewById
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Инициализируем ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Если уже залогинен — сразу на главный экран
        if (viewModel.isUserLoggedIn()) {
            goToMain();
            return;
        }

        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        // ШАГ 1: Кнопка "Получить код"
        binding.btnSendCode.setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString().trim();

            // Валидация
            if (phone.isEmpty()) {
                binding.etPhone.setError("Введите номер телефона");
                return;
            }

            // Форматируем номер: добавляем +996 если нет
            String fullPhone = formatPhone(phone);

            viewModel.sendCode(fullPhone, this);
        });

        // ШАГ 2: Кнопка "Войти"
        binding.btnVerify.setOnClickListener(v -> {
            String code = binding.etCode.getText().toString().trim();
            viewModel.verifyCode(code);
        });

        // Кнопка "Изменить номер" — вернуться к шагу 1
        binding.tvChangePhone.setOnClickListener(v -> showStep1());
    }

    private void observeViewModel() {
        // Загрузка — показываем/скрываем прогресс
        viewModel.loadingLiveData.observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSendCode.setEnabled(!isLoading);
            binding.btnVerify.setEnabled(!isLoading);
        });

        // SMS отправлена — переключаем на шаг 2
        viewModel.codeSentLiveData.observe(this, codeSent -> {
            if (codeSent) {
                showStep2();
                Toast.makeText(this, "SMS отправлена", Toast.LENGTH_SHORT).show();
            }
        });

        // Успешная авторизация — идём на главный экран
        viewModel.userLiveData.observe(this, user -> {
            if (user != null) {
                goToMain();
            }
        });

        // Ошибки — показываем Toast
        viewModel.errorLiveData.observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Показываем первый шаг: ввод номера телефона */
    private void showStep1() {
        binding.layoutStep1.setVisibility(View.VISIBLE);
        binding.layoutStep2.setVisibility(View.GONE);
    }

    /** Показываем второй шаг: ввод SMS кода */
    private void showStep2() {
        binding.layoutStep1.setVisibility(View.GONE);
        binding.layoutStep2.setVisibility(View.VISIBLE);

        // Показываем номер телефона в тексте "Код отправлен на +996..."
        String phone = formatPhone(binding.etPhone.getText().toString().trim());
        binding.tvPhoneHint.setText("Код отправлен на " + phone);

        // Фокус на поле кода
        binding.etCode.requestFocus();
    }

    /** Переход на главный экран */
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // закрываем LoginActivity, чтобы нельзя было вернуться назад
    }

    /**
     * Форматирует номер телефона в формат +996XXXXXXXXX.
     * Примеры: "0700123456" → "+996700123456"
     *          "700123456"  → "+996700123456"
     */
    private String formatPhone(String phone) {
        // Убираем пробелы и дефисы
        phone = phone.replaceAll("[\\s\\-()]", "");

        if (phone.startsWith("+996")) {
            return phone;
        } else if (phone.startsWith("0")) {
            return "+996" + phone.substring(1);
        } else if (phone.startsWith("996")) {
            return "+" + phone;
        } else {
            return "+996" + phone;
        }
    }
}
