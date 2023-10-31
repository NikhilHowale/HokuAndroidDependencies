package com.hokuapps.startvideocall.utils;

public class AppConstant {

    public static final String Call_PARAMS = "CallParams";
    public static final int INVALID_ID = -1;

    public static final int IMAGE_THUMB_SIZE_128 = 128;

    public static final int CALL_END_DELAY = 45000;
    public static final int BOTTOM_MENU_COLLAPSE_DELAY = 10000;
    public static final String EXTRA_USER_DETAILS = "extra_user_details";
    public static final String FOLDER_NAME_PROFILE_THUMB = "ProfileThumb";

    //Twilio Call
    public static final String TWILIO_CALL_ACTION = "${applicationId}.TWILIO_CALL_CANCEL";

    public static String CALL_STATUS_URL = "https://nannystreet.hokuapps.com/api/customsnippet_silentcallnotifications_hiddenfield_62321e0005023a1884c1d19c";
   // public static String CALL_STATUS_URL = "https://console.restoration-os.org/api/customsnippet_silentcallnotifications_hiddenfield_62321e0005023a1884c1d19c";
    public static final String MISSED_CALL_INFO_URL = "https://nannystreet.hokuapps.com/api/saveAjaxMissedcallInfo";
    public static final String CALL_ACTION = "call_action";
    public static final int CALL_ACCEPTED = 0;
    public static final int CALL_REJECTED = 1;
    public static final int RINGING = 2;

    public interface AppPref {

        String AUTH_TOKEN = "authToken";
        String AUTH_SECRET_KEY = "authSecretKey";

    }

    public static class CallData {

        public static String AUTH_TOKEN = "";
        public static String AUTH_SECRET_KEY = "";
        public static String NOTIFICATION_DATA = "";
        public static String CALL_UNIQUE_ID = "";

    }

    public interface ResponseCode{
        int STATUS_SUCCESS = 0;
        String STATUS_CODE = "statusCode";
    }

    public interface CallType {
        String VIDEO = "Video";
        String AUDIO = "Audio";
    }

    public interface BroadCastCallData {
        String DIALOG_CALL = "DIALOG_CALL";
        String CANCEL_CALL = "CANCEL_CALL";
        String RECEIVE_CALL = "RECEIVE_CALL";
        String ACTION_TYPE = "action_type";
        String NOTIFICATION_ID = "notification_id";
        String NOTIFICATION_DATA = "NotificationData";
    }

    public interface ManagerData {
        String MESSAGE_TEXT = "message";
        String UNIQUE_ID = "callUniqueId";
        String CALLER_NAME = "callerName";
        String PROFILE_IMAGE = "profileImage";
        String CALL_TYPE = "callType";
        String NOTIFICATION_DATA = "NotificationData";
    }

    public interface JSONFiled {
        String IS_AUDIO_ONLY = "isAudioOnly";
        String ROOM_NAME = "roomname";
        String USER_NAME = "username";
        String USER_ID = "userId";
        String CALLER_NAME = "callingname";
        String CALLER_IMAGE = "callingimage";
        String TWILIO_TOKEN_URL = "tokenurl";
        String CALL_UNIQUE_ID = "callUniqueID";
        String ACTION_STATUS = "actionstatus";
        String IS_VIDEO_CALL = "isVideoCalling";
        String VOIP_PAYLOAD = "voippayload";
        String SECRET_KEY = "secretKey";
        String TOKEN_KEY = "tokenKey";
        String ACCESS_TOKEN = "accesstoken";
        String STATUS = "status";
        String ERROR = "error";
        String IS_JOINING = "isJoining";
        String MESSAGE_TEXT = "messageText";
        String CALL_DENIED_URL = "deniedurl";
    }

}
