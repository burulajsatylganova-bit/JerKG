// Файл: app/src/main/java/com/jerkg/marketplace/ui/home/HomeViewModel.java
package com.jerkg.marketplace.ui.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerkg.marketplace.data.model.Ad;
import com.jerkg.marketplace.data.model.Category;
import com.jerkg.marketplace.data.repository.AdRepository;
import com.jerkg.marketplace.utils.CategoryHelper;

import java.util.List;

/**
 * HomeViewModel — данные для главного экрана.
 * Fragment наблюдает за LiveData, ViewModel управляет загрузкой.
 */
public class HomeViewModel extends ViewModel {

    private final AdRepository adRepository;

    public MutableLiveData<List<Ad>> adsLiveData = new MutableLiveData<>();
    public MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    // Текущие фильтры
    private String selectedCategory = null; // null = все категории
    private String selectedRegion = null;   // null = все регионы

    public HomeViewModel() {
        adRepository = new AdRepository();
        // Загружаем категории из списка (не из Firestore)
        categoriesLiveData.setValue(CategoryHelper.getAllCategories());
        // Загружаем объявления
        loadAds();
    }

    /**
     * Загружает объявления с учётом текущих фильтров.
     */
    public void loadAds() {
        loadingLiveData.setValue(true);

        AdRepository.AdsCallback callback = new AdRepository.AdsCallback() {
            @Override
            public void onSuccess(List<Ad> ads) {
                loadingLiveData.postValue(false);
                adsLiveData.postValue(ads);
            }
            @Override
            public void onError(String message) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(message);
            }
        };

        if (selectedCategory != null && selectedRegion != null) {
            adRepository.getAdsByCategoryAndRegion(selectedCategory, selectedRegion, callback);
        } else if (selectedCategory != null) {
            adRepository.getAdsByCategory(selectedCategory, callback);
        } else if (selectedRegion != null) {
            adRepository.getAdsByRegion(selectedRegion, callback);
        } else {
            adRepository.getAllAds(callback);
        }
    }

    /**
     * Поиск объявлений по тексту.
     */
    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadAds();
            return;
        }
        loadingLiveData.setValue(true);
        adRepository.searchAds(query, new AdRepository.AdsCallback() {
            @Override
            public void onSuccess(List<Ad> ads) {
                loadingLiveData.postValue(false);
                adsLiveData.postValue(ads);
            }
            @Override
            public void onError(String message) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(message);
            }
        });
    }

    /** Выбор категории фильтра */
    public void setCategory(String categoryId) {
        this.selectedCategory = categoryId;
        loadAds();
    }

    /** Выбор региона фильтра */
    public void setRegion(String region) {
        this.selectedRegion = ("Все регионы".equals(region)) ? null : region;
        loadAds();
    }

    /** Сбросить все фильтры */
    public void clearFilters() {
        selectedCategory = null;
        selectedRegion = null;
        loadAds();
    }

    public String getSelectedCategory() { return selectedCategory; }
    public String getSelectedRegion() { return selectedRegion; }
}
