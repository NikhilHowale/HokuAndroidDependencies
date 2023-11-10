package com.hokuapps.getappversion;

import android.app.Activity;
import android.webkit.WebView;

import org.json.JSONObject;

public class Utility {

    /**
     * This method retrieves string value from json
     * @param obj jsonObject
     * @param fieldName key in jsonObject
     * @return return string value from jsonObject
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
     * This method call javascript function in webpage load in webView
     * @param activity activity
     * @param webView webView reference
     * @param callingJavaScriptFn javascript function name
     * @param response JSON response
     */
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
