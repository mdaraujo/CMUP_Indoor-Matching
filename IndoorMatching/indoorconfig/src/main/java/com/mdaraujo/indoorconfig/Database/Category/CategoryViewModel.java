package com.mdaraujo.indoorconfig.Database.Category;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.mdaraujo.indoorconfig.Database.AppRepository;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    private AppRepository appRepository;

    public CategoryViewModel(Application application) {
        super(application);
        appRepository = new AppRepository(application);
    }

    public LiveData<List<Category>> getAllCategories() {
        return appRepository.getAllCategories();
    }

    void insert(Category category) {
        appRepository.insert(category);
    }

    public LiveData<List<Category>> getCategoriesFromDB() {
        return appRepository.getLocalCategories();
    }
}
