package com.mdaraujo.indoorconfig.Database.Item;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.mdaraujo.indoorconfig.Database.AppRepository;

import java.util.List;

public class ItemViewModel extends AndroidViewModel {

    private AppRepository appRepository;

    public ItemViewModel(Application application) {
        super(application);
        appRepository = new AppRepository(application);
    }

    public LiveData<List<String>> getItemsFromCategory(int categoryId) {
        return appRepository.getItemsFromCategory(categoryId);
    }

}
