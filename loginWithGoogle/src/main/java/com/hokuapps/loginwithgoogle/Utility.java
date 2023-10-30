package com.hokuapps.loginwithgoogle;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import org.json.JSONObject;

public class Utility {


    /**
     * This method check package name and return request token to google option
     * @param context context
     * @return return request token
     */
    public static String getRequestIdToken(Context context) {
        String reqToken = "";
        switch (context.getPackageName()) {
            case "com.milletbeta":
                reqToken = "802830660822-77gltl1ge27uoo7lsffjj98tsnj16iu6.apps.googleusercontent.com";
                break;
            case "com.carnert":
                reqToken = "757247591946-e38unq35icu2rqu5sk7ngms9mcts98pm.apps.googleusercontent.com";
                break;
            case "com.tenkey":
                reqToken = "191528046969-7ileivvnqrv7dp6ihgpp47eb5jovf46t.apps.googleusercontent.com";
                break;
            case "com.luxurybuys":
                reqToken = "1083884201219-c3q56v3j86f3ih36l86tatprk59ji1ek.apps.googleusercontent.com";
                break;
            case "com.astroire":
                reqToken="741115617254-tdnb70c1hkv8f85evrf9o2tu7s0vjd94.apps.googleusercontent.com";
                break;
            default:
                reqToken = "958873930632-sctf7jpq19dueom5gk8p6cblbphpa9n2.apps.googleusercontent.com";
        }
        return reqToken;
    }

    /**
     * This method retrieves string from jsonObject
     * @param obj jsonObject
     * @param fieldName key from jsonObject
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
     * This method call java script function
     * @param activity activity
     * @param webView webView reference
     * @param callingJavaScriptFn java script function name
     * @param response jsonObject
     */
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
