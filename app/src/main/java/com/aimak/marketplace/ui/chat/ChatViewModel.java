package com.aimak.marketplace.ui.chat;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.aimak.marketplace.data.model.Message;
import com.aimak.marketplace.data.repository.ChatRepository;
import com.aimak.marketplace.service.NotificationHelper;

import java.util.List;

public class ChatViewModel extends ViewModel {

    private final ChatRepository chatRepo = new ChatRepository();
    private ListenerRegistration messagesListener;

    public MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> sendingLiveData = new MutableLiveData<>(false);

    private String chatId;
    private String currentUid;
    private String otherUid;
    private String otherName;

    public void init(String chatId, String otherUid) {
        this.chatId     = chatId;
        this.otherUid   = otherUid;
        this.currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        // Загружаем имя собеседника для уведомлений
        loadOtherUserName();

        messagesListener = chatRepo.listenToMessages(chatId, new ChatRepository.MessagesCallback() {
            @Override
            public void onMessages(List<Message> messages) {
                messagesLiveData.postValue(messages);
                chatRepo.markMessagesAsRead(chatId, currentUid);
            }
            @Override
            public void onError(String message) {
                errorLiveData.postValue(message);
            }
        });
    }

    private void loadOtherUserName() {
        if (otherUid == null || otherUid.isEmpty()) return;
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(otherUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name  = doc.getString("name");
                        String phone = doc.getString("phoneNumber");
                        if (name != null && !name.isEmpty()) {
                            otherName = name;
                        } else if (phone != null && phone.length() >= 4) {
                            otherName = "..." + phone.substring(phone.length() - 4);
                        }
                    }
                });
    }

    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty()) return;
        if (chatId == null || currentUid == null) return;

        sendingLiveData.setValue(true);

        chatRepo.sendMessage(chatId, text, currentUid, otherUid,
                new ChatRepository.VoidCallback() {
                    @Override
                    public void onSuccess() {
                        sendingLiveData.postValue(false);
                        // ✅ Отправляем уведомление собеседнику
                        String senderName = getSenderName();
                        NotificationHelper.sendMessageNotification(
                                otherUid, senderName, chatId, text);
                    }
                    @Override
                    public void onError(String message) {
                        sendingLiveData.postValue(false);
                        errorLiveData.postValue(message);
                    }
                });
    }

    private String getSenderName() {
        // Пытаемся получить имя из Firestore профиля
        return "Аймак"; // будет заменено асинхронно в Cloud Function
    }

    public String getCurrentUid() { return currentUid; }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (messagesListener != null) messagesListener.remove();
    }
}
