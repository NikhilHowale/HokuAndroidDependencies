package com.hokuapps.getlocalimage.utility;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.WebView;

import com.hokuapps.getlocalimage.R;
import com.hokuapps.getlocalimage.constant.ImageConstants;

import org.json.JSONObject;

import java.io.File;

public class Utility {


    /**
     * This method retrieve string from json object
     * @param obj jsonObject
     * @param fieldName key in json object
     * @return return string value
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
     *  This method create sandbox directory
     * @param context context
     * @return return the sandbox directory path
     */
    public static File getHtmlDirFromSandbox(Context context) {
        File htmlDir = new File(context.getFilesDir() + File.separator + ImageConstants.FOLDER_NAME_WEB_HTML);

        if (!makeDirectory(htmlDir)) {
            return null;
        }

        return htmlDir;
    }


    /**
     * Create the storage directory if it does not exist
     *
     * @param mediaStorageDir directory path
     * @return true if directory created successfully
     * false Otherwise
     */
    public static boolean makeDirectory(File mediaStorageDir) {
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if  file is exist at given file location.
     *
     * @param path
     * @return
     */
    public static boolean isFileExist(String path) {

        boolean toReturn = false;

        if (path == null || path.length() == 0) return toReturn;

        try {
            File file = new File(path);
            if (file.exists() || file.length() > 0) toReturn = true;
        } catch (Exception ex) {

        }

        return toReturn;
    }


    public static void callJavaScriptFunction(Activity activity, final WebView webView, final String callingJavaScriptFn, final JSONObject response) {

        if (activity == null) return;


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    webView.evaluateJavascript(String.format("javascript:" + callingJavaScriptFn + "(%s)", response), null);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    public static String getDownloadFileParentDir(String folderName, boolean isSandboxDir, Context mContext) {
        File fileRootDir = new File(Utility.getRootDirPath(mContext));
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
            return Utility.getHtmlDirFromSandbox(mContext).getAbsolutePath();
        }
    }

    /**
     * Return the MyBeepsApp external storage directory.
     *
     * @return
     */
    public static String getRootDirPath(Context mContext) {
        return getRootDir() + File.separator + getResString(R.string.app_name,mContext);
    }

    /**
     * Return the primary external storage directory.
     *
     * @return
     */
    public static File getRootDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static String getResString(int resId, Context mContext) {
        return mContext.getResources().getString(resId);
    }

    public static String getExtensionWithDot(String name) {
        String ext;

        if (TextUtils.isEmpty(name)) return "";

        if (name.lastIndexOf(".") == -1) {
            ext = "";

        } else {
            int index = name.lastIndexOf(".");
            ext = name.substring(index);
        }
        return ext;
    }

    public static String getFileNameWithoutExtension(String filename) {

        return filename.substring(0, filename.length() - getExtensionWithDot(filename).length());
    }

    public static String getFileName(String filePath) {

        if (!isFileExist(filePath)) return "";

        File file = new File(filePath);

        return file.getName();
    }

}
