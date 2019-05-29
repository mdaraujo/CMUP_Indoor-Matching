package com.mdaraujo.indoorconfig;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mdaraujo.indoorconfig.Database.Category.Category;
import com.mdaraujo.indoorconfig.Database.Category.CategoryAdapter;
import com.mdaraujo.indoorconfig.Database.Category.CategoryViewModel;
import com.mdaraujo.indoorconfig.Database.Interest.InterestViewModel;
import com.mdaraujo.indoorconfig.Database.Item.Item;
import com.mdaraujo.indoorconfig.Database.Item.ItemViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private final static String TAG = "UserProfileActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private List<Category> categoriesInfo;
    private CategoryAdapter categoryAdapter;
    private RecyclerView recyclerView;

    private InterestViewModel interestViewModel;

    private List<Item> itemsList;
    private List<Integer> interestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        recyclerView = (RecyclerView)

                findViewById(R.id.categories_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        CategoryViewModel categoryViewModel;
        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);
        categoryViewModel.getCategoriesFromDB().observe(this, categories -> {
            for (Category category : categories)
                Log.d("DB_DEBUG", "CATEGORY: " + category.toString());

            categoriesInfo = categories;
            categoryAdapter = new CategoryAdapter(categories, this);
            recyclerView.setAdapter(categoryAdapter);
        });

        ItemViewModel itemViewModel;
        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        itemViewModel.getAllItems().observe(this, items -> {
            itemsList = items;
        });

        interestViewModel = ViewModelProviders.of(UserProfileActivity.this).get(InterestViewModel.class);
        interestViewModel.getUserInterests(user.getUid()).observe(this, interests -> {
            interestList = interests;
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        Category category = categoriesInfo.get(position);

        List<Item> categoryItems = new ArrayList<>();
        for (Item item : itemsList)
            if (item.getCategoryIdRef() == category.getCategoryId())
                categoryItems.add(item);

        List<Integer> categoryInterests = new ArrayList<>();
        for (Item item : categoryItems)
            if (interestList.contains(item.getItemId()))
                categoryInterests.add(item.getItemId());

        List<String> itemNames = new ArrayList<>();
        boolean[] selectedItems = new boolean[categoryItems.size()];

        //Item Names shown on category click
        for (Item item : categoryItems)
            itemNames.add(item.getItemName());
        Log.d("DB_DEBUG", "ALL ITEMS: " + String.valueOf(categoryItems));

        //User item interest fetched on category click
        for (int i = 0; i < categoryItems.size(); i++) {
            selectedItems[i] = false;
            if (categoryInterests.contains(categoryItems.get(i).getItemId())) {
                selectedItems[i] = true;
            }
        }
        Log.d("DB_DEBUG", "INTERESTS " + String.valueOf(categoryInterests) + " SELECTED " + Arrays.toString(selectedItems));


        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle(category.getCategoryName());
        builder.setMultiChoiceItems(itemNames.toArray(new String[itemNames.size()]), selectedItems, (dialog, which, isChecked) -> {
            int itemId = categoryItems.get(which).getItemId();
            if (isChecked) {
                categoryInterests.add(itemId);
            } else if (categoryInterests.contains(itemId)) {
                categoryInterests.remove(categoryInterests.indexOf(itemId));
            }
            Log.d("DB_DEBUG", String.valueOf(categoryInterests));
        });


        builder.setPositiveButton(android.R.string.ok, (dialog, id) -> {
            Log.d("DB_DEBUG", "OK " + String.valueOf(categoryInterests));
            interestViewModel.updateInterests(categoryItems, user.getUid(), categoryInterests);
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> {
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }
}
