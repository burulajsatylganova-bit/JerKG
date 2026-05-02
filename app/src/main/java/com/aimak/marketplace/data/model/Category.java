package com.aimak.marketplace.data.model;

public class Category {

    private String id;
    private String name;     // название на текущем языке (из strings.xml)
    private int iconRes;
    private String colorHex;

    public Category(String id, String name, int iconRes, String colorHex) {
        this.id = id;
        this.name = name;
        this.iconRes = iconRes;
        this.colorHex = colorHex;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    // Обратная совместимость
    public String getNameRu() { return name; }
    public String getNameKy() { return name; }
    public int getIconRes() { return iconRes; }
    public String getColorHex() { return colorHex; }
}
