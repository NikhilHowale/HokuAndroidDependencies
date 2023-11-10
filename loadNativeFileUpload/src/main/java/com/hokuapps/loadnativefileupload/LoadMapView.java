package com.hokuapps.loadnativefileupload;

import android.app.Activity;

import com.google.android.gms.maps.model.LatLng;
import com.hokuapps.loadnativefileupload.activity.LocationServiceActivity;
import com.hokuapps.loadnativefileupload.models.JSResponseData;
import com.hokuapps.loadnativefileupload.models.LocationMapModel;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;

import org.json.JSONObject;

public class LoadMapView {

    private static LoadMapView instance;

    public JSResponseData jsResponseData;

    public static LoadMapView getInstance(){
        if (instance == null){
            instance = new LoadMapView();
        }
        return instance;
    }

    /**
     * This method open map to take snap shot for edit
     * @param activity activity
     * @param mapResponse json string in string format
     */
    public void showMapView(Activity activity, String mapResponse){
        try {
            JSONObject responseJsonObj = new JSONObject(mapResponse);

            LocationMapModel locationMapModel = FileUploadUtility.getLocationMapModel(responseJsonObj);
            if (locationMapModel != null) {
                locationMapModel.setResponseData(mapResponse);
                if (locationMapModel.isOpenMapApp()) {
                    LatLng location =  FileUploadUtility.getLocationFromAddress(activity, locationMapModel.getPageTitle());
                    if(location != null){
                        FileUploadUtility.openInExternalMapByLatLong(activity, location.latitude, location.longitude, locationMapModel.getPageTitle());
                    }

                } else {
                    LocationServiceActivity.startActivityForResult(activity, locationMapModel);
                }
                JSResponseData jsResponseData = new JSResponseData();
                jsResponseData.setResponseData(mapResponse);
                jsResponseData.setLocationMapModel(locationMapModel);
                setJsResponseData(jsResponseData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }


/*    public void handleMapActivityResult(Activity activity, WebView mWebView, Intent intent){

        try {
            if(intent == null || intent.getExtras() == null) return;
            LocationMapModel locationMapModel = intent.getExtras().getParcelable("mapLocationModel");

            int resultCode = intent.getIntExtra(IS_RESULT_CANCEL,1);

            if(locationMapModel == null) return;

            if (resultCode == Activity.RESULT_OK) {

                String filepath = FileUploadUtility.getHtmlDirFromSandbox(activity) + File.separator + locationMapModel.getMapFileName();
                File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(activity) + File.separator + "map_" + System.currentTimeMillis() + ".png");

                if (outputFile.exists()) {
                    outputFile.delete();
                }

                String colorCode = locationMapModel.getColorCode();

                if (locationMapModel.getIsWithoutEditor()) {
                    String destFileName = FileUtility.getFileName(filepath);

                    locationMapModel.setMapFileName(destFileName);

                    instructionNumberClockIn = locationMapModel.getInstucationNumberClockIn();
                    offlineID = locationMapModel.getOfflineDataID();

                    setNativeSelectedPhotoCallbackFunction(destFileName, offlineID, locationMapModel);
                    startImageUpload(destFileName, offlineID, locationMapModel.getAppID(),
                            locationMapModel.getMapSrcName(), locationMapModel.getImageType());
                } else {
                    IPRectangleAnnotationActivity.start(this, filepath, outputFile.getAbsolutePath(), colorCode, locationMapModel.getPageTitle(), locationMapModel, ACTION_REQUEST_EDITIMAGE_MAP);
                }

            }  else if (!TextUtils.isEmpty(locationMapModel.getCancelButtonCallback())) {
                FileUploadUtility.callJavaScriptFunction(activity, mWebView, locationMapModel.getCancelButtonCallback(), new JSONObject(locationMapModel.getResponseData()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/

}
