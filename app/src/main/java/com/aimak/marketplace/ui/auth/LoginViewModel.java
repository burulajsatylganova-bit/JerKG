package com.aimak.marketplace.ui.auth;

import android.app.Activity;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.aimak.marketplace.data.repository.AuthRepository;


public class LoginViewModel extends ViewModel {

    private final AuthRepository repository;

    // LiveData для наблюдения из Activity
    public MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> codeSentLiveData = new MutableLiveData<>(false);

    public LoginViewModel() {
        repository = new AuthRepository();
    }

    /**
     * Уже залогинен? Сразу переходим на главный экран.
     */
    public boolean isUserLoggedIn() {
        return repository.isUserLoggedIn();
    }

    /**
     * Отправить SMS код.
     * Вызывается когда пользователь нажал "Получить код".
     */
    public void sendCode(String phoneNumber, Activity activity) {
        loadingLiveData.setValue(true);

        repository.sendVerificationCode(
                phoneNumber,
                activity,
                // onCodeSent
                () -> {
                    loadingLiveData.postValue(false);
                    codeSentLiveData.postValue(true);
                },
                // onError
                message -> {
                    loadingLiveData.postValue(false);
                    errorLiveData.postValue(message);
                }
        );
    }

    /**
     * Проверить введённый код.
     * Вызывается когда пользователь нажал "Войти".
     */
    public void verifyCode(String code) {
        if (code.length() != 6) {
            errorLiveData.setValue("Введите 6-значный код");
            return;
        }

        loadingLiveData.setValue(true);

        repository.verifyCode(
                code,
                userLiveData,   // при успехе — запишем сюда FirebaseUser
                message -> {
                    loadingLiveData.postValue(false);
                    errorLiveData.postValue(message);
                }
        );
    }
}
