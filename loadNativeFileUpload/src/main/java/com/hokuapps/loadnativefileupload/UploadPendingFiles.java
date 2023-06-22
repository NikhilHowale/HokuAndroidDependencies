package com.hokuapps.loadnativefileupload;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

import com.hokuapps.loadnativefileupload.backgroundtask.RoofingUploader;
import com.hokuapps.loadnativefileupload.dao.AppMediaDetailsDAO;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
import com.hokuapps.loadnativefileupload.restrequest.ServiceRequest;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class UploadPendingFiles {

    private Context mContext;
    private WebView mWebView;
    private Activity mActivity;
    private boolean isGetAllFileStatusCalled = false;
    private String[] requiredJSONObjectKey = {};
    private final String missingKeys = "Missing keys = ";
    private final boolean whileDebuggingShowMissingAlert = false;
    private String fileStatusCallBackFunction = "";
    private String syncOfflineNextButtonCallBack = "";
    private String objParams = "";
    private String appAuthToken = "";
    private int instucationNumberClockIn = 0;

    public UploadPendingFiles(Context mContext, String appAuthToken,WebView webView,Activity activity) {
        this.mContext = mContext;
        this.appAuthToken = appAuthToken;
        this.mWebView = webView;
        this.mActivity = activity;
    }

    public void uploadPendingFiles(final String fileStatusRes){
        try {
            if (TextUtils.isEmpty(fileStatusRes)) return;

            requiredJSONObjectKey = new String[]{"offlineDataID", "fileNm", "appID"};
            String missingKeysMsg = FileUploadUtility.showAlertBridgeMissingKeys(mContext, fileStatusRes, requiredJSONObjectKey);
            if (whileDebuggingShowMissingAlert && !missingKeysMsg.equals(missingKeys) && BuildConfig.DEBUG) {
                FileUploadUtility.showAlertMessage(mContext, missingKeysMsg, "retryFile");
                return;
            }

            JSONObject jOFileStatus = new JSONObject(fileStatusRes);
            syncOfflineNextButtonCallBack = FileUploadUtility.getStringObjectValue(jOFileStatus, "nextButtonCallback");
            String offlineDataID = FileUploadUtility.getStringObjectValue(jOFileStatus, "offlineDataID");
            String filename = FileUploadUtility.getStringObjectValue(jOFileStatus, "fileName");
            String appID = FileUploadUtility.getStringObjectValue(jOFileStatus, "appID");
            String filePath = FileUploadUtility.getStringObjectValue(jOFileStatus, "filePath");
            objParams = FileUploadUtility.getStringObjectValue(jOFileStatus, "objParams");

            retryFileUploadClient(appID, filename, offlineDataID, filePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


     /* retry file upload if file is failed during upload
     *
             * @param appID
     * @param fileName
     * @param offlineID
     */
    private void retryFileUploadClient(String appID, String fileName, String offlineID, String filePath) {
        File file;
        if (filePath != null && !filePath.isEmpty())
            file = new File(filePath);
        else
            file = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator + fileName);

        AppMediaDetails appMediaDetails = AppMediaDetailsDAO.getAppMediaDetailsByFileName(mContext, offlineID, fileName);

        //save as in progress
        appMediaDetails.setUploadStatus(AppMediaDetails.UPLOAD_INPROGRESS);

        appMediaDetails.save(mContext);

        instucationNumberClockIn = appMediaDetails.getInstructionNumber();

        if (appMediaDetails == null) {
            return;
        }

        RoofingUploader roofingUploader = RoofingUploader.getInstance(appMediaDetails,mContext);
        roofingUploader.setFilePath(file.getPath());
        roofingUploader.setAppID(appID);
        roofingUploader.setAppsServerToken(appAuthToken);
        roofingUploader.setUiCallBack(new RoofingUploader.IUICallBackRoofing() {
            @Override
            public void onSuccess(ServiceRequest serviceRequest) {
                // call javascript to update ui
                if (serviceRequest != null && serviceRequest.getAppMediaDetails() != null) {
                    updateFileStatus(serviceRequest.getAppMediaDetails().getOfflineDataID());
                }
            }

            @Override
            public void onFailure(ServiceRequest serviceRequest) {
                // call javascript to update ui
                if (serviceRequest != null && serviceRequest.getAppMediaDetails() != null) {
                    updateFileStatus(serviceRequest.getAppMediaDetails().getOfflineDataID());
                }
            }
        });

        roofingUploader.startUpload();
    }


    /**
     * update file status on summary page by its offlineID
     *
     * @param offlineDataID
     */
    private void updateFileStatus(String offlineDataID) {
        if (isGetAllFileStatusCalled && !TextUtils.isEmpty(fileStatusCallBackFunction)) {
            JSONObject jsonObjectFileStatus = getAllFileStatusList(offlineDataID);

            FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, fileStatusCallBackFunction, jsonObjectFileStatus);
        } else if (!TextUtils.isEmpty(syncOfflineNextButtonCallBack)) {
            JSONObject jsonObjectFileStatus = getAllFileStatusList(offlineDataID);
            if (objParams != null && !objParams.isEmpty()) {
                try {
                    jsonObjectFileStatus.put("objParams", objParams);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, syncOfflineNextButtonCallBack, jsonObjectFileStatus);
        }
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
