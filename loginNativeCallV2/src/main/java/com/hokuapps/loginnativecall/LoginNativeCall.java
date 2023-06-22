package com.hokuapps.loginnativecall;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;


import com.hokuapps.loginnativecall.utils.LoginPref;
import com.hokuapps.loginnativecall.model.Error;
import com.hokuapps.loginnativecall.utils.LoginUtility;

import org.json.JSONObject;

import java.io.File;

public class LoginNativeCall {

    public static String TAG = "LoginNativeCall";

    public WebView mWebView;
    public Activity mActivity;
    public Context mContext;
    private String[] requiredJSONObjectKey = {};
    private final boolean whileDebuggingShowMissingAlert = false;
    private String appAuthToken = "";
    private String appSecretKey = "";

    private String pushServiceID = "";

    private String appName = "";
    private String roleName = "";
    private String redirectHtmlPath;


    public LoginNativeCall(WebView mWebView, Activity mActivity,Context mContext) {
        this.mWebView = mWebView;
        this.mActivity = mActivity;
        this.mContext = mContext;
    }

    public void doAppLogIn(final String responseData, String pushServiceID, String appName) {

        if(responseData==null || pushServiceID == null || appName == null)
            return;

        this.pushServiceID = pushServiceID;
        this.appName = appName;

        try {

            JSONObject jsonObjectResponse = new JSONObject(responseData);

            if (jsonObjectResponse == null) return;
            // Change userAgent string to default.


            String nextButtonCallback = LoginUtility.getStringObjectValue(jsonObjectResponse, "nextButtonCallback");
            appAuthToken = LoginUtility.getStringObjectValue(jsonObjectResponse, "authToken");
            appSecretKey = LoginUtility.getStringObjectValue(jsonObjectResponse, "authSecretKey");

            saveTokenSecretKey(jsonObjectResponse);



            boolean launchNative = LoginUtility.getJsonObjectBooleanValue(jsonObjectResponse, "launchNative");
            JSONObject jsonUserResponse = (JSONObject) LoginUtility.getJsonObjectValue(jsonObjectResponse, "response");

            boolean ifSocialMediaLogin = LoginUtility.getJsonObjectBooleanValue(jsonObjectResponse, "isFromSocialMedia");
            String launchNextpage = LoginUtility.getStringObjectValue(jsonObjectResponse, "launchNextpage");
            String querystring = LoginUtility.getStringObjectValue(jsonObjectResponse, "querystring");

            //user device push info to server from client side api
            callUserDevicePushInfoApi(jsonObjectResponse);

            if (!launchNative) {

                jsonObjectResponse.put("userDeviceObj", LoginUtility.getUserDevicePushInfo(mContext));

                if (ifSocialMediaLogin && !TextUtils.isEmpty(launchNextpage)) {
                    mWebView.loadUrl(buildUrlWithSecretKeyIntegrations(launchNextpage, querystring));
                }

                LoginUtility.callJavaScriptFunction(mActivity, mWebView, nextButtonCallback, jsonObjectResponse);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void saveTokenSecretKey(JSONObject jsonObject) {
        try {
            String appAuthToken = (String) LoginUtility.getJsonObjectValue(jsonObject, "authToken");
            String roleName = (String) LoginUtility.getJsonObjectValue(jsonObject, "roleName");
            String appSecretKey = (String) LoginUtility.getJsonObjectValue(jsonObject, "authSecretKey");
            long expiredTime = jsonObject.has("expiredTime") ? jsonObject.getLong("expiredTime") : 0;

            LoginPref mybeepsPref = new LoginPref(mContext);
            mybeepsPref.setValue(LoginConstant.AppPref.AUTH_TOKEN, appAuthToken);
            mybeepsPref.setValue(LoginConstant.AppPref.AUTH_SECRET_KEY, appSecretKey);
            mybeepsPref.setLongValue(LoginConstant.AppPref.EXPIRED_TIME, expiredTime);
            mybeepsPref.setValue(LoginConstant.AppPref.ROLE_NAME, roleName);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public  void callUserDevicePushInfoApi(JSONObject jsonObjMain) {
        try {
            String apiName = LoginUtility.getStringObjectValue(jsonObjMain, "apiName");
            String appName = LoginUtility.getStringObjectValue(jsonObjMain, "appName");
            JSONObject addDeviceObject = (JSONObject) LoginUtility.getJsonObjectValue(jsonObjMain, "addDeviceObject");
            String roleName = LoginUtility.getStringObjectValue(addDeviceObject, "roleName");
            if (addDeviceObject == null) {
                addDeviceObject = new JSONObject();
            }
            addDeviceObject.put("userDeviceObj", getUserDevicePushInfo(roleName));
            addDeviceObject.put("appName", appName);
            LoginRestApiClientEvent restApiClientEvent = new LoginRestApiClientEvent(mContext, apiName);
            restApiClientEvent.setRequestJson(addDeviceObject);
            restApiClientEvent.setListener(new IWebSocketClientEvent() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    System.out.println(jsonObject != null ? jsonObject.toString() : "error");
                    if(jsonObject != null)
                        Log.e(TAG, "process===" + jsonObject.toString());
                    else
                        Log.i(TAG, "error===" + jsonObject.toString());
                }

                @Override
                public void onFailure(Error error) {
                    Log.e(TAG, "error===" + error.toString());
                }


            });

            restApiClientEvent.setLooper(Looper.getMainLooper());
            restApiClientEvent.fire();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String buildUrlWithSecretKeyIntegrations(String renderPageName, String queryStringFromUrl) {
        StringBuilder sb = new StringBuilder();

        LoginPref pref = new LoginPref(mActivity);
        appAuthToken = pref.getValue(LoginConstant.AppPref.AUTH_TOKEN);
        appSecretKey = pref.getValue(LoginConstant.AppPref.AUTH_SECRET_KEY);
        roleName = pref.getValue(LoginConstant.AppPref.ROLE_NAME);

        File sandboxFile = new File(LoginUtility.getHtmlDirFromSandbox(mContext) + File.separator
                + renderPageName);
        if (sandboxFile != null && sandboxFile.exists()) {
            redirectHtmlPath = Uri.fromFile(sandboxFile).toString();
        }

            redirectHtmlPath = new LoginPref(mContext).getValue("stargateUrl").replace("index.html", renderPageName);

        sb.append(redirectHtmlPath);
        sb.append("?queryMode=" + (TextUtils.isEmpty(LoginUtility.getPaypalPageName("queryMode",mContext))
                ? LoginConstant.FileName.DEFAULT_QUERY_MODE : LoginUtility.getPaypalPageName("queryMode", mContext)));
        sb.append("&tokenKey=").append(appAuthToken);
        sb.append("&secretKey=").append(appSecretKey);
        sb.append("&expiredTime=").append(pref.getLongValue(LoginConstant.AppPref.EXPIRED_TIME));
        sb.append("&roleName=").append(roleName);
        sb.append("&userID=").append(" ");
        if (!TextUtils.isEmpty(queryStringFromUrl)) {
            sb.append("&").append(queryStringFromUrl);
        }
        String extraparams = LoginUtility.getPaypalPageName("extraparams", mContext);
        if (!TextUtils.isEmpty(extraparams)) {
            sb.append("&").append(extraparams);
        }

        return sb.toString();
    }

    public  JSONObject getUserDevicePushInfo(String roleName) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("pushServiceType", LoginConstant.PUSH_SERVICE_TYPE.ANDROID);
            jsonObject.put("deviceName", LoginUtility.getDeviceName());
            jsonObject.put("pushServiceID", pushServiceID);
            jsonObject.put("app", appName);
            if (roleName != null && !TextUtils.isEmpty(roleName)) {
                jsonObject.put("roleName", roleName);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
