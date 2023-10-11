package com.hokuapps.loadnativefileupload.utilities;

import static com.hokuapps.loadnativefileupload.constants.FileUploadConstant.FOLDER_NAME_PROFILE_PICTURE;
import static com.hokuapps.loadnativefileupload.constants.FileUploadConstant.MessageType;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.hokuapps.loadnativefileupload.constants.FileUploadConstant;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
@SuppressLint("StaticFieldLeak")
public class CameraManager {
    public static final int REQUEST_IMAGE_CAPTURE = 100;
    public static final int TYPE_PROFILE_IMAGE = 4;
    private static Uri outputUri;
    static Activity activity;
    static Context context;

    public CameraManager(Activity activity, Context context) {
        CameraManager.activity = activity;
        CameraManager.context = context;
    }

    public static void launchCameraFromActivity(int type, int requestCode) {
        // create Intent to take a picture and return control to the calling application
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // create a file to save the image
        outputUri = getOutputMediaFileUri(type);

        // set the image file name
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        takePictureIntent.putExtra("return-data", true);

        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // start the image capture Intent
            activity.startActivityForResult(takePictureIntent, requestCode);
        }
    }

    /**
     * Create a file Uri for saving an image
     */
    private static Uri getOutputMediaFileUri(int type) {
        if (type == TYPE_PROFILE_IMAGE) {
            return getProfilePictureOutputFileUri();
        }

        return FileProvider.getUriForFile(context,context.getPackageName() + ".provider", getOutputMediaFile(type));

    }

    public static Uri getProfilePictureOutputFileUri() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        /*String path = Utility.getRootFileDir() + File.separator + AppConstant.APP_TAG;*/
        File mediaStorageDir = new File(FileUtility.getRootDirPath(), FOLDER_NAME_PROFILE_PICTURE);

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!FileUtility.makeDirectory(mediaStorageDir)) {
            return null;
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        File imageFile = null;
        imageFile = new File(mediaStorageDir.getPath() + File.separator +
                "Profile_photo_" + timeStamp + ".jpg");

        return FileProvider.getUriForFile(context,context.getPackageName() + ".provider", imageFile);
        //return Uri.fromFile(imageFile);
    }

    /**
     * Create a File for saving an image
     */
    public static File getOutputMediaFile(int type) {
        /* To be safe, you should check that the SDCard is mounted
           using Environment.getExternalStorageState() before doing this.
           String path = Utility.getRootFileDir() + File.separator + AppConstant.APP_TAG;
           File mediaStorageDir = new File(path, AppConstant.FOLDER_NAME_MEDIA);
           */
        File mediaStorageDir = FileUtility.getMediaDirPath();

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!FileUtility.makeDirectory(mediaStorageDir)) {
            return null;
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        File mediaFile = null;
        if (type == MessageType.TYPE_IMAGE) {
            File imageFile = new File(mediaStorageDir, FileUploadConstant.FOLDER_NAME_IMAGE);

            // Create the storage directory if it does not exist
            if (!FileUtility.makeDirectory(imageFile)) {
                return null;
            }

            mediaFile = new File(imageFile.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MessageType.TYPE_VIDEO) {
            File videoFile = new File(mediaStorageDir, FileUploadConstant.FOLDER_NAME_VIDEO);

            // Create the storage directory if it does not exist
            if (!FileUtility.makeDirectory(videoFile)) {
                return null;
            }

            mediaFile = new File(videoFile.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else if (type == MessageType.TYPE_AUDIO) {
            File audioFile = new File(mediaStorageDir, FileUploadConstant.FOLDER_NAME_AUDIO);

            // Create the storage directory if it does not exist
            if (!FileUtility.makeDirectory(audioFile)) {
                return null;
            }

            mediaFile = new File(audioFile.getPath() + File.separator +
                    "PTT-" + timeStamp + ".aac");
        }

        return mediaFile;
    }

}
