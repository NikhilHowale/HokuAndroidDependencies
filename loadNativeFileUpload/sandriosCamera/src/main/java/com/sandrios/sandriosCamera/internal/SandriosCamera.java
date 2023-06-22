package com.sandrios.sandriosCamera.internal;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.view.SurfaceHolder;

import androidx.annotation.IntRange;

import androidx.fragment.app.Fragment;


import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.sandrios.sandriosCamera.R;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.manager.CameraManager;
import com.sandrios.sandriosCamera.internal.manager.impl.Camera1Manager;
import com.sandrios.sandriosCamera.internal.ui.camera.Camera1Activity;
import com.sandrios.sandriosCamera.internal.ui.camera2.Camera2Activity;
import com.sandrios.sandriosCamera.internal.utils.CameraHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
/*import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;*/

public class SandriosCamera {

    private SandriosCamera mInstance = null;
    private Activity mActivity;
    private int requestCode;
    private int mediaAction = CameraConfiguration.MEDIA_ACTION_BOTH;
    private boolean showPicker = true;
    private boolean enableImageCrop;
    private boolean isCustomPreview;
    private boolean showCaption;
    private long videoSize = -1;
    private String instructionText = "";
    private boolean isHideRetakeBtn = false;
    private boolean isRectangle = false;
    private String responseData = "";
    private String galleryPageTitle = "";
    private String caption;


    /***
     * Creates SandriosCamera instance with default configuration set to both.
     *
     * @param activity - fromList which request was invoked
     * @param code     - request code which will return in onActivityForResult
     */
    public SandriosCamera(Activity activity, @IntRange(from = 0) int code) {
        mInstance = this;
        mActivity = activity;
        requestCode = code;
    }

    public SandriosCamera setShowPicker(boolean showPicker) {
        this.showPicker = showPicker;
        return mInstance;
    }

    public SandriosCamera setMediaAction(int mediaAction) {
        this.mediaAction = mediaAction;
        return mInstance;
    }

    public SandriosCamera enableImageCropping(boolean enableImageCrop) {
        this.enableImageCrop = enableImageCrop;
        return mInstance;
    }

    public SandriosCamera shouldShowCustomPreview(boolean isCustomPreview) {
        this.isCustomPreview = isCustomPreview;
        return mInstance;
    }

    public SandriosCamera shouldShowCaptionView(boolean showCaption) {
        this.showCaption = showCaption;
        return mInstance;
    }

    public SandriosCamera caption(String caption) {
        this.caption = caption;
        return mInstance;
    }

    public SandriosCamera setIntstructionText(String instructionText) {
        this.instructionText = instructionText;
        return mInstance;
    }

    public SandriosCamera setHideRetakeBtn(boolean isHideRetakeBtn) {
        this.isHideRetakeBtn = isHideRetakeBtn;
        return mInstance;
    }

    public SandriosCamera setIsRectangle(boolean isRectangle) {
        this.isRectangle = isRectangle;
        return mInstance;
    }

    public String getResponseData() {
        return responseData;
    }

    public SandriosCamera setResponseData(String responseData) {
        this.responseData = responseData;
        return mInstance;
    }

    public SandriosCamera setGalleryPageTitle(String pageTitle) {
        this.galleryPageTitle = pageTitle;
        return mInstance;
    }

    public SandriosCamera setVideoFileSize(int fileSize) {
        this.videoSize = fileSize;
        return mInstance;
    }

    public void launchCamera() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PermissionListener permissionListener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        launchIntent();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {

                    }
                };

                TedPermission.create()
                        .setPermissionListener(permissionListener)
                        .setPermissions(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();


            }
        });

    }

    public void launchCamera(Fragment fragment) {
        launchIntent(fragment);
    }

    private void launchIntent() {
        if (CameraHelper.hasCamera(mActivity)) {
            Intent cameraIntent;
            if (CameraHelper.hasCamera2(mActivity)) {
                cameraIntent = new Intent(mActivity, Camera2Activity.class);
            } else {
                cameraIntent = new Intent(mActivity, Camera1Activity.class);
            }
            cameraIntent.putExtra(CameraConfiguration.Arguments.REQUEST_CODE, requestCode);
            cameraIntent.putExtra(CameraConfiguration.Arguments.SHOW_PICKER, showPicker);
            cameraIntent.putExtra(CameraConfiguration.Arguments.MEDIA_ACTION, mediaAction);
            cameraIntent.putExtra(CameraConfiguration.Arguments.ENABLE_CROP, enableImageCrop);
            cameraIntent.putExtra(CameraConfiguration.Arguments.IS_RECTANGLE, isRectangle);
            cameraIntent.putExtra(CameraConfiguration.Arguments.RESPONSE_DATA, responseData);

            cameraIntent.putExtra(CameraConfiguration.Arguments.ENABLE_CUSTOM_PREVIEW, isCustomPreview);
            cameraIntent.putExtra(CameraConfiguration.Arguments.SHOW_CAPTION, showCaption);
            cameraIntent.putExtra(CameraConfiguration.Arguments.CAPTION, caption);
            cameraIntent.putExtra(CameraConfiguration.Arguments.INSTRUCTION_TEXT, instructionText);
            cameraIntent.putExtra(CameraConfiguration.Arguments.GALLERY_PAGE_TITLE, galleryPageTitle);
            //cameraIntent.putExtra(CameraConfiguration.Arguments.FLASH_MODE, new CameraPref(mActivity).getFlashMode());
            if (videoSize > 0) {
                cameraIntent.putExtra(CameraConfiguration.Arguments.VIDEO_FILE_SIZE, videoSize * 1024 * 1024);
            }

            try {
               JSONObject jsonObjectResponseData = new JSONObject(responseData);

                if(jsonObjectResponseData.has("setMediaQuality")){
                    int mediaQuality = jsonObjectResponseData.getInt("setMediaQuality");
                    cameraIntent.putExtra(CameraConfiguration.Arguments.MEDIA_QUALITY,mediaQuality);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mActivity.startActivityForResult(cameraIntent, requestCode);
            mActivity.overridePendingTransition(R.anim.anim_from_right, R.anim.anim_from_left);
        }
    }

    private void launchIntent(Fragment fragment) {
        if (CameraHelper.hasCamera(mActivity)) {
            Intent cameraIntent;
            if (CameraHelper.hasCamera2(mActivity)) {
                cameraIntent = new Intent(mActivity, Camera2Activity.class);
            } else {
                cameraIntent = new Intent(mActivity, Camera1Activity.class);
            }
            cameraIntent.putExtra(CameraConfiguration.Arguments.REQUEST_CODE, requestCode);
            cameraIntent.putExtra(CameraConfiguration.Arguments.SHOW_PICKER, showPicker);
            cameraIntent.putExtra(CameraConfiguration.Arguments.MEDIA_ACTION, mediaAction);
            cameraIntent.putExtra(CameraConfiguration.Arguments.ENABLE_CROP, enableImageCrop);
            cameraIntent.putExtra(CameraConfiguration.Arguments.SHOW_CAPTION, showCaption);
            cameraIntent.putExtra(CameraConfiguration.Arguments.ENABLE_CUSTOM_PREVIEW, isCustomPreview);
            //cameraIntent.putExtra(CameraConfiguration.Arguments.FLASH_MODE, new CameraPref(mActivity).getFlashMode());

            cameraIntent.putExtra(CameraConfiguration.Arguments.INSTRUCTION_TEXT, instructionText);
            if (videoSize > 0) {
                cameraIntent.putExtra(CameraConfiguration.Arguments.VIDEO_FILE_SIZE, videoSize * 1024 * 1024);
            }
            fragment.startActivityForResult(cameraIntent, requestCode);
            mActivity.overridePendingTransition(R.anim.anim_from_right, R.anim.anim_from_left);
        }
    }

}
