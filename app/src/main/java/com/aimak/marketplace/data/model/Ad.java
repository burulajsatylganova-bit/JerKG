package com.aimak.marketplace.data.model;

import java.util.List;

public class Ad {

    private String id;
    private String userId;
    private String userPhone;
    private String userName;
    private String title;
    private String description;
    private double price;
    private boolean negotiable;
    private String category;
    private String subcategory;
    private String region;
    private List<String> imageUrls;
    private long createdAt;
    private boolean active;
    private int viewCount;

    public Ad() {}

    public Ad(String userId, String title, String description,
              double price, String category, String region) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.region = region;
        this.createdAt = System.currentTimeMillis();
        this.active = true;
        this.viewCount = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getTitle() { return title != null ? title : ""; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isNegotiable() { return negotiable; }
    public void setNegotiable(boolean negotiable) { this.negotiable = negotiable; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    /**
     * Форматированная цена — без хардкода, возвращает числовое значение.
     * Для отображения "Договорная" используй getString(R.string.price_negotiable_short) в UI.
     */
    public String getFormattedPrice() {
        if (negotiable || price == 0) return null; // null = договорная, обрабатывается в UI
        return String.format("%,.0f", price);
    }

    public boolean isNegotiablePrice() {
        return negotiable || price == 0;
    }

    public String getFirstImage() {
        if (imageUrls != null && !imageUrls.isEmpty()) return imageUrls.get(0);
        return null;
    }
}
