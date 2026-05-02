package com.aimak.marketplace.data.repository;
import com.aimak.marketplace.R;
import com.aimak.marketplace.utils.ResUtils;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FavoritesRepository — управление избранными объявлениями.
 * Структура Firestore: favorites/{userId}/ads/{adId} = { adId: true }
 */
public class FavoritesRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FavoritesRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public interface FavoritesCallback {
        void onSuccess(List<String> adIds); // список ID избранных объявлений
        void onError(String message);
    }

    public interface BooleanCallback {
        void onResult(boolean isFavorite);
    }

    public interface VoidCallback {
        void onSuccess();
        void onError(String message);
    }

    private String getCurrentUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    /**
     * Добавить объявление в избранное.
     */
    public void addToFavorites(String adId, VoidCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onError(ResUtils.str(R.string.error_not_authorized_short)); return; }

        Map<String, Object> data = new HashMap<>();
        data.put("adId", adId);
        data.put("addedAt", System.currentTimeMillis());

        db.collection("favorites")
                .document(uid)
                .collection("ads")
                .document(adId)
                .set(data)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Убрать из избранного.
     */
    public void removeFromFavorites(String adId, VoidCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onError(ResUtils.str(R.string.error_not_authorized_short)); return; }

        db.collection("favorites")
                .document(uid)
                .collection("ads")
                .document(adId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Проверить, в избранном ли объявление.
     */
    public void isFavorite(String adId, BooleanCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onResult(false); return; }

        db.collection("favorites")
                .document(uid)
                .collection("ads")
                .document(adId)
                .get()
                .addOnSuccessListener(doc -> callback.onResult(doc.exists()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    /**
     * Получить список ID всех избранных объявлений.
     */
    public void getFavoriteIds(FavoritesCallback callback) {
        String uid = getCurrentUid();
        if (uid == null) { callback.onError(ResUtils.str(R.string.error_not_authorized_short)); return; }

        db.collection("favorites")
                .document(uid)
                .collection("ads")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        ids.add(doc.getId());
                    }
                    callback.onSuccess(ids);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}