package com.hokuapps.getbackgroundlocationupdates.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.hokuapps.getbackgroundlocationupdates.R;


public class BackgroundLocationService extends Service {

    public static int UPDATE_INTERVAL = 10 * 1000; // 10 sec 10000
    public static int UPDATE_DISTANCE = 5; // 05 meter
    public static String ACTION_LOCATION = "action_location";
    public static String ARG_LOCATION = "arg_location";
    private int NOTIFICATION_ID = 1001;
    private String CHANNEL_ID = "channel_id";
    private String CHANNEL_NAME = "Channel Name";

    private Bitmap notificationIcon;


    private LocationRequest mLocationRequest = null;
    private FusedLocationProviderClient mFusedLocationClient = null;


    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location currentLocation = locationResult.getLastLocation();
            Log.d("Locations", currentLocation.getLatitude() + "," + currentLocation.getLongitude());

            //Share/Publish Location
            Intent intent = new Intent(ACTION_LOCATION);
            intent.putExtra(ARG_LOCATION, currentLocation);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    };

    //onCreate
    @Override
    public void onCreate() {
        super.onCreate();
        initData();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent!=null && intent.getExtras() != null){
            Bundle extras = intent.getExtras();
            byte[] b = extras.getByteArray("picture");
            notificationIcon = BitmapFactory.decodeByteArray(b, 0, b.length);
        }


        prepareForegroundNotification();
        startLocationUpdates();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopLocation();
        super.onDestroy();
    }

    private void initData() {

        mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(UPDATE_INTERVAL)
                .setMaxUpdateDelayMillis(UPDATE_INTERVAL)
                .setMinUpdateDistanceMeters(UPDATE_DISTANCE)
                .build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(this.mLocationRequest,
                this.locationCallback, Looper.myLooper());
    }




    private void stopLocation() {
        mFusedLocationClient.removeLocationUpdates(this.locationCallback);
    }


    private void prepareForegroundNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentTitle(getString(R.string.app_notification_description))
                .setSmallIcon(IconCompat.createWithBitmap(notificationIcon))
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }




}
