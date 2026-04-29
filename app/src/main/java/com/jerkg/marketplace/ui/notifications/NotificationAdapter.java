package com.jerkg.marketplace.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerkg.marketplace.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    private final List<Notification> items;

    public NotificationAdapter(List<Notification> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Notification n = items.get(pos);
        h.tvIcon.setText(n.getIcon());
        h.tvTitle.setText(n.getTitle());
        h.tvMessage.setText(n.getMessage());
        h.tvTime.setText(n.getTime());
        h.dotUnread.setVisibility(n.isUnread() ? View.VISIBLE : View.INVISIBLE);

        // При нажатии — помечаем как прочитанное
        h.itemView.setOnClickListener(v -> {
            n.setUnread(false);
            notifyItemChanged(pos);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle, tvMessage, tvTime;
        View dotUnread;

        VH(@NonNull View v) {
            super(v);
            tvIcon    = v.findViewById(R.id.tvIcon);
            tvTitle   = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime    = v.findViewById(R.id.tvTime);
            dotUnread = v.findViewById(R.id.dotUnread);
        }
    }
}