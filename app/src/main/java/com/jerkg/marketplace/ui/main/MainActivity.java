package com.jerkg.marketplace.ui.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.jerkg.marketplace.R;
import com.jerkg.marketplace.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    // ID фрагментов которые показывают bottom nav
    private static final int[] TOP_LEVEL_DESTINATIONS = {
            R.id.homeFragment,
            R.id.searchFragment,
            R.id.chatsListFragment,
            R.id.profileFragment
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupNavigation();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHostFragment == null) return;

        navController = navHostFragment.getNavController();

        // Подключаем bottom nav, но кнопку "+" обрабатываем сами
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.addAdFragment) {
                // Открываем AddAdFragment без смены selected item
                navController.navigate(R.id.addAdFragment);
                return false; // не выделяем кнопку "+"
            }

            // Остальные пункты — стандартная навигация
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        // Скрываем/показываем bottom nav в зависимости от экрана
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            int destId = destination.getId();
            boolean isTopLevel = false;
            for (int id : TOP_LEVEL_DESTINATIONS) {
                if (id == destId) { isTopLevel = true; break; }
            }
            binding.bottomNavigation.setVisibility(
                    isTopLevel ? android.view.View.VISIBLE : android.view.View.VISIBLE);
        });
    }
}