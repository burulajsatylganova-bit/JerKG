package com.aimak.marketplace.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.aimak.marketplace.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private ChatViewModel viewModel;
    private MessageAdapter messageAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String chatId       = args != null ? args.getString("chatId") : null;
        String otherUserName = args != null ? args.getString("otherUserName", "") : "";
        // otherUid — нужен чтобы participants записывались правильно
        String otherUid     = args != null ? args.getString("otherUid", "") : "";

        // Если otherUid не передан — извлекаем из chatId (формат: "uid1_uid2")
        if ((otherUid == null || otherUid.isEmpty()) && chatId != null && chatId.contains("_")) {
            String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
            String[] parts = chatId.split("_");
            if (parts.length == 2) {
                otherUid = parts[0].equals(currentUid) ? parts[1] : parts[0];
            }
        }

        if (chatId == null) {
            requireActivity().onBackPressed();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.init(chatId, otherUid);

        setupToolbar(otherUserName);
        setupRecyclerView();
        setupSendButton();
        observeViewModel();
    }

    private void setupToolbar(String userName) {
        binding.toolbar.setTitle(userName);
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(viewModel.getCurrentUid());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(messageAdapter);
    }

    private void setupSendButton() {
        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            viewModel.sendMessage(text);
            binding.etMessage.setText("");
        }
    }

    private void observeViewModel() {
        viewModel.messagesLiveData.observe(getViewLifecycleOwner(), messages -> {
            messageAdapter.setMessages(messages);
            if (!messages.isEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size() - 1);
            }
            binding.tvEmptyChat.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.sendingLiveData.observe(getViewLifecycleOwner(), sending ->
                binding.btnSend.setEnabled(!sending));

        viewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null)
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
