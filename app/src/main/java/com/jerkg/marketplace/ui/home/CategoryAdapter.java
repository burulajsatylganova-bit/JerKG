// Файл: app/src/main/java/com/jerkg/marketplace/ui/home/CategoryAdapter.java
package com.jerkg.marketplace.ui.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerkg.marketplace.JerKGApp;
import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.model.Category;
import com.jerkg.marketplace.utils.CategoryHelper;

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
            String lang = JerKGApp.getInstance().getSelectedLanguage();
            tvName.setText("ky".equals(lang) ? "Баары" : "Все");
            ivIcon.setImageResource(R.drawable.ic_cat_all);
            boolean isSelected = (selectedCategoryId == null);
            applySelection(isSelected, "#1A6EFF");
            itemView.setOnClickListener(v -> {
                setSelectedCategory(null);
                listener.onCategoryClick(null);
            });
        }

        public void bind(Category category) {
            String lang = JerKGApp.getInstance().getSelectedLanguage();
            tvName.setText(CategoryHelper.getCategoryName(category, lang));
            ivIcon.setImageResource(category.getIconRes());
            boolean isSelected = category.getId().equals(selectedCategoryId);
            applySelection(isSelected, category.getColorHex());
            itemView.setOnClickListener(v -> {
                setSelectedCategory(category.getId());
                listener.onCategoryClick(category);
            });
        }

        private void applySelection(boolean selected, String colorHex) {
            if (selected) {
                try {
                    vBackground.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor(colorHex)));
                } catch (Exception e) {
                    vBackground.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#1A6EFF")));
                }
                tvName.setTextColor(Color.WHITE);
            } else {
                vBackground.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#F0F4FF")));
                tvName.setTextColor(Color.parseColor("#4A5568"));
            }
        }
    }
}
