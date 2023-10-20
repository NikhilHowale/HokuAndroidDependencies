package com.hokuapps.getbackgroundlocationupdates.Locations;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.getbackgroundlocationupdates.Utility.LocPref;
import com.hokuapps.getbackgroundlocationupdates.Utility.LocUtility;
import com.hokuapps.getbackgroundlocationupdates.callbacks.ForegroundLocationCallback;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class ForegroundLocation {
    private static volatile ForegroundLocation INSTANCE = null;
    private Context mContext;
    public static  String TAG = ForegroundLocation.class.getName();
    private ForegroundLocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient=null;

    public static int UPDATE_INTERVAL = 10 * 1000; // 10 sec 10000
    public static int UPDATE_DISTANCE = 5; // 05 meter


    private ForegroundLocation() {}

    public static ForegroundLocation getInstance() {
        if(INSTANCE == null) {
            synchronized (ForegroundLocation.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ForegroundLocation();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * start foreground location update and send the location data to server
     * @param responseData
     * @param context
     */
    public void startForegroundLocationUpdate(String responseData, Context context){
        this.mContext = context;

        LocPref locPref = new LocPref(mContext);
        locPref.setDoubleValue("latitude",0);
        locPref.setDoubleValue("longitude",0);

        try {
            JSONObject jsonObject = new JSONObject(responseData);
            String filter = LocUtility.getStringObjectValue(jsonObject,"filter");
            UPDATE_DISTANCE = Integer.parseInt(Objects.requireNonNull(LocUtility.getStringObjectValue(jsonObject, "updateDistance")));
            UPDATE_INTERVAL = Integer.parseInt(Objects.requireNonNull(LocUtility.getStringObjectValue(jsonObject, "updateInterVal")));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startLocationUpdateService(mContext, new ForegroundLocationCallback() {
                    @Override
                    public void onNewUpdatedLocation(Location currentLocation) {
                        Toast.makeText(mContext, currentLocation.getLatitude()+","+currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                        LocPref locPref = new LocPref(mContext);
                        Location lastLocation = new Location("LastLocation");
                        if(locPref.getDoubleValue("latitude")!= 0 && locPref.getDoubleValue("longitude")!= 0 ) {
                            lastLocation.setLatitude(locPref.getDoubleValue("latitude"));
                            lastLocation.setLongitude(locPref.getDoubleValue("longitude"));
                            int distance = Math.round(lastLocation.distanceTo(currentLocation)) ;
                            Log.e(TAG,"distance=="+distance);
                            //Toast.makeText(mContext, "Distance="+distance, Toast.LENGTH_SHORT).show();
                            if(distance >= Integer.parseInt(filter)){
                                LocUtility.sendFilteredLocationToServer(currentLocation,responseData,mContext);
                            }

                        }else{
                            LocUtility.sendFilteredLocationToServer(currentLocation,responseData,mContext);
                        }

                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Ask for location permission and start getting updates
     * @param context
     * @param foregroundLocationCallback
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void startLocationUpdateService (Context context, ForegroundLocationCallback foregroundLocationCallback){
        this.mContext = context;
        this.mLocationCallback = foregroundLocationCallback;
        if(mContext==null || mLocationCallback ==null){
            return;
        }
        LocUtility.askForLocationPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                StartLocationUpdate();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        });
    }


    /**
     * start Foreground Location update
     */
    private void StartLocationUpdate() {

        LocationRequest mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(UPDATE_INTERVAL)
                .setMaxUpdateDelayMillis(UPDATE_INTERVAL)
                .setMinUpdateDistanceMeters(UPDATE_DISTANCE)
                .build();

         mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                this.locationCallback, Looper.myLooper());

    }


    /**
     * stop getting location updates
     */
    private void StopLocationUpdate(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(this.locationCallback);
        }
    }


    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location currentLocation = locationResult.getLastLocation();
            Log.d("Locations", currentLocation.getLatitude() + "," + currentLocation.getLongitude());

           mLocationCallback.onNewUpdatedLocation(currentLocation);
        }
    };

}
