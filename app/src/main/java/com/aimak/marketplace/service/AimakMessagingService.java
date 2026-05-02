package com.aimak.marketplace.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.aimak.marketplace.R;
import com.aimak.marketplace.ui.main.MainActivity;

import java.util.HashMap;
import java.util.Map;

public class AimakMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID   = "aimak_messages";
    private static final String CHANNEL_NAME = "Сообщения";
    private static final int    NOTIF_ID     = 1001;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "Аймак";
        String body  = "Новое сообщение";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null)
                title = remoteMessage.getNotification().getTitle();
            if (remoteMessage.getNotification().getBody() != null)
                body = remoteMessage.getNotification().getBody();
        } else if (!remoteMessage.getData().isEmpty()) {
            Map<String, String> data = remoteMessage.getData();
            if (data.containsKey("title")) title = data.get("title");
            if (data.containsKey("body"))  body  = data.get("body");
        }

        showNotification(title, body, remoteMessage.getData());

        // Сохраняем в Firestore для отображения в экране уведомлений
        saveToFirestore(title, body);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Сохраняем новый токен в Firestore
        saveFcmToken(token);
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Создаём канал для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Уведомления о новых сообщениях");
            channel.enableVibration(true);
            channel.enableLights(true);
            manager.createNotificationChannel(channel);
        }

        // Intent — открываем MainActivity при нажатии
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Если есть chatId — передаём чтобы сразу открыть чат
        if (data != null && data.containsKey("chatId")) {
            intent.putExtra("chatId", data.get("chatId"));
            intent.putExtra("otherUserName", data.getOrDefault("senderName", "Пользователь"));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        manager.notify(NOTIF_ID, builder.build());
    }

    private void saveToFirestore(String title, String body) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        Map<String, Object> notif = new HashMap<>();
        notif.put("icon",      "💬");
        notif.put("title",     title);
        notif.put("message",   body);
        notif.put("createdAt", System.currentTimeMillis());
        notif.put("unread",    true);

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(uid)
                .collection("items")
                .add(notif);
    }

    public static void saveFcmToken(String token) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null || token == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("fcmToken",    token);
        data.put("updatedAt",   System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update(data)
                .addOnFailureListener(e -> {
                    // Если документ не существует — создаём
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .set(data, com.google.firebase.firestore.SetOptions.merge());
                });
    }
}
