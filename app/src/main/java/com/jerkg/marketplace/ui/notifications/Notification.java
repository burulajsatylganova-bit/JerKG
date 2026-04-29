package com.jerkg.marketplace.ui.notifications;

public class Notification {
    private String icon;
    private String title;
    private String message;
    private String time;
    private boolean unread;

    public Notification(String icon, String title, String message, String time, boolean unread) {
        this.icon = icon;
        this.title = title;
        this.message = message;
        this.time = time;
        this.unread = unread;
    }

    public String getIcon() { return icon; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public boolean isUnread() { return unread; }
    public void setUnread(boolean unread) { this.unread = unread; }
}