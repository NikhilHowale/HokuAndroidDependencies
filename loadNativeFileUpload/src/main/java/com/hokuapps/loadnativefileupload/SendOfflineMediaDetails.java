package com.hokuapps.loadnativefileupload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

    private Context mContext;
    private String[] requiredJSONObjectKey = {};
    private final String missingKeys = "Missing keys = ";
    private final boolean whileDebuggingShowMissingAlert = false;
    private ProgressDialog progressDialog;
    private JSResponseData jsResponseData;
    private WebView mWebView;
    private Activity mActivity;

    public SendOfflineMediaDetails(Context mContext, WebView mWebView, Activity activity) {
        this.mContext = mContext;
        this.mWebView = mWebView;
        this.mActivity = activity;
    }

    public void sendOfflineMediaDetails(final String responseData) {
        try {

            showOrHideProgressDialogPopup(true);

            requiredJSONObjectKey = new String[]{"offlineDataID", "nextButtonCallback"};
            String missingKeysMsg = FileUploadUtility.showAlertBridgeMissingKeys(mContext, responseData, requiredJSONObjectKey);
            if (whileDebuggingShowMissingAlert && !missingKeysMsg.equals(missingKeys) && BuildConfig.DEBUG) {
                FileUploadUtility.showAlertMessage(mContext, missingKeysMsg, "sendOfflineMediaDetails");
                return;
            }

            parseJsResponseDataSendOfflineMediaDetails(responseData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseJsResponseDataSendOfflineMediaDetails(String responseData) {
        try {
            JSONObject responseJsonObj = new JSONObject(responseData);
            JSResponseData jsResponseDataModel = new JSResponseData();

            jsResponseDataModel.setCallbackfunction((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "nextButtonCallback"));
            jsResponseDataModel.setOfflineID(FileUploadUtility.getStringObjectValue(responseJsonObj, "offlineDataID"));
            jsResponseDataModel.setResponseData(responseData);

            setJsResponseData(jsResponseDataModel);

            setClockInCallbackFunction();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setClockInCallbackFunction() {
        final JSONObject jsonObject = getAndWaitJsonArrayUploadedFiles();
        callJavaScriptFunction(getJsResponseData().getCallbackfunction(), jsonObject);
    }

    private void callJavaScriptFunction(final String callbackFunction, final JSONObject jsonObject) {

        showOrHideProgressDialogPopup(false);
        FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, callbackFunction, jsonObject);
        clearJsCallbackFunction();

    }

    private void clearJsCallbackFunction() {
        getJsResponseData().setCallbackfunction(null);

        if (getJsResponseData().getLocationMapModel() != null) {
            getJsResponseData().getLocationMapModel().setNextButtonCallback(null);
            getJsResponseData().getLocationMapModel().setCancelButtonCallback(null);
        }
    }

    private JSONObject getAndWaitJsonArrayUploadedFiles() {

        try {
            JSONObject jsonObjectResponse = new JSONObject();
            jsonObjectResponse.put("dataDictionay", new JSONObject(getJsResponseData().getResponseData()));
            JSONArray jsonArray = new JSONArray();

            if (isAnyFileRenamingToUploadV2()) {
                ArrayList<AppMediaDetails> appMediaDetailsArrayList = AppMediaDetailsDAO.getAppMediaDetailsListByOfflineID(mContext, getJsResponseData().getOfflineID());
                String mapFileMediaID = "";
                String mapFileName = "";
                String mapPlanFileMediaID = "";
                String mapPlanFileName = "";
                String mapPlanS3FilePath = "";
                for (AppMediaDetails appMediaDetails : appMediaDetailsArrayList) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("step", appMediaDetails.getInstructionNumber());
                    jsonObject.put("fileNm", appMediaDetails.getFileName());
                    jsonObject.put("mediaID", appMediaDetails.getMediaID());
                    jsonObject.put("S3FilePath", appMediaDetails.getS3FilePath());
                    jsonObject.putOpt("caption", appMediaDetails.getImageCaption());
                    jsonArray.put(jsonObject);
                }

                jsonObjectResponse.put("appMediaArrayForUploadArray", jsonArray);

                if (!TextUtils.isEmpty(mapFileMediaID)) {
                    jsonObjectResponse.put("mapFileMediaID", mapFileMediaID);
                }

                if (!TextUtils.isEmpty(mapFileName)) {
                    jsonObjectResponse.put("mapFileName", mapFileName);
                }

                if (!TextUtils.isEmpty(mapPlanFileMediaID)) {
                    jsonObjectResponse.put("mapPlanMediaID", mapPlanFileMediaID);
                }

                if (!TextUtils.isEmpty(mapPlanFileName)) {
                    jsonObjectResponse.put("mapPlanFileNm", mapPlanFileName);
                }

                if (!TextUtils.isEmpty(mapPlanS3FilePath)) {
                    jsonObjectResponse.put("mapPlanS3FilePath", mapPlanS3FilePath);
                }
            }

            return jsonObjectResponse;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject();
    }


    private boolean isAnyFileRenamingToUploadV2() {
        ArrayList<AppMediaDetails> appMediaDetailsArrayList = AppMediaDetailsDAO.getAppMediaDetailsListByOfflineIDWithUploadedFile(mContext,
                getJsResponseData().getOfflineID(), false);
        for (AppMediaDetails appMediaDetails : appMediaDetailsArrayList) {
            try {
                while (!appMediaDetails.isUploadStatus()) {
                    AppMediaDetails amd = AppMediaDetailsDAO.getAppMediaDetailsListByOfflineIDWithUploadedFile(mContext,
                            getJsResponseData().getOfflineID(), !appMediaDetails.isUploadStatus(), appMediaDetails.getFileName());
                    if (amd != null) {
                        appMediaDetails = amd;
                    }
                    Thread.sleep(2000);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return true;
    }

    public JSResponseData getJsResponseData() {
        return jsResponseData != null ? jsResponseData : new JSResponseData();
    }

    public void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }

    private void showOrHideProgressDialogPopup(boolean shown) {
        showOrHideProgressDialogPopup(shown, mContext.getResources().getString(R.string.label_loading));
    }

    private void showOrHideProgressDialogPopup(boolean shown, String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(mContext, R.style.AppCompatAlertDialogStyle);
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            progressDialog.setCancelable(false);
            progressDialog.setMessage(message);

        }

        if (shown) {
            progressDialog.show();
        } else {
            //            progressDialog.hide();
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}
