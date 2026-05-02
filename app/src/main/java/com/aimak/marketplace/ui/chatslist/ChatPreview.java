package com.aimak.marketplace.ui.chatslist;

import android.content.Context;
import com.aimak.marketplace.R;

public class ChatPreview {
    private String chatId;
    private String otherUid;
    private String otherName;
    private String lastMessage;
    private long lastMessageAt;
    private boolean hasUnread;

    public ChatPreview(String chatId, String otherUid, String otherName,
                       String lastMessage, long lastMessageAt, boolean hasUnread) {
        this.chatId = chatId;
        this.otherUid = otherUid;
        this.otherName = otherName;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.hasUnread = hasUnread;
    }

    public String getChatId() { return chatId; }
    public String getOtherUid() { return otherUid; }
    public String getOtherName() { return otherName; }
    public String getLastMessage() { return lastMessage != null ? lastMessage : ""; }
    public long getLastMessageAt() { return lastMessageAt; }
    public boolean hasUnread() { return hasUnread; }
    public void setUnread(boolean unread) { this.hasUnread = unread; }

    public String getAvatarLetter() {
        if (otherName != null && !otherName.isEmpty())
            return String.valueOf(otherName.charAt(0)).toUpperCase();
        return "?";
    }

    /** Форматирует время с учётом локализации */
    public String getFormattedTime(Context context) {
        if (lastMessageAt == 0) return "";
        long diff = System.currentTimeMillis() - lastMessageAt;
        long minutes = diff / 60000;
        if (minutes < 1) return context.getString(R.string.time_just_now);
        if (minutes < 60) return minutes + context.getString(R.string.time_min_ago);
        long hours = minutes / 60;
        if (hours < 24) return hours + context.getString(R.string.time_hour_ago);
        long days = hours / 24;
        if (days == 1) return context.getString(R.string.time_yesterday);
        return days + context.getString(R.string.time_day_ago);
    }

    /** Запасной вариант без Context */
    public String getFormattedTime() {
        if (lastMessageAt == 0) return "";
        long diff = System.currentTimeMillis() - lastMessageAt;
        long minutes = diff / 60000;
        if (minutes < 1) return "•";
        if (minutes < 60) return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h";
        return (hours / 24) + "d";
    }
}
