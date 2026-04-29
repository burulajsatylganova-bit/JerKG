package com.jerkg.marketplace.ui.favorites;

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

import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.model.Ad;
import com.jerkg.marketplace.data.repository.AdRepository;
import com.jerkg.marketplace.data.repository.FavoritesRepository;
import com.jerkg.marketplace.databinding.FragmentFavoritesBinding;
import com.jerkg.marketplace.ui.home.AdAdapter;

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
        adAdapter = new AdAdapter(new AdAdapter.OnAdClickListener() {
            @Override public void onAdClick(Ad ad) {
                Bundle args = new Bundle();
                args.putString("adId", ad.getId());
                Navigation.findNavController(requireView()).navigate(R.id.action_favorites_to_detail, args);
            }
            @Override public void onFavoriteClick(Ad ad) {
                favRepo.removeFromFavorites(ad.getId(), new FavoritesRepository.VoidCallback() {
                    @Override public void onSuccess() { loadFavorites(); }
                    @Override public void onError(String msg) {}
                });
            }
        });
        binding.rvFavorites.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvFavorites.setAdapter(adAdapter);
        loadFavorites();
    }

    private void loadFavorites() {
        binding.progressBar.setVisibility(View.VISIBLE);
        favRepo.getFavoriteIds(new FavoritesRepository.FavoritesCallback() {
            @Override public void onSuccess(List<String> ids) {
                if (ids.isEmpty()) {
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
                                    binding.progressBar.setVisibility(View.GONE);
                                    adAdapter.setAds(result);
                                    binding.layoutEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
                                });
                            }
                        }
                        @Override public void onError(String m) { rem[0]--; }
                    });
                }
            }
            @Override public void onError(String msg) { binding.progressBar.setVisibility(View.GONE); }
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
