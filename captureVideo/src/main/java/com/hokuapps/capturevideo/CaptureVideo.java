package com.hokuapps.capturevideo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.webkit.WebView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.capturevideo.activity.CameraActivity;
import com.hokuapps.capturevideo.utils.AppConstant;
import com.hokuapps.capturevideo.utils.RecordFileUtil;
import com.hokuapps.loadnativefileupload.backgroundtask.ImageUpload;
import com.hokuapps.loadnativefileupload.database.FileContentProvider;
import com.hokuapps.loadnativefileupload.models.JSResponseData;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class CaptureVideo {
    @SuppressLint("StaticFieldLeak")
    private static CaptureVideo instance;

    private WebView mWebView;

    private Activity mActivity;

    private JSResponseData jsResponseData;
    private String serverAuthToken;

    public static CaptureVideo getInstance(){
        if(instance == null){
            instance = new CaptureVideo();
        }
        return instance;
    }

    public void initialize(WebView mWebView, Activity activity, String uploadUrl, String mAuthority){
        this.mActivity = activity;
        this.mWebView = mWebView;
        AppConstant.FILE_UPLOAD_URL = "https://appfiles.hokuapps.com";
        FileContentProvider.getInstance().setUpDatabase(mAuthority);
    }

    /**
     *  set data for authorization
     * @param responseData jsonObject for retrieve auth data
     */
    public void setAuthDetails(String responseData){
        try {
            JSONObject object = new JSONObject(responseData);
            this.serverAuthToken = FileUploadUtility.getStringObjectValue(object, "authToken");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method open camera activity to record video and preview in next activity
     * @param responseData json data in string format
     */
    public void openCamera(String responseData){

        JSResponseData jsResponseDataModel = FileUploadUtility.parseLoadNativeFileUploadJsResponseData(responseData);
        setJsResponseData(jsResponseDataModel);
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                launchIntent();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setPermissions(
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO

                    )
                    .check();
        }else {
            TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setPermissions( Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    .check();
        }
    }

    /**
     *  This method launch camera activity
     */
    private void launchIntent() {
        mActivity.startActivityForResult(new Intent(mActivity, CameraActivity.class), AppConstant.INTENT_VIDEO_CAPTURE_RESULT);
    }


    /**
     * This method delete video file after uploading file
     * @param respData contain list of file path
     */
    public void deleteVideoFile(String respData){
        try {
            JSONObject jsonObject = new JSONObject(respData);
            if(jsonObject.has(AppConstant.JSONParameter.FILE_PATHS) && jsonObject.getJSONArray(AppConstant.JSONParameter.FILE_PATHS).length() > 0){
                JSONArray jsonArray = jsonObject.getJSONArray(AppConstant.JSONParameter.FILE_PATHS);
                for (int i = 0; i < jsonArray.length() ; i++){
                    String filePath = jsonArray.getString(i);
                    RecordFileUtil.deleteFile(filePath);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * set json response data
     * @param jsResponseData provide jsonObject
     */
    public void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }

    /**
     * get the response data
     *
     * @return json response data
     */
    public JSResponseData getJsResponseData() {
        return jsResponseData != null ? jsResponseData : new JSResponseData();
    }

    /**
     * This method handle result of video capture
     * @param intent contain extra data
     */
    public void handleVideoActivityResult( Intent intent){
        try {

            if(intent == null || intent.getExtras() == null) return;
            if (jsResponseData == null) return;

            String newFilePath = intent.getStringExtra(AppConstant.INTENT_PATH);
            String caption = intent.getStringExtra(AppConstant.INTENT_FILE_CAPTION);

            if(newFilePath == null) return;

            File file = new File(newFilePath);
            if(file == null) return;

            String destFileName = file.getName();
            File desFilePath = new File(RecordFileUtil.getHtmlDirFromSandbox(mActivity) + File.separator + destFileName);

            ImageUpload imageUpload = ImageUpload.getInstance();
            imageUpload.initUpload(mActivity, mWebView, jsResponseData, serverAuthToken);
            imageUpload.setUploadUrl(AppConstant.FILE_UPLOAD_URL);

            imageUpload.setNativeCaptureVideoCallbackFunction(file, jsResponseData.getCallbackFunction(), caption);
            imageUpload.startImageUpload(desFilePath.getPath(), jsResponseData.getOfflineID(), jsResponseData.getAppID(), jsResponseData.getSrcImageName(), jsResponseData.getImageType());

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
