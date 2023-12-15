package com.hokuapps.searchlocationonmap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.webkit.WebView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.searchlocationonmap.activity.SearchDropMapActivity;
import com.hokuapps.searchlocationonmap.model.LocationMapModel;
import com.hokuapps.searchlocationonmap.utils.AppConstant;
import com.hokuapps.searchlocationonmap.utils.Utility;

import org.json.JSONObject;

import java.util.List;

public class SearchOnMap {
    private static SearchOnMap instance;

    private Activity mActivity;
    private WebView mWebview;
    public static SearchOnMap getInstance(){
        if(instance == null){
            instance = new SearchOnMap();
        }
        return instance;
    }

    /**
     * This method initialize required parameter to class
     * @param mWebView mWebView
     * @param activity Activity reference
     */
    public void initialize(WebView mWebView, Activity activity){
        this.mWebview = mWebView;
        this.mActivity = activity;
    }

    /**
     * This method search and drop map activity to retrieve address
     * @param response JSONObject is in string format
     */
    public void openMapForSearch(String response){
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                try {
                    JSONObject responseJsonObj = new JSONObject(response);
                    final LocationMapModel locationMapModel = getLocationMapObject(responseJsonObj);
                    SearchDropMapActivity.startActivityForResult(mActivity, locationMapModel);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };
        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                .check();
    }


    /**
     * This method parse json data to object
     * @param responseJsonObj json Object
     * @return locationMapModel object
     */
    private LocationMapModel getLocationMapObject(JSONObject responseJsonObj) {
        LocationMapModel locationMapModel = new LocationMapModel();
        try {

            locationMapModel.setNextButtonCallback(Utility.getStringObjectValue(responseJsonObj, "nextButtonCallback"));
            locationMapModel.setColorCode(responseJsonObj.has("colorCode") ? responseJsonObj.getString("colorCode") : "#448aff");


            //for select location
            locationMapModel.setIsSelectLocation(Utility.getJsonObjectIntValue(responseJsonObj, "isSelectLocation"));
            locationMapModel.setIsSearchAndDrop(Utility.getJsonObjectIntValue(responseJsonObj, "isSearchAndDrop"));
            locationMapModel.setIsAllCountry(Utility.getJsonObjectIntValue(responseJsonObj, "isAllCountry"));


            locationMapModel.setResponseData(responseJsonObj.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return locationMapModel;
    }

    /**
     * This method handle search and drop activity result and return address with lat/ long to callback
     * @param intent contain extra data
     */
    public  void handleSearchDropResult( Intent intent){
        try {
            if (intent != null && intent.getExtras() != null) {
                LocationMapModel locationMapModel = intent.getExtras().getParcelable(AppConstant.IntentParam.MAP_LOCATION_MODEL);

                if(locationMapModel == null) return;

                int resultCode =  intent.getExtras().getInt(AppConstant.IntentParam.IS_RESULT_CANCEL,-1);

                if (resultCode == Activity.RESULT_OK) {

                    JSONObject jsonObjResponse = new JSONObject();

                    String mapResultCallback = intent.getExtras().containsKey(AppConstant.IntentParam.EXTRA_MAP_RESULT_CALLBACK)
                            ? intent.getExtras().getString(AppConstant.IntentParam.EXTRA_MAP_RESULT_CALLBACK)
                            : "";

                    if (!TextUtils.isEmpty(mapResultCallback)) {
                        jsonObjResponse = new JSONObject(mapResultCallback);
                    }

                    Utility.callJavaScriptFunction(mActivity, mWebview, locationMapModel.getNextButtonCallback(), jsonObjResponse);

                } else {

                    if(locationMapModel.getResponseData() == null || locationMapModel.getResponseData().isEmpty()) return;

                    JSONObject jsonObj = new JSONObject(locationMapModel.getResponseData());

                    String backQueryCallBackName = Utility.getStringObjectValue(jsonObj, AppConstant.JSONParameter.BACK_CALLBACK_FUNCTION);

                    if (!TextUtils.isEmpty(backQueryCallBackName)) {
                        Utility.callJavaScriptFunction(mActivity, mWebview, backQueryCallBackName, jsonObj);
                    } else if (!TextUtils.isEmpty(Utility.getStringObjectValue(jsonObj, AppConstant.JSONParameter.NEXT_CALLBACK_FUNCTION))) {
                        Utility.callJavaScriptFunction(mActivity, mWebview, backQueryCallBackName, jsonObj);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
