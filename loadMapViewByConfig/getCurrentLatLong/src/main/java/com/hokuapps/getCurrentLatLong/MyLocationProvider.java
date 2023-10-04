package com.hokuapps.getCurrentLatLong;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class MyLocationProvider {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Context context;

    public MyLocationProvider(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public interface LocationCallback {
        void onNewLocationAvailable(Location location);
    }

    public void requestLocationUpdates(final LocationCallback callback) {

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    callback.onNewLocationAvailable(location);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            Log.e("LocationProvider", "Security exception: " + e.getMessage());
        }
    }

    public void removeLocationUpdates() {
        try {
            locationManager.removeUpdates(locationListener);
        } catch (SecurityException e) {
            Log.e("LocationProvider", "Security exception: " + e.getMessage());
        }
    }
}