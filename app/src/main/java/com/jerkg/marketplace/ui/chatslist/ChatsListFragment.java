package com.jerkg.marketplace.ui.chatslist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.jerkg.marketplace.R;
import com.jerkg.marketplace.databinding.FragmentChatsListBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatsListFragment extends Fragment {

    private FragmentChatsListBinding binding;
    private ChatsAdapter adapter;
    private ListenerRegistration chatsListener;
    private String currentUid;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        adapter = new ChatsAdapter(chat -> {
            // Переход в конкретный чат
            Bundle args = new Bundle();
            args.putString("chatId", chat.getChatId());
            args.putString("otherUserName", chat.getOtherName());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_chatsList_to_chat, args);
        });

        binding.rvChats.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvChats.setAdapter(adapter);
        binding.rvChats.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        listenToChats();
    }

    private void listenToChats() {
        if (currentUid == null) {
            showEmpty();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);

        chatsListener = FirebaseFirestore.getInstance()
                .collection("chats")
                .whereArrayContains("participants", currentUid)
                .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);

                    if (error != null || snapshots == null || snapshots.isEmpty()) {
                        showEmpty();
                        return;
                    }

                    List<DocumentSnapshot> docs = snapshots.getDocuments();
                    List<ChatPreview> result = new ArrayList<>();
                    AtomicInteger remaining = new AtomicInteger(docs.size());

                    for (DocumentSnapshot doc : docs) {
                        String chatId = doc.getId();
                        List<String> participants = (List<String>) doc.get("participants");
                        String lastMessage = doc.getString("lastMessage");
                        Long lastMessageAt = doc.getLong("lastMessageAt");

                        // Определяем UID собеседника
                        String otherUid = "";
                        if (participants != null) {
                            for (String uid : participants) {
                                if (!uid.equals(currentUid)) {
                                    otherUid = uid;
                                    break;
                                }
                            }
                        }

                        final String finalOtherUid = otherUid;

                        // Проверяем есть ли непрочитанные сообщения
                        final String fChatId = chatId;
                        final String fLastMsg = lastMessage != null ? lastMessage : "";
                        final long fLastAt = lastMessageAt != null ? lastMessageAt : 0;

                        checkUnreadAndAdd(fChatId, finalOtherUid, fLastMsg, fLastAt,
                                result, remaining, docs.size());
                    }
                });
    }

    private void checkUnreadAndAdd(String chatId, String otherUid,
                                   String lastMessage, long lastMessageAt,
                                   List<ChatPreview> result, AtomicInteger remaining, int total) {

        // Получаем имя собеседника из его номера телефона
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(otherUid)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String otherName = "Пользователь";
                    if (userDoc.exists()) {
                        String phone = userDoc.getString("phoneNumber");
                        String displayName = userDoc.getString("displayName");
                        if (displayName != null && !displayName.isEmpty()) {
                            otherName = displayName;
                        } else if (phone != null && !phone.isEmpty()) {
                            // Показываем последние 4 цифры телефона
                            otherName = phone.length() > 4
                                    ? "..." + phone.substring(phone.length() - 4)
                                    : phone;
                        }
                    }

                    // Считаем непрочитанные сообщения от собеседника
                    final String name = otherName;
                    FirebaseFirestore.getInstance()
                            .collection("chats")
                            .document(chatId)
                            .collection("messages")
                            .whereEqualTo("senderId", otherUid)
                            .whereEqualTo("read", false)
                            .get()
                            .addOnSuccessListener(unreadSnap -> {
                                boolean hasUnread = !unreadSnap.isEmpty();
                                result.add(new ChatPreview(chatId, otherUid, name,
                                        lastMessage, lastMessageAt, hasUnread));

                                if (remaining.decrementAndGet() == 0) {
                                    // Все чаты загружены — сортируем и показываем
                                    result.sort((a, b) ->
                                            Long.compare(b.getLastMessageAt(), a.getLastMessageAt()));
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            if (binding == null) return;
                                            adapter.setChats(result);
                                            binding.rvChats.setVisibility(View.VISIBLE);
                                            binding.layoutEmpty.setVisibility(View.GONE);
                                        });
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                result.add(new ChatPreview(chatId, otherUid, name,
                                        lastMessage, lastMessageAt, false));
                                if (remaining.decrementAndGet() == 0 && getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        if (binding == null) return;
                                        adapter.setChats(result);
                                        binding.rvChats.setVisibility(View.VISIBLE);
                                    });
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    result.add(new ChatPreview(chatId, otherUid, "Пользователь",
                            lastMessage, lastMessageAt, false));
                    if (remaining.decrementAndGet() == 0 && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (binding == null) return;
                            adapter.setChats(result);
                            binding.rvChats.setVisibility(View.VISIBLE);
                        });
                    }
                });
    }

    private void showEmpty() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.GONE);
        binding.rvChats.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatsListener != null) chatsListener.remove();
        binding = null;
    }
}