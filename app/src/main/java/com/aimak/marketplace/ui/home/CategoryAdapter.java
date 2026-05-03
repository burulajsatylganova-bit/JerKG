package com.aimak.marketplace.ui.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aimak.marketplace.R;
import com.aimak.marketplace.data.model.Category;

import java.util.List;

/**
 * CategoryAdapter — горизонтальный список категорий на главном экране.
 * Активная категория подсвечивается.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private String selectedCategoryId = null; // null = "Все"
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category); // null = "Все категории"
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // Позиция 0 — "Все"
        if (position == 0) {
            holder.bindAll();
        } else {
            holder.bind(categories.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return categories.size() + 1; // +1 для "Все"
    }

    public void setSelectedCategory(String categoryId) {
        this.selectedCategoryId = categoryId;
        notifyDataSetChanged();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivIcon;
        private final TextView tvName;
        private final View vBackground;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon     = itemView.findViewById(R.id.ivCategoryIcon);
            tvName     = itemView.findViewById(R.id.tvCategoryName);
            vBackground = itemView.findViewById(R.id.vCategoryBg);
        }

        public void bindAll() {
            tvName.setText(itemView.getContext().getString(com.aimak.marketplace.R.string.cat_all));
            ivIcon.setImageResource(R.drawable.ic_cat_all);
            boolean isSelected = (selectedCategoryId == null);
            applySelection(isSelected, "#1A6EFF");
            itemView.setOnClickListener(v -> {
                setSelectedCategory(null);
                listener.onCategoryClick(null);
            });
        }

        public void bind(Category category) {
            // Название уже локализовано в Category через CategoryHelper(context)
            tvName.setText(category.getName());
            ivIcon.setImageResource(category.getIconRes());
            boolean isSelected = category.getId().equals(selectedCategoryId);
            applySelection(isSelected, category.getColorHex());
            itemView.setOnClickListener(v -> {
                setSelectedCategory(category.getId());
                listener.onCategoryClick(category);
            });
        }

        private void applySelection(boolean selected, String colorHex) {
            int color;
            try {
                color = Color.parseColor(colorHex);
            } catch (Exception e) {
                color = Color.parseColor("#1A6EFF");
            }

            if (selected) {
                // ✅ Выбрано: цветная рамка + очень светлый фон того же цвета
                // Делаем цвет фона прозрачным (10% от цвета)
                int bgColor = Color.argb(30,
                        Color.red(color), Color.green(color), Color.blue(color));
                vBackground.getBackground().mutate()
                        .setTint(bgColor);

                // Рисуем рамку программно через обводку
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                bg.setCornerRadius(16f * itemView.getContext().getResources().getDisplayMetrics().density);
                bg.setColor(bgColor);
                bg.setStroke(Math.round(2f * itemView.getContext().getResources().getDisplayMetrics().density), color);
                vBackground.setBackground(bg);

                tvName.setTextColor(color);
                tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                // Не выбрано: нейтральный светло-серый фон
                android.graphics.drawable.GradientDrawable bg =
                        new android.graphics.drawable.GradientDrawable();
                bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                bg.setCornerRadius(16f * itemView.getContext().getResources().getDisplayMetrics().density);
                // Адаптируемся к тёмной теме
                boolean isDark = (itemView.getContext().getResources().getConfiguration().uiMode
                        & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                        == android.content.res.Configuration.UI_MODE_NIGHT_YES;
                bg.setColor(isDark ? Color.parseColor("#1E2530") : Color.parseColor("#F0F4FF"));
                bg.setStroke(0, Color.TRANSPARENT);
                vBackground.setBackground(bg);

                tvName.setTextColor(isDark ? Color.parseColor("#8B949E") : Color.parseColor("#4A5568"));
                tvName.setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }
}