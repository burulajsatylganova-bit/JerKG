package com.aimak.marketplace.ui.detail;

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
import com.aimak.marketplace.R;
import com.aimak.marketplace.data.repository.FavoritesRepository;
import com.aimak.marketplace.databinding.FragmentAdDetailBinding;

public class AdDetailFragment extends Fragment {

    private FragmentAdDetailBinding binding;
    private AdDetailViewModel viewModel;
    private FavoritesRepository favoritesRepo;
    private boolean isFavorite = false;
    private PhotoPagerAdapter pagerAdapter;

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
            Toast.makeText(getContext(), getString(R.string.error_ad_not_found), Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        viewModel.loadAd(adId);
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.adLiveData.observe(getViewLifecycleOwner(), ad -> {
            if (ad == null) return;

            binding.toolbar.setTitle(ad.getCategory());
            binding.tvTitle.setText(ad.getTitle());

            // ✅ Цена через strings
            if (ad.isNegotiablePrice()) {
                binding.tvPrice.setText(getString(R.string.price_negotiable_short));
            } else {
                binding.tvPrice.setText(ad.getFormattedPrice() + getString(R.string.currency_som));
            }

            binding.tvRegion.setText(ad.getRegion());
            binding.tvDescription.setText(ad.getDescription());
            binding.tvViews.setText(getString(R.string.ad_views) + ad.getViewCount());

            if (ad.getImageUrls() != null && !ad.getImageUrls().isEmpty()) {
                if (pagerAdapter == null) {
                    pagerAdapter = new PhotoPagerAdapter(ad.getImageUrls());
                    binding.viewPagerPhotos.setAdapter(pagerAdapter);
                    binding.dotsIndicator.setViewPager2(binding.viewPagerPhotos);
                }
                binding.viewPagerPhotos.setVisibility(View.VISIBLE);
                binding.ivNoPhoto.setVisibility(View.GONE);
            } else {
                binding.viewPagerPhotos.setVisibility(View.GONE);
                binding.ivNoPhoto.setVisibility(View.VISIBLE);
            }

            if (ad.getUserPhone() != null) {
                binding.btnCall.setOnClickListener(v -> startActivity(
                        new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + ad.getUserPhone()))));
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
                args.putString("otherUid", ad.getUserId());
                args.putString("otherUserName",
                        ad.getUserName() != null ? ad.getUserName()
                                : getString(R.string.chat_seller));
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_detail_to_chat, args);
            });

            String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            boolean isOwner = ad.getUserId() != null && ad.getUserId().equals(currentUid);

            binding.layoutOwnerActions.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            binding.layoutGuestActions.setVisibility(isOwner ? View.GONE : View.VISIBLE);

            if (isOwner) {
                binding.btnEdit.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putString("adId", ad.getId());
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_detail_to_edit, args);
                });
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
            if (error != null && !error.isEmpty()) {
                String msg = error.contains("PERMISSION_DENIED")
                        ? getString(R.string.error_permission_denied) : error;
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.deletedLiveData.observe(getViewLifecycleOwner(), deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                Toast.makeText(getContext(), getString(R.string.ad_deleted), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), getString(R.string.favorites_removed), Toast.LENGTH_SHORT).show();
                }
                @Override public void onError(String msg) {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            favoritesRepo.addToFavorites(adId, new FavoritesRepository.VoidCallback() {
                @Override public void onSuccess() {
                    isFavorite = true;
                    updateFavoriteButton();
                    Toast.makeText(getContext(), getString(R.string.favorites_added), Toast.LENGTH_SHORT).show();
                }
                @Override public void onError(String msg) {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateFavoriteButton() {
        if (binding == null) return;
        binding.btnFavorite.setIconResource(
                isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
        binding.btnFavorite.setText(
                isFavorite ? getString(R.string.ad_in_favorite) : getString(R.string.ad_favorite));
    }

    private void confirmDelete(String adId) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.ad_confirm_delete_title))
                .setMessage(getString(R.string.ad_confirm_delete_msg))
                .setPositiveButton(getString(R.string.btn_delete), (d, w) -> viewModel.deleteAd(adId))
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
