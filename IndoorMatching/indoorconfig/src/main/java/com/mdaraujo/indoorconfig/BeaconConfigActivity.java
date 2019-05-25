package com.mdaraujo.indoorconfig;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mdaraujo.commonlibrary.model.BeaconInfo;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import static com.mdaraujo.commonlibrary.model.BeaconInfo.BEACONS_COLLECTION_NAME;

public class BeaconConfigActivity extends AppCompatActivity {

    private static String TAG = "BeaconConfigActivity";

    private ColorPicker colorPicker;
    private int cpSelectedColor;

    private FirebaseFirestore firestoreDb;
    private BeaconInfo beacon;

    private TextView namespaceIdView;
    private TextView instanceIdView;
    private TextView macAddressView;
    private EditText nameView;
    private View colorView;
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
        nameView = findViewById(R.id.beacon_name);
        colorView = findViewById(R.id.beacon_color);
        posXView = findViewById(R.id.position_x_edit);
        posYView = findViewById(R.id.position_y_edit);

        Intent intent = getIntent();
        beacon = (BeaconInfo) intent.getSerializableExtra("BeaconInfo");

        namespaceIdView.setText(beacon.getNamespaceId());
        instanceIdView.setText(beacon.getInstanceId());
        macAddressView.setText(beacon.getMacAddress());
        nameView.setText(beacon.getName());

        if (beacon.getColor() == 255) { //If color equals transparent <=> No Color
            colorView.setBackgroundColor(Color.rgb(0, 0, 255));
        } else {
            colorView.setBackgroundColor(beacon.getColor());
        }

        posXView.setText(String.valueOf(beacon.getPosX()));
        posYView.setText(String.valueOf(beacon.getPosY()));

        colorView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                colorPicker.show();
                colorPicker.enableAutoClose();
            }
        });

        cpSelectedColor = beacon.getColor();

        colorPicker = new ColorPicker(this, Color.red(beacon.getColor()), Color.green(beacon.getColor()), Color.blue(beacon.getColor()));
        colorPicker.enableAutoClose();
        colorPicker.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {
                Log.d("Red", Integer.toString(Color.red(color)));
                Log.d("Green", Integer.toString(Color.green(color)));
                Log.d("Blue", Integer.toString(Color.blue(color)));
                Log.d("Pure Hex", Integer.toHexString(color));

                cpSelectedColor = color;
                colorView.setBackgroundColor(color);

                // If the auto-dismiss option is not enable (disabled as default) you have to manually dimiss the dialog
                // cp.dismiss();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                beacon.setName(String.valueOf(nameView.getText()));
                beacon.setColor(cpSelectedColor);
                beacon.setPosX(Float.parseFloat(posXView.getText().toString()));
                beacon.setPosY(Float.parseFloat(posYView.getText().toString()));

                Snackbar.make(view, "Saved beacon [ID=" + beacon.getMacAddress() + "] configurations.", Snackbar.LENGTH_LONG);
                DocumentReference beaconRef = firestoreDb.collection(BEACONS_COLLECTION_NAME).document(beacon.getMacAddress());

                beaconRef.set(beacon.toMap())
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                Log.i(TAG, "Delete beacon");
                DocumentReference beaconRef = firestoreDb.collection(BEACONS_COLLECTION_NAME).document(beacon.getMacAddress());
                beaconRef.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Beacon successfully deleted!");
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting beacon", e);
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
