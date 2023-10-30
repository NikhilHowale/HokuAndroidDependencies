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
import com.hokuapps.shownativecarousel.service.IntegrationManager;

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
    public static boolean isCampusAffairs() {
        return (CarouselConstant.APPLICATION_ID.equals("com.hokucampusaffairs"))
                || (CarouselConstant.FLAVOR.equalsIgnoreCase("BeepupCampusAffairs") && (CarouselConstant.APPLICATION_ID.equals("com.beepup")));
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
     * Change color to primary color
     *
     * @param color - String color as #FFFFFF
     */
    public static int changeColorToPrimaryHSB(String color) {
        float[] hsv = new float[3];
        int brandColor = Color.parseColor(color);
        Color.colorToHSV(brandColor, hsv);
        hsv[1] = hsv[1] + 0.1f;
        hsv[2] = hsv[2] - 0.1f;
        int argbColor = Color.HSVToColor(hsv);
        String hexColor = String.format("#%07X", argbColor);
        return Color.parseColor(hexColor);
    }


    /**
     * check if the application is RoofingSouthwest
     */
    public static boolean isRoofingSouthwest() {
        return (CarouselConstant.APPLICATION_ID.equals("com.roofingsouthwest"))
                || (CarouselConstant.FLAVOR.equalsIgnoreCase("RoofingSouthwestBeepup") && (CarouselConstant.APPLICATION_ID.equals("com.beepup")));
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
     * get the color from given drawable
     * @param activity activity reference
     * @param drawableResId drawable resource id
     * @param colorId color id
     */
    public static Drawable getColorDrawable(Activity activity, int drawableResId, String colorId) {
        @SuppressLint("UseCompatLoadingForDrawables") final Drawable drawable = activity.getResources().getDrawable(drawableResId);

        if (isEmpty(colorId)) return drawable;

        drawable.setColorFilter(Color.parseColor(colorId), PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }


    /**
     * change toolbar text color based on theme
     * @param toolbar toolbar
     */
    public static void changedToolbarTextColorByTheme(View toolbar) {
        if (toolbar != null) {
            try {
                String headerTextColor = Utility.getStringObjectValue(IntegrationManager.configJson, "headerTextColor");
                if (isEmpty(headerTextColor)) {
                    headerTextColor = "#000000";
                }
                if (CarouselConstant.LOAD_HTML_DIRECTLY && !isEmpty(headerTextColor)) {
                    if (toolbar instanceof TextView) {
                        ((TextView) toolbar).setTextColor(Color.parseColor(headerTextColor));
                    } else if (toolbar instanceof Toolbar) {
                        ((Toolbar) toolbar).setTitleTextColor(Color.parseColor(headerTextColor));
                    }
                }
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
