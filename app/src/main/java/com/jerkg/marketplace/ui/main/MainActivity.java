// Файл: app/src/main/java/com/jerkg/marketplace/ui/main/MainActivity.java
package com.jerkg.marketplace.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.jerkg.marketplace.R;
import com.jerkg.marketplace.databinding.ActivityMainBinding;

/**
 * MainActivity — контейнер для всех фрагментов.
 * Содержит BottomNavigationView + NavHostFragment (Navigation Component).
 *
 * Фрагменты:
 * - HomeFragment      (Главная)
 * - SearchFragment    (Поиск)
 * - AddAdFragment     (Добавить объявление)
 * - FavoritesFragment (Избранное)
 * - ProfileFragment   (Профиль)
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

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
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }
    }
}
