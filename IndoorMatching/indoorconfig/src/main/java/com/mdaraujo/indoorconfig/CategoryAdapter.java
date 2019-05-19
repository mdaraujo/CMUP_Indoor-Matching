package com.mdaraujo.indoorconfig;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter {
    private ArrayList<Category> categories;
    private RecyclerViewClickListener itemListener;


    public CategoryAdapter(ArrayList<Category> categories, RecyclerViewClickListener itemListener) {
        this.categories = categories;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new CategoryViewHolder(view, itemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ((CategoryViewHolder) holder).bindData(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.category_item_view;
    }


}
