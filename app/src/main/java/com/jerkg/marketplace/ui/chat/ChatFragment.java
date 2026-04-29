package com.jerkg.marketplace.ui.chat;

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

import com.jerkg.marketplace.databinding.FragmentChatBinding;

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

        // Аргументы из навигации
        String chatId = getArguments() != null ? getArguments().getString("chatId") : null;
        String otherUserName = getArguments() != null
                ? getArguments().getString("otherUserName", "Продавец")
                : "Продавец";

        if (chatId == null) {
            requireActivity().onBackPressed();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.init(chatId, ""); // otherUid будет извлечён из chatId при отправке

        setupToolbar(otherUserName);
        setupRecyclerView();
        setupSendButton();
        observeViewModel();
    }

    private void setupToolbar(String userName) {
        binding.toolbar.setTitle(userName);
        binding.toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed());
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(viewModel.getCurrentUid());

        // LinearLayoutManager с reverseLayout=false, stackFromEnd=true
        // — новые сообщения всегда внизу
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);

        binding.rvMessages.setLayoutManager(layoutManager);
        binding.rvMessages.setAdapter(messageAdapter);
    }

    private void setupSendButton() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.sendMessage(text);
                binding.etMessage.setText("");
            }
        });

        // Отправка по нажатию Enter на клавиатуре
        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            String text = binding.etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                viewModel.sendMessage(text);
                binding.etMessage.setText("");
                return true;
            }
            return false;
        });
    }

    private void observeViewModel() {
        // Новые сообщения — обновляем список и скроллим вниз
        viewModel.messagesLiveData.observe(getViewLifecycleOwner(), messages -> {
            messageAdapter.setMessages(messages);
            // Скроллим к последнему сообщению
            if (!messages.isEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size() - 1);
            }
            // Показываем/скрываем заглушку "нет сообщений"
            binding.tvEmptyChat.setVisibility(
                    messages.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Статус отправки
        viewModel.sendingLiveData.observe(getViewLifecycleOwner(), sending -> {
            binding.btnSend.setEnabled(!sending);
        });

        // Ошибки
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
