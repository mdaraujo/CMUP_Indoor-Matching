package com.mdaraujo.indoorconfig;

import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mdaraujo.commonlibrary.BaseMainActivity;
import com.mdaraujo.commonlibrary.model.BeaconInfo;
import com.mdaraujo.commonlibrary.model.Room;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.mdaraujo.commonlibrary.model.BeaconInfo.BEACONS_COLLECTION_NAME;
import static com.mdaraujo.commonlibrary.model.Room.ROOMS_COLLECTION_NAME;

public class MainActivity extends BaseMainActivity implements RangeNotifier, RecyclerViewClickListener {

    private static String TAG = "MainActivity";

    private String roomKey;

    private BeaconsAdapter beaconsAdapter;
    private RecyclerView recyclerView;

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

        recyclerView = (RecyclerView) findViewById(R.id.beacons_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);

        roomNameView = findViewById(R.id.room_name_text);
        roomAddBtn = findViewById(R.id.room_add_btn);
        roomEditBtn = findViewById(R.id.room_edit_btn);

        roomCanvas = findViewById(R.id.room_canvas);

    }

    @Override
    protected void refreshScan() {
        super.refreshScan();
        beaconsAdapter = new BeaconsAdapter(beaconsInfo, this);
        recyclerView.setAdapter(beaconsAdapter);
    }

    @Override
    public void onBeaconServiceConnect() {
        super.onBeaconServiceConnect();
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

        List<BeaconInfo> beaconsToDraw = new ArrayList<>();

        for (BeaconInfo beaconInfo : beaconsInfo)
            if (beaconInfo.getRoomKey() != null)
                beaconsToDraw.add(beaconInfo);

        roomCanvas.drawBeacons(beaconsToDraw);
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        BeaconInfo beacon = beaconsInfo.get(position);
//        Toast.makeText(v.getContext(), beacon.getInstanceId(), Toast.LENGTH_SHORT).show();

        if (room == null && roomKey == null) {
            Toast.makeText(v.getContext(), "Room not found. Please add new room.", Toast.LENGTH_SHORT).show();
            return;
        }

        beacon.setRoomKey(roomKey);
        Intent beaconConfigIntent = new Intent(this, BeaconConfigActivity.class);
        beaconConfigIntent.putExtra("BeaconInfo", beacon);

        startActivity(beaconConfigIntent);
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
                    roomNameView.setText(room.getName());
                    roomAddBtn.setVisibility(View.GONE);
                    roomEditBtn.setVisibility(View.VISIBLE);

                }
            }
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
            case R.id.action_refresh:
                if (mBeaconManager.isBound(this)) {
                    mBeaconManager.unbind(this);
                }
                refreshScan();
                return true;
            case R.id.user_profile:
                startActivity(new Intent(this, UserProfileActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
