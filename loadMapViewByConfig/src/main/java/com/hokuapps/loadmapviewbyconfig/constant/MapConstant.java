package com.hokuapps.loadmapviewbyconfig.constant;

public class MapConstant {
    public static final String MAP_LOCATION_MODEL = "mapLocationModel";
    public static final String API_KEY = "";

    public static String AUTH_TOKEN = "";
    public static int THEME_ID = 0;
    public static String AUTH_SECRET_KEY = "";
    public static final String FOLDER_NAME_WEB_HTML = "WebHtml";
    public static final String EXTRA_MAP_RESULT_CALLBACK = "extra_result_callback";
    public static int UPDATE_INTERVAL = 5000; // 10 sec 10000
    public static int FASTEST_INTERVAL = 2000; // 05 sec 5000
    public static int DISPLACEMENT = 0;       // 10 meters
    public static final int INVALID_ID = -1;
    public static final int DEVICE_TYPE = 2; //for android

    public static String APPLICATION_ID = "";

    public static boolean LOAD_HTML_DIRECTLY = true;


    public interface AuthIO {
        String AUTH_TOKEN = "token";
        String STATUS_CODE = "statusCode";
    }

    public interface ChatIO {
        String EVENT_REGISTER_APP_TOKEN = "registerAppToken";

    }

    public interface AuthorizationParams {
        String EVENT_LOGIN = "loginV2";
        String EVENT_REFRESH_TOKEN_V2 = "refreshTokenV2";
        String EVENT_UPDATE_PROFILE_V2 = "updateProfileV2";

    }

    public interface themeId {
        int THEME_DEFAULT = 1;
        int THEME_GRAY = 2;
        int THEME_DARK_BLUE = 3;
        int THEME_RED = 4;
        int THEME_PURPLE = 5;
        int THEME_CYNA = 6;
        int THEME_GREEN = 7;
        int THEME_BLUE = 8;
        int THEME_PINK = 9;
        int THEME_YELLOW = 10;
        int THEME_DARK = 11;
    }

    public interface google_location_blue_pointer {
        // integer values for the blue pointer location on the google map to show the current location pointer
        String LOCATION_ONE = "1";
        String LOCATION_TWO = "2";
    }

    public interface googlePlace {

        // google Map places Near by you..

        String PLACE_RESPONSE_RESULT = "result";
        String PLACE_GEOMETRY = "geometry";
        String PLACE_LOCATION = "location";
        String PLACE_LAT = "lat"; // latitude of the location for google map
        String PLACE_LNG = "lng"; // longitude of the location for google map
        String SOURCE = "Source";
        String DESTINATION = "Destination";
    }

    public interface google_DirectionsUrl {
        // this is link of the google api for the map the route on the source and destination location
        String BASE_URL = "https://maps.googleapis.com/maps/api/directions/";

    }

    public interface google_LatLongFromPlaceId {
        // google api link for getting the Lat and Long of the place
        String BASE_PATH_lAT_LONG_FROM_PLACE_ID = "https://maps.googleapis.com/maps/api/place/details/json?placeid=";
    }

    public interface google_NearBy_place {
        // getting the google api link for the places list of the near by places
        String BASE_PATH_NEAR_BY_PLACE = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    }

    public interface TeacherPlace {
        String DATA = "data";
        String MARKER_DATA = "markerData";
        String COORDINATES = "coordinates";
        String PLACE_ICON = "icon"; // marker of the location for the google map

    }

    public interface Keys {
        String PAGE_TITLE = "pageTitle";
        String NEXT_BUTTON_CALLBACK = "nextButtonCallback";
        String CANCEL_BUTTON_CALLBACK = "cancelButtonCallback";
        String COLOR_CODE = "colorCode";
        String IS_LOAD_NEAR_BY_PLACES = "isLoadNearByPlaces";
        String RADIUS = "radius";
        String IS_SHOW_TAB = "isShowTab";
        String TAB_1 = "tab1";
        String TAB_2 = "tab2";
        String SEARCH_PLACEHOLDER = "searchPlaceholder";
        String SEARCH_TITLE = "searchTitle";
        String IS_MARKER_CLICK_CALLBACK = "isMarkerClickCallback";
        String MAP_ZOOM_LEVEL = "mapZoomLevel";
        String IS_SHOW_BOTTOM_BUTTON = "isShowBottomButton";
        String BOTTOM_BUTTON_TEXT = "bottomButtonText";
        String IS_SELECT_LOCATION = "isSelectLocation";
        String IS_LIVE_TRACKING = "isLiveTracking";
        String LIVE_TRACKING_INTERVAL_IN_MS = "liveTrackingIntervalInMs";
        String IS_SHOW_CURRENT_MARKER = "isShowCurrentMarker";
        String IS_REQUEST_GUARD = "isRequestGaurd";
        String IS_NAVIGATION = "isNavigation";
        String IS_NAVIGATION_ON = "isNavigationOn";
        String LATITUDE = "Latitude";
        String LONGITUDE = "Longitude";
        String DEST_LATITUDE = "DestLatitude";
        String DEST_LONGITUDE = "DestLongitude";
        String API_NAME = "apiName";
        String RECORD_ID = "recordID";
        String LOCATION_TITLE = "locationTitle";
        String LOCATION_ADDRESS = "locationAddress";
        String MARKER_IMAGE_URL = "markerImageURL";
        String OVERLAY_PAGE = "overlayPage";
        String IS_SHOW_OVERLAY = "isShowOverlay";
        String OVERLAY_SIZE = "overlaySize";
        String IS_MARKER_CLICK_SHOW_OVERLAY = "isMarkerClickShowOverlay";
        String IS_SEARCH_AUTO_COMPLETE = "isSearchAutoComplete";
        String IS_ADMIN_LOGIN = "isAdminLogin";
        String IS_TRACKING = "isTracking";
        String IS_PLOT_LOCATION = "isPlotLocation";
        String IS_NAV_FROM_CUR_LOC = "isNavFromCurLoc";
        String QUERY_STRING = "querystring";
        String M_DEST_LATITUDE = "mDestLatitude";
        String M_DEST_LONGITUDE = "mDestLongitude";
        String OPEN_MAP_APP = "openMapApp";
        String IS_SHOW_DIRECTIONS = "isShowDirections";
        String IS_PLOT_ADDRESS_LOCATION = "isPlotAddressLocation";
        String ADDRESS_STRING = "addressString";
        String SHOW_SEARCHBAR = "showSearchbar";
        String SEARCH_BY_COUNTRIES_LIST = "searchByCountriesList";
        String MARKER_DATA = "markerData";
        String FILE_NAME = "fileName";
        String OFFLINE_DATA_ID = "offlineDataID";
        String CAPTION = "caption";
        String COUNTRY_NAME = "countryName";
        String COUNTRY_CODE = "countryCode";
        String LOCALITY = "locality";
        String SUB_LOCALITY = "subLocality";
        String SUB_ADMIN_AREA = "subAdminArea";
        String POSTAL_CODE = "postalCode";
        String ADMIN_AREA = "adminArea";
        String SHOW_ONLY_MY_LOCATION_CARD = "showOnlyMyLocationCard";
        String SOURCE_TITLE = "sourceTitle";
        String DESTINATION_TITLE = "destinationTitle";
        String SHOULD_SHOW_LOCATION_CARD = "shouldShowLocationCard";
        String START_LOCATION_NAME = "startLocationName";
        String DESTINATION_LOCATION_NAME = "destinationLocationName";
        String CAN_EDIT_MY_LOCATION_ON_CARD = "canEditMyLocationOnCard";
        String HEADER_BUTTON_COLOR = "headerButtonColor";
        String SOURCE_LOCATION = "sourceLocation";
        String LAT = "lat";
        String LONG = "long";
        String DESTINATION_LOCATION = "destinationLocation";
        String SOURCE_MARKER_ICON = "sourceMarkerIcon";
        String DEST_MARKER_ICON = "destMarkerIcon";
        String MAP_ROUTE_COLOR = "mapRouteColor";
        String IS_MAP_OVERLAY_HIDE_ON_SWIPE = "isMapOverlayHideOnSwipe";
        String IS_MENU_SHOW = "isMenuShow";
        String HEADER_LOGO = "headerLogo";
        String IS_SPLIT_OVERLAY = "isSplitOverlay";
        String API_PARAMETERS = "apiParameters";
        String TOKEN_KEY = "tokenKey";
        String SECRET_KEY = "secretKey";
        String QUERY_MODE = "queryMode";
        String REQUEST_ID = "requestid";
        String ICON = "icon";
        String LOAD_PLACES_AND_ROUTE_BOTH = "loadPlacesAndRouteBoth";
        String MARKER_POPUP_TEXT = "markerPopupText";
        String ADDRESS = "address";
        String FORMATTED_ADDRESS = "formattedAddress";
        String KEEP_OVERLAY_AT_BOTTOM = "keepOverlayAtBottom";
        String MENU_PAGE = "menuPage";
        String SELECTED_LOCATION_FROM_SEARCH = "selectedLocationFromSearch";
        String IS_OTHER_NEARBY_LOCATION = "isOtherNearbyLocation";
        String IS_SHOW_WAZE = "isShowWaze";
        String UPDATE_MARKER = "updateMarker";
        String REDIRECT_PAGE = "redirectPage";
        String POINTS = "points";
        String OVERLAY_BAR_COLOR = "overlaybarcolor";
        String OVERLAY_HANDLER_COLOR = "overlayhandlercolor";
        String SHOW_OVERLAY_BAR = "showoverlaybar";
        String IS_SHOW_ANYTHING_SEARCH_LAYOUT = "isShowAnythingSearchLayout";
        String SEARCH_ANYTHING_HINT = "searchAnythingHint";
        String OVERLAY_HEIGHT = "overlayheight";
        String IS_WRAP_CONTENT = "isWrapContent";
        String BOTTOM_BUTTON_ICON_NAME = "bottomButtonIconName";
        String CAN_SEARCH_LOCATION = "canSearchLocation";
        String FILTER_ICON = "filterIcon";
        String IS_GRAY_COLOR = "isGrayColor";
        String SEARCH_ICON = "searchIcon";
        String HEADER_BUTTONS = "headerButtons";
        String SHOW_FULL_OVERLAY_WITH_BUTTONS = "showFullOverlayWithButtons";
        String TOGGLE_OVERLAY_LIST = "toggleoverlaylist";
        String IS_FILTER_MENU_CLICK = "isFilterMenuClick";
        String IS_RIGHT_MENU_CLICK = "isRightMenuClick";
        String DATA = "data";
        String DEVICE_ID = "deviceid";
        String DEVICE_TYPE = "devicetype";
        String DEVICE_NAME = "devicename";
        String TIMESTAMP = "timestamp";
        String BV = "bv";
        String COUNTRY = "country";
        String STATE = "state";
        String NAME = "name";
        String CITY = "city";
        String DRIVER_ID_NAME ="driverid_name";
        String IU_NUMBER = "iuNumber";
        String RIGHT_MENU = "rightMenu";
        String BRIDGE_DATA = "bridgeData";
        String LOCATION_DICT = "locationDict";
        String IS_SHOW_FLOATING_PIN = "isShowFloatingPin";
        String SCROLL = "scroll";
        String TOGGLE_BODY_SCROLL ="toggleBodyScroll";
        String ON_MARKER_CLICK = "onMarkerClick";
        String HEADER_CONTENT_START = "headercontentstart";
        String HEADER_TEXT_COLOR = "headerTextColor";
        String PREDICTIONS = "predictions";
        String LOAD_STATIC_MAP_FROM_POINTS = "loadStaticMapFromPoints";
        String READ_ONLY = "readOnly";
        String IS_REDIRECT_TO_PAGE = "isRedirectToPage";
        String SEARCH_ANYTHING_CALL_BACK = "searchAnythingCallBack";

        String STRUCTURED_FORMATTING = "structured_formatting";
        String SECONDARY_TEXT = "secondary_text";
        String PLACE_ID = "place_id";
        String MAIN_TEXT = "main_text";
        String STATUS = "status";

    }
}
