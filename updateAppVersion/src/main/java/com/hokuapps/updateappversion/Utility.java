package com.hokuapps.updateappversion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Utility {

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
     * Gets the version code of app.
     *
     * @param context
     * @return
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

    public static void showAlertMessageDialog(Context context, String msg, boolean cancelable, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = createAlertDialogWithOk(context, 0, listener);
        builder.setCancelable(cancelable);
        builder.setMessage(msg);
        builder.show();
    }

    private static AlertDialog.Builder createAlertDialogWithOk(Context context, int theme, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, theme);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.label_ok, listener);
        return builder;
    }



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
}
