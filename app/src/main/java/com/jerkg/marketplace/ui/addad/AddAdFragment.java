package com.jerkg.marketplace.ui.addad;

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

import com.jerkg.marketplace.R;
import com.jerkg.marketplace.data.model.Category;
import com.jerkg.marketplace.databinding.FragmentAddAdBinding;
import com.jerkg.marketplace.utils.CategoryHelper;

import java.util.ArrayList;
import java.util.List;


public class AddAdFragment extends Fragment {

    private FragmentAddAdBinding binding;
    private AddAdViewModel viewModel;
    private SelectedPhotoAdapter photoAdapter;
    private final List<Uri> selectedPhotoUris = new ArrayList<>();

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
                                binding.tvPhotoCount.setText(selectedPhotoUris.size() + " / 5 фото");
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

        setupCategorySpinner();
        setupRegionSpinner();
        setupPhotoList();
        setupButtons();
        observeViewModel();
    }

    private void setupCategorySpinner() {
        List<String> names = new ArrayList<>();
        names.add("Выберите категорию");
        for (Category cat : CategoryHelper.getAllCategories()) names.add(cat.getNameRu());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void setupRegionSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, CategoryHelper.getRegions());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRegion.setAdapter(adapter);
    }

    private void setupPhotoList() {
        photoAdapter = new SelectedPhotoAdapter(selectedPhotoUris, uri -> {
            selectedPhotoUris.remove(uri);
            photoAdapter.notifyDataSetChanged();
            binding.tvPhotoCount.setText(selectedPhotoUris.size() + " / 5 фото");
        });
        binding.rvPhotos.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPhotos.setAdapter(photoAdapter);
    }

    private void setupButtons() {
        binding.btnAddPhoto.setOnClickListener(v -> {
            if (selectedPhotoUris.size() >= 5) {
                Toast.makeText(getContext(), "Максимум 5 фотографий", Toast.LENGTH_SHORT).show();
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

        binding.btnPublish.setOnClickListener(v -> publishAd());
    }

    private void publishAd() {
        String title = binding.etTitle.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();
        String priceStr = binding.etPrice.getText().toString().trim();
        boolean negotiable = binding.switchNegotiable.isChecked();
        int catPos = binding.spinnerCategory.getSelectedItemPosition();
        String region = (String) binding.spinnerRegion.getSelectedItem();

        if (title.isEmpty()) { binding.etTitle.setError("Введите название"); return; }
        if (desc.isEmpty()) { binding.etDescription.setError("Введите описание"); return; }
        if (!negotiable && priceStr.isEmpty()) { binding.etPrice.setError("Введите цену"); return; }
        if (catPos == 0) { Toast.makeText(getContext(), "Выберите категорию", Toast.LENGTH_SHORT).show(); return; }
        if ("Все регионы".equals(region)) { Toast.makeText(getContext(), "Выберите регион", Toast.LENGTH_SHORT).show(); return; }

        double price = 0;
        if (!negotiable && !priceStr.isEmpty()) {
            try { price = Double.parseDouble(priceStr); }
            catch (NumberFormatException e) { binding.etPrice.setError("Неверная цена"); return; }
        }

        String categoryId = CategoryHelper.getAllCategories().get(catPos - 1).getId();
        viewModel.publishAd(requireContext(), title, desc, price, negotiable, categoryId, region, selectedPhotoUris);
    }

    private void observeViewModel() {
        viewModel.loadingLiveData.observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnPublish.setEnabled(!loading);
            binding.btnPublish.setText(loading ? "Публикация..." : "Опубликовать");
        });

        viewModel.successLiveData.observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Объявление опубликовано ✅", Toast.LENGTH_SHORT).show();
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