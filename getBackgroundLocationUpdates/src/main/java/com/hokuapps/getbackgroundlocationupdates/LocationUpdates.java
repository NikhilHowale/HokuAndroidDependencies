package com.hokuapps.getbackgroundlocationupdates;


import static com.hokuapps.getbackgroundlocationupdates.constants.LocationConstants.BACKGROUND_LOCATION;
import static com.hokuapps.getbackgroundlocationupdates.constants.LocationConstants.FOREGROUND_LOCATION;
import static com.hokuapps.getbackgroundlocationupdates.constants.LocationConstants.ONETIME_LOCATION;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;

import com.hokuapps.getbackgroundlocationupdates.Locations.BackgroundLocation;
import com.hokuapps.getbackgroundlocationupdates.Locations.ForegroundLocation;
import com.hokuapps.getbackgroundlocationupdates.Locations.OneTimeLocation;
import com.hokuapps.getbackgroundlocationupdates.Utility.LocUtility;
import com.hokuapps.getbackgroundlocationupdates.constants.LocationConstants;

import org.json.JSONObject;

public class LocationUpdates {


    /**
     * start to  location updates according to different modes (foreground, background, onetime)
     * @param webView - instance of webView
     * @param activity- calling activity
     * @param context- activity context
     * @param responseData- responseData from javascript
     * @param appName- requires to show foreground notification
     * @param bytes - image bytes to show foreground notification icon
     */
    public void startLocationUpdates( WebView webView,Activity activity, Context context, String responseData , String appName, byte[] bytes){
        try {
           JSONObject jsonObject = new JSONObject(responseData);
            String locationType = LocUtility.getStringObjectValue(jsonObject, LocationConstants.LOCATION_TYPE);

            switch(locationType) {
                case FOREGROUND_LOCATION:
                    ForegroundLocation foregroundLocation = ForegroundLocation.getInstance();
                    foregroundLocation.startForegroundLocationUpdate(responseData,context);
                     break;
                case BACKGROUND_LOCATION:
                    BackgroundLocation backgroundLocation = BackgroundLocation.getInstance();
                    backgroundLocation.startBackgroundLocationService(responseData,context,appName,bytes) ;
                    break;
                case ONETIME_LOCATION:
                    OneTimeLocation oneTimeLocation = OneTimeLocation.getInstance();
                    oneTimeLocation.getOneTimeLocation(responseData,context,webView,activity); ;
                    break;
                default:
                    // code block
            }


        } catch (Exception e){

        }


    }
}
