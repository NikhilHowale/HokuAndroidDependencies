package com.hokuapps.loginwithfb.utility;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONObject;

public class Utility {

    /**
     * returns string value from the json object
     * @param obj jsonObject response
     * @param fieldName key for retrieve String value
     * @return String value for filedName
     */
    public static String getStringObjectValue(JSONObject obj, String fieldName) {
        try {
            if (obj == null) return "";

            if (obj.has(fieldName)) {
                Object o = obj.get(fieldName);
                return o.toString();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * calls function
     * @param activity context of activity
     * @param webView reference of webView
     * @param callingJavaScriptFn function name of java script
     * @param response jsonObject for javascript function
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
     * Show toast message to user
     * @param context context
     * @param msg message string to display
     */
    public static void showMessage(Context context, String msg) {
        if (context != null && !TextUtils.isEmpty(msg))
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
