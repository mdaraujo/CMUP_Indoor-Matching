package com.mdaraujo.indoorconfig.Database.Interest;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.mdaraujo.indoorconfig.Database.AppRepository;

import java.util.List;

public class InterestViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private LiveData<List<Interest>> allInterests;

    public InterestViewModel(Application application) {
        super(application);
        appRepository = new AppRepository(application);
    }

    public LiveData<List<Integer>> getInterestsFromCategory(String userId, int categoryId) {
        return appRepository.getInterestsFromCategory(userId, categoryId);
    }

    void insert(Interest interest) {
        appRepository.insert(interest);
    }

}
