package com.aimak.marketplace.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.aimak.marketplace.R;


public class ImageUtils {

    public static void loadImage(ImageView imageView, String imageData) {
        if (imageData == null || imageData.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }

        try {
            // Base64 строка
            String base64 = imageData;
            if (base64.contains(",")) {
                base64 = base64.split(",")[1]; // убираем "data:image/jpeg;base64,"
            }
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.ic_image_placeholder);
            }
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
        }
    }
}