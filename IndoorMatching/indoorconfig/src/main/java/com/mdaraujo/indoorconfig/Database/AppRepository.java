package com.mdaraujo.indoorconfig.Database;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import com.mdaraujo.indoorconfig.Database.Category.Category;
import com.mdaraujo.indoorconfig.Database.Category.CategoryDAO;
import com.mdaraujo.indoorconfig.Database.Interest.Interest;
import com.mdaraujo.indoorconfig.Database.Interest.InterestDAO;
import com.mdaraujo.indoorconfig.Database.Item.Item;
import com.mdaraujo.indoorconfig.Database.Item.ItemDAO;

import java.util.List;

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 * https://developer.android.com/topic/libraries/architecture/guide.html
 */
public class AppRepository {

    private CategoryDAO categoryDAO;
    private ItemDAO itemDAO;
    private InterestDAO interestDAO;

    public AppRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);

        categoryDAO = db.categoryDAO();
        itemDAO = db.itemDAO();
        interestDAO = db.interestDAO();
    }

    //CATEGORY
    public LiveData<List<Category>> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public LiveData<List<Category>> getLocalCategories() {
        return categoryDAO.getLocalCategories();
    }

    public void insert(Category category) {
        categoryDAO.insert(category);
//        new insertAsyncTask(categoryDAO).execute(category);
    }

    //ITEM
    public LiveData<List<String>> getItemsFromCategory(int categoryId) {
        return itemDAO.getItemsFromCategory(categoryId);
    }

    public void insert(Item item) {
        itemDAO.insert(item);
    }

    // INTEREST
    public LiveData<List<Integer>> getInterestsFromCategory(String userId, int categoryId) {
        return interestDAO.getInterestsFromCategory(userId, categoryId);
    }

    public void insert(Interest interest) {
        interestDAO.insert(interest);
    }

//    private static class insertAsyncTask extends AsyncTask<Category, Void, Void> {
//
//        private CategoryDAO categoryDAO;
//        private ItemDAO itemDAO;
//
//        insertAsyncTask(CategoryDAO dao) {
//            categoryDAO = dao;
//        }
//
//        insertAsyncTask(ItemDAO dao) {
//            itemDAO = dao;
//        }
//
//        @Override
//        protected Void doInBackground(final Category... params) {
//            categoryDAO.insert(params[0]);
//            return null;
//        }
//    }

}