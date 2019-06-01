package com.mdaraujo.indoormatching.Database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mdaraujo.indoormatching.Database.Category.Category;
import com.mdaraujo.indoormatching.Database.Category.CategoryDAO;
import com.mdaraujo.indoormatching.Database.Interest.Interest;
import com.mdaraujo.indoormatching.Database.Interest.InterestDAO;
import com.mdaraujo.indoormatching.Database.Item.Item;
import com.mdaraujo.indoormatching.Database.Item.ItemDAO;
import com.mdaraujo.indoormatching.R;

import static com.mdaraujo.commonlibrary.CommonParams.FACEBOOK_CATEGORY_ID;

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

    public static void setINSTANCE(AppDatabase INSTANCE) {
        AppDatabase.INSTANCE = INSTANCE;
    }

    /**
     * If is single database instance, create databases
     *
     * @param context
     * @return
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Log.d("DB_DEBUG", "DATABASE CREATED");
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

        PopulateDbAsync(AppDatabase db) {
            categoryDAO = db.categoryDAO();
            itemDAO = db.itemDAO();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            //Categories
            Category category;
            category = new Category(FACEBOOK_CATEGORY_ID, "Facebook Interests", "User Facebook Interests", false, R.drawable.com_facebook_button_icon_blue);
            categoryDAO.insert(category);
            category = new Category(1, "Movie Preferences", "Preferred Movies Genres", false, R.mipmap.ic_movie_foreground);
            categoryDAO.insert(category);
            category = new Category(2, "Music Preferences", "Preferred Music Genres", false, R.mipmap.ic_music_foreground);
            categoryDAO.insert(category);
            category = new Category(3, "Book Preferences", "Preferred Book Genres", false, R.mipmap.ic_book_foreground);
            categoryDAO.insert(category);

            //Items
            Item item;
            item = new Item(1, "Action", 1);
            itemDAO.insert(item);
            item = new Item(2, "Adventure", 1);
            itemDAO.insert(item);
            item = new Item(3, "Comedy", 1);
            itemDAO.insert(item);
            item = new Item(4, "Crime", 1);
            itemDAO.insert(item);
            item = new Item(5, "Documentary", 1);
            itemDAO.insert(item);
            item = new Item(6, "Drama", 1);
            itemDAO.insert(item);
            item = new Item(7, "Mystery", 1);
            itemDAO.insert(item);
            item = new Item(8, "Political", 1);
            itemDAO.insert(item);
            item = new Item(9, "Romance", 1);
            itemDAO.insert(item);
            item = new Item(10, "Science", 1);
            itemDAO.insert(item);

            item = new Item(11, "Blues", 2);
            itemDAO.insert(item);
            item = new Item(12, "Caribbean", 2);
            itemDAO.insert(item);
            item = new Item(13, "Country", 2);
            itemDAO.insert(item);
            item = new Item(14, "Folk", 2);
            itemDAO.insert(item);
            item = new Item(15, "Hip hop", 2);
            itemDAO.insert(item);
            item = new Item(16, "Pop", 2);
            itemDAO.insert(item);
            item = new Item(17, "R&B", 2);
            itemDAO.insert(item);
            item = new Item(18, "Soul", 2);
            itemDAO.insert(item);
            item = new Item(19, "Reggae", 2);
            itemDAO.insert(item);
            item = new Item(20, "Rock", 2);
            itemDAO.insert(item);

            item = new Item(21, "Action", 3);
            itemDAO.insert(item);
            item = new Item(22, "Adventure", 3);
            itemDAO.insert(item);
            item = new Item(23, "Art", 3);
            itemDAO.insert(item);
            item = new Item(24, "Alternate history", 3);
            itemDAO.insert(item);
            item = new Item(25, "Autobiography", 3);
            itemDAO.insert(item);
            item = new Item(26, "Anthology", 3);
            itemDAO.insert(item);
            item = new Item(27, "Biography", 3);
            itemDAO.insert(item);
            item = new Item(28, "Book review", 3);
            itemDAO.insert(item);
            item = new Item(29, "Cookbook", 3);
            itemDAO.insert(item);
            item = new Item(30, "Comic", 3);
            itemDAO.insert(item);

            return null;
        }
    }

}
