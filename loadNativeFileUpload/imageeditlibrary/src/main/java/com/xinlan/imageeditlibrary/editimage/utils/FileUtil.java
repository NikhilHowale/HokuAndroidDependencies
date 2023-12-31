package com.xinlan.imageeditlibrary.editimage.utils;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by panyi on 16/10/23.
 */
public class FileUtil {
    public static boolean checkFileExist(final String path) {
        if (TextUtils.isEmpty(path))
            return false;

        File file = new File(path);
        return file.exists();
    }

    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    /**
     * @param context
     * @param dstPath
     */
    public static void ablumUpdate(final Context context, final String dstPath) {
        try {
            if (TextUtils.isEmpty(dstPath) || context == null)
                return;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues(2);
                String extensionName = getExtensionName(dstPath);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + (TextUtils.isEmpty(extensionName) ? "jpeg" : extensionName));
                values.put(MediaStore.Images.Media.DISPLAY_NAME, dstPath);
                context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                ContentValues values = new ContentValues(2);
                String extensionName = getExtensionName(dstPath);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + (TextUtils.isEmpty(extensionName) ? "jpeg" : extensionName));
                values.put(MediaStore.Images.Media.DATA, dstPath);
                context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}//end class
