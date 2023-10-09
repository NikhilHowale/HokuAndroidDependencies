package com.hokuapps.loadmapviewbyconfig.locationProvider;

import static com.google.android.gms.location.LocationRequest.*;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.LAT;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.LONG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.loadmapviewbyconfig.constant.MapConstant;
import com.hokuapps.loadmapviewbyconfig.utility.Utility;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class LocationProvider implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int REQUEST_CODE_BACKGROUND_LOCATION_UPDATES = 1000;
    private boolean shouldStartBackgroundLocationUpdates = false;
    private String requestData = "";

    public interface LocationCallback {
         void handleNewLocation(Location location);

         void handleLastLocation(Location location);
    }

    public static final String TAG = LocationProvider.class.getSimpleName();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationCallback mLocationCallback;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    public com.google.android.gms.location.LocationRequest mLocationRequest;
    //    =============== ******** Google location CallBack object *************** ==============
    private com.google.android.gms.location.LocationCallback locationCallback;

    public LocationProvider(Context context, LocationCallback callback) {
        mContext = context;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .build();


        mLocationCallback = callback;
//        ================= *********** Google Location callback object ************===================

        // Create the LocationRequest object
        mLocationRequest = create()
                .setPriority(PRIORITY_HIGH_ACCURACY)
                .setInterval(MapConstant.UPDATE_INTERVAL)
                .setFastestInterval(MapConstant.FASTEST_INTERVAL)
                .setSmallestDisplacement(MapConstant.DISPLACEMENT);

        locationCallback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    if (location != null) {
                        mLocationCallback.handleNewLocation(location);
                        return;
                    }
                }
            }
        };
    }

    public void connect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    public void onConnected(Bundle bundle) {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                startLocationUpdates();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };
        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .check();


    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution() && mContext instanceof Activity) {
            try {
                Activity activity = (Activity) mContext;
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationCallback.handleNewLocation(location);
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.getFusedLocationProviderClient(mContext).removeLocationUpdates(locationCallback);

            mGoogleApiClient.disconnect();

        }
    }


    /**
     * Start location updates
     */
    public void startLocationUpdates() {
        try {

            Task<Location> location = LocationServices.getFusedLocationProviderClient(mContext).getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    mLocationCallback.handleLastLocation(task.getResult());
                }
            });
            if (shouldStartBackgroundLocationUpdates) {
                LocationServices.getFusedLocationProviderClient(mContext).requestLocationUpdates(mLocationRequest, getPendingIntent());
            } else {
                LocationServices.getFusedLocationProviderClient(mContext).requestLocationUpdates(mLocationRequest, locationCallback, null);
            }


        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * this application returns pending intent to update the background location
     * @return returns pending intent
     */
    public PendingIntent getPendingIntent() {
        Intent intent = new Intent(mContext, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        intent.putExtra("REQUEST_DATA", getRequestData());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(
                    mContext,
                    REQUEST_CODE_BACKGROUND_LOCATION_UPDATES,
                    intent,
                    PendingIntent.FLAG_MUTABLE
            );
        } else {
            return PendingIntent.getBroadcast(
                    mContext,
                    REQUEST_CODE_BACKGROUND_LOCATION_UPDATES,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        }
    }

    public String getRequestData() {
        return requestData;
    }



    public interface OnCapturedLocationObject {
         void onCaptured(JSONObject jsonObjectAddress);
    }

    public interface OnCapturedLocationString {
         void onCapturedAddress(String addressString);
    }


    /**
     * Get json object containing complete address
     * @param lat latitude
     * @param lng longitude
     * @param onCapturedLocationObject interface
     */
    @SuppressLint("StaticFieldLeak")
    public void getCompleteAddressJsonObject(double lat, double lng, final OnCapturedLocationObject onCapturedLocationObject) {
//    Async to get address string from GeoCoder.
        new AsyncTask<Double, String, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Double... locationArgs) {

                return getAddressJsonObjectFromLatLng(locationArgs[0], locationArgs[1]);
            }

            @Override
            protected void onPostExecute(JSONObject addressString) {
//            You get complete address string here.
                onCapturedLocationObject.onCaptured(addressString);
            }
        }.execute(lat, lng);

    }


    /**
     * Get complete address string
     * @param lat latitude
     * @param lng longitude
     * @param capturedLocationString interface
     */
    @SuppressLint("StaticFieldLeak")
    public void getCompleteAddressString(double lat, double lng, final OnCapturedLocationString capturedLocationString) {
//    Async to get address string from GeoCoder.
        new AsyncTask<Double, String, StringBuilder>() {

            @Override
            protected StringBuilder doInBackground(Double... locationArgs) {

                return getCompleteAddressString(locationArgs[0], locationArgs[1]);
            }

            @Override
            protected void onPostExecute(StringBuilder addressString) {
//            You get complete address string here.
                capturedLocationString.onCapturedAddress(addressString.toString());
            }
        }.execute(lat, lng);

    }


    /**
     * Get address from latitude and longitude and put in json object
     * @param latitude
     * @param longitude
     * @return returns JSONObject containing address details
     */
    private JSONObject getAddressJsonObjectFromLatLng(double latitude, double longitude) {

        Geocoder gc = new Geocoder(mContext, Locale.ENGLISH);
        if (Geocoder.isPresent()) {
            try {
                List<Address> addresses;
                addresses = gc.getFromLocation(latitude, longitude, 1);

                if (addresses.size() > 0) {
                    Address fetchedAddress = addresses.get(0);
                    StringBuilder strAddress = new StringBuilder();
                    for (int i = 0; i <= fetchedAddress.getMaxAddressLineIndex(); i++) {
                        strAddress.append(fetchedAddress.getAddressLine(i)).append(" ");
                    }

                    JSONObject jsonObjAddress = Utility.getAddressJson(fetchedAddress);

                    jsonObjAddress.put(LAT, String.valueOf(latitude));
                    jsonObjAddress.put(LONG, String.valueOf(longitude));

                    return jsonObjAddress;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return new JSONObject();
    }


    /**
     * Get complete address from latitude and longitude
     * @param latitude latitude
     * @param longitude longitude
     * @return returns string containing address details
     */
    public StringBuilder getCompleteAddressString(double latitude, double longitude) {

        Geocoder gc = new Geocoder(mContext, Locale.ENGLISH);
        if (Geocoder.isPresent()) {
            try {
                List<Address> addresses;
                addresses = gc.getFromLocation(latitude, longitude, 1);

                if (addresses.size() > 0) {
                    Address fetchedAddress = addresses.get(0);
                    StringBuilder strAddress = new StringBuilder();
                    for (int i = 0; i <= fetchedAddress.getMaxAddressLineIndex(); i++) {
                        strAddress.append(fetchedAddress.getAddressLine(i)).append(" ");
                    }

                    return strAddress;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return new StringBuilder();
    }


}
