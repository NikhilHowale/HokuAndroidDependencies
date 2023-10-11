package com.hokuapps.loadnativefileupload.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import com.sandrios.sandriosCamera.internal.SandriosCamera;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.xinlan.imageeditlibrary.picchooser.SelectPictureActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 20/3/17.
 */
public class CustomCameraManager {
    public static final int CAPTURE_MEDIA_PHOTO = 368;
    public static final int CAPTURE_MEDIA_VIDEO = 369;
    public static final int REQUEST_FILE_BROWSER = 9012;
    public static final int SCAN_IMAGE_REQUEST_CAMERA = 9032;
    public static final int SCAN_IMAGE_REQUEST_GALLERY = 9031;
    public static final int SELECT_GALLERY_IMAGE_CODE = 7000;
    public static final int ACTION_REQUEST_EDITIMAGE = 9006;

    private CustomCameraManager() {
    }

    /**
     * launch custom camera activity
     *
     * @param activity        - activity context
     * @param isShowPicker    - true, show image picker horizontal false otherwise
     * @param isCustomPreview - true, to show custom preview control, false, to show inbuilt preview control
     */
    public static void launchCameraFromActivity(Activity activity, boolean isShowPicker,
                                                String instructionText, boolean isCustomPreview,
                                                boolean isShowCaption, boolean isCroppingEnabled,
                                                boolean isHideRetakeBtn, boolean isRectangle, String responseData) {
        new SandriosCamera(activity, CAPTURE_MEDIA_PHOTO)
                .setShowPicker(isShowPicker)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .enableImageCropping(isCroppingEnabled)
                .shouldShowCustomPreview(isCustomPreview)
                .shouldShowCaptionView(isShowCaption)
                .setIntstructionText(instructionText)
                .setHideRetakeBtn(isHideRetakeBtn)
                .setIsRectangle(isRectangle) // Crop image in rectangle or Oval.
                .setResponseData(responseData) // Crop image in rectangle or Oval.
                .launchCamera();

    }



    public static void launchVideoFromActivity(Activity activity) {
        new SandriosCamera(activity, CAPTURE_MEDIA_VIDEO)
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_VIDEO)
                .enableImageCropping(false)
                .launchCamera();
    }

    public static String getFilePathFromIntent(Intent intent) {
        if (intent == null) {
            return null;
        }

        return intent.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
    }


    /**
     * show custom image gallery
     *
     * @param activity     - from fragment
     * @param title        - default title for gallery
     * @param toolbarColor - hex color only
     */
    public static void launchCustomImageGallery(final Activity activity, final String title,
                                                final String toolbarColor, final int requestCode) {

        if (activity == null) return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                PermissionListener permissionlistener = new PermissionListener() {

                    @Override
                    public void onPermissionGranted() {
                        SelectPictureActivity.startActivityForResultImage(activity, SelectPictureActivity.IMAGE_GALLERY, title, toolbarColor, requestCode);

                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(activity, "Permission required to select image.", Toast.LENGTH_SHORT).show();

                    }

                };

                TedPermission.create()
                        .setPermissionListener(permissionlistener)
                        .setPermissions(   Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();
            }

        });

    }

    /**
     * show custom video gallery
     *
     * @param activity     - from activity
     * @param title        - default title for gallery
     * @param toolbarColor - hex color only
     */
    public static void launchCustomVideoGallery(Activity activity, String title, String toolbarColor, int requestCode) {
        if (activity == null) return;
        SelectPictureActivity.startActivityForResultVideo(activity, SelectPictureActivity.VIDEO_GALLERY, title, toolbarColor, requestCode);
    }
}
