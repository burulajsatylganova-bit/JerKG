package com.aimak.marketplace.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.aimak.marketplace.AimakApp;
import com.aimak.marketplace.R;
import com.aimak.marketplace.data.model.Ad;
import com.aimak.marketplace.data.repository.AdRepository;
import com.aimak.marketplace.data.repository.AuthRepository;
import com.aimak.marketplace.databinding.FragmentProfileBinding;
import com.aimak.marketplace.ui.auth.LoginActivity;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private final AdRepository adRepo = new AdRepository();
    private final AuthRepository authRepo = new AuthRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUserInfo();
        setupMyAdsCount();
        setupFavoritesCount();
        setupNotifCount();
        setupMenuItems();
        setupDarkThemeSwitch();
        setupLogout();
        updateCurrentLanguageLabel();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupUserInfo();
        setupMyAdsCount();
        setupFavoritesCount();
        setupNotifCount();
        updateCurrentLanguageLabel();
    }

    private void updateCurrentLanguageLabel() {
        if (binding == null) return;
        String lang = AimakApp.getInstance().getSelectedLanguage();
        binding.tvCurrentLang.setText(
                lang.equals("ky") ? getString(R.string.profile_lang_ky)
                        : getString(R.string.profile_lang_ru));
    }

    private void setupUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String phone = user.getPhoneNumber();
        binding.tvPhone.setText(phone != null ? phone : getString(R.string.profile_phone_default));

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (binding == null) return;
                    String name  = doc.exists() ? doc.getString("name")       : null;
                    String photo = doc.exists() ? doc.getString("photoBase64") : null;

                    binding.tvName.setText(
                            (name != null && !name.isEmpty()) ? name
                                    : getString(R.string.profile_user_default));

                    // Показываем инициал в аватаре
                    if (name != null && !name.isEmpty()) {
                        binding.tvAvatarLetter.setText(
                                String.valueOf(name.charAt(0)).toUpperCase());
                    }

                    // Если есть фото — показываем его
                    if (photo != null && !photo.isEmpty()) {
                        try {
                            String data = photo.replace("data:image/jpeg;base64,", "");
                            byte[] bytes = Base64.decode(data, Base64.NO_WRAP);
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            if (bmp != null) {
                                binding.ivAvatar.setImageBitmap(bmp);
                                binding.ivAvatar.setVisibility(android.view.View.VISIBLE);
                                binding.tvAvatarLetter.setVisibility(android.view.View.GONE);
                            }
                        } catch (Exception e) { /* показываем букву */ }
                    } else {
                        binding.ivAvatar.setVisibility(android.view.View.GONE);
                        binding.tvAvatarLetter.setVisibility(android.view.View.VISIBLE);
                    }
                });
    }

    private void setupMyAdsCount() {
        adRepo.getMyAds(new AdRepository.AdsCallback() {
            @Override public void onSuccess(java.util.List<Ad> ads) {
                if (binding == null) return;
                binding.tvMyAdsCount.setText(ads.size() + getString(R.string.profile_active_ads));
            }
            @Override public void onError(String msg) {}
        });
    }

    private void setupFavoritesCount() {
        String uid = getCurrentUid();
        if (uid == null) return;
        FirebaseFirestore.getInstance()
                .collection("favorites").document(uid).collection("ads").get()
                .addOnSuccessListener(snap -> {
                    if (binding == null) return;
                    int count = snap.size();
                    binding.tvFavCount.setText(count > 0 ? count + " " : "");
                });
    }

    private void setupNotifCount() {
        String uid = getCurrentUid();
        if (uid == null) return;
        FirebaseFirestore.getInstance()
                .collection("notifications").document(uid).collection("items")
                .whereEqualTo("unread", true).get()
                .addOnSuccessListener(snap -> {
                    if (binding == null) return;
                    int count = snap.size();
                    binding.tvNotifCount.setText(count > 0
                            ? count + getString(R.string.profile_new_notif)
                            : getString(R.string.profile_no_notif));
                });
    }

    private void setupMenuItems() {
        binding.itemMyAds.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_profile_to_my_ads));
        binding.itemNotifications.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_profile_to_notifications));
        binding.itemFavorites.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_profile_to_favorites));
        binding.itemLanguage.setOnClickListener(v -> showLanguageDialog());
        binding.itemSettings.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_profile_to_settings));
        binding.itemSupport.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_profile_to_support));
        binding.btnEditProfile.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_profile_to_edit_profile));
    }

    private void setupDarkThemeSwitch() {
        // Читаем сохранённое значение
        boolean isDark = requireContext()
                .getSharedPreferences("AimakPrefs", android.content.Context.MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        binding.switchDarkTheme.setChecked(isDark);

        binding.switchDarkTheme.setOnCheckedChangeListener((btn, checked) -> {
            // Сохраняем выбор
            requireContext()
                    .getSharedPreferences("AimakPrefs", android.content.Context.MODE_PRIVATE)
                    .edit().putBoolean("dark_mode", checked).apply();
            // Применяем тему
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    private void showLanguageDialog() {
        String[] languages = {
                getString(R.string.profile_lang_ru),
                getString(R.string.profile_lang_ky)
        };
        String[] codes = {"ru", "ky"};
        String current = AimakApp.getInstance().getSelectedLanguage();
        int checked = current.equals("ky") ? 1 : 0;

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.profile_lang_dialog_title))
                .setSingleChoiceItems(languages, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (!codes[which].equals(current)) {
                        AimakApp.getInstance().setLanguage(codes[which]);
                        if (getActivity() != null) getActivity().recreate();
                    }
                })
                .show();
    }

    private void setupLogout() {
        binding.btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.profile_logout_title))
                        .setMessage(getString(R.string.profile_logout_msg))
                        .setPositiveButton(getString(R.string.profile_logout_confirm), (d, w) -> {
                            authRepo.signOut();
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton(getString(R.string.btn_cancel), null)
                        .show());
    }

    private String getCurrentUid() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return u != null ? u.getUid() : null;
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
