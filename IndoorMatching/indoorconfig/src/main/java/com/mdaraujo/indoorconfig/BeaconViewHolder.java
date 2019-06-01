package com.mdaraujo.indoorconfig;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mdaraujo.commonlibrary.RecyclerViewClickListener;
import com.mdaraujo.commonlibrary.model.BeaconInfo;

public class BeaconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public static final int inRangeColor = Color.rgb(171, 235, 198);
    public static final int notInRangeColor = Color.rgb(230, 176, 170);

    private RecyclerViewClickListener itemListener;
    private ImageView beaconColor;
    private TextView beaconName;
    private TextView coordinates;


    public BeaconViewHolder(@NonNull View itemView, RecyclerViewClickListener itemListener) {
        super(itemView);
        this.itemListener = itemListener;
        beaconColor = (ImageView) itemView.findViewById(R.id.beacon_color);
        beaconName = (TextView) itemView.findViewById(R.id.beacon_name);
        coordinates = (TextView) itemView.findViewById(R.id.beacon_coords);
        itemView.setOnClickListener(this);
    }

    public void bindData(final BeaconInfo viewModel) {
        beaconColor.setColorFilter(viewModel.getColor());
        beaconName.setText(viewModel.getName());
        String posText = viewModel.getRoomKey() != null ? "X: " + viewModel.getPosX() + " Y: " + viewModel.getPosY() : "Configure ->";
        coordinates.setText(String.format("%.2fm    %s", viewModel.getDistance(), posText));
        itemView.setBackgroundColor(viewModel.isInRange() ? inRangeColor : notInRangeColor);
    }

    @Override
    public void onClick(View v) {
        itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
    }
}
