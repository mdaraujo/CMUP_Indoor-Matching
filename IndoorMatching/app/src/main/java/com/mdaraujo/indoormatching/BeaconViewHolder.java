package com.mdaraujo.indoormatching;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mdaraujo.commonlibrary.model.BeaconInfo;

import java.util.Locale;

public class BeaconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public static final int inRangeColor = Color.rgb(171, 235, 198);
    public static final int notInRangeColor = Color.rgb(230, 176, 170);

    private RecyclerViewClickListener itemListener;
    private TextView namespaceId;
    private TextView instanceId;
    private TextView distance;
    private TextView rssi;
    private TextView macAddress;
    private TextView coordinates;


    public BeaconViewHolder(@NonNull View itemView, RecyclerViewClickListener itemListener) {
        super(itemView);
        this.itemListener = itemListener;
        namespaceId = (TextView) itemView.findViewById(R.id.b_namespace_id);
        instanceId = (TextView) itemView.findViewById(R.id.b_instance_id);
        distance = (TextView) itemView.findViewById(R.id.b_distance);
        rssi = (TextView) itemView.findViewById(R.id.b_rssi);
        macAddress = (TextView) itemView.findViewById(R.id.b_mac_address);
        coordinates = (TextView) itemView.findViewById(R.id.b_coordinates);
        itemView.setOnClickListener(this);
    }

    public void bindData(final BeaconInfo viewModel) {
        namespaceId.setText("Namespace: " + viewModel.getNamespaceId());
        instanceId.setText("Instance: " + viewModel.getInstanceId());
        distance.setText(String.format(Locale.US, "Distance: %5.2f meters", viewModel.getDistance()));
        rssi.setText("RSSI: " + String.valueOf(viewModel.getRssi()));
        macAddress.setText("MAC: " + viewModel.getMacAddress());
        coordinates.setText(viewModel.getRoomKey() != null ? "X: " + viewModel.getPosX() + " Y: " + viewModel.getPosY() : "Configure ->");
        itemView.setBackgroundColor(viewModel.isInRange() ? inRangeColor : notInRangeColor);
    }

    @Override
    public void onClick(View v) {
        itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
    }
}
