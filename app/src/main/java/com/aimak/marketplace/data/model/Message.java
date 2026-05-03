package com.aimak.marketplace.data.model;

public class Message {

    private String id;
    private String senderId;
    private String text;
    private long createdAt;
    private boolean read;
    private long readAt; // ✅ время прочтения

    public Message() {}

    public Message(String senderId, String text) {
        this.senderId  = senderId;
        this.text      = text;
        this.createdAt = System.currentTimeMillis();
        this.read      = false;
        this.readAt    = 0;
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
    public long getReadAt() { return readAt; }
    public void setReadAt(long readAt) { this.readAt = readAt; }
}
