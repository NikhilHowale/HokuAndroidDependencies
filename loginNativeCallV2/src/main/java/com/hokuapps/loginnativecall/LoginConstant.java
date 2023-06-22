package com.hokuapps.loginnativecall;

public class LoginConstant {
    public static final String FOLDER_NAME_WEB_HTML = "WebHtml";
    public interface AppPref {
        String AUTH_TOKEN = "authToken";
        String AUTH_SECRET_KEY = "authSecretKey";
        String EXPIRED_TIME = "expiredTime";
        String ROLE_NAME = "roleName";
    }

    public interface PUSH_SERVICE_TYPE {
        int ANDROID = 2;
    }

    public interface GCMConstant {

        final String PREF_NAME = "GCMPref";
        final String PREF_REG_ID = "registration_id";
        final String PREF_APP_VERSION = "appVersion";

    }

    public interface FileName {

        String DEFAULT_QUERY_MODE = "mylist";
    }

    public interface AuthIO {
        String STATUS_CODE = "statusCode";

    }


}
