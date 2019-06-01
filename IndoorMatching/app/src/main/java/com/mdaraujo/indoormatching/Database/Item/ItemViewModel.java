package com.mdaraujo.indoormatching.Database.Item;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.mdaraujo.indoormatching.Database.AppRepository;

import java.util.List;

public class ItemViewModel extends AndroidViewModel {

    private AppRepository appRepository;

    public ItemViewModel(Application application) {
        super(application);
        appRepository = new AppRepository(application);
    }

    public LiveData<List<Item>> getItemsFromCategory(int categoryId) {
        return appRepository.getItemsFromCategory(categoryId);
    }

    public LiveData<List<Item>> getAllItems() {
        return appRepository.getAllItems();
    }
}
