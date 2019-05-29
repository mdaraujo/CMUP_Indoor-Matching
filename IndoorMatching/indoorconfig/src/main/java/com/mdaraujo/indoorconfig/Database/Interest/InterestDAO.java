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

    @Query("SELECT item_id_ref from interests WHERE user_id = :userId")
    LiveData<List<Integer>> getUserInterests(String userId);

    @Query("SELECT item_id FROM interests " +
            "JOIN items ON interests.item_id_ref = items.item_id " +
            "WHERE user_id LIKE :userId " +
            "AND items.category_id_ref = :categoryId")
    LiveData<List<Integer>> getInterestsFromCategory(String userId, int categoryId);

    @Query("SELECT COUNT(*) FROM interests WHERE user_id = :userId AND item_id_ref = :itemId")
    int countItemInterest(String userId, int itemId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Interest interest);

    @Query("DELETE FROM interests WHERE user_id=:userId AND item_id_ref=:itemId")
    void delete(String userId, int itemId);

    @Query("DELETE FROM interests")
    void deleteAll();
}
