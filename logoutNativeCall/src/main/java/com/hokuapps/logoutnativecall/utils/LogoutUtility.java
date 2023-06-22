package com.hokuapps.logoutnativecall.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.hokuapps.logoutnativecall.IWebSocketClientEvent;
import com.hokuapps.logoutnativecall.LogoutRestApiClientEvent;
import com.hokuapps.logoutnativecall.models.Error;
import com.hokuapps.logoutnativecall.constants.LogoutConstant;
import com.hokuapps.logoutnativecall.constants.LogoutConstant.GCMConstant;
import com.hokuappslogoutnativecall.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LogoutUtility {

    public static final ReentrantReadWriteLock REENTRANT_READ_WRITE_LOCK = new ReentrantReadWriteLock();


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

    public static String showAlertBridgeMissingKeys(Context context, String jsonData, String[] requiredJSONObjectKey) {

        String missingKeysMsg = "";


            try {
                missingKeysMsg = LogoutUtility.checkBridgeMissingKeys(context, new JSONObject(jsonData), requiredJSONObjectKey);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        return missingKeysMsg;
    }

    public static String checkBridgeMissingKeys(Context context, JSONObject missingValues, String[] requiredValues) {

        StringBuffer missingKeys = new StringBuffer();

        for (int i = 0; i < requiredValues.length; i++) {

            if (!missingValues.has(requiredValues[i])) {

                if (missingKeys.length() == 0) {
                    missingKeys.append(requiredValues[i]);
                } else {
                    missingKeys.append(", " + requiredValues[i]);
                }

            }
        }

        return "Missing keys = " + missingKeys;
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

    public static String getDeviceName() {
        String deviceName = String.format("%s%s, Android SDK %s", Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK_INT);
        deviceName = deviceName.replaceAll("_", "");
        return deviceName;
    }
    public static JSONObject getUserDevicePushInfo(Context mContext) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("pushServiceType", LogoutConstant.PUSH_SERVICE_TYPE.ANDROID);
            jsonObject.put("deviceID", "");
            jsonObject.put("deviceName", LogoutUtility.getDeviceName());
            jsonObject.put("pushServiceID", getRegistrationId(mContext));
            jsonObject.put("app", getResString(mContext, R.string.app_name));
            Log.i("@deviceInfo", "ID call back===" + getRegistrationId(mContext));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(GCMConstant.PREF_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(GCMConstant.PREF_APP_VERSION, Integer.MIN_VALUE);
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
        return context.getSharedPreferences(GCMConstant.PREF_NAME, Context.MODE_PRIVATE);
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

    public static String getResString(Context mContext, int resId) {
        return mContext.getResources().getString(resId);
    }

    public static void callUserDevicePushInfoApi(JSONObject jsonObjMain, Context mContext) {
        try {
            String apiName = LogoutUtility.getStringObjectValue(jsonObjMain, "apiName");
            String appName = LogoutUtility.getStringObjectValue(jsonObjMain, "appName");
            JSONObject addDeviceObject = (JSONObject) LogoutUtility.getJsonObjectValue(jsonObjMain, "addDeviceObject");
            String roleName = LogoutUtility.getStringObjectValue(addDeviceObject, "roleName");
            if (addDeviceObject == null) {
                addDeviceObject = new JSONObject();
            }
            addDeviceObject.put("userDeviceObj", LogoutUtility.getUserDevicePushInfo(roleName,mContext));
            addDeviceObject.put("appName", TextUtils.isEmpty(appName)
                    ? getResString(mContext,R.string.app_name)
                    : appName);
            LogoutRestApiClientEvent restApiClientEvent = new LogoutRestApiClientEvent(mContext, apiName);
            restApiClientEvent.setRequestJson(addDeviceObject);
            restApiClientEvent.setListener(new IWebSocketClientEvent() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    if(jsonObject != null)
                        Log.i("@deviceInfo", "process===" + jsonObject.toString());
                    else
                        Log.i("@deviceInfo", "error===" + jsonObject.toString());
                }
                @Override
                public void onFailure(Error error) {
                    Log.e("@deviceInfo", "error===" + error.toString());
                }

            });

            restApiClientEvent.setLooper(Looper.getMainLooper());
            restApiClientEvent.fire();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    public static JSONObject getUserDevicePushInfo(String roleName,Context mContext) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("pushServiceType", LogoutConstant.PUSH_SERVICE_TYPE.ANDROID);
//            jsonObject.put("deviceID", Utility.getDeviceId(App.getInstance().getApplicationContext()));
            jsonObject.put("deviceName", LogoutUtility.getDeviceName());
            jsonObject.put("pushServiceID", getRegistrationId(mContext));
            jsonObject.put("app", getResString(mContext,R.string.app_name));
            if (roleName != null && !TextUtils.isEmpty(roleName)) {
                jsonObject.put("roleName", roleName);
            }
            Log.i("@deviceInfo", "ID===" + getRegistrationId(mContext));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
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
