package com.hokuapps.hokunativeshell.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cometchat.pro.uikit.CometChatStart;
import com.hokuapps.Loadnativeqrcodescannerupload.ScanBarcode;
import com.hokuapps.biometricauthentication.AuthenticateWithTouch;
import com.hokuapps.getnetworkstatus.GetNetworkStatus;
import com.hokuapps.hokunativeshell.BuildConfig;
import com.hokuapps.hokunativeshell.R;
import com.hokuapps.hokunativeshell.WebAppJavaScriptInterface;
import com.hokuapps.hokunativeshell.constants.AppConstant;
import com.hokuapps.hokunativeshell.pref.MybeepsPref;
import com.hokuapps.hokunativeshell.receivers.NotificationReceiver;
import com.hokuapps.hokunativeshell.utils.Utility;
import com.hokuapps.loadmapviewbyconfig.LoadMapViewByConfig;
import com.hokuapps.loadnativefileupload.NativeFileUpload;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class WebAppActivity extends AppCompatActivity {

    private transient Context mContext;

    private String mUrl = "";
    private String mtitle = "";
    private String messageId = "";
    private String appUrl = "";
    private String integrationID = "";
    private String command;
    private boolean isAppCommand;
    private String fileName;
    private String queryMode;
    private String queryString;
    private String localUrl = "";
    private boolean isLFC;
    private boolean isHideMobileHeader;

    public String notificationData = null;


    private boolean isFromNotification = false;
    private String launchPageName = "";

    boolean isLoadLocalHtml;
    private boolean isFromAuthentication = false;

    private String appAuthToken = "";
    private String appSecretKey = "";
    private String roleName = "";
    private WebView mWebView;
    private MybeepsPref mybeepsPref;
    public static boolean isOpen = false;



    private WebAppJavaScriptInterface webAppJavaScriptInterface;

    private final WebChromeClient webChromeClient = new WebChromeClient() {

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return true;
        }

        /**
         * File upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
         * @param filePathCallback
         * @param acceptType
         */
        public void openFileChooser(ValueCallback filePathCallback, String acceptType) {
            try {

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * File upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
         * @param acceptType
         * @param capture
         */
        public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
            try {

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * File upload callback (Android 5.0 (API level 21) -- current) (public method)
         * @param webView
         * @param filePathCallback
         * @param fileChooserParams
         * @return
         */
        public boolean onShowFileChooser(WebView webView, final ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            try {

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            try {
                return super.onJsAlert(view, url, message, result);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            try {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return true;
        }

        public void onProgressChanged(WebView view, int progress) {
            try {
                //Make the bar disappear after URL is loaded, and changes string to Loading...
                setTitle("Loading...");
                setProgress(progress * 100); //Make the bar disappear after URL is loaded

                // Return the app name after finish loading
                if (progress == 100)
                    Utility.getResString(R.string.app_name);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            try {
                super.onGeolocationPermissionsHidePrompt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            try {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };


    public ActivityResultLauncher<Intent> authenticateForForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                    }
                }
            });




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webappactivity);

        mContext = WebAppActivity.this;
        isOpen = true;
        initView();

        webAppJavaScriptInterface = new WebAppJavaScriptInterface(this, mWebView,WebAppActivity.this,authenticateForForResult);
        mWebView.addJavascriptInterface(webAppJavaScriptInterface, "Android");

        mybeepsPref = new MybeepsPref(WebAppActivity.this);

        loadBundleData(getIntent());

        if(isFromNotification){
            showSplashIcon(false);
            boolean isCometChat = false;
            if(getIntent().getExtras().containsKey("isCometChat")){
                isCometChat = getIntent().getExtras().getBoolean("isCometChat");
            }
            CometChatStart.getInstance().handleCometChatNotification(this,notificationData, mybeepsPref.getValue(AppConstant.NOTIFICATION_TOKEN), isCometChat);
            CometChatStart.getInstance().handleWebChatNotification(this,notificationData,true);
        }

        if (loadWebpageFromFileNameOrURL(getIntent())) return;

    }

    private void initView() {
        mWebView = findViewById(R.id.webkitWebView1);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        //registerReceiver();
        new GetNetworkStatus(mContext,WebAppActivity.this,mWebView).registerNetworkReceiver();

        super.onResume();

        NotificationReceiver.setNotificationListener(new NotificationReceiver.NotificationListener() {
            @Override
            public void onReceive(Intent intent) {
                if (intent.getExtras() == null) return;

                showSplashIcon(false);

                if (intent.getAction().equalsIgnoreCase("android.net.conn.NOTIFICATION")) {
                    try {
                        notificationData = intent.getExtras().getString(AppConstant.EXTRA_NOTIFICATION_DATA);
                        handleNotificationDataBackground(notificationData);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new GetNetworkStatus(mContext,WebAppActivity.this,mWebView).unregisterNetworkReceiver();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

            try {
                if (resultCode == Activity.RESULT_OK) {
                    switch (requestCode) {
                        case AppConstant.ActivityResultCode.BIOMETRIC_RESULT_CODE:
                            new AuthenticateWithTouch(WebAppActivity.this, mWebView).handleActivityResultCallback(intent);
                            break;

                        case AppConstant.ActivityResultCode.CAPTURE_MEDIA_PHOTO:
                            NativeFileUpload.getInstance().handleImageResultIntent(intent);
                            break;

                        case AppConstant.ActivityResultCode.REQUEST_FILE_BROWSER:
                            NativeFileUpload.getInstance().handleFileBrowsing(intent);
                            break;

                        case AppConstant.ActivityResultCode.SCAN_IMAGE_REQUEST_CAMERA:

                        case AppConstant.ActivityResultCode.SCAN_IMAGE_REQUEST_GALLERY:
                            NativeFileUpload.getInstance().handleScanTextResult(this, requestCode, resultCode, intent);
                            break;

                        case AppConstant.ActivityResultCode.SELECT_GALLERY_IMAGE_CODE:
                            NativeFileUpload.getInstance().handleCustomImageGallery(intent);

                            break;
                        case AppConstant.ActivityResultCode.ACTION_REQUEST_EDIT_IMAGE:
                            NativeFileUpload.getInstance().handleFreeDrawingImage(intent);
                            break;

                        case AppConstant.ActivityResultCode.ACTION_MAP_GET_ADDRESS:
                            new LoadMapViewByConfig(this).handleMapResult(resultCode,intent,mWebView);
                            break;

                        case AppConstant.ActivityResultCode.RC_BARCODE_CAPTURE:
                            ScanBarcode.getInstance().handleScanResult(this,mWebView,intent);
                            break;

                        case AppConstant.ActivityResultCode.ACTION_REQUEST_EDIT_IMAGE_MAP_PLAN:
                            NativeFileUpload.getInstance().handleEditImagePlan(intent);
                            break;


                    }
                } else if(resultCode == RESULT_CANCELED){
                    switch (requestCode) {
                        case AppConstant.ActivityResultCode.ACTION_MAP_GET_ADDRESS:
                            new LoadMapViewByConfig(this).handleMapResult(resultCode, intent, mWebView);
                            break;
                    }
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }



        super.onActivityResult(requestCode, resultCode, intent);
    }


    public void showSplashIcon(boolean shown) {
        ImageView imageViewSplash = findViewById(R.id.splash_iv);
        imageViewSplash.setImageResource(R.drawable.splash);
        imageViewSplash.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    public boolean loadWebpageFromFileNameOrURL(Intent intent) {


        String filenameOrUrl = intent.getExtras().containsKey(AppConstant.EXTRA_FILENAME_URL)
                ? intent.getExtras().getString(AppConstant.EXTRA_FILENAME_URL) : null;

        isLoadLocalHtml = intent.getExtras().containsKey("isLoadLocalHtml") && intent.getExtras().getBoolean("isLoadLocalHtml");
        isFromAuthentication = intent.getExtras().containsKey("isFromAuth") && intent.getExtras().getBoolean("isFromAuth");
        boolean isScanQrCode = intent.getExtras().containsKey("isScanQrCode") && intent.getExtras().getBoolean("isScanQrCode");

        if (isScanQrCode) {
            isFromAuthentication = false;
        }

        String urlToLoad = isLoadLocalHtml
                ? Uri.fromFile(new File(Utility.getHtmlDirFromSandbox() + File.separator + filenameOrUrl)).toString()
                : filenameOrUrl;

        if (!TextUtils.isEmpty(urlToLoad)) {
            if (isFromAuthentication) {
                fileName = filenameOrUrl;
                isLFC = true;

                if (!isLoadLocalHtml) {
                    mUrl = urlToLoad + "?" + (TextUtils.isEmpty(queryMode) ? AppConstant.FileName.DEFAULT_QUERY_MODE : queryMode);
                }
                loadUrlForApp();
            } else {
                setWebView(urlToLoad);
            }
            return true;
        } else {
            return false;
        }

    }

    private void loadUrlForApp() {
        if (isLFC) {
            mUrl = isLoadLocalHtml ? Utility.getRedirectedUrl(fileName, queryMode, true) : fileName;

            if (isFromAuthentication) {
                mUrl = buildUrlWithSecretKey();
            }
        }
        if (!TextUtils.isEmpty(mUrl)) {
            setWebView(mUrl);
        }
    }

    private String buildUrlWithSecretKey() {
        StringBuilder sb = new StringBuilder(mUrl);

        MybeepsPref pref = new MybeepsPref(this);
        appAuthToken = pref.getValue(AppConstant.AppPref.AUTH_TOKEN);
        appSecretKey = pref.getValue(AppConstant.AppPref.AUTH_SECRET_KEY);

        sb.append("&tokenKey=").append(appAuthToken);
        sb.append("&secretKey=").append(appSecretKey);

        if (!TextUtils.isEmpty(queryString)) {
            sb.append("&").append(queryString);
        }

        sb.append("&pageName=").append(TextUtils.isEmpty(launchPageName) ? "aaaaa" : launchPageName);

        return sb.toString();
    }

    private void setWebView(String url) {
        mWebView = findViewById(R.id.webkitWebView1);
        setWebViewSettings();
        Log.e("@Startup","url==="+url);
        mWebView.loadUrl(url);
    }

    private void setWebViewSettings() {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        setupDebuggingModeForWebView();

        // By using this method together with the overridden method onReceivedSslError()
        // you will avoid the "WebView Blank Page" problem to appear. This might happen if you
        // use a "https" url!
        settings.setDomStorageEnabled(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        settings.setAllowFileAccess(true);

        mWebView.setWebViewClient(new AuthWebViewClient());


        mWebView.setWebChromeClient(webChromeClient);

        if (Build.VERSION.SDK_INT >= 19) {
            // chromium, enable hardware acceleration
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        //To accept thirt party cookies e.g facebook
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        }

    }

    private void setupDebuggingModeForWebView() {
        // debug web view on chrome
        if (BuildConfig.DEBUG) {
            if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
    }

    /**
     * This method get values from intent that are stored in bundle
     * @param intent intent
     */
    private void loadBundleData(Intent intent) {

        if (intent.getExtras() != null) {

            mUrl = intent.getExtras().getString(AppConstant.EXTRA_WEB_URL);
            mtitle = intent.getExtras().getString(AppConstant.EXTRA_SCREEN_TITLE);
            messageId = intent.getExtras().getString(AppConstant.EXTRA_MESSAGE_ID);
            appUrl = intent.getExtras().getString(AppConstant.EXTRA_APP_URL);
            integrationID = intent.getExtras().getString(AppConstant.EXTRA_INTEGRATION_ID);

            command = intent.getExtras().getString(AppConstant.EXTRA_APP_COMMAND);
            isAppCommand = intent.getExtras().getBoolean(AppConstant.EXTRA_IS_APP_COMMAND);
            isLFC = intent.getExtras().getBoolean(AppConstant.EXTRA_LFC);
            localUrl = intent.getExtras().getString(AppConstant.EXTRA_LOCAL_URL);
            queryMode = intent.getExtras().containsKey(AppConstant.EXTRA_QUERY_MODE)
                    ? intent.getExtras().getString(AppConstant.EXTRA_QUERY_MODE)
                    : "";
            queryString = intent.getExtras().containsKey(AppConstant.EXTRA_QUERY_STRING)
                    ? intent.getExtras().getString(AppConstant.EXTRA_QUERY_STRING)
                    : "";

            notificationData = intent.getExtras().containsKey(AppConstant.EXTRA_NOTIFICATION_DATA)
                    ? intent.getExtras().getString(AppConstant.EXTRA_NOTIFICATION_DATA)
                    : null;


            isHideMobileHeader = intent.getExtras().getBoolean(AppConstant.EXTRA_IS_HIDE_MOBILE_HEADER);

            isFromNotification = intent.getExtras().getBoolean("isFromNotification");

            launchPageName = intent.getExtras().getString("launchPageName");
        }
    }


    /**
     * This method handles background notification when an app is in the background or kill state
     * @param notificationData notification data when notification is clicked
     */
    public void handleNotificationDataBackground(String notificationData) {
        if (!TextUtils.isEmpty(notificationData) && BuildConfig.LOAD_HTML_DIRECTLY) {

            try {

                JSONObject object = new JSONObject(notificationData);

                boolean isRedirect = Utility.getJsonObjectBooleanValue(object, "isRedirect");

                if (isRedirect) {
                    JSONObject notifDataJsonObj = new JSONObject((String) Utility.getJsonObjectValue(object, "msg"));
                    String htmlSyncPageName = (String) Utility.getJsonObjectValue(notifDataJsonObj, "htmlSynchPage");
                    String queryMode = (String) Utility.getJsonObjectValue(notifDataJsonObj, "qm");
                    String queryString = Utility.getStringObjectValue(notifDataJsonObj, "querystring");

                    Intent notifyIntent = new Intent();
                    String fileName = !TextUtils.isEmpty(htmlSyncPageName) ? htmlSyncPageName : AppConstant.FileName.DEFAULT_FILE_NAME;
                    notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    notifyIntent.putExtra("targetId", "");
                    notifyIntent.putExtra(AppConstant.EXTRA_IS_HIDE_MOBILE_HEADER, true);
                    notifyIntent.putExtra("isFromNotification", true);
                    notifyIntent.putExtra("isIncomingCall", true);
                    notifyIntent.putExtra("isLoadLocalHtml", true);
                    notifyIntent.putExtra(AppConstant.EXTRA_FILENAME_URL, fileName);
                    notifyIntent.putExtra("isFromAuth", true);
                    notifyIntent.putExtra(AppConstant.EXTRA_LFC, true);
                    notifyIntent.putExtra(AppConstant.EXTRA_IS_APP_COMMAND, true);
                    notifyIntent.putExtra(AppConstant.EXTRA_QUERY_MODE, queryMode);
                    notifyIntent.putExtra(AppConstant.EXTRA_QUERY_STRING, queryString);
                    notifyIntent.putExtra(AppConstant.EXTRA_IS_HIDE_MOBILE_HEADER, true);

                    final String targetID = notifDataJsonObj.has("targetID") ?
                            (String) Utility.getJsonObjectValue(notifDataJsonObj, "targetID") :
                            (String) Utility.getJsonObjectValue(notifDataJsonObj, "targetId");

                    loadBundleData(notifyIntent);
                    loadWebpageFromFileNameOrURL(notifyIntent);

                    clearNotification();


                    return;
                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void clearNotification() {
        Utility.cancelNotification(WebAppActivity.this);
    }


    /**
     * This method calls activity with the webpage URL to load it into the web view
     * @param context context
     * @param filenameOrUrl webpage name or url
     * @param isScanQrCode if true when file is downloaded from aws otherwise false
     * @param isLoadLocalHtml if true then load a web page from the local directory
     * @param isHideMobileHeader if true then hide the toolbar otherwise false
     * @param isAuth if true load page with authorization otherwise false
     * @param activityClass activity
     * @param launchPageName launch page name
     */
    public static void loadWebPageForURLWithOrWithoutAuth(Context context,
                                                          String filenameOrUrl,
                                                          boolean isScanQrCode,
                                                          boolean isLoadLocalHtml,
                                                          boolean isHideMobileHeader,
                                                          boolean isAuth,
                                                          Class<? extends Context> activityClass,
                                                          String launchPageName) {
        Intent intent = new Intent(context, WebAppActivity.class);
        intent.putExtra(AppConstant.EXTRA_FILENAME_URL, filenameOrUrl);
        intent.putExtra("isScanQrCode", isScanQrCode);
        intent.putExtra("isLoadLocalHtml", isLoadLocalHtml);
        intent.putExtra(AppConstant.EXTRA_IS_HIDE_MOBILE_HEADER, isHideMobileHeader);
        intent.putExtra("Extra_Class_Activity", activityClass);
        intent.putExtra("isFromAuth", isAuth);
        intent.putExtra("launchPageName", launchPageName);
        context.startActivity(intent);
    }

    /**
     * Webview client for internal url loading
     * callback url: ""
     */
    static class AuthWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            try {

                Log.i("onStartup", "url====" + url);
                super.onPageStarted(view, url, favicon);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            try {
                if (!url.toLowerCase().contains("wvjbscheme://__BRIDGE_LOADED__".toLowerCase())) {
                    boolean isAvailable = !Utility.isLoadFromLocalHtmlDir(url);
                    view.loadUrl(url);
                    return true;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return true;
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            Log.i("onStartup", "url====" + url);
            super.onPageFinished(view, url);


        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            try {
                super.onReceivedError(view, errorCode, description, failingUrl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @TargetApi(23)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

            try {
                super.onReceivedError(view, request, error);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            //super.onReceivedSslError(view, handler, error);
            try {

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }



}
