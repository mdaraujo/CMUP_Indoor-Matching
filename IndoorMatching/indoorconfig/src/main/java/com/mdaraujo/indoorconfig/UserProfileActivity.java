package com.mdaraujo.indoorconfig;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.mdaraujo.indoorconfig.Database.Category.Category;
import com.mdaraujo.indoorconfig.Database.Category.CategoryAdapter;
import com.mdaraujo.indoorconfig.Database.Category.CategoryViewModel;
import com.mdaraujo.indoorconfig.Database.Interest.InterestViewModel;
import com.mdaraujo.indoorconfig.Database.Item.ItemViewModel;

import java.util.List;

public class UserProfileActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private final static String TAG = "UserProfileActivity";

//    private FirebaseAuth mAuth;
//    private FirebaseUser user;

    private List<Category> categoriesInfo;
    private CategoryAdapter categoryAdapter;
    private RecyclerView recyclerView;
    private UserProfileActivity upa;

    private CategoryViewModel categoryViewModel;
    private ItemViewModel itemViewModel;
    private InterestViewModel interestViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        mAuth = FirebaseAuth.getInstance();
//        user = mAuth.getCurrentUser();

        // TODO: THIS IS PROBABLY NOT THE BEST WAY TO DO THIS.
        upa = this;

        recyclerView = (RecyclerView) findViewById(R.id.categories_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);
        categoryViewModel.getCategoriesFromDB().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable final List<Category> categories) {
                for (Category category : categories)
                    Log.d("DB_DEBUG", "CATEGORY: " + category.toString());

                categoriesInfo = categories;
                categoryAdapter = new CategoryAdapter(categories, UserProfileActivity.this);
                recyclerView.setAdapter(categoryAdapter);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        Category category = categoriesInfo.get(position);
        Log.d(TAG, "CATEGORY CLICKED: " + category.getCategoryId());
        upa = this;

        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        itemViewModel.getItemsFromCategory(category.getCategoryId()).observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable final List<String> items) {
                Log.d("DB_DEBUG", "ITEMS_SIZE " + items.size());
                for (String item : items)
                    Log.d("DB_DEBUG", "ITEM: " + item.toString());

                AlertDialog.Builder builder = new AlertDialog.Builder(upa);
                builder.setTitle(category.getCategoryName());

                String itemsList[] = items.toArray(new String[items.size()]);

                builder.setMultiChoiceItems(itemsList, null/*selectedItems*/, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                        if (isChecked) {
//                            selectedItems.add(which);
//                        } else if (selectedItems.contains(which)) {
//                            selectedItems.remove(Integer.valueOf(which));
//                        }
                    }
                });

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("DB_DEBUG", "UPDATE INTERESTS");
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
