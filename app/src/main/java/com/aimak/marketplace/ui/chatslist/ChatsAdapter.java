package com.aimak.marketplace.ui.chatslist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aimak.marketplace.R;

import java.util.ArrayList;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.VH> {

    public interface OnChatClickListener {
        void onChatClick(ChatPreview chat);
    }

    private List<ChatPreview> chats = new ArrayList<>();
    private final OnChatClickListener listener;

    public ChatsAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void setChats(List<ChatPreview> newChats) {
        this.chats = newChats != null ? newChats : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ChatPreview chat = chats.get(pos);

        h.tvAvatar.setText(chat.getAvatarLetter());
        h.tvName.setText(chat.getOtherName());
        h.tvLastMessage.setText(chat.getLastMessage());
        h.tvTime.setText(chat.getFormattedTime(h.itemView.getContext()));
        h.dotUnread.setVisibility(chat.hasUnread() ? View.VISIBLE : View.GONE);

        // Жирный текст если есть непрочитанные
        h.tvLastMessage.setTextColor(
                chat.hasUnread()
                        ? h.itemView.getContext().getColor(R.color.text_primary)
                        : h.itemView.getContext().getColor(R.color.text_tertiary));

        h.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() { return chats.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvLastMessage, tvTime;
        View dotUnread;

        VH(@NonNull View v) {
            super(v);
            tvAvatar      = v.findViewById(R.id.tvAvatar);
            tvName        = v.findViewById(R.id.tvName);
            tvLastMessage = v.findViewById(R.id.tvLastMessage);
            tvTime        = v.findViewById(R.id.tvTime);
            dotUnread     = v.findViewById(R.id.dotUnread);
        }
    }
}
