package com.aimak.marketplace.ui.support;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aimak.marketplace.R;
import com.aimak.marketplace.databinding.FragmentSupportBinding;

public class SupportFragment extends Fragment {

    private FragmentSupportBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSupportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack());

        setupFaq();
        setupContacts();
    }

    private void setupFaq() {
        // Аккордеон — клик открывает/закрывает ответ
        setupFaqItem(binding.cardFaq1, binding.tvFaq1Answer, binding.tvFaq1Arrow);
        setupFaqItem(binding.cardFaq2, binding.tvFaq2Answer, binding.tvFaq2Arrow);
        setupFaqItem(binding.cardFaq3, binding.tvFaq3Answer, binding.tvFaq3Arrow);
        setupFaqItem(binding.cardFaq4, binding.tvFaq4Answer, binding.tvFaq4Arrow);
        setupFaqItem(binding.cardFaq5, binding.tvFaq5Answer, binding.tvFaq5Arrow);
    }

    private void setupFaqItem(View card, View answer, android.widget.TextView arrow) {
        card.setOnClickListener(v -> {
            boolean isVisible = answer.getVisibility() == View.VISIBLE;
            answer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            arrow.setText(isVisible ? "▼" : "▲");
        });
    }

    private void setupContacts() {
        binding.btnEmailSupport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@aimak.kg"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Поддержка Аймак");
            startActivity(Intent.createChooser(intent, "Написать в поддержку"));
        });

        binding.btnWhatsapp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/996700000000?text=Привет, мне нужна помощь с приложением Аймак"));
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
