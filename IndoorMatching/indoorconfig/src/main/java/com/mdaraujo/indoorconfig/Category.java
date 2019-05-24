package com.mdaraujo.indoorconfig;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Entity(tableName = "category_preferences")
public class Category implements Serializable {
    public static final String CATEGORIES_COLLECTION = "categories";

    private static final Map<String, Integer> categoryIdMap = new HashMap<>();

//    @PrimaryKey
//    @NonNull
//    @ColumnInfo(name = "category")
    private int id;
    private String name;
    private String description;
    private List<String> genres;
    private int icon;
    private List<Integer> categoryPreferences;

    //TODO: Check better location+instantiation of this map.
    private void setCategoryContext(Context context) {
        categoryIdMap.put(context.getResources().getString(R.string.movie_preferences), 1);
        categoryIdMap.put(context.getResources().getString(R.string.music_preferences), 2);
        categoryIdMap.put(context.getResources().getString(R.string.book_preferences), 3);
    }

    public Category(Context context, String name, String description, List<String> genres, int icon) {
        setCategoryContext(context);
        this.id = categoryIdMap.get(name);
        this.name = name;
        this.description = description;
        this.genres = genres;
        this.icon = icon;
        this.categoryPreferences = new ArrayList<>();
    }

    public Category(Context context, String name, String description, List<String> genres) {
        setCategoryContext(context);
        this.id = categoryIdMap.get(name);
        this.name = name;
        this.description = description;
        this.genres = genres;
        this.icon = R.drawable.color_circle;
        this.categoryPreferences = new ArrayList<>();
    }

    public boolean[] getCategoryPreferencesCheckedItems() {
        boolean[] checkedItems = new boolean[this.genres.size()];
        for (int i = 0; i < checkedItems.length; i++)
            checkedItems[i] = false;

        if (this.categoryPreferences == null)
            return checkedItems;

        for (int itemId : this.categoryPreferences)
            checkedItems[itemId] = true;

        Log.d("PREFERENCES", Arrays.toString(checkedItems));

        return checkedItems;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public List<Integer> getCategoryPreferences() {
        return categoryPreferences;
    }

    public void setCategoryPreferences(List<Integer> categoryPreferences) {
        this.categoryPreferences = categoryPreferences;
    }
}
