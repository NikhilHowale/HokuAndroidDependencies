package com.hokuapps.loadmapviewbyconfig;


import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.*;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.*;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.TeacherPlace.PLACE_ICON;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.googlePlace.DESTINATION;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.googlePlace.PLACE_LAT;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.googlePlace.PLACE_LNG;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.googlePlace.SOURCE;
import static com.hokuapps.loadmapviewbyconfig.utility.ThemeUtils.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.hokuapps.getCurrentLatLong.LocationDetails;
import com.hokuapps.loadmapviewbyconfig.adapter.GooglePlacesAutocompleteAdapter;
import com.hokuapps.loadmapviewbyconfig.adapter.SearchAutoCompleteAdapter;
import com.hokuapps.loadmapviewbyconfig.constant.MapConstant;
import com.hokuapps.loadmapviewbyconfig.delegate.IWebSocketClientEvent;
import com.hokuapps.loadmapviewbyconfig.locationProvider.LocationProvider;
import com.hokuapps.loadmapviewbyconfig.models.Error;
import com.hokuapps.loadmapviewbyconfig.models.JSResponseData;
import com.hokuapps.loadmapviewbyconfig.models.LocationMapModel;
import com.hokuapps.loadmapviewbyconfig.models.PlaceModel;

import com.hokuapps.loadmapviewbyconfig.synchronizer.GetNearByPlacesClientEvent;
import com.hokuapps.loadmapviewbyconfig.synchronizer.MapAsyncTask;
import com.hokuapps.loadmapviewbyconfig.synchronizer.UpdateCurrentLocationClientEvent;
import com.hokuapps.loadmapviewbyconfig.synchronizer.WebSocketClientEvent;
import com.hokuapps.loadmapviewbyconfig.utility.KeyboardUtils;
import com.hokuapps.loadmapviewbyconfig.utility.ThemeUtils;
import com.hokuapps.loadmapviewbyconfig.utility.ToolbarColorizeHelper;
import com.hokuapps.loadmapviewbyconfig.utility.Utility;
import com.hokuapps.loadmapviewbyconfig.widgets.behavior.AnchorBottomSheetBehavior;
import com.koushikdutta.ion.Ion;


import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;




@SuppressWarnings("ALL")
public class MapsAppCompactActivity extends AppCompatActivity implements OnMapReadyCallback, LocationProvider.LocationCallback, View.OnClickListener {

    public static final String TAG = MapsAppCompactActivity.class.getSimpleName();
    public static final int RESULT_CODE_MAP_GET_ADDRESS_ACTION_ACTIVITY = 2400;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected static final int ZOOM_LEVEL = 20;
    private static final int overview = 0;
    public TabLayout tabLayout;
    public ImageButton searchTeacherButton;
    public Button buttonBottom;
    public LinearLayout bottomButtonLayout;
    public AutoCompleteTextView simpleSearchEdittext;
    public ImageButton btnCloseOverlay;
    public Toolbar mToolbar;
    public ImageButton btnCollapse;
    public JSONObject mResultNearByLocation;
    public AutoCompleteTextView searchAddressAutoCompleteText;
    public AutoCompleteTextView searchAddressAutoCompleteTextSource, searchAddressAutoCompleteTextDestination;
    public AutoCompleteTextView auto_complete_text_my_location;
    private String updateLocationApi = "";
    private int timerInterval = 5;
    private String status = "";
    private boolean isOnline = false;
    private boolean isStartSendUpdate = true;
    private int pointNumber = 0;
    private JSONObject jsonObjectMapPoints = null;
    private ArrayList<LatLng> arrayListPoints = new ArrayList<>();
    private GoogleMap mGoogleMap;
    private MapView mMap;
    ProgressDialog progressDialog = null;
    private LocationMapModel mLocationMapModel;
    private Context context;
    private boolean mIsCurrentFirst = true;
    private ImageView imageViewMapFloatingPin;
    private LinearLayout simpleSearchLayout;
    private ImageView imageViewClearSearchText;
    private CardView cardViewMapSourceAndDestination;
    private WebView mWebviewConfirm;
    private Location mCurrentLocation;
    private Location mPreviousLocation;
    private String clickAddress;
    private double clickLatitude;
    private double clickLongitude;
    private GooglePlacesAutocompleteAdapter mGooglePlacesAutocompleteAdapter;
    private PlaceModel mPlaceModel;
    private Marker mLastSelectedMarker = null;
    private Marker mCurrentLocationMarker = null;
    private Bitmap mCarBitmapIcon = null;
    private Bitmap mMapPinIconFromUrl = null;
    private JSONObject mResponseJson = null;
    private JSResponseData jsResponseData;
    private String offlineID;
    private int instucationNumberClockIn;
    private Uri mImageCaptureUri = null;
    private String caption = null;
    private String currentLatLongCallback = null;
    private JSONObject jsonObjAddress;
    private CountDownTimer mLiveTrackerApiCallTimer;
    private LatLng mapCenterLatLng = null;
    private boolean showOnlyMyLocationCard;
    private boolean isMapOverlayHideOnSwipe;
    private TextView textViewMyLocation;
    private TextView textViewCarShopLocation;
    private TextView textViewSourceTitle;
    private TextView textViewDestinationTitle;
    public static JSONObject configJson = new JSONObject();
    private boolean showOverlayCloseButton = false;
    private int isShowFloatingPin = 0;
    private Location location;
    private String destinationAddress;
    private boolean showBottomButtonLeft = false;
    private JSONObject currenjsonObj;
    private JSONObject locatinDictJsonObject;
    private AnchorBottomSheetBehavior anchorBottomSheetBehavior;
    private CoordinatorLayout.LayoutParams layoutParams;
    private FrameLayout frameLayoutBottomLayout;
    private boolean isLoadedForFirstTime = true;
    private String shareButtonCallback = "";
    private RelativeLayout relativeLayoutSearchAnything;
    private AutoCompleteTextView editTextSearchAnything;
    private String searchAnythingCallBack = "";
    private String selectedAppNameForShare = "";
    private FrameLayout frameLayoutOverlayBar;
    private ImageView imageViewOverlayHandle;
    private Menu menu;
    private int previousState = AnchorBottomSheetBehavior.STATE_COLLAPSED;
    private JSONObject otherRequestData;
    private boolean shouldLoadMapViewWithSettings = false;
    private String missingKeys = "Missing keys = ";
    private String[] requiredJSONObjectKey = {};
    private boolean whileDebuggingShowMissingAlert = false;


    /**
     * Interface definition for a callback to be invoked when an item in this AdapterView has been clicked.
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
                            JSONObject resultObject = object.getJSONObject(googlePlace.PLACE_RESPONSE_RESULT);
                            JSONObject locationObject = resultObject.getJSONObject(googlePlace.PLACE_GEOMETRY).getJSONObject(googlePlace.PLACE_LOCATION);

                            LatLng originLatLng = new LatLng(locationObject.getDouble(PLACE_LAT), locationObject.getDouble(googlePlace.PLACE_LNG));
                            clickLatitude = originLatLng.latitude;
                            clickLongitude = originLatLng.longitude;

                            addOrMoveSelectedLocationMarker(originLatLng);
                            moveToCurrentLocation(originLatLng);
                            if (mLocationMapModel.getIsRequestGaurd() == 1) {
                                //call api with this lat/long to get nearest gaurd locations
                                getNearByPlacesFromCustomApi(clickLatitude, clickLongitude);
                            }
                        }
                        Utility.hideSoftKeyboard(MapsAppCompactActivity.this, getWindow().getDecorView().getWindowToken());
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

    private String appAuthTokenNew = "";
    private String appSecretKeyNew = "";
    private ImageView imageViewGlobe;
    private int isShowWase = 0;
    private boolean isSourceClicked = true;
    private boolean sourceClicked = false;
    private boolean destinationClicked = false;
    private LatLng sourceLatLong;
    private LatLng destinationLatLong;
    private JSONObject jsonObjectSourceData;
    private JSONObject jsonObjectDestinationData;
    private int globalPeekHeight = 0;
    private Bitmap mSourceIconBitmap = null;
    private Bitmap mDestIconBitmap = null;
    private LocationProvider mLocationProvider;


    /**
     * Callback interface for when the My Location button is clicked.
     */
    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            try {
                gpsSettingsRequest();
                if (isGooglePlayServicesAvailable()) {
                    if (mLocationProvider.getGoogleApiClient().isConnected()) {
                        mLocationProvider.connect();
                    }
                }

                if (mLocationMapModel.getIsRequestGaurd() == 1) {
                    if (mCurrentLocation != null) {
                        getNearByPlacesFromCustomApi(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                        mLocationProvider.getCompleteAddressString(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), new LocationProvider.OnCapturedLocationString() {
                            @Override
                            public void onCapturedAddress(String addressString) {
                                searchAddressAutoCompleteText.setText(addressString);

                                setToolbarTitle(addressString);

                            }
                        });

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return false;
        }
    };


    /**
     * Callback interface for when the user taps on the map.
     * Listeners will be invoked on the Android UI thread
     */
    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
//            Hide search box here.
            setSearchLayoutVisibility(false);

            if (isShowFloatingPin == 1) return;

            if (mLocationMapModel.getIsSelectLocation() == 1 || showOnlyMyLocationCard) {

                setAddressToAutoCompleteEditTextView(latLng);
            }
        }
    };


    private DrawerLayout mDrawerLayout = null;
    private ActionBarDrawerToggle mDrawerToggle;
    private WebView mWebViewNavDrawer;
    private CountDownTimer mSendCurLatLongInTimeInterval;


    /**
     * start map activity
     * @param activity
     * @param mapModel Location details model object
     */
    public static void startActivityForResult(Activity activity, LocationMapModel mapModel) {
        Intent mapIntent = new Intent(activity, MapsAppCompactActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MAP_LOCATION_MODEL, mapModel);
        mapIntent.putExtras(bundle);
        activity.startActivityForResult(mapIntent, RESULT_CODE_MAP_GET_ADDRESS_ACTION_ACTIVITY);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_compact_activity);
//        Apply settings for mobo here.
        context = this;

        mLocationProvider = new LocationProvider(context, (LocationProvider.LocationCallback) this);

        loadBundleData();

        initView();
        initializerMapsAsync();
        mMap.onCreate(savedInstanceState);

        asyncImageIcon();

        setupToolbar();
        initTabLayout();
        setAllListener();
        setWebView();

        loadUrlOvelayPage(mLocationMapModel.getOverlayPage());

        initAutoCompleteAdapter();

        setupMenuDrawerLayout();
    }


    /**
     * get bitmap from image and set to imageview in background
     */
    private void asyncImageIcon() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                carPinBitmap();
                mapPinFromUrl();
                sourceDestMarkerIconFromUrl();
                return null;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        gpsSettingsRequest();
        if (isGooglePlayServicesAvailable()) {
            mLocationProvider.connect();
        }
    }

    @Override
    protected void onResume() {
        mMap.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMap.onDestroy();

        if (mWebviewConfirm != null) {
            mWebviewConfirm.destroy();
        }
        cancelLiveTrackerCallApiTimer();
    }


    /**
     * Handle click events on  views (edittext,button,imageview)
     *
     * @param view
     */
    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.editText_search_anything) {
            try {
                JSONObject jsonObj = new JSONObject(mLocationMapModel.getResponseData());
                jsonObj.put(SEARCH_ANYTHING_CALL_BACK, true);
                jsonObj = getMapMovedNewLocation(jsonObj);
                setResultToCallingActivity(Activity.RESULT_OK, jsonObj);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        if (id == R.id.imageView_clear_search_text) {
            simpleSearchEdittext.setText("");
        }
        if (id == R.id.button_search_teacher) {
            //search click handling for teacher
            if (!TextUtils.isEmpty(simpleSearchEdittext.getText().toString().trim())) {
                parseNearByPlacesResponse(mResultNearByLocation, simpleSearchEdittext.getText().toString());
                Utility.hideSoftKeyboard(context, getWindow().getDecorView().getWindowToken());
            } else {
                Toast.makeText(context, context.getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
            }
        }
        if (id == R.id.button_bottom) {

        }
        if (id == R.id.bottom_button_layout) {
            try {
                if (mLocationMapModel.getIsSelectLocation() == 1) {
                    if (TextUtils.isEmpty(clickAddress)) {
                        clickAddress = getCompleteAddressString(clickLatitude, clickLongitude).toString();
                        setResultToCallingActivity(Activity.RESULT_OK, getMapSelectLocationResult());

                    } else {
                        clickAddress = getCompleteAddressString(clickLatitude, clickLongitude).toString();
                        setResultToCallingActivity(Activity.RESULT_OK, getMapSelectLocationResult());
                    }
                } else {
                    JSONObject jsonObject = new JSONObject(mLocationMapModel.getResponseData());
                    jsonObject.put(IS_REDIRECT_TO_PAGE, true);
                    setResultToCallingActivity(Activity.RESULT_OK, jsonObject);
                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
        if (id == R.id.btn_close_overlay) {
            showOrHideOverlay(false);
        }

    }


    /**
     * this function gets called when Map is initialized
     *
     * @param googleMap Map object
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mGoogleMap = googleMap;
            setCustomMapStyleFromResource();
            initMapSettings();

            plotLocationMarker();
            plotMarkerByAddress();

            //read only map show only address string on map
            int readOnly = Utility.getJsonObjectIntValue(mResponseJson, READ_ONLY);
            if (readOnly == 1) {
                mGoogleMap.setOnMapClickListener(null);
                return;
            }

            if (mLocationMapModel.getIsNavFromCurLoc() != 1) {
                showMyLocationAddress(mLocationMapModel.getLatitude(), mLocationMapModel.getLongitude());
                setupNavigation();
            }

            showDestinationAddress(mLocationMapModel.getDestLatitude(), mLocationMapModel.getDestLongitude());


            if (Utility.getJsonObjectBooleanValue(mResponseJson, IS_OTHER_NEARBY_LOCATION)) {
                getNearByPlacesFromCustomApi(Utility.getJsonObjectDoubleValue(mResponseJson, PLACE_LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG));
//                Move camera to provided lat, lng.
                loadLatLongOnMap(new LatLng(Utility.getJsonObjectDoubleValue(mResponseJson, PLACE_LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG)));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(Utility.getJsonObjectDoubleValue(mResponseJson, PLACE_LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG)));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                mGoogleMap.addMarker(markerOptions);
            }
//            Add new parameter for loading static map loadStaticMapFromPoints: true,
//            and draw polyline on the map with given points of array.
            if (Utility.getJsonObjectBooleanValue(mResponseJson, LOAD_STATIC_MAP_FROM_POINTS)) {
                drawStaticMap(mGoogleMap, jsonObjectMapPoints);
            }

//                Call places api to show markers on the map.
            if (Utility.getJsonObjectBooleanValue(mResponseJson, LOAD_PLACES_AND_ROUTE_BOTH)) {

                getNearByPlacesFromCustomApi(Utility.getJsonObjectDoubleValue(mResponseJson, LATITUDE), Utility.getJsonObjectDoubleValue(mResponseJson, LONGITUDE));

            }

            if (showBottomButtonLeft || isShowFloatingPin == 1) {
                mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        mapCenterLatLng = mGoogleMap.getCameraPosition().target;
                        double lat = mapCenterLatLng.latitude;
                        double lng = mapCenterLatLng.longitude;

                        setAddressToAutoCompleteEditTextView(mapCenterLatLng);
                        clickLatitude = lat;
                        clickLongitude = lng;
//                        Show address on location card:
                        if (Utility.getJsonObjectBooleanValue(mResponseJson, CAN_EDIT_MY_LOCATION_ON_CARD)) {
                            if (isSourceClicked) {
                                showMyLocationAddress(lat, lng);
                                return;
                            } else {
//                        Show Destination address:
                                showDestinationAddress(lat, lng);
                                return;
                            }
                        }

                        setAddressToAutoCompleteEditTextView(mapCenterLatLng);
                    }
                });
            }

            GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    try {
                        JSONObject jsonObject = new JSONObject(String.valueOf(marker.getTag()));

                        if (jsonObject == null) return null;

                        String driverName = Utility.getStringObjectValue(jsonObject, DRIVER_ID_NAME);
                        String iuNumber = Utility.getStringObjectValue(jsonObject, IU_NUMBER);

                        String markerText = "";
                        if (driverName != null) {
                            markerText = "Driver Name : " + driverName + "\n";
                        }

                        if (iuNumber != null) {
                            markerText += "IuNumber : " + iuNumber;
                        }

                        View view = getLayoutInflater().inflate(R.layout.layout_driver_details, null);
                        TextView messageData = view.findViewById(R.id.driverData);
                        messageData.setText(markerText);
                        return view;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            if (Utility.isIpLimomob()) {
                mGoogleMap.setInfoWindowAdapter(infoWindowAdapter);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Draw static map with source and destination with polyline
     *
     * @param googleMap Map object
     * @param jsonObject json object containing map points to draw
     */
    public void drawStaticMap(GoogleMap googleMap, JSONObject jsonObject) {
        try {
            JSONArray jsonArrayPoints = jsonObject.getJSONArray(POINTS);
            if (jsonArrayPoints != null && jsonArrayPoints.length() > 0) {

                final LatLng latLngSource = new LatLng(jsonArrayPoints.getJSONObject(0).getDouble(PLACE_LAT)
                        , jsonArrayPoints.getJSONObject(0).getDouble(PLACE_LNG));

                final LatLng latLngDestination = new LatLng(jsonArrayPoints.getJSONObject(jsonArrayPoints.length() - 1).getDouble(PLACE_LAT)
                        , jsonArrayPoints.getJSONObject(jsonArrayPoints.length() - 1).getDouble(PLACE_LNG));


                PolylineOptions options = new PolylineOptions().width(10).color(Color.parseColor("#0361AD")).geodesic(true);
                if (!TextUtils.isEmpty(Utility.getStringObjectValue(mResponseJson, MAP_ROUTE_COLOR))) {
                    options.color(Color.parseColor(Utility.getStringObjectValue(mResponseJson, MAP_ROUTE_COLOR)));
                }
                for (int index = 0; index < jsonArrayPoints.length(); index++) {
//                Add marker on first and last point.
                    LatLng point = new LatLng(jsonArrayPoints.getJSONObject(index).getDouble(PLACE_LAT)
                            , jsonArrayPoints.getJSONObject(index).getDouble(PLACE_LNG));
                    options.add(point);
                }
                Polyline line = mGoogleMap.addPolyline(options);
                addMarkerOnMap(latLngSource, "", SOURCE);
                addMarkerOnMap(latLngDestination, "", DESTINATION);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setZoomMapForRoute(latLngSource, latLngDestination);
                    }
                }, 2000);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Add marker on the location
     */
    private void plotLocationMarker() {
        if (mLocationMapModel.getIsPlotLocation() == 1) {
            addOrMoveSelectedLocationMarker(new LatLng(mLocationMapModel.getLatitude(), mLocationMapModel.getLongitude()));
            loadLatLongOnMap(new LatLng(mLocationMapModel.getLatitude(), mLocationMapModel.getLongitude()));
        }
    }


    /**
     * Get the location from address
     *
     * @param context Application context
     * @param strAddress Address string to get the location
     * @return returnd lattitude and longitude
     */
    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng latLng = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.isEmpty()) {
                return new LatLng(0, 0);
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            latLng = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return latLng == null ? new LatLng(0.0, 0.0) : latLng;
    }


    /**
     * Plot marker on the map using address
     */
    private void plotMarkerByAddress() {
        if (mLocationMapModel.getIsPlotAddressLocation()) {
            LatLng latLng = getLocationFromAddress(this, mLocationMapModel.getAddressString());
            if (latLng != null) {
                addOrMoveSelectedLocationMarker(latLng);
                loadLatLongOnMap(latLng);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMap.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMap.onLowMemory();
    }


    /**
     * Specify the options menu for an activity
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            menu.clear();

            JSONObject menuItemJson = (JSONObject) Utility.getJsonObjectValue(mResponseJson, RIGHT_MENU);

            if (menuItemJson != null) {

                int size = 24;
//                Filter icon.
                if (!TextUtils.isEmpty(Utility.getStringObjectValue(menuItemJson, FILTER_ICON))) {

                    SubMenu subMenufilterIcon = menu.addSubMenu(0, 3, Menu.NONE,
                            Utility.getStringObjectValue(menuItemJson, ""));
                    subMenufilterIcon.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    subMenufilterIcon.getItem().setIcon(Utility.getImageDrawableFromAssetsBySize(this,
                            Utility.getStringObjectValue(menuItemJson,
                                    FILTER_ICON), size, size));

//                Make filter icon gray
                    if (Utility.getJsonObjectBooleanValue(menuItemJson,
                            IS_GRAY_COLOR)) {
                        subMenufilterIcon.getItem().setIcon(Utility.getColorDrawable(
                                Utility.getImageDrawableFromAssetsBySize(this,
                                        Utility.getStringObjectValue(menuItemJson, FILTER_ICON), size, size), "#828282"));
                    }
                }

                if (!TextUtils.isEmpty(Utility.getStringObjectValue(menuItemJson, SEARCH_ICON))) {

                    SubMenu subMenusearchIcon = menu.addSubMenu(0, 2, Menu.NONE,
                            Utility.getStringObjectValue(menuItemJson, ""));
                    subMenusearchIcon.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    subMenusearchIcon.getItem().setIcon(Utility.getImageDrawableFromAssetsBySize(this,
                            Utility.getStringObjectValue(menuItemJson,
                                    SEARCH_ICON), size, size));
                }
                searchAddressAutoCompleteText.setVisibility(View.GONE);

//                New headerButtons goe's here.
                JSONObject headerButtonsItems = (JSONObject) Utility.getJsonObjectValue(mResponseJson, HEADER_BUTTONS);
                if (headerButtonsItems != null) {

                    //                Add map icon
                    if (Utility.getJsonObjectBooleanValue(mResponseJson, SHOW_FULL_OVERLAY_WITH_BUTTONS)) {

                        SubMenu subMenu = menu.addSubMenu(0, 4, Menu.NONE,
                                "");
                        subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                        subMenu.getItem().setIcon(Utility.getImageDrawableFromAssetsBySize(this,
                                Utility.getStringObjectValue(headerButtonsItems, PLACE_ICON), size, size));
                    }

                    if ((anchorBottomSheetBehavior != null && anchorBottomSheetBehavior.getState() == AnchorBottomSheetBehavior.STATE_EXPANDED)) {
                        showListIconOnToolbar(menu, false);
                        showMapIconOnToolbar(menu, true);
                    } else if ((anchorBottomSheetBehavior != null &&
                            anchorBottomSheetBehavior.getState() == AnchorBottomSheetBehavior.STATE_ANCHORED
                            || anchorBottomSheetBehavior.getState() == AnchorBottomSheetBehavior.STATE_COLLAPSED)) {
                        showListIconOnToolbar(menu, true);
                        showMapIconOnToolbar(menu, false);
                    }

                }

            }
            this.menu = menu;

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Show map icon on toolbar
     *
     * @param menu toolbar menu object
     * @param show Boolean value to show the menu options(True or false)
     */
    private void showMapIconOnToolbar(Menu menu, boolean show) {
        if (menu.findItem(4) != null)
            menu.findItem(4).setVisible(show);

    }


    /**
     * this function gets called when menu option gets clicked
     *
     * @param item menu item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (Utility.getJsonObjectBooleanValue(mResponseJson, IS_MENU_SHOW)) {
                    if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
                        return true;
                    } else {
                        onBackPressed();
                    }
                } else {
                    onBackPressed();
                }
                break;

            case 2:
                //search menu
                if (simpleSearchLayout.getVisibility() == View.GONE) {
//                    Focus and show key pad for search.
                    setSearchLayoutVisibility(true);
                } else {
                    setSearchLayoutVisibility(false);
                }
                break;

            case 1:
                //list menu
                if (Utility.getJsonObjectBooleanValue(mResponseJson, TOGGLE_OVERLAY_LIST)) {
                    setOverlayStateToCollapse(AnchorBottomSheetBehavior.STATE_EXPANDED);
                } else {
                    callListMenuClick();
                }
                break;

            case 3:
                //filter menu
                try {
                    JSONObject jsonObj = new JSONObject(mLocationMapModel.getResponseData());
                    jsonObj.put(IS_FILTER_MENU_CLICK, true);
                    jsonObj = getMapMovedNewLocation(jsonObj);
                    setResultToCallingActivity(Activity.RESULT_OK, jsonObj);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                break;
            case 4:
                setOverlayStateToCollapse(AnchorBottomSheetBehavior.STATE_COLLAPSED);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * @param jsonObj
     * @return returns json object containing latitude longitude and location details
     */
    private JSONObject getMapMovedNewLocation(JSONObject jsonObj) {
        JSONObject jsonObjectLocation = new JSONObject();
        try {
            if (mapCenterLatLng != null) {
                jsonObjectLocation.put(LAT, mapCenterLatLng.latitude);
                jsonObjectLocation.put(LONG, mapCenterLatLng.longitude);
                jsonObj.put(BRIDGE_DATA, jsonObjectLocation);
                jsonObj.put(LOCATION_DICT, locatinDictJsonObject);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObj;
    }


    /**
     *
     */
    private void callListMenuClick() {
        try {
            JSONObject jsonObj = new JSONObject(mLocationMapModel.getResponseData());
            jsonObj.put(IS_RIGHT_MENU_CLICK, true);
            jsonObj = getMapMovedNewLocation(jsonObj);
            setResultToCallingActivity(Activity.RESULT_OK, jsonObj);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * this function is called when back pressed
     */
    @Override
    public void onBackPressed() {
        if (anchorBottomSheetBehavior != null && (anchorBottomSheetBehavior.getState() == AnchorBottomSheetBehavior.STATE_ANCHORED ||
                anchorBottomSheetBehavior.getState() == AnchorBottomSheetBehavior.STATE_EXPANDED)) {
            setOverlayStateToCollapse(AnchorBottomSheetBehavior.STATE_COLLAPSED);
            if (mLocationMapModel.getIsSelectLocation() == 1) {
                setResultToCallingActivity(Activity.RESULT_OK, getMapSelectLocationResult());
            } else {
                setResultToCallingActivity(Activity.RESULT_CANCELED, mResponseJson);
                finish();
            }
            return;
        }
        if (isNavDrawerOpen()) {
            closeNavDrawer();
            return;
        }

        if (mLocationMapModel.getIsSelectLocation() == 1) {
            setResultToCallingActivity(Activity.RESULT_OK, getMapSelectLocationResult());
        } else {
            setResultToCallingActivity(Activity.RESULT_CANCELED, mResponseJson);
            finish();
        }
    }


    /**
     * this function receives/gets data from other activities
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {

            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (mLocationProvider != null) {
                            mLocationProvider.connect();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        if (!isGPSInfo()) {
                            Toast.makeText(context, R.string.gps_not_enabled, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                break;

            default:
                break;
        }

    }


    /**
     * Generate image name by app id
     *
     * @param extension String containing extension to set to image file
     * @return returns image string with given extension
     */
    private String generateImageFileNameByAppID(String extension) {
        try {
            if (!TextUtils.isEmpty(getJsResponseData().getAppID())) {
                return getJsResponseData().getAppID() + "_" + Utility.getCurrentDateTimeInMS() + extension;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "" + System.currentTimeMillis() + extension;
    }


    /**
     * Move file to new file
     *
     * @param file file to move to new file
     * @param newFile new file where old file has to be moved
     * @return returns true if file os moved or false if any error occurs
     * @throws IOException
     */
    private boolean moveFile(File file, File newFile) throws IOException {

        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        boolean isMoved = false;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            isMoved = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            isMoved = false;
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

        return isMoved;

    }


    /**
     * set file anme and offline id to the response data
     *
     * @param filename name of the file to set to response data
     * @param offlineID offline id to set to response data
     * @return returns JSONObject containing file name, offline data id and caption
     */
    private JSONObject setFileNameAndOfflineIDToResponseData(String filename, String offlineID) {
        try {
            JSONObject jsonObject = new JSONObject(getJsResponseData().getResponseData());
            jsonObject.put(FILE_NAME, filename);
            jsonObject.put(OFFLINE_DATA_ID, offlineID);
            jsonObject.putOpt(CAPTION, "");
            return jsonObject;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject();
    }


    /**
     * set response data to json object
     *
     * @return returns JSResponseData canrtaining resonse data
     */
    public JSResponseData getJsResponseData() {
        return jsResponseData != null ? jsResponseData : new JSResponseData();
    }


    /**
     * Get complete address by latitude and longitude
     *
     * @param latitude latitude to get complete address
     * @param longitude longitude to get complete address
     * @return returns string containing complete address
     */
    private StringBuilder getCompleteAddressString(double latitude, double longitude) {
        Geocoder gc = new Geocoder(context, Locale.ENGLISH);
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

                    jsonObjAddress = Utility.getAddressJson(fetchedAddress);

                    return strAddress;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return new StringBuilder();
    }


    /**
     * set the address details to json object
     *
     * @param fetchedAddress Fetched address object
     * @return returns JSONObject containing address details
     */
    private JSONObject getAddressJson(Address fetchedAddress) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(COUNTRY_NAME, fetchedAddress.getCountryName());
            jsonObj.put(COUNTRY_CODE, fetchedAddress.getCountryCode());
            jsonObj.put(LOCALITY, fetchedAddress.getLocality());
            jsonObj.put(SUB_LOCALITY, fetchedAddress.getSubLocality());
            jsonObj.put(SUB_ADMIN_AREA, fetchedAddress.getSubAdminArea());
            jsonObj.put(POSTAL_CODE, fetchedAddress.getPostalCode());
            jsonObj.put(ADMIN_AREA, fetchedAddress.getAdminArea());

            return jsonObj;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObj;
    }


    /**
     * Callback to javascript function
     * @param currentLocation Current location object
     */
    private void currentLocationCallback(final Location currentLocation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (currentLocation != null && !TextUtils.isEmpty(currentLatLongCallback)) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(LAT, String.valueOf(currentLocation.getLatitude()));
                        jsonObject.put(LONG, String.valueOf(currentLocation.getLongitude()));
                        jsonObject.put(DATA, currenjsonObj);
                        Utility.callJavaScriptFunction(MapsAppCompactActivity.this, mWebviewConfirm, currentLatLongCallback, jsonObject);
                        currentLatLongCallback = null;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }


    /**
     * @param location Location object
     */
    private void handlingNavigation(final Location location) {

        if (mLocationMapModel.getIsNavigation() == 1) {
            if (mIsCurrentFirst) {
                mIsCurrentFirst = false;
                clickLatitude = location.getLatitude();
                clickLongitude = location.getLongitude();
                if (mLocationMapModel.getIsNavFromCurLoc() == 1) {
                    mLocationMapModel.setLatitude(location.getLatitude());
                    mLocationMapModel.setLongitude(location.getLongitude());
                    setupNavigation();
                    return;
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mLocationMapModel.getIsNavigationOn() == 1) {

                        //recent map if it is out of map
                        reCenterOnMap(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));

                        if (mPreviousLocation == null) {
                            mPreviousLocation = mCurrentLocation;
                        }

                        addOrMoveCurrentLocationMarker(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                                new LatLng(mPreviousLocation.getLatitude(), mPreviousLocation.getLongitude())
                                );

                        animateMarker(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                                new LatLng(mPreviousLocation.getLatitude(), mPreviousLocation.getLongitude()), mCurrentLocationMarker);

                        mPreviousLocation = mCurrentLocation;
                    }
                }
            });
        }
    }


    /**
     * initialize the views(map, imageview,buttons) and set click listeners
     */
    private void initView() {
        mMap = (MapView) findViewById(R.id.map);
        imageViewMapFloatingPin = (ImageView) findViewById(R.id.imageView_map_floating_pin);
        btnCollapse = findViewById(R.id.btn_collapse);

        if (!TextUtils.isEmpty(Utility.getStringObjectValue(mResponseJson, IS_SHOW_FLOATING_PIN))) {
            try {
                isShowFloatingPin = Utility.getJsonObjectIntValue(mResponseJson, IS_SHOW_FLOATING_PIN);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            imageViewMapFloatingPin.setVisibility(isShowFloatingPin == 1 ? View.VISIBLE : View.GONE);
        }
        frameLayoutOverlayBar = (FrameLayout) findViewById(R.id.frameLayout_overlay_bar);
        imageViewOverlayHandle = (ImageView) findViewById(R.id.imageView_overlay_handle);
//        Configure color for overlay bar and overlay handle.
        if (!TextUtils.isEmpty(Utility.getStringObjectValue(mResponseJson, OVERLAY_BAR_COLOR))) {
            frameLayoutOverlayBar.setBackgroundColor(Color.parseColor(Utility.getStringObjectValue(mResponseJson, OVERLAY_BAR_COLOR)));
        }

        if (!TextUtils.isEmpty(Utility.getStringObjectValue(mResponseJson, OVERLAY_HANDLER_COLOR))) {
            imageViewOverlayHandle.setBackgroundColor(Color.parseColor(Utility.getStringObjectValue(mResponseJson, OVERLAY_HANDLER_COLOR)));
        }

//        Globe icon on toolbar.
        imageViewGlobe = (ImageView) findViewById(R.id.imageView_globe);

        imageViewGlobe.setVisibility(View.GONE);

        btnCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (anchorBottomSheetBehavior.getState() == AnchorBottomSheetBehavior.STATE_EXPANDED) {
                    anchorBottomSheetBehavior.setState(AnchorBottomSheetBehavior.STATE_COLLAPSED);
                    btnCollapse.setVisibility(View.GONE);
                }
            }
        });

//        Hide show overlay bar.
        if (!TextUtils.isEmpty(Utility.getStringObjectValue(mResponseJson, SHOW_OVERLAY_BAR))) {
            frameLayoutOverlayBar.setVisibility(Utility.getJsonObjectBooleanValue(mResponseJson, SHOW_OVERLAY_BAR) ? View.VISIBLE : View.GONE);
        }

        frameLayoutOverlayBar.setVisibility(View.VISIBLE);

//        Search anything layout.
        relativeLayoutSearchAnything = (RelativeLayout) findViewById(R.id.relativeLayout_search_anything);
        editTextSearchAnything = (AutoCompleteTextView) findViewById(R.id.editText_search_anything);
        if (Utility.getJsonObjectBooleanValue(mResponseJson, IS_SHOW_ANYTHING_SEARCH_LAYOUT)) {
            relativeLayoutSearchAnything.setVisibility(View.VISIBLE);
//        set hint here.
            editTextSearchAnything.setHint(Utility.getStringObjectValue(mResponseJson, SEARCH_ANYTHING_HINT));
        }

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        searchTeacherButton = (ImageButton) findViewById(R.id.button_search_teacher);
//        Bottom sheet layout.
        layoutParams = (CoordinatorLayout.LayoutParams) ((CardView) findViewById(R.id.overlay_layout)).getLayoutParams();
        anchorBottomSheetBehavior = (AnchorBottomSheetBehavior) layoutParams.getBehavior();

//        Anchor the offset to 70 from the top of screen to overlay.
//        Set peek height to overlay as 24% of screen. // 42.9
        int peekHeight = 0;
        try {
            if (Utility.getJsonObjectIntValue(mResponseJson, OVERLAY_HEIGHT) != 0) {
                int anchorPercent = 100 - Utility.getJsonObjectIntValue(mResponseJson, OVERLAY_HEIGHT);
                // Should be half of the anchor offset, then it will matcht with peekHeight.
                peekHeight = (int) ((Utility.getScreenHeight() * (Utility.getJsonObjectIntValue(mResponseJson, OVERLAY_HEIGHT))) / 100);

//                Get the bottom anchor offset for overlay.
                setPeekHeightToBottomSheet(peekHeight);
                anchorBottomSheetBehavior.setAnchorOffset(Utility.getScreenHeight() * anchorPercent / 100);
            } else {
                int anchorPercent = 100 - 40;
                // Should be half of the anchor offset, then it will matcht with peekHeight.
                peekHeight = (int) ((Utility.getScreenHeight() * (40)) / 100);

//                Get the bottom anchor offset for overlay.
                setPeekHeightToBottomSheet(peekHeight);
                anchorBottomSheetBehavior.setAnchorOffset(Utility.getScreenHeight() * anchorPercent / 100);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        globalPeekHeight = peekHeight;

//        Bottom layout.
        frameLayoutBottomLayout = (FrameLayout) findViewById(R.id.bottom_layout);
        FrameLayout.LayoutParams layoutParamsBottomLayout = (FrameLayout.LayoutParams) frameLayoutBottomLayout.getLayoutParams();

//        To keep overlay stick at bottom.
        if (Utility.getJsonObjectBooleanValue(mResponseJson, KEEP_OVERLAY_AT_BOTTOM)) {
            layoutParamsBottomLayout.setMargins(0, 0, 0, (int) Utility.getDimension(R.dimen.overlay_above_btn_height, getApplicationContext()));
            frameLayoutBottomLayout.setLayoutParams(layoutParamsBottomLayout);
        }

        buttonBottom = (Button) findViewById(R.id.button_bottom);
        buttonBottom.setText(mLocationMapModel.getBottomButtonText());

        bottomButtonLayout = (LinearLayout) findViewById(R.id.bottom_button_layout);
        bottomButtonLayout.setBackgroundDrawable(ThemeUtils.getBgRoundedRectByTheme(this, mLocationMapModel.getColorCode()));
        bottomButtonLayout.setVisibility(mLocationMapModel.getIsShowBottomButton() == 1 ? View.VISIBLE : View.GONE);

        boolean isWrapContent = Utility.getJsonObjectBooleanValue(mResponseJson, IS_WRAP_CONTENT);
        String bottomButtonIconName = Utility.getStringObjectValue(mResponseJson, BOTTOM_BUTTON_ICON_NAME);

        //bottom layout for buttons
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) bottomButtonLayout.getLayoutParams();
        layoutParams.width = isWrapContent ? LinearLayout.LayoutParams.WRAP_CONTENT : LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParams.rightMargin = (int) Utility.convertDpToPixel(isWrapContent ? 0 : 44, this);
        layoutParams.leftMargin = (int) Utility.convertDpToPixel(isWrapContent ? 0 : 16, this);

        bottomButtonLayout.setLayoutParams(layoutParams);

        ImageView imageView = (ImageView) findViewById(R.id.bottom_btn_icon);

        if (!TextUtils.isEmpty(bottomButtonIconName)) {
            imageView.setImageDrawable(Utility.getImageDrawableFromAssets(this, bottomButtonIconName));
            imageView.setVisibility(View.VISIBLE);
        }

        searchAddressAutoCompleteText = (AutoCompleteTextView) findViewById(R.id.auto_complete_text_search_address);
        auto_complete_text_my_location = (AutoCompleteTextView) findViewById(R.id.auto_complete_text_my_location);

//        Source and destination autoComplete.
        searchAddressAutoCompleteTextSource = (AutoCompleteTextView) findViewById(R.id.auto_complete_text_search_address_source);
        searchAddressAutoCompleteTextDestination = (AutoCompleteTextView) findViewById(R.id.auto_complete_text_search_address_destination);

        initShouldShowLocationCard();
        initShowOnlyMyLocationCard();

        //Can user search for source and destination.
        if (!TextUtils.isEmpty(Utility.getStringObjectValue(mResponseJson, CAN_SEARCH_LOCATION))) {
            if (Utility.getJsonObjectBooleanValue(mResponseJson, CAN_SEARCH_LOCATION)) {
                searchAddressAutoCompleteTextSource.setVisibility(View.VISIBLE);
                searchAddressAutoCompleteTextDestination.setVisibility(View.VISIBLE);
                textViewMyLocation.setVisibility(View.GONE);
                textViewCarShopLocation.setVisibility(View.GONE);
            } else {
                searchAddressAutoCompleteTextSource.setVisibility(View.GONE);
                searchAddressAutoCompleteTextDestination.setVisibility(View.GONE);
                textViewMyLocation.setVisibility(View.VISIBLE);
                textViewCarShopLocation.setVisibility(View.VISIBLE);
            }
        }

        searchAddressAutoCompleteText.setVisibility(
                mLocationMapModel.getIsSelectLocation() == 1 ? View.VISIBLE : View.GONE);

        simpleSearchEdittext = (AutoCompleteTextView)

                findViewById(R.id.edittext_search_by_key);

        imageViewClearSearchText = (ImageView)

                findViewById(R.id.imageView_clear_search_text);

        simpleSearchLayout = (LinearLayout)

                findViewById(R.id.search_by_key_layout);

        simpleSearchEdittext.setHint(mLocationMapModel.getSearchPlaceholder());

        ((TextView)

                findViewById(R.id.textview_search_label)).

                setText(mLocationMapModel.getSearchTitle());
        ((TextView)

                findViewById(R.id.textview_search_label)).

                setVisibility(TextUtils.isEmpty(mLocationMapModel.getSearchTitle()) ? View.GONE : View.VISIBLE);

        setBtnCloseOverlay();

//        ================================= BottomSheet listener ============================
        anchorBottomSheetBehavior.addBottomSheetCallback(new AnchorBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == AnchorBottomSheetBehavior.STATE_DRAGGING)
                    return;
                if (newState == AnchorBottomSheetBehavior.STATE_EXPANDED) {
                    try {
                        Utility.callJavaScriptFunction(MapsAppCompactActivity.this, mWebviewConfirm, TOGGLE_BODY_SCROLL, new JSONObject().put(SCROLL, true));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (newState == AnchorBottomSheetBehavior.STATE_ANCHORED || newState == AnchorBottomSheetBehavior.STATE_COLLAPSED) {
//                    Avoid animation if previous state is collapse.
                    try {
                        Utility.callJavaScriptFunction(MapsAppCompactActivity.this, mWebviewConfirm, TOGGLE_BODY_SCROLL, new JSONObject().put(SCROLL, false));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (previousState == AnchorBottomSheetBehavior.STATE_COLLAPSED ||
                            previousState == AnchorBottomSheetBehavior.STATE_ANCHORED)
                        return;

                    anchorBottomSheetBehavior.setAllowUserDragging(true);

                    invalidateOptionsMenu();
                    previousState = newState;

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }


    /**
     * @param view
     */
    private void animateScaleViewOut(final View view) {
        ScaleAnimation anim = new ScaleAnimation(1, 1, 1, 0);
        anim.setFillAfter(false);
        anim.setDuration(100);
        view.startAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * @param view
     */
    private void animateScaleViewIn(final View view) {
        ScaleAnimation anim = new ScaleAnimation(1, 1, 0, 1);
        anim.setFillAfter(false);
        anim.setDuration(100);
        view.startAnimation(anim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * Show icon on toolbar
     * @param menu
     * @param show boolean value(true,false)
     */
    private void showListIconOnToolbar(Menu menu, boolean show) {
//                    Hide list menu.
        if (menu.findItem(1) != null)
            menu.findItem(1).setVisible(show);
    }


    /**
     * show or hide location card based on response
     */
    private void initShowOnlyMyLocationCard() {
        showOnlyMyLocationCard = Utility.getJsonObjectBooleanValue(mResponseJson, SHOW_ONLY_MY_LOCATION_CARD);
        CardView cardview_only_my_location = (CardView) findViewById(R.id.cardview_only_my_location);
        cardview_only_my_location.setVisibility(showOnlyMyLocationCard ? View.VISIBLE : View.GONE);
    }


    /**
     * initialize location card and set visibility
     */
    private void initShouldShowLocationCard() {
//        Init components for fix my ride.
        cardViewMapSourceAndDestination = (CardView) findViewById(R.id.cartView_my_location);
        textViewMyLocation = (TextView) findViewById(R.id.textView_my_location);
        textViewCarShopLocation = (TextView) findViewById(R.id.textview_car_shop_location);

        textViewSourceTitle = (TextView) findViewById(R.id.textView_source_title);
        textViewDestinationTitle = (TextView) findViewById(R.id.textView_destination_title);

        if (!Utility.isEmpty(Utility.getStringObjectValue(mResponseJson, SOURCE_TITLE))) {
            textViewSourceTitle.setText(Utility.getStringObjectValue(mResponseJson, SOURCE_TITLE));
        }

        if (!Utility.isEmpty(Utility.getStringObjectValue(mResponseJson, DESTINATION_TITLE))) {
            textViewDestinationTitle.setText(Utility.getStringObjectValue(mResponseJson, DESTINATION_TITLE));
        }

//        Show location card here.
        boolean showMapSourceAndDestinationControl = Utility.getJsonObjectBooleanValue(mResponseJson, SHOULD_SHOW_LOCATION_CARD);

        cardViewMapSourceAndDestination.setVisibility(showMapSourceAndDestinationControl ? View.VISIBLE : View.GONE);

        textViewMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSourceClicked = true;
            }
        });

        textViewCarShopLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSourceClicked = false;
            }
        });

        if (!Utility.isEmpty(Utility.getStringObjectValue(mResponseJson, START_LOCATION_NAME))) {
            searchAddressAutoCompleteTextSource.setText(Utility.getStringObjectValue(mResponseJson, START_LOCATION_NAME));
        }

        if (!Utility.isEmpty(Utility.getStringObjectValue(mResponseJson, DESTINATION_LOCATION_NAME))) {
            searchAddressAutoCompleteTextDestination.setText(Utility.getStringObjectValue(mResponseJson, DESTINATION_LOCATION_NAME));
        }

//        Source and destination autocomplete clicked.
        searchAddressAutoCompleteTextSource.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                    setOverlayStateToCollapse(AnchorBottomSheetBehavior.STATE_COLLAPSED);
                    isSourceClicked = true;
                    sourceClicked = true;
                    if (sourceLatLong != null) {
                        moveToCurrentLocation(sourceLatLong);
                    }
                }
            }
        });

        searchAddressAutoCompleteTextDestination.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    setOverlayStateToCollapse(AnchorBottomSheetBehavior.STATE_COLLAPSED);
                    isSourceClicked = false;
                    destinationClicked = true;
                    if (destinationLatLong != null) {
                        moveToCurrentLocation(destinationLatLong);
                    }
                }
            }
        });

//        ================ Keybaord focus change listener ==============
        new KeyboardUtils().setKeyboardListener(searchAddressAutoCompleteTextSource, new KeyboardUtils.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                if (Utility.getStringObjectValue(mResponseJson, CAN_EDIT_MY_LOCATION_ON_CARD) != null) {
                    setPeekHeightToBottomSheet(0);
                }
            }

            @Override
            public void onKeyboardClose() {
                if (Utility.getStringObjectValue(mResponseJson, CAN_EDIT_MY_LOCATION_ON_CARD) != null) {
                    setPeekHeightToBottomSheet(globalPeekHeight);
                }
            }
        });
    }


    /**
     * initialize close overlay button set click listener and visibility to button
     */
    private void setBtnCloseOverlay() {
        btnCloseOverlay = (ImageButton) findViewById(R.id.btn_close_overlay);
//        Hide button close for overlay.
        btnCloseOverlay.setVisibility(showOverlayCloseButton ? View.VISIBLE : View.GONE);

        btnCloseOverlay.setOnClickListener(this);
        String headerButtonColor = Utility.getStringObjectValue(configJson, HEADER_BUTTON_COLOR);

        if (!TextUtils.isEmpty(headerButtonColor)) {
            btnCloseOverlay.setImageTintList(ColorStateList.valueOf(Color.parseColor(headerButtonColor)));
        }

    }


    /**
     * initialize GooglePlacesAutocompleteAdapter
     */
    private void initAutoCompleteAdapter() {
        mGooglePlacesAutocompleteAdapter = new GooglePlacesAutocompleteAdapter(context, android.R.layout.simple_list_item_1, mLocationMapModel.getSearchByCountriesList());
        searchAddressAutoCompleteText.setAdapter(mGooglePlacesAutocompleteAdapter);
        auto_complete_text_my_location.setAdapter(mGooglePlacesAutocompleteAdapter);

//        Auto complete for source and destination.
        searchAddressAutoCompleteTextSource.setAdapter(mGooglePlacesAutocompleteAdapter);
        searchAddressAutoCompleteTextDestination.setAdapter(mGooglePlacesAutocompleteAdapter);

    }


    /**
     * initialize autocomplete adapter and set autocomplet adapter to Search edittext
     */
    private void initAutoCompleteForSearch() {
        try {
            if (mLocationMapModel.isSearchAutoComplete() == 1) {
                final SearchAutoCompleteAdapter searchAutoCompleteAdapter = new SearchAutoCompleteAdapter(context, android.R.layout.simple_list_item_1);
                searchAutoCompleteAdapter.setKeyToBind(mLocationMapModel.getLocationTitle());
                searchAutoCompleteAdapter.setSearchArrJson((JSONArray) Utility.getJsonObjectValue(mResultNearByLocation, TeacherPlace.DATA));
                simpleSearchEdittext.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                        Hide search box here
                        setSearchLayoutVisibility(false);
                        simpleSearchEdittext.setText(Utility.getStringObjectValue(searchAutoCompleteAdapter.getItem(i), mLocationMapModel.getLocationTitle()));
                        parseNearByPlacesResponse(mResultNearByLocation, simpleSearchEdittext.getText().toString());

                        try {
                            JSONObject jsonObjGeoAddress = (JSONObject) Utility.getJsonObjectValue(searchAutoCompleteAdapter.getItem(i), mLocationMapModel.getLocationAddress());

                            JSONArray coordinateArr = (JSONArray) Utility.getJsonObjectValue(jsonObjGeoAddress, TeacherPlace.COORDINATES);

                            double latitude = coordinateArr.getDouble(1);
                            double longitude = coordinateArr.getDouble(0);
                            loadLatLongOnMap(new LatLng(latitude, longitude));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                simpleSearchEdittext.setAdapter(searchAutoCompleteAdapter);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * Initialize all the listeners
     */
    private void setAllListener() {
        buttonBottom.setOnClickListener(this);
        bottomButtonLayout.setOnClickListener(this);
        searchTeacherButton.setOnClickListener(this);
        imageViewClearSearchText.setOnClickListener(this);
//        Search anything click
        editTextSearchAnything.setOnClickListener(this);
        searchAddressAutoCompleteText.setOnItemClickListener(autoCompleteListener);
        searchAddressAutoCompleteTextSource.setOnItemClickListener(autoCompleteListener);
        searchAddressAutoCompleteTextDestination.setOnItemClickListener(autoCompleteListener);

        auto_complete_text_my_location.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                mPlaceModel = (PlaceModel) parent.getAdapter().getItem(position);

                MapAsyncTask mapAsyncTask = new MapAsyncTask();
                mapAsyncTask.setOnMapAsyncListener(new MapAsyncTask.OnMapAsyncListener() {
                    @Override
                    public void onMapAsyncTaskResult(JSONObject object) {
                        try {
                            if (object != null) {
                                JSONObject resultObject = object.getJSONObject(googlePlace.PLACE_RESPONSE_RESULT);
                                JSONObject locationObject = resultObject.getJSONObject(googlePlace.PLACE_GEOMETRY).getJSONObject(googlePlace.PLACE_LOCATION);

                                LatLng originLatLng = new LatLng(locationObject.getDouble(PLACE_LAT), locationObject.getDouble(googlePlace.PLACE_LNG));
                                clickLatitude = originLatLng.latitude;
                                clickLongitude = originLatLng.longitude;

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
        });

        searchAddressAutoCompleteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int startNum, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == 0) {
                    setSearchLayoutVisibility(false);
                } else {
                    setSearchLayoutVisibility(true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
        });

        simpleSearchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (TextUtils.isEmpty(charSequence)) {
                    parseNearByPlacesResponse(mResultNearByLocation, null);
                    imageViewClearSearchText.setVisibility(View.GONE);
                } else {
                    imageViewClearSearchText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        simpleSearchEdittext.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //search click handling for teacher
                    if (!TextUtils.isEmpty(simpleSearchEdittext.getText().toString().trim())) {
                        parseNearByPlacesResponse(mResultNearByLocation, simpleSearchEdittext.getText().toString());
                        Utility.hideSoftKeyboard(context, getWindow().getDecorView().getWindowToken());
                    } else {
                        Toast.makeText(context, "Please enter " + mLocationMapModel.getSearchPlaceholder(), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                return false;
            }
        });

    }


    /**
     * Show curreent location address using latitude and longitude
     *
     * @param latitude lattitude to get address
     * @param longitude longitude to get address
     */
    private void showMyLocationAddress(final double latitude, final double longitude) {
        if (latitude > 0 && longitude > 0) {
            sourceLatLong = new LatLng(latitude, longitude);
            mLocationProvider.getCompleteAddressString(latitude, longitude, new LocationProvider.OnCapturedLocationString() {
                @Override
                public void onCapturedAddress(String addressString) {
                    auto_complete_text_my_location.setText(addressString);
                    textViewMyLocation.setText(addressString);

                    if (Utility.getStringObjectValue(mResponseJson, SOURCE_LOCATION) != null) {
                        if (sourceClicked) {
                            searchAddressAutoCompleteTextSource.setText(addressString);
                        } else {
                            try {
                                JSONObject jsonObject = new JSONObject(Utility.getStringObjectValue(mResponseJson, SOURCE_LOCATION));
                                searchAddressAutoCompleteTextSource.setText(jsonObject.getString(ADDRESS_STRING));
                                addressString = jsonObject.getString(ADDRESS_STRING);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {

                        searchAddressAutoCompleteTextSource.setText(addressString);
                    }


                    searchAddressAutoCompleteTextSource.dismissDropDown();

                    jsonObjectSourceData = new JSONObject();
                    try {
                        jsonObjectSourceData.put(ADDRESS_STRING, addressString);
                        jsonObjectSourceData.put(LAT, latitude);
                        jsonObjectSourceData.put(LONG, longitude);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    }


    /**
     * Show destination address using given latitude and longitude
     *
     * @param latitude lattitude to get destination address
     * @param longitude longitude to get destination address
     */
    private void showDestinationAddress(final double latitude, final double longitude) {
        if (latitude > 0 && longitude > 0) {
            destinationLatLong = new LatLng(latitude, longitude);
            mLocationProvider.getCompleteAddressString(latitude, longitude, new LocationProvider.OnCapturedLocationString() {
                @Override
                public void onCapturedAddress(String addressString) {
                    if (Utility.getStringObjectValue(mResponseJson, DESTINATION_LOCATION) != null) {
                        if (destinationClicked) {
                            searchAddressAutoCompleteTextDestination.setText(addressString);
                        } else {
                            try {
                                JSONObject jsonObject = new JSONObject(Utility.getStringObjectValue(mResponseJson, DESTINATION_LOCATION));
                                searchAddressAutoCompleteTextDestination.setText(jsonObject.getString(ADDRESS_STRING));
                                addressString = jsonObject.getString(ADDRESS_STRING);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        textViewCarShopLocation.setText(addressString);
                        searchAddressAutoCompleteTextDestination.setText(addressString);
                    }

                    searchAddressAutoCompleteTextDestination.dismissDropDown();
                    try {
                        jsonObjectDestinationData = new JSONObject();
                        jsonObjectDestinationData.put(ADDRESS_STRING, addressString);
                        jsonObjectDestinationData.put(LAT, latitude);
                        jsonObjectDestinationData.put(LONG, longitude);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    /**
     * Initialize tab layout
     */
    private void initTabLayout() {

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tabLayout.addTab(tabLayout
                .newTab()
                .setText(!TextUtils.isEmpty(mLocationMapModel.getTab1()) ? mLocationMapModel.getTab1() : Utility.getResString(R.string.near_me_tab, context)));

        tabLayout.addTab(tabLayout
                .newTab()
                .setText(!TextUtils.isEmpty(mLocationMapModel.getTab2()) ? mLocationMapModel.getTab2() : Utility.getResString(R.string.search_teacher_tab, context)));

        tabLayout.setVisibility(mLocationMapModel.getIsShowTab() == 1 ? View.VISIBLE : View.GONE);

    }


    /**
     * Needs to call MapsInitializer before doing any CameraUpdateFactory calls
     */
    private void initializerMapsAsync() {
        mMap.getMapAsync(this);
    }


    /**
     * Initialize required map settings
     */
    private void initMapSettings() {
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setTrafficEnabled(false);
        mGoogleMap.setIndoorEnabled(true);
        mGoogleMap.setBuildingsEnabled(true);

        // Map UI level Settings
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setCompassEnabled(false);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(mLocationMapModel.getIsPlotAddressLocation());
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(true);

        mGoogleMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);

        //Uncomment To Show Google Location Blue Pointer
        if (mMap != null && mMap.findViewById(Integer.parseInt(google_location_blue_pointer.LOCATION_ONE)) != null) {
            View locationButton = ((View) mMap.findViewById(Integer.parseInt(google_location_blue_pointer.LOCATION_ONE)).getParent()).findViewById(Integer.parseInt(google_location_blue_pointer.LOCATION_TWO));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);

            rlp.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            rlp.addRule(RelativeLayout.ALIGN_END, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rlp.setMargins((int) Utility.getDimension(R.dimen.activity_horizontal_margin_16, getApplicationContext()),
                    0, 0, (int) Utility.getDimension(R.dimen.activity_horizontal_margin_16, getApplicationContext()));
        }

        if (mMap != null && mMap.findViewById(4) != null) {
            RelativeLayout relativeLayout = ((RelativeLayout) mMap.findViewById(4).getParent());

            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) relativeLayout.getLayoutParams();

        }

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    //handling click listener for marker
                    Object markerObj = marker.getTag();
                    if (markerObj == null) return false;

                    if (mLocationMapModel.getIsMarkerClickCallback() == 1) {
                        if (marker != null && marker.getTag() != null) {
                            setResultToCallingActivity(Activity.RESULT_OK, (JSONObject) marker.getTag());
                        }
                    } else if (mLocationMapModel.isMarkerClickShowOverlay()) {
                        showOrHideOverlay(true);
                        JSONObject jsonObject = (JSONObject) markerObj;
                        jsonObject.put(LAT, String.valueOf(mCurrentLocation.getLatitude()));
                        jsonObject.put(LONG, String.valueOf(mCurrentLocation.getLongitude()));
                        jsonObject = getMapMovedNewLocation(jsonObject);
//                        To add location for mobo app if the showBottomButtonLeft is :  true.
                        if (showBottomButtonLeft) {
                            mLocationProvider.getCompleteAddressJsonObject(mapCenterLatLng.latitude, mapCenterLatLng.longitude, new LocationProvider.OnCapturedLocationObject() {
                                @Override
                                public void onCaptured(JSONObject jsonObjectAddress) {
                                    locatinDictJsonObject = jsonObjectAddress;
                                }
                            });
                        }
                        Utility.callJavaScriptFunction(MapsAppCompactActivity.this, mWebviewConfirm, ON_MARKER_CLICK, jsonObject);
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

                return false;
            }
        });

        mGoogleMap.setOnMapClickListener(onMapClickListener);
    }


    /**
     * Build url using secret key
     *
     * @param url url to build using Secret Key
     * @return returns string url
     */
    private String buildUrlWithSecretKey(String url) {
        StringBuilder sb = new StringBuilder(url);

        sb.append("?").append("queryMode=mylist");
        sb.append("&tokenKey=").append(AUTH_TOKEN);
        sb.append("&secretKey=").append(AUTH_SECRET_KEY);

        return sb.toString();
    }


    /**
     *
     */
    private void setupNavigation() {

        if (mLocationMapModel.getIsNavigation() == 1) {

            new AsyncTask<Void, Void, DirectionsResult>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected DirectionsResult doInBackground(Void... voids) {
                    DirectionsResult results = getDirectionsDetails(
                            new com.google.maps.model.LatLng(mLocationMapModel.getLatitude(), mLocationMapModel.getLongitude()),
                            new com.google.maps.model.LatLng(mLocationMapModel.getDestLatitude(), mLocationMapModel.getDestLongitude()),
                            TravelMode.DRIVING);
                    return results;
                }

                @Override
                protected void onPostExecute(DirectionsResult results) {
                    super.onPostExecute(results);
                    showOrHideProgressDialog(false, "");
                    if (results != null) {
                        if (results.routes.length > 0) {
                            addPolyline(results, mGoogleMap);
                            addMarkersToMap(results, mGoogleMap);
                            setZoomMapForRoute(results.routes[overview]);
                        }
                    }
                }
            }.execute();
        }
    }


    /**
     * create bitmap from drawable image
     */
    private void carPinBitmap() {
        int height = 80;
        int width = 45;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.car);
        Bitmap b = bitmapdraw.getBitmap();
        mCarBitmapIcon = Bitmap.createScaledBitmap(b, width, height, false);

    }


    /**
     * get map pin icon from url and add to memory cache
     */
    private void mapPinFromUrl() {
        try {
            if (!TextUtils.isEmpty(mLocationMapModel.getMapPinIcon())) {

                mMapPinIconFromUrl = App.getInstance().getBitmapFromMemCache(mLocationMapModel.getMapPinIcon().toString());

                if (mMapPinIconFromUrl == null) {
                    mMapPinIconFromUrl = Ion.with(this)
                            .load(mLocationMapModel.getMapPinIcon().toString()).asBitmap().get();
                    App.getInstance().addBitmapToMemoryCache(mLocationMapModel.getMapPinIcon().toString(), mMapPinIconFromUrl);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * get source and destination icon from url
     */
    private void sourceDestMarkerIconFromUrl() {
        try {
            String sourceIconUrl = Utility.getStringObjectValue(mResponseJson, SOURCE_MARKER_ICON);
            String destIconUrl = Utility.getStringObjectValue(mResponseJson, DEST_MARKER_ICON);

            if (!TextUtils.isEmpty(sourceIconUrl)) {

                mSourceIconBitmap = App.getInstance().getBitmapFromMemCache(sourceIconUrl);

                if (mSourceIconBitmap == null) {
                    mSourceIconBitmap = Ion.with(this)
                            .load(sourceIconUrl).asBitmap().get();
                    App.getInstance().addBitmapToMemoryCache(sourceIconUrl, mSourceIconBitmap);
                }
            }

            if (!TextUtils.isEmpty(destIconUrl)) {
                mDestIconBitmap = App.getInstance().getBitmapFromMemCache(destIconUrl);

                if (mDestIconBitmap == null) {
                    mDestIconBitmap = Ion.with(this)
                            .load(destIconUrl).asBitmap().get();
                    App.getInstance().addBitmapToMemoryCache(destIconUrl, mDestIconBitmap);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * returns  the horizontal direction of travel between 2 locations
     * @param latLng1
     * @param latLng2
     * @return
     */
    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }


    /**
     * Add or remove current location marker
     *
     * @param currentPosition location to move the marker
     * @param previousPosition previous location
     */
    private void addOrMoveCurrentLocationMarker(LatLng currentPosition, LatLng previousPosition) {

        if (mCurrentLocationMarker == null) {
            mCurrentLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(currentPosition)
                    .icon(BitmapDescriptorFactory.fromBitmap(mMapPinIconFromUrl != null ? mMapPinIconFromUrl : mCarBitmapIcon))
                    .rotation((float) bearingBetweenLocations(currentPosition, previousPosition)));
        } else {
            mCurrentLocationMarker.setPosition(currentPosition);
            mCurrentLocationMarker.setRotation((float) bearingBetweenLocations(previousPosition, currentPosition));
        }

    }


    /**
     * Animate the marker (set position,visibility,rotation)
     * @param curPostion current position
     * @param prevPosition previous position
     * @param mMarker Marker object to animate
     */
    private void animateMarker(final LatLng curPostion, final LatLng prevPosition, final Marker mMarker) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;  //5000
        final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {

                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        curPostion.latitude * (1 - t) + prevPosition.latitude * t,
                        curPostion.longitude * (1 - t) + prevPosition.longitude * t);


                mMarker.setPosition(currentPosition);
                mMarker.setFlat(true);

                mMarker.setRotation((float) bearingBetweenLocations(curPostion, prevPosition));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        mMarker.setVisible(false);
                    } else {
                        mMarker.setVisible(true);
                    }
                }
            }

        });

    }


    /**
     * Add markers on map
     *
     * @param results Direction Result object containing details(start position, next position
     * @param mMap Google map object
     */
    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        LatLng startPosition = new LatLng(results.routes[overview].legs[overview].startLocation.lat, results.routes[overview].legs[overview].startLocation.lng);

        LatLng nextPosition = new LatLng(results.routes[overview].legs[overview].endLocation.lat, results.routes[overview].legs[overview].endLocation.lng);

        MarkerOptions sourceMarkerOptions = new MarkerOptions()
                .position(startPosition)
                .title(results.routes[overview].legs[overview].startAddress);

        if (mSourceIconBitmap != null) {
            sourceMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(mSourceIconBitmap));
        }

        mMap.addMarker(sourceMarkerOptions);
        MarkerOptions destMarkerOptions = new MarkerOptions()
                .position(nextPosition)
                .title(results.routes[overview].legs[overview].endAddress);

        if (mDestIconBitmap != null) {
            destMarkerOptions.icon(BitmapDescriptorFactory.fromBitmap(mDestIconBitmap));
        }

        mMap.addMarker(destMarkerOptions);
    }


    /**
     * Get End location title
     *
     * @param results DirectionsResult object containing end location details(title)
     * @return
     */
    private String getEndLocationTitle(DirectionsResult results) {
        return "Time :" + results.routes[overview].legs[overview].duration.humanReadable + " Distance :" + results.routes[overview].legs[overview].distance.humanReadable;
    }


    /**
     * Move camera to given latitude and longitude
     *
     * @param route Route direction on map
     * @param mMap Map object
     */
    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[overview].startLocation.lat,
                route.legs[overview].startLocation.lng), 12));
    }


    /**
     * Add poly line on given route
     *
     * @param results DirectionsResult containing list of lattitude and longitude to add polyline
     * @param mMap Map object
     */
    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[overview].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions()
                .addAll(decodedPath)
                .geodesic(true)
                .color(!TextUtils.isEmpty(Utility.getStringObjectValue(mResponseJson, MAP_ROUTE_COLOR)) ? Color.parseColor(Utility.getStringObjectValue(mResponseJson, MAP_ROUTE_COLOR))
                        : Color.parseColor("#0361AD")));
    }


    /**
     * @return GeoApiContext
     */
    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(3)
                .setApiKey(Utility.getMapApiKey(context))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }


    /**
     * et direction details from source and destination
     *
     * @param origin start location to get direction details
     * @param destination end location to get direction details
     * @param mode mode of transportation/travel
     * @return
     */
    private DirectionsResult getDirectionsDetails(com.google.maps.model.LatLng origin, com.google.maps.model.LatLng destination, TravelMode mode) {
        DateTime now = new DateTime();
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now)
                    .await();
        } catch (com.google.maps.errors.ApiException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * load data from bundle
     */
    private void loadBundleData() {
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mLocationMapModel = bundle.containsKey(MAP_LOCATION_MODEL)
                        ? (LocationMapModel) bundle.getParcelable(MAP_LOCATION_MODEL)
                        : new LocationMapModel();
                if (mLocationMapModel == null) {
                    mLocationMapModel = new LocationMapModel();
                }

                mResponseJson = new JSONObject(mLocationMapModel.getResponseData());

            }
            isMapOverlayHideOnSwipe = Utility.getJsonObjectBooleanValue(mResponseJson, IS_MAP_OVERLAY_HIDE_ON_SWIPE);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * set toolbar (home icon,title,color)
     */
    private void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(
                Utility.getJsonObjectBooleanValue(mResponseJson, IS_MENU_SHOW)
                        ? R.drawable.ic_menu_black_24dp
                        : R.drawable.ic_arrow_back_black_24dp);

        getSupportActionBar().setTitle("");
        String title = mLocationMapModel.getPageTitle();

        setToolbarTitle(!TextUtils.isEmpty(mLocationMapModel.getPageTitle()) ? "Map" : mLocationMapModel.getPageTitle());

//        Change toolbar icon colors.
        setColorizeToolbar(mToolbar);

    }


    /**
     * set color to toolbar
     *
     * @param toolbar Toolbar object
     */
    public void setColorizeToolbar(Toolbar toolbar) {
        try {
            String headerButtonColor = Utility.getStringObjectValue(Utility.configJson, HEADER_BUTTON_COLOR);
            if (MapConstant.LOAD_HTML_DIRECTLY && !TextUtils.isEmpty(headerButtonColor)) {
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.parseColor(headerButtonColor), this);
            } else if (THEME_ID == themeId.THEME_GRAY) {
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.BLACK, this);
            } else {

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * set toolbar title with given string
     *
     * @param title String to set as toolbar title
     */
    public void setToolbarTitle(String title) {
        TextView textView = (TextView) mToolbar.findViewById(android.R.id.text1);
        ImageView logoIcon = (ImageView) mToolbar.findViewById(R.id.logoIcon);
        try {
            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

            textView.setText(title);

            String headerLogoName = Utility.getStringObjectValue(new JSONObject(mLocationMapModel.getResponseData()),
                    HEADER_LOGO);
            logoIcon.setImageDrawable(Utility.getImageDrawableFromAssets(this, headerLogoName));

            if (!TextUtils.isEmpty(headerLogoName)) {
                logoIcon.setVisibility(View.VISIBLE);
                textView.setVisibility(View.GONE);
            } else {
                logoIcon.setVisibility(View.GONE);
                textView.setVisibility(View.VISIBLE);

            }

            changedToolbarTextColorByTheme(textView);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * set up webview
     * enable javascript, dom storage
     * allow file access
     * set WebViewClient, WebChromeClient
     * addJ avascript Interface
     * set Overlay Height
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void setWebView() {
        boolean isSplitOverlay = Utility.getJsonObjectBooleanValue(mResponseJson, IS_SPLIT_OVERLAY);
        mWebviewConfirm = (WebView) findViewById(isSplitOverlay ? R.id.webview_confirm : R.id.webview_overlay);

        showOrHideOverlay(mLocationMapModel.getIsShowOverlay() == 1);

        WebSettings settings = mWebviewConfirm.getSettings();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        settings.setAllowFileAccess(true);

        mWebviewConfirm.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        mWebviewConfirm.setWebChromeClient(new WebChromeClient() {
        });

        if (Build.VERSION.SDK_INT >= 19) {
            // chromium, enable hardware acceleration
            mWebviewConfirm.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            mWebviewConfirm.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mWebviewConfirm.addJavascriptInterface(new WebAppJavascriptInterface(this), "Android");

        if (isSplitOverlay) {
            setOverlayHeight();
        }

    }


    /**
     * set webview height
     */
    private void setOverlayHeight() {
        mWebviewConfirm.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels,
                (int) Utility.convertDpToPixel(mLocationMapModel.getOverlaySize(), this)));
    }


    /**
     * load url in webview using page name
     * @param pageName
     */
    private void loadUrlOvelayPage(String pageName) {
        if (!TextUtils.isEmpty(pageName)) {
            mWebviewConfirm.loadUrl(Uri.decode(getCompleteUrl(pageName)));
        }
    }


    /**
     * get complete url using page name
     *
     * @param pageName page name to form url
     * @return
     */
    private String getCompleteUrl(String pageName) {
        File sandboxFile = new File(Utility.getHtmlDirFromSandbox(getApplicationContext()) + File.separator + pageName);
        String url = Uri.fromFile(sandboxFile).toString();
        url = buildUrlWithSecretKey(url);

        if (!TextUtils.isEmpty(mLocationMapModel.getQueryString())) {
            url = url + mLocationMapModel.getQueryString();
        }
        return url;

    }


    /**
     * set search layout visibility based on given boolean value
     * @param isShowSearchLayout boolen value to know search layout visibility(visible,invisible)
     */
    public void setSearchLayoutVisibility(boolean isShowSearchLayout) {
        simpleSearchLayout.setVisibility(isShowSearchLayout ? View.VISIBLE : View.GONE);
        if (isShowSearchLayout) {
            simpleSearchEdittext.requestFocus();
            Utility.showKeyboard(this);
        } else {
            Utility.hideSoftKeyboard(MapsAppCompactActivity.this, simpleSearchEdittext.getWindowToken());
        }
    }


    /**
     * checks if GooglePlayServices is available
     *
     * @return returns true if GooglePlayServices is available or returns false
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {

            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, R.string.cannot_connect_to_play_service, Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    /**
     * this function returns true if GPS information is available or returns false
     * @return returns true if gps info is available
     */
    private boolean isGPSInfo() {

        LocationManager locationmanager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // YOUR MAPS ACTIVITY CALLING or WHAT YOU NEED
            return true;
        } else {
            return false;
        }
    }


    /**
     *
     */
    public void gpsSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationProvider.mLocationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mLocationProvider.getGoogleApiClient(), builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
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
                            status.startResolutionForResult((Activity) context, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Toast.makeText(context, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }


    /**
     * set custom style to map
     */
    public void setCustomMapStyleFromResource() {
        try {
            boolean success = mGoogleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.google_map_style_map));

            if (!success) {
                Log.e("onMapReady", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("onMapReady", "Can't find style. Error: ", e);
        }
    }


    /**
     * get nearBy places from custom Api
     *
     * @param latitude
     * @param longitude
     */
    private void getNearByPlacesFromCustomApi(double latitude, double longitude) {
        getNearByPlacesFromCustomApi(latitude, longitude, null);
    }


    /**
     * Get near by places using custom Api
     *
     * @param latitude
     * @param longitude
     * @param mapRefreshJson
     */
    private void getNearByPlacesFromCustomApi(double latitude, double longitude, final JSONObject mapRefreshJson) {
        try {

            if (TextUtils.isEmpty(mLocationMapModel.getApiName())) {
                showOrHideProgressDialog(false, "");
                return;
            }

            Utility.hideSoftKeyboard(context, getWindow().getDecorView().getWindowToken());

            GetNearByPlacesClientEvent getNearByPlacesClientEvent =
                    new GetNearByPlacesClientEvent(this, mLocationMapModel.getApiName());

            JSONObject object = new JSONObject();
            if (new JSONObject(mLocationMapModel.getResponseData()).has(API_PARAMETERS)) {
                object.put(TOKEN_KEY, Utility.getStringObjectValue(new JSONObject(mLocationMapModel.getResponseData()),
                        TOKEN_KEY));
                object.put(SECRET_KEY, Utility.getStringObjectValue(new JSONObject(mLocationMapModel.getResponseData()),
                        SECRET_KEY));
                object.put(QUERY_MODE, Utility.getStringObjectValue(new JSONObject(mLocationMapModel.getResponseData()),
                        QUERY_MODE));
                object.put(REQUEST_ID, Utility.getStringObjectValue(new JSONObject(mLocationMapModel.getResponseData()),
                        REQUEST_ID));
                getNearByPlacesClientEvent.setRequestJson(object);
            } else {
                getNearByPlacesClientEvent.setRequestJson(new JSONObject(mLocationMapModel.getResponseData()));
            }
//            Append extra json object and deepMerge that jsonObject in existing input object.
            if (shouldLoadMapViewWithSettings) {
                getNearByPlacesClientEvent.setOtherRequestData(otherRequestData);
            }

            if (mapRefreshJson != null) {
                getNearByPlacesClientEvent.setMapRefreshJson(mapRefreshJson);
            }

            getNearByPlacesClientEvent.setLooper(Looper.getMainLooper());
            getNearByPlacesClientEvent.setListener(new IWebSocketClientEvent() {
                @Override
                public void onFinish(final Error error, final Object process, WebSocketClientEvent socketClientEvent) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                if (mapRefreshJson == null) {
                                    loadUrlOvelayPage(mLocationMapModel.getOverlayPage());
                                }
                                //check if live tracking is enable with timer
                                if ((mLocationMapModel.getIsLiveTracking() == 1 || Utility.isIpLimomob())
                                        && mLocationMapModel.getLiveTrackingIntervalInMs() > 0) {
                                    cancelLiveTrackerCallApiTimer();
                                    liveTrackerCallApiTimer(mLocationMapModel.getLiveTrackingIntervalInMs());
                                }

                                showOrHideProgressDialog(false, "");
                                if (error != null) {
                                    return;
                                }

                                if (process != null) {
                                    mResultNearByLocation = ((JSONObject) process);
                                    if (Utility.isIpLimomob()) {
                                        parseNearByDriverResponse(mResultNearByLocation);
                                    } else {
                                        parseNearByPlacesResponse(mResultNearByLocation, null);
                                    }
                                }

                                initAutoCompleteForSearch();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                        }
                    });

                }
            });
            getNearByPlacesClientEvent.fire();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * parse response
     *
     * @param jsonObject JSONObject
     */
    private void parseNearByDriverResponse(final JSONObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = null;
                    if (jsonObject.has(MARKER_DATA))
                        jsonArray = (JSONArray) Utility.getJsonObjectValue(jsonObject, TeacherPlace.MARKER_DATA);
                    else
                        jsonArray = (JSONArray) Utility.getJsonObjectValue(jsonObject, TeacherPlace.DATA);

                    mGoogleMap.clear();

                    mLastSelectedMarker = null;

                    if (jsonArray != null && jsonArray.length() > 0) {

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject placeJsonObj = jsonArray.getJSONObject(i);

                            double longitude = Double.parseDouble(Utility.getStringObjectValue(placeJsonObj, LONG));
                            double latitude = Double.parseDouble(Utility.getStringObjectValue(placeJsonObj, LAT));
                            int iconFlag = Utility.getJsonObjectIntValue(placeJsonObj, ICON);

                            String icon = Utility.getStringObjectValue(placeJsonObj, PLACE_ICON);

                            LatLng latLng = new LatLng(latitude, longitude);

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            if (iconFlag == 1) {
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_red_car));
                            } else {
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_green_car));
                            }

                            Marker marker = mGoogleMap.addMarker(markerOptions);

                            if (marker != null) {
                                marker.setTag(placeJsonObj);
                            }
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


    /**
     * parse near by places responce data
     *
     * @param jsonObj JSONObject containing
     * @param filterStr
     */
    private void parseNearByPlacesResponse(final JSONObject jsonObj, final String filterStr) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONArray jsonArray = null;
                    if (jsonObj.has(MARKER_DATA))
                        jsonArray = (JSONArray) Utility.getJsonObjectValue(jsonObj, TeacherPlace.MARKER_DATA);
                    else
                        jsonArray = (JSONArray) Utility.getJsonObjectValue(jsonObj, TeacherPlace.DATA);

                    if (!Utility.getJsonObjectBooleanValue(mResponseJson, LOAD_PLACES_AND_ROUTE_BOTH)) {
                        mGoogleMap.clear();
                    }

                    mLastSelectedMarker = null;
                    addOrMoveSelectedLocationMarker(new LatLng(clickLatitude, clickLongitude));

                    if (jsonArray != null && jsonArray.length() > 0) {

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject placeJsonObj = jsonArray.getJSONObject(i);

                            String title = Utility.getStringObjectValue(placeJsonObj, MARKER_POPUP_TEXT);

                            JSONObject jsonObjGeoAddress;

                            if (placeJsonObj.has(LOCATION_ADDRESS))
                                jsonObjGeoAddress = (JSONObject) Utility.getJsonObjectValue(placeJsonObj, LOCATION_ADDRESS);
                            else
                                jsonObjGeoAddress = (JSONObject) Utility.getJsonObjectValue(placeJsonObj, mLocationMapModel.getLocationAddress());

                            JSONArray coordinateArr = (JSONArray) Utility.getJsonObjectValue(jsonObjGeoAddress, TeacherPlace.COORDINATES);

                            double longitude = coordinateArr.getDouble(0);
                            double latitude = coordinateArr.getDouble(1);

                            String icon = Utility.getStringObjectValue(placeJsonObj, PLACE_ICON);

                            LatLng latLng = new LatLng(latitude, longitude);

                            Marker marker = null;

                            if (!TextUtils.isEmpty(filterStr) && !TextUtils.isEmpty(title)) {
                                if (title.toLowerCase().contains(filterStr.toLowerCase())) {

                                    marker = addMarkerOnMap(latLng, icon, title);
                                }
                            } else {

                                marker = addMarkerOnMap(latLng, icon, title);
                            }

                            if (marker != null) {
                                marker.setTag(placeJsonObj);
                            }
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


    /**
     * load latitude and longitude on map
     *
     * @param latLng lattitude and longitude to load on map
     */
    private void loadLatLongOnMap(LatLng latLng) {
        if (latLng != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, mLocationMapModel.getMapZoomLevel() <= 0
                    ? ZOOM_LEVEL : mLocationMapModel.getMapZoomLevel());
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }


    /**
     * Move camera to current location
     *
     * @param latLng current position lattitude and longitude
     */
    private void moveToCurrentLocation(LatLng latLng) {
        if (latLng != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
            mGoogleMap.animateCamera(cameraUpdate);
        }
    }


    /**
     * recenter camera on given latitude and longitude
     * @param latLng
     */
    private void reCenterOnMap(LatLng latLng) {

        try {
            if (latLng != null) {

                if (!mGoogleMap.getProjection()
                        .getVisibleRegion()
                        .latLngBounds
                        .contains(latLng)) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                    mGoogleMap.animateCamera(cameraUpdate);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Add or move selected markers from the map
     *
     * @param latLng latitude and longitude to add or move marker
     */
    private void addOrMoveSelectedLocationMarker(LatLng latLng) {
        clickLatitude = latLng.latitude;
        clickLongitude = latLng.longitude;

        if (clickLatitude == 0.0 || clickLongitude == 0.0) return;

        mLocationProvider.getCompleteAddressString(clickLatitude, clickLongitude, new LocationProvider.OnCapturedLocationString() {
            @Override
            public void onCapturedAddress(String addressString) {
                clickAddress = addressString;

            }
        });

        if (mLastSelectedMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.flat(true);
            markerOptions.anchor(0.5f, 0.5f);

            if (mMapPinIconFromUrl != null && isShowFloatingPin != 1) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(mMapPinIconFromUrl));
                mLastSelectedMarker = mGoogleMap.addMarker(markerOptions);
            } else {
                if (isShowFloatingPin != 1) {
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.drop_location));
                }

            }
            if (isShowFloatingPin != 1) {
                mLastSelectedMarker = mGoogleMap.addMarker(markerOptions);
            }
        } else {
            mLastSelectedMarker.setPosition(latLng);
        }

    }


    /**
     * Add marker on given location
     *
     * @param latLng lattitude and longitude to add marker
     * @param icon icon to add as marker
     * @param title string title to add as marker title
     * @return
     */
    private Marker addMarkerOnMap(LatLng latLng, String icon, String title) {
        Marker marker = null;
        try {
            if (latLng != null) {
                Bitmap bmImg = null;

                if (TextUtils.isEmpty(icon)) {

                } else {
                    bmImg = App.getInstance().getBitmapFromMemCache(icon.toString());
                    if (bmImg == null) {
                        bmImg = Ion.with(this)
                                .load(icon.toString()).asBitmap().get();
                        bmImg = Utility.getImageDrawableFromBitmapBySize(MapsAppCompactActivity.this, bmImg, 24, 24);
                        App.getInstance().addBitmapToMemoryCache(icon.toString(), bmImg);
                    }

                }

                if (mMapPinIconFromUrl != null) {
                    bmImg = mMapPinIconFromUrl;
                }

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(title);
                markerOptions.flat(true);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmImg));

                marker = mGoogleMap.addMarker(markerOptions);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return marker;
    }


    /**
     * Get latitude and longitude from place id url
     *
     * @param placeId
     * @return returns string url
     */
    public String getLatLongFromPlaceIdUrl(String placeId) {
        String url = google_LatLongFromPlaceId.BASE_PATH_lAT_LONG_FROM_PLACE_ID + placeId +
                "&key=" + API_KEY;
        return url;
    }


    /**
     * create url containing near by places details
     * @param lat
     * @param lng
     * @param radius radius
     * @param namePlace
     * @param type
     * @return
     */
    public String getNearByPlacesUrl(double lat, double lng, String radius, String namePlace, String type) {

        String url = google_NearBy_place.BASE_PATH_NEAR_BY_PLACE
                + "location=" + lat
                + "," + lng
                + "&radius=" + radius
                + "&name=" + namePlace
                + "&types=" + type
                + "&key=" + API_KEY;
        return url;
    }


    /**
     * Get direction url from origin and destination
     *
     * @param origin origin location to form url
     * @param dest destination location to form url
     * @return returns directions url
     */
    public String getDirectionsUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String sensor = "sensor=false";
        String mode = "mode=driving";

        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        String output = "json";

        String url = google_DirectionsUrl.BASE_URL + output + "?key=" + API_KEY + "&" + parameters;
        return url;
    }


    /**
     * Show or hide progress dialog with message
     *
     * @param shown boolean value to show or hide progress bar(true, false)
     * @param message String to show on progress bar
     */
    private void showOrHideProgressDialog(boolean shown, String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            progressDialog.setCancelable(false);
            progressDialog.setMessage(message);
        }

        if (shown) {
            progressDialog.show();
        } else {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            progressDialog = null;
        }
    }


    /**
     * Cancle live tracking Api calling timer
     */
    private void cancelLiveTrackerCallApiTimer() {
        if (mLiveTrackerApiCallTimer != null) {
            mLiveTrackerApiCallTimer.cancel();
            mLiveTrackerApiCallTimer = null;
        }
    }


    /**
     * Call live tracking api after every given time frame
     *
     * @param numberOfMs time interval to call API
     */
    private void liveTrackerCallApiTimer(int numberOfMs) {
        mLiveTrackerApiCallTimer = new CountDownTimer(numberOfMs, DateUtils.SECOND_IN_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                getNearByPlacesFromCustomApi(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            }
        };
        mLiveTrackerApiCallTimer.start();
    }


    /**
     * this function is called to pass result(data) to calling activity
     * @param resultCode code used to verify the result(success,failure)
     * @param jsonObject json object(data) sending to activity
     */
    private void setResultToCallingActivity(int resultCode, JSONObject jsonObject) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable(MAP_LOCATION_MODEL, mLocationMapModel);
        bundle.putString(EXTRA_MAP_RESULT_CALLBACK, jsonObject.toString());
        intent.putExtras(bundle);
        setResult(resultCode, intent);
        finish();
    }


    /**
     * set data to json object
     *
     * @return
     */
    private JSONObject getMapSelectLocationResult() {

        JSONObject jsonObjResponse = new JSONObject();
        try {
            jsonObjResponse.put(ADDRESS, clickAddress);
            jsonObjResponse.put(LAT, clickLatitude);
            jsonObjResponse.put(LONG, clickLongitude);

            if (jsonObjAddress != null) {
                jsonObjResponse.put(FORMATTED_ADDRESS, jsonObjAddress);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObjResponse;
    }


    /**
     * set address to auto complete edit text
     *
     * @param latLng LatLng object to fetch the address
     */
    private void setAddressToAutoCompleteEditTextView(LatLng latLng) {
        Geocoder gc = new Geocoder(context, Locale.ENGLISH);
        if (gc.isPresent()) {
            try {
                List<Address> addresses;
                addresses = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if (addresses.size() > 0) {
                    Address fetchedAddress = addresses.get(0);
                    StringBuilder strAddress = new StringBuilder();
                    for (int i = 0; i <= fetchedAddress.getMaxAddressLineIndex(); i++) {
                        strAddress.append(fetchedAddress.getAddressLine(i)).append(" ");
                    }

                    clickAddress = strAddress.toString();
                    clickLatitude = latLng.latitude;
                    clickLongitude = latLng.longitude;

                    addOrMoveSelectedLocationMarker(latLng);

                    searchAddressAutoCompleteText.setText(strAddress);

                    setToolbarTitle(strAddress.toString());

                    searchAddressAutoCompleteText.dismissDropDown();

                    if (showOnlyMyLocationCard) {
                        auto_complete_text_my_location.setText(strAddress);
                        auto_complete_text_my_location.dismissDropDown();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * get location details in object and set it to json object
     *
     * @param responseJsonObj json object containing location details
     * @return
     */
    private LocationMapModel getLocationMapObject(JSONObject responseJsonObj) {
        LocationMapModel locationMapModel = new LocationMapModel();
        try {
            locationMapModel.setPageTitle(Utility.getStringObjectValue(responseJsonObj, PAGE_TITLE));
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

            //For navigation from current location
            locationMapModel.setIsNavigation(Utility.getJsonObjectIntValue(responseJsonObj, IS_NAVIGATION));
            locationMapModel.setIsNavigationOn(Utility.getJsonObjectIntValue(responseJsonObj, IS_NAVIGATION_ON));

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

            locationMapModel.setIsNavFromCurLoc(Utility.getJsonObjectIntValue(responseJsonObj, IS_NAV_FROM_CUR_LOC));

            locationMapModel.setQueryString(Utility.getStringObjectValue(responseJsonObj, QUERY_STRING));

            locationMapModel.setmDestLatitude(Utility.getJsonObjectDoubleValue(responseJsonObj, M_DEST_LATITUDE));
            locationMapModel.setmDestLongitude(Utility.getJsonObjectDoubleValue(responseJsonObj, M_DEST_LONGITUDE));

            locationMapModel.setOpenMapApp(Utility.getJsonObjectBooleanValue(responseJsonObj, OPEN_MAP_APP));
            locationMapModel.setIsShowDirection(Utility.getJsonObjectBooleanValue(responseJsonObj, IS_SHOW_DIRECTIONS) ? 1 : 0);
            locationMapModel.setIsPlotAddressLocation(Utility.getJsonObjectBooleanValue(responseJsonObj, IS_PLOT_ADDRESS_LOCATION));

            locationMapModel.setAddressString(Utility.getStringObjectValue(responseJsonObj, ADDRESS_STRING));

            locationMapModel.setResponseData(responseJsonObj.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return locationMapModel;
    }


    /**
     * set zoom on given rout
     *
     * @param route
     */
    private void setZoomMapForRoute(DirectionsRoute route) {

        try {

            LatLng sourceLatLang = new LatLng(route.legs[overview].startLocation.lat,
                    route.legs[overview].startLocation.lng);
            LatLng destLatLng = new LatLng(route.legs[overview].endLocation.lat,
                    route.legs[overview].endLocation.lng);
            // Zooming Control On MAP for the All Markers show on screen
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            builder.include(sourceLatLang);
            builder.include(destLatLng);

            LatLngBounds bounds = builder.build();

            int width = mMap.getWidth();
            int height = mMap.getHeight();
            int padding = (int) (width * 0.30); // offset from edges of the map 10% of screen  0.10

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, (int) Utility.convertDpToPixel(40, this));

            mGoogleMap.moveCamera(cu);
            mGoogleMap.animateCamera(cu);

        } catch (IllegalStateException iex) {
            iex.printStackTrace();
        }
    }


    /**
     * set zoom on rout for given latitude and longitude
     * @param sourceLatLang latitude and longitude of source point
     * @param destLatLng latitude and longitude of destination point
     */
    private void setZoomMapForRoute(LatLng sourceLatLang, LatLng destLatLng) {

        try {
            // Zooming Control On MAP for the All Markers show on screen
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            builder.include(sourceLatLang);
            builder.include(destLatLng);

            LatLngBounds bounds = builder.build();

            int width = mMap.getWidth();
            int height = mMap.getHeight();
            int padding = (int) (width * 0.30); // offset from edges of the map 10% of screen  0.10

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, (int) Utility.convertDpToPixel(40, this));

            mGoogleMap.moveCamera(cu);
            mGoogleMap.animateCamera(cu);

        } catch (IllegalStateException iex) {
            iex.printStackTrace();
        }
    }

    /**
     * set navigation drawer webview.
     * Enabled JavaScript
     * set debugging for webview
     * add Javascript Interface
     * Set WebView Client
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void setNavDrawerWebView(String url) {
        mWebViewNavDrawer = (WebView) findViewById(R.id.navdrawerWebview);
        WebSettings settings = mWebViewNavDrawer.getSettings();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        settings.setJavaScriptEnabled(true);
        setupDebuggingModeForWebView();

        mWebViewNavDrawer.addJavascriptInterface(new WebAppJavascriptInterface(this), "Android");
        settings.setDomStorageEnabled(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }

        mWebViewNavDrawer.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        mWebViewNavDrawer.setWebChromeClient(new WebChromeClient() {
        });

        if (Build.VERSION.SDK_INT >= 19) {
            // chromium, enable hardware acceleration
            mWebViewNavDrawer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            mWebViewNavDrawer.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mWebViewNavDrawer.loadUrl(Uri.decode(url));
    }


    /**
     * Enable debugging for webview
     */
    private void setupDebuggingModeForWebView() {
        // debug web view on chrome
        if (BuildConfig.DEBUG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
            }
        }
    }


    /**
     * Ckeck if overlay is open
     * @return
     */
    private boolean isOverlayOpen() {
        return mWebviewConfirm != null && mWebviewConfirm.getVisibility() == View.VISIBLE;
    }


    /**
     * Show or hide overlay based on it's current state
     * @param isShow boolen value to show or hide overlay(true,false)
     */
    private void showOrHideOverlay(boolean isShow) {

        if (mWebviewConfirm != null) {
            mWebviewConfirm.setVisibility(isShow ? View.VISIBLE : View.GONE);
        }

        if (isShow) {


            findViewById(R.id.overlay_layout).setVisibility(
                    Utility.getJsonObjectBooleanValue(mResponseJson, IS_SPLIT_OVERLAY)
                            ? View.GONE
                            : View.VISIBLE);
        } else {

            setOverlayStateToCollapse(AnchorBottomSheetBehavior.STATE_COLLAPSED);

        }
    }


    /**
     * set maximum height to bottom sheet
     * @param peekHeight definite height of bottomsheet
     */
    public void setPeekHeightToBottomSheet(int peekHeight) {
        anchorBottomSheetBehavior.setPeekHeight(peekHeight);
    }


    /**
     * set BottomSheet state/height
     * @param state
     */
    private void setOverlayStateToCollapse(int state) {

        boolean keepOverlayAtBottom = Utility.getJsonObjectBooleanValue(mResponseJson, KEEP_OVERLAY_AT_BOTTOM);


        if (isMapOverlayHideOnSwipe) {
            keepOverlayAtBottom = !isMapOverlayHideOnSwipe;
        }

        //        Hide bottom sheet if keepOverlayAtBottom = false else stick it to bottom.
        if (!keepOverlayAtBottom)
            setPeekHeightToBottomSheet(0);

        anchorBottomSheetBehavior.setHideable(!keepOverlayAtBottom);
        anchorBottomSheetBehavior.setState(state);
    }


    /**
     * Check if navigation drawer is open
     * @return
     */
    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.LEFT);
    }


    /**
     * close avigation drawer
     */
    public void closeNavDrawer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDrawerLayout != null) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }
            }
        });

    }


    /**
     * Disable drawer
     * set DrawerLockMode to locked
     */
    public void disableDrawer() {
        if (mDrawerLayout != null)
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }


    /**
     * Enable drawer
     * set DrawerLockMode to unlock
     */
    public void enableDrawer() {
        if (mDrawerLayout != null)
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }


    /**
     * Set up drawer
     * initialize, set listener to drawer
     */
    private void setupMenuDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        boolean mIsMenuLoad = Utility.getJsonObjectBooleanValue(mResponseJson, IS_MENU_SHOW);
        String menuPageName = Utility.getStringObjectValue(mResponseJson, MENU_PAGE);

        if (TextUtils.isEmpty(menuPageName) || !mIsMenuLoad) {
            disableDrawer();
            return;
        }

        setNavDrawerWebView(getCompleteUrl(menuPageName));

        if (mDrawerToggle == null) {

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.string.app_name, R.string.app_name) {
                public void onDrawerClosed(View view) {
                }

                public void onDrawerOpened(View drawerView) {

                    Utility.hideSoftKeyboard(MapsAppCompactActivity.this, searchAddressAutoCompleteText.getWindowToken());
                }

                public void onDrawerSlide(View drawerView, float slideOffset) {
                }

                public void onDrawerStateChanged(int newState) {

                }

            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }

        mDrawerToggle.syncState();

    }


    /**
     * Cancle sending current latitude and longitude to Api
     */
    private void cancelSendCurLatLongCallApiTimer() {
        if (mSendCurLatLongInTimeInterval != null) {
            mSendCurLatLongInTimeInterval.cancel();
            mSendCurLatLongInTimeInterval = null;
        }
    }


    /**
     * sending current latitude and longitude to Api
     *
     * @param numberOfMs Time interval to call API
     */
    private void liveSendCurLatLongCallApiTimer(final int numberOfMs) {
        mSendCurLatLongInTimeInterval = new CountDownTimer(numberOfMs * 1000, DateUtils.SECOND_IN_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                if (!isOnline) {
                    cancelSendCurLatLongCallApiTimer();
                }

                if (location != null) {

                    UpdateCurrentLocationClientEvent.callUpdateCurrentLocationAPI(MapsAppCompactActivity.this,
                            location.getLatitude(), location.getLongitude(),
                            updateLocationApi, status, new IWebSocketClientEvent() {
                                @Override
                                public void onFinish(Error error, Object process, WebSocketClientEvent socketClientEvent) {
                                    cancelSendCurLatLongCallApiTimer();
                                    liveSendCurLatLongCallApiTimer(numberOfMs);
                                }
                            }, false, new JSONObject());
                } else {
                    liveSendCurLatLongCallApiTimer(numberOfMs);
                }
            }
        };
        mSendCurLatLongInTimeInterval.start();
    }

    @Override
    public void handleNewLocation(Location location) {
        try {
            mCurrentLocation = location;
            //Give current location call to webview
            currentLocationCallback(location);
//            Show current location on header view.
            if (!TextUtils.isEmpty(Utility.getStringObjectValue(mResponseJson, SELECTED_LOCATION_FROM_SEARCH))) {
                StringBuilder address = mLocationProvider.getCompleteAddressString(Utility.getJsonObjectDoubleValue(mResponseJson, LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG));
                setToolbarTitle(address.toString());
                loadLatLongOnMap(new LatLng(Utility.getJsonObjectDoubleValue(mResponseJson, LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG)));
            } else {
                mLocationProvider.getCompleteAddressString(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), new LocationProvider.OnCapturedLocationString() {
                    @Override
                    public void onCapturedAddress(String addressString) {
                        clickLatitude = mCurrentLocation.getLatitude();
                        clickLongitude = mCurrentLocation.getLongitude();
                        setToolbarTitle(addressString);

                    }
                });
            }

            if (isOnline && !TextUtils.isEmpty(updateLocationApi)) {

                if (isStartSendUpdate) {
                    UpdateCurrentLocationClientEvent.callUpdateCurrentLocationAPI(MapsAppCompactActivity.this,
                            location.getLatitude(), location.getLongitude(),
                            updateLocationApi, status, new IWebSocketClientEvent() {
                                @Override
                                public void onFinish(Error error, final Object process, WebSocketClientEvent socketClientEvent) {
                                    liveSendCurLatLongCallApiTimer(timerInterval);
                                }
                            }, false, new JSONObject());

                    isStartSendUpdate = false;
                }
            } else {
                cancelSendCurLatLongCallApiTimer();
                isStartSendUpdate = true;
                if (mLocationProvider != null) {
                    mLocationProvider.stopLocationUpdates();
                }
            }

            if (Utility.getJsonObjectBooleanValue(mResponseJson, CAN_EDIT_MY_LOCATION_ON_CARD)) {
                loadLatLongOnMap(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
//                Show destination address
                showDestinationAddress(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mLocationProvider.stopLocationUpdates();
                return;
            }
            if (mLocationMapModel.getIsPlotAddressLocation()) return;

            if (mLocationMapModel.getIsPlotLocation() == 1) return;


            if (Utility.getJsonObjectBooleanValue(mResponseJson, IS_OTHER_NEARBY_LOCATION)) {
                if (isLoadedForFirstTime) {
                    loadLatLongOnMap(new LatLng(Utility.getJsonObjectDoubleValue(mResponseJson, LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG)));
                    isLoadedForFirstTime = false;
                }

                return;
            } else {
                if (!TextUtils.isEmpty(Utility.getStringObjectValue(mResponseJson, SELECTED_LOCATION_FROM_SEARCH))) {
                    StringBuilder address = mLocationProvider.getCompleteAddressString(Utility.getJsonObjectDoubleValue(mResponseJson, LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG));
                    setToolbarTitle(address.toString());
                    loadLatLongOnMap(new LatLng(Utility.getJsonObjectDoubleValue(mResponseJson, LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG)));
                } else
                    loadLatLongOnMap(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            }

            if (location != null) {
                mCurrentLocation = location;

                //Handling draw route and showing current marker as movable depending on device current location
                handlingNavigation(location);

                if (mIsCurrentFirst && mLocationMapModel.getIsShowCurrentMarker() == 1) {
                    addOrMoveSelectedLocationMarker(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                    loadLatLongOnMap(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                }

                if (location != null
                        && mIsCurrentFirst
                        && mLocationMapModel.getIsLoadNearByPlaces() == 1) {
                    mIsCurrentFirst = false;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (!TextUtils.isEmpty(Utility.getStringObjectValue(mResponseJson, SELECTED_LOCATION_FROM_SEARCH))) {
                        StringBuilder address = mLocationProvider.getCompleteAddressString(Utility.getJsonObjectDoubleValue(mResponseJson, LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG));
                        setToolbarTitle(address.toString());
                        loadLatLongOnMap(new LatLng(Utility.getJsonObjectDoubleValue(mResponseJson, LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG)));
                    } else
                        loadLatLongOnMap(latLng);

                    getNearByPlacesFromCustomApi(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

                }

                //Updated current location
                if (mLocationMapModel.getIsLiveTracking() == 1) {
                    addOrMoveSelectedLocationMarker(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                }

                if (mLocationMapModel.getIsSelectLocation() == 1) {
                    mLocationProvider.stopLocationUpdates();
//            Load location model lat/lng on map and move camera to there.
                    if (Utility.getJsonObjectDoubleValue(mResponseJson, LAT) > 0 &&
                            Utility.getJsonObjectDoubleValue(mResponseJson, LONG) > 0) {
                        loadLatLongOnMap(new LatLng(Utility.getJsonObjectDoubleValue(mResponseJson, LAT), Utility.getJsonObjectDoubleValue(mResponseJson, LONG)));
                    } else {
                        loadLatLongOnMap(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                    }
                }

                if (showOnlyMyLocationCard) {
                    mLocationProvider.getCompleteAddressString(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), new LocationProvider.OnCapturedLocationString() {
                        @Override
                        public void onCapturedAddress(String addressString) {
                            auto_complete_text_my_location.setText(addressString);
                        }
                    });
                    auto_complete_text_my_location.dismissDropDown();
                    mLocationProvider.stopLocationUpdates();
                }

                if (mLocationMapModel.getIsNavigationOn() == 1) {
                }

//                Handle new location for Fix my ride.
                if (mLocationMapModel.getIsNavFromCurLoc() == 1) {
                    showMyLocationAddress(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Get the last known location
     * @param location
     */
    @Override
    public void handleLastLocation(Location location) {
        if (location != null) {
            this.location = location;
            handleNewLocation(location);
        }
    }


    /**
     * Class provides the facility to create any method as the javascript Interface which means that
     * method can be used by web components to communicate with Android.
     */
    public class WebAppJavascriptInterface {
        Context mContext;

        WebAppJavascriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void getCurrentLatLong(final String currentLatLong) {

            try {
                LocationDetails locationDetails = new LocationDetails(mWebviewConfirm, context, MapsAppCompactActivity.this);
                locationDetails.getCurLocationLatLong(currentLatLong);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void updateMarker(final String responseData) {
            try {
                requiredJSONObjectKey = new String[]{IS_SHOW_WAZE};
                String missingKeysMsg = Utility.showAlertBridgeMissingKeys( responseData, requiredJSONObjectKey);
                if (whileDebuggingShowMissingAlert && !missingKeysMsg.equals(missingKeys) && BuildConfig.DEBUG) {
                    Utility.showAlertMessage(context, missingKeysMsg, UPDATE_MARKER);
                    return;
                }

                parseLoadMapViewByConfig(responseData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void redirectPage(final String redirectPage) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        requiredJSONObjectKey = new String[]{""};
                        String missingKeysMsg = Utility.showAlertBridgeMissingKeys( redirectPage, requiredJSONObjectKey);
                        if (whileDebuggingShowMissingAlert && !missingKeysMsg.equals(missingKeys) && BuildConfig.DEBUG) {
                            Utility.showAlertMessage(context, missingKeysMsg, REDIRECT_PAGE);
                            return;
                        }

                        setResultToCallingActivity(Activity.RESULT_OK, new JSONObject(redirectPage));
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            });

        }

        @JavascriptInterface
        public void getLocalImage(final String appData) {

        }


        @JavascriptInterface
        public void getNetworkStatus(final String targetData) {

        }
    }


    /**
     * parse response data
     * @param launchMapViewRes Responce data to parse
     */
    private void parseLoadMapViewByConfig(String launchMapViewRes) {
        try {
            JSONObject responseJsonObj = new JSONObject(launchMapViewRes);

            appAuthTokenNew = Utility.getStringObjectValue(responseJsonObj, TOKEN_KEY);
            appSecretKeyNew = Utility.getStringObjectValue(responseJsonObj, SECRET_KEY);

            final LocationMapModel locationMapModel = getLocationMapObject(responseJsonObj);
            isShowWase = Utility.getJsonObjectIntValue(responseJsonObj, IS_SHOW_WAZE);

            if (locationMapModel != null) {
                locationMapModel.setResponseData(launchMapViewRes);

                if (locationMapModel.isOpenMapApp()) {
                    //open google default map application
                    if (locationMapModel.getIsShowDirection() == 1) {

                        if (!TextUtils.isEmpty(locationMapModel.getAddressString())) {

                            destinationAddress = locationMapModel.getAddressString();
                            if (location != null) {

                            } else {
                                if (isGPSInfo()) {
                                    if (isGooglePlayServicesAvailable()) {
                                        mLocationProvider.connect();
                                    }

                                } else {
                                }
                            }
                        } else {
                            Utility.openGoogleMapDirection(MapsAppCompactActivity.this, locationMapModel.getLatitude(),
                                    locationMapModel.getLongitude(), locationMapModel.getDestLatitude(),
                                    locationMapModel.getDestLongitude());
                        }

                    } else if (locationMapModel.getIsPlotAddressLocation() || !TextUtils.isEmpty(locationMapModel.getAddressString())) {
                        Utility.openInExternalMapByAddress(this, locationMapModel.getAddressString());
                    } else {
                        Utility.openInExternalMapByLatLong(MapsAppCompactActivity.this, locationMapModel.getLatitude(), locationMapModel.getLongitude());
                    }

                } else {
                    parseNearByPlacesResponse(responseJsonObj, "");
                }

                JSResponseData jsResponseData = new JSResponseData();
                jsResponseData.setResponseData(launchMapViewRes);
                jsResponseData.setLocationMapModel(locationMapModel);
                setJsResponseData(jsResponseData);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Set the parsed response data to json object
     *
     * @param jsResponseData Parsed json response data
     */
    public void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }

}
