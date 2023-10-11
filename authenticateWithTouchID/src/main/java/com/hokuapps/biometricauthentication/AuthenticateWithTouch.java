package com.hokuapps.biometricauthentication;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("ConstantConditions")
public class AuthenticateWithTouch {

    Activity mWebAppActivity;
    private final int BIOMETRIC_AUT = 1001;
    private final WebView mWebView;
    public AuthenticateWithTouch (Activity activity, WebView webView){
         this.mWebAppActivity = activity;
         this.mWebView = webView;

    }


    public void authenticateWithTouchID(final String data) {

        mWebAppActivity.runOnUiThread(() -> {
            final String nextButtonCallback;

            try {
                final JSONObject jsonObj = new JSONObject(data);
                nextButtonCallback = getStringObjectValue(jsonObj, "nextButtonCallback");
                if (!TextUtils.isEmpty(nextButtonCallback)) {
                    Intent biometricIntent = new Intent(mWebAppActivity, BiometricAuthentication.class);
                    biometricIntent.putExtra("nextButtonCallback",nextButtonCallback);
                    biometricIntent.putExtra("jasonObject",jsonObj.toString());
                    mWebAppActivity.startActivityForResult(biometricIntent,BIOMETRIC_AUT);

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    public void sendFingerPrintResultToServer(String nextButtonCallback, JSONObject jsonObj, int result) {

        try {
            jsonObj.put("status", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        callJavaScriptFunction(mWebAppActivity, mWebView, nextButtonCallback, jsonObj);
    }


    public void handleActivityResultCallback(Intent intent) {

        String nextCallBackButton = intent.getStringExtra("nextButtonCallBack");
        String jasonObject = intent.getStringExtra("jasonObject");
        int biometricResult = Integer.parseInt(intent.getStringExtra("Result"));

        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(jasonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        sendFingerPrintResultToServer(nextCallBackButton,jsonObj,biometricResult);
    }

    public  String getStringObjectValue(JSONObject obj, String fieldName) {
        try {
            if (obj == null) return null;

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

    public  void callJavaScriptFunction(Activity activity, final WebView webView, final String callingJavaScriptFn, final JSONObject response) {

        if (activity == null) return;


        activity.runOnUiThread(() -> {
            try {
                webView.evaluateJavascript(String.format("javascript:" + callingJavaScriptFn + "(%s)", response), null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
    }

}
