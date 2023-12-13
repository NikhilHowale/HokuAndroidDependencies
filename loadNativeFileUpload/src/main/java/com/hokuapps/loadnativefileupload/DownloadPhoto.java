package com.hokuapps.loadnativefileupload;

import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.AUTH_TOKEN;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.loadnativefileupload.backgroundtask.DownloadFile;
import com.hokuapps.loadnativefileupload.backgroundtask.ImageUpload;
import com.hokuapps.loadnativefileupload.constants.KeyConstants;
import com.hokuapps.loadnativefileupload.database.FileContentProvider;
import com.hokuapps.loadnativefileupload.imageEditor.IPRectangleAnnotationActivity;
import com.hokuapps.loadnativefileupload.models.DownloadParams;
import com.hokuapps.loadnativefileupload.models.JSResponseData;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;
import com.hokuapps.loadnativefileupload.utilities.FileUtility;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class DownloadPhoto {

    @SuppressLint("StaticFieldLeak")
    private static DownloadPhoto instance;
    private Activity activity;
    private WebView mWebView;

    private JSResponseData jsResponseData;
    private String serverAuthToken;

    private ProgressDialog downloadProgressDialog;

    public static DownloadPhoto getInstance(){
        if(instance == null){
            instance = new DownloadPhoto();
        }
        return instance;
    }

    public void initialize(Activity activity) {
        this.activity = activity;
    }

    public void initialize(WebView mWebView, Activity mActivity, String uploadUrl, String mAuthority){
        this.mWebView = mWebView;
        this.activity = mActivity;
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
     * This method retrieves data from json and checks file exit in the download directory if they do not download it using the url
     * later downloaded image display in activity for editing
     * @param response JSON data in string format
     */
    public void downloadPhoto(String response){
        try {
            JSResponseData jsResponseDataModel = FileUploadUtility.parseLoadNativeFileUploadJsResponseData(response);
            setJsResponseData(jsResponseDataModel);

            try {

                JSONObject jsonObject = new JSONObject(response);

                String mapPlanFilename = (String) FileUploadUtility.getJsonObjectValue(jsonObject, "fileName");
                String mapPlanS3FilePath = (String) FileUploadUtility.getJsonObjectValue(jsonObject, "s3FilePath");
                String mapPlanMediaID = (String) FileUploadUtility.getJsonObjectValue(jsonObject, "mediaID");
                String mapPlanOfflineDataID = (String) FileUploadUtility.getJsonObjectValue(jsonObject, "offlineID");

                String filepath = FileUploadUtility.getHtmlDirFromSandbox(activity) + File.separator + mapPlanFilename;
                File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(activity) + File.separator + "map_plan_" + System.currentTimeMillis() + ".png");

                if (!TextUtils.isEmpty(mapPlanFilename) && new File(filepath).exists()) {
                    IPRectangleAnnotationActivity.start(activity, filepath, outputFile.getAbsolutePath(), getJsResponseData().getColorCode(), getJsResponseData().getPageTitle(), KeyConstants.ACTION_REQUEST_EDIT_IMAGE_MAP_PLAN);
                } else if (!TextUtils.isEmpty(mapPlanS3FilePath)) {
                    downloadFileImageIfRequired(mapPlanS3FilePath,mapPlanFilename);
                } else {
                    Toast.makeText(activity, "url is empty", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    /**
     * This method check file exit in storage with filename other wise download using url
     * @param mUrl file url
     * @param filename name of file
     */
    private void downloadFileImageIfRequired(final String mUrl , final  String filename) {

        activity.runOnUiThread(() -> {
            PermissionListener permissionListener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {

                    final DownloadFile downloadFile = new DownloadFile();
                    DownloadParams downloadParams = new DownloadParams();
                    downloadParams.setDownloadFolderPath(FileUploadUtility.getDownloadFileParentDir(activity));
                    downloadParams.setDownloadUrl(mUrl);
                    downloadFile.setOriginalFileName(filename);
                    downloadFile.setDownloadUrl(mUrl);
                    downloadFile.setDownloadParams(downloadParams);

                    downloadFile.setDownloadCallback(new DownloadFile.DownloadCallback() {
                        @Override
                        public void onDownloadStatus(boolean isDownloaded, String filePath, String originalFilepath) {

                            DownloadFile.updateDownloadedFileByName(isDownloaded, filePath, originalFilepath);

                            //dismissProgressBar
                            if (downloadProgressDialog != null)
                                downloadProgressDialog.dismiss();

                            File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(activity) + File.separator + System.currentTimeMillis() + ".png");

                            if (new File(originalFilepath).exists()) {
                                IPRectangleAnnotationActivity.start(activity, originalFilepath, outputFile.getAbsolutePath(), getJsResponseData().getColorCode(), getJsResponseData().getPageTitle(), KeyConstants.ACTION_REQUEST_EDIT_IMAGE_MAP_PLAN);
                            }

                        }

                        @Override
                        public void onDownloadProgressUpdate(String status) {
                            try {
                                downloadProgressDialog.setProgress(Integer.parseInt(status));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        @Override
                        public void onDownloadStarted(String filePath) {
                            //show progress bar
                            downloadProgressDialog = new ProgressDialog(activity);
                            downloadProgressDialog.setMessage(activity.getString(R.string.lbl_downloading_file));
                            downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                            downloadProgressDialog.setIndeterminate(false);
                            downloadProgressDialog.setMax(100);
                            downloadProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    try {
                                        if (downloadFile != null) {
                                            downloadFile.cancel(true);
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                            downloadProgressDialog.show();

                        }
                    });
                    downloadFile.execute();

                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {

                }
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                TedPermission.create()
                        .setPermissionListener(permissionListener)
                        .setPermissions(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO
                        )
                        .check();
            }
            else {
                TedPermission.create()
                        .setPermissionListener(permissionListener)
                        .setPermissions(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();
            }

        });

    }

    public JSResponseData getJsResponseData() {
        return jsResponseData != null ? jsResponseData : new JSResponseData();
    }

    private void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }


    /**
     * Handle onActivity result of edit map plan and upload it
     * @param intent contain image path of edited map plan
     */
    public void handleEditImagePlan(Intent intent) {
        try {
            if (intent != null) {

                if (DownloadPhoto.getInstance().getJsResponseData() == null) return;

                String newFilePath = intent.getStringExtra(IPRectangleAnnotationActivity.SAVE_FILE_PATH);
                String destFileName = FileUtility.getFileName(newFilePath);

                JSResponseData responseData = DownloadPhoto.getInstance().getJsResponseData();

                ImageUpload.getInstance().initUpload(activity,mWebView, getJsResponseData(),serverAuthToken);

                ImageUpload.getInstance().setNativeSelectedPhotoCallbackFunction(newFilePath,responseData.getOfflineID(), responseData.getCallbackFunction());
                ImageUpload.getInstance().startImageUpload(destFileName, responseData.getOfflineID(),
                        responseData.getAppID(),
                        responseData.getSrcImageName(), responseData.getImageType());

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
