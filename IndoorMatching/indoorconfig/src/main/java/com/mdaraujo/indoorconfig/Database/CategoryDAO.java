package com.mdaraujo.indoorconfig.Database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mdaraujo.indoorconfig.Category;

import java.util.List;

/**
 * Category Data Access Object
 */
@Dao
public interface CategoryDAO {

    @Query("SELECT * from category_preferences ORDER BY category_id ASC")
    LiveData<List<Category>> getAllCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Category category);

    @Query("DELETE FROM category_preferences")
    void deleteAll();



}