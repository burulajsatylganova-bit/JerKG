package com.aimak.marketplace.ui.addad;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aimak.marketplace.R;
import com.aimak.marketplace.data.model.Ad;
import com.aimak.marketplace.data.model.Category;
import com.aimak.marketplace.databinding.FragmentAddAdBinding;
import com.aimak.marketplace.utils.CategoryHelper;

import java.util.ArrayList;
import java.util.List;


public class AddAdFragment extends Fragment {

    private FragmentAddAdBinding binding;
    private AddAdViewModel viewModel;
    private SelectedPhotoAdapter photoAdapter;
    private final List<Uri> selectedPhotoUris = new ArrayList<>();

    // ✅ ДОБАВЛЕНО: хранит adId если это режим редактирования
    private String editAdId = null;
    // ✅ ДОБАВЛЕНО: хранит уже загруженные фото из Firestore
    private List<String> existingImageUrls = new ArrayList<>();

    private final ActivityResultLauncher<Intent> pickPhotoLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) {
                                selectedPhotoUris.add(uri);
                                photoAdapter.notifyDataSetChanged();
                                int total = existingImageUrls.size() + selectedPhotoUris.size();
                                binding.tvPhotoCount.setText(total + " / 5 фото");
                            }
                        }
                    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddAdBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddAdViewModel.class);

        // ✅ ДОБАВЛЕНО: определяем режим (создание или редактирование)
        editAdId = getArguments() != null ? getArguments().getString("adId") : null;

        setupCategorySpinner();
        setupRegionSpinner();
        setupPhotoList();
        setupButtons();
        observeViewModel();

        // ✅ ДОБАВЛЕНО: если режим редактирования — загружаем объявление
        if (editAdId != null) {
            binding.btnPublish.setText(getString(R.string.edit_profile_save));
            viewModel.loadAd(editAdId);
        }
    }

    private void setupCategorySpinner() {
        List<String> names = new ArrayList<>();
        names.add(getString(R.string.add_ad_category_hint));
        for (Category cat : CategoryHelper.getAllCategories(requireContext())) names.add(cat.getNameRu());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void setupRegionSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, CategoryHelper.getRegions(requireContext()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRegion.setAdapter(adapter);
    }

    private void setupPhotoList() {
        photoAdapter = new SelectedPhotoAdapter(selectedPhotoUris, uri -> {
            selectedPhotoUris.remove(uri);
            photoAdapter.notifyDataSetChanged();
            int total = existingImageUrls.size() + selectedPhotoUris.size();
            binding.tvPhotoCount.setText(total + " / 5 фото");
        });
        binding.rvPhotos.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPhotos.setAdapter(photoAdapter);
    }

    private void setupButtons() {
        binding.btnAddPhoto.setOnClickListener(v -> {
            int total = existingImageUrls.size() + selectedPhotoUris.size();
            if (total >= 5) {
                Toast.makeText(getContext(), getString(R.string.add_ad_max_photos), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickPhotoLauncher.launch(intent);
        });

        binding.switchNegotiable.setOnCheckedChangeListener((btn, checked) -> {
            binding.etPrice.setEnabled(!checked);
            if (checked) binding.etPrice.setText("");
        });

        binding.btnPublish.setOnClickListener(v -> {
            if (editAdId != null) {
                updateAd();
            } else {
                publishAd();
            }
        });
    }

    // ✅ ДОБАВЛЕНО: заполнение формы данными из существующего объявления
    private void fillFormWithAd(Ad ad) {
        binding.etTitle.setText(ad.getTitle());
        binding.etDescription.setText(ad.getDescription());

        if (ad.isNegotiable()) {
            binding.switchNegotiable.setChecked(true);
            binding.etPrice.setEnabled(false);
        } else {
            binding.etPrice.setText(String.valueOf((long) ad.getPrice()));
        }

        // Регион
        List<String> regions = CategoryHelper.getRegions(requireContext());
        int regionPos = regions.indexOf(ad.getRegion());
        if (regionPos >= 0) binding.spinnerRegion.setSelection(regionPos);

        // Категория
        List<Category> cats = CategoryHelper.getAllCategories(requireContext());
        for (int i = 0; i < cats.size(); i++) {
            if (cats.get(i).getId().equals(ad.getCategory())) {
                binding.spinnerCategory.setSelection(i + 1); // +1 из-за "Выберите категорию"
                break;
            }
        }

        // Сохраняем уже загруженные фото
        if (ad.getImageUrls() != null) {
            existingImageUrls = new ArrayList<>(ad.getImageUrls());
        }
        int total = existingImageUrls.size() + selectedPhotoUris.size();
        binding.tvPhotoCount.setText(total + " / 5 фото");
    }

    private void publishAd() {
        String title = binding.etTitle.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        boolean negotiable = binding.switchNegotiable.isChecked();
        int catPos = binding.spinnerCategory.getSelectedItemPosition();
        String region = (String) binding.spinnerRegion.getSelectedItem();

        if (title.isEmpty()) { binding.etTitle.setError(getString(R.string.add_ad_error_title)); return; }
        if (desc.isEmpty()) { binding.etDescription.setError(getString(R.string.add_ad_error_desc)); return; }
        if (!negotiable && priceStr.isEmpty()) { binding.etPrice.setError(getString(R.string.add_ad_error_price)); return; }
        if (catPos == 0) { Toast.makeText(getContext(), getString(R.string.add_ad_select_category), Toast.LENGTH_SHORT).show(); return; }
        if (getString(R.string.region_all).equals(region)) { Toast.makeText(getContext(), getString(R.string.add_ad_select_region), Toast.LENGTH_SHORT).show(); return; }

        double price = 0;
        if (!negotiable && !priceStr.isEmpty()) {
            try { price = Double.parseDouble(priceStr); }
            catch (NumberFormatException e) { binding.etPrice.setError(getString(R.string.error_invalid_price)); return; }
        }

        String categoryId = CategoryHelper.getAllCategories(requireContext()).get(catPos - 1).getId();
        viewModel.publishAd(requireContext(), title, desc, price, negotiable, categoryId, region, selectedPhotoUris);
    }

    // ✅ ДОБАВЛЕНО: сохранение изменений при редактировании
    private void updateAd() {
        String title = binding.etTitle.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        boolean negotiable = binding.switchNegotiable.isChecked();
        int catPos = binding.spinnerCategory.getSelectedItemPosition();
        String region = (String) binding.spinnerRegion.getSelectedItem();

        if (title.isEmpty()) { binding.etTitle.setError(getString(R.string.add_ad_error_title)); return; }
        if (desc.isEmpty()) { binding.etDescription.setError(getString(R.string.add_ad_error_desc)); return; }
        if (!negotiable && priceStr.isEmpty()) { binding.etPrice.setError(getString(R.string.add_ad_error_price)); return; }
        if (catPos == 0) { Toast.makeText(getContext(), getString(R.string.add_ad_select_category), Toast.LENGTH_SHORT).show(); return; }
        if (getString(R.string.region_all).equals(region)) { Toast.makeText(getContext(), getString(R.string.add_ad_select_region), Toast.LENGTH_SHORT).show(); return; }

        double price = 0;
        if (!negotiable && !priceStr.isEmpty()) {
            try { price = Double.parseDouble(priceStr); }
            catch (NumberFormatException e) { binding.etPrice.setError(getString(R.string.error_invalid_price)); return; }
        }

        String categoryId = CategoryHelper.getAllCategories(requireContext()).get(catPos - 1).getId();
        viewModel.updateAd(requireContext(), editAdId, title, desc, price, negotiable,
                categoryId, region, selectedPhotoUris, existingImageUrls);
    }

    private void observeViewModel() {
        // ✅ ДОБАВЛЕНО: заполняем форму когда загрузили объявление для редактирования
        viewModel.adLiveData.observe(getViewLifecycleOwner(), ad -> {
            if (ad != null) fillFormWithAd(ad);
        });

        viewModel.loadingLiveData.observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnPublish.setEnabled(!loading);
            if (editAdId != null) {
                binding.btnPublish.setText(loading ? getString(R.string.saving) : getString(R.string.edit_profile_save));
            } else {
                binding.btnPublish.setText(loading ? getString(R.string.publishing) : getString(R.string.add_ad_publish));
            }
        });

        viewModel.successLiveData.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                String msg = editAdId != null ? getString(R.string.add_ad_updated) : getString(R.string.add_ad_published);
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.homeFragment);
            }
        });

        viewModel.errorLiveData.observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
