package com.aimak.marketplace.service;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    /**
     * Запрашиваем разрешение на уведомления (Android 13+) и сохраняем токен.
     * Вызывать при входе в приложение.
     */
    public static void initNotifications(Activity activity) {
        // Android 13+ требует явного разрешения
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        // Получаем и сохраняем FCM токен
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> AimakMessagingService.saveFcmToken(token));
    }

    /**
     * Сохраняем запрос на уведомление получателю.
     * Вызывать когда отправляем сообщение.
     * Cloud Function подхватит это и отправит push через FCM.
     */
    public static void sendMessageNotification(
            String recipientUid, String senderName, String chatId, String messageText) {

        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (currentUid == null || recipientUid == null || recipientUid.isEmpty()) return;

        // Не уведомляем самого себя
        if (currentUid.equals(recipientUid)) return;

        Map<String, Object> notification = new HashMap<>();
        notification.put("recipientUid", recipientUid);
        notification.put("senderUid",    currentUid);
        notification.put("senderName",   senderName != null ? senderName : "Пользователь");
        notification.put("chatId",       chatId);
        notification.put("message",      messageText.length() > 100
                ? messageText.substring(0, 100) + "..." : messageText);
        notification.put("createdAt",    System.currentTimeMillis());
        notification.put("sent",         false); // Cloud Function меняет на true после отправки

        FirebaseFirestore.getInstance()
                .collection("notification_queue")
                .add(notification);
    }
}
