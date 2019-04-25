package com.mdaraujo.indoorconfig;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class BeaconsAdapter extends RecyclerView.Adapter {

    private List<BeaconInfo> beacons;
    private RecyclerViewClickListener itemListener;


    public BeaconsAdapter(List<BeaconInfo> models, RecyclerViewClickListener itemListener) {
        this.beacons = models;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new BeaconViewHolder(view, itemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ((BeaconViewHolder) holder).bindData(beacons.get(position));
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.beacon_item_view;
    }
}
