package com.hokuapps.loadnativefileupload.utilities;

import static com.hokuapps.loadnativefileupload.NativeFileUpload.REQUEST_FILE_BROWSER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Toast;


import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.loadnativefileupload.R;
import com.hokuapps.loadnativefileupload.constants.FileUploadConstant;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class FileUploadUtility {

    private Context context;

    private SharedPreferences preferences;
    private ReentrantReadWriteLock reentrantReadWriteLock;

    public FileUploadUtility(Context context) {
        this.context = context;
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        preferences = context.getSharedPreferences(context.getString(R.string.pref_name), Context.MODE_PRIVATE);
    }


    public static void showAlertMessage(Context context, String msg, String title) {
        if (context == null) {
            return;
        } else if (context instanceof Activity && ((Activity) context).isFinishing()) {
            return;
        }

        //Instantiate an AlertDialog.Builder with its constructor
        new androidx.appcompat.app.AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                .setMessage(msg)
                .setTitle(title)
                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public static boolean isEmpty(String string) {
        if (string != null) {
            string = string.trim();
        }
        return TextUtils.isEmpty(string);
    }



    /**
     * Change color to primary color
     *
     * @param color - String color as #FFFFFF
     * @return
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



    public static String getRandomUUID() {
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 24; i++) {
            sb.append(Integer.toHexString(rnd.nextInt(16)));
        }
        return sb.toString();
    }

    public static long getCurrentDateTimeInMS() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime().getTime();
    }


    public static void launchIntentByFileFormat(final Activity activity, final String type) {
        activity.runOnUiThread(() -> {

            PermissionListener permissionListener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType(type);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activity.startActivityForResult(intent, REQUEST_FILE_BROWSER);
                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {

                }




            };
            TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
                    .check();

        });
    }



    /**
     * Show toast message to user
     *
     * @param context
     * @param msg
     */
    public static void showMessage(Context context, String msg) {
        if (context != null && !TextUtils.isEmpty(msg))
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static String showAlertBridgeMissingKeys(Context context, String jsonData, String[] requiredJSONObjectKey) {

        String missingKeysMsg = "";

            try {
                missingKeysMsg = FileUploadUtility.checkBridgeMissingKeys(context, new JSONObject(jsonData), requiredJSONObjectKey);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        return missingKeysMsg;
    }

    public static String checkBridgeMissingKeys(Context context, JSONObject missingValues, String[] requiredValues) {

        StringBuffer missingKeys = new StringBuffer();

        for (int i = 0; i < requiredValues.length; i++) {

            if (!missingValues.has(requiredValues[i])) {

                if (missingKeys.length() == 0) {
                    missingKeys.append(requiredValues[i]);
                } else {
                    missingKeys.append(", " + requiredValues[i]);
                }

            }
        }

        return "Missing keys = " + missingKeys;
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

    public static int getJsonObjectIntValue(JSONObject obj, String fieldName) throws JSONException {
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

    public static void callJavaScriptFunction(Activity activity, final WebView webView, final String callingJavaScriptFn, final JSONObject response) {

        if (activity == null) return;


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        webView.evaluateJavascript(String.format("javascript:" + callingJavaScriptFn + "(%s)", response), null);
                    } else {
                        webView.loadUrl(String.format("javascript:" + callingJavaScriptFn + "(%s)", response));
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    public static File getHtmlDirFromSandbox(Context mContext) {
        File htmlDir = new File(mContext.getFilesDir() + File.separator + FileUploadConstant.FOLDER_NAME_WEB_HTML);

        if (!makeDirectory(htmlDir)) {
            return null;
        }

        return htmlDir;
    }

    /**
     * Create the storage directory if it does not exist
     *
     * @param mediaStorageDir
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
}
