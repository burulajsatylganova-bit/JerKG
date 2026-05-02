
package com.aimak.marketplace.data.repository;

import android.app.Activity;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.aimak.marketplace.data.model.User;
import com.aimak.marketplace.R;
import com.aimak.marketplace.utils.ResUtils;

import java.util.concurrent.TimeUnit;

/**
 * AuthRepository — весь код работы с Firebase Auth.
 * ViewModel обращается только сюда, не знает о Firebase напрямую.
 */
public class AuthRepository {

    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // ID для верификации — нужен при отправке кода
    private String verificationId;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Проверяем, авторизован ли пользователь уже.
     * Если да — пропускаем экран логина.
     */
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Шаг 1: Отправить SMS с кодом на номер телефона.
     *
     * @param phoneNumber номер в формате +996XXXXXXXXX
     * @param activity    нужна для reCAPTCHA верификации
     * @param onCodeSent  вызывается когда SMS отправлена
     * @param onError     вызывается при ошибке
     */
    public void sendVerificationCode(
            String phoneNumber,
            Activity activity,
            Runnable onCodeSent,
            OnErrorCallback onError
    ) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Авто-верификация (если Google распознал SMS)
                        // Можно сразу войти
                        signInWithCredential(credential, null, onError);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        onError.onError(e.getMessage());
                    }

                    @Override
                    public void onCodeSent(
                            String vId,
                            PhoneAuthProvider.ForceResendingToken token
                    ) {
                        verificationId = vId; // сохраняем для шага 2
                        onCodeSent.run();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    /**
     * Шаг 2: Проверить введённый пользователем код.
     *
     * @param code      6-значный код из SMS
     * @param onSuccess вызывается при успешной авторизации
     * @param onError   вызывается при неверном коде
     */
    public void verifyCode(
            String code,
            MutableLiveData<FirebaseUser> onSuccess,
            OnErrorCallback onError
    ) {
        if (verificationId == null) {
            onError.onError(ResUtils.str(R.string.error_request_code_first));
            return;
        }

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, code);

        signInWithCredential(credential, onSuccess, onError);
    }

    /**
     * Внутренний метод: войти с credential.
     * После успешного входа — сохраняем/обновляем пользователя в Firestore.
     */
    private void signInWithCredential(
            PhoneAuthCredential credential,
            MutableLiveData<FirebaseUser> onSuccess,
            OnErrorCallback onError
    ) {
        auth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser != null) {
                        // Создаём/обновляем запись в Firestore
                        saveUserToFirestore(firebaseUser);
                        if (onSuccess != null) {
                            onSuccess.setValue(firebaseUser);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (onError != null) {
                        onError.onError(ResUtils.str(R.string.error_wrong_code));
                    }
                });
    }

    /**
     * Сохраняем пользователя в Firestore collection "users".
     * Используем merge — чтобы не перезаписать существующие данные (имя, фото).
     */
    private void saveUserToFirestore(FirebaseUser firebaseUser) {
        // Используем merge — не перезаписываем name/region которые пользователь уже сохранил
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("uid", firebaseUser.getUid());
        data.put("phoneNumber", firebaseUser.getPhoneNumber());
        data.put("lastLoginAt", System.currentTimeMillis());

        db.collection("users")
                .document(firebaseUser.getUid())
                .set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnFailureListener(Throwable::printStackTrace);
    }

    /**
     * Выход из аккаунта.
     */
    public void signOut() {
        auth.signOut();
    }

    /**
     * Callback для ошибок.
     */
    public interface OnErrorCallback {
        void onError(String message);
    }
}