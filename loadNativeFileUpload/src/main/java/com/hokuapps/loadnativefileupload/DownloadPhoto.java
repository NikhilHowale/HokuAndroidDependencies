package com.hokuapps.loadnativefileupload;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.loadnativefileupload.backgroundtask.DownloadFile;
import com.hokuapps.loadnativefileupload.constants.KeyConstants;
import com.hokuapps.loadnativefileupload.imageEditor.IPRectangleAnnotationActivity;
import com.hokuapps.loadnativefileupload.models.DownloadParams;
import com.hokuapps.loadnativefileupload.models.JSResponseData;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class DownloadPhoto {

    @SuppressLint("StaticFieldLeak")
    private static DownloadPhoto instance;
    private Activity activity;

    private JSResponseData jsResponseData;

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
}
