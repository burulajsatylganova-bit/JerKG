// Файл: app/src/main/java/com/jerkg/marketplace/ui/chat/MessageAdapter.java
package com.jerkg.marketplace.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MessageAdapter — адаптер для списка сообщений чата.
 *
 * Два типа view:
 * - VIEW_TYPE_SENT   (1) — сообщения текущего пользователя (справа, синие)
 * - VIEW_TYPE_RECEIVED (0) — сообщения собеседника (слева, серые)
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_RECEIVED = 0;
    private static final int VIEW_TYPE_SENT = 1;

    private List<Message> messages = new ArrayList<>();
    private final String currentUid;
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(String currentUid) {
        this.currentUid = currentUid;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        return currentUid.equals(msg.getSenderId()) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = (viewType == VIEW_TYPE_SENT)
                ? R.layout.item_message_sent
                : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new MessageViewHolder(view);
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

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
        }

        void bind(Message message) {
            tvText.setText(message.getText());
            tvTime.setText(timeFormat.format(new Date(message.getCreatedAt())));
        }
    }
}
