package com.hokuapps.loadnativefileupload;

import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.AUTH_TOKEN;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_RESULT_CANCEL;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.webkit.WebView;

import com.google.android.gms.maps.model.LatLng;
import com.hokuapps.loadnativefileupload.activity.LocationServiceActivity;
import com.hokuapps.loadnativefileupload.backgroundtask.ImageUpload;
import com.hokuapps.loadnativefileupload.constants.KeyConstants;
import com.hokuapps.loadnativefileupload.database.FileContentProvider;
import com.hokuapps.loadnativefileupload.imageEditor.IPRectangleAnnotationActivity;
import com.hokuapps.loadnativefileupload.models.JSResponseData;
import com.hokuapps.loadnativefileupload.models.LocationMapModel;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;
import com.hokuapps.loadnativefileupload.utilities.FileUtility;

import org.json.JSONObject;

import java.io.File;

public class LoadMapView {

    private static LoadMapView instance;

    public JSResponseData jsResponseData;
    private String serverAuthToken;

    private WebView mWebView;
    private Activity mActivity;

    public static LoadMapView getInstance(){
        if (instance == null){
            instance = new LoadMapView();
        }
        return instance;
    }

    public void initMap(WebView mWebView, Activity mActivity, String uploadUrl, String mAuthority){
        this.mWebView = mWebView;
        this.mActivity = mActivity;
        KeyConstants.APP_FILE_URL = uploadUrl;
        FileContentProvider.getInstance().setUpDatabase(mAuthority);
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
     * This method open map to take snap shot for edit
     * @param mapResponse json string in string format
     */
    public void showMapView(String mapResponse){
        try {
            JSONObject responseJsonObj = new JSONObject(mapResponse);

            LocationMapModel locationMapModel = FileUploadUtility.getLocationMapModel(responseJsonObj);
            if (locationMapModel != null) {
                locationMapModel.setResponseData(mapResponse);
                if (locationMapModel.isOpenMapApp()) {
                    LatLng location =  FileUploadUtility.getLocationFromAddress(mActivity, locationMapModel.getPageTitle());
                    if(location != null){
                        FileUploadUtility.openInExternalMapByLatLong(mActivity, location.latitude, location.longitude, locationMapModel.getPageTitle());
                    }

                } else {
                    LocationServiceActivity.startActivityForResult(mActivity, locationMapModel);
                }
                JSResponseData jsResponseData = new JSResponseData();
                jsResponseData.setResponseData(mapResponse);
                jsResponseData.setLocationMapModel(locationMapModel);
                jsResponseData.setInstructionNumberClockIn(locationMapModel.getInstucationNumberClockIn());
                jsResponseData.setOfflineID(locationMapModel.getOfflineDataID());
                setJsResponseData(jsResponseData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }

    public JSResponseData getJsResponseData() {
        return jsResponseData != null ? jsResponseData : new JSResponseData();
    }

    public void handleLocationServiceResult(Intent intent) {

        try {
            if(intent == null || intent.getExtras() == null) return;

            LocationMapModel locationMapModel = intent.getExtras().getParcelable("mapLocationModel");

            int resultCode = intent.getIntExtra(IS_RESULT_CANCEL,1);

            if(locationMapModel == null) return;

            if (resultCode == Activity.RESULT_OK) {

                String filepath = FileUploadUtility.getHtmlDirFromSandbox(mActivity) + File.separator + locationMapModel.getMapFileName();
                File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mActivity) + File.separator + "map_" + System.currentTimeMillis() + ".png");

                if (outputFile.exists()) {
                    outputFile.delete();
                }

                String colorCode = locationMapModel.getColorCode();

                if (locationMapModel.getIsWithoutEditor()) {
                    String destFileName = FileUtility.getFileName(filepath);

                    locationMapModel.setMapFileName(destFileName);


                    ImageUpload.getInstance().initUpload(mActivity,mWebView, getJsResponseData(),serverAuthToken);

                    ImageUpload.getInstance().setNativeSelectedPhotoCallbackFunction(destFileName,locationMapModel);
                    ImageUpload.getInstance().startImageUpload(destFileName,locationMapModel.getOfflineDataID(), locationMapModel.getAppID()
                            , locationMapModel.getMapSrcName(),locationMapModel.getImageType());


                } else {
                    IPRectangleAnnotationActivity.start(mActivity, filepath, outputFile.getAbsolutePath(), colorCode, locationMapModel.getPageTitle(), locationMapModel, KeyConstants.ACTION_REQUEST_EDIT_IMAGE_MAP);
                }

            }  else if (!TextUtils.isEmpty(locationMapModel.getCancelButtonCallback())) {
                FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, locationMapModel.getCancelButtonCallback(), new JSONObject(locationMapModel.getResponseData()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void handleEditMapImageResult(Intent intent) {
        try {
            if(intent == null || intent.getExtras() == null) return;

            LocationMapModel locationMapModel = intent.getExtras().getParcelable("mapLocationModel");

            int resultCode = intent.getIntExtra(IS_RESULT_CANCEL,0);

            if(locationMapModel == null) return;

            if (resultCode == Activity.RESULT_OK) {
                String newFilePath = intent.getStringExtra(IPRectangleAnnotationActivity.SAVE_FILE_PATH);
                String destFileName = FileUtility.getFileName(newFilePath);

                locationMapModel.setMapFileName(destFileName);

                ImageUpload.getInstance().initUpload(mActivity,mWebView,getJsResponseData(),serverAuthToken);

                ImageUpload.getInstance().setNativeSelectedPhotoCallbackFunction(destFileName,locationMapModel);
                ImageUpload.getInstance().startImageUpload(destFileName,locationMapModel.getOfflineDataID(), locationMapModel.getAppID()
                        , locationMapModel.getMapSrcName(),locationMapModel.getImageType());

            } else {
                try {
                    if (!TextUtils.isEmpty(locationMapModel.getCancelButtonCallback())) {
                        FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, locationMapModel.getCancelButtonCallback(), new JSONObject(locationMapModel.getResponseData()));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
