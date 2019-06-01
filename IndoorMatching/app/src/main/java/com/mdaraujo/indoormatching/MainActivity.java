package com.mdaraujo.indoormatching;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mdaraujo.commonlibrary.BaseMainActivity;
import com.mdaraujo.commonlibrary.model.BeaconInfo;
import com.mdaraujo.commonlibrary.model.Room;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collection;

import static com.mdaraujo.commonlibrary.model.BeaconInfo.BEACONS_COLLECTION_NAME;
import static com.mdaraujo.commonlibrary.model.Room.ROOMS_COLLECTION_NAME;

public class MainActivity extends BaseMainActivity {

    private static String TAG = "MainActivity";

    private static String GATEWAY_TOPIC = "gateway_service";

    private MqttAndroidClient client;

    protected ImageView matchItemColorView;
    protected TextView matchItemNameView;
    protected TextView matchItemCoordsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        fillBaseLayout();

        View matchItemView = findViewById(R.id.match_item);
        matchItemColorView = matchItemView.findViewById(R.id.beacon_color);
        matchItemNameView = matchItemView.findViewById(R.id.beacon_name);
        matchItemCoordsView = matchItemView.findViewById(R.id.beacon_coords);

        matchItemColorView.setColorFilter(Color.WHITE);
        matchItemNameView.setText(R.string.server_waiting);

        mBeaconManager.setBackgroundScanPeriod(400L);
        mBeaconManager.setBackgroundBetweenScanPeriod(1000L);
        
    }

    private void sendMessage(String topic, String payload) {
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void connectToMqtt() {
        if (room == null)
            return;

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), room.getServerURL(),
                clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "MqttAndroidClient: onSuccess");
                    JSONObject userInfoMsg = new JSONObject();
                    try {
                        userInfoMsg.put("msgType", 0);
                        userInfoMsg.put("userId", user.getUid());
                        userInfoMsg.put("name", user.getDisplayName());
                        

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendMessage(GATEWAY_TOPIC, userInfoMsg.toString());
                    matchItemNameView.setText(R.string.match_not_found);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "MqttAndroidClient: onFailure");
                    matchItemNameView.setText(R.string.server_not_found);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> foundBeacons, Region region) {
        super.didRangeBeaconsInRegion(foundBeacons, region);

        PointF position = positionEstimation.getEstimation();

        if (position != null) {
            JSONObject userInfoMsg = new JSONObject();
            try {
                userInfoMsg.put("msgType", 1);
                userInfoMsg.put("userId", user.getUid());
                userInfoMsg.put("x", position.x);
                userInfoMsg.put("y", position.y);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessage(GATEWAY_TOPIC, userInfoMsg.toString());
        }
    }

    @Override
    protected void getRoomOfBeacon(Beacon foundBeacon) {
        String instanceId = foundBeacon.getId2().toHexString();

        // do query on beacon MAC address
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

                                        roomNameView.setText("Welcome to " + room.getName());
                                        getBeaconsOfRoom(beaconInfo.getRoomKey());

                                        connectToMqtt();
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
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
