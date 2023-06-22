package com.hokuapps.updateappversion;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateAppVersion {

    private WebView mWebView;
    private Context mContext;
    private Activity mActivity;
    public String applicationID;

    public UpdateAppVersion(Context context, Activity activity, WebView webView) {
        this.mWebView  = webView;
        this.mContext = context;
        this.mActivity = activity;
    }

    public void doUpdateAppVersion(final String data , String applicationID){

        if(data == null && applicationID ==null)
            return;

        this.applicationID = applicationID;

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject jsonObjectData = new JSONObject(data);
                    String apiName = Utility.getStringObjectValue(jsonObjectData, "apiName");
                    String tokenKey = Utility.getStringObjectValue(jsonObjectData, "tokenKey");
                    String secretKey = Utility.getStringObjectValue(jsonObjectData, "secretKey");
                    final boolean showNativeDialog = Utility.getJsonObjectBooleanValue(jsonObjectData, "showNativeDialog");


                    UpdateVersionApiClient restApiClientEvent = new UpdateVersionApiClient(mContext, apiName,tokenKey,secretKey);
                    restApiClientEvent.setRequestJson(jsonObjectData);
                    restApiClientEvent.setListener(new IWebSocketClientEvent() {
                        @Override
                        public void onSuccess(JSONObject jsonObject) {

                            if (jsonObject != null) {
                                try {
                                    JSONObject jsonObjectApiResponse = null;
                                    jsonObjectApiResponse = new JSONObject(jsonObject.toString());
                                    if (Utility.getStringObjectValue(jsonObjectApiResponse, "status").equals("0")) {
                                        String stringServerAppVersion = Utility.getStringObjectValue((JSONObject) Utility.getJsonObjectValue(jsonObjectApiResponse, "recordDetails"), "appversion");

                                        System.out.println(jsonObject != null ? jsonObject.toString() : "error");
                                        String versionName = Utility.getVersionName(mContext);

                                        //  Current app version.
                                        if (versionName.contains(".")) {
                                            versionName = versionName.replace(".", "");
                                        }

                                       //   Server app version.
                                        if (stringServerAppVersion.contains(".")) {
                                            stringServerAppVersion = stringServerAppVersion.replace(".", "");
                                        }

                                        int serverAppVersion = 0;
                                        int currentAppVersion = 0;

                                        serverAppVersion = Integer.parseInt(stringServerAppVersion);
                                        currentAppVersion = Integer.parseInt(versionName);
                                       checkAppVersionForUpdate(currentAppVersion, serverAppVersion, jsonObjectApiResponse, showNativeDialog, data, jsonObjectData);

                                    }
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Error error) {

                        }

                    });

                    restApiClientEvent.setLooper(Looper.getMainLooper());
                    restApiClientEvent.fire();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

    }

    private void checkAppVersionForUpdate(int currentAppVersion, int serverAppVersion, JSONObject jsonObjectResponse, boolean showNativeDialog, final String data, JSONObject jsonObjectData) {
        Log.e("APP_VERSION", "CURRENT APP VERSION:" + currentAppVersion);
        Log.e("APP_VERSION", "SERVER APP VERSION:" + serverAppVersion);
        try {
            if (currentAppVersion < serverAppVersion) {
                //  Send callBack here.
                String nextButtonCallback = Utility.getStringObjectValue(jsonObjectData, "nextButtonCallback");
                jsonObjectData.put("newVersionFound", true);
                Utility.callJavaScriptFunction(mActivity, mWebView, nextButtonCallback, jsonObjectData);
                if (showNativeDialog) {
                    Utility.showAlertMessageDialog(mContext,
                            Utility.getStringObjectValue((JSONObject) Utility.getJsonObjectValue(jsonObjectResponse, "recordDetails"), "updatemessage"),
                            false,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parseOpenPlayStoreForUpdate(data);
                                }
                            });

                } else {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseOpenPlayStoreForUpdate(String data) {
        try {
            Intent viewIntent =
                    new Intent("android.intent.action.VIEW",
                            Uri.parse("https://play.google.com/store/apps/details?id=" + applicationID));
            mContext.startActivity(viewIntent);
        } catch (Exception e) {
            Toast.makeText(mContext, "Unable to Connect Try Again...",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
