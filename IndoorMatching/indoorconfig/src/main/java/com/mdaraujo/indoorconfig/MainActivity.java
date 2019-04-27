package com.mdaraujo.indoorconfig;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.mdaraujo.commonlibrary.CommonParams.PERMISSION_REQUEST_COARSE_LOCATION;
import static com.mdaraujo.commonlibrary.model.BeaconInfo.BEACONS_COLLECTION_NAME;
import static com.mdaraujo.commonlibrary.model.Room.ROOMS_COLLECTION_NAME;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier, RecyclerViewClickListener {

    private static String TAG = "MainActivity";

    private BeaconManager mBeaconManager;

    private FirebaseFirestore firestoreDb;
    private Room room;
    private String roomKey;
    private List<BeaconInfo> beaconsInfo;
    private BeaconsAdapter beaconsAdapter;
    private RecyclerView recyclerView;

    private Button scanBtn;
    private TextView roomNameView;
    private Button roomAddBtn;
    private Button roomEditBtn;

    public static final int ROOM_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView displayName = findViewById(R.id.user_name_text);
            ImageView displayImage = findViewById(R.id.user_photo);
            String name = user.getDisplayName();
            Uri photoUrl = user.getPhotoUrl();
            Glide.with(this).load(photoUrl + "?type=large").centerCrop().into(displayImage);
            displayName.setText(name);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        firestoreDb = FirebaseFirestore.getInstance();

//        BeaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
//        RunningAverageRssiFilter.setSampleExpirationMilliseconds(5000l);

        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);

        recyclerView = (RecyclerView) findViewById(R.id.beacons_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);

        scanBtn = findViewById(R.id.scan_btn);
        roomNameView = findViewById(R.id.room_name_text);
        roomAddBtn = findViewById(R.id.room_add_btn);
        roomEditBtn = findViewById(R.id.room_edit_btn);

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

        room = null;
        beaconsInfo = new ArrayList<>();
        beaconsAdapter = new BeaconsAdapter(beaconsInfo, this);
        recyclerView.setAdapter(beaconsAdapter);

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
        scanBtn.setText(R.string.stop);
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

                BeaconInfo beaconInfo = getBeaconFromList(foundBeacon.getId2().toHexString());

                if (beaconInfo == null) {

                    if (room == null) {
                        getRoomOfBeacon(foundBeacon);
                    } else {
                        addFoundBeaconToList(foundBeacon);
                    }

                } else {
                    beaconInfo.setDistance(foundBeacon.getDistance());
                    beaconInfo.setRssi(foundBeacon.getRssi());
                    beaconInfo.setInRange(true);
                }
            }
        }
        Collections.sort(beaconsInfo, (o1, o2) -> o1.getInstanceId().compareTo(o2.getInstanceId()));
        beaconsAdapter.notifyDataSetChanged();
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        BeaconInfo beacon = beaconsInfo.get(position);
        Toast.makeText(v.getContext(), beacon.getInstanceId(), Toast.LENGTH_SHORT).show();

        if (room == null && roomKey == null) return;

        beacon.setRoomKey(roomKey);
        Intent beaconConfigIntent = new Intent(this, BeaconConfigActivity.class);
        beaconConfigIntent.putExtra("BeaconInfo", beacon);

        startActivity(beaconConfigIntent);
    }

    private BeaconInfo getBeaconFromList(String instanceId) {
        for (BeaconInfo beacon : beaconsInfo) {
            if (beacon.getInstanceId().equals(instanceId))
                return beacon;
        }
        return null;
    }

    private void addFoundBeaconToList(Beacon foundBeacon) {
        beaconsInfo.add(new BeaconInfo(foundBeacon.getId1().toHexString(),
                foundBeacon.getId2().toHexString(), foundBeacon.getBluetoothAddress(),
                foundBeacon.getRssi(), foundBeacon.getDistance(), true));
    }

    private void getBeaconsOfRoom(String roomKey) {
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

    private void getRoomOfBeacon(Beacon foundBeacon) {
        String instanceId = foundBeacon.getId2().toHexString();

        Query queryBeacon = firestoreDb.collection(BEACONS_COLLECTION_NAME)
                .whereEqualTo("instanceId", instanceId)
                .limit(1);

        queryBeacon.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot beaconsSnapshots) {

                        if (!beaconsSnapshots.isEmpty()) {
                            DocumentSnapshot beaconDocument = beaconsSnapshots.getDocuments().get(0);
                            Log.i(TAG, "Beacon data: " + beaconDocument.getData());
                            BeaconInfo beaconInfo = beaconDocument.toObject(BeaconInfo.class);

                            Log.i(TAG, "Beacon getRoomKey: " + beaconInfo.getRoomKey());

                            DocumentReference docRef = firestoreDb.collection(ROOMS_COLLECTION_NAME)
                                    .document(beaconInfo.getRoomKey());

                            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot roomSnapshot) {
                                    if (roomSnapshot.exists() && room == null) {
                                        room = roomSnapshot.toObject(Room.class);
                                        Log.i(TAG, "Room: " + room.getName());

                                        roomNameView.setText(room.getName());
                                        roomAddBtn.setVisibility(View.GONE);
                                        roomEditBtn.setVisibility(View.VISIBLE);

                                        roomKey = beaconInfo.getRoomKey();

                                        getBeaconsOfRoom(beaconInfo.getRoomKey());
                                    }
                                }
                            });

                        } else {
                            Log.i(TAG, "Beacon not found.");
                            BeaconInfo beaconInfo = getBeaconFromList(foundBeacon.getId2().toHexString());

                            if (beaconInfo == null) {
                                addFoundBeaconToList(foundBeacon);
                            }
                        }
                    }
                });
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

    public void addNewRoomBtnClick(View view) {
        DocumentReference roomRef = firestoreDb.collection(ROOMS_COLLECTION_NAME).document();
        roomKey = roomRef.getId();

        Intent roomConfigIntent = new Intent(this, RoomConfigActivity.class);
        roomConfigIntent.putExtra("roomKey", roomKey);

        startActivityForResult(roomConfigIntent, ROOM_REQUEST_CODE);
    }

    public void editRoomBtnClick(View view) {
        DocumentReference roomRef = firestoreDb.collection(ROOMS_COLLECTION_NAME).document(roomKey);

        Intent roomConfigIntent = new Intent(this, RoomConfigActivity.class);
        roomConfigIntent.putExtra("roomKey", roomKey);

        startActivity(roomConfigIntent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ROOM_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                room = (Room) data.getSerializableExtra("room");
                roomKey = data.getStringExtra("roomKey");

                if (room != null) {
                    Log.i(TAG, "Room: " + room.getName() + " RoomKey: " + roomKey);
//                    roomNameView.setText(room.getName());
                }
            }
        }
    }


    public void scanBtnClick(View view) {
        if (mBeaconManager.isBound(this)) {
            mBeaconManager.unbind(this);
            scanBtn.setText(R.string.start);
        } else {
            mBeaconManager.bind(this);
            scanBtn.setText(R.string.stop);
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
                // User action not recognized.
                return super.onOptionsItemSelected(item);

        }
    }

}
