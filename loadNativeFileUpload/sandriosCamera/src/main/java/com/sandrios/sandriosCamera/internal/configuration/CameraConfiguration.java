package com.sandrios.sandriosCamera.internal.configuration;

import android.app.Activity;
import androidx.annotation.IntDef;

import com.sandrios.sandriosCamera.internal.pref.CameraPref;
import com.sandrios.sandriosCamera.internal.ui.view.CameraSwitchView;
import com.sandrios.sandriosCamera.internal.ui.view.FlashSwitchView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Arpit Gandhi on 7/6/16.
 */
public final class CameraConfiguration {

    public static final int MEDIA_QUALITY_AUTO = 10;
    public static final int MEDIA_QUALITY_LOWEST = 15;
    public static final int MEDIA_QUALITY_LOW = 11;
    public static final int MEDIA_QUALITY_MEDIUM = 12;
    public static final int MEDIA_QUALITY_HIGH = 13;
    public static final int MEDIA_QUALITY_HIGHEST = 14;

    public static final int MEDIA_ACTION_VIDEO = 100;
    public static final int MEDIA_ACTION_PHOTO = 101;
    public static final int MEDIA_ACTION_BOTH = 102;

    public static final int CAMERA_FACE_FRONT = 0;
    public static final int CAMERA_FACE_REAR = 1;

    public static final int SENSOR_POSITION_UP = 90;
    public static final int SENSOR_POSITION_UP_SIDE_DOWN = 270;
    public static final int SENSOR_POSITION_LEFT = 0;
    public static final int SENSOR_POSITION_RIGHT = 180;
    public static final int SENSOR_POSITION_UNSPECIFIED = -1;

    public static final int DISPLAY_ROTATION_0 = 0;
    public static final int DISPLAY_ROTATION_90 = 90;
    public static final int DISPLAY_ROTATION_180 = 180;
    public static final int DISPLAY_ROTATION_270 = 270;

    public static final int ORIENTATION_PORTRAIT = 0x111;
    public static final int ORIENTATION_LANDSCAPE = 0x222;

    public static final int FLASH_MODE_ON = 1;
    public static final int FLASH_MODE_OFF = 2;
    public static final int FLASH_MODE_AUTO = 3;
    public static final int REQUEST_CROP_IMAGE = 9014;

    public static int getCurrentCameraType(Activity activity) {
        switch (new CameraPref(activity).getCameraRotation()) {
            case CameraSwitchView.CAMERA_TYPE_FRONT:
                return CameraSwitchView.CAMERA_TYPE_FRONT;
            case CameraSwitchView.CAMERA_TYPE_REAR:
            default:
                return CameraSwitchView.CAMERA_TYPE_REAR;
        }
    }

    public static int getFlashModeFromLocal(Activity activity) {
        switch (new CameraPref(activity).getFlashMode()) {
            case FlashSwitchView.FLASH_ON:
                return FlashSwitchView.FLASH_ON;
            case FlashSwitchView.FLASH_OFF:
                return FlashSwitchView.FLASH_OFF;
            case FlashSwitchView.FLASH_AUTO:
            default:
                return FlashSwitchView.FLASH_AUTO;
        }
    }

    public interface Arguments {
        String REQUEST_CODE = "com.sandrios.sandriosCamera.request_code";
        String MEDIA_ACTION = "com.sandrios.sandriosCamera.media_action";
        String MEDIA_QUALITY = "com.sandrios.sandriosCamera.camera_media_quality";
        String VIDEO_DURATION = "com.sandrios.sandriosCamera.video_duration";
        String MINIMUM_VIDEO_DURATION = "com.sandrios.sandriosCamera.minimum.video_duration";
        String VIDEO_FILE_SIZE = "com.sandrios.sandriosCamera.camera_video_file_size";
        String FILE_PATH = "com.sandrios.sandriosCamera.camera_video_file_path";
        String FLASH_MODE = "com.sandrios.sandriosCamera.camera_flash_mode";
        String SHOW_PICKER = "com.sandrios.sandriosCamera.show_picker";
        String ENABLE_CROP = "com.sandrios.sandriosCamera.enable_crop";
        String IS_RECTANGLE = "com.sandrios.sandriosCamera.is_rectangle";
        String RESPONSE_DATA = "com.sandrios.sandriosCamera.response_data";
        String ENABLE_CUSTOM_PREVIEW = "com.sandrios.sandriosCamera.enable_custom_preview";
        String INSTRUCTION_TEXT = "com.sandrios.sandriosCamera.INSTRUCTION_TEXT";
        String GALLERY_PAGE_TITLE = "com.sandrios.sandriosCamera.GALLERYPAGETITLE";
        String ISCAPTIONREADONLY = "com.sandrios.sandriosCamera.isCaptionReadOnly";
        String SHOW_CAPTION = "com.sandrios.sandriosCamera.IMAGE_CAPTION";
        String CAPTION = "com.sandrios.sandriosCamera.CAPTION";
        String IS_MULTIPLE_IMAGES = "is_multiple_images";
        String ARRAY_LIST_OF_IMAGES = "selectedImages";
    }

    @IntDef({MEDIA_QUALITY_AUTO, MEDIA_QUALITY_LOWEST, MEDIA_QUALITY_LOW, MEDIA_QUALITY_MEDIUM, MEDIA_QUALITY_HIGH, MEDIA_QUALITY_HIGHEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MediaQuality {
    }

    @IntDef({MEDIA_ACTION_VIDEO, MEDIA_ACTION_PHOTO, MEDIA_ACTION_BOTH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MediaAction {
    }

    @IntDef({CAMERA_FACE_FRONT, CAMERA_FACE_REAR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraFace {
    }

    @IntDef({SENSOR_POSITION_UP, SENSOR_POSITION_UP_SIDE_DOWN, SENSOR_POSITION_LEFT, SENSOR_POSITION_RIGHT, SENSOR_POSITION_UNSPECIFIED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SensorPosition {
    }

    @IntDef({DISPLAY_ROTATION_0, DISPLAY_ROTATION_90, DISPLAY_ROTATION_180, DISPLAY_ROTATION_270})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DisplayRotation {
    }

    @IntDef({ORIENTATION_PORTRAIT, ORIENTATION_LANDSCAPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DeviceDefaultOrientation {
    }

    @IntDef({FLASH_MODE_ON, FLASH_MODE_OFF, FLASH_MODE_AUTO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FlashMode {
    }
}
