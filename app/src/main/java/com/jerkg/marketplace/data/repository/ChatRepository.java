// Файл: app/src/main/java/com/jerkg/marketplace/data/repository/ChatRepository.java
package com.jerkg.marketplace.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.jerkg.marketplace.data.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public ChatRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public interface MessagesCallback {
        void onMessages(List<Message> messages);
        void onError(String message);
    }

    public interface VoidCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface ChatsCallback {
        void onChats(List<Map<String, Object>> chats);
        void onError(String message);
    }

    public ListenerRegistration listenToMessages(String chatId, MessagesCallback callback) {
        return db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) { callback.onError(error.getMessage()); return; }
                    if (snapshots == null) return;

                    List<Message> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Message msg = doc.toObject(Message.class);
                        if (msg != null) {
                            msg.setId(doc.getId());
                            messages.add(msg);
                        }
                    }
                    callback.onMessages(messages);
                });
    }

    public void sendMessage(String chatId, String text,
                            String currentUid, String otherUid,
                            VoidCallback callback) {
        Message message = new Message(currentUid, text.trim());

        ensureChatExists(chatId, currentUid, otherUid, () ->
                db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .add(message)
                        .addOnSuccessListener(ref -> {
                            updateChatMeta(chatId, text);
                            callback.onSuccess();
                        })
                        .addOnFailureListener(e -> callback.onError(e.getMessage()))
        );
    }

    private void ensureChatExists(String chatId, String uid1, String uid2, Runnable onDone) {
        db.collection("chats").document(chatId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        List<String> participants = new ArrayList<>();
                        participants.add(uid1);
                        participants.add(uid2);

                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("participants", participants);
                        chatData.put("createdAt", System.currentTimeMillis());

                        db.collection("chats").document(chatId)
                                .set(chatData)
                                .addOnSuccessListener(v -> onDone.run())
                                .addOnFailureListener(e -> onDone.run());
                    } else {
                        onDone.run();
                    }
                })
                .addOnFailureListener(e -> onDone.run());
    }

    private void updateChatMeta(String chatId, String lastMessageText) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", lastMessageText);
        updates.put("lastMessageAt", System.currentTimeMillis());
        db.collection("chats").document(chatId).update(updates);
    }

    public void markMessagesAsRead(String chatId, String currentUid) {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .whereNotEqualTo("senderId", currentUid)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        doc.getReference().update("read", true);
                    }
                });
    }

    public ListenerRegistration listenToChats(String currentUid, ChatsCallback callback) {
        return db.collection("chats")
                .whereArrayContains("participants", currentUid)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) { callback.onError(error.getMessage()); return; }
                    if (snapshots == null) return;

                    List<Map<String, Object>> chats = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            data.put("chatId", doc.getId());
                            chats.add(data);
                        }
                    }
                    callback.onChats(chats);
                });
    }
}