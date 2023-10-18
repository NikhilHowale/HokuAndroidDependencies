package com.cometchat.pro.uikit;

import java.util.List;

public class AppConfig {

    public static AppConfig instance;

    public static String APP_ID = "";
    public static String AUTH_KEY = "";
    public static String HTML_DIRECTORY = "";
    public static String SECRET_KEY = "";
    public static String TOKEN_KEY = "";
    public static final String REGION = "us";
    public static String COMET_CHAT_USER_UID = "";
    public static String PUSH_TOKEN = "";
    public static String GROUP_ID  = "";
    public static String USER_ID  = "";
    public static String MEDICAL_DETAILS = "";
    public static boolean ENABLE_DELETE_MESSAGE = false;
    public static boolean DISABLE_LOCATION_MESSAGE = true;
    public static boolean ENABLE_PIN_MESSAGE = false;

    public static String CALLBACK_FUNCTION = "";

    public static boolean SHOW_DEFAULT_GALLERY = false;

    private List<String> roles;

    public static synchronized AppConfig getInstance(){
        if(instance == null ){
            instance = new AppConfig();
        }
        return instance;
    }

    public static class DashBoardTabs {
        public static boolean IS_CHATS = true;
        public static boolean IS_USERS = true;
        public static boolean IS_GROUPS = true;
    }

    public interface FcmConstant {

        String DEFAULT_FILE_NAME = "index.html";
        String EXTRA_NOTIFICATION_DATA = "extra_notification_data";
        String EXTRA_FILENAME_URL = "extra_filename_url";
        String EXTRA_LFC = "extra_lfc";
        String EXTRA_IS_APP_COMMAND = "extra_is_app_command";
        String EXTRA_QUERY_MODE = "extra_query_mode";
        String EXTRA_QUERY_STRING = "extra_query_string";
        String EXTRA_IS_HIDE_MOBILE_HEADER = "extra_is_hide_mobile_header";
        String CHANNEL_ID = "comet123";
        String IS_FROM_NOTIFICATION = "isFromNotification";
        String IS_COMET_CHAT = "isCometChat";
        String IS_LOCAL_HTML = "isLoadLocalHtml";
        String IS_FROM_AUTH = "isFromAuth";
        String NOTIFICATION_DATA = "data";

        String MESSAGE_TYPE = "type";
        String MESSAGE_TEXT = "text";
        String MESSAGE_IMAGE = "image";
        String MESSAGE_FILE = "file";
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
