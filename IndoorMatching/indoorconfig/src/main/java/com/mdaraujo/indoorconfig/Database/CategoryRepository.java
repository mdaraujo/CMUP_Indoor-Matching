package com.mdaraujo.indoorconfig.Database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.mdaraujo.indoorconfig.Category;

import java.util.List;

public class CategoryRepository {

    private CategoryDAO categoryDAO;
    private LiveData<List<Category>> allCategories;

    public CategoryRepository(Application application) {
        CategoryDatabase db = CategoryDatabase.getDatabase(application);
        categoryDAO = db.categoryDAO();
        allCategories = categoryDAO.getAllCategories();
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }

    public void insert(Category category) {
        new insertAsyncTask(categoryDAO).execute(category);
    }

    private static class insertAsyncTask extends AsyncTask<Category, Void, Void> {

        private CategoryDAO asyncTaskDao;

        insertAsyncTask(CategoryDAO dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Category... params) {
            asyncTaskDao.insert(params[0]);
            return null;
        }
    }

}