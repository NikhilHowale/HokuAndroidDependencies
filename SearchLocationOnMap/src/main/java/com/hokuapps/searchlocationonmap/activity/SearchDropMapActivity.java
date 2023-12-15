package com.hokuapps.searchlocationonmap.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hokuapps.getcurrentlocationdetails.HokuLocationProvider;
import com.hokuapps.searchlocationonmap.R;
import com.hokuapps.searchlocationonmap.adapter.GooglePlacesAutocompleteAdapter;
import com.hokuapps.searchlocationonmap.model.LocationMapModel;
import com.hokuapps.searchlocationonmap.model.PlaceModel;
import com.hokuapps.searchlocationonmap.synchronizer.MapAsyncTask;
import com.hokuapps.searchlocationonmap.utils.AppConstant;
import com.hokuapps.searchlocationonmap.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchDropMapActivity extends AppCompatActivity implements OnMapReadyCallback, HokuLocationProvider.NewLocationCallback, View.OnClickListener {

    public static final int RESULT_SEARCH_DROP_ACTIVITY = 2500;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected static final int ZOOM_LEVEL = 20;

    private MapView mMap;
    private GoogleMap mGoogleMap;
    private GooglePlacesAutocompleteAdapter mGooglePlacesAutocompleteAdapter;
    private Marker mLastSelectedMarker = null;

    private HokuLocationProvider mLocationProvider;
    private Location mCurrentLocation;

    private PlaceModel mPlaceModel;
    private LocationMapModel mLocationMapModel;
    private JSONObject mResponseJson = null;

    private String clickAddress;
    private double clickLatitude;
    private double clickLongitude;

    public AutoCompleteTextView searchAddressAutoCompleteText;
    private AppCompatImageView mBackToMain;
    private View showDropAddress;


    /**
     * This method invoke by clicking on map
     */
    private final GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(@NonNull LatLng latLng) {
            addOrMoveSelectedLocationMarker(latLng);
        }
    };

    /**
     * This method is called by selecting item showing in GooglePlacesAutocompleteAdapter
     */
    AdapterView.OnItemClickListener autoCompleteListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
            mPlaceModel = (PlaceModel) parent.getAdapter().getItem(position);

            MapAsyncTask mapAsyncTask = new MapAsyncTask();
            mapAsyncTask.setOnMapAsyncListener(new MapAsyncTask.OnMapAsyncListener() {
                @Override
                public void onMapAsyncTaskResult(JSONObject object) {
                    try {
                        if (object != null) {
                            // Hide keyboard here.
                            Utility.hideSoftKeyboard(SearchDropMapActivity.this, getWindow().getDecorView().getWindowToken());

                            JSONObject resultObject = object.getJSONObject(AppConstant.googlePlace.PLACE_RESPONSE_RESULT);
                            JSONObject locationObject = resultObject.getJSONObject(AppConstant.googlePlace.PLACE_GEOMETRY).getJSONObject(AppConstant.googlePlace.PLACE_LOCATION);

                            LatLng originLatLng = new LatLng(locationObject.getDouble(AppConstant.googlePlace.PLACE_LAT), locationObject.getDouble(AppConstant.googlePlace.PLACE_LNG));
                            addOrMoveSelectedLocationMarker(originLatLng);
                            moveToCurrentLocation(originLatLng);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMapAsyncTaskBefore() {

                }
            });

            mapAsyncTask.execute(getLatLongFromPlaceIdUrl(mPlaceModel.getPlaceId()));

        }
    };


    /**
     * This method open SearchDropMapActivity
     * @param activity Previous activity reference
     * @param mapModel LocationMapModel object
     */
    public static void startActivityForResult(Activity activity, LocationMapModel mapModel) {
        Intent mapIntent = new Intent(activity, SearchDropMapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConstant.IntentParam.MAP_LOCATION_MODEL, mapModel);
        mapIntent.putExtras(bundle);
        activity.startActivityForResult(mapIntent, RESULT_SEARCH_DROP_ACTIVITY);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_drop);
        mLocationProvider = new HokuLocationProvider(this, this);

        loadBundleData();
        initView();
        initializerMapsAsync();
        initAutoCompleteAdapter();
        setAllListener();
        mMap.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        mMap.onResume();
        super.onResume();
    }

    private void loadBundleData() {
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mLocationMapModel = bundle.containsKey(AppConstant.IntentParam.MAP_LOCATION_MODEL)
                        ? (LocationMapModel) bundle.getParcelable(AppConstant.IntentParam.MAP_LOCATION_MODEL)
                        : new LocationMapModel();
                if (mLocationMapModel == null) {
                    mLocationMapModel = new LocationMapModel();
                }

                mResponseJson = new JSONObject(mLocationMapModel.getResponseData());

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initView(){
        mMap = findViewById(R.id.mapSearchDrop);
        searchAddressAutoCompleteText = findViewById(R.id.auto_search_complete);
        showDropAddress = findViewById(R.id.showDropAddress);

        mBackToMain = findViewById(R.id.backToMain);

        mBackToMain.setOnClickListener(this);
    }

    // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
    private void initializerMapsAsync() {
        mMap.getMapAsync(this);
    }

    /**
     * This method initialize auto complete adapter and set to AutoCompleteTextView
     */
    private void initAutoCompleteAdapter() {
        mGooglePlacesAutocompleteAdapter = new GooglePlacesAutocompleteAdapter(this, android.R.layout.simple_list_item_1, null, 1);
        searchAddressAutoCompleteText.setAdapter(mGooglePlacesAutocompleteAdapter);

    }

    /**
     * This method initialize map settings
     */
    @SuppressLint("MissingPermission")
    private void initMapSettings() {
        mGoogleMap.setMyLocationEnabled(true);

        // Map UI level Settings
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setCompassEnabled(false);
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL/*mLocationMapModel.getMapType()*/);
        mGoogleMap.setOnMapClickListener(onMapClickListener);
    }

    private void setAllListener(){
        searchAddressAutoCompleteText.setOnItemClickListener(autoCompleteListener);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        try {
            mGoogleMap = googleMap;
            setCustomMapStyleFromResource();
            initMapSettings();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (mLocationProvider != null) {
                        mLocationProvider.connect();
                    }
                    break;
                case Activity.RESULT_CANCELED:
                    if (!Utility.isGPSInfo(this)) {
                        Toast.makeText(this, getString(R.string.gps_error), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        gpsSettingsRequest();
        if (Utility.isGooglePlayServicesAvailable(this)) {
            mLocationProvider.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationProvider != null) {
            mLocationProvider.disconnect();
            mLocationProvider.stopLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMap.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMap.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMap.onLowMemory();
    }


    /**
     * Add or move current location marker
     *
     * @param latLng lat/ Long value
     */
    private void addOrMoveSelectedLocationMarker(LatLng latLng) {
        clickLatitude = latLng.latitude;
        clickLongitude = latLng.longitude;

        if (clickLatitude == 0.0 || clickLongitude == 0.0) return;

        mLocationProvider.getCompleteAddressString(clickLatitude, clickLongitude, new HokuLocationProvider.OnCapturedLocationString() {
            @Override
            public void onCapturedAddress(String addressString) {
                clickAddress = addressString;
                showDropBottomDialog(clickAddress);

            }
        });

        if (mLastSelectedMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.flat(true);
            markerOptions.anchor(0.5f, 0.5f);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_location));
            mLastSelectedMarker = mGoogleMap.addMarker(markerOptions);
        } else {
            mLastSelectedMarker.setPosition(latLng);
        }

    }

    /**
     * This method display address with lat/ long value into bottom view
     * @param address address string
     */
    private void showDropBottomDialog(String address){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {


                    showDropAddress.setVisibility(View.VISIBLE);

                    AppCompatTextView textAddressView = findViewById(R.id.textAddress);
                    textAddressView.setText(address);

                    AppCompatTextView textLat = findViewById(R.id.textLat);
                    textLat.setText("Latitude : "+ clickLatitude);

                    AppCompatTextView textLong = findViewById(R.id.textLong);
                    textLong.setText("Longitude : "+ clickLongitude);

                    AppCompatButton confirmButton = findViewById(R.id.btnConfirm);
                    AppCompatImageView closeDialog = findViewById(R.id.drop_close);

                    confirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setResultToCallingActivity(Activity.RESULT_OK, getMapSelectLocationResult());
                        }
                    });

                    closeDialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showDropAddress.setVisibility(View.GONE);
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * This method check GPS is enable then we can request for location
     * otherwise show dialog to enable GPS
     */
    public void gpsSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationProvider.mLocationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mLocationProvider.getmGoogleApiClient(), builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult((Activity) SearchDropMapActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Toast.makeText(SearchDropMapActivity.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    /**
     * Customise the styling of the base map using a JSON object defined in a raw resource file.
     */
    public void setCustomMapStyleFromResource() {
        try {
            boolean success = mGoogleMap.setMapStyle( MapStyleOptions.loadRawResourceStyle(this, R.raw.google_map_style_map));

            if (!success) {
                Log.e("onMapReady", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("onMapReady", "Can't find style. Error: ", e);
        }
    }

    /**
     * This method build place id url to retrieve Place information
     * @param placeId id of place
     * @return return build url
     */
    public String getLatLongFromPlaceIdUrl(String placeId) {
        return AppConstant.google_LatLongFromPlaceId.BASE_PATH_lATLONGFROMPLACEID + placeId +
                "&key=" + Utility.getMapApiKey(this);
    }

    /**
     * Animate to specific location without zoom
     *
     * @param latLng lat/ Long value
     */
    private void moveToCurrentLocation(LatLng latLng) {
        if (latLng != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }


    /**
     * This method return result to previous activity
     * @param resultCode resultCode
     * @param jsonObject data in JSONObject
     */
    private void setResultToCallingActivity(int resultCode, JSONObject jsonObject) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConstant.IntentParam.MAP_LOCATION_MODEL, mLocationMapModel);
        bundle.putString(AppConstant.IntentParam.EXTRA_MAP_RESULT_CALLBACK, jsonObject.toString());

        if(resultCode == Activity.RESULT_CANCELED){
            bundle.putInt(AppConstant.IntentParam.IS_RESULT_CANCEL, 0);
        }

        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * This method build JSONObject with address, lat and long value
     * @return JSONObject for drop location
     */
    private JSONObject getMapSelectLocationResult() {

        JSONObject jsonObjResponse = new JSONObject();
        try {
            jsonObjResponse.put(AppConstant.JSONParameter.ADDRESS, clickAddress);
            jsonObjResponse.put(AppConstant.JSONParameter.LATITUDE, clickLatitude);
            jsonObjResponse.put(AppConstant.JSONParameter.LONGITUDE, clickLongitude);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObjResponse;
    }

    /**
     * This method move camera to provided lat/Long with provided zoom level
     * @param latLng lat/long value
     */
    private void loadLatLongOnMap(LatLng latLng) {
        if (latLng != null) {

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mLocationMapModel.getMapZoomLevel() <= 0
                    ? ZOOM_LEVEL : mLocationMapModel.getMapZoomLevel());
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void handleNewLocation(Location location) {
        try {

            if (!Utility.isActivityLive(SearchDropMapActivity.this)) return;

            if (location != null) {
                mCurrentLocation = location;
                loadLatLongOnMap(new LatLng(location.getLatitude(), location.getLongitude()));

                mLocationProvider.getCompleteAddressString(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), addressString -> {
                    clickAddress = addressString;
                    clickLatitude = mCurrentLocation.getLatitude();
                    clickLongitude = mCurrentLocation.getLongitude();
                    showDropBottomDialog(addressString);

                });
                if (mLocationProvider != null) {
                    mLocationProvider.stopLocationUpdates();
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleLastLocation(Location location) {
        if (location != null) {
            handleNewLocation(location);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backToMain) {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        setResultToCallingActivity(Activity.RESULT_CANCELED, mResponseJson);
        finish();

    }
}
