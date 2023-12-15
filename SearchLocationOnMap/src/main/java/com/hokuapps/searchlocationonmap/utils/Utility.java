package com.hokuapps.searchlocationonmap.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.LocationManager;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.hokuapps.searchlocationonmap.R;

import org.json.JSONException;
import org.json.JSONObject;

public class Utility {


    public static int getJsonObjectIntValue(JSONObject obj, String fieldName) throws JSONException {
        if (obj == null) return 0;
        if (obj.has(fieldName)) {
            try {
                return obj.getInt(fieldName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

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


    public static void callJavaScriptFunction(Activity activity, final WebView webView, final String callingJavaScriptFn, final JSONObject response) {

        if (activity == null) return;


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    webView.evaluateJavascript(String.format("javascript:" + callingJavaScriptFn + "(%s)", response), null);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }


    public static String getMapApiKey(Context context) {
        String map_api_key = "";

        switch (context.getPackageName()) {

            case "com.cpclient":
                map_api_key = Utility.getResString(context, R.string.map_api_key_cp_client);
                break;

            case "com.cpdriver":
                map_api_key = Utility.getResString(context, R.string.map_api_key_cp_driver);
                break;

            case "com.restorationOSLite":
                map_api_key = Utility.getResString(context, R.string.map_api_key_restoration);
                break;

            default:
                map_api_key = Utility.getResString(context, R.string.map_api_key);
        }
        return map_api_key;
    }

    public static String getResString(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    public static JSONObject convertStringToJson(String strToConvert) {
        try {
            if (TextUtils.isEmpty(strToConvert)) return null;

            return new JSONObject(strToConvert);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Hide soft input keyboard
     *
     * @param context
     * @param windowToken
     */
    public static void hideSoftKeyboard(Context context, IBinder windowToken) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isGPSInfo(Context context) {

        LocationManager locationmanager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationmanager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(activity);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {

            Dialog dialog = api.getErrorDialog(activity, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(activity, "Cannot Connect To Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    public static boolean isActivityLive(Activity activity) {
        if (activity == null) {
            return false;
        }
        return !activity.isFinishing();
    }
}
