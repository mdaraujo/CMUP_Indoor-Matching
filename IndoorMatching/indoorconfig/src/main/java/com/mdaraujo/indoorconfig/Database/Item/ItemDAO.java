package com.mdaraujo.indoorconfig.Database.Item;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Item Data Access Object
 */
@Dao
public interface ItemDAO {

    @Query("SELECT * from items ORDER BY item_id ASC")
    LiveData<List<Item>> getAllItems();

    @Query("SELECT * FROM items WHERE category_id_ref = :categoryId ORDER BY item_name ASC")
    LiveData<List<Item>> getItemsFromCategory(int categoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Item item);

    @Query("DELETE FROM items")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM items WHERE item_id = :itemId")
    int countItem(long itemId);
}
