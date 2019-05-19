package com.mdaraujo.indoorconfig;

import java.io.Serializable;
import java.util.List;

public class Category implements Serializable {
    public static final String CATEGORIES_COLLECTION = "categories";

    private String name;
    private String description;
    private List<String> genres;
    private int icon = R.drawable.color_circle;

    public Category(String name, String description, List<String> genres, int icon) {
        this.name = name;
        this.description = description;
        this.genres = genres;
        this.icon = icon;
    }

    public Category(String name, String description, List<String> genres) {
        this.name = name;
        this.description = description;
        this.genres = genres;
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

    public int getIcon() { return icon; }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
