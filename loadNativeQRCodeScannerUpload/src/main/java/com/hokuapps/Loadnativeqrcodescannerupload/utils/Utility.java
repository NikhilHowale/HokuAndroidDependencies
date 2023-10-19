package com.hokuapps.Loadnativeqrcodescannerupload.utils;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Utility {


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
     * Vibrate device till provided time is up
     *
     * @param context
     * @param time
     */
    public static void vibrateDevice(Context context, long time) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (!Utility.isVibratorAvailable(context)) return;
        vibrator.vibrate(time);
    }

    /**
     * Check is vibrator support by device
     *
     * @param appContext
     * @return status
     */
    public static final boolean isVibratorAvailable(Context appContext) {
        if (appContext == null) {
            Log.e("BarcodeCaptureActivity", "isVibratorAvailable: context is null");
            return false;
        }

        Vibrator vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            return true;
        }

        return false;
    }

    public static String getCompleteUrl(Context context,String pageName) {
        File sandboxFile = new File(getHtmlDirFromSandbox(context) + File.separator + pageName);
        String url = Uri.fromFile(sandboxFile).toString();
        url = buildUrlWithSecretKey(url);

        return url;

    }

    /**
     * This method provide sandbox directory file
     * @param context context
     * @return return sandbox file
     */
    public static File getHtmlDirFromSandbox(Context context) {
        File htmlDir = new File(context.getFilesDir() + File.separator + AppConstant.FOLDER_NAME_WEB_HTML);

        if (!makeDirectory(htmlDir)) {
            return null;
        }

        return htmlDir;
    }

    /**
     * Create the storage directory if it does not exist
     *
     * @param mediaStorageDir directory file
     * @return true if directory created successfully
     * false Otherwise
     */
    public static boolean makeDirectory(File mediaStorageDir) {
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return false;
            }
        }
        return true;
    }

    private static String buildUrlWithSecretKey(String url) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("?").append("queryMode=mylist");
        sb.append("&tokenKey=").append(AppConstant.URLConstant.AUTH_TOKEN);
        sb.append("&secretKey=").append(AppConstant.URLConstant.AUTH_SECRET_KEY);
        return sb.toString();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
}
