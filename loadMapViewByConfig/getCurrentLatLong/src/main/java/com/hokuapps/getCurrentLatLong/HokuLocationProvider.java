package com.hokuapps.getCurrentLatLong;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HokuLocationProvider implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int REQUEST_CODE_BACKGROUND_LOCATION_UPDATES = 1000;
    private boolean shouldStartBackgroundLocationUpdates = false;
    private String requestData = "";

    public static int UPDATE_INTERVAL = 5000; // 10 sec 10000
    public static int FATEST_INTERVAL = 2000; // 05 sec 5000
    public static int DISPLACEMENT = 0;

    public abstract interface NewLocationCallback {
        public void handleNewLocation(Location location);

        public void handleLastLocation(Location location);
    }

    public static final String TAG = HokuLocationProvider.class.getSimpleName();

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private NewLocationCallback mLocationCallback;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    public LocationRequest mLocationRequest;
    //    =============== ******** Google location CallBack object *************** ==============
    private LocationCallback locationCallback;

    public HokuLocationProvider(Context context, NewLocationCallback callback) {
        mContext = context;

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationCallback = callback;


        // Create the LocationRequest object
        mLocationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(FATEST_INTERVAL)
                .setMaxUpdateDelayMillis(UPDATE_INTERVAL)
                .build();


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

    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public void connect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    public void disconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            LocationServices.getFusedLocationProviderClient(mContext).removeLocationUpdates(locationCallback);

            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
             startLocationUpdates();
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
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
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


    public void startLocationUpdates() {
        try {

            Task<Location> location = LocationServices.getFusedLocationProviderClient(mContext).getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    mLocationCallback.handleLastLocation(task.getResult());
                }
            });

            LocationServices.getFusedLocationProviderClient(mContext).requestLocationUpdates(mLocationRequest, locationCallback, null);


        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return {@link PendingIntent} object for listening location updates in BroadCastReceiver.
     */


    public String getRequestData() {
        return requestData;
    }

    private int displacementMeter = 10;

    public void setDisplacementInMeter(int meter) {
        displacementMeter = meter;
    }



    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public boolean isShouldStartBackgroundLocationUpdates() {
        return shouldStartBackgroundLocationUpdates;
    }

    public void setShouldStartBackgroundLocationUpdates(boolean shouldStartBackgroundLocationUpdates) {
        this.shouldStartBackgroundLocationUpdates = shouldStartBackgroundLocationUpdates;
    }

    public interface OnCapturedLocationObject {
        public void onCaptured(JSONObject jsonObjectAddress);
    }

    public interface OnCapturedLocationString {
        public void onCapturedAddress(String addressString);
    }


    public void getCompleteAddressJsonObject(double lat, double lng, final OnCapturedLocationObject onCapturedLocationObject) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                JSONObject addressString = getAddressJsonObjectFromLatLng(lat, lng);
                onCapturedLocationObject.onCaptured(addressString);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                    }
                });
            }
        });

    }

    /**
     * Get complete address string from lat/lng.
     *
     * @param lat
     * @param lng
     * @param capturedLocationString
     */
    public void getCompleteAddressString(double lat, double lng, final OnCapturedLocationString capturedLocationString) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                JSONObject addressString = getAddressJsonObjectFromLatLng(lat, lng);
                capturedLocationString.onCapturedAddress(addressString.toString());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                    }
                });
            }
        });

    }

    /**
     * Get JSONObject of address provided with parameter lat/lng.
     *
     * @param latitude
     * @param longitude
     * @return JSONObject build by this method, look here for detailed view {@link com.mybeeps.utils.Utility#getAddressJson(android.location.Address)}
     */
    private JSONObject getAddressJsonObjectFromLatLng(double latitude, double longitude) {

        Geocoder gc = new Geocoder(mContext, Locale.ENGLISH);
        if (gc.isPresent()) {
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

                    jsonObjAddress.put("lat", String.valueOf(latitude));
                    jsonObjAddress.put("long", String.valueOf(longitude));

                    return jsonObjAddress;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return new JSONObject();
    }

    /**
     * Get complete address of lat/lng.
     *
     * @param latitude
     * @param longitude
     * @return
     */
    public StringBuilder getCompleteAddressString(double latitude, double longitude) {

        Geocoder gc = new Geocoder(mContext, Locale.ENGLISH);
        if (gc.isPresent()) {
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

                   /* jsonObjAddress.put("lat", String.valueOf(latitude));
                    jsonObjAddress.put("long", String.valueOf(longitude));*/

                    return strAddress;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return new StringBuilder();
    }


}