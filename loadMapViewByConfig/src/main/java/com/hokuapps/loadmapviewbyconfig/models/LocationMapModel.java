package com.hokuapps.loadmapviewbyconfig.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.android.gms.maps.GoogleMap;


/**
 * Created by user on 21/12/16.
 */

@SuppressWarnings("ALL")
public class LocationMapModel implements Parcelable {

    public static final Creator<LocationMapModel> CREATOR = new Creator<LocationMapModel>() {

        public LocationMapModel createFromParcel(Parcel source) {

            LocationMapModel locationMapModel = new LocationMapModel();
            locationMapModel.pageTitle = source.readString();
            locationMapModel.nextButtonTitle = source.readString();
            locationMapModel.isReadOnly = source.readInt();
            locationMapModel.nextRedirectionURL = source.readString();
            locationMapModel.nextButtonCallback = source.readString();
            locationMapModel.offlineDataID = source.readString();
            locationMapModel.responseData = source.readString();
            locationMapModel.latitude = source.readDouble();
            locationMapModel.longitude = source.readDouble();
            locationMapModel.mDestLatitude = source.readDouble();
            locationMapModel.mDestLongitude = source.readDouble();
            locationMapModel.destLatitude = source.readDouble();
            locationMapModel.destLongitude = source.readDouble();
            locationMapModel.appID = source.readString();
            locationMapModel.colorCode = source.readString();
            locationMapModel.cancelButtonCallback = source.readString();
            locationMapModel.isNavigation = source.readInt();
            locationMapModel.isLoadNearByPlaces = source.readInt();

            locationMapModel.addressString = source.readString();
            locationMapModel.mapType = source.readInt();
            locationMapModel.getMapSnapShot = source.readInt();
            locationMapModel.openMapApp = source.readInt();
            locationMapModel.mapFileName = source.readString();
            locationMapModel.mapSrcName = source.readString();
            locationMapModel.isShowCurrentLoc = source.readInt();
            locationMapModel.isWithoutEditor = source.readInt();
            locationMapModel.isShowTab = source.readInt();
            locationMapModel.isShowOverlay = source.readInt();
            locationMapModel.isSelectLocation = source.readInt();
            locationMapModel.isShowBottomButton = source.readInt();
            locationMapModel.bottomButtonText = source.readString();
            locationMapModel.nearRadius = source.readInt();
            locationMapModel.isLiveTracking = source.readInt();
            locationMapModel.liveTrackingIntervalInMs = source.readInt();
            locationMapModel.isShowCurrentMarker = source.readInt();
            locationMapModel.isRequestGaurd = source.readInt();
            locationMapModel.isNavigationOn = source.readInt();
            locationMapModel.apiName = source.readString();
            locationMapModel.recordID = source.readString();
            locationMapModel.locationTitle = source.readString();
            locationMapModel.locationAddress = source.readString();
            locationMapModel.tab1 = source.readString();
            locationMapModel.tab2 = source.readString();
            locationMapModel.searchTitle = source.readString();
            locationMapModel.searchPlaceholder = source.readString();
            locationMapModel.isMarkerClickCallback = source.readInt();
            locationMapModel.mapZoomLevel = source.readInt();
            locationMapModel.mapPinIcon = source.readString();
            locationMapModel.overlayPage = source.readString();
            locationMapModel.overlaySize = source.readInt();
            locationMapModel.isMarkerClickShowOverlay = source.readInt();
            locationMapModel.isSearchAutoComplete = source.readInt();
			locationMapModel.isAdminLogin = source.readInt();
			locationMapModel.isTracking = source.readInt();
			locationMapModel.isPlotLocation = source.readInt();
			locationMapModel.queryString = source.readString();
			locationMapModel.isNavFromCurLoc = source.readInt();
			locationMapModel.isPlotAddressLocation = source.readInt();
            locationMapModel.showSearchbar = source.readInt();
            locationMapModel.markerData = source.readInt();
            locationMapModel.searchByCountriesList = source.readString();
            return locationMapModel;
        }

        public LocationMapModel[] newArray(int size) {
            return new LocationMapModel[size];
        }
    };
    private String pageTitle;
    private String nextButtonTitle;
    private int isReadOnly;
    private String nextRedirectionURL;
    private String nextButtonCallback;
    private String offlineDataID;
    private String responseData;
    private String appID;
    private double longitude;
    private double latitude;
	
	private double mDestLatitude;
    private double mDestLongitude;

    private double destLatitude;
    private double destLongitude;
    private String colorCode;

    // new variables need to from function
    private String cancelButtonCallback;
    private String addressString;
    private int mapType = 2;
    private int getMapSnapShot = 1;
    private int openMapApp = 0;
    private int isShowDirection = 0;

    private String mapFileName;

    private String mapSrcName;
    private int isShowCurrentLoc;
    private int isWithoutEditor;

    //Default value set to 0, so that until required navigation functionality will not start.
    private int isNavigation = 0;
    private int isNavFromCurLoc = 0;
    private int isNavigationOn = 0;
    private int isLoadNearByPlaces = 0;
    private int isShowTab = 0;
    private int isShowBottomButton = 0;
    private String bottomButtonText = "";
    private int isShowOverlay = 0;
    private int isSelectLocation = 0;
    private int nearRadius = 10000;
    private int isLiveTracking = 0;
    private int isTracking = 0;
    private int isPlotLocation = 0;
    private int liveTrackingIntervalInMs = 0;
    private int isShowCurrentMarker = 0;
    private int isRequestGaurd = 0;
    private int isMarkerClickCallback = 0;
    private int mapZoomLevel = 0;

    //for api and response keys
    private String apiName ="";
    private String recordID ="";
    private String locationTitle ="";
    private String locationAddress ="";
    private String tab1 ="";
    private String tab2 ="";
    private String searchTitle ="";
    private String searchPlaceholder ="";
    private String mapPinIcon = "";

    //for overlay
    private String overlayPage = "";
    private String queryString = "";
    private int overlaySize = 0;
    private int isMarkerClickShowOverlay;
    private int isSearchAutoComplete;

	private int isAdminLogin = 0;
    private int isPlotAddressLocation = 0;
    private int showSearchbar = 0;
    private int markerData = 0;
    private String selectedLocationFromSearch ="";
    private String searchByCountriesList;

    public LocationMapModel() {
    }

    public static int getValidMapType(int mapType) {

        switch (mapType) {
            case 1:
                return GoogleMap.MAP_TYPE_NORMAL;
            case 2:
                return GoogleMap.MAP_TYPE_SATELLITE;
            case 3:
                return GoogleMap.MAP_TYPE_TERRAIN;
            case 4:
                return GoogleMap.MAP_TYPE_HYBRID;
        }

        return GoogleMap.MAP_TYPE_NORMAL;
    }

    public String getPageTitle() {
        return TextUtils.isEmpty(pageTitle) ? "" : pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getNextButtonTitle() {
        return nextButtonTitle;
    }

    public void setNextButtonTitle(String nextButtonTitle) {
        this.nextButtonTitle = nextButtonTitle;
    }

    public boolean getIsReadOnly() {
        return isReadOnly == 1 ? true : false;
    }

    public void setIsReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly ? 1 : 0;
    }

    public String getNextRedirectionURL() {
        return nextRedirectionURL;
    }

    public void setNextRedirectionURL(String nextRedirectionURL) {
        this.nextRedirectionURL = nextRedirectionURL;
    }

    public String getNextButtonCallback() {
        return nextButtonCallback;
    }

    public void setNextButtonCallback(String nextButtonCallback) {
        this.nextButtonCallback = nextButtonCallback;
    }

    public String getOfflineDataID() {
        return offlineDataID;
    }

    public void setOfflineDataID(String offlineDataID) {
        this.offlineDataID = offlineDataID;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getmDestLatitude() {
        return mDestLatitude;
    }

    public void setmDestLatitude(double mDestLatitude) {
        this.mDestLatitude = mDestLatitude;
    }

    public double getmDestLongitude() {
        return mDestLongitude;
    }

    public void setmDestLongitude(double mDestLongitude) {
        this.mDestLongitude = mDestLongitude;
    }


    public String getSearchByCountriesList() {
        return searchByCountriesList;
    }

    public void setSearchByCountriesList(String searchByCountriesList) {
        this.searchByCountriesList = searchByCountriesList;
    }

    public double getDestLatitude() {
        return destLatitude;
    }

    public void setDestLatitude(double destLatitude) {
        this.destLatitude = destLatitude;
    }

    public double getDestLongitude() {
        return destLongitude;
    }

    public void setDestLongitude(double destLongitude) {
        this.destLongitude = destLongitude;
    }

    public int getIsNavigation() {
        return isNavigation;
    }

    public void setIsNavigation(int isNavigation) {
        this.isNavigation = isNavigation;
    }

    public int getIsLoadNearByPlaces() {
        return isLoadNearByPlaces;
    }

    public void setIsLoadNearByPlaces(int isLoadNearByPlaces) {
        this.isLoadNearByPlaces = isLoadNearByPlaces;
    }


    public int getisAdminLogin() {
        return isAdminLogin;
    }

    public void setisAdminLogin(int isAdminLogin) {
        this.isAdminLogin = isAdminLogin;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getCancelButtonCallback() {
        return cancelButtonCallback;
    }

    public void setCancelButtonCallback(String cancelButtonCallback) {
        this.cancelButtonCallback = cancelButtonCallback;
    }

    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    public int getMapType() {
        return mapType;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }

    public boolean isGetMapSnapShot() {
        return getMapSnapShot == 1 ? true : false;
    }

    public void setGetMapSnapShot(boolean getMapSnapShot) {
        this.getMapSnapShot = getMapSnapShot ? 1 : 0;
    }

    public boolean isShowSearchbar() {
        return showSearchbar == 1 ? true : false;
    }

    public void setShowSearchbar(boolean showSearchbar) {
        this.showSearchbar = showSearchbar ? 1 : 0;
    }

    public boolean isMarkerData() {
        return markerData == 1 ? true : false;
    }

    public void setMarkerData(boolean markerData) {
        this.markerData = markerData ? 1 : 0;
    }

    public String getMapFileName() {
        return mapFileName;
    }

    public void setMapFileName(String mapFileName) {
        this.mapFileName = mapFileName;
    }

    public String getMapSrcName() {
        return mapSrcName;
    }

    public void setMapSrcName(String mapSrcName) {
        this.mapSrcName = mapSrcName;
    }

    public boolean isOpenMapApp() {
        return openMapApp == 1 ? true : false;
    }

    public boolean getIsShowCurrentLoc() {
        return isShowCurrentLoc == 1 ? true : false;
    }

    public void setIsShowCurrentLoc(boolean isShowCurrentLoc) {
        this.isShowCurrentLoc = isShowCurrentLoc ? 1 : 0;
    }

    public boolean getIsWithoutEditor() {
        return isWithoutEditor == 1 ? true : false;
    }

    public void setIsWithoutEditor(boolean isWithoutEditor) {
        this.isWithoutEditor = isWithoutEditor  ? 1 : 0;
    }


    public void setOpenMapApp(boolean openMapApp) {
        this.openMapApp = openMapApp ? 1 : 0;
    }

    public int getIsShowTab() {
        return isShowTab;
    }

    public void setIsShowTab(int isShowTab) {
        this.isShowTab = isShowTab;
    }

    public int getIsShowOverlay() {
        return isShowOverlay;
    }

    public void setIsShowOverlay(int isShowOverlay) {
        this.isShowOverlay = isShowOverlay;
    }

    public int getIsSelectLocation() {
        return isSelectLocation;
    }

    public void setIsSelectLocation(int isSelectLocation) {
        this.isSelectLocation = isSelectLocation;
    }

    public int getIsShowBottomButton() {
        return isShowBottomButton;
    }

    public void setIsShowBottomButton(int isShowBottomButton) {
        this.isShowBottomButton = isShowBottomButton;
    }

    public String getBottomButtonText() {
        return bottomButtonText;
    }

    public void setBottomButtonText(String bottomButtonText) {
        this.bottomButtonText = bottomButtonText;
    }

    public int getNearRadius() {
        return nearRadius;
    }

    public void setNearRadius(int nearRadius) {
        this.nearRadius = nearRadius;
    }

    public int getIsLiveTracking() {
        return isLiveTracking;
    }

    public void setIsLiveTracking(int isLiveTracking) {
        this.isLiveTracking = isLiveTracking;
    }

    public int getLiveTrackingIntervalInMs() {
        return liveTrackingIntervalInMs;
    }

    public void setLiveTrackingIntervalInMs(int liveTrackingIntervalInMs) {
        this.liveTrackingIntervalInMs = liveTrackingIntervalInMs;
    }

    public int getIsShowCurrentMarker() {
        return isShowCurrentMarker;
    }

    public void setIsShowCurrentMarker(int isShowCurrentMarker) {
        this.isShowCurrentMarker = isShowCurrentMarker;
    }

    public int getIsRequestGaurd() {
        return isRequestGaurd;
    }

    public void setIsRequestGaurd(int isRequestGaurd) {
        this.isRequestGaurd = isRequestGaurd;
    }

    public int getIsNavigationOn() {
        return isNavigationOn;
    }

    public void setIsNavigationOn(int isNavigationOn) {
        this.isNavigationOn = isNavigationOn;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getRecordID() {
        return recordID;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }

    public String getLocationTitle() {
        return locationTitle;
    }

    public void setLocationTitle(String locationTitle) {
        this.locationTitle = locationTitle;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getTab1() {
        return tab1;
    }

    public void setTab1(String tab1) {
        this.tab1 = tab1;
    }

    public String getTab2() {
        return tab2;
    }

    public void setTab2(String tab2) {
        this.tab2 = tab2;
    }

    public String getSearchTitle() {
        return searchTitle;
    }

    public void setSearchTitle(String searchTitle) {
        this.searchTitle = searchTitle;
    }

    public String getSearchPlaceholder() {
        return searchPlaceholder;
    }

    public void setSearchPlaceholder(String searchPlaceholder) {
        this.searchPlaceholder = searchPlaceholder;
    }

    public int getIsMarkerClickCallback() {
        return isMarkerClickCallback;
    }

    public void setIsMarkerClickCallback(int isMarkerClickCallback) {
        this.isMarkerClickCallback = isMarkerClickCallback;
    }

    public int getMapZoomLevel() {
        return mapZoomLevel;
    }

    public void setMapZoomLevel(int mapZoomLevel) {
        this.mapZoomLevel = mapZoomLevel;
    }

    public String getMapPinIcon() {
        return mapPinIcon;
    }

    public void setMapPinIcon(String mapPinIcon) {
        this.mapPinIcon = mapPinIcon;
    }

    public String getOverlayPage() {
        return overlayPage;
    }

    public void setOverlayPage(String overlayPage) {
        this.overlayPage = overlayPage;
    }

    public int getOverlaySize() {
        return overlaySize;
    }

    public void setOverlaySize(int overlaySize) {
        this.overlaySize = overlaySize;
    }

    public boolean isMarkerClickShowOverlay() {
        return isMarkerClickShowOverlay == 1;
    }

    public void setIsMarkerClickShowOverlay(int isMarkerClickShowOverlay) {
        this.isMarkerClickShowOverlay = isMarkerClickShowOverlay;
    }

    public int isSearchAutoComplete() {
        return isSearchAutoComplete;
    }

    public void setIsSearchAutoComplete(int isSearchAutoComplete) {
        this.isSearchAutoComplete = isSearchAutoComplete;
    }

    public int getIsTracking() {
        return isTracking;
    }

    public void setIsTracking(int isTracking) {
        this.isTracking = isTracking;
    }

    public int getIsPlotLocation() {
        return isPlotLocation;
    }

    public void setIsPlotLocation(int isPlotLocation) {
        this.isPlotLocation = isPlotLocation;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public int getIsNavFromCurLoc() {
        return isNavFromCurLoc;
    }

    public void setIsNavFromCurLoc(int isNavFromCurLoc) {
        this.isNavFromCurLoc = isNavFromCurLoc;
    }

    public boolean getIsPlotAddressLocation() {
        return isPlotAddressLocation == 1;
    }

    public void setIsPlotAddressLocation(boolean isPlotAddressLocation) {
        this.isPlotAddressLocation = isPlotAddressLocation ? 1 : 0;
    }

    public int getIsShowDirection() {
        return isShowDirection;
    }

    public void setIsShowDirection(int isShowDirection) {
        this.isShowDirection = isShowDirection;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        parcel.writeString(pageTitle);
        parcel.writeString(nextButtonTitle);
        parcel.writeInt(isReadOnly);
        parcel.writeString(nextRedirectionURL);
        parcel.writeString(nextButtonCallback);
        parcel.writeString(offlineDataID);
        parcel.writeString(responseData);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeDouble(mDestLatitude);
        parcel.writeDouble(mDestLongitude);
        parcel.writeDouble(destLatitude);
        parcel.writeDouble(destLongitude);
        parcel.writeString(appID);
        parcel.writeString(colorCode);
        parcel.writeString(cancelButtonCallback);
        parcel.writeInt(isNavigation);
        parcel.writeInt(isLoadNearByPlaces);

        parcel.writeString(addressString);
        parcel.writeInt(mapType);
        parcel.writeInt(getMapSnapShot);
        parcel.writeInt(openMapApp);
        parcel.writeString(mapFileName);
        parcel.writeString(mapSrcName);
        parcel.writeInt(isShowCurrentLoc);
        parcel.writeInt(isWithoutEditor);
        parcel.writeInt(isShowTab);
        parcel.writeInt(isShowOverlay);
        parcel.writeInt(isSelectLocation);
        parcel.writeInt(isShowBottomButton);
        parcel.writeString(bottomButtonText);
        parcel.writeInt(nearRadius);
        parcel.writeInt(isLiveTracking);
        parcel.writeInt(liveTrackingIntervalInMs);
        parcel.writeInt(isShowCurrentMarker);
        parcel.writeInt(isRequestGaurd);
        parcel.writeInt(isNavigationOn);

        parcel.writeString(apiName);
        parcel.writeString(recordID);
        parcel.writeString(locationTitle);
        parcel.writeString(locationAddress);
        parcel.writeString(tab1);
        parcel.writeString(tab2);
        parcel.writeString(searchTitle);
        parcel.writeString(searchPlaceholder);
        parcel.writeInt(isMarkerClickCallback);
        parcel.writeInt(mapZoomLevel);
        parcel.writeString(mapPinIcon);
        parcel.writeString(overlayPage);
        parcel.writeInt(overlaySize);
        parcel.writeInt(isMarkerClickShowOverlay);
        parcel.writeInt(isSearchAutoComplete);
		parcel.writeInt(isAdminLogin);
		parcel.writeInt(isTracking);
		parcel.writeInt(isPlotLocation);
		parcel.writeString(queryString);
		parcel.writeInt(isNavFromCurLoc);
		parcel.writeInt(isPlotAddressLocation);
        parcel.writeInt(showSearchbar);
        parcel.writeInt(markerData);
        parcel.writeString(searchByCountriesList);
    }
}
