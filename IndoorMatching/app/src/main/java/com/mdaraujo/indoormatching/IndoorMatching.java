package com.mdaraujo.indoormatching;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.mdaraujo.commonlibrary.BaseMainActivity;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

public class IndoorMatching extends Application implements BootstrapNotifier {
    private static final String TAG = "IndoorMatchingApp";
    private RegionBootstrap regionBootstrap;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "App started up");
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        // beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        // set the duration of the scan to be 1.1 seconds
        beaconManager.setBackgroundScanPeriod(1100L);
        // set the time between each scan to be 2.5min (150 seconds)
        beaconManager.setBackgroundBetweenScanPeriod(150000L);

        // wake up the app when any beacon is seen (you can specify specific id filers in the parameters below)
        regionBootstrap = new RegionBootstrap(this, BaseMainActivity.getCustomRegion());
    }

    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // Don't care
    }

    @Override
    public void didEnterRegion(Region arg0) {
        Log.i(TAG, "Got a didEnterRegion call");
        // This call to disable will make it so the activity below only gets launched the first time a beacon is seen (until the next time the app is launched)
        // if you want the Activity to launch every single time beacons come into view, remove this call.
        regionBootstrap.disable();
        Intent intent = new Intent(this, MainActivity.class);
        // IMPORTANT: in the AndroidManifest.xml definition of this activity, you must set android:launchMode="singleInstance" or you will get two instances
        // created when a user launches the activity manually and it gets launched from here.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationChannel notificationChannel;
        NotificationManager notificationManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("indoor_matching_channel_id", "indoor_matching_channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Indoor Matching Notification Channel");
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "indoor_matching_channel_id")
                .setSmallIcon(R.drawable.color_circle)
                .setContentTitle("Indoor Matching")
                .setContentText("This establishment supports this application. Click to open the app!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(new long[]{100L, 0L})
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));

        Log.d("NOTIFICATION", "NOTIFY!");

        notificationManagerCompat.notify(1, notificationBuilder.build());

    }

    @Override
    public void didExitRegion(Region arg0) {
        // Don't care
    }
}