package com.mdaraujo.indoorconfig.Database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mdaraujo.indoorconfig.Database.Category.Category;
import com.mdaraujo.indoorconfig.Database.Category.CategoryDAO;
import com.mdaraujo.indoorconfig.Database.Interest.Interest;
import com.mdaraujo.indoorconfig.Database.Interest.InterestDAO;
import com.mdaraujo.indoorconfig.Database.Item.Item;
import com.mdaraujo.indoorconfig.Database.Item.ItemDAO;
import com.mdaraujo.indoorconfig.R;

@Database(entities = {Category.class, Item.class, Interest.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CategoryDAO categoryDAO();

    public abstract ItemDAO itemDAO();

    public abstract InterestDAO interestDAO();

    /**
     * Make Database a Singleton.
     * Volatile ensures atomic access to the variable.
     */
    private static volatile AppDatabase INSTANCE;

    /**
     * If is single database instance, create databases
     *
     * @param context
     * @return
     */
    static AppDatabase getDatabase(final Context context) {
        Log.d("DB_DEBUG", "GET DATABASE");
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "user_preferences")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final CategoryDAO categoryDAO;
        private final ItemDAO itemDAO;
        private final InterestDAO interestDAO;

        PopulateDbAsync(AppDatabase db) {
            categoryDAO = db.categoryDAO();
            itemDAO = db.itemDAO();
            interestDAO = db.interestDAO();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            //Categories
            Category category;
            category = new Category(1, "Movie Preferences", "Preferred Movies Genres", false, R.mipmap.ic_movie_foreground);
            categoryDAO.insert(category);
            category = new Category(2, "Music Preferences", "Preferred Music Genres", false, R.mipmap.ic_music_foreground);
            categoryDAO.insert(category);
            category = new Category(3, "Book Preferences", "Preferred Book Genres", false, R.mipmap.ic_book_foreground);
            categoryDAO.insert(category);

            category = new Category(4, "Favourite Athletes", "", true, R.drawable.color_circle);
            categoryDAO.insert(category);

            //Items
            Item item;
            item = new Item(1, "Action", 1);
            itemDAO.insert(item);
            item = new Item(2, "Adventure", 1);
            itemDAO.insert(item);
            item = new Item(3, "Comedy", 1);
            itemDAO.insert(item);

            item = new Item(4, "Blues", 2);
            itemDAO.insert(item);
            item = new Item(5, "Caribbean", 2);
            itemDAO.insert(item);
            item = new Item(6, "Country", 2);
            itemDAO.insert(item);

            item = new Item(7, "Action", 3);
            itemDAO.insert(item);
            item = new Item(8, "Adventure", 3);
            itemDAO.insert(item);
            item = new Item(9, "Art", 3);
            itemDAO.insert(item);

            item = new Item(12121212, "Cristiano Ronaldo", 4);
            itemDAO.insert(item);

            return null;
        }
    }

}
