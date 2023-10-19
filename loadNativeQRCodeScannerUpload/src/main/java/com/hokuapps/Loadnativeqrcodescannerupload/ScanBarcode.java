package com.hokuapps.Loadnativeqrcodescannerupload;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.webkit.WebView;

import com.google.android.gms.vision.barcode.Barcode;
import com.hokuapps.Loadnativeqrcodescannerupload.activity.BarcodeCaptureActivity;
import com.hokuapps.Loadnativeqrcodescannerupload.utils.AppConstant;
import com.hokuapps.Loadnativeqrcodescannerupload.utils.BarcodeConstant;
import com.hokuapps.Loadnativeqrcodescannerupload.utils.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ScanBarcode {
    @SuppressLint("StaticFieldLeak")
    private static ScanBarcode mInstance = null;

    public static ScanBarcode getInstance() {
        if(mInstance == null){
            mInstance = new ScanBarcode();
        }
        return mInstance;
    }


    /**
     * This function are use for to launch BarcodeCaptureActivity
     * @param activity activity context for launch activity
     * @param resData JSON data to provide extra information
     */
    public void launchQRCodeScanner(Activity activity, String resData ){
        try {
            JSONObject jsonObject = new JSONObject(resData);

            AppConstant.USERConstant.USER_NAME = Utility.getStringObjectValue(jsonObject,"userName");
            AppConstant.USERConstant.USER_ID = Utility.getStringObjectValue(jsonObject,"userID");
            AppConstant.URLConstant.AUTH_TOKEN = Utility.getStringObjectValue(jsonObject,"tokenKey");
            AppConstant.URLConstant.AUTH_SECRET_KEY = Utility.getStringObjectValue(jsonObject,"secretKey");

            BarcodeCaptureActivity.launchBarcodeReaderActivity(activity, true,false, resData);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *  This method handle the result of scan QRCode
     * @param activity activity context for launch activity
     * @param mWebView webView reference for return result to web page
     * @param intent intent provide scan data which contain scan result
     */
    public void handleScanResult(Activity activity, WebView mWebView, Intent intent){
        try {
            if (intent != null && intent.getExtras() != null) {
                String requestJson = intent.getExtras().getString(BarcodeConstant.IntentExtras.REQUEST_JSON_OBJ_STR);
                boolean isFromHeaderClick = intent.getExtras().getBoolean("isFromHeaderClick", false);
                if (!TextUtils.isEmpty(requestJson)) {
                    JSONObject jsonObject = new JSONObject(requestJson);
                    String nextCallBackButton = Utility.getStringObjectValue(jsonObject, "nextButtonCallback");
                    JSONObject requestJsonData = new JSONObject();
                    if (isFromHeaderClick) {
                        String callBackObj = intent.getExtras().getString(BarcodeConstant.IntentExtras.REQUEST_CALL_BACK_OBJ);
                        requestJsonData = new JSONObject(callBackObj);
                    } else {
                        if(intent.hasExtra(BarcodeConstant.IntentExtras.MULTIPLE_BARCODE_OBJECT)) {
                            ArrayList<String> barcodes = intent.getStringArrayListExtra(BarcodeConstant.IntentExtras.MULTIPLE_BARCODE_OBJECT);
                            JSONArray jsonArray = new JSONArray();
                            if (barcodes != null && barcodes.size() > 0) {
                                for (int i = 0; i < barcodes.size(); i++) {
                                    jsonArray.put(barcodes.get(i));
                                }
                            }
                            requestJsonData.put("data", jsonArray);
                        }
                        if(intent.hasExtra(BarcodeConstant.IntentExtras.BARCODE_OBJECT)){
                            Barcode barcode = intent.getParcelableExtra(BarcodeConstant.IntentExtras.BARCODE_OBJECT);
                            requestJsonData.put("data", barcode.displayValue);
                        }
                    }
                    Utility.callJavaScriptFunction(activity, mWebView, nextCallBackButton, requestJsonData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
