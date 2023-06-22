package com.hokuapps.loadnativefileupload;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

import com.hokuapps.loadnativefileupload.dao.AppMediaDetailsDAO;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetAllFileStatus {

    private Context mContext;
    private WebView mWebView;
    private Activity mActivity;
    private String[] requiredJSONObjectKey = {};
    private final String missingKeys = "Missing keys = ";
    private final boolean whileDebuggingShowMissingAlert = false;
    private String fileStatusCallBackFunction = "";
    private boolean isGetAllFileStatusCalled = false;

    public GetAllFileStatus(Activity activity, Context context, WebView webView) {
        this.mActivity = activity;
        this.mContext = context;
        this.mWebView = webView;
    }

    public void getAllFileStatus(final String fileStatusRes) {
        try {
            getAllFileStatusClient(fileStatusRes);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    private void getAllFileStatusClient(String fileStatusRes) throws Exception {
        if (TextUtils.isEmpty(fileStatusRes)) return;

        requiredJSONObjectKey = new String[]{"offlineDataID", "nextButtonCallback"};
        String missingKeysMsg = FileUploadUtility.showAlertBridgeMissingKeys(mContext, fileStatusRes, requiredJSONObjectKey);
        if (whileDebuggingShowMissingAlert && !missingKeysMsg.equals(missingKeys) && BuildConfig.DEBUG) {
            FileUploadUtility.showAlertMessage(mContext, missingKeysMsg, "getAllFileStatus");
            return;
        }

        JSONObject jOFileStatus = new JSONObject(fileStatusRes);
        String offlineDataID = FileUploadUtility.getStringObjectValue(jOFileStatus, "offlineDataID");
        String appID = FileUploadUtility.getStringObjectValue(jOFileStatus, "appID");

        fileStatusCallBackFunction = FileUploadUtility.getStringObjectValue(jOFileStatus, "nextButtonCallback");
        isGetAllFileStatusCalled = FileUploadUtility.getJsonObjectBooleanValue(jOFileStatus, "isGetAllFileStatus");

        JSONObject jsonObjectFileStatus = getAllFileStatusList(offlineDataID);

        FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, fileStatusCallBackFunction, jsonObjectFileStatus);
    }

    private JSONObject getAllFileStatusList(String offlineDataID) {

        try {
            JSONObject jsonObjectResponse = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            ArrayList<AppMediaDetails> appMediaDetailsArrayList = AppMediaDetailsDAO.getAppMediaDetailsListByOfflineID(
                    mContext, offlineDataID);
            String mapFileMediaID = "";
            String mapFileName = "";
            int mapFileStatus = 0;
            String caption = "";
            String mapPlanFileMediaID = "";
            String mapPlanFileName = "";
            String mapPlanS3FilePath = "";
            int mapPlanStatus = 0;

            for (AppMediaDetails appMediaDetails : appMediaDetailsArrayList) {
                if (appMediaDetails.getImageType() == AppMediaDetails.MAP_PLAN_IMAGE_TYPE) {
                    mapPlanFileMediaID = appMediaDetails.getMediaID();
                    mapPlanFileName = appMediaDetails.getFileName();
                    mapPlanS3FilePath = appMediaDetails.getS3FilePath();
                    mapPlanStatus = appMediaDetails.getUploadStatus();
                } else if (appMediaDetails.getImageType() == AppMediaDetails.MAP_IMAGE_TYPE) {
                    mapFileMediaID = appMediaDetails.getMediaID();
                    mapFileName = appMediaDetails.getFileName();
                    mapFileStatus = appMediaDetails.getUploadStatus();
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("step", appMediaDetails.getInstructionNumber());
                    jsonObject.put("fileNm", appMediaDetails.getFileName());
                    jsonObject.put("mediaID", appMediaDetails.getMediaID());
                    jsonObject.put("S3FilePath", appMediaDetails.getS3FilePath());
                    jsonObject.put("status", appMediaDetails.getUploadStatus());
                    jsonObject.putOpt("caption", appMediaDetails.getImageCaption());
                    jsonArray.put(jsonObject);
                }
                caption = appMediaDetails.getImageCaption();

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

            jsonObjectResponse.put("mapPlanStatus", mapPlanStatus);
            jsonObjectResponse.put("mapFileStatus", mapFileStatus);


            return jsonObjectResponse;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject();
    }
}
