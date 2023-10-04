package com.hokuapps.getCurrentLatLong;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.LocationManager;
import android.os.Build;
import android.webkit.WebView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONObject;

public class Utility {

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public static String getStringObjectValue(JSONObject obj, String fieldName) {
        try {
            if (obj == null) return "";

            if (obj.has(fieldName)) {
                Object o = obj.get(fieldName);
                if (o != null) {
                    return o.toString();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static boolean isGPSInfo(Context context) {
        LocationManager locationmanager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        return locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isGooglePlayServicesAvailable(Context context, Activity activity) {
        // Check that Google Play services is available
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            return true;
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, resultCode, CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
            return false;
        }
    }

    public static JSONObject getAddressJson(Address fetchedAddress) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("country", fetchedAddress.getCountryName());
            jsonObj.put("countryCode", fetchedAddress.getCountryCode());
            jsonObj.put("state", fetchedAddress.getAdminArea());
            jsonObj.put("name", fetchedAddress.getSubThoroughfare() + " " + fetchedAddress.getThoroughfare());
            jsonObj.put("subAdminArea", fetchedAddress.getSubAdminArea());
            jsonObj.put("postalCode", fetchedAddress.getPostalCode());
            jsonObj.put("city", fetchedAddress.getLocality());
            jsonObj.put("subLocality", fetchedAddress.getSubLocality());

            return jsonObj;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObj;
    }

    public static boolean isActivityLive(Activity activity) {
        if (activity == null) {
            return false;
        }
        return !activity.isFinishing();
    }


    public static void callJavaScriptFunction(Activity activity, WebView mWebView, String callingJavaScriptFn, JSONObject response) {
        if (activity == null) return;


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mWebView.evaluateJavascript(String.format("javascript:" + callingJavaScriptFn + "(%s)", response), null);
                    } else {
                        mWebView.loadUrl(String.format("javascript:" + callingJavaScriptFn + "(%s)", response));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }
}
