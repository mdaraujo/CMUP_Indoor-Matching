package com.mdaraujo.commonlibrary;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mdaraujo.commonlibrary.model.BeaconInfo;
import com.mdaraujo.commonlibrary.model.Room;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.util.ArrayList;
import java.util.List;

import static com.mdaraujo.commonlibrary.CommonParams.NAMESPACE_ID;
import static com.mdaraujo.commonlibrary.CommonParams.PERMISSION_REQUEST_COARSE_LOCATION;
import static com.mdaraujo.commonlibrary.model.BeaconInfo.BEACONS_COLLECTION_NAME;

public class BaseMainActivity extends AppCompatActivity implements BeaconConsumer {

    private static String TAG = "BaseMainActivity";

    private BackgroundPowerSaver backgroundPowerSaver;
    protected BeaconManager mBeaconManager;
    protected FirebaseFirestore firestoreDb;

    protected RoomCanvasView roomCanvas;
    protected Room room;
    protected List<BeaconInfo> beaconsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        verifyBluetooth();

        // enables auto battery saving of about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        firestoreDb = FirebaseFirestore.getInstance();

//        BeaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
//        RunningAverageRssiFilter.setSampleExpirationMilliseconds(3000L);

        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
//        ArmaRssiFilter.setDEFAULT_ARMA_SPEED(0.1);

        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

    }

    @Override
    public void onResume() {
        super.onResume();

        refreshScan();
    }

    protected void refreshScan() {
        room = null;
        beaconsInfo = new ArrayList<>();

        // reset object that estimates phone position

        mBeaconManager.setForegroundScanPeriod(400L);
        mBeaconManager.setForegroundBetweenScanPeriod(0L); // duration spent not scanning between each Bluetooth scan cycle

        mBeaconManager.bind(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        try {
            mBeaconManager.startRangingBeaconsInRegion(getCustomRegion());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static Region getCustomRegion() {
        Identifier customNamespaceId = Identifier.parse(NAMESPACE_ID);
        return new Region("indoor-matching-beacons-region", customNamespaceId, null, null);
    }

    protected BeaconInfo getBeaconFromList(String instanceId) {
        for (BeaconInfo beacon : beaconsInfo) {
            if (beacon.getInstanceId().equals(instanceId))
                return beacon;
        }
        return null;
    }

    protected void addFoundBeaconToList(Beacon foundBeacon) {
        beaconsInfo.add(new BeaconInfo(foundBeacon.getId1().toHexString(),
                foundBeacon.getId2().toHexString(), foundBeacon.getBluetoothAddress(),
                foundBeacon.getRssi(), foundBeacon.getDistance(), true));
    }

    protected void getBeaconsOfRoom(String roomKey) {
        Query queryBeacons = firestoreDb.collection(BEACONS_COLLECTION_NAME)
                .whereEqualTo("roomKey", roomKey);

        queryBeacons.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {

                        if (!documentSnapshots.isEmpty()) {

                            for (DocumentSnapshot beaconSnapshot : documentSnapshots.getDocuments()) {
                                BeaconInfo retrievedBeacon = beaconSnapshot.toObject(BeaconInfo.class);

                                BeaconInfo beaconFound = getBeaconFromList(retrievedBeacon.getInstanceId());

                                if (beaconFound != null) {
                                    beaconFound.setRoomKey(roomKey);
                                    beaconFound.setPosX(retrievedBeacon.getPosX());
                                    beaconFound.setPosY(retrievedBeacon.getPosY());
                                    beaconFound.setName(retrievedBeacon.getName());
                                    beaconFound.setColor(retrievedBeacon.getColor());
                                    Log.i(TAG, "getBeaconsOfRoom: beaconFound.setRoomKey(roomKey) " + beaconFound.getInstanceId());
                                } else {
                                    beaconsInfo.add(retrievedBeacon);
                                    Log.i(TAG, "getBeaconsOfRoom: beaconsInfo.add() " + retrievedBeacon.getInstanceId());
                                }
                            }
                        } else {
                            Log.i(TAG, "Zero beacons associated with room " + roomKey);
                        }
                    }
                });
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

}
