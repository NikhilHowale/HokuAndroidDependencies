package com.sandrios.sandriosCamera.internal.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.sandrios.sandriosCamera.R;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.preview.PreviewActivity;
import com.sandrios.sandriosCamera.internal.ui.view.CameraControlPanel;
import com.sandrios.sandriosCamera.internal.ui.view.CameraSwitchView;
import com.sandrios.sandriosCamera.internal.ui.view.FlashSwitchView;
import com.sandrios.sandriosCamera.internal.ui.view.MediaActionSwitchView;
import com.sandrios.sandriosCamera.internal.ui.view.RecordButton;
import com.sandrios.sandriosCamera.internal.utils.ScalingUtility;
import com.sandrios.sandriosCamera.internal.utils.Size;
import com.sandrios.sandriosCamera.internal.utils.Utils;
import com.xinlan.imageeditlibrary.picchooser.SelectPictureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arpit Gandhi on 12/1/16.
 */

public abstract class BaseSandriosActivity<CameraId> extends SandriosCameraActivity<CameraId>
        implements
        RecordButton.RecordButtonListener,
        FlashSwitchView.FlashModeSwitchListener,
        MediaActionSwitchView.OnMediaActionStateChangeListener,
        CameraSwitchView.OnCameraTypeChangeListener, CameraControlPanel.SettingsClickListener, CameraControlPanel.PickerItemClickListener, CameraControlPanel.GalleryClickListener {

    public static final int ACTION_CONFIRM = 900;
    public static final int ACTION_RETAKE = 901;
    public static final int ACTION_CANCEL = 902;
    protected static final int REQUEST_PREVIEW_CODE = 1001;
    public static final int REQUEST_SELECT_MULTIPLE_PHOTOS_FROM_GALLERY_CODE = 1004;
    protected JSONObject jsonObjectResponseData;
    protected int requestCode = -1;
    @CameraConfiguration.MediaAction
    protected int mediaAction = CameraConfiguration.MEDIA_ACTION_BOTH;
    @CameraConfiguration.MediaQuality
    protected int mediaQuality = CameraConfiguration.MEDIA_QUALITY_MEDIUM;
    @CameraConfiguration.MediaQuality
    protected int passedMediaQuality = CameraConfiguration.MEDIA_QUALITY_MEDIUM;
    protected CharSequence[] videoQualities;
    protected CharSequence[] photoQualities;
    protected boolean enableImageCrop = false;
    protected boolean isShutterSoundEnable = false;
    protected boolean isRectangle = false;
    protected String responseData = "";
    protected boolean isCustomPreview = false;
    protected boolean showCaption = false;
    protected String caption;
    protected String galleryPageTitle;

    protected int videoDuration = -1;
    protected long videoFileSize = -1;
    protected int minimumVideoDuration = -1;
    protected boolean showPicker = true;
    protected String instructionText = "";
    protected int currentMediaActionState;
    protected int currentCameraType = CameraSwitchView.CAMERA_TYPE_REAR;
    @CameraConfiguration.MediaQuality
    protected int newQuality = -1;
    protected int flashMode = CameraConfiguration.FLASH_MODE_AUTO;
    private CameraControlPanel cameraControlPanel;
    private AlertDialog settingsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentCameraType = CameraConfiguration.getCurrentCameraType(this);
        flashMode = CameraConfiguration.getFlashModeFromLocal(this);
    }

    @Override
    protected void onProcessBundle(Bundle savedInstanceState) {
        super.onProcessBundle(savedInstanceState);

        extractConfiguration(getIntent().getExtras());
        currentMediaActionState = mediaAction == CameraConfiguration.MEDIA_ACTION_VIDEO ?
                MediaActionSwitchView.ACTION_VIDEO : MediaActionSwitchView.ACTION_PHOTO;
    }

    @Override
    protected void onCameraControllerReady() {
        super.onCameraControllerReady();

        videoQualities = getVideoQualityOptions();
        photoQualities = getPhotoQualityOptions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraControlPanel.lockControls();
        cameraControlPanel.allowRecord(false);
        cameraControlPanel.showPicker(showPicker);
        cameraControlPanel.setInstructionText(instructionText);
    }

    @Override
    protected void onPause() {
        super.onPause();

        cameraControlPanel.lockControls();
        cameraControlPanel.allowRecord(false);
    }

    private void extractConfiguration(Bundle bundle) {
        if (bundle != null) {
            if (bundle.containsKey(CameraConfiguration.Arguments.REQUEST_CODE))
                requestCode = bundle.getInt(CameraConfiguration.Arguments.REQUEST_CODE);

            if (bundle.containsKey(CameraConfiguration.Arguments.MEDIA_ACTION)) {
                switch (bundle.getInt(CameraConfiguration.Arguments.MEDIA_ACTION)) {
                    case CameraConfiguration.MEDIA_ACTION_PHOTO:
                        mediaAction = CameraConfiguration.MEDIA_ACTION_PHOTO;
                        break;
                    case CameraConfiguration.MEDIA_ACTION_VIDEO:
                        mediaAction = CameraConfiguration.MEDIA_ACTION_VIDEO;
                        break;
                    default:
                        mediaAction = CameraConfiguration.MEDIA_ACTION_BOTH;
                        break;
                }
            }

            if (bundle.containsKey(CameraConfiguration.Arguments.MEDIA_QUALITY)) {
                switch (bundle.getInt(CameraConfiguration.Arguments.MEDIA_QUALITY)) {
                    case CameraConfiguration.MEDIA_QUALITY_AUTO:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_AUTO;
                        break;
                    case CameraConfiguration.MEDIA_QUALITY_HIGHEST:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_HIGHEST;
                        break;
                    case CameraConfiguration.MEDIA_QUALITY_HIGH:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_HIGH;
                        break;
                    case CameraConfiguration.MEDIA_QUALITY_MEDIUM:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_MEDIUM;
                        break;
                    case CameraConfiguration.MEDIA_QUALITY_LOW:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_LOW;
                        break;
                    case CameraConfiguration.MEDIA_QUALITY_LOWEST:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_LOWEST;
                        break;
                    default:
                        mediaQuality = CameraConfiguration.MEDIA_QUALITY_MEDIUM;
                        break;
                }
                passedMediaQuality = mediaQuality;
            }

            if (bundle.containsKey(CameraConfiguration.Arguments.VIDEO_DURATION))
                videoDuration = bundle.getInt(CameraConfiguration.Arguments.VIDEO_DURATION);

            if (bundle.containsKey(CameraConfiguration.Arguments.VIDEO_FILE_SIZE))
                videoFileSize = bundle.getLong(CameraConfiguration.Arguments.VIDEO_FILE_SIZE);

            if (bundle.containsKey(CameraConfiguration.Arguments.MINIMUM_VIDEO_DURATION))
                minimumVideoDuration = bundle.getInt(CameraConfiguration.Arguments.MINIMUM_VIDEO_DURATION);

            if (bundle.containsKey(CameraConfiguration.Arguments.SHOW_PICKER))
                showPicker = bundle.getBoolean(CameraConfiguration.Arguments.SHOW_PICKER);

            if (bundle.containsKey(CameraConfiguration.Arguments.ENABLE_CROP))
                enableImageCrop = bundle.getBoolean(CameraConfiguration.Arguments.ENABLE_CROP);

//            Crop image in rectangle or oval.
            if (bundle.containsKey(CameraConfiguration.Arguments.IS_RECTANGLE))
                isRectangle = bundle.getBoolean(CameraConfiguration.Arguments.IS_RECTANGLE);
//            Response data
            if (bundle.containsKey(CameraConfiguration.Arguments.RESPONSE_DATA)) {
                responseData = bundle.getString(CameraConfiguration.Arguments.RESPONSE_DATA);
                try {
                    jsonObjectResponseData = new JSONObject(bundle.getString(CameraConfiguration.Arguments.RESPONSE_DATA));

                    if(jsonObjectResponseData.has("isShutterSoundDisable") && jsonObjectResponseData.optBoolean("isShutterSoundDisable")){
                        isShutterSoundEnable = jsonObjectResponseData.getBoolean("isShutterSoundDisable");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (bundle.containsKey(CameraConfiguration.Arguments.INSTRUCTION_TEXT)) {
                instructionText = bundle.getString(CameraConfiguration.Arguments.INSTRUCTION_TEXT);
            }

            if (bundle.containsKey(CameraConfiguration.Arguments.ENABLE_CUSTOM_PREVIEW)) {
                isCustomPreview = bundle.getBoolean(CameraConfiguration.Arguments.ENABLE_CUSTOM_PREVIEW);
            }

            if (bundle.containsKey(CameraConfiguration.Arguments.SHOW_CAPTION)) {
                showCaption = bundle.getBoolean(CameraConfiguration.Arguments.SHOW_CAPTION);
            }

            if (bundle.containsKey(CameraConfiguration.Arguments.CAPTION)) {
                caption = bundle.getString(CameraConfiguration.Arguments.CAPTION);
            }

            if (bundle.containsKey(CameraConfiguration.Arguments.GALLERY_PAGE_TITLE)) {
                galleryPageTitle = bundle.getString(CameraConfiguration.Arguments.GALLERY_PAGE_TITLE);
            }

            if (bundle.containsKey(CameraConfiguration.Arguments.FLASH_MODE)) {
                switch (bundle.getInt(CameraConfiguration.Arguments.FLASH_MODE)) {
                    case CameraConfiguration.FLASH_MODE_AUTO:
                        flashMode = CameraConfiguration.FLASH_MODE_AUTO;
                        break;
                    case CameraConfiguration.FLASH_MODE_ON:
                        flashMode = CameraConfiguration.FLASH_MODE_ON;
                        break;
                    case CameraConfiguration.FLASH_MODE_OFF:
                        flashMode = CameraConfiguration.FLASH_MODE_OFF;
                        break;
                    default:
                        flashMode = CameraConfiguration.FLASH_MODE_AUTO;
                        break;
                }
            }
        }
    }

    @Override
    View getUserContentView(LayoutInflater layoutInflater, ViewGroup parent) {
        cameraControlPanel = (CameraControlPanel) layoutInflater.inflate(R.layout.user_control_layout, parent, false);

        if (cameraControlPanel != null) {
            cameraControlPanel.setup(getMediaAction());

            switch (flashMode) {
                case CameraConfiguration.FLASH_MODE_AUTO:
                    cameraControlPanel.setFlasMode(FlashSwitchView.FLASH_AUTO);
                    break;
                case CameraConfiguration.FLASH_MODE_ON:
                    cameraControlPanel.setFlasMode(FlashSwitchView.FLASH_ON);
                    break;
                case CameraConfiguration.FLASH_MODE_OFF:
                    cameraControlPanel.setFlasMode(FlashSwitchView.FLASH_OFF);
                    break;
            }

            cameraControlPanel.setRecordButtonListener(this);
            cameraControlPanel.setFlashModeSwitchListener(this);
            cameraControlPanel.setOnMediaActionStateChangeListener(this);
            cameraControlPanel.setOnCameraTypeChangeListener(this);
            cameraControlPanel.setMaxVideoDuration(getVideoDuration());
            cameraControlPanel.setMaxVideoFileSize(getVideoFileSize());
            cameraControlPanel.setSettingsClickListener(this);
            cameraControlPanel.setGalleryClickListener(this);
            cameraControlPanel.shouldShowCrop(enableImageCrop);
            cameraControlPanel.setIsRectangle(isRectangle);
            cameraControlPanel.setResponseData(responseData);
        }
        return cameraControlPanel;
    }

    @Override
    public void onGalleryClick() {
        if (jsonObjectResponseData != null
                && (jsonObjectResponseData.has("isMultiSelectImage") && jsonObjectResponseData.optBoolean("isMultiSelectImage"))) {

            try {
                ImagePicker.create(this) // Activity or Fragment
                        .folderMode(true)
                        .multi() // single mode
                        .showCamera(false)
                        .start(REQUEST_SELECT_MULTIPLE_PHOTOS_FROM_GALLERY_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else{

            SelectPictureActivity.startActivityForResultImage(this,
            SelectPictureActivity.IMAGE_GALLERY, TextUtils.isEmpty(galleryPageTitle)
            ? "Select Image" : galleryPageTitle, "#3c3c3c", SelectPictureActivity.SELECT_GALLERY_IMAGE_CODE);

        }


    }

    @Override
    public void onSettingsClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (currentMediaActionState == MediaActionSwitchView.ACTION_VIDEO) {
            builder.setSingleChoiceItems(videoQualities, getVideoOptionCheckedIndex(), getVideoOptionSelectedListener());
            if (getVideoFileSize() > 0)
                builder.setTitle(String.format(getString(R.string.settings_video_quality_title),
                        "(Max " + String.valueOf(getVideoFileSize() / (1024 * 1024) + " MB)")));
            else
                builder.setTitle(String.format(getString(R.string.settings_video_quality_title), ""));
        } else {
            builder.setSingleChoiceItems(photoQualities, getPhotoOptionCheckedIndex(), getPhotoOptionSelectedListener());
            builder.setTitle(R.string.settings_photo_quality_title);
        }

        builder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (newQuality > 0 && newQuality != mediaQuality) {
                    mediaQuality = newQuality;
                    dialogInterface.dismiss();
                    cameraControlPanel.lockControls();
                    getCameraController().switchQuality();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        settingsDialog = builder.create();
        settingsDialog.show();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(settingsDialog.getWindow().getAttributes());
        layoutParams.width = Utils.convertDipToPixels(this, 350);
        layoutParams.height = Utils.convertDipToPixels(this, 350);
        settingsDialog.getWindow().setAttributes(layoutParams);
    }

    @Override
    public void onItemClick(Uri filePath) {
        startPreviewActivity(filePath.getPath(), true);
    }

    @Override
    public void onCameraTypeChanged(@CameraSwitchView.CameraType int cameraType) {
        if (currentCameraType == cameraType) return;
        currentCameraType = cameraType;

        cameraControlPanel.lockControls();
        cameraControlPanel.allowRecord(false);

        int cameraFace = cameraType == CameraSwitchView.CAMERA_TYPE_FRONT
                ? CameraConfiguration.CAMERA_FACE_FRONT : CameraConfiguration.CAMERA_FACE_REAR;

        getCameraController().switchCamera(cameraFace);
    }


    @Override
    public void onFlashModeChanged(@FlashSwitchView.FlashMode int mode) {
        switch (mode) {
            case FlashSwitchView.FLASH_AUTO:
                flashMode = CameraConfiguration.FLASH_MODE_AUTO;
                getCameraController().setFlashMode(CameraConfiguration.FLASH_MODE_AUTO);
                break;
            case FlashSwitchView.FLASH_ON:
                flashMode = CameraConfiguration.FLASH_MODE_ON;
                getCameraController().setFlashMode(CameraConfiguration.FLASH_MODE_ON);
                break;
            case FlashSwitchView.FLASH_OFF:
                flashMode = CameraConfiguration.FLASH_MODE_OFF;
                getCameraController().setFlashMode(CameraConfiguration.FLASH_MODE_OFF);
                break;
        }
    }


    @Override
    public void onMediaActionChanged(int mediaActionState) {
        if (currentMediaActionState == mediaActionState) return;
        currentMediaActionState = mediaActionState;
    }

    @Override
    public void onTakePhotoButtonPressed() {
        getCameraController().takePhoto();
    }

    @Override
    public void onStartRecordingButtonPressed() {
        getCameraController().startVideoRecord();
    }

    @Override
    public void onStopRecordingButtonPressed() {
        getCameraController().stopVideoRecord();
    }

    @Override
    protected void onScreenRotation(int degrees) {
//        cameraControlPanel.rotateControls(degrees);
//        rotateSettingsDialog(degrees);
    }

    @Override
    public int getRequestCode() {
        return requestCode;
    }

    @Override
    public int getMediaAction() {
        return mediaAction;
    }

    @Override
    public int getMediaQuality() {
        return mediaQuality;
    }

    @Override
    public int getVideoDuration() {
        return videoDuration;
    }

    @Override
    public long getVideoFileSize() {
        return videoFileSize;
    }

    @Override
    public int getFlashMode() {
        return flashMode;
    }

    @Override
    public int getMinimumVideoDuration() {
        return minimumVideoDuration / 1000;
    }

    @Override
    public boolean isShutterSoundEnable() {
        return isShutterSoundEnable;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void updateCameraPreview(Size size, View cameraPreview) {
        cameraControlPanel.unLockControls();
        cameraControlPanel.allowRecord(true);

        setCameraPreview(cameraPreview, size);
    }

    @Override
    public void updateUiForMediaAction(@CameraConfiguration.MediaAction int mediaAction) {

    }

    @Override
    public void updateCameraSwitcher(int numberOfCameras) {
        cameraControlPanel.allowCameraSwitching(numberOfCameras > 1);
    }


    @Override
    public void onPhotoTaken() {
        ScalingUtility.saveBitmap(ScalingUtility.decodeFile(getCameraController().getOutputFile().toString()),
                getCameraController().getOutputFile().toString());
        Utils.galleryAddPic(this, getCameraController().getOutputFile().toString());
        if (isCustomPreview) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(CameraConfiguration.Arguments.FILE_PATH,
                    getCameraController().getOutputFile().toString());
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            startPreviewActivity(null, false);
        }
    }

    @Override
    public void onZoomInOrOut(int zoom) {
        cameraControlPanel.setZoomlevel(zoom);
    }

    @Override
    public void onVideoRecordStart(int width, int height) {
        cameraControlPanel.onStartVideoRecord(getCameraController().getOutputFile());
    }

    @Override
    public void onVideoRecordStop() {
        cameraControlPanel.allowRecord(false);
        cameraControlPanel.onStopVideoRecord();
        startPreviewActivity(null, false);
    }

    @Override
    public void releaseCameraPreview() {
        clearCameraPreview();
    }

    private void startPreviewActivity(String filePath, boolean isHideRetake) {
        Intent intent;
        if (filePath == null) {
            intent = PreviewActivity.newIntent(this,
                    getMediaAction(),
                    getCameraController().getOutputFile().toString(),
                    cameraControlPanel.showCrop(),
                    showCaption,
                    caption,
                    isHideRetake, false, cameraControlPanel.isRectangle(),cameraControlPanel.getResponseData());
        } else {
            intent = PreviewActivity.newIntent(this,
                    getMediaAction(),
                    filePath,
                    cameraControlPanel.showCrop(),
                    showCaption,
                    caption,
                    isHideRetake);
        }
        startActivityForResult(intent, REQUEST_PREVIEW_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            List<Image> images = null;
            if (requestCode == REQUEST_PREVIEW_CODE) {
                if (PreviewActivity.isResultConfirm(data)) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(CameraConfiguration.Arguments.FILE_PATH,
                            PreviewActivity.getMediaFilePatch(data));
                    resultIntent.putExtra(CameraConfiguration.Arguments.CAPTION,
                            PreviewActivity.getCaptionPatch(data));
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else if (PreviewActivity.isResultCancel(data)) {
                    setResult(RESULT_CANCELED);
                    finish();
                } else if (PreviewActivity.isResultRetake(data)) {
                    //ignore, just proceed the camera
                }
            } else if (requestCode == SelectPictureActivity.SELECT_GALLERY_IMAGE_CODE) {

                String imgPath = data.getStringExtra("imgPath");

                Intent resultIntent = new Intent();
                resultIntent.putExtra(CameraConfiguration.Arguments.FILE_PATH,
                        imgPath);
                resultIntent.putExtra(CameraConfiguration.Arguments.CAPTION,
                        "");
                resultIntent.putExtra("isFromGallery",
                        true);
                setResult(RESULT_OK, resultIntent);
                finish();
            }else if (requestCode == REQUEST_SELECT_MULTIPLE_PHOTOS_FROM_GALLERY_CODE) {
                // Get a list of picked images
                images = ImagePicker.getImages(data);
                if (images != null && images.size() == 1) {
                    String imgPath = images.get(0).getPath();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(CameraConfiguration.Arguments.FILE_PATH,
                            imgPath);
                    resultIntent.putExtra(CameraConfiguration.Arguments.CAPTION,
                            caption);
//                    Is showing multiple images from gallery.
                    resultIntent.putExtra(CameraConfiguration.Arguments.IS_MULTIPLE_IMAGES,
                            false);

                   // resultIntent.putExtra(CameraConfiguration.Arguments.ISCAPTIONREADONLY, isCaptionReadOnly);

                    resultIntent.putExtra("isFromGallery",
                            true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
//                    Handle multiple images here.
                    images = ImagePicker.getImages(data);
                    if (images != null && images.size() > 1) {

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(CameraConfiguration.Arguments.CAPTION,
                                caption);
//                    Is showing multiple images from gallery.
                        resultIntent.putExtra(CameraConfiguration.Arguments.IS_MULTIPLE_IMAGES,
                                true);

                       // resultIntent.putExtra(CameraConfiguration.Arguments.ISCAPTIONREADONLY, isCaptionReadOnly);
                        resultIntent.putParcelableArrayListExtra(CameraConfiguration.Arguments.ARRAY_LIST_OF_IMAGES, (ArrayList<? extends Parcelable>) images);

                        resultIntent.putExtra("isFromGallery",
                                false);
                        setResult(RESULT_OK, resultIntent);
                        finish();

                    }
                }
            }
        }
    }

    private void rotateSettingsDialog(int degrees) {
        if (settingsDialog != null && settingsDialog.isShowing() && Build.VERSION.SDK_INT > 10) {
            ViewGroup dialogView = (ViewGroup) settingsDialog.getWindow().getDecorView();
            for (int i = 0; i < dialogView.getChildCount(); i++) {
                dialogView.getChildAt(i).setRotation(degrees);
            }
        }
    }

    protected abstract CharSequence[] getVideoQualityOptions();

    protected abstract CharSequence[] getPhotoQualityOptions();

    protected int getVideoOptionCheckedIndex() {
        int checkedIndex = -1;
        if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_AUTO) checkedIndex = 0;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGH) checkedIndex = 1;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_MEDIUM) checkedIndex = 2;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOW) checkedIndex = 3;

        if (passedMediaQuality != CameraConfiguration.MEDIA_QUALITY_AUTO) checkedIndex--;

        return checkedIndex;
    }

    protected int getPhotoOptionCheckedIndex() {
        int checkedIndex = -1;
        if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGHEST) checkedIndex = 0;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGH) checkedIndex = 1;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_MEDIUM) checkedIndex = 2;
        else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOWEST) checkedIndex = 3;
        return checkedIndex;
    }

    protected DialogInterface.OnClickListener getVideoOptionSelectedListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {

            }
        };
    }

    protected DialogInterface.OnClickListener getPhotoOptionSelectedListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int index) {

            }
        };
    }

}
