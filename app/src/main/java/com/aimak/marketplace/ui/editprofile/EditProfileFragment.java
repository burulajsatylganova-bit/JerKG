package com.aimak.marketplace.ui.editprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private FirebaseFirestore db;
    private String currentUid;

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
        if (user == null) {
            requireActivity().onBackPressed();
            return;
        }
        currentUid = user.getUid();

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());

        // Показываем телефон
        binding.tvPhoneLabel.setText(
                user.getPhoneNumber() != null ? user.getPhoneNumber() : "");

        setupRegionSpinner();
        loadUserData();
        setupSaveButton();
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

                        if (name != null && !name.isEmpty()) {
                            binding.etName.setText(name);
                            updateAvatarLetter(name);
                        }

                        if (region != null) {
                            List<String> regions = CategoryHelper.getRegions(requireContext());
                            int pos = regions.indexOf(region);
                            if (pos >= 0) binding.spinnerRegion.setSelection(pos);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                });
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

        binding.btnChangePhoto.setOnClickListener(v ->
                Toast.makeText(getContext(), getString(R.string.edit_profile_photo_soon), Toast.LENGTH_SHORT).show());
    }

    private void saveProfile() {
        String name   = binding.etName.getText().toString().trim();
        String region = (String) binding.spinnerRegion.getSelectedItem();

        if (name.isEmpty()) {
            binding.etName.setError(getString(R.string.edit_profile_name_error));
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);
        binding.btnSave.setText(getString(R.string.edit_profile_saving));

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("region", region);
        updates.put("updatedAt", System.currentTimeMillis());

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
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                    binding.btnSave.setText(getString(R.string.edit_profile_save));

                    // Если документ не существует — создаём через set
                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", currentUid);
                    data.put("name", name);
                    data.put("region", region);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) data.put("phoneNumber", user.getPhoneNumber());
                    data.put("createdAt", System.currentTimeMillis());

                    db.collection("users").document(currentUid)
                            .set(data)
                            .addOnSuccessListener(v2 -> {
                                if (binding == null) return;
                                Toast.makeText(getContext(), getString(R.string.edit_profile_created), Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(requireView()).popBackStack();
                            })
                            .addOnFailureListener(e2 ->
                                    Toast.makeText(getContext(), getString(R.string.error_fill_all_fields) + ": " + e2.getMessage(),
                                            Toast.LENGTH_LONG).show());
                });
    }

    private void updateAvatarLetter(String name) {
        if (binding == null) return;
        if (name != null && !name.isEmpty()) {
            binding.tvAvatarLetter.setText(
                    String.valueOf(name.charAt(0)).toUpperCase());
        } else {
            binding.tvAvatarLetter.setText("?");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
