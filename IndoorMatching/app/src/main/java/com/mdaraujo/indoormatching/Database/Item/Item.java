package com.mdaraujo.indoormatching.Database.Item;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.mdaraujo.indoormatching.Database.Category.Category;

@Entity(tableName = "items", foreignKeys = @ForeignKey(entity = Category.class, parentColumns = "category_id", childColumns = "category_id_ref"), indices = {@Index("category_id_ref")})
public class Item {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "item_id")
    private long itemId;

    @ColumnInfo(name = "item_name")
    private String itemName;

    @ColumnInfo(name = "category_id_ref")
    private int categoryIdRef;

    public Item(long itemId, String itemName, int categoryIdRef) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.categoryIdRef = categoryIdRef;
    }

    public long getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getCategoryIdRef() {
        return categoryIdRef;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setCategoryIdRef(int categoryIdRef) {
        this.categoryIdRef = categoryIdRef;
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", categoryIdRef=" + categoryIdRef +
                '}';
    }
}
