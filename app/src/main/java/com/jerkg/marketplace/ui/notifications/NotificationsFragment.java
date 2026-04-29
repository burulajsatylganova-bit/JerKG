package com.jerkg.marketplace.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jerkg.marketplace.databinding.FragmentNotificationsBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;
    private final List<Notification> notifications = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        adapter = new NotificationAdapter(notifications);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid == null) {
            showEmpty();
            return;
        }

        // Читаем уведомления из Firestore: notifications/{userId}/items
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(uid)
                .collection("items")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    notifications.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String icon    = doc.getString("icon") != null ? doc.getString("icon") : "🔔";
                        String title   = doc.getString("title") != null ? doc.getString("title") : "";
                        String message = doc.getString("message") != null ? doc.getString("message") : "";
                        String time    = formatTime(doc.getLong("createdAt"));
                        Boolean unread = doc.getBoolean("unread");
                        notifications.add(new Notification(icon, title, message, time,
                                unread != null && unread));
                    }

                    if (notifications.isEmpty()) {
                        showEmpty();
                    } else {
                        binding.layoutEmpty.setVisibility(View.GONE);
                        binding.rvNotifications.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    showEmpty();
                });
    }

    private void showEmpty() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.GONE);
        binding.rvNotifications.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
    }

    private String formatTime(Long timestamp) {
        if (timestamp == null) return "";
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60000;
        if (minutes < 1) return "только что";
        if (minutes < 60) return minutes + " мин назад";
        long hours = minutes / 60;
        if (hours < 24) return hours + " ч назад";
        long days = hours / 24;
        return days + " дн назад";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}