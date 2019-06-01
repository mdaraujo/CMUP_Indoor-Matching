package com.mdaraujo.indoormatching.Database.Interest;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.mdaraujo.indoormatching.Database.AppRepository;
import com.mdaraujo.indoormatching.Database.Item.Item;

import java.util.List;

public class InterestViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private LiveData<List<Interest>> allInterests;

    public InterestViewModel(Application application) {
        super(application);
        appRepository = new AppRepository(application);
    }

    public LiveData<List<Long>> getInterestsFromCategory(String userId, int categoryId) {
        return appRepository.getInterestsFromCategory(userId, categoryId);
    }

    public void delete(String userId, long itemId) {
        appRepository.delete(userId, itemId);
    }

    public void updateInterests(List<Item> items, String userId, List<Long> interests) {
        appRepository.updateInterests(items, userId, interests);
    }

    public boolean checkIfExists(String userId, long itemId) {
        return appRepository.checkIfExists(userId, itemId);
    }

    public void deleteAll() {
        appRepository.deleteAll();
    }

    public LiveData<List<Long>> getUserInterests(String userId) {
        return appRepository.getUserInterests(userId);
    }
}
