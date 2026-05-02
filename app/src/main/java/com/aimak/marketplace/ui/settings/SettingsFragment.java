package com.aimak.marketplace.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aimak.marketplace.R;
import com.aimak.marketplace.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private static final String PREFS = "AimakPrefs";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());

        loadSettings();
        setupListeners();

        // Версия приложения
        try {
            String version = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            binding.tvVersion.setText(version);
        } catch (Exception e) {
            binding.tvVersion.setText("1.0.0");
        }
    }

    private void loadSettings() {
        var prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        binding.switchNotifMessages.setChecked(prefs.getBoolean("notif_messages", true));
        binding.switchNotifNewAds.setChecked(prefs.getBoolean("notif_new_ads", false));
        binding.switchShowPhone.setChecked(prefs.getBoolean("show_phone", true));
        binding.switchShowOnline.setChecked(prefs.getBoolean("show_online", true));
    }

    private void setupListeners() {
        var prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        binding.switchNotifMessages.setOnCheckedChangeListener((v, checked) ->
                prefs.edit().putBoolean("notif_messages", checked).apply());

        binding.switchNotifNewAds.setOnCheckedChangeListener((v, checked) ->
                prefs.edit().putBoolean("notif_new_ads", checked).apply());

        binding.switchShowPhone.setOnCheckedChangeListener((v, checked) ->
                prefs.edit().putBoolean("show_phone", checked).apply());

        binding.switchShowOnline.setOnCheckedChangeListener((v, checked) ->
                prefs.edit().putBoolean("show_online", checked).apply());

        binding.itemClearCache.setOnClickListener(v -> confirmClearCache());
    }

    private void confirmClearCache() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.settings_clear_cache))
                .setMessage(getString(R.string.settings_clear_cache_msg))
                .setPositiveButton(getString(R.string.btn_delete), (d, w) -> {
                    // Очищаем кэш приложения
                    try {
                        requireContext().getCacheDir().delete();
                        Toast.makeText(getContext(),
                                getString(R.string.settings_cache_cleared),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
