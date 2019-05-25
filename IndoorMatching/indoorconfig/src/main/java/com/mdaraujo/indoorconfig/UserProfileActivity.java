package com.mdaraujo.indoorconfig;

import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mdaraujo.indoorconfig.Database.CategoryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private final static String TAG = "UserProfileActivity";

    private ArrayList<Category> categoriesInfo;
    private CategoryAdapter categoryAdapter;
    private RecyclerView recyclerView;
    private Context context;

    private CategoryRepository categoryRepository;
    private LiveData<List<Category>> allCategories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saved Preferences Data", Snackbar.LENGTH_LONG);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.categories_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        categoryRepository = new CategoryRepository(getApplication());
        allCategories = categoryRepository.getAllCategories();

        context = getBaseContext();
        categoriesInfo = new ArrayList<>();
        categoriesInfo.add(new Category(context, getResources().getString(R.string.movie_preferences), getResources().getString(R.string.movie_pref_desc), new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.movie_genres))), R.mipmap.ic_movie_foreground));
        categoriesInfo.add(new Category(context, getResources().getString(R.string.music_preferences), getResources().getString(R.string.music_pref_desc), new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.music_genres))), R.mipmap.ic_music_foreground));
        categoriesInfo.add(new Category(context, getResources().getString(R.string.book_preferences), getResources().getString(R.string.book_pref_desc), new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.book_genres))), R.mipmap.ic_book_foreground));
        categoryAdapter = new CategoryAdapter(categoriesInfo, this);
        recyclerView.setAdapter(categoryAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        context = getBaseContext();
        categoriesInfo = new ArrayList<>();
        categoriesInfo.add(new Category(context, getResources().getString(R.string.movie_preferences), getResources().getString(R.string.movie_pref_desc), new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.movie_genres))), R.mipmap.ic_movie_foreground));
        categoriesInfo.add(new Category(context, getResources().getString(R.string.music_preferences), getResources().getString(R.string.music_pref_desc), new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.music_genres))), R.mipmap.ic_music_foreground));
        categoriesInfo.add(new Category(context, getResources().getString(R.string.book_preferences), getResources().getString(R.string.book_pref_desc), new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.book_genres))), R.mipmap.ic_book_foreground));
        categoryAdapter = new CategoryAdapter(categoriesInfo, this);
        recyclerView.setAdapter(categoryAdapter);
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        Category category = categoriesInfo.get(position);
        String categoryName = category.getName().toUpperCase();
        Log.d(TAG, "CATEGORY CLICKED");

        List<Integer> selectedItems = category.getCategoryPreferences();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(category.getName());

        if (categoryName.contains("MOVIE")) {
            builder.setMultiChoiceItems(R.array.movie_genres, category.getCategoryPreferencesCheckedItems(), new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (isChecked) {
                        selectedItems.add(which);
                    } else if (selectedItems.contains(which)) {
                        selectedItems.remove(Integer.valueOf(which));
                    }
                }
            });
            category.setCategoryPreferences(selectedItems);
        } else if (categoryName.contains("MUSIC")) {
            builder.setMultiChoiceItems(R.array.music_genres, category.getCategoryPreferencesCheckedItems(), new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (isChecked) {
                        selectedItems.add(which);
                    } else if (selectedItems.contains(which)) {
                        selectedItems.remove(Integer.valueOf(which));
                    }
                }
            });
            category.setCategoryPreferences(selectedItems);
        } else if (categoryName.contains("BOOK")) {
            builder.setMultiChoiceItems(R.array.book_genres, category.getCategoryPreferencesCheckedItems(), new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (isChecked) {
                        selectedItems.add(which);
                    } else if (selectedItems.contains(which)) {
                        selectedItems.remove(Integer.valueOf(which));
                    }
                }
            });
            category.setCategoryPreferences(selectedItems);
        } else {
            Log.d(TAG, "Invalid Category Value");
        }

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(UserProfileActivity.this, "Saved User Preferences", Toast.LENGTH_SHORT).show();
                Log.d("CATEGORY_DESC", category.toString());
                categoryRepository.insert(category);
                Log.d("DB_REPO", String.valueOf(categoryRepository.getAllCategories().getValue()));
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
