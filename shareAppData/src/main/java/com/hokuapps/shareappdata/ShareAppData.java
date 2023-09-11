package com.hokuapps.shareappdata;

import static com.hokuapps.shareappdata.ShareAppConstants.FileType.CALENDER;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.DIAL_TO;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.DOC;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.DOCX;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.FILE_NAME;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.IMAGE;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.MAIL;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.MAIL_TO;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.MESSAGE;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.PDF;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.SETTINGS;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.SHARE;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.SHARE_APP_DATA;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.TOAST;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.WEB;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.XLS;
import static com.hokuapps.shareappdata.ShareAppConstants.FileType.XLSX;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class ShareAppData {

    public String applicationName = "";
    public Context context = null;
    public File htmlDirectory;
    private ProgressDialog downloadProgressDialog;

    private Activity mActivity;


    public ShareAppData() {
    }

    /**
     * Entry point of the shareAppData module
     *
     * @param mContext
     * @param shareJSON
     * @param v1
     * @param appName
     * @param htmlDir
     * @param progressDialog
     * @param activity
     */
    public void shareAppData(Context mContext, String shareJSON, View v1, String appName, File htmlDir, ProgressDialog progressDialog, final Activity activity) {
        try {

            if (mContext == null || shareJSON == null || v1 == null || progressDialog == null) {
                return;
            }

            this.mActivity = activity;
            JSONObject shareJSONObj = new JSONObject(shareJSON);
            String type = ShareUtility.getStringObjectValue(shareJSONObj, "type");
            Object dataShare = ShareUtility.getJsonObjectValue(shareJSONObj, "data");

            applicationName = appName;
            context = mContext;
            htmlDirectory = htmlDir;
            downloadProgressDialog = progressDialog;


            if (!TextUtils.isEmpty(type) && dataShare != null) {

                if (type != null) {
                    switch (type) {

                        case SHARE:
                            ShareUtility.shareTextMessage(mContext, dataShare.toString(), false);
                            break;
                        case MESSAGE:
                            ShareUtility.shareTextViaMessageApp(mContext, dataShare.toString());
                            break;
                        case MAIL:
                            ShareUtility.shareTextViaMail(mContext, dataShare.toString());
                            break;
                        case MAIL_TO:
                            ShareUtility.shareTextViaMail(mContext, dataShare.toString(), " ", " ");
                            break;
                        case DIAL_TO:
                            ShareUtility.openDialer(mContext, dataShare.toString());
                            break;
                        case TOAST:
                            ShareUtility.showMessage(mContext, dataShare.toString());
                            break;
                        case CALENDER:
                            ShareUtility.saveToCalendar(mContext, dataShare.toString());
                            break;
                        case SETTINGS:
                            Log.i(SHARE_APP_DATA, SETTINGS + ": " + SETTINGS);
                            break;
                        case WEB:
                            //show all install browser apps
                            ShareUtility.openBrowserIntent(mContext, dataShare.toString());
                            break;
                        case PDF:
                            downloadFileIfRequired(dataShare.toString(), ShareUtility.getStringObjectValue(shareJSONObj, "fileName"), type);
                            break;

                        case DOC:
                        case DOCX:
                        case XLS:
                        case XLSX:
                        case IMAGE:
                            downloadFileIfRequired(dataShare.toString(),
                                    ShareUtility.getStringObjectValue(shareJSONObj, FILE_NAME), type);
                            break;

                    }
                }
            } else {
                Toast.makeText(mContext, "Data is empty.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

            Log.i(SHARE_APP_DATA, "Exception : " + e.getMessage());

        }

    }


    /**
     * Download file according to the type(Image, Document,Video)
     *
     * @param mUrl
     * @param fileName
     * @param type
     */
    private void downloadFileIfRequired(final String mUrl, final String fileName, final String type) {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
               downloadFile(mUrl,fileName,type);
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            TedPermission.create()
                    .setPermissionListener(permissionlistener)
                    .setPermissions(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO
                    )
                    .check();
        } else {
            TedPermission.create()
                    .setPermissionListener(permissionlistener)
                    .setPermissions(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    .check();
        }
    }


    /**
     * Download file according to type
     * @param mUrl
     * @param fileName
     * @param type
     */

    private void downloadFile(String mUrl, String fileName, String type) {
        final DownloadFile downloadFile = new DownloadFile();
        downloadFile.setDownloadFolderPath(FileUtility.getDownloadFileParentDir(type, false));
        downloadFile.setOriginalFileName(fileName);
        downloadFile.setDownloadUrl(mUrl);
        downloadFile.setDownloadCallback(new DownloadFile.DownloadCallback() {
            @Override
            public void onDownloadStatus(boolean isDownloaded, String filePath) {
                //dismissProgressBar
                if (downloadProgressDialog != null)
                    downloadProgressDialog.dismiss();

                if (isDownloaded) {
                    ShareUtility.showPdfFileInApp(context, filePath, type);
                } else {
                    ShareUtility.showMessage(context, "Download Failed");
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
                downloadProgressDialog = new ProgressDialog(context);
                downloadProgressDialog.setMessage("Downloading file....");
                downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                downloadProgressDialog.setIndeterminate(false);
                downloadProgressDialog.setOnDismissListener(dialog -> {
                    try {
                        downloadFile.cancel(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                downloadProgressDialog.show();
            }
        });
        downloadFile.execute();
    }

}
