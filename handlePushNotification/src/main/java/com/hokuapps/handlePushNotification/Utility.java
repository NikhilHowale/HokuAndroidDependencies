package com.hokuapps.handlePushNotification;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Utility {

    public static String getStringObjectValue(JSONObject obj, String fieldName) {
        try {
            if (obj == null) return "";

            if (obj.has(fieldName)) {
                Object o = obj.get(fieldName);
                if (o != null) {
                    return o.toString();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
    public static boolean getJsonObjectBooleanValue(JSONObject obj, String fieldName) {
        if (obj == null) return false;

        try {

            if (obj.has(fieldName)) {
                return obj.getBoolean(fieldName);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static File getHtmlDirFromSandbox(Context context) {
        File htmlDir = new File(context.getFilesDir() + File.separator + AppConstant.FOLDER_NAME_WEB_HTML);

        if (!makeDirectory(htmlDir)) {
            return null;
        }

        return htmlDir;
    }


    public static Object getJsonObjectValue(JSONObject obj, String fieldName) throws JSONException {
        try {
            if (obj == null) return null;
            if (obj.has(fieldName)) {
                return obj.get(fieldName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean makeDirectory(File mediaStorageDir) {
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return false;
            }
        }
        return true;
    }


    /**
     * To get external chache dir "/Android/data/packageName/cache/";
     *
     * @param context
     * @return
     */
    public static File getExternalCacheDir(Context context) {
        File file = null;
        if (hasExternalCacheDir()) {
            file = context.getExternalCacheDir();
        }

        if (file == null) {
            final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
            file = new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
        }
        return file;
    }

    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

}
