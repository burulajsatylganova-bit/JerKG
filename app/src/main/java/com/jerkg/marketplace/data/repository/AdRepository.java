// Файл: app/src/main/java/com/jerkg/marketplace/data/repository/AdRepository.java
package com.jerkg.marketplace.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jerkg.marketplace.data.model.Ad;

import java.util.ArrayList;
import java.util.List;

/**
 * AdRepository — весь код работы с объявлениями в Firestore.
 * Поддерживает: загрузку, фильтрацию по категории и региону, поиск.
 */
public class AdRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public AdRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // ─────────────────────────────────────────────
    // Callback интерфейсы
    // ─────────────────────────────────────────────

    public interface AdsCallback {
        void onSuccess(List<Ad> ads);
        void onError(String message);
    }

    public interface AdCallback {
        void onSuccess(Ad ad);
        void onError(String message);
    }

    public interface VoidCallback {
        void onSuccess();
        void onError(String message);
    }

    // ─────────────────────────────────────────────
    // Получение объявлений
    // ─────────────────────────────────────────────

    /**
     * Загружает все активные объявления, сортированные по дате (новые первые).
     */
    public void getAllAds(AdsCallback callback) {
        db.collection("ads")
                .whereEqualTo("active", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Ad> ads = parseAds(snapshots.getDocuments());
                    callback.onSuccess(ads);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Объявления по категории.
     */
    public void getAdsByCategory(String categoryId, AdsCallback callback) {
        db.collection("ads")
                .whereEqualTo("active", true)
                .whereEqualTo("category", categoryId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshots -> {
                    callback.onSuccess(parseAds(snapshots.getDocuments()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Объявления по региону.
     */
    public void getAdsByRegion(String region, AdsCallback callback) {
        db.collection("ads")
                .whereEqualTo("active", true)
                .whereEqualTo("region", region)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshots -> {
                    callback.onSuccess(parseAds(snapshots.getDocuments()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Объявления по категории И региону.
     */
    public void getAdsByCategoryAndRegion(String categoryId, String region, AdsCallback callback) {
        db.collection("ads")
                .whereEqualTo("active", true)
                .whereEqualTo("category", categoryId)
                .whereEqualTo("region", region)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshots -> {
                    callback.onSuccess(parseAds(snapshots.getDocuments()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Поиск по ключевым словам (клиентская фильтрация — Firestore не поддерживает full-text search).
     * Для продакшна рекомендуется Algolia или Firebase Extension.
     */
    public void searchAds(String query, AdsCallback callback) {
        // Загружаем все и фильтруем на клиенте
        getAllAds(new AdsCallback() {
            @Override
            public void onSuccess(List<Ad> ads) {
                String lowerQuery = query.toLowerCase().trim();
                List<Ad> filtered = new ArrayList<>();
                for (Ad ad : ads) {
                    if (ad.getTitle().toLowerCase().contains(lowerQuery)
                            || ad.getDescription().toLowerCase().contains(lowerQuery)) {
                        filtered.add(ad);
                    }
                }
                callback.onSuccess(filtered);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    /**
     * Объявления текущего пользователя (для профиля).
     */
    public void getMyAds(AdsCallback callback) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onError("Пользователь не авторизован");
            return;
        }

        db.collection("ads")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    callback.onSuccess(parseAds(snapshots.getDocuments()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Одно объявление по ID.
     */
    public void getAdById(String adId, AdCallback callback) {
        db.collection("ads").document(adId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Ad ad = doc.toObject(Ad.class);
                        if (ad != null) ad.setId(doc.getId());
                        callback.onSuccess(ad);
                    } else {
                        callback.onError("Объявление не найдено");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    /**
     * Добавить новое объявление.
     */
    public void createAd(Ad ad, AdCallback callback) {
        db.collection("ads")
                .add(ad)
                .addOnSuccessListener(ref -> {
                    ad.setId(ref.getId());
                    callback.onSuccess(ad);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Обновить объявление.
     */
    public void updateAd(Ad ad, VoidCallback callback) {
        db.collection("ads").document(ad.getId())
                .set(ad)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Удалить объявление (мягкое удаление — ставим active = false).
     */
    public void deleteAd(String adId, VoidCallback callback) {
        db.collection("ads").document(adId)
                .update("active", false)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void incrementViewCount(String adId) {
        db.collection("ads").document(adId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        long current = doc.getLong("viewCount") != null
                                ? doc.getLong("viewCount") : 0;
                        db.collection("ads").document(adId)
                                .update("viewCount", current + 1);
                    }
                });
    }

    private List<Ad> parseAds(List<? extends com.google.firebase.firestore.DocumentSnapshot> docs) {
        List<Ad> ads = new ArrayList<>();
        for (com.google.firebase.firestore.DocumentSnapshot doc : docs) {
            Ad ad = doc.toObject(Ad.class);
            if (ad != null) {
                ad.setId(doc.getId());
                ads.add(ad);
            }
        }
        return ads;
    }
}