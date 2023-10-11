package com.hokuapps.loadnativefileupload;

import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.APP_MEDIA_ARRAY;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.CAPTION;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.FILE_NM;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_GET_ALL_FILE_STATUS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_FILE_NAME;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_FILE_STATUS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_FILE_NM;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_MEDIA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_S3_FILE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAP_PLAN_STATUS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MEDIA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.NEXT_BUTTON_CALLBACK;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.OFFLINE_DATA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.S3_FILE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.STATUS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.STEP;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

import com.hokuapps.loadnativefileupload.dao.AppMediaDetailsDAO;
import com.hokuapps.loadnativefileupload.database.FileContentProvider;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetAllFileStatus {

    private final Context mContext;
    private final WebView mWebView;
    private final Activity mActivity;


    public GetAllFileStatus(Activity activity, Context context, WebView webView,String authority) {
        this.mActivity = activity;
        this.mContext = context;
        this.mWebView = webView;
        FileContentProvider.getInstance().setUpDatabase(authority);
    }

    /**
     * Checks the status of the file()
     * @param fileStatusRes jsonObject with offlineID to check file status
     */
    public void getAllFileStatus(final String fileStatusRes) {
        try {

            if (TextUtils.isEmpty(fileStatusRes)) return;

            JSONObject jOFileStatus = new JSONObject(fileStatusRes);
            String offlineDataID = FileUploadUtility.getStringObjectValue(jOFileStatus, OFFLINE_DATA_ID);

            String fileStatusCallBackFunction = FileUploadUtility.getStringObjectValue(jOFileStatus, NEXT_BUTTON_CALLBACK);
            boolean isGetAllFileStatusCalled = FileUploadUtility.getJsonObjectBooleanValue(jOFileStatus, IS_GET_ALL_FILE_STATUS);

            JSONObject jsonObjectFileStatus = getAllFileStatusList(offlineDataID);

            FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, fileStatusCallBackFunction, jsonObjectFileStatus);

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    /**
     * Get the status of all the files
     * @param offlineDataID search file details against offlineID
     * @return return all file details for offlineID
     */
    private JSONObject getAllFileStatusList(String offlineDataID) {

        try {
            JSONObject jsonObjectResponse = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            ArrayList<AppMediaDetails> appMediaDetailsArrayList = AppMediaDetailsDAO.getAppMediaDetailsListByOfflineID(
                    mContext, offlineDataID);
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
                jsonObjectResponse.put(MAP_PLAN_MEDIA_ID, mapFileMediaID);
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
            jsonObjectResponse.put(MAP_FILE_STATUS, mapFileStatus);


            return jsonObjectResponse;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject();
    }



}
