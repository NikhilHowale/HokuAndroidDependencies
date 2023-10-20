package com.hokuapps.getbackgroundlocationupdates.Locations;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
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
import com.hokuapps.getbackgroundlocationupdates.Utility.LocUtility;
import com.hokuapps.getbackgroundlocationupdates.callbacks.OnCapturedLocationObject;
import com.hokuapps.getbackgroundlocationupdates.callbacks.OneTimeLocationCallback;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class OneTimeLocation {

    private static volatile OneTimeLocation INSTANCE = null;
    private Context mContext;
    public static  String TAG = OneTimeLocation.class.getName();
    private OneTimeLocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient=null;

    public static int UPDATE_INTERVAL = 10 * 1000; // 10 sec 10000
    public static int UPDATE_DISTANCE = 5;// 05 meter



    private OneTimeLocation() {}

    public static OneTimeLocation getInstance() {
        if(INSTANCE == null) {
            synchronized (OneTimeLocation.class) {
                if (INSTANCE == null) {
                    INSTANCE = new OneTimeLocation();
                }
            }
        }
        return INSTANCE;
    }

    public void getOneTimeLocation(String responseData, Context context , WebView mWebView , Activity mActivity){
        this.mContext = context;

        try {
            JSONObject jsonObject = new JSONObject(responseData);

            UPDATE_DISTANCE = Integer.parseInt(Objects.requireNonNull(LocUtility.getStringObjectValue(jsonObject, "updateDistance")));
            UPDATE_INTERVAL = Integer.parseInt(Objects.requireNonNull(LocUtility.getStringObjectValue(jsonObject, "updateInterVal")));
            String callbackName = LocUtility.getStringObjectValue(jsonObject,"nextButtonCallBack");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startLocationUpdateService(mContext, new OneTimeLocationCallback() {
                    @Override
                    public void onNewUpdatedLocation(Location currentLocation) {
                        Toast.makeText(mContext, currentLocation.getLatitude()+","+currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                        LocUtility.getCompleteAddressJsonObject(currentLocation.getLatitude(), currentLocation.getLongitude(), mContext, new OnCapturedLocationObject() {
                            @Override
                            public void onCaptured(JSONObject jsonObjectAddress) {


                                try {
                                    JSONObject jsonObject = jsonObjectAddress;
                                    LocUtility.callJavaScriptFunction(mActivity, mWebView, callbackName, jsonObject);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }



                            }
                        });

                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void startLocationUpdateService (Context context, OneTimeLocationCallback oneTimeLocationCallback){
        this.mContext = context;
        this.mLocationCallback = oneTimeLocationCallback;
        if(mContext==null || mLocationCallback ==null){
            return;
        }
        askForLocationPermission();
    }

    private void askForLocationPermission(){

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                StartLocationUpdate();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }

        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();

    }

    private void StartLocationUpdate() {

        LocationRequest mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(UPDATE_INTERVAL)
                .setMaxUpdateDelayMillis(UPDATE_INTERVAL)
                .setMinUpdateDistanceMeters(UPDATE_DISTANCE)
                .setMaxUpdates(1) // To get only one update
                .build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                this.locationCallback, Looper.myLooper());

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
