
package com.jerkg.marketplace.ui.detail;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jerkg.marketplace.data.model.Ad;
import com.jerkg.marketplace.data.repository.AdRepository;

public class AdDetailViewModel extends ViewModel {

    private final AdRepository adRepository = new AdRepository();

    public MutableLiveData<Ad> adLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    public MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> deletedLiveData = new MutableLiveData<>(false);

    public void loadAd(String adId) {
        loadingLiveData.setValue(true);
        adRepository.getAdById(adId, new AdRepository.AdCallback() {
            @Override
            public void onSuccess(Ad ad) {
                loadingLiveData.postValue(false);
                adLiveData.postValue(ad);
                // Увеличиваем счётчик просмотров
                adRepository.incrementViewCount(adId);
            }
            @Override
            public void onError(String message) {
                loadingLiveData.postValue(false);
                errorLiveData.postValue(message);
            }
        });
    }

    public void deleteAd(String adId) {
        adRepository.deleteAd(adId, new AdRepository.VoidCallback() {
            @Override public void onSuccess() { deletedLiveData.postValue(true); }
            @Override public void onError(String message) { errorLiveData.postValue(message); }
        });
    }
}
