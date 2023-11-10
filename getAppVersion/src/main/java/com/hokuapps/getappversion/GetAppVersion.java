package com.hokuapps.getappversion;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.webkit.WebView;

import org.json.JSONObject;

public class GetAppVersion {
    private static GetAppVersion instance;

    public static GetAppVersion getInstance(){
        if(instance == null){
            instance = new GetAppVersion();
        }
        return instance;
    }

    /**
     * This method return version of application
     * @param context context
     * @param mWebView webView reference
     * @param response json in string format
     */
    public void getVersion(Activity context, WebView mWebView, String response){
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("appVersion", version);
            JSONObject jsonObject = new JSONObject(response);
            String nextButtonCallback = Utility.getStringObjectValue(jsonObject, "nextButtonCallback");
            Utility.callJavaScriptFunction(context, mWebView, nextButtonCallback, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
