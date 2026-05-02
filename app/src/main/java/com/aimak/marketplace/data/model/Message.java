
package com.aimak.marketplace.data.model;

/**
 * Модель сообщения чата.
 * Хранится в Firestore: chats/{chatId}/messages/{messageId}
 */
public class Message {

    private String id;
    private String senderId;    // UID отправителя
    private String text;        // текст сообщения
    private long createdAt;     // timestamp
    private boolean read;       // прочитано ли

    // Firestore требует пустой конструктор
    public Message() {}

    public Message(String senderId, String text) {
        this.senderId = senderId;
        this.text = text;
        this.createdAt = System.currentTimeMillis();
        this.read = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
