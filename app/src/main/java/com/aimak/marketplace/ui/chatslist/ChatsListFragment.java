package com.aimak.marketplace.ui.chatslist;

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
import com.aimak.marketplace.R;
import com.aimak.marketplace.databinding.FragmentChatsListBinding;

import java.util.ArrayList;
import java.util.List;
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
            Bundle args = new Bundle();
            args.putString("chatId", chat.getChatId());
            args.putString("otherUid", chat.getOtherUid()); // ✅ передаём otherUid
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
        if (currentUid == null) { showEmpty(); return; }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.rvChats.setVisibility(View.GONE);

        // БЕЗ orderBy — Firestore не требует составного индекса
        chatsListener = FirebaseFirestore.getInstance()
                .collection("chats")
                .whereArrayContains("participants", currentUid)
                .addSnapshotListener((snapshots, error) -> {
                    if (binding == null) return;

                    if (error != null || snapshots == null || snapshots.isEmpty()) {
                        showEmpty();
                        return;
                    }

                    List<DocumentSnapshot> docs = snapshots.getDocuments();
                    List<ChatPreview> result = new ArrayList<>();
                    AtomicInteger remaining = new AtomicInteger(docs.size());

                    for (DocumentSnapshot doc : docs) {
                        String chatId      = doc.getId();
                        String lastMessage = doc.getString("lastMessage");
                        Long lastAt        = doc.getLong("lastMessageAt");
                        long lastMessageAt = lastAt != null ? lastAt : 0;

                        // Находим UID собеседника
                        List<String> participants = (List<String>) doc.get("participants");
                        String otherUid = "";
                        if (participants != null) {
                            for (String uid : participants) {
                                if (!uid.equals(currentUid)) { otherUid = uid; break; }
                            }
                        }

                        fetchNameAndAdd(chatId, otherUid,
                                lastMessage != null ? lastMessage : "",
                                lastMessageAt, result, remaining);
                    }
                });
    }

    private void fetchNameAndAdd(String chatId, String otherUid,
                                 String lastMessage, long lastMessageAt,
                                 List<ChatPreview> result, AtomicInteger remaining) {

        if (otherUid.isEmpty()) {
            result.add(new ChatPreview(chatId, "", getString(R.string.profile_user_default), lastMessage, lastMessageAt, false));
            checkAndShow(result, remaining);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(otherUid)
                .get()
                .addOnCompleteListener(task -> {
                    String name = getString(R.string.profile_user_default);
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().exists()) {
                        String displayName = task.getResult().getString("name");
                        String phone       = task.getResult().getString("phoneNumber");
                        if (displayName != null && !displayName.isEmpty()) {
                            name = displayName;
                        } else if (phone != null && phone.length() >= 4) {
                            name = "..." + phone.substring(phone.length() - 4);
                        }
                    }

                    // Проверяем непрочитанные
                    final String finalName = name;
                    FirebaseFirestore.getInstance()
                            .collection("chats").document(chatId)
                            .collection("messages")
                            .whereEqualTo("senderId", otherUid)
                            .whereEqualTo("read", false)
                            .get()
                            .addOnCompleteListener(t -> {
                                boolean hasUnread = t.isSuccessful()
                                        && t.getResult() != null
                                        && !t.getResult().isEmpty();
                                result.add(new ChatPreview(chatId, otherUid, finalName,
                                        lastMessage, lastMessageAt, hasUnread));
                                checkAndShow(result, remaining);
                            });
                });
    }

    private void checkAndShow(List<ChatPreview> result, AtomicInteger remaining) {
        if (remaining.decrementAndGet() == 0 && getActivity() != null) {
            // Сортируем по времени последнего сообщения на клиенте
            result.sort((a, b) -> Long.compare(b.getLastMessageAt(), a.getLastMessageAt()));
            getActivity().runOnUiThread(() -> {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                if (result.isEmpty()) {
                    showEmpty();
                } else {
                    adapter.setChats(result);
                    binding.rvChats.setVisibility(View.VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                }
            });
        }
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
