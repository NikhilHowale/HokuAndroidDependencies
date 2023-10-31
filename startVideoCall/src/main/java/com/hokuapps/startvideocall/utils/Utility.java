package com.hokuapps.startvideocall.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Utility {

    /**
     * This method check activity is live or not
     * @param activity activity reference
     * @return return true if activity is live
     */
    public static boolean isActivityLive(Activity activity) {
        if (activity == null) {
            return false;
        }
        return !activity.isFinishing();
    }

    /**
     * This method retrieve int value from jsonObject
     * @param obj json object
     * @param fieldName key name in json object
     * @return return int if key is available in jsonObject
     */
    public static int getJsonObjectIntValue(JSONObject obj, String fieldName) {
        if (obj == null) return 0;
        if (obj.has(fieldName)) {
            try {
                return obj.getInt(fieldName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * This method retrieve object value from jsonObject
     * @param obj json object
     * @param fieldName key name in json object
     * @return return another object if key is available in jsonObject
     */
    public static Object getJsonObjectValue(JSONObject obj, String fieldName) {
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

    /**
     * This method retrieve String value from jsonObject
     * @param obj json object
     * @param fieldName key name in json object
     * @return return String if key is available in jsonObject
     */
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

    /**
     * This method retrieve boolean value from jsonObject
     * @param obj json object
     * @param fieldName key name in json object
     * @return return boolean if key is available in jsonObject
     */
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

    /**
     * Check if file is exist
     */
    public static boolean isFileExist(File file) {
        boolean toReturn = false;

        if (file == null) return false;
        if (file.exists()) toReturn = true;

        return toReturn;
    }

    /**
     * Check string is null or empty by trim
     *
     * @param string string value for check
     * @return status
     */
    public static boolean isEmpty(String string) {
        if (string != null) {
            string = string.trim();
        }
        return TextUtils.isEmpty(string);
    }

    /**
     * This create local file for thumbnail
     * @param context context
     * @param filename filename
     * @return return newly created file
     */
    public static File generateLocalFilePathForThumbnail(Context context, String filename) {
        File root = context.getFilesDir();
        File file = new File(root + File.separator + AppConstant.FOLDER_NAME_PROFILE_THUMB);

        if (!file.exists()) {
            file.mkdir();
        }

        File newFile = new File(file.getAbsolutePath() + File.separator + filename);
        return newFile;
    }

    /**
     * This create local file for profile thumbnail
     * @param context context
     * @param fileName filename
     * @return return newly created file
     */
    public static File getProfileThumbPath(Context context,String fileName) {
        File root = context.getFilesDir();
        return new File(root + File.separator + AppConstant.FOLDER_NAME_PROFILE_THUMB + File.separator + fileName);
    }

    /**
     * Check Internet connection availability.
     * @param context context
     * @return return true if network is available
     */
    public static boolean isNetworkAvailable(Context context) {

        if (context == null) {
            return false;
        }
        ReentrantReadWriteLock REENTRANT_READ_WRITE_LOCK = new ReentrantReadWriteLock();
        ReentrantReadWriteLock.ReadLock readLock = REENTRANT_READ_WRITE_LOCK.readLock();
        readLock.lock();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // if no network is available networkInfo will be null, otherwise check if we are connected
        try {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void setCommunicationDevice(AudioManager audioManager, Integer targetDeviceType) {
        List<AudioDeviceInfo> devices = audioManager.getAvailableCommunicationDevices();
        for (AudioDeviceInfo device: devices) {
            if (device.getType() == targetDeviceType) {
                audioManager.setMode(AudioManager.MODE_NORMAL);
                boolean result = audioManager.setCommunicationDevice(device);
            }
        }
    }
}
