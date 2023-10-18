package com.cometchat.pro.uikit;

import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.webkit.WebView;

import androidx.core.app.NotificationCompat;

import com.cometchat.pro.uikit.ui_resources.utils.HtmlTagHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class Utility {


    public static Spanned fromHtml(String htmlText) {
        return Html.fromHtml(htmlText, null, new HtmlTagHandler());
    }

    /**
     * This method return string value from jsonObject
     * @param obj jsonObject
     * @param fieldName key name jsonObject
     * @return return boolean value
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
     * This method return boolean value from jsonObject
     * @param obj jsonObject
     * @param fieldName key name jsonObject
     * @return return boolean value
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
     *  This method call java script function
     * @param activity activity reference
     * @param webView webView
     * @param callingJavaScriptFn java script callback name
     * @param response jsonObject
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

    public static NotificationCompat.Style getNotificationStyle(String contentTitle, String messageText) {
        try {
            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.bigText(Utility.fromHtml(messageText));
            bigTextStyle.setBigContentTitle(contentTitle);
            return bigTextStyle;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
