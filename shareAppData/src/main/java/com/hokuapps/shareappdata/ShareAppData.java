package com.hokuapps.shareappdata;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShareAppData {


    // pass view  View v1 = findViewById(R.id.webkitWebView1);
    // pass AppName
    // pass htmlDir
    // pass  ProgressDialog

   public String applicationName = "";
   public Context context = null;
   public File htmlDirectory;
   private  ProgressDialog downloadProgressDialog;

   private  Activity mActivity;


   public ShareAppData(){
   }

    public  void shareAppData (Context mContext, String shareJSON , View v1, String appName , File htmlDir, ProgressDialog progressDialog, final Activity activity) {
        try {

            if(mContext==null || shareJSON == null|| v1==null|| progressDialog==null){
                return;
            }

            this.mActivity = activity;
            JSONObject shareJSONObj = new JSONObject(shareJSON);
            String type = ShareUtility.getStringObjectValue(shareJSONObj, "type");
            String packageName = ShareUtility.getStringObjectValue(shareJSONObj, "packageName");
            Object dataShare = ShareUtility.getJsonObjectValue(shareJSONObj, "data");

            applicationName = appName;
            context = mContext;
            htmlDirectory = htmlDir;
            downloadProgressDialog = progressDialog;

//          Launch app if package name exits in device.
            if (!TextUtils.isEmpty(packageName)) {
                if (ShareUtility.isPackageExists(mContext, packageName)) {
                    ShareUtility.launchApplication(mContext, packageName);
                    return;
                }
            }

            if (!TextUtils.isEmpty(type) && dataShare != null) {
//                            Take screenShot and share.
                if (type != null) {
                    if (type.equalsIgnoreCase("screenshot")) {
                        takeScreenshot(mContext,v1);
                    } else if (type.equalsIgnoreCase("share")) {
                        ShareUtility.shareTextMessage(mContext, dataShare.toString(), false);
                    } else if (type.equalsIgnoreCase("message")) {
                        ShareUtility.shareTextViaMessageApp(mContext, dataShare.toString());
                    } else if (type.equalsIgnoreCase("mail")) {
                        ShareUtility.shareTextViaMail(mContext, dataShare.toString());
                    } else if (type.equalsIgnoreCase("mailTo")) {
                        ShareUtility.shareTextViaMail(mContext, dataShare.toString()," "," ");
                    } else if (type.equalsIgnoreCase("DialTo")) {
                        ShareUtility.openDialer(mContext, dataShare.toString());
                    } else if (type.equalsIgnoreCase("toast")) {
                        ShareUtility.showMessage(mContext, dataShare.toString());
                    } else if (type.equalsIgnoreCase("calendar")) {
                        ShareUtility.saveToCalendar(mContext, dataShare.toString());
                    } else if (type.equalsIgnoreCase("settings")) {
                        Log.i ("ShareAppData", "settings : "+ "settings");
                    } else if (type.equalsIgnoreCase("web")) {
                        //show all install browser apps
                        ShareUtility.openBrowserIntent(mContext, dataShare.toString());
                    } else if (type.equalsIgnoreCase("pdf")) {
                        downloadFileIfRequired(dataShare.toString(), ShareUtility.getStringObjectValue(shareJSONObj, "fileName"), type);
                    } else if (type.equalsIgnoreCase("doc")
                            || type.equalsIgnoreCase("docx")) {
                        downloadFileIfRequired(dataShare.toString(),
                                ShareUtility.getStringObjectValue(shareJSONObj, "fileName"), type);

                    } else if (type.equalsIgnoreCase("xls")) {
                        downloadFileIfRequired(dataShare.toString(),
                                ShareUtility.getStringObjectValue(shareJSONObj, "fileName"), type);

                    } else if (type.equalsIgnoreCase("xlsx")) {
                        downloadFileIfRequired(dataShare.toString(),
                                ShareUtility.getStringObjectValue(shareJSONObj, "fileName"), type);

                    } else if (type.equalsIgnoreCase("image")) {
                        downloadFileIfRequired(dataShare.toString(),
                                ShareUtility.getStringObjectValue(shareJSONObj, "fileName"), type);
                    }
                }
            } else {
                Toast.makeText(mContext, "Data is empty.", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){

            Log.i ("ShareAppData", "Exception : "+ e.getMessage());

        }

    }



    private void takeScreenshot( Context context, View v1) {


//                new TedPermission(context)
//                        .setPermissionListener(new PermissionListener() {
//                            @Override
//                            public void onPermissionGranted() {
//                                Date now = new Date();
//                                android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
//
//                                try {
//                                    // image naming and path  to include sd card  appending name you choose for file
//                                   // String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
//                                    v1.setDrawingCacheEnabled(true);
//
//                                    Bitmap bitmap = v1.getDrawingCache();
//                                    File imageFile = saveBitmapToFile(bitmap);
//                                    v1.setDrawingCacheEnabled(false);
//                                    shareImageOnOtherApp(imageFile,context);
//
//                                } catch (Throwable e) {
//                                    // Several error may come out with file handling or DOM
//                                    e.printStackTrace();
//                                }
//
//                            }
//
//                            @Override
//                            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//
//                            }
//                        })
//                        .setPermissions(
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                Manifest.permission.READ_EXTERNAL_STORAGE
//                        )
//                        .check();
            }




    private static void shareImageOnOtherApp(File imageFile, Context context) {

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharingIntent.setType("image/*");
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", imageFile);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(sharingIntent, "Share Image Using"));
    }



    private  File saveBitmapToFile(Bitmap bitmap) throws IOException {
        File imageFile = getOutputMediaFile(MessageType.TYPE_IMAGE, ".jpeg");

        if (imageFile != null) {

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        }
        return imageFile;
    }



    public  File getOutputMediaFile(int type, String extension) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        /*String path = Utility.getRootFileDir() + File.separator + AppConstant.APP_TAG;
        File mediaStorageDir = new File(path, AppConstant.FOLDER_NAME_MEDIA);*/
        File mediaStorageDir = ShareUtility.getMediaDirPath();

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!ShareUtility.makeDirectory(mediaStorageDir)) {
            return null;
        }

        String suffix = TextUtils.isEmpty(extension) ? "unknown" : extension;

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        File mediaFile = null;
        if (type == MessageType.TYPE_IMAGE) {
            File imageFile = new File(mediaStorageDir,applicationName + " Images");

            // Create the storage directory if it does not exist
            if (!ShareUtility.makeDirectory(imageFile)) {
                return null;
            }

            mediaFile = new File(imageFile.getPath() + File.separator +
                    "IMG_" + timeStamp + suffix);
        } else if (type == MessageType.TYPE_VIDEO) {
            File videoFile = new File(mediaStorageDir, applicationName + " Videos");

            // Create the storage directory if it does not exist
            if (!ShareUtility.makeDirectory(videoFile)) {
                return null;
            }

            mediaFile = new File(videoFile.getPath() + File.separator +
                    "VID_" + timeStamp + suffix);
        } else if (type == MessageType.TYPE_AUDIO) {
            File audioFile = new File(mediaStorageDir,applicationName + " Audio");

            // Create the storage directory if it does not exist
            if (!ShareUtility.makeDirectory(audioFile)) {
                return null;
            }
            mediaFile = new File(audioFile.getPath() + File.separator +
                    "AUD_" + timeStamp + suffix);
        } else if (type == MessageType.TYPE_FILE) {
            File file = new File(mediaStorageDir, applicationName + " Files");

            // Create the storage directory if it does not exist
            if (!ShareUtility.makeDirectory(file)) {
                return null;
            }
            mediaFile = new File(file.getPath() + File.separator +
                    "FILE_" + timeStamp + suffix);
        }

        return mediaFile;
    }



    private  void downloadFileIfRequired(final String mUrl, final String fileName, final String type) {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                final DownloadFile downloadFile = new DownloadFile();
                               downloadFile.setDownloadFolderPath(FileUtility.getDownloadFileParentDir("pdf", false));
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
                                       // progressDialog.setProgressPercentFormat(null);
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

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }


        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();


    }

}
