package com.hokuapps.loadnativefileupload;

import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.APP_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.APP_MEDIA_ARRAY;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.AUTH_TOKEN;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.CAPTION;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.FILE_NAME;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.FILE_NM;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.FILE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_FILE_MEDIA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_FILE_NAME;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_FILE_NM;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_MEDIA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_S3_FILE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_STATUS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MEDIA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.NEXT_BUTTON_CALLBACK;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.OBJ_PARAMS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.OFFLINE_DATA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.S3_FILE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.STATUS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.STEP;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

import com.hokuapps.loadnativefileupload.backgroundtask.FileUploader;
import com.hokuapps.loadnativefileupload.dao.AppMediaDetailsDAO;
import com.hokuapps.loadnativefileupload.database.FileContentProvider;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
import com.hokuapps.loadnativefileupload.restrequest.ServiceRequest;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class UploadPendingFiles {

    private final Context mContext;
    private final WebView mWebView;
    private final Activity mActivity;
    private String syncOfflineNextButtonCallBack = "";
    private String objParams = "";
    private String serverAuthToken = "";
    private int instructionNumberClockIn = 0;


    /**
     * Parameterized constructor
     * @param mContext context
     * @param webView webView reference
     * @param activity activity context
     */
    public UploadPendingFiles(Context mContext,WebView webView,Activity activity,String authority) {
        this.mContext = mContext;
        this.mWebView = webView;
        this.mActivity = activity;
        FileContentProvider.getInstance().setUpDatabase(authority);
    }

    /**
     *  set data for authorization
     * @param responseData jsonObject for retrieve auth data
     */
    public void setAuthDetails(String responseData){
        try {
            JSONObject object = new JSONObject(responseData);
            this.serverAuthToken = FileUploadUtility.getStringObjectValue(object, AUTH_TOKEN);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Entry point for uploadPendingFiles module
     * @param fileStatusRes jsonObject with file details for retry upload file
     */
    public void uploadPendingFiles(final String fileStatusRes){
        try {
            if (TextUtils.isEmpty(fileStatusRes)) return;

            JSONObject jOFileStatus = new JSONObject(fileStatusRes);
            syncOfflineNextButtonCallBack = FileUploadUtility.getStringObjectValue(jOFileStatus, NEXT_BUTTON_CALLBACK);
            String offlineDataID = FileUploadUtility.getStringObjectValue(jOFileStatus, OFFLINE_DATA_ID);
            String filename = FileUploadUtility.getStringObjectValue(jOFileStatus, FILE_NAME);
            String appID = FileUploadUtility.getStringObjectValue(jOFileStatus, APP_ID);
            String filePath = FileUploadUtility.getStringObjectValue(jOFileStatus, FILE_PATH);
            objParams = FileUploadUtility.getStringObjectValue(jOFileStatus, OBJ_PARAMS);

            retryFileUploadClient(appID, filename, offlineDataID, filePath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * retry file upload if file is failed during upload
     * @param appID ID of app
     * @param fileName name of file
     * @param offlineID upload file whose offline ID matches with file name
     * @param filePath file path
     */
    private void retryFileUploadClient(String appID, String fileName, String offlineID, String filePath) {
        File file;
        if (filePath != null && !filePath.isEmpty())
            file = new File(filePath);
        else
            file = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator + fileName);

        AppMediaDetails appMediaDetails = AppMediaDetailsDAO.getAppMediaDetailsByFileName(mContext, offlineID, fileName);

        //save as in progress
        appMediaDetails.setUploadStatus(AppMediaDetails.UPLOAD_IN_PROGRESS);

        appMediaDetails.save(mContext);

        instructionNumberClockIn = appMediaDetails.getInstructionNumber();

        if (appMediaDetails == null) {
            return;
        }

        FileUploader roofingUploader = FileUploader.getInstance(appMediaDetails,mContext);
        roofingUploader.setFilePath(file.getPath());
        roofingUploader.setAppID(appID);
        roofingUploader.setAppsServerToken(serverAuthToken);
        roofingUploader.setUiCallBack(new FileUploader.IUICallBackRoofing() {
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
     * @param offlineDataID search file details against offlineID
     */
    private void updateFileStatus(String offlineDataID) {
        if (!TextUtils.isEmpty(syncOfflineNextButtonCallBack)) {
            JSONObject jsonObjectFileStatus = getAllFileStatusList(offlineDataID);
            if (objParams != null && !objParams.isEmpty()) {
                try {
                    jsonObjectFileStatus.put(OBJ_PARAMS, objParams);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, syncOfflineNextButtonCallBack, jsonObjectFileStatus);
        }
    }

    /**
     * Check the status of all the files uploads and send all the file details to callback function
     * @param offlineDataID search file details against offlineID
     * @return return all file details for offlineID
     */
    private JSONObject getAllFileStatusList(String offlineDataID) {

        try {
            JSONObject jsonObjectResponse = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            ArrayList<AppMediaDetails> appMediaDetailsArrayList = AppMediaDetailsDAO.getAppMediaDetailsListByOfflineID(mContext, offlineDataID);
            String mapFileMediaID = "";
            String mapFileName = "";
            int mapFileStatus = 0;
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
                    jsonObject.put(STEP, appMediaDetails.getInstructionNumber());
                    jsonObject.put(FILE_NM, appMediaDetails.getFileName());
                    jsonObject.put(MEDIA_ID, appMediaDetails.getMediaID());
                    jsonObject.put(S3_FILE_PATH, appMediaDetails.getS3FilePath());
                    jsonObject.put(STATUS, appMediaDetails.getUploadStatus());
                    jsonObject.putOpt(CAPTION, appMediaDetails.getImageCaption());
                    jsonArray.put(jsonObject);
                }

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

            jsonObjectResponse.put(MAP_PLAN_STATUS, mapPlanStatus);
            jsonObjectResponse.put(MAP_PLAN_STATUS, mapFileStatus);


            return jsonObjectResponse;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject();
    }
}
