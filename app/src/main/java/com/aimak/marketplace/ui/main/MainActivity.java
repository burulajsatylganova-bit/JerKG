package com.aimak.marketplace.ui.main;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.aimak.marketplace.AimakApp;
import com.aimak.marketplace.R;
import com.aimak.marketplace.databinding.ActivityMainBinding;
import com.aimak.marketplace.service.NotificationHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Применяем язык до создания Activity
        AimakApp.applyLocale(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupNavigation();
        NotificationHelper.initNotifications(this);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHostFragment == null) return;

        navController = navHostFragment.getNavController();

        // Стандартная навигация для всех табов
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        // Кнопка "+" — переопределяем чтобы не выделялась как таб
        binding.bottomNavigation.findViewById(R.id.addAdFragment)
                .setOnClickListener(v -> {
                    if (navController.getCurrentDestination() != null
                            && navController.getCurrentDestination().getId() == R.id.addAdFragment) {
                        return;
                    }
                    navController.navigate(R.id.addAdFragment,
                            null,
                            new NavOptions.Builder()
                                    .setPopUpTo(R.id.homeFragment, false)
                                    .build());
                });

        // Скрываем bottom nav на вложенных экранах
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            int id = destination.getId();
            boolean show = id == R.id.homeFragment
                    || id == R.id.searchFragment
                    || id == R.id.chatsListFragment
                    || id == R.id.profileFragment
                    || id == R.id.addAdFragment;
            binding.bottomNavigation.setVisibility(
                    show ? android.view.View.VISIBLE : android.view.View.GONE);
        });
    }
}
