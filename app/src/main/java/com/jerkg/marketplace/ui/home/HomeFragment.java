// Файл: app/src/main/java/com/jerkg/marketplace/ui/home/HomeFragment.java
package com.jerkg.marketplace.ui.home;

import android.app.AlertDialog;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.repository.FavoritesRepository;
import com.jerkg.marketplace.databinding.FragmentHomeBinding;
import com.jerkg.marketplace.utils.CategoryHelper;

/**
 * HomeFragment — главный экран приложения.
 *
 * Содержит:
 * - Строку поиска
 * - Горизонтальный список категорий
 * - Выбор региона (кнопка фильтра)
 * - Сетку объявлений (2 колонки)
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private AdAdapter adAdapter;
    private CategoryAdapter categoryAdapter;
    private final FavoritesRepository favoritesRepo = new FavoritesRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecyclerViews();
        setupSearch();
        setupRegionFilter();
        observeViewModel();
    }

    private void setupRecyclerViews() {
        // Категории — горизонтальный список
        categoryAdapter = new CategoryAdapter(
                CategoryHelper.getAllCategories(),
                category -> {
                    if (category == null) {
                        viewModel.clearFilters();
                    } else {
                        viewModel.setCategory(category.getId());
                    }
                }
        );
        binding.rvCategories.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoryAdapter);

        // Объявления — сетка 2 колонки
        adAdapter = new AdAdapter(new AdAdapter.OnAdClickListener() {
            @Override
            public void onAdClick(com.jerkg.marketplace.data.model.Ad ad) {
                // Переход на экран деталей (Этап 3)
                Bundle args = new Bundle();
                args.putString("adId", ad.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_home_to_detail, args);
            }

            @Override
            public void onFavoriteClick(com.jerkg.marketplace.data.model.Ad ad) {
                toggleFavorite(ad);
            }
        });
        binding.rvAds.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvAds.setAdapter(adAdapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.search(s.toString());
            }
        });
    }

    private void setupRegionFilter() {
        binding.btnRegion.setOnClickListener(v -> showRegionDialog());
    }

    private void showRegionDialog() {
        String[] regions = CategoryHelper.getRegions().toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Выберите регион")
                .setItems(regions, (dialog, which) -> {
                    String region = regions[which];
                    binding.btnRegion.setText(region);
                    viewModel.setRegion(region);
                })
                .show();
    }

    private void toggleFavorite(com.jerkg.marketplace.data.model.Ad ad) {
        favoritesRepo.isFavorite(ad.getId(), isFav -> {
            if (isFav) {
                favoritesRepo.removeFromFavorites(ad.getId(), new FavoritesRepository.VoidCallback() {
                    @Override public void onSuccess() {
                        Toast.makeText(getContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onError(String message) {}
                });
            } else {
                favoritesRepo.addToFavorites(ad.getId(), new FavoritesRepository.VoidCallback() {
                    @Override public void onSuccess() {
                        Toast.makeText(getContext(), "Добавлено в избранное ❤️", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onError(String message) {}
                });
            }
        });
    }

    private void observeViewModel() {
        // Список объявлений
        viewModel.adsLiveData.observe(getViewLifecycleOwner(), ads -> {
            adAdapter.setAds(ads);
            // Показываем/скрываем "пусто"
            binding.layoutEmpty.setVisibility(
                    (ads == null || ads.isEmpty()) ? View.VISIBLE : View.GONE);
        });

        // Категории
        viewModel.categoriesLiveData.observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter = new CategoryAdapter(categories,
                    category -> {
                        if (category == null) viewModel.clearFilters();
                        else viewModel.setCategory(category.getId());
                    });
            binding.rvCategories.setAdapter(categoryAdapter);
        });

        // Загрузка
        viewModel.loadingLiveData.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Ошибки
        viewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
