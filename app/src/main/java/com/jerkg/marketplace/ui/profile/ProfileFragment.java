package com.jerkg.marketplace.ui.profile;

import android.content.Intent;
import android.content.res.Configuration;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.model.Ad;
import com.jerkg.marketplace.data.repository.AdRepository;
import com.jerkg.marketplace.data.repository.AuthRepository;
import com.jerkg.marketplace.databinding.FragmentProfileBinding;
import com.jerkg.marketplace.ui.auth.LoginActivity;

import java.util.Locale;

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
    }

    private void setupUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String phone = user.getPhoneNumber();
            binding.tvPhone.setText(phone != null ? phone : "+996 — — —");

            // Отображаем первые буквы как имя пока нет полного имени
            if (phone != null && phone.length() >= 4) {
                binding.tvName.setText("Пользователь");
            }
        }
    }

    private void setupMyAdsCount() {
        adRepo.getMyAds(new AdRepository.AdsCallback() {
            @Override
            public void onSuccess(java.util.List<Ad> ads) {
                if (binding == null) return;
                binding.tvMyAdsCount.setText(ads.size() + " активных");
            }
            @Override
            public void onError(String msg) {}
        });
    }

    private void setupFavoritesCount() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(uid)
                .collection("ads")
                .get()
                .addOnSuccessListener(snap -> {
                    if (binding == null) return;
                    int count = snap.size();
                    binding.tvFavCount.setText(count > 0 ? count + " объявлений" : "");
                });
    }

    private void setupNotifCount() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .document(uid)
                .collection("items")
                .whereEqualTo("unread", true)
                .get()
                .addOnSuccessListener(snap -> {
                    if (binding == null) return;
                    int count = snap.size();
                    binding.tvNotifCount.setText(count > 0 ? count + " новых" : "нет новых");
                });
    }

    private void setupMenuItems() {
        // Мои объявления
        binding.itemMyAds.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_profile_to_my_ads));

        // Уведомления
        binding.itemNotifications.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_profile_to_notifications));

        // Мои избранные
        binding.itemFavorites.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_profile_to_favorites));

        // Язык
        binding.itemLanguage.setOnClickListener(v -> showLanguageDialog());

        // Настройки
        binding.itemSettings.setOnClickListener(v ->
                Toast.makeText(getContext(), "Настройки — скоро", Toast.LENGTH_SHORT).show());

        // Поддержка
        binding.itemSupport.setOnClickListener(v ->
                Toast.makeText(getContext(), "Поддержка — скоро", Toast.LENGTH_SHORT).show());

        // Редактировать профиль
        binding.btnEditProfile.setOnClickListener(v ->
                Toast.makeText(getContext(), "Редактирование профиля — скоро", Toast.LENGTH_SHORT).show());
    }

    private void setupDarkThemeSwitch() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        binding.switchDarkTheme.setChecked(currentMode == AppCompatDelegate.MODE_NIGHT_YES);

        binding.switchDarkTheme.setOnCheckedChangeListener((btn, isChecked) ->
                AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                                : AppCompatDelegate.MODE_NIGHT_NO));
    }

    private void showLanguageDialog() {
        String[] languages = {"Русский", "Кыргызча"};
        String[] codes     = {"ru",      "ky"};

        // Определяем текущий выбранный язык
        String current = Locale.getDefault().getLanguage();
        int checked = current.equals("ky") ? 1 : 0;

        new AlertDialog.Builder(requireContext())
                .setTitle("Выберите язык / Тилди тандаңыз")
                .setSingleChoiceItems(languages, checked, (dialog, which) -> {
                    binding.tvCurrentLang.setText(languages[which]);
                    applyLocale(codes[which]);
                    dialog.dismiss();
                })
                .show();
    }

    private void applyLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration(requireContext().getResources().getConfiguration());
        config.setLocale(locale);
        requireContext().createConfigurationContext(config);

        // Перезапускаем активити чтобы применить язык
        if (getActivity() != null) {
            getActivity().recreate();
        }
    }

    private void setupLogout() {
        binding.btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Выйти из аккаунта?")
                        .setMessage("Вы уверены, что хотите выйти?")
                        .setPositiveButton("Выйти", (d, w) -> {
                            authRepo.signOut();
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Отмена", null)
                        .show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}