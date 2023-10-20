package com.hokuapps.getbackgroundlocationupdates.Utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;


import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.getbackgroundlocationupdates.backgroundTask.SendLocationRestApiClientEvent;
import com.hokuapps.getbackgroundlocationupdates.callbacks.OnCapturedLocationObject;
import com.hokuapps.getbackgroundlocationupdates.callbacks.SendLocationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import okhttp3.Response;

public class LocUtility {

    public static  String TAG = LocUtility.class.getName();

    public static final ReentrantReadWriteLock REENTRANT_READ_WRITE_LOCK = new ReentrantReadWriteLock();


    /**
     * get String value from JSONObject by passing key
     * @param obj
     * @param fieldName
     * @return
     */
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


    /**
     * Check Internet connection availability.
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {

        if (context == null) {
            return false;
        }

        ReentrantReadWriteLock.ReadLock readLock = REENTRANT_READ_WRITE_LOCK.readLock();
        readLock.lock();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // if no network is available networkInfo will be null, otherwise check if we are connected
        try {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                return true;
            }
        } catch (Exception e) {
        } finally {
            readLock.unlock();
        }
        return false;
    }

    /**
     * send response data from native to javascript function
     * @param activity
     * @param webView
     * @param callingJavaScriptFn
     * @param response
     */

    public static void callJavaScriptFunction(Activity activity, final WebView webView, final String callingJavaScriptFn, final JSONObject response) {

        if (activity == null) return;


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.evaluateJavascript(String.format("javascript:" + callingJavaScriptFn + "(%s)", response), null);
                    } else {
                        webView.loadUrl(String.format("javascript:" + callingJavaScriptFn + "(%s)", response));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    /**
     * send location details to server
     * @param location
     * @param responseData
     * @param mContext
     */

    public static void sendFilteredLocationToServer(Location location, String responseData, Context mContext) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            String callUrl = LocUtility.getStringObjectValue(jsonObject,"callUrl");
            String driverID=  LocUtility.getStringObjectValue(jsonObject,"driverID");
            String tokenKey=  LocUtility.getStringObjectValue(jsonObject,"tokenKey");
            String secretKey=  LocUtility.getStringObjectValue(jsonObject,"secretKey");
            String userID = LocUtility.getStringObjectValue(jsonObject,"userID");
            String userName = LocUtility.getStringObjectValue(jsonObject,"userName");//userName

            JSONObject addDeviceObject = new JSONObject();
            if(tokenKey!= null && !tokenKey.isEmpty()) addDeviceObject.put("tokenKey",tokenKey);
            if(secretKey!= null && !secretKey.isEmpty()) addDeviceObject.put("secretKey",secretKey);
            if(driverID!= null && !driverID.isEmpty()) addDeviceObject.put("driverID",driverID);
            if(userID!= null && !userID.isEmpty()) addDeviceObject.put("userID",userID);
            if(userName!= null && !userName.isEmpty()) addDeviceObject.put("userName",userName);

            try {
                String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
                addDeviceObject.put("lat", location.getLatitude());
                addDeviceObject.put("long", location.getLongitude());
                addDeviceObject.put("timestamp", Calendar.getInstance().getTimeInMillis());
                addDeviceObject.put("date", currentDate);
                addDeviceObject.put("time", currentTime);
            }catch (Exception e){
                Log.e(TAG, "TimeDateException ==" + e.getLocalizedMessage());
            }


            Log.e(TAG, "body ==" + addDeviceObject.toString());
            Log.e(TAG, "URL ==" + callUrl);
            SendLocationRestApiClientEvent restApiClientEvent = new SendLocationRestApiClientEvent(mContext, callUrl);
            restApiClientEvent.setRequestJson(addDeviceObject);
            restApiClientEvent.setListener(new SendLocationListener() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    System.out.println(jsonObject != null ? jsonObject.toString() : "error");
                    if(jsonObject != null) {
                        Log.e(TAG, "process===" + jsonObject.toString());
                        LocPref locPref = new LocPref(mContext);
                        locPref.setDoubleValue("latitude",location.getLatitude());
                        locPref.setDoubleValue("longitude",location.getLongitude());
                    }

                }

                @Override
                public void onFailure(Response error) {
                    Log.e(TAG, "error===" + error.toString());
                }


            });

            restApiClientEvent.setLooper(Looper.getMainLooper());
            restApiClientEvent.fire();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }


    /**
     * Get complete address string from lat/lng.
     *
     * @param lat
     * @param lng
     * @param onCapturedLocationObject
     */
    public static void getCompleteAddressJsonObject(double lat, double lng, Context mContext, final OnCapturedLocationObject onCapturedLocationObject) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                JSONObject addressString = getAddressJsonObjectFromLatLng(lat, lng,mContext);
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
     * Get JSONObject of address provided with parameter lat/lng.
     *
     * @param latitude
     * @param longitude
     * @return JSONObject build by this method, look here for detailed view }
     */
    private static JSONObject getAddressJsonObjectFromLatLng(double latitude, double longitude, Context mContext) {

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

                    JSONObject jsonObjAddress = getAddressJson(fetchedAddress);

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
     *  returns javaobject fill with address details
     * @param fetchedAddress
     * @return
     */
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


    /**
     * check for location permission given or not
     * @param permissionListener
     */
    public static void askForLocationPermission(PermissionListener permissionListener){
        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();
    }


}
