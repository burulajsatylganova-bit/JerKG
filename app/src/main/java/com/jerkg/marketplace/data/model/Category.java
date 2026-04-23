// Файл: app/src/main/java/com/jerkg/marketplace/data/model/Category.java
package com.jerkg.marketplace.data.model;

/**
 * Модель категории объявления.
 * Используется в списке категорий на главном экране.
 */
public class Category {

    private String id;       // ключ: "electronics", "cars", ...
    private String nameRu;   // название на русском
    private String nameKy;   // название на кыргызском
    private int iconRes;     // ресурс иконки (drawable)
    private String colorHex; // цвет фона плашки

    public Category(String id, String nameRu, String nameKy, int iconRes, String colorHex) {
        this.id = id;
        this.nameRu = nameRu;
        this.nameKy = nameKy;
        this.iconRes = iconRes;
        this.colorHex = colorHex;
    }

    public String getId() { return id; }
    public String getNameRu() { return nameRu; }
    public String getNameKy() { return nameKy; }
    public int getIconRes() { return iconRes; }
    public String getColorHex() { return colorHex; }
}
