package com.hokuapps.loadnativefileupload;

import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.APP_MEDIA_ARRAY;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.CAPTION;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.DATA_DICTIONARY;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.FILE_NM;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_FILE_MEDIA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_FILE_NAME;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_FILE_NM;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_MEDIA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_S3_FILE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MEDIA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.S3_FILE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.STEP;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

import com.hokuapps.loadnativefileupload.dao.AppMediaDetailsDAO;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
import com.hokuapps.loadnativefileupload.models.JSResponseData;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SendOfflineMediaDetails {

    private final Context mContext;

    private ProgressDialog progressDialog;
    private JSResponseData jsResponseData;
    private final WebView mWebView;
    private final Activity mActivity;

    /**
     * Parameterized constructor
     * @param mContext context
     * @param mWebView webView reference
     * @param activity activity context
     */
    public SendOfflineMediaDetails(Context mContext, WebView mWebView, Activity activity) {
        this.mContext = mContext;
        this.mWebView = mWebView;
        this.mActivity = activity;
    }

    /**
     * entry point of sendOfflineMediaDetails module
     * @param responseData provide require data for fetch offline images
     */
    public void sendOfflineMediaDetails(final String responseData) {
        try {
            showOrHideProgressDialogPopup(true, mContext.getResources().getString(R.string.label_loading));

            parseJsResponseDataSendOfflineMediaDetails(responseData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * parse response data and set it to json object
     * @param responseData parse json response
     */
    private void parseJsResponseDataSendOfflineMediaDetails(String responseData) {
        try {
            JSONObject responseJsonObj = new JSONObject(responseData);
            JSResponseData jsResponseDataModel = new JSResponseData();

            jsResponseDataModel.setCallbackFunction((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "nextButtonCallback"));
            jsResponseDataModel.setOfflineID(FileUploadUtility.getStringObjectValue(responseJsonObj, "offlineDataID"));
            jsResponseDataModel.setResponseData(responseData);

            setJsResponseData(jsResponseDataModel);

            setClockInCallbackFunction();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * add data to json object and return it to callback function
     */
    private void setClockInCallbackFunction() {
        final JSONObject jsonObject = getAndWaitJsonArrayUploadedFiles();
        callJavaScriptFunction(getJsResponseData().getCallbackFunction(), jsonObject);
    }

    /**
     * add data to json object and return it to callback function
     * @param callbackFunction call javascript function
     * @param jsonObject provide image data to javascript callback
     */
    private void callJavaScriptFunction(final String callbackFunction, final JSONObject jsonObject) {

        showOrHideProgressDialogPopup(false, mContext.getResources().getString(R.string.label_loading));
        FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, callbackFunction, jsonObject);
        clearJsCallbackFunction();

    }

    /**
     *
     */
    private void clearJsCallbackFunction() {
        getJsResponseData().setCallbackFunction(null);

        if (getJsResponseData().getLocationMapModel() != null) {
            getJsResponseData().getLocationMapModel().setNextButtonCallback(null);
            getJsResponseData().getLocationMapModel().setCancelButtonCallback(null);
        }
    }

    /**
     * add data to json object
     * @return add file details to jsonObject and return
     */
    private JSONObject getAndWaitJsonArrayUploadedFiles() {

        try {
            JSONObject jsonObjectResponse = new JSONObject();
            jsonObjectResponse.put(DATA_DICTIONARY, new JSONObject(getJsResponseData().getResponseData()));
            JSONArray jsonArray = new JSONArray();

            if (FileUploadUtility.isAnyFileRenamingToUploadV2(mContext,getJsResponseData().getOfflineID())) {
                ArrayList<AppMediaDetails> appMediaDetailsArrayList = AppMediaDetailsDAO.getAppMediaDetailsListByOfflineID(mContext, getJsResponseData().getOfflineID());
                String mapFileMediaID = "";
                String mapFileName = "";
                String mapPlanFileMediaID = "";
                String mapPlanFileName = "";
                String mapPlanS3FilePath = "";
                for (AppMediaDetails appMediaDetails : appMediaDetailsArrayList) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(STEP, appMediaDetails.getInstructionNumber());
                    jsonObject.put(FILE_NM, appMediaDetails.getFileName());
                    jsonObject.put(MEDIA_ID, appMediaDetails.getMediaID());
                    jsonObject.put(S3_FILE_PATH, appMediaDetails.getS3FilePath());
                    jsonObject.putOpt(CAPTION, appMediaDetails.getImageCaption());
                    jsonArray.put(jsonObject);
                }

                jsonObjectResponse.put(APP_MEDIA_ARRAY, jsonArray);

                if (!TextUtils.isEmpty(mapFileMediaID)) {
                    jsonObjectResponse.put(MAP_FILE_MEDIA_ID, mapFileMediaID);
                }

                if (!TextUtils.isEmpty(mapFileName)) {
                    jsonObjectResponse.put(MAP_FILE_NAME, mapFileName);
                }

                if (!TextUtils.isEmpty(mapPlanFileMediaID)) {
                    jsonObjectResponse.put(MAP_PLAN_MEDIA_ID, mapPlanFileMediaID);
                }

                if (!TextUtils.isEmpty(mapPlanFileName)) {
                    jsonObjectResponse.put(MAP_PLAN_FILE_NM, mapPlanFileName);
                }

                if (!TextUtils.isEmpty(mapPlanS3FilePath)) {
                    jsonObjectResponse.put(MAP_PLAN_S3_FILE_PATH, mapPlanS3FilePath);
                }
            }

            return jsonObjectResponse;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject();
    }



    /**
     * get response data from json object
     * @return provide response data
     */
    public JSResponseData getJsResponseData() {
        return jsResponseData != null ? jsResponseData : new JSResponseData();
    }

    /**
     * set response data to json object
     * @param jsResponseData set response data
     */
    public void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }

    /**
     * show or hide progress bar with given message
     * @param shown flag decide to show ot hide dialog
     * @param message display message
     */
    private void showOrHideProgressDialogPopup(boolean shown, String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext, R.style.AppCompatAlertDialogStyle);

            progressDialog.setCancelable(false);
            progressDialog.setMessage(message);

        }

        if (shown) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
