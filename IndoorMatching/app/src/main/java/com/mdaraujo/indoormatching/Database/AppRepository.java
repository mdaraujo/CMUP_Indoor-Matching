package com.mdaraujo.indoormatching.Database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.util.Log;

import com.mdaraujo.indoormatching.Database.Category.Category;
import com.mdaraujo.indoormatching.Database.Category.CategoryDAO;
import com.mdaraujo.indoormatching.Database.Interest.Interest;
import com.mdaraujo.indoormatching.Database.Interest.InterestDAO;
import com.mdaraujo.indoormatching.Database.Item.Item;
import com.mdaraujo.indoormatching.Database.Item.ItemDAO;

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
    }

    //ITEM
    public LiveData<List<Item>> getAllItems() {
        return itemDAO.getAllItems();
    }

    public LiveData<List<Item>> getItemsFromCategory(int categoryId) {
        return itemDAO.getItemsFromCategory(categoryId);
    }

    public void insert(Item item) {
        itemDAO.insert(item);
    }

    // INTEREST
    public LiveData<List<Long>> getInterestsFromCategory(String userId, int categoryId) {
        return interestDAO.getInterestsFromCategory(userId, categoryId);
    }

    public void updateInterests(List<Item> items, String userId, List<Long> interests) {
        new updateAsyncTask(interestDAO, items, userId, interests).execute();
    }

    public void updateFacebookAsyncTask(List<Item> items, String userId) {
        new updateFacebookAsyncTask(interestDAO, itemDAO, items, userId).execute();
    }

    public void delete(String userId, long itemId) {
        interestDAO.delete(userId, itemId);
    }

    public boolean checkIfExists(String userId, long itemId) {
        return interestDAO.countItemInterest(userId, itemId) > 0;
    }

    public void deleteAll() {
        interestDAO.deleteAll();
    }

    public LiveData<List<Long>> getUserInterests(String userId) {
        return interestDAO.getUserInterests(userId);
    }


    private static class updateAsyncTask extends AsyncTask<Void, Void, Void> {

        private InterestDAO interestDAO;
        private List<Item> items;
        private String userId;
        private List<Long> interests;

        public updateAsyncTask(InterestDAO interestDAO, List<Item> items, String userId, List<Long> interests) {
            this.interestDAO = interestDAO;
            this.items = items;
            this.userId = userId;
            this.interests = interests;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            for (Item item : items) {
                boolean itemExists = interestDAO.countItemInterest(userId, item.getItemId()) > 0;
                Log.d("DB_DEBUG", "ID: " + item.getItemId() + " EXISTS: " + itemExists);
                if (interests.contains(item.getItemId())) {
                    if (!itemExists) {
                        interestDAO.insert(new Interest(userId, item.getItemId()));
                    }
                } else {
                    if (itemExists) {
                        interestDAO.delete(userId, item.getItemId());
                    }
                }
            }

            return null;
        }

    }

    private static class updateFacebookAsyncTask extends AsyncTask<Void, Void, Void> {

        private InterestDAO interestDAO;
        private ItemDAO itemDAO;
        private List<Item> items;
        private String userId;

        public updateFacebookAsyncTask(InterestDAO interestDAO, ItemDAO itemDAO, List<Item> items, String userId) {
            this.interestDAO = interestDAO;
            this.itemDAO = itemDAO;
            this.items = items;
            this.userId = userId;
        }

        @Override
        protected Void doInBackground(final Void... params) {

            for (Item item : items) {
                boolean itemExists = itemDAO.countItem(item.getItemId()) > 0;
                if (!itemExists) {
                    Log.d("DB_DEBUG", "INSERTED " + item.getItemId() + " " + item.getItemName() + " " + item.getCategoryIdRef());
                    itemDAO.insert(item);
                    interestDAO.insert(new Interest(userId, item.getItemId()));
                    continue;
                }
                boolean interestExists = (interestDAO.countItemInterest(userId, item.getItemId())) > 0;
                if (!interestExists) {
                    interestDAO.insert(new Interest(userId, item.getItemId()));
                }
            }

            return null;
        }

    }


}