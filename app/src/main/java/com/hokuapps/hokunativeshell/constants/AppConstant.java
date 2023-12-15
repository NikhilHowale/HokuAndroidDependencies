package com.hokuapps.hokunativeshell.constants;

public class AppConstant {

    public static final String LOGGED_IN_USER_ID = "user_id";
    public static final int BIOMETRIC_RESULT_CODE = 1001;

    private AppConstant() {
    }
    public static final String EXTRA_WEB_URL = "extra_web_url";
    public static final String EXTRA_SCREEN_TITLE = "extra_screen_title";
    public static final String EXTRA_MESSAGE_ID = "extra_messageId";
    public static final String EXTRA_APP_URL = "extra_app_url";
    public static final String EXTRA_INTEGRATION_ID = "extra_web_integrationId";
    public static final String EXTRA_APP_COMMAND = "extra_app_command";
    public static final String EXTRA_LFC = "extra_lfc";
    public static final String EXTRA_LOCAL_URL = "extra_local_url";

    public static final String EXTRA_IS_APP_COMMAND = "extra_is_app_command";
    public static final String EXTRA_QUERY_MODE = "extra_query_mode";
    public static final String EXTRA_QUERY_STRING = "extra_query_string";

    public static final String EXTRA_NOTIFICATION_DATA = "extra_notification_data";

    public static final String EXTRA_IS_HIDE_MOBILE_HEADER = "extra_is_hide_mobile_header";

    public static final String EXTRA_FILENAME_URL = "extra_filename_url";

    public static final String FOLDER_NAME_WEB_HTML = "WebHtml";

    public static final String NOTIFICATION_TOKEN = "notification_token";


    public interface FileName {

        String DEFAULT_FILE_NAME = "index.html";
        String DEFAULT_START_NAME = "indexstart.html";
        String DEFAULT_QUERY_MODE = "mylist";
    }

    public interface AppPref {
        String LAUNCH_FILE_NAME = "index.html";
        String AUTH_TOKEN = "authToken";

        String AUTH_SECRET_KEY = "authSecretKey";

        String ROLE_NAME = "roleName";

        String AUTHORIZATION_KEY = "authorizationKey";
    }

    public interface ActivityResultCode {
        int BIOMETRIC_RESULT_CODE = 1001;

        int CAPTURE_MEDIA_PHOTO = 368;

        int REQUEST_FILE_BROWSER = 9012;

        int SCAN_IMAGE_REQUEST_CAMERA = 9032;

        int SCAN_IMAGE_REQUEST_GALLERY = 9031;

        int SELECT_GALLERY_IMAGE_CODE = 7000;

        int ACTION_REQUEST_EDIT_IMAGE = 9006;

        int ACTION_MAP_GET_ADDRESS = 2400;

        int RC_BARCODE_CAPTURE = 9001;

        int ACTION_REQUEST_EDIT_IMAGE_MAP_PLAN = 9008;

        int RC_SIGN_IN = 9122;

        int LOCATION_SERVICE_REQUEST_CODE = 8000;

        int ACTION_REQUEST_EDIT_IMAGE_MAP = 9004;
        int ACTION_CAPTURE_VIDEO_RESULT = 1011;

        int RESULT_SEARCH_DROP_ACTIVITY = 2500;


    }
}
