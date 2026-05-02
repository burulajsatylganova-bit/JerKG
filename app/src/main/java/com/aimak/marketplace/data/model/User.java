
package com.aimak.marketplace.data.model;

/**
 * Модель пользователя.
 * Хранится в Firestore: collection "users", document = uid.
 */
public class User {

    private String uid;
    private String phone;       // номер телефона (+996...)
    private String name;        // имя пользователя
    private String avatarUrl;   // ссылка на фото профиля в Firebase Storage
    private String region;      // регион КР (Бишкек, Ош, и т.д.)
    private long createdAt;     // timestamp создания аккаунта

    // Firestore требует пустой конструктор
    public User() {}

    public User(String uid, String phone) {
        this.uid = uid;
        this.phone = phone;
        this.createdAt = System.currentTimeMillis();
    }

    // Геттеры и сеттеры
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
