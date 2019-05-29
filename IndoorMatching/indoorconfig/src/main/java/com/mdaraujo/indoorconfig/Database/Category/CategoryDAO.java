package com.mdaraujo.indoorconfig.Database.Category;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Category Data Access Object
 */
@Dao
public interface CategoryDAO {

    @Query("SELECT * from categories ORDER BY category_id ASC")
    LiveData<List<Category>> getAllCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Category category);

    @Query("DELETE FROM categories")
    void deleteAll();

    @Query("SELECT * FROM categories WHERE from_fb = 0 ORDER BY category_id ASC")
    LiveData<List<Category>> getLocalCategories();
}