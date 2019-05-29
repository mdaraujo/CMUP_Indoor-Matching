package com.mdaraujo.indoorconfig.Database.Category;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.mdaraujo.indoorconfig.R;

@Entity(tableName = "categories")
public class Category {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "category_id")
    private int categoryId;

    @ColumnInfo(name = "category_name")
    private String categoryName;

    @ColumnInfo(name = "category_description")
    private String categoryDescription;

    @ColumnInfo(name = "from_fb")
    private boolean fromFB;

    @ColumnInfo(name = "icon")
    private int iconRef;

    public Category(int categoryId, String categoryName, String categoryDescription, boolean fromFB, int iconRef) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
        this.fromFB = fromFB;
        this.iconRef = iconRef;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryDescription() {
        return categoryDescription;
    }

    public boolean isFromFB() {
        return fromFB;
    }

    public int getIconRef() {
        return iconRef;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setCategoryDescription(String categoryDescription) {
        this.categoryDescription = categoryDescription;
    }

    public void setFromFB(boolean fromFB) {
        this.fromFB = fromFB;
    }

    public void setIconRef(int iconRef) {
        this.iconRef = iconRef;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", categoryDescription='" + categoryDescription + '\'' +
                ", fromFB=" + fromFB +
                '}';
    }
}