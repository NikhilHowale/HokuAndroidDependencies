package com.cometchat.pro.uikit.ui_components.groups.group_details;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cometchat.pro.uikit.AppConfig;
import com.cometchat.pro.uikit.R;

import java.io.File;

public class MedicalDetailsActivity extends AppCompatActivity {

    private static final String TAG = "GroupStudentMedical";
    private String groupId = "";


    private final WebChromeClient webChromeClient = new WebChromeClient() {

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

        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            try {
                super.onGeolocationPermissionsHidePrompt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    };
    private String mUrl = "";
    private String filename = "";
    private WebView mWebView;
    private ProgressBar progressBar;

    /**
     * This method open medical details activity
     * @param activity activity context
     * @param bundle extra data
     */
    public static void  startActivity(android.app.Activity activity, Bundle bundle){
        Intent intent = new Intent(activity, MedicalDetailsActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.anim_from_right, R.anim.anim_from_left);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_details);
        initView();
        loadBundleData();
        loadUrl();
        setWebView(mUrl);
    }

    private void initView() {
        mWebView = findViewById(R.id.webviewMedic);
        progressBar = findViewById(R.id.progress);
    }

    private void loadBundleData() {
        if(getIntent() != null && getIntent().getExtras() != null) {
            groupId = getIntent().getExtras().getString("groupId");
            filename = AppConfig.MEDICAL_DETAILS;
        }
    }

    /**
     *  This method load the url to webView
     */
    private void loadUrl() {
        if(filename.length() == 0){
            return;
        }

        File sandboxFile = new File( AppConfig.HTML_DIRECTORY + File.separator + filename);
        mUrl = Uri.fromFile(sandboxFile).toString()+"?groupid=" + groupId;
        mUrl = buildUrlWithSecretKey();
    }

    private void setWebView(String url) {
        setWebViewSettings();
        mWebView.loadUrl(Uri.decode(url));
    }

    /**
     * This method build url with secret key and token key
     * @return return url
     */
    private String buildUrlWithSecretKey() {
        StringBuilder sb = new StringBuilder(mUrl);

        if(AppConfig.TOKEN_KEY != null && AppConfig.SECRET_KEY!= null) {
            sb.append("&tokenKey=").append(AppConfig.TOKEN_KEY);
            sb.append("&secretKey=").append(AppConfig.SECRET_KEY);
        }

        return sb.toString();
    }

    /**
     * Set web view settings
     */
    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void setWebViewSettings() {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        setupDebuggingModeForWebView();
        mWebView.addJavascriptInterface(new WebAppJavascriptInterface(this), "Android");
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                    WebView.setWebContentsDebuggingEnabled(true);
                }
            }

    }

    /**
     * Webview client for internal url loading
     * callback url: ""
     */
    public class AuthWebViewClient extends WebViewClient {
       /* @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }*/

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {


            return true;
        }

        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.INVISIBLE);
        }


        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            Log.e(TAG, "onReceivedSslError: " + error.toString());
        }

    }

    private class WebAppJavascriptInterface {

        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppJavascriptInterface(Context c) {
            mContext = c;
        }


        @JavascriptInterface
        public void backToChat(String response){
            finish();
        }

        @JavascriptInterface
        public void getNetworkStatus(String response){
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void writeLogs(String response){

        }

    }
}
