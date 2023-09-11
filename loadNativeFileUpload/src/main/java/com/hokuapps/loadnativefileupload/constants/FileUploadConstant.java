package com.hokuapps.loadnativefileupload.constants;


public class FileUploadConstant {

    public static final String APP_TAG = "Library";
    public static final int INVALID_ID = -1;
    public static final String UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
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
        public static String UPLOAD_MP_HELITRACK = "upload";

    }
    public interface AuthIO {
        String AUTH_TOKEN = "token";
        String STATUS_CODE = "statusCode";

    }

    public interface options{
        String IS_SCAN_TEXT = "isScanText";
        String IS_DEFAULT_CAMERA ="isDefaultCamera";
        String IS_FREE_DRAW ="isFreeDraw";
        String IS_ANNOTATION = "isAnnotation";
        String IS_ANNOTATION_WITH_IMAGE_PATH = "isAnnotationWithImagePath";
        String IS_ANNOTATION_WITH_LOCAL_IMAGE = "isAnnotationWithLocalImage";
        String IS_ANNOTATION_WITH_IMAGE_URL = "isAnnotationWithImageUrl";

    }

    public interface Shape{
        String LINE ="Line";
        String CIRCLE = "Circle";
        String Rectangle = "Rectangle";
        String PATH = "Path";
    }
}
