package com.hokuapps.logoutnativecall;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;

import com.hokuapps.logoutnativecall.utils.LogoutUtility;

import org.json.JSONException;
import org.json.JSONObject;

public class LogoutNativeCall {

    private String[] requiredJSONObjectKey = {};
    public WebView mWebView;
    public Activity mActivity;
    public Context mContext;

    public LogoutNativeCall(WebView mWebView, Activity mActivity, Context mContext) {
        this.mWebView = mWebView;
        this.mActivity = mActivity;
        this.mContext = mContext;
    }

    public void doLogout(final String logoutNativeRes){

        try {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(logoutNativeRes);
                        String nextButtonCallback = LogoutUtility.getStringObjectValue(jsonObject, "nextButtonCallback");
                        jsonObject.put("userDeviceObj", LogoutUtility.getUserDevicePushInfo(mContext));

                        LogoutUtility.callUserDevicePushInfoApi(jsonObject,mContext);
                        LogoutUtility.callJavaScriptFunction(mActivity, mWebView, nextButtonCallback, jsonObject);

                        mWebView.clearHistory();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
