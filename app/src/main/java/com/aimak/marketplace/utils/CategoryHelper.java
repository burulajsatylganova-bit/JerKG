package com.aimak.marketplace.utils;

import android.content.Context;

import com.aimak.marketplace.R;
import com.aimak.marketplace.data.model.Category;

import java.util.Arrays;
import java.util.List;

public class CategoryHelper {

    /**
     * Список категорий. Название берётся из strings.xml текущего языка.
     */
    public static List<Category> getAllCategories(Context context) {
        return Arrays.asList(
                new Category("realty",      context.getString(R.string.cat_realty),      R.drawable.ic_cat_realty,      "#4CAF50"),
                new Category("work",        context.getString(R.string.cat_work),        R.drawable.ic_cat_work,        "#2196F3"),
                new Category("clothes",     context.getString(R.string.cat_clothes),     R.drawable.ic_cat_clothes,     "#E91E63"),
                new Category("cars",        context.getString(R.string.cat_cars),        R.drawable.ic_cat_cars,        "#FF9800"),
                new Category("animals",     context.getString(R.string.cat_animals),     R.drawable.ic_cat_animals,     "#795548"),
                new Category("electronics", context.getString(R.string.cat_electronics), R.drawable.ic_cat_electronics, "#9C27B0"),
                new Category("companion",   context.getString(R.string.cat_companion),   R.drawable.ic_cat_companion,   "#00BCD4"),
                new Category("services",    context.getString(R.string.cat_services),    R.drawable.ic_cat_services,    "#FF5722")
        );
    }

    /**
     * Список регионов из strings.xml текущего языка.
     */
    public static List<String> getRegions(Context context) {
        return Arrays.asList(
                context.getString(R.string.region_all),
                context.getString(R.string.region_bishkek),
                context.getString(R.string.region_osh),
                context.getString(R.string.region_chui),
                context.getString(R.string.region_issykkul),
                context.getString(R.string.region_naryn),
                context.getString(R.string.region_talas),
                context.getString(R.string.region_jalal),
                context.getString(R.string.region_batken)
        );
    }

    /**
     * Найти категорию по id.
     */
    public static Category getById(Context context, String id) {
        for (Category cat : getAllCategories(context)) {
            if (cat.getId().equals(id)) return cat;
        }
        return null;
    }

    /**
     * Обратная совместимость — без Context (только id).
     */
    public static String getCategoryId(int position) {
        String[] ids = {"realty","work","clothes","cars","animals","electronics","companion","services"};
        if (position >= 0 && position < ids.length) return ids[position];
        return "";
    }
}
