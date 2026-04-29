package com.jerkg.marketplace.utils;

import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.model.Category;

import java.util.Arrays;
import java.util.List;

public class CategoryHelper {

    public static List<Category> getAllCategories() {
        return Arrays.asList(
                new Category("realty",      "Недвижимость",    "Кыймылсыз мүлк",        R.drawable.ic_cat_realty,      "#4CAF50"),
                new Category("work",        "Работа",          "Жумуш",                  R.drawable.ic_cat_work,        "#2196F3"),
                new Category("clothes",     "Одежда",          "Кийим",                  R.drawable.ic_cat_clothes,     "#E91E63"),
                new Category("cars",        "Автомобили",      "Автоунаа",               R.drawable.ic_cat_cars,        "#FF9800"),
                new Category("animals",     "Животные и скот", "Мал жана жаныбарлар",   R.drawable.ic_cat_animals,     "#795548"),
                new Category("electronics", "Электроника",     "Электроника",            R.drawable.ic_cat_electronics, "#9C27B0"),
                new Category("companion",   "Попутчик",        "Каттам",                 R.drawable.ic_cat_companion,   "#00BCD4"),
                new Category("services",    "Услуги",          "Кызматтар",              R.drawable.ic_cat_services,    "#FF5722")
        );
    }

    public static List<String> getRegions() {
        return Arrays.asList(
                "Все регионы",
                "Бишкек",
                "Ош",
                "Чуйская область",
                "Иссык-Кульская область",
                "Нарынская область",
                "Таласская область",
                "Джалал-Абадская область",
                "Баткенская область"
        );
    }

    public static String getCategoryName(Category category, String lang) {
        if ("ky".equals(lang)) return category.getNameKy();
        return category.getNameRu();
    }

    public static Category getById(String id) {
        for (Category cat : getAllCategories()) {
            if (cat.getId().equals(id)) return cat;
        }
        return null;
    }
}
