package com.jerkg.marketplace.ui.chat;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.jerkg.marketplace.data.model.Message;
import com.jerkg.marketplace.data.repository.ChatRepository;

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

    public void init(String chatId, String otherUid) {
        this.chatId = chatId;
        this.otherUid = otherUid;
        this.currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        // Подписываемся на сообщения в реальном времени
        messagesListener = chatRepo.listenToMessages(chatId, new ChatRepository.MessagesCallback() {
            @Override
            public void onMessages(List<Message> messages) {
                messagesLiveData.postValue(messages);
                // Помечаем как прочитанные
                chatRepo.markMessagesAsRead(chatId, currentUid);
            }
            @Override
            public void onError(String message) {
                errorLiveData.postValue(message);
            }
        });
    }

    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty()) return;
        if (chatId == null || currentUid == null) return;

        sendingLiveData.setValue(true);

        chatRepo.sendMessage(chatId, text, currentUid, otherUid,
                new ChatRepository.VoidCallback() {
                    @Override public void onSuccess() {
                        sendingLiveData.postValue(false);
                    }
                    @Override public void onError(String message) {
                        sendingLiveData.postValue(false);
                        errorLiveData.postValue(message);
                    }
                });
    }

    public String getCurrentUid() { return currentUid; }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Снимаем подписку Firestore при уничтожении ViewModel
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}
