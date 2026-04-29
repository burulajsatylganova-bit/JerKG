package com.jerkg.marketplace.ui.addad;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jerkg.marketplace.R;

import java.util.List;


public class SelectedPhotoAdapter extends RecyclerView.Adapter<SelectedPhotoAdapter.PhotoVH> {

    private final List<Uri> uris;
    private final OnRemoveListener removeListener;

    public interface OnRemoveListener {
        void onRemove(Uri uri);
    }

    public SelectedPhotoAdapter(List<Uri> uris, OnRemoveListener removeListener) {
        this.uris = uris;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public PhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_photo, parent, false);
        return new PhotoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoVH holder, int position) {
        Uri uri = uris.get(position);
        Glide.with(holder.ivPhoto.getContext())
                .load(uri)
                .centerCrop()
                .into(holder.ivPhoto);
        holder.ivRemove.setOnClickListener(v -> removeListener.onRemove(uri));
    }

    @Override
    public int getItemCount() { return uris.size(); }

    static class PhotoVH extends RecyclerView.ViewHolder {
        ImageView ivPhoto, ivRemove;
        PhotoVH(@NonNull View v) {
            super(v);
            ivPhoto  = v.findViewById(R.id.ivSelectedPhoto);
            ivRemove = v.findViewById(R.id.ivRemovePhoto);
        }
    }
}
