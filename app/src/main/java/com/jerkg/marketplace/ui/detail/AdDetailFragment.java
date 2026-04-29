package com.jerkg.marketplace.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.repository.FavoritesRepository;
import com.jerkg.marketplace.databinding.FragmentAdDetailBinding;

public class AdDetailFragment extends Fragment {

    private FragmentAdDetailBinding binding;
    private AdDetailViewModel viewModel;
    private FavoritesRepository favoritesRepo;
    private boolean isFavorite = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAdDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AdDetailViewModel.class);
        favoritesRepo = new FavoritesRepository();

        String adId = getArguments() != null ? getArguments().getString("adId") : null;
        if (adId == null) {
            Toast.makeText(getContext(), "Ошибка: объявление не найдено", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        viewModel.loadAd(adId);

        setupToolbar();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed());
    }

    private void observeViewModel() {
        viewModel.adLiveData.observe(getViewLifecycleOwner(), ad -> {
            if (ad == null) return;

            binding.toolbar.setTitle(ad.getCategory());
            binding.tvTitle.setText(ad.getTitle());
            binding.tvPrice.setText(ad.getFormattedPrice());
            binding.tvRegion.setText(ad.getRegion());
            binding.tvDescription.setText(ad.getDescription());
            binding.tvViews.setText("Просмотры: " + ad.getViewCount());

            if (ad.getImageUrls() != null && !ad.getImageUrls().isEmpty()) {
                PhotoPagerAdapter pagerAdapter = new PhotoPagerAdapter(ad.getImageUrls());
                binding.viewPagerPhotos.setAdapter(pagerAdapter);
                binding.dotsIndicator.setViewPager2(binding.viewPagerPhotos);
                binding.viewPagerPhotos.setVisibility(View.VISIBLE);
                binding.ivNoPhoto.setVisibility(View.GONE);
            } else {
                binding.viewPagerPhotos.setVisibility(View.GONE);
                binding.ivNoPhoto.setVisibility(View.VISIBLE);
            }

            if (ad.getUserPhone() != null) {
                binding.btnCall.setOnClickListener(v -> {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:" + ad.getUserPhone()));
                    startActivity(callIntent);
                });
                binding.btnCall.setVisibility(View.VISIBLE);
            }

            binding.btnChat.setOnClickListener(v -> {
                String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                if (currentUid == null) return;

                String chatId = currentUid.compareTo(ad.getUserId()) < 0
                        ? currentUid + "_" + ad.getUserId()
                        : ad.getUserId() + "_" + currentUid;

                Bundle args = new Bundle();
                args.putString("chatId", chatId);
                args.putString("otherUserName",
                        ad.getUserName() != null ? ad.getUserName() : "Продавец");
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_detail_to_chat, args);
            });

            String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            boolean isOwner = ad.getUserId() != null && ad.getUserId().equals(currentUid);

            binding.layoutOwnerActions.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            binding.layoutGuestActions.setVisibility(isOwner ? View.GONE : View.VISIBLE);

            if (isOwner) {
                // ✅ ИСПРАВЛЕНО: переход на экран редактирования с передачей adId
                binding.btnEdit.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString("adId", ad.getId());
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_detail_to_edit, args);
                });

                // ✅ ИСПРАВЛЕНО: подтверждение и удаление
                binding.btnDelete.setOnClickListener(v -> confirmDelete(ad.getId()));
            }

            favoritesRepo.isFavorite(ad.getId(), fav -> {
                isFavorite = fav;
                updateFavoriteButton();
            });

            binding.btnFavorite.setOnClickListener(v -> toggleFavorite(ad.getId()));
        });

        viewModel.loadingLiveData.observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.scrollContent.setVisibility(loading ? View.GONE : View.VISIBLE);
        });

        viewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null)
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        viewModel.deletedLiveData.observe(getViewLifecycleOwner(), deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                Toast.makeText(getContext(), "Объявление удалено", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });
    }

    private void toggleFavorite(String adId) {
        if (isFavorite) {
            favoritesRepo.removeFromFavorites(adId, new FavoritesRepository.VoidCallback() {
                @Override public void onSuccess() {
                    isFavorite = false;
                    updateFavoriteButton();
                    Toast.makeText(getContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
                }
                @Override public void onError(String msg) {}
            });
        } else {
            favoritesRepo.addToFavorites(adId, new FavoritesRepository.VoidCallback() {
                @Override public void onSuccess() {
                    isFavorite = true;
                    updateFavoriteButton();
                    Toast.makeText(getContext(), "Добавлено в избранное ❤️", Toast.LENGTH_SHORT).show();
                }
                @Override public void onError(String msg) {}
            });
        }
    }

    private void updateFavoriteButton() {
        if (binding == null) return;
        binding.btnFavorite.setIconResource(
                isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
        binding.btnFavorite.setText(isFavorite ? "В избранном" : "В избранное");
    }

    private void confirmDelete(String adId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Удалить объявление?")
                .setMessage("Объявление будет удалено. Это действие нельзя отменить.")
                .setPositiveButton("Удалить", (d, w) -> viewModel.deleteAd(adId))
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}