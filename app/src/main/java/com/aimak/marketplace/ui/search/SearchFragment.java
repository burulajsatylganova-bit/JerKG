
package com.aimak.marketplace.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.chip.Chip;
import com.aimak.marketplace.R;
import com.aimak.marketplace.data.model.Ad;
import com.aimak.marketplace.data.model.Category;
import com.aimak.marketplace.data.repository.AdRepository;
import com.aimak.marketplace.databinding.FragmentSearchBinding;
import com.aimak.marketplace.ui.home.AdAdapter;
import com.aimak.marketplace.utils.CategoryHelper;

import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private AdAdapter adAdapter;
    private final AdRepository adRepo = new AdRepository();
    private String selectedCategory = null;
    private String selectedRegion = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adAdapter = new AdAdapter(new AdAdapter.OnAdClickListener() {
            @Override public void onAdClick(Ad ad) {
                Bundle args = new Bundle();
                args.putString("adId", ad.getId());
                Navigation.findNavController(requireView()).navigate(R.id.action_search_to_detail, args);
            }
            @Override public void onFavoriteClick(Ad ad) {}
        });
        binding.rvResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvResults.setAdapter(adAdapter);

        // Поиск по тексту
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().isEmpty()) loadAds();
                else searchWithQuery(s.toString().trim());
            }
        });

        setupCategoryChips();
        setupRegionChips();
        loadAds();
    }

    private void setupCategoryChips() {
        addChip(binding.chipGroupCategories, getString(R.string.all_label), null, true, true);
        for (Category c : CategoryHelper.getAllCategories(requireContext())) {
            addChip(binding.chipGroupCategories, c.getNameRu(), c.getId(), false, true);
        }
    }

    private void setupRegionChips() {
        List<String> regions = CategoryHelper.getRegions(requireContext());
        for (int i = 0; i < regions.size(); i++) {
            String region = regions.get(i);
            boolean isFirst = (i == 0);
            Chip chip = new Chip(requireContext());
            chip.setText(region);
            chip.setCheckable(true);
            chip.setChecked(isFirst);
            chip.setChipBackgroundColorResource(R.color.badge_blue_bg);
            chip.setTextColor(getResources().getColor(R.color.text_secondary, null));
            chip.setOnCheckedChangeListener((b, checked) -> {
                if (checked) {
                    selectedRegion = getString(R.string.region_all).equals(region) ? null : region;
                    loadAds();
                }
            });
            binding.chipGroupRegions.addView(chip);
        }
    }

    private void addChip(com.google.android.material.chip.ChipGroup group,
                         String label, String id, boolean checked, boolean isCategory) {
        Chip chip = new Chip(requireContext());
        chip.setText(label);
        chip.setCheckable(true);
        chip.setChecked(checked);
        chip.setChipBackgroundColorResource(R.color.badge_blue_bg);
        chip.setTextColor(getResources().getColor(R.color.brand_blue, null));
        chip.setOnCheckedChangeListener((b, isChecked) -> {
            if (isChecked) { selectedCategory = id; loadAds(); }
        });
        group.addView(chip);
    }

    private void loadAds() {
        binding.progressBar.setVisibility(View.VISIBLE);
        AdRepository.AdsCallback cb = new AdRepository.AdsCallback() {
            @Override public void onSuccess(List<Ad> ads) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                adAdapter.setAds(ads);
                binding.tvResultCount.setText(getString(R.string.found_count) + ads.size());
                binding.layoutEmpty.setVisibility(ads.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(String m) {
                if (binding != null) binding.progressBar.setVisibility(View.GONE);
            }
        };
        if (selectedCategory != null && selectedRegion != null)
            adRepo.getAdsByCategoryAndRegion(selectedCategory, selectedRegion, cb);
        else if (selectedCategory != null) adRepo.getAdsByCategory(selectedCategory, cb);
        else if (selectedRegion != null) adRepo.getAdsByRegion(selectedRegion, cb);
        else adRepo.getAllAds(cb);
    }

    private void searchWithQuery(String query) {
        binding.progressBar.setVisibility(View.VISIBLE);
        adRepo.searchAds(query, new AdRepository.AdsCallback() {
            @Override public void onSuccess(List<Ad> ads) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                adAdapter.setAds(ads);
                binding.tvResultCount.setText(getString(R.string.found_count) + ads.size());
                binding.layoutEmpty.setVisibility(ads.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(String m) {
                if (binding != null) binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
