package com.jerkg.marketplace.ui.myads;

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
import com.jerkg.marketplace.databinding.FragmentMyAdsBinding;
import com.jerkg.marketplace.ui.home.AdAdapter;

import java.util.List;

public class MyAdsFragment extends Fragment {

    private FragmentMyAdsBinding binding;
    private final AdRepository adRepo = new AdRepository();
    private AdAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyAdsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        adapter = new AdAdapter(new AdAdapter.OnAdClickListener() {
            @Override
            public void onAdClick(Ad ad) {
                Bundle args = new Bundle();
                args.putString("adId", ad.getId());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_myAds_to_detail, args);
            }
            @Override
            public void onFavoriteClick(Ad ad) {
                // В своих объявлениях кнопка избранного не нужна — игнорируем
            }
        });

        binding.rvMyAds.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvMyAds.setAdapter(adapter);

        // Кнопка "Добавить объявление" в пустом состоянии
        binding.btnAddAd.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.addAdFragment));

        loadMyAds();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Перезагружаем список при каждом возврате на экран
        // (чтобы удалённые объявления исчезали сразу)
        loadMyAds();
    }

    private void loadMyAds() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.rvMyAds.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);

        adRepo.getMyAds(new AdRepository.AdsCallback() {
            @Override
            public void onSuccess(List<Ad> ads) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                if (ads.isEmpty()) {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    binding.rvMyAds.setVisibility(View.GONE);
                    binding.tvCount.setText("");
                } else {
                    binding.layoutEmpty.setVisibility(View.GONE);
                    binding.rvMyAds.setVisibility(View.VISIBLE);
                    binding.tvCount.setText("Всего объявлений: " + ads.size());
                    adapter.setAds(ads);
                }
            }

            @Override
            public void onError(String msg) {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}