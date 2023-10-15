package com.hokuapps.getcurrentlocationdetails;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class LocationDetails implements HokuLocationProvider.NewLocationCallback {

    private final WebView mWebView;
    private final Context mContext;
    private final Activity mActivity;

    private JSONObject currentJsonObj;

    private String currentLatLongCallback = null;

    public static final int REQUEST_CHECK_SETTINGS = 0x1;

    private ProgressDialog progressDialog;
    private HokuLocationProvider mLocationProvider;

    public LocationDetails(WebView webView,Context context,Activity activity) {
        this.mWebView  = webView;
        this.mContext = context;
        this.mActivity = activity;
    }

    public void getCurLocationLatLong(final String currentLatLong) {

        if(currentLatLong == null)
            return;

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                try {
                    JSONObject jsonObj = new JSONObject(currentLatLong);
                    currentJsonObj = jsonObj;
                    currentLatLongCallback = Utility.getStringObjectValue(jsonObj, "nextButtonCallback");

                    if (Utility.isGPSInfo(mContext)) {
                        if (Utility.isGooglePlayServicesAvailable(mContext,mActivity)) {
                            mLocationProvider = new HokuLocationProvider(mContext, LocationDetails.this);
                            mLocationProvider.setDisplacementInMeter(0);
                            mLocationProvider.connect();
                        }

                    } else {
                        gpsSettingsRequestPopup();
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

                JSONObject jsonObj = null;
                try {
                    jsonObj = new JSONObject(currentLatLong);
                    currentJsonObj = jsonObj;
                    currentLatLongCallback = Utility.getStringObjectValue(jsonObj, "nextButtonCallback");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Utility.callJavaScriptFunction(mActivity, mWebView, currentLatLongCallback, jsonObj);
                currentLatLongCallback = null;

            }


        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();



    }

    private void gpsSettingsRequestPopup() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationProvider.mLocationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(mContext).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                resolvable.startResolutionForResult(
                                        mActivity,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException | ClassCastException e) {
                                e.printStackTrace();
                            }

                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Toast.makeText(mContext, "Setting change unavailable.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    private void showOrHideProgressDialogPopup(boolean shown, String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext, R.style.AppCompatAlertDialogStyle);

            progressDialog.setCancelable(false);
            progressDialog.setMessage(message);

        }

        if (shown) {
            progressDialog.show();
        } else {
            //            progressDialog.hide();
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void handleNewLocation(Location location) {

        //check if activity is visible to user
        if (Utility.isActivityLive(mActivity)) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        if (location != null) {
                            mLocationProvider.getCompleteAddressJsonObject(location.getLatitude(), location.getLongitude(), new HokuLocationProvider.OnCapturedLocationObject(){
                                @Override
                                public void onCaptured(JSONObject jsonObjectAddress) {

                                    try {
                                        jsonObjectAddress.put("response", currentJsonObj);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Utility.callJavaScriptFunction(mActivity, mWebView, currentLatLongCallback, jsonObjectAddress);
                                    currentLatLongCallback = null;
                                }
                            });
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

    }

    @Override
    public void handleLastLocation(Location location) {

        if (Utility.isActivityLive(mActivity)) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        if (location != null) {
                            mLocationProvider.getCompleteAddressJsonObject(location.getLatitude(), location.getLongitude(), new HokuLocationProvider.OnCapturedLocationObject(){
                                @Override
                                public void onCaptured(JSONObject jsonObjectAddress) {

                                    try {
                                        jsonObjectAddress.put("response", currentJsonObj);
                                        JSONObject object = new JSONObject(jsonObjectAddress.toString());
                                        jsonObjectAddress.put("locationDict", object);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Utility.callJavaScriptFunction(mActivity, mWebView, currentLatLongCallback, jsonObjectAddress);
                                    currentLatLongCallback = null;
                                }
                            });
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

    }
}
