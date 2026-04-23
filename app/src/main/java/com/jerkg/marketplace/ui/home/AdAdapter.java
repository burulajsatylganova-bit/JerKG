// Файл: app/src/main/java/com/jerkg/marketplace/ui/home/AdAdapter.java
package com.jerkg.marketplace.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.model.Ad;
import com.jerkg.marketplace.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class AdAdapter extends RecyclerView.Adapter<AdAdapter.AdViewHolder> {

    private List<Ad> ads = new ArrayList<>();
    private final OnAdClickListener listener;

    public interface OnAdClickListener {
        void onAdClick(Ad ad);
        void onFavoriteClick(Ad ad);
    }

    public AdAdapter(OnAdClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ad, parent, false);
        return new AdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        holder.bind(ads.get(position));
    }

    @Override
    public int getItemCount() { return ads.size(); }

    public void setAds(List<Ad> newAds) {
        this.ads = newAds != null ? newAds : new ArrayList<>();
        notifyDataSetChanged();
    }

    class AdViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivPhoto;
        private final TextView tvTitle;
        private final TextView tvPrice;
        private final TextView tvLocation;
        private final ImageView ivFavorite;

        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto    = itemView.findViewById(R.id.ivAdPhoto);
            tvTitle    = itemView.findViewById(R.id.tvAdTitle);
            tvPrice    = itemView.findViewById(R.id.tvAdPrice);
            tvLocation = itemView.findViewById(R.id.tvAdLocation);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }

        public void bind(Ad ad) {
            tvTitle.setText(ad.getTitle());
            tvPrice.setText(ad.getFormattedPrice());
            tvLocation.setText(ad.getRegion() != null ? ad.getRegion() : "");

            // Загружаем Base64 фото
            ImageUtils.loadImage(ivPhoto, ad.getFirstImage());

            itemView.setOnClickListener(v -> listener.onAdClick(ad));
            ivFavorite.setOnClickListener(v -> listener.onFavoriteClick(ad));
        }
    }
}