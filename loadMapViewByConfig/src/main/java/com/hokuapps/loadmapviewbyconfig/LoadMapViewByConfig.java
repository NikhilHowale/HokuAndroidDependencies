package com.hokuapps.loadmapviewbyconfig;

import static com.hokuapps.loadmapviewbyconfig.MapsAppCompactActivity.REQUEST_CHECK_SETTINGS;
import static com.hokuapps.loadmapviewbyconfig.R.string.label_loading;
import static com.hokuapps.loadmapviewbyconfig.R.string.navigation;
import static com.hokuapps.loadmapviewbyconfig.R.string.navigation_via;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.ADDRESS_STRING;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.API_NAME;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.BOTTOM_BUTTON_TEXT;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.CANCEL_BUTTON_CALLBACK;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.COLOR_CODE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.DEST_LATITUDE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.DEST_LONGITUDE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.HEADER_CONTENT_START;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_ADMIN_LOGIN;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_LIVE_TRACKING;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_LOAD_NEAR_BY_PLACES;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_MARKER_CLICK_CALLBACK;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_MARKER_CLICK_SHOW_OVERLAY;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_NAVIGATION;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_NAVIGATION_ON;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_NAV_FROM_CUR_LOC;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_PLOT_ADDRESS_LOCATION;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_PLOT_LOCATION;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_REQUEST_GUARD;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_SEARCH_AUTO_COMPLETE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_SELECT_LOCATION;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_SHOW_BOTTOM_BUTTON;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_SHOW_CURRENT_MARKER;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_SHOW_DIRECTIONS;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_SHOW_OVERLAY;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_SHOW_TAB;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_SHOW_WAZE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.IS_TRACKING;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.LATITUDE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.LIVE_TRACKING_INTERVAL_IN_MS;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.LOCATION_ADDRESS;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.LOCATION_TITLE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.LONGITUDE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.MAP_ZOOM_LEVEL;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.MARKER_DATA;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.MARKER_IMAGE_URL;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.M_DEST_LATITUDE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.M_DEST_LONGITUDE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.NEXT_BUTTON_CALLBACK;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.OPEN_MAP_APP;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.OVERLAY_PAGE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.OVERLAY_SIZE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.QUERY_STRING;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.RADIUS;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.RECORD_ID;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.SEARCH_BY_COUNTRIES_LIST;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.SEARCH_PLACEHOLDER;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.SEARCH_TITLE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.SECRET_KEY;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.SHOW_SEARCHBAR;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.TAB_1;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.TAB_2;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.TOKEN_KEY;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.getCurrentLatLong.HokuLocationProvider;
import com.hokuapps.loadmapviewbyconfig.constant.MapConstant;
import com.hokuapps.loadmapviewbyconfig.models.JSResponseData;
import com.hokuapps.loadmapviewbyconfig.models.LocationMapModel;
import com.hokuapps.loadmapviewbyconfig.utility.Utility;
import com.hokuapps.loadmapviewbyconfig.widgets.BottomSheetShare;

import org.json.JSONObject;

import java.util.List;

public class LoadMapViewByConfig {
    private Context mContext;
    private final Activity mActivity;
    private int isShowWase = 0;
    private String destinationAddress = "";
    private Location location;
    private JSResponseData jsResponseData;
    private int themeId;
    private ProgressDialog progressDialog;
    public HokuLocationProvider mLocationProvider;
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * Parameterized constructor
     * @param mContext Activity context
     * @param activity Activity reference
     * @param themeId theme id to set the theme
     * @param app_id Application id
     * @param html
     */
    public LoadMapViewByConfig(Context mContext, Activity activity, int themeId, String app_id, boolean html) {
        this.mContext = mContext;
        this.mActivity = activity;
        this.themeId = themeId;
        MapConstant.APPLICATION_ID = app_id;
        MapConstant.LOAD_HTML_DIRECTLY = html;
    }

    public LoadMapViewByConfig(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * Entry point of loadMapViewByConfig dependency
     * @param respData Data got from bridge call
     */
    public void loadMapViewByConfig(final String respData) {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                parseLoadMapViewByConfig(respData);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };

        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                .check();
    }

    /**
     * Parse the json data got in bridge call response
     * @param launchMapViewRes Response data to be parsed
     */
    private void parseLoadMapViewByConfig(String launchMapViewRes) {
        try {
            JSONObject responseJsonObj = new JSONObject(launchMapViewRes);

            String headerContentStart = Utility.getStringObjectValue(responseJsonObj, HEADER_CONTENT_START);

            if (headerContentStart != null) {
                responseJsonObj = new JSONObject(headerContentStart);
            }

            final LocationMapModel locationMapModel = getLocationMapObject(responseJsonObj);
            isShowWase = Utility.getJsonObjectIntValue(responseJsonObj, IS_SHOW_WAZE);
            MapConstant.AUTH_TOKEN = Utility.getStringObjectValue(responseJsonObj, TOKEN_KEY);
            MapConstant.AUTH_SECRET_KEY = Utility.getStringObjectValue(responseJsonObj, SECRET_KEY);
            MapConstant.THEME_ID = themeId;

            if (locationMapModel != null) {

                if (headerContentStart != null) {
                    locationMapModel.setResponseData(headerContentStart);
                } else {
                    locationMapModel.setResponseData(launchMapViewRes);
                }

                if (locationMapModel.isOpenMapApp()) {
                    //open google default map application
                    if (locationMapModel.getIsShowDirection() == 1) {

                        if (!TextUtils.isEmpty(locationMapModel.getAddressString())) {

                            destinationAddress = locationMapModel.getAddressString();
                            if (location != null) {
                                handleShowDirectionFromCur();
                            } else {
                                if (isGPSInfo()) {
                                    if (isGooglePlayServicesAvailable()) {
                                        mLocationProvider.connect();
                                    }
                                } else {
                                    gpsSettingsRequestPopup();
                                }
                            }
                        } else {
//                            Show direction bottom sheet chooser if showWaze == 1 else go with GoogleMapDirection default.
                            if (isShowWase == 1) {
                                BottomSheetShare bottomSheetShare = new BottomSheetShare();
                                bottomSheetShare.setNavigation(true);
                                bottomSheetShare.setAppSelectedListener(appInfo -> {
                                    destinationAddress = null;
                                    Utility.openDirectionVia(appInfo, mContext, locationMapModel.getLatitude(),
                                            locationMapModel.getLongitude(), locationMapModel.getDestLatitude(), locationMapModel.getDestLongitude());
                                });
                                bottomSheetShare.setTitle(mContext.getString(navigation_via));
                                bottomSheetShare.show(((AppCompatActivity) mContext).getSupportFragmentManager(), mContext.getString(navigation));
                                return;
                            }
                            if (locationMapModel.getIsNavFromCurLoc() == 1)
                                Utility.openGoogleMapDirection(mContext, locationMapModel.getDestLatitude(),
                                        locationMapModel.getDestLongitude());
                            else
                                Utility.openGoogleMapDirection(mContext, locationMapModel.getLatitude(),
                                        locationMapModel.getLongitude(), locationMapModel.getDestLatitude(), locationMapModel.getDestLongitude());
                        }

                    } else if (locationMapModel.getIsPlotAddressLocation() || !TextUtils.isEmpty(locationMapModel.getAddressString())) {
                        Utility.openInExternalMapByAddress(mContext, locationMapModel.getAddressString());
                    } else {
                        Utility.openInExternalMapByLatLong(mContext, locationMapModel.getLatitude(), locationMapModel.getLongitude());
                    }

                } else {
                    MapsAppCompactActivity.startActivityForResult(mActivity, locationMapModel);
                }

                JSResponseData jsResponseData = new JSResponseData();
                if (headerContentStart != null) {
                    jsResponseData.setResponseData(headerContentStart);
                } else {
                    jsResponseData.setResponseData(launchMapViewRes);
                }
                jsResponseData.setLocationMapModel(locationMapModel);
                setJsResponseData(jsResponseData);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Setting gps request popup
     */
    public void gpsSettingsRequestPopup() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationProvider.mLocationRequest);

        builder.setAlwaysShow(true); //this is the key ingredient
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(mActivity).checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                // All location settings are satisfied. The client can initialize location
                // requests here.

            } catch (ApiException exception) {
                switch (exception.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                    mActivity,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        } catch (ClassCastException e) {
                            // Ignore, should be an impossible error.
                        }
                        showOrHideProgressDialogPopup(false);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Toast.makeText(mContext, mContext.getString(R.string.setting_change_unavailable), Toast.LENGTH_SHORT).show();
                        showOrHideProgressDialogPopup(false);
                        break;
                }
            }
        });
    }

    /**
     * Show or hide progress dialog popup based on given value and message
     * @param shown Boolean value to show or hide progress bar
     */
    private void showOrHideProgressDialogPopup(boolean shown) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext, R.style.AppCompatAlertDialogStyle);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(mContext.getResources().getString(label_loading));
        }

        if (shown) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * Checks if google play service is available or not
     * @return returns boolean value (True,False)
     */
    boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            return true;
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(mActivity, resultCode, CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
            return false;
        }
    }

    /**
     * returns gps information(enable,disable)
     * @return Returns gps information
     */
    private boolean isGPSInfo() {

        LocationManager locationmanager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        // YOUR MAPS ACTIVITY CALLING or WHAT YOU NEED
        return locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Opens external map and show direction given latitude and longitude
     */
    private void handleShowDirectionFromCur() {
        try {
            if (!TextUtils.isEmpty(destinationAddress)) {

                final LatLng destLoc = Utility.getLocationFromAddress(destinationAddress, mContext);

                if (location != null && destLoc != null) {

                    if (isShowWase == 1) {
                        BottomSheetShare bottomSheetShare = new BottomSheetShare();
                        bottomSheetShare.setNavigation(true);
                        bottomSheetShare.setAppSelectedListener(appInfo -> {
                            isShowWase = 0;
                            Utility.openDirectionVia(appInfo, mContext, location.getLatitude(),
                                    location.getLongitude(), destLoc.latitude, destLoc.longitude);
                        });
                        bottomSheetShare.setTitle(mContext.getString(navigation_via));
                        bottomSheetShare.show(((AppCompatActivity) mContext).getSupportFragmentManager(), mContext.getString(navigation));
                        destinationAddress = null;
                        return;
                    }

                    Utility.openGoogleMapDirection(mContext, location.getLatitude(),
                            location.getLongitude(), destLoc.latitude, destLoc.longitude);
                }
                destinationAddress = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Set response data to json object
     * @param jsResponseData parsed response data
     */
    public void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }


    public void handleMapResult(int resultCode, Intent intent, WebView mWebView){
        try {
            if (intent != null && intent.getExtras() != null ) {
                LocationMapModel locationMapModel = intent.getExtras().getParcelable(MapConstant.MAP_LOCATION_MODEL);

                if(locationMapModel == null) return;
                if (resultCode == Activity.RESULT_OK) {

                    JSONObject jsonObjResponse = new JSONObject();

                    String mapResultCallback = intent.getExtras().containsKey(MapConstant.EXTRA_MAP_RESULT_CALLBACK)
                            ? intent.getExtras().getString(MapConstant.EXTRA_MAP_RESULT_CALLBACK)
                            : "";

                    if (!TextUtils.isEmpty(mapResultCallback)) {
                        jsonObjResponse = new JSONObject(mapResultCallback);
                    }

                    Utility.callJavaScriptFunction(mActivity, mWebView,
                            locationMapModel.getNextButtonCallback(), jsonObjResponse);

                }
                else {

                    JSONObject jsonObj = new JSONObject(locationMapModel.getResponseData());

                    if (Utility.getJsonObjectBooleanValue(jsonObj, "isMenuShow")) {
                        mActivity.finish();
                    } else {
                        String backQueryCallBackName = Utility.getStringObjectValue(jsonObj, "backCallbackFunction");

                        if (!TextUtils.isEmpty(backQueryCallBackName)) {
                            Utility.callJavaScriptFunction(mActivity, mWebView, backQueryCallBackName, jsonObj);
                        } else if (!TextUtils.isEmpty(Utility.getStringObjectValue(jsonObj, "nextButtonCallback"))) {
                            Utility.callJavaScriptFunction(mActivity, mWebView,backQueryCallBackName, jsonObj);
                        }
                    }
                }
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    /**
     * set data to json object
     * @param responseJsonObj parsed response data containing location details
     * @return Returns LocationMapModel object
     */
    private LocationMapModel getLocationMapObject(JSONObject responseJsonObj) {
        LocationMapModel locationMapModel = new LocationMapModel();
        try {
            locationMapModel.setPageTitle(Utility.getStringObjectValue(responseJsonObj, MapConstant.Keys.PAGE_TITLE));
            locationMapModel.setNextButtonCallback(Utility.getStringObjectValue(responseJsonObj, NEXT_BUTTON_CALLBACK));
            locationMapModel.setCancelButtonCallback((String) Utility.getJsonObjectValue(responseJsonObj, CANCEL_BUTTON_CALLBACK));
            locationMapModel.setColorCode(responseJsonObj.has(COLOR_CODE) ? responseJsonObj.getString(COLOR_CODE) : "#448aff");

            //For contact growth
            locationMapModel.setIsLoadNearByPlaces(Utility.getJsonObjectIntValue(responseJsonObj, IS_LOAD_NEAR_BY_PLACES));
            locationMapModel.setNearRadius(Utility.getJsonObjectIntValue(responseJsonObj, RADIUS));
            locationMapModel.setIsShowTab(Utility.getJsonObjectIntValue(responseJsonObj, IS_SHOW_TAB));
            locationMapModel.setTab1(Utility.getStringObjectValue(responseJsonObj, TAB_1));
            locationMapModel.setTab2(Utility.getStringObjectValue(responseJsonObj, TAB_2));
            locationMapModel.setSearchPlaceholder(Utility.getStringObjectValue(responseJsonObj, SEARCH_PLACEHOLDER));
            locationMapModel.setSearchTitle(Utility.getStringObjectValue(responseJsonObj, SEARCH_TITLE));
            locationMapModel.setIsMarkerClickCallback(Utility.getJsonObjectIntValue(responseJsonObj, IS_MARKER_CLICK_CALLBACK));
            locationMapModel.setMapZoomLevel(Utility.getJsonObjectIntValue(responseJsonObj, MAP_ZOOM_LEVEL));
            //common setting to show or hide bottom button and its action
            locationMapModel.setIsShowBottomButton(Utility.getJsonObjectIntValue(responseJsonObj, IS_SHOW_BOTTOM_BUTTON));
            locationMapModel.setBottomButtonText(Utility.getStringObjectValue(responseJsonObj, BOTTOM_BUTTON_TEXT));
            //for select location
            locationMapModel.setIsSelectLocation(Utility.getJsonObjectIntValue(responseJsonObj, IS_SELECT_LOCATION));
            //for live tracking guard
            locationMapModel.setIsLiveTracking(Utility.getJsonObjectIntValue(responseJsonObj, IS_LIVE_TRACKING));
            locationMapModel.setLiveTrackingIntervalInMs(Utility.getJsonObjectIntValue(responseJsonObj, LIVE_TRACKING_INTERVAL_IN_MS));
            locationMapModel.setIsShowCurrentMarker(Utility.getJsonObjectIntValue(responseJsonObj, IS_SHOW_CURRENT_MARKER));
            //for Request tracker guard
            locationMapModel.setIsRequestGaurd(Utility.getJsonObjectIntValue(responseJsonObj, IS_REQUEST_GUARD));
            //For navigation route path draw
            locationMapModel.setIsNavigation(Utility.getJsonObjectIntValue(responseJsonObj, IS_NAVIGATION));
            locationMapModel.setIsNavigationOn(Utility.getJsonObjectIntValue(responseJsonObj, IS_NAVIGATION_ON));
            //To draw route path need following params
            locationMapModel.setLatitude(responseJsonObj.has(LATITUDE) ? responseJsonObj.getDouble(LATITUDE) : 0.0);
            locationMapModel.setLongitude(responseJsonObj.has(LONGITUDE) ? responseJsonObj.getDouble(LONGITUDE) : 0.0);
            locationMapModel.setDestLatitude(responseJsonObj.has(DEST_LATITUDE) ? responseJsonObj.getDouble(DEST_LATITUDE) : 0.0);
            locationMapModel.setDestLongitude(responseJsonObj.has(DEST_LONGITUDE) ? responseJsonObj.getDouble(DEST_LONGITUDE) : 0.0);
            //for api information config
            locationMapModel.setApiName(Utility.getStringObjectValue(responseJsonObj, API_NAME));
            locationMapModel.setRecordID(Utility.getStringObjectValue(responseJsonObj, RECORD_ID));
            locationMapModel.setLocationTitle(Utility.getStringObjectValue(responseJsonObj, LOCATION_TITLE));
            locationMapModel.setLocationAddress(Utility.getStringObjectValue(responseJsonObj, LOCATION_ADDRESS));
            locationMapModel.setMapPinIcon(Utility.getStringObjectValue(responseJsonObj, MARKER_IMAGE_URL));
            //for overlay
            locationMapModel.setOverlayPage(Utility.getStringObjectValue(responseJsonObj, OVERLAY_PAGE));
            locationMapModel.setIsShowOverlay(Utility.getJsonObjectIntValue(responseJsonObj, IS_SHOW_OVERLAY));
            locationMapModel.setOverlaySize(Utility.getJsonObjectIntValue(responseJsonObj, OVERLAY_SIZE));
            locationMapModel.setIsMarkerClickShowOverlay(Utility.getJsonObjectIntValue(responseJsonObj, IS_MARKER_CLICK_SHOW_OVERLAY));
            locationMapModel.setIsSearchAutoComplete(Utility.getJsonObjectIntValue(responseJsonObj, IS_SEARCH_AUTO_COMPLETE));
            locationMapModel.setisAdminLogin(Utility.getJsonObjectIntValue(responseJsonObj, IS_ADMIN_LOGIN));
            locationMapModel.setIsTracking(Utility.getJsonObjectIntValue(responseJsonObj, IS_TRACKING));
            locationMapModel.setIsPlotLocation(Utility.getJsonObjectIntValue(responseJsonObj, IS_PLOT_LOCATION));
            //navigation route draw from current location
            locationMapModel.setIsNavFromCurLoc(Utility.getJsonObjectIntValue(responseJsonObj, IS_NAV_FROM_CUR_LOC));
            locationMapModel.setQueryString(Utility.getStringObjectValue(responseJsonObj, QUERY_STRING));
            //unused params in MapAppActivity
            locationMapModel.setmDestLatitude(Utility.getJsonObjectDoubleValue(responseJsonObj, M_DEST_LATITUDE));
            locationMapModel.setmDestLongitude(Utility.getJsonObjectDoubleValue(responseJsonObj, M_DEST_LONGITUDE));
            locationMapModel.setOpenMapApp(Utility.getJsonObjectBooleanValue(responseJsonObj, OPEN_MAP_APP));
            locationMapModel.setIsShowDirection(Utility.getJsonObjectBooleanValue(responseJsonObj, IS_SHOW_DIRECTIONS) ? 1 : 0);
            locationMapModel.setIsPlotAddressLocation(Utility.getJsonObjectBooleanValue(responseJsonObj, IS_PLOT_ADDRESS_LOCATION));
            locationMapModel.setAddressString(Utility.getStringObjectValue(responseJsonObj, ADDRESS_STRING));
            //For Search View hide or show
            locationMapModel.setShowSearchbar(Utility.getJsonObjectBooleanValue(responseJsonObj, SHOW_SEARCHBAR));
            locationMapModel.setSearchByCountriesList(Utility.getStringObjectValue(responseJsonObj, SEARCH_BY_COUNTRIES_LIST));
            locationMapModel.setResponseData(responseJsonObj.toString());
            locationMapModel.setShowSearchbar(Utility.getJsonObjectBooleanValue(responseJsonObj, MARKER_DATA));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return locationMapModel;
    }

}
