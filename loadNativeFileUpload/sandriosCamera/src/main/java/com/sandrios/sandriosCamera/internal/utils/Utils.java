package com.sandrios.sandriosCamera.internal.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Surface;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Created by Arpit Gandhi on 7/18/16.
 */
public class Utils {

    /**
     *  Check device default orientation of device
     * @param context context
     * @return return default orientation
     */
    public static int getDeviceDefaultOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Configuration config = context.getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    /**
     * Check mime type of file
     * @param url path of  fi
     * @return return mime type
     */
    public static String getMimeType(String url) {
        String type = "";
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (!TextUtils.isEmpty(extension)) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        } else {
            String reCheckExtension = MimeTypeMap.getFileExtensionFromUrl(url.replaceAll("\\s+", ""));
            if (!TextUtils.isEmpty(reCheckExtension)) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(reCheckExtension);
            }
        }
        return type;
    }

    /**
     *  convert dp to pixel
     * @param context context
     * @param dip dip ( Density-independent Pixels) as int to convert
     * @return return actual pixel corresponds to screen
     */
    public static int convertDipToPixels(Context context, int dip) {
        Resources resources = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, resources.getDisplayMetrics());
        return (int) px;
    }

    /**
     *  Add image to media ( gallery ) directly
     * @param context context
     * @param mCurrentPhotoPath path of image
     */
    public static void galleryAddPic(Context context, String mCurrentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

}
