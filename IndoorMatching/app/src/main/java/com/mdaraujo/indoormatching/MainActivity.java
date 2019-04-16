package com.mdaraujo.indoormatching;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    private static String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BeaconManager mBeaconManager;
    private Button btnStartScan;

    private List<BeaconInfo> beaconsInfo;
    private BeaconsAdapter beaconsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView displayName = findViewById(R.id.user_name);
            ImageView displayImage = findViewById(R.id.user_photo);
            String name = user.getDisplayName();
            Uri photoUrl = user.getPhotoUrl();
            Glide.with(this).load(photoUrl + "?type=large").centerCrop().into(displayImage);
            displayName.setText(name);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

//        BeaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
//        RunningAverageRssiFilter.setSampleExpirationMilliseconds(5000l);

        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.beacons_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        beaconsInfo = new ArrayList<>();
        beaconsAdapter = new BeaconsAdapter(beaconsInfo);
        recyclerView.setAdapter(beaconsAdapter);

        btnStartScan = findViewById(R.id.btn_start_scan);

        verifyBluetooth();

        Log.i(TAG, "onCreate");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION));
                builder.show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

//        // set the duration of the scan to be 1.1 seconds
//        mBeaconManager.setBackgroundScanPeriod(1100l);
//        // set the time between each scan to be 1 hour (3600 seconds)
//        mBeaconManager.setBackgroundBetweenScanPeriod(3600000l);

        mBeaconManager.setForegroundScanPeriod(1100L);
        mBeaconManager.setForegroundBetweenScanPeriod(1200L);

        mBeaconManager.bind(this);
        btnStartScan.setText(R.string.stop);
    }

    public void onBeaconServiceConnect() {
        Region region = new Region("all-beacons-region", null, null, null);
        try {
            mBeaconManager.startRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mBeaconManager.addRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> foundBeacons, Region region) {

        for (BeaconInfo beacon : beaconsInfo) {
            beacon.setInRange(false);
        }

        if (foundBeacons.size() <= 0) {
            beaconsAdapter.notifyDataSetChanged();
            return;
        }

        for (Beacon foundBeacon : foundBeacons) {
            if (foundBeacon.getServiceUuid() == 0xfeaa && foundBeacon.getBeaconTypeCode() == 0x00) {
                // This is a Eddystone-UID frame

                BeaconInfo beaconInfo = getBeaconFromList(foundBeacon);

                if (beaconInfo == null) {
                    beaconsInfo.add(new BeaconInfo(foundBeacon.getId1().toHexString(),
                            foundBeacon.getId2().toHexString(), foundBeacon.getBluetoothAddress(),
                            foundBeacon.getRssi(), foundBeacon.getDistance(), true));
                } else {
                    beaconInfo.setDistance(foundBeacon.getDistance());
                    beaconInfo.setRssi(foundBeacon.getRssi());
                    beaconInfo.setInRange(true);
                }
            }
        }
        Log.i(TAG, "FOUND: " + foundBeacons.size());

        beaconsAdapter.notifyDataSetChanged();
    }

    private BeaconInfo getBeaconFromList(Beacon foundBeacon) {
        for (BeaconInfo beacon : beaconsInfo) {
            if (beacon.getMacAddress().equals(foundBeacon.getBluetoothAddress()))
                return beacon;
        }
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }

    private void verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(dialog -> {
                    finish();
                    System.exit(0);
                });
                builder.show();
            }
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog -> {
                finish();
                System.exit(0);
            });
            builder.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(dialog -> {
                    });
                    builder.show();
                }
            }
        }
    }

    public void startMonitoring(View view) {
        if (mBeaconManager.isBound(this)) {
            mBeaconManager.unbind(this);
            btnStartScan.setText(R.string.start);
        } else {
            mBeaconManager.bind(this);
            btnStartScan.setText(R.string.stop);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
