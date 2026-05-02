package com.aimak.marketplace.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.aimak.marketplace.R;
import com.aimak.marketplace.data.model.Ad;
import com.aimak.marketplace.data.repository.AdRepository;
import com.aimak.marketplace.data.repository.FavoritesRepository;
import com.aimak.marketplace.databinding.FragmentFavoritesBinding;
import com.aimak.marketplace.ui.home.AdAdapter;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private AdAdapter adAdapter;
    private final FavoritesRepository favRepo = new FavoritesRepository();
    private final AdRepository adRepo = new AdRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Toolbar с кнопкой назад (так как теперь открывается из профиля)
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());

        adAdapter = new AdAdapter(new AdAdapter.OnAdClickListener() {
            @Override
            public void onAdClick(Ad ad) {
                Bundle args = new Bundle();
                args.putString("adId", ad.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_favorites_to_detail, args);
            }
            @Override
            public void onFavoriteClick(Ad ad) {
                favRepo.removeFromFavorites(ad.getId(), new FavoritesRepository.VoidCallback() {
                    @Override public void onSuccess() {
                        Toast.makeText(getContext(), getString(R.string.favorites_removed), Toast.LENGTH_SHORT).show();
                        loadFavorites();
                    }
                    @Override public void onError(String msg) {}
                });
            }
        });

        binding.rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvFavorites.setAdapter(adAdapter);
        loadFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.rvFavorites.setVisibility(View.GONE);

        favRepo.getFavoriteIds(new FavoritesRepository.FavoritesCallback() {
            @Override
            public void onSuccess(List<String> ids) {
                if (ids.isEmpty()) {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    adAdapter.setAds(new ArrayList<>());
                    return;
                }

                List<Ad> result = new ArrayList<>();
                int[] rem = {ids.size()};
                for (String id : ids) {
                    adRepo.getAdById(id, new AdRepository.AdCallback() {
                        @Override public void onSuccess(Ad ad) {
                            if (ad != null) result.add(ad);
                            if (--rem[0] == 0 && getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (binding == null) return;
                                    binding.progressBar.setVisibility(View.GONE);
                                    adAdapter.setAds(result);
                                    if (result.isEmpty()) {
                                        binding.layoutEmpty.setVisibility(View.VISIBLE);
                                        binding.rvFavorites.setVisibility(View.GONE);
                                    } else {
                                        binding.layoutEmpty.setVisibility(View.GONE);
                                        binding.rvFavorites.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }
                        @Override public void onError(String m) {
                            if (--rem[0] == 0) {
                                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                                    if (binding == null) return;
                                    binding.progressBar.setVisibility(View.GONE);
                                });
                            }
                        }
                    });
                }
            }
            @Override
            public void onError(String msg) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
