package com.hokuapps.loginnativecall.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.hokuapps.loginnativecall.LoginConstant;
import com.hokuappsloginnativecall.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LoginUtility {

    public static final ReentrantReadWriteLock REENTRANT_READ_WRITE_LOCK = new ReentrantReadWriteLock();

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

    public static String getDeviceName() {
        String deviceName = String.format("%s%s, Android SDK %s", Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK_INT);
        deviceName = deviceName.replaceAll("_", "");
        return deviceName;
    }


    /**
     * Check Internet connection availability.
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {

        if (context == null) {
            return false;
        }

        ReentrantReadWriteLock.ReadLock readLock = REENTRANT_READ_WRITE_LOCK.readLock();
        readLock.lock();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // if no network is available networkInfo will be null, otherwise check if we are connected
        try {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                return true;
            }
        } catch (Exception e) {
        } finally {
            readLock.unlock();
        }
        return false;
    }

    public static JSONObject getUserDevicePushInfo(Context context) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("pushServiceType", LoginConstant.PUSH_SERVICE_TYPE.ANDROID);
            jsonObject.put("deviceID", "");
            jsonObject.put("deviceName", LoginUtility.getDeviceName());
            jsonObject.put("pushServiceID", getRegistrationId(context));
            jsonObject.put("app", getResString(R.string.app_name, context));
            Log.i("@deviceInfo", "ID call back===" + getRegistrationId(context));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static String getResString(int resId, Context context) {
        return context.getResources().getString(resId);
    }

    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(LoginConstant.GCMConstant.PREF_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(LoginConstant.GCMConstant.PREF_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersionCode(context);
        if (registeredVersion != currentVersion) {
            //UnRegisterOLDDevicesClientEvent.callApiUnRegisterOldDevices(context);
            return /*""*/ registrationId;
        }
        return registrationId;
    }

    private static SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(LoginConstant.GCMConstant.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static int getAppVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static File getHtmlDirFromSandbox(Context context) {
        File htmlDir = new File(context.getFilesDir() + File.separator + LoginConstant.FOLDER_NAME_WEB_HTML);

        if (!makeDirectory(htmlDir)) {
            return null;
        }

        return htmlDir;
    }

    public static boolean makeDirectory(File mediaStorageDir) {
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return false;
            }
        }
        return true;
    }

    public static String getPaypalPageName(String pageKey, Context mContext) {

        String resultPageName = "mybookings_5a394f0d5c9e4d1b4396cd6a.html";
        String paypalConfigStr = new LoginPref(mContext).getValue("paypalConfig");
        try {
            if (!TextUtils.isEmpty(paypalConfigStr)) {
                resultPageName = LoginUtility.getStringObjectValue(new JSONObject(paypalConfigStr), pageKey);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return resultPageName;
    }

    public static CharSequence getValidString(String value) {
        if (TextUtils.isEmpty(value))
            return "";

        return value;
    }

    public static void callJavaScriptFunction(Activity activity, final WebView webView, final String callingJavaScriptFn, final JSONObject response) {

        if (activity == null) return;


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.evaluateJavascript(String.format("javascript:" + callingJavaScriptFn + "(%s)", response), null);
                    } else {
                        webView.loadUrl(String.format("javascript:" + callingJavaScriptFn + "(%s)", response));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }


}
