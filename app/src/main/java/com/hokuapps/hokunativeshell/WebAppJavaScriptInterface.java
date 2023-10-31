package com.hokuapps.hokunativeshell;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.activity.result.ActivityResultLauncher;

import com.cometchat.pro.uikit.CometChatStart;
import com.hokuapps.Loadnativeqrcodescannerupload.ScanBarcode;
import com.hokuapps.biometricauthentication.AuthenticateWithTouch;
import com.hokuapps.calendaroprations.CalendarOperations;
import com.hokuapps.getbackgroundlocationupdates.Locations.BackgroundLocation;
import com.hokuapps.getcurrentlocationdetails.LocationDetails;
import com.hokuapps.getlocalimage.GetLocalImage;
import com.hokuapps.getnetworkstatus.GetNetworkStatus;
import com.hokuapps.hokunativeshell.activity.WebAppActivity;
import com.hokuapps.hokunativeshell.constants.AppConstant;
import com.hokuapps.hokunativeshell.constants.IntegrationManager;
import com.hokuapps.hokunativeshell.pref.MybeepsPref;
import com.hokuapps.hokunativeshell.utils.Utility;
import com.hokuapps.loadmapviewbyconfig.LoadMapViewByConfig;
import com.hokuapps.loadnativefileupload.DownloadPhoto;
import com.hokuapps.loadnativefileupload.GetAllFileStatus;
import com.hokuapps.loadnativefileupload.NativeFileUpload;
import com.hokuapps.loadnativefileupload.SendOfflineMediaDetails;
import com.hokuapps.loadnativefileupload.UploadPendingFiles;
import com.hokuapps.loginnativecall.LoginNativeCall;
import com.hokuapps.loginwithfb.LoginWithFB;
import com.hokuapps.loginwithgoogle.SignInWithGoogle;
import com.hokuapps.logoutnativecall.LogoutNativeCall;
import com.hokuapps.shareappdata.ShareAppData;
import com.hokuapps.shownativecarousel.ShowNativeCarousel;
import com.hokuapps.startvideocall.StartVideoCall;
import com.hokuapps.updateappversion.UpdateAppVersion;

import org.json.JSONObject;



public class WebAppJavaScriptInterface {

    public WebAppActivity mWebAppActivity;
    public WebView mWebView;
    public MybeepsPref mybeepsPref;
    private String appAuthToken = "";
    private String appSecretKey = "";

    private Context mContext;

    ActivityResultLauncher webAppResultLauncher;


    public WebAppJavaScriptInterface(WebAppActivity c, WebView webView, Context context, ActivityResultLauncher activityResultLauncher) {
        mWebAppActivity = c;
        mWebView = webView;
        mContext = context;
        mybeepsPref = new MybeepsPref(context);
        webAppResultLauncher = activityResultLauncher;
    }

    @JavascriptInterface
    public void proceedToApp(final String targetData) {

        mWebAppActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("BRIDGE", "ProceedTOApp BRidge Call");

                if (mWebAppActivity.notificationData != null && !mWebAppActivity.notificationData.isEmpty()) {
                    mWebAppActivity.handleNotificationDataBackground(mWebAppActivity.notificationData);
                } else {
                    Intent intent = new Intent(mContext, WebAppActivity.class);
                    intent.putExtra(AppConstant.EXTRA_FILENAME_URL, AppConstant.FileName.DEFAULT_FILE_NAME);
                    intent.putExtra("isScanQrCode", false);
                    intent.putExtra("isLoadLocalHtml", true);
                    intent.putExtra(AppConstant.EXTRA_IS_HIDE_MOBILE_HEADER, true);
                    intent.putExtra("isFromAuth", !Utility.isEmpty(mybeepsPref.getValue(AppConstant.LOGGED_IN_USER_ID)));
                    intent.putExtra("launchPageName", "");
                    mWebAppActivity.loadWebpageFromFileNameOrURL(intent);
                }

                //call remove_splash in 2000 miSec
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Log.i("BRIDGE", "stop Splash");
                                mWebAppActivity.showSplashIcon(false);
                            }
                        },
                        3000);
            }
        });


    }

    @JavascriptInterface
    public void updateToken(final String data) {
        try {
            JSONObject requestJsonObj = new JSONObject(data);
            String token = (String) Utility.getJsonObjectValue(requestJsonObj, "idToken");
            if (token != null && !TextUtils.isEmpty(token)) {
                mybeepsPref.setValue(AppConstant.AppPref.AUTHORIZATION_KEY,token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void loginNativeCallV2(final String responseData) {
        try {
            JSONObject jsonObjectResponse = new JSONObject(responseData);
            appAuthToken = Utility.getStringObjectValue(jsonObjectResponse, "authToken");
            appSecretKey = Utility.getStringObjectValue(jsonObjectResponse, "authSecretKey");
            mybeepsPref.setValue(AppConstant.AppPref.AUTH_TOKEN,appAuthToken);
            mybeepsPref.setValue(AppConstant.AppPref.AUTH_SECRET_KEY,appSecretKey);
            JSONObject jsonUserResponse = (JSONObject) Utility.getJsonObjectValue(jsonObjectResponse, "response");
            JSONObject responseUser = (JSONObject) Utility.getJsonObjectValue(jsonUserResponse, "user");
            String userId = Utility.getStringObjectValue(responseUser, "userId");
            mybeepsPref.setValue(AppConstant.LOGGED_IN_USER_ID,userId);

            LoginNativeCall loginNativeCall = new LoginNativeCall(mWebView,mWebAppActivity,mContext);
            loginNativeCall.doAppLogIn(responseData,mybeepsPref.getValue(AppConstant.NOTIFICATION_TOKEN), Utility.getResString(R.string.app_name));

            // Initialize cometChat for notification to work before opening chat
            CometChatStart.getInstance().startCometChatInit(mWebAppActivity,responseData,mybeepsPref.getValue(AppConstant.NOTIFICATION_TOKEN),false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void logoutNativeCall(final String logoutNativeRes) {
        LogoutNativeCall logoutNativeCall = new LogoutNativeCall(mWebView,mWebAppActivity,mContext);
        logoutNativeCall.doLogout(logoutNativeRes);
        SignInWithGoogle.socialMediaLogout(logoutNativeRes);
    }

    @JavascriptInterface
    public void getNetworkStatus(final String targetData) {
        GetNetworkStatus getNetworkStatus = new GetNetworkStatus(mContext,mWebAppActivity,mWebView);
        getNetworkStatus.getMobileNetworkStatus(targetData);
    }

    @JavascriptInterface
    public void authenticateWithTouchID(final String data) {
        try {
            AuthenticateWithTouch authenticateWithTouch = new AuthenticateWithTouch(mWebAppActivity,mWebView);
            authenticateWithTouch.authenticateWithTouchID(data);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @JavascriptInterface
    public void shareAppData(final String shareObj) {
        try {
            ShareAppData shareAppData = new ShareAppData();
            shareAppData.shareAppData(mContext, shareObj, Utility.getResString(R.string.app_name));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @JavascriptInterface
    public void getCurrentLocationDetails(final String locationDetailsRes) {
        try {
            LocationDetails locationDetails = new LocationDetails(mWebView,mContext,mWebAppActivity);
            locationDetails.getCurLocationLatLong(locationDetailsRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void getCurrentLatLong(final String currentLatLong) {
        try {
            LocationDetails locationDetails = new LocationDetails(mWebView,mContext,mWebAppActivity);
            locationDetails.getCurLocationLatLong(currentLatLong);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void calendarOperations(final String eventData){
        try {
            CalendarOperations calendarOperations = new CalendarOperations(mContext);
            calendarOperations.performCalendarOperations(mWebAppActivity,eventData,mWebView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @JavascriptInterface
    public void updateAppVersion(String data) {
        try{
            UpdateAppVersion updateAppVersion = new UpdateAppVersion(mContext,mWebAppActivity,mWebView);
            updateAppVersion.doUpdateAppVersion(data, BuildConfig.APPLICATION_ID);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @JavascriptInterface
    public void LoadNativeFileUpload(final String responseData) {
        try {

            NativeFileUpload nativeFileUpload = NativeFileUpload.getInstance();
            nativeFileUpload.initialization(mWebView, mWebAppActivity,
                    mContext, IntegrationManager.APP_FILE_URL,
                    BuildConfig.AUTHORITY);

            nativeFileUpload.setAuthDetails(responseData);
            nativeFileUpload.loadNativeFileUpload(responseData);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void getAllFileStatus(String data){
        new GetAllFileStatus(mWebAppActivity,mContext,mWebView,BuildConfig.AUTHORITY).getAllFileStatus(data);
    }

    @JavascriptInterface
    public void sendOfflineMediaDetails(final String responseData){
        new SendOfflineMediaDetails(mContext,mWebView,mWebAppActivity).sendOfflineMediaDetails(responseData);
    }

    @JavascriptInterface
    public void uploadPendingFiles(final String fileStatusRes) {
        UploadPendingFiles pendingFiles = new UploadPendingFiles(mContext,mWebView,mWebAppActivity,BuildConfig.AUTHORITY);
        pendingFiles.setAuthDetails(fileStatusRes);
        pendingFiles.uploadPendingFiles(fileStatusRes);
    }


    @JavascriptInterface
    public void initCometChat(final String shareObj){
        CometChatStart.getInstance().startCometChatInit(mWebAppActivity, shareObj, mybeepsPref.getValue(AppConstant.NOTIFICATION_TOKEN),false);
    }

    @JavascriptInterface
    public void startCometChat(final String data) {
        try {

            CometChatStart.getInstance().cometChatSetup(
                    mWebAppActivity,
                    data,
                    mybeepsPref.getValue(AppConstant.NOTIFICATION_TOKEN),
                    false,
                    mWebView
            );
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void openCometChatDashBoard(final String data){
        try {
            CometChatStart.getInstance().cometChatSetup(
                    mWebAppActivity,
                    data,
                    mybeepsPref.getValue(AppConstant.NOTIFICATION_TOKEN),
                    true,
                    mWebView
            );

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void loadNativeQRCodeScannerUpload(final String resData) {
        ScanBarcode.getInstance().launchQRCodeScanner(mWebAppActivity,resData);
    }

    @JavascriptInterface
    public void loadMapViewByConfig(final String respData){
        String app_id = BuildConfig.APPLICATION_ID;
        boolean html = BuildConfig.LOAD_HTML_DIRECTLY;
        new LoadMapViewByConfig(mContext,mWebAppActivity,1,app_id,html).loadMapViewByConfig(respData);
    }

    @JavascriptInterface
    public void getLocalImage(final String response){
        new GetLocalImage(mWebView,mWebAppActivity).getLocalImage(response);
    }

    @JavascriptInterface
    public void downloadPhoto(final String downloadPhoto) {
        DownloadPhoto.getInstance().initialize(mWebAppActivity);
        DownloadPhoto.getInstance().downloadPhoto(downloadPhoto);
    }

    @JavascriptInterface
    public void sendBackgroundLocationToServer(String responseData){
        try {
            mWebAppActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BackgroundLocation backgroundLocation = BackgroundLocation.getInstance();
                    backgroundLocation.startBackgroundLocationService(responseData,mContext,Utility.getResString(R.string.app_name),Utility.getBytes(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher)) );
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void showNativeCarousel(final String resData) {
        ShowNativeCarousel showNativeCarousel = new ShowNativeCarousel(mContext, mWebAppActivity);
        showNativeCarousel.showNativeCarousel(resData);
    }

    @JavascriptInterface
    public void getAppVersion(final String data){

    }

    @JavascriptInterface
    public void startVideoCall(final String data) {

        String appAuthToken = mybeepsPref.getValue(AppConstant.AppPref.AUTH_TOKEN);
        String appSecretKey = mybeepsPref.getValue(AppConstant.AppPref.AUTH_SECRET_KEY);
        StartVideoCall.getInstance().start(mWebAppActivity,data,appSecretKey,appAuthToken);
    }

    @JavascriptInterface
    public void loginWithGoogle(final String googleLogin) {
        mWebAppActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SignInWithGoogle.initGoogleSignIn(mWebAppActivity);
                SignInWithGoogle.googleSignOut();
                SignInWithGoogle.googleSignIn(mWebAppActivity,googleLogin);
            }
        });
    }

    @JavascriptInterface
    public void loginWithFB(final String googleLogin) {
        LoginWithFB.getInstance().init(mWebAppActivity,mWebView);
        LoginWithFB.getInstance().loginWithFB(googleLogin);
    }
}
