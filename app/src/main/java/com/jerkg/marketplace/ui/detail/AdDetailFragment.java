// Файл: app/src/main/java/com/jerkg/marketplace/ui/detail/AdDetailFragment.java
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
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.repository.FavoritesRepository;
import com.jerkg.marketplace.databinding.FragmentAdDetailBinding;

/**
 * AdDetailFragment — полный просмотр объявления.
 *
 * Содержит:
 * - Слайдер фотографий (ViewPager2)
 * - Название, цена, регион, категория
 * - Описание
 * - Кнопки: Позвонить, Написать в чат, В избранное
 * - Для автора: Редактировать, Удалить
 */
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

        // Получаем ID объявления из аргументов навигации
        String adId = getArguments() != null ? getArguments().getString("adId") : null;
        if (adId == null) {
            Toast.makeText(getContext(), "Ошибка: объявление не найдено", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        // Загружаем объявление
        viewModel.loadAd(adId);

        setupToolbar();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed());
    }

    private void observeViewModel() {
        // Данные объявления
        viewModel.adLiveData.observe(getViewLifecycleOwner(), ad -> {
            if (ad == null) return;

            // Заголовок тулбара
            binding.toolbar.setTitle(ad.getCategory());

            // Название и цена
            binding.tvTitle.setText(ad.getTitle());
            binding.tvPrice.setText(ad.getFormattedPrice());

            // Регион
            binding.tvRegion.setText(ad.getRegion());

            // Описание
            binding.tvDescription.setText(ad.getDescription());

            // Счётчик просмотров
            binding.tvViews.setText("Просмотры: " + ad.getViewCount());

            // Фото — слайдер
            if (ad.getImageUrls() != null && !ad.getImageUrls().isEmpty()) {
                PhotoPagerAdapter pagerAdapter =
                        new PhotoPagerAdapter(ad.getImageUrls());
                binding.viewPagerPhotos.setAdapter(pagerAdapter);
                binding.dotsIndicator.setViewPager2(binding.viewPagerPhotos);
                binding.viewPagerPhotos.setVisibility(View.VISIBLE);
                binding.ivNoPhoto.setVisibility(View.GONE);
            } else {
                binding.viewPagerPhotos.setVisibility(View.GONE);
                binding.ivNoPhoto.setVisibility(View.VISIBLE);
            }

            // Кнопка "Позвонить"
            if (ad.getUserPhone() != null) {
                binding.btnCall.setOnClickListener(v -> {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:" + ad.getUserPhone()));
                    startActivity(callIntent);
                });
                binding.btnCall.setVisibility(View.VISIBLE);
            }

            // Кнопка "Написать" (чат)
            binding.btnChat.setOnClickListener(v -> {
                String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                if (currentUid == null) return;

                // chatId = комбинация двух UID (отсортированных)
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

            // Показываем кнопки редактирования только автору
            String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            boolean isOwner = ad.getUserId() != null && ad.getUserId().equals(currentUid);

            binding.layoutOwnerActions.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            binding.layoutGuestActions.setVisibility(isOwner ? View.GONE : View.VISIBLE);

            if (isOwner) {
                binding.btnEdit.setOnClickListener(v -> {
                    // TODO: переход на экран редактирования с заполненными полями
                    Toast.makeText(getContext(), "Редактирование", Toast.LENGTH_SHORT).show();
                });
                binding.btnDelete.setOnClickListener(v -> confirmDelete(ad.getId()));
            }

            // Проверяем избранное
            favoritesRepo.isFavorite(ad.getId(), fav -> {
                isFavorite = fav;
                updateFavoriteButton();
            });

            // Кнопка избранного
            binding.btnFavorite.setOnClickListener(v -> toggleFavorite(ad.getId()));
        });

        // Загрузка
        viewModel.loadingLiveData.observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.scrollContent.setVisibility(loading ? View.GONE : View.VISIBLE);
        });

        // Ошибки
        viewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null)
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        // Удаление
        viewModel.deletedLiveData.observe(getViewLifecycleOwner(), deleted -> {
            if (Boolean.TRUE.equals(deleted)) {
                Toast.makeText(getContext(), "Объявление удалено", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
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
