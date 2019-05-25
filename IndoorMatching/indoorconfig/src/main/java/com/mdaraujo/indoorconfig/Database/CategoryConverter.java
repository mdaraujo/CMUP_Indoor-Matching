package com.mdaraujo.indoorconfig.Database;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Convert Category genres and preferences fields from list to String for Room to store them.
 */
public class CategoryConverter {

    Gson gson = new Gson();

    @TypeConverter
    public String categoryToString(ArrayList<String> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public List stringToCategory(String genres) {
        return Arrays.asList(gson.fromJson(genres, new TypeToken<List<String>>() {
        }.getType()));
    }
}
