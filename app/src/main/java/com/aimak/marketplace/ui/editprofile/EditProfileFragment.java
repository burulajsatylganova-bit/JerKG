package com.aimak.marketplace.ui.editprofile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
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
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.aimak.marketplace.R;
import com.aimak.marketplace.databinding.FragmentEditProfileBinding;
import com.aimak.marketplace.utils.CategoryHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private FirebaseFirestore db;
    private String currentUid;
    private String currentPhotoBase64 = null; // хранит текущее фото

    // Лаунчер для выбора фото из галереи
    private final ActivityResultLauncher<Intent> pickPhotoLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) processPhoto(uri);
                        }
                    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { requireActivity().onBackPressed(); return; }
        currentUid = user.getUid();

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());

        binding.tvPhoneLabel.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");

        setupRegionSpinner();
        loadUserData();
        setupSaveButton();

        // Нажатие на камеру — выбор фото
        binding.btnChangePhoto.setOnClickListener(v -> openGallery());
        // Нажатие на сам аватар — тоже выбор фото
        binding.tvAvatarLetter.setOnClickListener(v -> openGallery());
        binding.ivProfilePhoto.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickPhotoLauncher.launch(intent);
    }

    private void processPhoto(Uri uri) {
        new Thread(() -> {
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
                Bitmap original = BitmapFactory.decodeStream(inputStream);
                if (original == null) return;

                // Ресайзим до 300x300
                int size = 300;
                float scale = Math.min((float) size / original.getWidth(),
                        (float) size / original.getHeight());
                Bitmap resized = Bitmap.createScaledBitmap(original,
                        Math.round(original.getWidth() * scale),
                        Math.round(original.getHeight() * scale), true);
                original.recycle();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                resized.recycle();

                currentPhotoBase64 = "data:image/jpeg;base64,"
                        + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

                // Показываем фото в UI
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    byte[] bytes = Base64.decode(
                            currentPhotoBase64.replace("data:image/jpeg;base64,", ""),
                            Base64.NO_WRAP);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.ivProfilePhoto.setImageBitmap(bmp);
                    binding.ivProfilePhoto.setVisibility(View.VISIBLE);
                    binding.tvAvatarLetter.setVisibility(View.GONE);
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Ошибка загрузки фото", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setupRegionSpinner() {
        List<String> regions = CategoryHelper.getRegions(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, regions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRegion.setAdapter(adapter);
    }

    private void loadUserData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);

                    if (doc.exists()) {
                        String name   = doc.getString("name");
                        String region = doc.getString("region");
                        String photo  = doc.getString("photoBase64");

                        if (name != null && !name.isEmpty()) {
                            binding.etName.setText(name);
                            updateAvatarLetter(name);
                        }

                        if (region != null) {
                            List<String> regions = CategoryHelper.getRegions(requireContext());
                            int pos = regions.indexOf(region);
                            if (pos >= 0) binding.spinnerRegion.setSelection(pos);
                        }

                        // Показываем сохранённое фото
                        if (photo != null && !photo.isEmpty()) {
                            currentPhotoBase64 = photo;
                            showSavedPhoto(photo);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                });
    }

    private void showSavedPhoto(String base64) {
        try {
            String data = base64.replace("data:image/jpeg;base64,", "");
            byte[] bytes = Base64.decode(data, Base64.NO_WRAP);
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null && binding != null) {
                binding.ivProfilePhoto.setImageBitmap(bmp);
                binding.ivProfilePhoto.setVisibility(View.VISIBLE);
                binding.tvAvatarLetter.setVisibility(View.GONE);
            }
        } catch (Exception e) { /* показываем букву */ }
    }

    private void setupSaveButton() {
        binding.etName.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                updateAvatarLetter(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name   = binding.etName.getText().toString().trim();
        String region = (String) binding.spinnerRegion.getSelectedItem();

        if (name.isEmpty()) { binding.etName.setError(getString(R.string.edit_profile_name_error)); return; }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);
        binding.btnSave.setText(getString(R.string.edit_profile_saving));

        Map<String, Object> updates = new HashMap<>();
        updates.put("name",      name);
        updates.put("region",    region);
        updates.put("updatedAt", System.currentTimeMillis());
        if (currentPhotoBase64 != null) updates.put("photoBase64", currentPhotoBase64);

        db.collection("users").document(currentUid)
                .update(updates)
                .addOnSuccessListener(v -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText(getString(R.string.edit_profile_save));
                    Toast.makeText(getContext(), getString(R.string.edit_profile_saved), Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .addOnFailureListener(e -> {
                    // Если документ не существует — создаём
                    Map<String, Object> data = new HashMap<>(updates);
                    data.put("uid", currentUid);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) data.put("phoneNumber", user.getPhoneNumber());
                    data.put("createdAt", System.currentTimeMillis());

                    db.collection("users").document(currentUid)
                            .set(data, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(v2 -> {
                                if (binding == null) return;
                                Toast.makeText(getContext(), getString(R.string.edit_profile_created), Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(requireView()).popBackStack();
                            })
                            .addOnFailureListener(e2 ->
                                    Toast.makeText(getContext(), e2.getMessage(), Toast.LENGTH_LONG).show());
                });
    }

    private void updateAvatarLetter(String name) {
        if (binding == null || currentPhotoBase64 != null) return;
        binding.tvAvatarLetter.setText(!name.isEmpty()
                ? String.valueOf(name.charAt(0)).toUpperCase() : "?");
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
