package com.jerkg.marketplace.ui.chatslist;

/**
 * Модель превью чата для списка переписок
 */
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

    public String getAvatarLetter() {
        if (otherName != null && !otherName.isEmpty()) {
            return String.valueOf(otherName.charAt(0)).toUpperCase();
        }
        return "?";
    }

    public String getFormattedTime() {
        if (lastMessageAt == 0) return "";
        long diff = System.currentTimeMillis() - lastMessageAt;
        long minutes = diff / 60000;
        if (minutes < 1) return "только что";
        if (minutes < 60) return minutes + " мин";
        long hours = minutes / 60;
        if (hours < 24) return hours + " ч";
        long days = hours / 24;
        if (days == 1) return "вчера";
        return days + " дн";
    }
}