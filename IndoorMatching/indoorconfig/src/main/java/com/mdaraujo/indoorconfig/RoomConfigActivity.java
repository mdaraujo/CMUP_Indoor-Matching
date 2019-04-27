package com.mdaraujo.indoorconfig;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mdaraujo.commonlibrary.model.Room;

import static com.mdaraujo.commonlibrary.model.Room.ROOMS_COLLECTION_NAME;

public class RoomConfigActivity extends AppCompatActivity {

    private static String TAG = "BeaconConfigActivity";

    private FirebaseFirestore firestoreDb;
    private Room room;
    private String roomKey;

    private TextView roomNameView;
    private TextView roomServerURLView;
    private TextView roomWidth;
    private TextView roomHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_config);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firestoreDb = FirebaseFirestore.getInstance();
        roomNameView = findViewById(R.id.room_name_view);
        roomServerURLView = findViewById(R.id.room_server_url_view);
        roomWidth = findViewById(R.id.room_width_view);
        roomHeight = findViewById(R.id.room_height_view);

        Intent intent = getIntent();
        roomKey = intent.getExtras().getString("roomKey");
        DocumentReference roomRef = firestoreDb.collection(ROOMS_COLLECTION_NAME).document(roomKey);

        roomRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot room = task.getResult();
                    if (room.exists()) {
                        roomNameView.setText(room.get("name").toString());
                        roomServerURLView.setText(room.get("serverURL").toString());
                        roomWidth.setText(room.get("width").toString());
                        roomHeight.setText(room.get("height").toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Room created", Snackbar.LENGTH_LONG);

                Room room = new Room(roomNameView.getText().toString(), roomServerURLView.getText().toString(), Float.parseFloat(roomWidth.getText().toString()), Float.parseFloat(roomHeight.getText().toString()));
                roomRef.set(room)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Room successfully set!");

                                Intent mainActivityRoomDataIntent = new Intent();
                                mainActivityRoomDataIntent.putExtra("room", room);
                                mainActivityRoomDataIntent.putExtra("roomKey", roomKey);
                                Log.i(TAG, "RoomKey: " + roomKey);

                                setResult(RESULT_OK, mainActivityRoomDataIntent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error setting room", e);
                            }
                        });


            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
