package com.mdaraujo.indoorconfig.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.mdaraujo.indoorconfig.Category;

@Database(entities = {Category.class}, version = 1, exportSchema = false)
@TypeConverters({CategoryConverter.class})
public abstract class CategoryDatabase extends RoomDatabase {

    public abstract CategoryDAO categoryDAO();

    /**
     * Make Database a Singleton.
     */
    private static volatile CategoryDatabase INSTANCE;

    /**
     * If is single database instance, create databases
     *
     * @param context
     * @return
     */
    static CategoryDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CategoryDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            CategoryDatabase.class, "category_preferences")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
