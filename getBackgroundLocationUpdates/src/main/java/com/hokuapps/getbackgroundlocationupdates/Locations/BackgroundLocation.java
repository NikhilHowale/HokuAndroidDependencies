package com.hokuapps.getbackgroundlocationupdates.Locations;


import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gun0912.tedpermission.PermissionListener;

import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.getbackgroundlocationupdates.Utility.LocPref;
import com.hokuapps.getbackgroundlocationupdates.Utility.LocUtility;
import com.hokuapps.getbackgroundlocationupdates.R;
import com.hokuapps.getbackgroundlocationupdates.callbacks.BackgroundLocationCallback;
import com.hokuapps.getbackgroundlocationupdates.services.BackgroundLocationService;


import org.json.JSONObject;

import java.util.List;


public class BackgroundLocation {
    private static volatile BackgroundLocation INSTANCE = null;
    private Context mContext;
    public static  String TAG = BackgroundLocation.class.getName();
    private BackgroundLocationCallback mLocationCallback;

    private byte[] iconBytes;
    private BackgroundLocation() {}
    public static BackgroundLocation getInstance() {
        if(INSTANCE == null) {
            synchronized (BackgroundLocation.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BackgroundLocation();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Start Background location updates and send the location to the server
     * @param responseData
     * @param context
     * @param appName
     * @param bytes
     */
    public void startBackgroundLocationService(String responseData, Context context, String appName, byte[] bytes){
        this.mContext = context;
        if(bytes != null){
            iconBytes = bytes;
        }
        LocPref locPref = new LocPref(mContext);
        locPref.setDoubleValue("latitude",0);
        locPref.setDoubleValue("longitude",0);

        try {
            JSONObject jsonObject = new JSONObject(responseData);
            String filter = LocUtility.getStringObjectValue(jsonObject,"filter");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startLocationUpdateService(mContext,appName, new BackgroundLocationCallback() {
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



    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void startLocationUpdateService (Context context, String appName , BackgroundLocationCallback backgroundLocationCallback){
        this.mContext = context;
        this.mLocationCallback = backgroundLocationCallback;
        if(mContext==null || mLocationCallback ==null){
            return;
        }
        showProminentDialog(appName);
    }


    /**
     * Show prominent dialog to user for using background location
     * @param appName
     */
    private void showProminentDialog(String appName){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        String prominentMessage = appName + " "+mContext.getResources().getString(R.string.prominent_message);
        builder.setMessage(prominentMessage)
                .setCancelable(false)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        askForLocationPermission();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("");
        alert.show();
    }


    /**
     * Check location permission if not given get the location permission
     */
    private void askForLocationPermission(){

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                StartLocationService();
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


    /**
     * start background location service to get location updates
     * even in background
     */
    public void StartLocationService(){
        Intent intent = new Intent(mContext, BackgroundLocationService.class);
        intent.putExtra("picture", iconBytes);
        mContext.startService(intent);
        LocalBroadcastManager
                .getInstance(mContext)
                .registerReceiver(broadcastReceiver, new IntentFilter(BackgroundLocationService.ACTION_LOCATION));
    }


    /**
     * stop the background location updates
     */
    public void stopLocationUpdateService(){
         Intent intent = new Intent(mContext, BackgroundLocationService.class);
         mContext.stopService(intent);
         LocalBroadcastManager
                 .getInstance(mContext)
                 .unregisterReceiver(broadcastReceiver);

        LocPref locPref = new LocPref(mContext);
        locPref.setDoubleValue("latitude",0);
        locPref.setDoubleValue("longitude",0);
     }

    /**
     * Broadcast location object from service
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                Location location =  (Location) intent.getParcelableExtra(BackgroundLocationService.ARG_LOCATION);
                Log.e(TAG,location.getLatitude()+", "+location.getLongitude());
                mLocationCallback.onNewUpdatedLocation(location);

        }
    };
}
