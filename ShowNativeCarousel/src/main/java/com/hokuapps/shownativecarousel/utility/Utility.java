package com.hokuapps.shownativecarousel.utility;

import static android.text.TextUtils.isEmpty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.hokuapps.shownativecarousel.R;
import com.hokuapps.shownativecarousel.constants.CarouselConstant;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;

public class Utility {

    /**
     * Show toast message to user
     * @param context context
     * @param id string id
     */
    public static void showMessage(Context context, int id) {
        Toast.makeText(context, id, Toast.LENGTH_LONG).show();
    }


    /**
     * get the field value from given json object and field name
     * @param obj json object
     * @param fieldName field name
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
     * get the boolean field value from given json object and field name
     * @param obj json object
     * @param fieldName field name
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
     * get the extension of the file with dot
     * @param name name of the file
     */
    public static String getExtensionWithDot(String name) {
        String ext;

        if (isEmpty(name)) return "";

        if (name.lastIndexOf(".") == -1) {
            ext = "";

        } else {
            int index = name.lastIndexOf(".");
            ext = name.substring(index);
        }
        return ext;
    }


    /**
     * get the file name without extension
     * @param filename name of the file
     */
    public static String getFileNameWithoutExtension(String filename) {

        return filename.substring(0, filename.length() - getExtensionWithDot(filename).length());
    }


    /**
     * Get the file name from the path
     * @param filePath path
     */
    public static String getFileName(String filePath) {

        if (!isFileExist(filePath)) return "";

        File file = new File(filePath);

        return file.getName();
    }

    /**
     * Check if  file is exist at given file location.
     * @param path file path
     */
    public static boolean isFileExist(String path) {

        boolean toReturn = false;

        if (path == null || path.length() == 0) return toReturn;

        try {
            File file = new File(path);
            if (file.exists() || file.length() > 0) toReturn = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return toReturn;
    }


    /**
     * get the root directory of the downloaded file
     * @param folderName folder name
     * @param isSandboxDir local root
     * @param context context
     */
    public static String getDownloadFileParentDir(String folderName, boolean isSandboxDir, Context context) {
        File fileRootDir = new File(Utility.getRootDirPath(context));
        if (!isSandboxDir) {

            if (!fileRootDir.exists()) {
                fileRootDir.mkdir();
            }

            File pdfFolder = new File(fileRootDir, folderName);

            if (!pdfFolder.exists()) {
                pdfFolder.mkdir();
            }

            return pdfFolder.getAbsolutePath();

        } else {
            return Objects.requireNonNull(Utility.getHtmlDirFromSandbox(context)).getAbsolutePath();
        }
    }

    /**
     * Return the MyBeepsApp external storage directory.
     */
    public static String getRootDirPath(Context context) {
        return getRootDir() + File.separator + getResString(R.string.app_name, context);
    }


    /**
     * get the string from id
     * @param resId resource id
     * @param mContext context
     */
    public static String getResString(int resId, Context mContext) {
        return mContext.getResources().getString(resId);
    }


    /**
     * Return the primary external storage directory.
     */
    public static File getRootDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }


    /**
     * get the root directory
     * @param context context
     */
    public static File getHtmlDirFromSandbox(Context context) {
        File htmlDir = new File(context.getFilesDir() + File.separator + CarouselConstant.FOLDER_NAME_WEB_HTML);

        if (!makeDirectory(htmlDir)) {
            return null;
        }

        return htmlDir;
    }

    /**
     * Create the storage directory if it does not exist
     *
     * @param mediaStorageDir file name
     * @return true if directory created successfully
     * false Otherwise
     */
    public static boolean makeDirectory(File mediaStorageDir) {
        if (!mediaStorageDir.exists()) {
            return mediaStorageDir.mkdirs();
        }
        return true;
    }



    /**
     * get the field value from given json object and field name
     * @param obj json object
     * @param fieldName field name
     */
    public static String getStringObjectValue(JSONObject obj, String fieldName) {
        try {
            if (obj == null) return "";

            if (obj.has(fieldName)) {
                Object o = obj.get(fieldName);
                return o.toString();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }



    /**
     * change toolbar text color based on theme
     * @param toolbar toolbar
     */
    public static void changedToolbarTextColorByTheme(View toolbar) {
        if (toolbar != null) {
            try {
                ((Toolbar) toolbar).setTitleTextColor(Color.parseColor("#000000"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * get Bitmap from given base 64 string
     * @param base64String base64 data string
     */
    public static Bitmap getBitmap(String base64String) {
        if (isEmpty(base64String)) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return bitmap;
    }

}
