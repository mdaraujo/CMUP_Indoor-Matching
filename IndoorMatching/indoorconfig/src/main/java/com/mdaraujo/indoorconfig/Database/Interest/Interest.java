package com.mdaraujo.indoorconfig.Database.Interest;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

import com.mdaraujo.indoorconfig.Database.Item.Item;

@Entity(tableName = "interests", primaryKeys = {"user_id", "item_id_ref"}, foreignKeys = @ForeignKey(entity = Item.class, parentColumns = "item_id", childColumns = "item_id_ref"), indices = {@Index("item_id_ref")})
public class Interest {

    @NonNull
    @ColumnInfo(name = "user_id")
    private String userId;

    @NonNull
    @ColumnInfo(name = "item_id_ref")
    private long itemIdRef;

    public Interest(String userId, long itemIdRef) {
        this.userId = userId;
        this.itemIdRef = itemIdRef;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getItemIdRef() {
        return itemIdRef;
    }

    public void setItemIdRef(long itemIdRef) {
        this.itemIdRef = itemIdRef;
    }

    @Override
    public String toString() {
        return "Interest{" +
                "userId='" + userId + '\'' +
                ", itemIdRef=" + itemIdRef +
                '}';
    }
}
