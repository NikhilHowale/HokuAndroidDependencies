package com.hokuapps.hokunativeshell.constants;

import android.content.Context;

import com.hokuapps.hokunativeshell.pref.MybeepsPref;

import org.json.JSONObject;

public class IntegrationManager {
    public static boolean isNativeBackHandle = false;
    public static String appStatusBarColor = "#FFFFFF";
    public static String appHeaderColor = "#FFFFFF";
    public static boolean isWhiteColor = false;
    public static String newUploadUrl = "";
    static IntegrationManager iManager;
    private MybeepsPref pref;
    private Context mContext;
    public static  String SYNC_OS_URL="";
    public static  String APP_FILE_URL="";
    public static  String SOCKET_HOST="";
    public static  String REST_API_HOST="";
    public static  String APP_FILE_HOST="";
    public static  String DEFAULT_TEMPLATE_SUB_DOMAIN="";
    public static  boolean PREVENT_SCREEN_CAPTURE = false;
    public static  boolean BLOCK_ROOTED_DEVICE_SUPPORT = false;
    public static  boolean DISABLE_NATIVE_BACK_BUTTON=false;

    private IntegrationManager(Context context) {
        this.mContext = context;
        pref = new MybeepsPref(mContext);
    }
}
