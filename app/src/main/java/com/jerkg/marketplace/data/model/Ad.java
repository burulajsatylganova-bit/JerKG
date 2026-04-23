// Файл: app/src/main/java/com/jerkg/marketplace/data/model/Ad.java
package com.jerkg.marketplace.data.model;

import java.util.List;

/**
 * Модель объявления.
 * Хранится в Firestore: collection "ads", document = auto-generated ID.
 */
public class Ad {

    private String id;
    private String userId;          // UID автора
    private String userPhone;       // телефон автора
    private String userName;        // имя автора
    private String title;           // название объявления
    private String description;     // описание
    private double price;           // цена (0 = договорная)
    private boolean negotiable;     // договорная цена
    private String category;        // категория (electronics, cars, ...)
    private String subcategory;     // подкатегория
    private String region;          // регион КР
    private List<String> imageUrls; // ссылки на фото в Firebase Storage
    private long createdAt;         // timestamp
    private boolean active;         // активно ли объявление
    private int viewCount;          // количество просмотров

    // Firestore требует пустой конструктор
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

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
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

    /** Форматированная цена для отображения */
    public String getFormattedPrice() {
        if (negotiable || price == 0) return "Договорная";
        if (price == Math.floor(price)) {
            return String.format("%,.0f сом", price);
        }
        return String.format("%,.0f сом", price);
    }

    /** Первое фото объявления (для превью в списке) */
    public String getFirstImage() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }
}
