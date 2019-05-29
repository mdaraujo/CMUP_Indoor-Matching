package com.mdaraujo.indoorconfig.Database.Interest;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Interest Data Access Object
 */
@Dao
public interface InterestDAO {

    @Query("SELECT * from interests WHERE user_id LIKE :userId ORDER BY interest_id ASC")
    LiveData<List<Interest>> getAllInterests(String userId);

    @Query("SELECT item_id FROM interests " +
            "JOIN items ON interests.item_id_ref = items.item_id " +
            "WHERE user_id LIKE :userId " +
            "AND items.category_id_ref = :categoryId " +
            "ORDER BY interest_id ASC")
    LiveData<List<Integer>> getInterestsFromCategory(String userId, int categoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Interest interest);

    @Query("DELETE FROM items")
    void deleteAll();

}
