package com.jerkg.marketplace.ui.addad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jerkg.marketplace.data.model.Ad;
import com.jerkg.marketplace.data.repository.AdRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class AddAdViewModel extends ViewModel {

    private final AdRepository adRepository = new AdRepository();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> successLiveData = new MutableLiveData<>(false);
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public void publishAd(
            Context context,
            String title,
            String description,
            double price,
            boolean negotiable,
            String categoryId,
            String region,
            List<Uri> photoUris
    ) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            errorLiveData.setValue("Необходима авторизация");
            return;
        }

        loadingLiveData.setValue(true);

        // Конвертируем фото в Base64 в фоновом потоке
        new Thread(() -> {
            List<String> base64Images = new ArrayList<>();
            for (Uri uri : photoUris) {
                String base64 = convertToBase64(context, uri);
                if (base64 != null) {
                    base64Images.add(base64);
                }
            }
            saveAd(user, title, description, price, negotiable, categoryId, region, base64Images);
        }).start();
    }


    private String convertToBase64(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(inputStream);

            // Сжимаем до 400x400 чтобы не превысить лимит Firestore (1MB на документ)
            int maxSize = 400;
            int width = original.getWidth();
            int height = original.getHeight();
            float scale = Math.min((float) maxSize / width, (float) maxSize / height);
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);

            Bitmap resized = Bitmap.createScaledBitmap(original, newWidth, newHeight, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 60, baos); // качество 60%
            byte[] bytes = baos.toByteArray();

            return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveAd(FirebaseUser user, String title, String description,
                        double price, boolean negotiable,
                        String categoryId, String region, List<String> imageBase64List) {
        Ad ad = new Ad(user.getUid(), title, description, price, categoryId, region);
        ad.setNegotiable(negotiable);
        ad.setImageUrls(imageBase64List); // Base64 строки вместо URL
        ad.setUserPhone(user.getPhoneNumber());

        adRepository.createAd(ad, new AdRepository.AdCallback() {
            @Override
            public void onSuccess(Ad createdAd) {
                loadingLiveData.postValue(false);
                successLiveData.postValue(true);
            }
            @Override
            public void onError(String message) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(message);
            }
        });
    }
}