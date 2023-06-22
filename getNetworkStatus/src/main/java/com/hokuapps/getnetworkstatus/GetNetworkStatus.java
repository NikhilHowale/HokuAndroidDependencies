package com.hokuapps.getnetworkstatus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

public class GetNetworkStatus {
    private boolean isOfflineMode;
    Context mContext;
    Activity mActivity;
    private String networkInfoData;

    WebView mWebView;

    public BroadcastReceiver networkCheckReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String status = NetworkUtil.getConnectivityStatusString(context);
            if (status.isEmpty() || status.equalsIgnoreCase("No internet is available")) {
                isOfflineMode = true;
            } else {
                isOfflineMode = false;
            }
              getMobileNetworkStatus(networkInfoData);

        }
    };

    public GetNetworkStatus(Context context, Activity activity , WebView webView) {
        this.mContext = context;
        this.mActivity = activity;
        this.mWebView = webView;
    }

    public void getMobileNetworkStatus(String targetData){
        networkInfoData = targetData;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(targetData == null){
                        return;
                    }

                    JSONObject targetDataJsonObj = new JSONObject(targetData);
                    String nextButtonCallback = NetworkUtil.getStringObjectValue(targetDataJsonObj, "nextButtonCallback");
                    NetworkUtil.callJavaScriptFunction(mActivity, mWebView,
                            nextButtonCallback, new JSONObject().put("mode", isOfflineMode));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void registerNetworkReceiver() {
        mActivity.registerReceiver(networkCheckReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    public void unregisterNetworkReceiver() {
        mActivity.unregisterReceiver(networkCheckReceiver);
    }



}
