package com.aimak.marketplace.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.aimak.marketplace.data.model.Ad;
import com.aimak.marketplace.data.model.Category;
import com.aimak.marketplace.data.repository.AdRepository;
import com.aimak.marketplace.utils.CategoryHelper;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final AdRepository adRepository;

    public MutableLiveData<List<Ad>> adsLiveData = new MutableLiveData<>();
    public MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private String selectedCategory = null;
    private String selectedRegion = null;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        adRepository = new AdRepository();
        loadCategories();
        loadAds();
    }

    /** Перезагружаем категории с актуальным языком — передаём свежий контекст из фрагмента */
    public void loadCategories(android.content.Context context) {
        categoriesLiveData.setValue(CategoryHelper.getAllCategories(context));
    }

    /** Вариант без контекста — для первоначальной загрузки */
    public void loadCategories() {
        categoriesLiveData.setValue(CategoryHelper.getAllCategories(getApplication()));
    }

    public void loadAds() {
        loadingLiveData.setValue(true);
        AdRepository.AdsCallback callback = new AdRepository.AdsCallback() {
            @Override public void onSuccess(List<Ad> ads) {
                loadingLiveData.postValue(false);
                adsLiveData.postValue(ads);
            }
            @Override public void onError(String message) {
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

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) { loadAds(); return; }
        loadingLiveData.setValue(true);
        adRepository.searchAds(query, new AdRepository.AdsCallback() {
            @Override public void onSuccess(List<Ad> ads) {
                loadingLiveData.postValue(false);
                adsLiveData.postValue(ads);
            }
            @Override public void onError(String message) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(message);
            }
        });
    }

    public void setCategory(String categoryId) {
        this.selectedCategory = categoryId;
        loadAds();
    }

    public void setRegion(String region) {
        // Сравниваем с локализованным "все регионы"
        String allRegions = getApplication().getString(com.aimak.marketplace.R.string.region_all);
        this.selectedRegion = allRegions.equals(region) ? null : region;
        loadAds();
    }

    public void clearFilters() {
        selectedCategory = null;
        selectedRegion = null;
        loadAds();
    }

    public String getSelectedCategory() { return selectedCategory; }
    public String getSelectedRegion() { return selectedRegion; }
}