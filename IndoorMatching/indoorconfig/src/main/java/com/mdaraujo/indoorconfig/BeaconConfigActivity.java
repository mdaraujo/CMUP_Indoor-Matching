package com.mdaraujo.indoorconfig;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mdaraujo.commonlibrary.model.BeaconInfo;

import static com.mdaraujo.commonlibrary.model.BeaconInfo.BEACONS_COLLECTION_NAME;

public class BeaconConfigActivity extends AppCompatActivity {

    private static String TAG = "BeaconConfigActivity";

    private FirebaseFirestore firestoreDb;
    private BeaconInfo beacon;

    private TextView namespaceIdView;
    private TextView instanceIdView;
    private TextView macAddressView;
    private TextView posXView;
    private TextView posYView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_config);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firestoreDb = FirebaseFirestore.getInstance();
        namespaceIdView = findViewById(R.id.namespace_id_view);
        instanceIdView = findViewById(R.id.instance_id_view);
        macAddressView = findViewById(R.id.mac_view);
        posXView = findViewById(R.id.position_x_edit);
        posYView = findViewById(R.id.position_y_edit);

        Intent intent = getIntent();
        beacon = (BeaconInfo) intent.getSerializableExtra("BeaconInfo");

        namespaceIdView.setText(beacon.getNamespaceId());
        instanceIdView.setText(beacon.getInstanceId());
        macAddressView.setText(beacon.getMacAddress());
        posXView.setText(String.valueOf(beacon.getPosX()));
        posYView.setText(String.valueOf(beacon.getPosY()));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                beacon.setPosX(Float.parseFloat(posXView.getText().toString()));
                beacon.setPosY(Float.parseFloat(posYView.getText().toString()));

                Snackbar.make(view, "Saved beacon [ID=" + beacon.getMacAddress() + "] configurations.", Snackbar.LENGTH_LONG);
                DocumentReference beaconRef = firestoreDb.collection(BEACONS_COLLECTION_NAME).document(beacon.getMacAddress());
                beaconRef.update(beacon.toMap())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Beacon successfully updated!");
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating beacon", e);
                            }
                        });
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.beaconconfigmenu, menu);
        return true;
    }

}
