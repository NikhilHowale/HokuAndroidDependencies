package com.hokuapps.loadmapviewbyconfig.utility;

import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.CITY;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.COUNTRY;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.COUNTRY_CODE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.NAME;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.POSTAL_CODE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.STATE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.SUB_ADMIN_AREA;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.SUB_LOCALITY;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.IBinder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.text.HtmlCompat;

import com.google.android.gms.maps.model.LatLng;
import com.hokuapps.loadmapviewbyconfig.App;
import com.hokuapps.loadmapviewbyconfig.BuildConfig;
import com.hokuapps.loadmapviewbyconfig.R;
import com.hokuapps.loadmapviewbyconfig.constant.MapConstant;
import com.hokuapps.loadmapviewbyconfig.models.Error;
import com.hokuapps.loadmapviewbyconfig.services.SocketManager;
import com.hokuapps.loadmapviewbyconfig.widgets.bottomsheetshare.AppAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class Utility {


    public static boolean isWhiteColor = false;
    public static JSONObject configJson = new JSONObject();

    public static void openDirectionVia(AppAdapter.AppInfo appInfo, Context context, double latitude, double longitude, double destLatitude, double destLongitude) {
        switch (appInfo.packageName) {
            case "com.waze":
                Utility.openWazeMapDirection(context, destLatitude, destLongitude);
                break;
            case "com.google.android.apps.maps":
            default:
                Utility.openGoogleMapDirection(context, latitude,
                        longitude, destLatitude, destLongitude);
                break;
        }
    }

    public static void openWazeMapDirection(Context context,  double dstLat, double dstLong) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("waze://?ll=" + dstLat + "," + dstLong + "&navigate=yes&zoom=17"));
        context.startActivity(intent);
    }


    /**
     * Show toast with given message
     * @param context context
     * @param msg message to be shown
     */
    public static void showMessage(Context context, String msg) {
        if (context != null && !TextUtils.isEmpty(msg))
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static Spanned fromHtml(String htmlText) {
        return HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    public static CharSequence getValidString(String value) {
        if (TextUtils.isEmpty(value))
            return "";

        return value;
    }


    /**
     * Get latitude and longitude from address
     * @param strAddress Address string to get the location
     * @param context context
     * @return return lat and long value from address
     */
    public static LatLng getLocationFromAddress(String strAddress, Context context) {

        Geocoder coder = new Geocoder(context, Locale.getDefault());
        List<Address> address;
        LatLng addressLatLng = null;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.size() == 0) {
                return null;
            }
            Address location = address.get(0);
            addressLatLng = new LatLng(location.getLatitude(), location.getLongitude());


        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressLatLng;
    }


    /**
     * get map Api key
     * @param context context
     * @return return map key for app
     */
    public static String getMapApiKey(Context context) {
        String map_api_key;

        switch (context.getPackageName()) {

            case "com.cpclient":
                map_api_key = Utility.getResString(R.string.map_api_key_cp_client,context);
                break;

            case "com.cpdriver":
                map_api_key = Utility.getResString(R.string.map_api_key_cp_driver,context);
                break;

            default:
                map_api_key = Utility.getResString(R.string.map_api_key,context);
        }
        return map_api_key;
    }


    /**
     * Open external map with given latitude and longitude
     * @param context context
     * @param latitude latitude
     * @param longitude longitude
     */
    @SuppressLint("QueryPermissionsNeeded")
    public static void openInExternalMapByLatLong(Context context, double latitude, double longitude) {
        try {
            Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (" + ")");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Open external map with given address
     * @param context context
     * @param address address value to navigate using google map
     */
    @SuppressLint("QueryPermissionsNeeded")
    public static void openInExternalMapByAddress(Context context, String address) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://maps.google.co.in/maps?q=" + address));
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * open External google map for provided lat and long
     * @param context context
     * @param dstLat destination latitude
     * @param dstLong destination longitude
     */
    public static void openGoogleMapDirection(Context context, double dstLat, double dstLong) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d" +
                "&daddr=" + dstLat + "," + dstLong));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        context.startActivity(intent);
    }


    /**
     * open External google map for provided source location and destination location
     * @param context context
     * @param srcLat source latitude
     * @param srcLong source longitude
     * @param dstLat destination latitude
     * @param dstLong destination longitude
     */
    public static void openGoogleMapDirection(Context context, double srcLat, double srcLong, double dstLat, double dstLong) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?f=d&saddr=" +
                srcLat + "," + srcLong +
                "&daddr=" + dstLat + "," + dstLong));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        context.startActivity(intent);
    }


    /**
     * Parse the response
     * @param object api response
     * @param listener callback - return data after parse
     */
    public static void parseResponse(Object object, SocketManager.DataListener<JSONObject> listener) {
        try {
            JSONObject jsonObject = new JSONObject(object.toString());
            int statusCode = 0;

            if (jsonObject.has(MapConstant.AuthIO.STATUS_CODE))
                statusCode = jsonObject.getInt(MapConstant.AuthIO.STATUS_CODE);

            if (statusCode == SocketManager.STATUS_SUCCESS) {
                listener.onSuccess(jsonObject);
            } else {
                listener.onError(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Error error = new Error(MapConstant.INVALID_ID, App.getInstance().getApplicationContext().getResources().getString(R.string.err_parsing_msg));
            listener.onError(error.createJSONObject());
        }
    }


    /**
     * Get random id
     * @return return random number string
     */
    public static String getRandomUUID() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 24; i++) {
            sb.append(Integer.toHexString(rnd.nextInt(16)));
        }
        return sb.toString();
    }


    /**
     * Get version name
     * @param context context
     * @return return version name
     */
    public static String getVersionName(Context context) {

        String versionName = null;
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }


    /**
     * Get drawable with given color id
     * @param drawable drawable
     * @param colorId color string
     * @return return drawable after changing color
     */
    public static Drawable getColorDrawable( Drawable drawable, String colorId) {

        if (TextUtils.isEmpty(colorId)) return drawable;

        drawable.setColorFilter(Color.parseColor(colorId), PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }


    /**
     * Get resource string by id
     * @param resId string resource id
     * @param context context
     * @return return string
     */
    public static String getResString(int resId,Context context) {
        return context.getResources().getString(resId);
    }


    /**
     * Get screen height
     */
    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    /**
     * Get image drawable from assets folder
     * @param context context
     * @param fileName file name
     * @return return file as drawable from asset
     */
    public static Drawable getImageDrawableFromAssets(Context context, String fileName) {

        String filePath = Objects.requireNonNull(Utility.getHtmlDirFromSandbox(context)).getAbsolutePath() + "/" + fileName;
        try {
            return BitmapDrawable.createFromPath(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Check if application is iplimomob
     */
    public static boolean isIpLimomob() {

        return MapConstant.APPLICATION_ID.equals("com.iplimomob");

    }


    /**
     * get string for missing keys

     * @param jsonData jsonObject
     * @param requiredJSONObjectKey array of require key
     * @return return string if any key is miss
     */
    public static String showAlertBridgeMissingKeys( String jsonData, String[] requiredJSONObjectKey) {

        String missingKeysMsg = "";

        if (BuildConfig.DEBUG) {
            try {
                missingKeysMsg = Utility.checkBridgeMissingKeys( new JSONObject(jsonData), requiredJSONObjectKey);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return missingKeysMsg;
    }


    /**
     * Get current date and time in milli seconds
     */
    public static long getCurrentDateTimeInMS() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime().getTime();
    }


    /**
     * Get json object that contains address
     * @param fetchedAddress address object
     * @return return jsonObject of address data
     */
    public static JSONObject getAddressJson(Address fetchedAddress) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(COUNTRY, fetchedAddress.getCountryName());
            jsonObj.put(COUNTRY_CODE, fetchedAddress.getCountryCode());
            jsonObj.put(STATE, fetchedAddress.getAdminArea());
            jsonObj.put(NAME, fetchedAddress.getSubThoroughfare() + " " + fetchedAddress.getThoroughfare());
            jsonObj.put(SUB_ADMIN_AREA, fetchedAddress.getSubAdminArea());
            jsonObj.put(POSTAL_CODE, fetchedAddress.getPostalCode());
            jsonObj.put(CITY, fetchedAddress.getLocality());
            jsonObj.put(SUB_LOCALITY, fetchedAddress.getSubLocality());

            return jsonObj;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObj;
    }


    /**
     * Show alert dialog with given message and title
     * @param context context
     * @param msg message
     * @param title title
     */
    public static void showAlertMessage(Context context, String msg, String title) {
        if (context == null) {
            return;
        } else if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }

        //Instantiate an AlertDialog.Builder with its constructor
        new androidx.appcompat.app.AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                .setMessage(msg)
                .setTitle(title)
                .setPositiveButton(R.string.label_ok, (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }


    /**
     * Get bridge missing keys
     * @param missingValues jsonObject
     * @param requiredValues array of require key
     * @return return string if any key is miss
     */
    public static String checkBridgeMissingKeys( JSONObject missingValues, String[] requiredValues) {

        StringBuilder missingKeys = new StringBuilder();

        for (String requiredValue : requiredValues) {

            if (!missingValues.has(requiredValue)) {

                if (missingKeys.length() == 0) {
                    missingKeys.append(requiredValue);
                } else {
                    missingKeys.append(", ").append(requiredValue);
                }

            }
        }

        return "Missing keys = " + missingKeys;
    }


    /**
     * Convert string to json
     * @param strToConvert json string
     * @return return jsonObject from string
     */
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
     * Get image from bitmap by size
     * @param context context
     * @param bitmapOriginal original bitmap
     * @param width require width
     * @param height require height
     * @return return bitmap after scale
     */
    public static Bitmap getImageDrawableFromBitmapBySize(Context context, Bitmap bitmapOriginal, int width, int height) {

        try {

            int density = (context.getResources().getConfiguration().densityDpi / 160);


            return Bitmap.createScaledBitmap(bitmapOriginal, density * width, density * height, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Show keyboard
     * @param context context
     */
    public static void showKeyboard(Context context) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).
                toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    /**
     * get image from assets folder by size
     * @param context context
     * @param fileName file name
     * @param width require width
     * @param height require height
     * @return return drawable after scale
     */
    public static Drawable getImageDrawableFromAssetsBySize(Context context, String fileName, int width, int height) {

        String filePath = Objects.requireNonNull(Utility.getHtmlDirFromSandbox(context)).getAbsolutePath() + "/" + fileName;
        try {

            int density = (context.getResources().getConfiguration().densityDpi / 160);

            Bitmap bitmapOriginal = ScalingUtility.decodeFile(filePath);

            Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmapOriginal, density * width, density * height, false);

            return new BitmapDrawable(context.getResources(), bitmapResized);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Get html directory from sandbox
     * @param context context
     * @return return sandbox directory
     */
    public static File getHtmlDirFromSandbox(Context context) {
        File htmlDir = new File(context.getFilesDir() + File.separator + MapConstant.FOLDER_NAME_WEB_HTML);

        if (!makeDirectory(htmlDir)) {
            return null;
        }

        return htmlDir;
    }


    /**
     * Create directory
     * @param mediaStorageDir directory path
     * @return return true if directory is created otherwise false
     */
    public static boolean makeDirectory(File mediaStorageDir) {
        if (!mediaStorageDir.exists()) {
            return mediaStorageDir.mkdirs();
        }
        return true;
    }


    /**
     * get boolean value from json object by field name
     * @param obj jsonObject
     * @param fieldName key in jsonObject
     * @return return value in jsonObject
     */
    public static boolean getJsonObjectBooleanValue(JSONObject obj, String fieldName) {
        if (obj == null) return false;

        try {

            if (obj.has(fieldName)) {
                return obj.getBoolean(fieldName);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    /**
     * get double value from json object by field name
     * @param obj jsonObject
     * @param fieldName key in jsonObject
     * @return return value in jsonObject
     */
    public static double getJsonObjectDoubleValue(JSONObject obj, String fieldName) {
        if (obj == null) return 0;
        if (obj.has(fieldName)) {
            try {
                Object obj1 = obj.get(fieldName);
                return Double.parseDouble(obj1.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }


    /**
     * get string value from json object by field name
     * @param obj jsonObject
     * @param fieldName key in jsonObject
     * @return return value in jsonObject
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
     * Hide keyboard
     * @param context context
     * @param windowToken token
     */
    public static void hideSoftKeyboard(Context context, IBinder windowToken) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(windowToken, InputMethodManager.RESULT_UNCHANGED_SHOWN);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    /**
     * Call javascript function given function name
     * @param activity activity
     * @param webView webView reference
     * @param callingJavaScriptFn callback name
     * @param response jsonObject data
     */
    public static void callJavaScriptFunction(Activity activity, final WebView webView, final String callingJavaScriptFn, final JSONObject response) {

        if (activity == null) return;

        activity.runOnUiThread(() -> {
            try {
                webView.evaluateJavascript(String.format("javascript:" + callingJavaScriptFn + "(%s)", response), null);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
    }


    /**
     * Convert dp to pixel
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }


    /**
     * Get dimensions from resource id
     */
    public static float getDimension(int resId, Context applicationContext) {
        return applicationContext.getResources().getDimension(resId);
    }


    /**
     * Check if string is empty
     */
    public static boolean isEmpty(String string) {
        if (string != null) {
            string = string.trim();
        }
        return TextUtils.isEmpty(string);
    }


    /**
     * Get json object value by field name
     * @param obj jsonObject
     * @param fieldName key in jsonObject
     * @return return value in jsonObject
     * @throws JSONException if jsonObject is inValid
     */
    public static Object getJsonObjectValue(JSONObject obj, String fieldName) throws JSONException {
        try {
            if (obj == null) return null;
            if (obj.has(fieldName)) {
                return obj.get(fieldName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    /**
     * Get json object integer value by field name
     * @param obj jsonObject
     * @param fieldName key in jsonObject
     * @return return value in jsonObject
     * @throws JSONException if jsonObject is inValid
     */
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

}
