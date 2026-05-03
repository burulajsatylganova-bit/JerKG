package com.aimak.marketplace.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aimak.marketplace.R;
import com.aimak.marketplace.data.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_RECEIVED = 0;
    private static final int VIEW_TYPE_SENT     = 1;

    private List<Message> messages = new ArrayList<>();
    private final String currentUid;
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(String currentUid) {
        this.currentUid = currentUid;
    }

    @Override
    public int getItemViewType(int position) {
        return currentUid.equals(messages.get(position).getSenderId())
                ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_TYPE_SENT
                ? R.layout.item_message_sent
                : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(view, viewType == VIEW_TYPE_SENT);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() { return messages.size(); }

    public void setMessages(List<Message> newMessages) {
        this.messages = newMessages != null ? newMessages : new ArrayList<>();
        notifyDataSetChanged();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvTime;
        // Только у исходящих
        TextView tvReadStatus, tvReadAt;
        boolean isSent;

        MessageViewHolder(@NonNull View itemView, boolean isSent) {
            super(itemView);
            this.isSent    = isSent;
            tvText         = itemView.findViewById(R.id.tvMessageText);
            tvTime         = itemView.findViewById(R.id.tvMessageTime);
            if (isSent) {
                tvReadStatus = itemView.findViewById(R.id.tvReadStatus);
                tvReadAt     = itemView.findViewById(R.id.tvReadAt);
            }
        }

        void bind(Message msg) {
            tvText.setText(msg.getText());
            tvTime.setText(timeFormat.format(new Date(msg.getCreatedAt())));

            if (isSent && tvReadStatus != null) {
                if (msg.isRead()) {
                    // ✅✅ Прочитано — двойная галочка синяя
                    tvReadStatus.setText("✓✓");
                    tvReadStatus.setTextColor(0xFFADD8FF);

                    // Показываем время прочтения
                    if (msg.getReadAt() > 0 && tvReadAt != null) {
                        tvReadAt.setVisibility(View.VISIBLE);
                        tvReadAt.setText("прочитано в "
                                + timeFormat.format(new Date(msg.getReadAt())));
                    }
                } else {
                    // ✓ Отправлено — одна галочка серая
                    tvReadStatus.setText("✓");
                    tvReadStatus.setTextColor(0xCCFFFFFF);
                    if (tvReadAt != null) tvReadAt.setVisibility(View.GONE);
                }
            }
        }
    }
}
