package com.hokuapps.loadnativefileupload.activity;

import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_RESULT_CANCEL;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hokuapps.loadnativefileupload.R;
import com.hokuapps.loadnativefileupload.models.LocationMapModel;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;
import com.hokuapps.loadnativefileupload.utilities.MapWrapperLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by user on 13/12/16.
 */
public class LocationServiceActivity extends AppCompatActivity implements OnMapReadyCallback , MapWrapperLayout.OnDragListener {

    public static final String TAG = LocationServiceActivity.class.getSimpleName();
    public static final int LOCATION_SERVICE_REQUEST_CODE = 8000;

    private GoogleMap mMap;

    private LocationMapModel locationMapModel;
    private ImageView mMarkerImageView;
    private Marker mOldMarker = null;
    private MenuItem menuItem = null;
    private MapView googleMapView;
    private final GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            locationMapModel.setLatitude(latLng.latitude);
            locationMapModel.setLongitude(latLng.longitude);
            addOrMoveMarker(latLng);
        }
    };


    private final GoogleMap.OnMapLoadedCallback onMapLoadedCallback = new GoogleMap.OnMapLoadedCallback() {
        @Override
        public void onMapLoaded() {
            if (menuItem != null) {
                menuItem.setEnabled(true);
            }
        }
    };
    private final GoogleMap.SnapshotReadyCallback snapshotReadyCallback = new GoogleMap.SnapshotReadyCallback() {
        @Override
        public void onSnapshotReady(Bitmap snapshot) {
            // Callback is called from the main thread, so we can modify the ImageView safely.
            if (locationMapModel != null) {
                setResultActivityCallback(snapshot);
            }
        }
    };


    /**
     * This method open LocationServiceActivity with extras
     * @param context context
     * @param mapModel LocationModel
     */
    public static void startActivityForResult(Activity context, LocationMapModel mapModel) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("mapData", mapModel);
        Intent intent = new Intent(context, LocationServiceActivity.class);
        intent.putExtras(bundle);
        context.startActivityForResult(intent, LOCATION_SERVICE_REQUEST_CODE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_service_activity);
        loadBundleData();
        setupToolbar();

        setUpMapIfNeeded(savedInstanceState);

        initView();


    }

    private void initView() {
        mMarkerImageView = findViewById(R.id.marker_icon_view);
    }

    @Override
    public void onMove(MotionEvent motionEvent) {
    }

    @Override
    public void onDrag(MotionEvent motionEvent) {
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    }

    @Override
    protected void onStart() {
        googleMapView.onStart();
        super.onStart();

    }

    @Override
    protected void onPause() {
        googleMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        googleMapView.onResume();
        super.onResume();
        mMarkerImageView.setVisibility(View.GONE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (locationMapModel != null && !TextUtils.isEmpty(locationMapModel.getNextButtonTitle())) {
            menuItem = menu.add(0, R.id.menu_next_button, Menu.NONE, locationMapModel != null ? locationMapModel.getNextButtonTitle() : "");
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            menuItem.setEnabled(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.menu_next_button) {
            if (locationMapModel != null) {

                locationMapModel.setLatitude(locationMapModel.getLatitude());
                locationMapModel.setLongitude(locationMapModel.getLongitude());

                takeMapSnapshot();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResultToCallingActivity(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    /**
     * This method take snapshot of map
     */
    private void takeMapSnapshot() {
        if (mMap != null) {
            if (locationMapModel.isGetMapSnapShot()) {
                mMap.snapshot(snapshotReadyCallback);
            } else {
                setResultToCallingActivity(Activity.RESULT_OK);
                LocationServiceActivity.this.finish();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        try {
            mMap = googleMap;
            // if (mapFragment != null) {
            setMapUiSettings(mMap);

            LatLng latLng = getLocationFromAddress(this, locationMapModel.getAddressString());

            loadLatLongOnMap(latLng);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        googleMapView.onLowMemory();
    }


    private void setUpMapIfNeeded(Bundle savedInstanceState) {

        googleMapView = findViewById(R.id.googleMapView);
        googleMapView.onCreate(savedInstanceState);
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMapView.getMapAsync(this);


    }


    private void loadLatLongOnMap(LatLng latLng) {
        if (latLng != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            mMarkerImageView.setVisibility(View.GONE);
            mMap.animateCamera(cameraUpdate);
        }
    }


    /**
     * This method add marker or move marker location on map click
     */
    private void addOrMoveMarker(LatLng latLng) {
        if (mOldMarker == null) {
            mOldMarker = mMap.addMarker(new MarkerOptions().position(latLng)
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_location)));

        } else {
            mOldMarker.setPosition(latLng);
        }

        locationMapModel.setLatitude(latLng.latitude);
        locationMapModel.setLongitude(latLng.longitude);

    }

    /**
     * This method return lat and long value from address string
     * @param context context
     * @param strAddress address string
     * @return return lat long value
     */
    public LatLng getLocationFromAddress(Context context, String strAddress) {
        showOrHideProgressView(true);
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng latLng = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.isEmpty()) {
                return null/*new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)*/;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            latLng = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (Exception ex) {

            ex.printStackTrace();
        } finally {
            showOrHideProgressView(false);
        }

        return latLng/*latLng == null ? new LatLng(0.0, 0.0) : latLng*/;
    }


    /**
     * Load bundle data
     */
    private void loadBundleData() {
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            locationMapModel = bundle.containsKey("mapData") ? (LocationMapModel) bundle.getParcelable("mapData") : new LocationMapModel();

            if (locationMapModel == null) {
                locationMapModel = new LocationMapModel();
            }
        }

    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);


        ((TextView) toolbar.findViewById(R.id.toolbar_title)).setText(locationMapModel != null
                ? locationMapModel.getPageTitle()
                : "");

        setToolbarBackground(toolbar);

    }

    /**
     * This method set toolbar background
     */
    private void setToolbarBackground(Toolbar toolbar) {
        try {
            if (locationMapModel != null && !TextUtils.isEmpty(locationMapModel.getColorCode())) {
                toolbar.setBackgroundColor(Color.parseColor(locationMapModel.getColorCode()));
                setStatusBarColor(FileUploadUtility.changeColorToPrimaryHSB(locationMapModel.getColorCode()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * set up all map UI settings
     */
    private void setMapUiSettings(GoogleMap googleMap) {
        if (googleMap != null) {
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(false);
            googleMap.setMapType(locationMapModel.getMapType());

            googleMap.setOnMapLoadedCallback(onMapLoadedCallback);

            if (!locationMapModel.getIsReadOnly()) {
                mMap.setOnMapClickListener(onMapClickListener);
            }
        }
    }

    /**
     * This method save bitmap to local storage
     * @param bitmap bitmap
     * @param file file to write bitmap
     */
    private void saveBitmapImage(Bitmap bitmap, File file) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void setResultActivityCallback(Bitmap bitmap) {
        try {
            String fileName = "map_" + locationMapModel.getAppID() + "_" + System.currentTimeMillis() + ".png";
            File file = new File(FileUploadUtility.getHtmlDirFromSandbox(this) + File.separator + fileName);
            locationMapModel.setMapFileName(fileName);
            saveBitmapImage(bitmap, file);
            setResultToCallingActivity(Activity.RESULT_OK);
            finish();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * This method return result to previous call activity
     */
    private void setResultToCallingActivity(int resultCode) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("mapLocationModel", locationMapModel);

        if(resultCode == RESULT_CANCELED){
            bundle.putInt(IS_RESULT_CANCEL, 0);
        }

        intent.putExtras(bundle);
        setResult(resultCode, intent);
    }

    private void showOrHideProgressView(boolean shown) {
        findViewById(R.id.progress_view).setVisibility(shown ? View.VISIBLE : View.GONE);
    }


    /**
     * Set color to status bar of screen
     */
    public void setStatusBarColor(int color) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        TypedValue outValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimaryDark, outValue, true);
        getWindow().setStatusBarColor(color == 0 ? outValue.data : color);
    }

}