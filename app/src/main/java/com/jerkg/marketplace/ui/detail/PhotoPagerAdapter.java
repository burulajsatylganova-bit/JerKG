// Файл: app/src/main/java/com/jerkg/marketplace/ui/detail/PhotoPagerAdapter.java
package com.jerkg.marketplace.ui.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jerkg.marketplace.R;
import com.jerkg.marketplace.utils.ImageUtils;

import java.util.List;

public class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder> {

    private final List<String> imageDataList; // Base64 строки

    public PhotoPagerAdapter(List<String> imageDataList) {
        this.imageDataList = imageDataList;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        ImageUtils.loadImage(holder.imageView, imageDataList.get(position));
    }

    @Override
    public int getItemCount() { return imageDataList.size(); }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        PhotoViewHolder(@NonNull View v) {
            super(v);
            imageView = v.findViewById(R.id.ivPhoto);
        }
    }
}