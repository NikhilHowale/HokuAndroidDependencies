package com.hokuapps.loadnativefileupload.constants;

import com.hokuapps.loadnativefileupload.utilities.FileUtility;

public class FileUploadConstant {

    public static final String APP_TAG = "Library";
    public static final int INVALID_ID = -1;
    public static final int maxWidth = 1920/*720*/;
    public static final int maxHeight = 1080/*1280*/;
    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final int IMAGE_THUMB_SIZE_128 = 128;
    public static final String FOLDER_NAME_WEB_HTML = "WebHtml";
    public static final String FOLDER_NAME_IMAGE = FileUploadConstant.FOLDER_PARENT_NAME + " Images";
    public static final String FOLDER_NAME_VIDEO = FileUploadConstant.FOLDER_PARENT_NAME + " Videos";
    public static final String FOLDER_NAME_AUDIO = FileUploadConstant.FOLDER_PARENT_NAME + " Audio";
    public static final String FOLDER_PARENT_NAME = APP_TAG;
    public static final String FOLDER_NAME_MEDIA = "Media";
    public static final String FOLDER_NAME_PROFILE_PICTURE = "Profile Pictures";

    public interface MessageType {
        int TYPE_IMAGE = 1;
        int TYPE_AUDIO = 5;
        int TYPE_VIDEO = 2;
    }


    public static class REST_URLS {





        public static String UPLOAD_CHAT_MEDIA = "upload";
        public static String UPLOAD_ROOFING_MEDIA = "uploadFile";
        public static String UPLOAD_MP_HELITRACK = "upload";

    }
    public interface AuthIO {
        String AUTH_TOKEN = "token";
        String STATUS_CODE = "statusCode";

    }



}
