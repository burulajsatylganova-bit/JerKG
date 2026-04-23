// Файл: app/src/main/java/com/jerkg/marketplace/ui/profile/ProfileFragment.java
package com.jerkg.marketplace.ui.profile;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.model.Ad;
import com.jerkg.marketplace.data.repository.AdRepository;
import com.jerkg.marketplace.data.repository.AuthRepository;
import com.jerkg.marketplace.databinding.FragmentProfileBinding;
import com.jerkg.marketplace.ui.auth.LoginActivity;
import com.jerkg.marketplace.ui.home.AdAdapter;

import androidx.recyclerview.widget.GridLayoutManager;

/**
 * ProfileFragment — экран профиля пользователя.
 * Показывает номер телефона, мои объявления, кнопку выхода.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private final AdRepository adRepo = new AdRepository();
    private final AuthRepository authRepo = new AuthRepository();
    private AdAdapter myAdsAdapter;

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
        setupMyAds();
        setupLogout();
    }

    private void setupUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            binding.tvPhone.setText(user.getPhoneNumber());
        }
    }

    private void setupMyAds() {
        myAdsAdapter = new AdAdapter(new AdAdapter.OnAdClickListener() {
            @Override public void onAdClick(Ad ad) {
                Bundle args = new Bundle();
                args.putString("adId", ad.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_profile_to_my_ads, args);
            }
            @Override public void onFavoriteClick(Ad ad) { /* не нужно в профиле */ }
        });

        binding.rvMyAds.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvMyAds.setAdapter(myAdsAdapter);

        loadMyAds();
    }

    private void loadMyAds() {
        binding.progressBar.setVisibility(View.VISIBLE);
        adRepo.getMyAds(new AdRepository.AdsCallback() {
            @Override public void onSuccess(java.util.List<Ad> ads) {
                binding.progressBar.setVisibility(View.GONE);
                myAdsAdapter.setAds(ads);
                binding.tvMyAdsCount.setText("Мои объявления (" + ads.size() + ")");
                binding.layoutNoAds.setVisibility(ads.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(String msg) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLogout() {
        binding.btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Выйти из аккаунта?")
                    .setPositiveButton("Выйти", (d, w) -> {
                        authRepo.signOut();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
