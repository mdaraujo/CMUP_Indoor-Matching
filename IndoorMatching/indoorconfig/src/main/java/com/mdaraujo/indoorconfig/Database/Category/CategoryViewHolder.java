package com.mdaraujo.indoorconfig.Database.Category;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mdaraujo.indoorconfig.R;
import com.mdaraujo.indoorconfig.RecyclerViewClickListener;

public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private RecyclerViewClickListener itemListener;
    private ImageView categoryIcon;
    private TextView categoryName;
    private TextView categoryDescription;


    public CategoryViewHolder(@NonNull View itemView, RecyclerViewClickListener itemListener) {
        super(itemView);
        this.itemListener = itemListener;
        categoryIcon = (ImageView) itemView.findViewById(R.id.cat_icon);
        categoryName = (TextView) itemView.findViewById(R.id.cat_name);
        categoryDescription = (TextView) itemView.findViewById(R.id.cat_desc);
        itemView.setOnClickListener(this);
    }

    public void bindData(final Category viewModel) {
        categoryIcon.setImageResource(viewModel.getIconRef());
        categoryName.setText(viewModel.getCategoryName());
        categoryDescription.setText(viewModel.getCategoryDescription());
    }

    @Override
    public void onClick(View v) {
        itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
    }
}
