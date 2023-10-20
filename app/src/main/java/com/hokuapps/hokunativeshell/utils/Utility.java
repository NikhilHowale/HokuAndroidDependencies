package com.hokuapps.hokunativeshell.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.hokuapps.hokunativeshell.App;
import com.hokuapps.hokunativeshell.constants.AppConstant;
import com.hokuapps.hokunativeshell.constants.IntegrationManager;
import com.hokuapps.hokunativeshell.pref.MybeepsPref;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.XMLReader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utility {

    private static final String TAG = "Utility";

    public static void copyAssetDirToSandbox(Context context, String fromAssetPath, String toPath) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(fromAssetPath);
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }

        if (files != null) {
            for (String file : files) {

                if (!FileUtility.isFileExist(toPath + "/" + file)) {

                    if (!TextUtils.isEmpty(file) && (file.toLowerCase().indexOf(".zip") != -1)) {

                        extractFileToHtmlDir(context, fromAssetPath, file, Utility.getHtmlDirFromSandbox().getAbsolutePath());
                    }
                    copyAssetFile(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);

                }
            }
        }
    }

    private static void copyAssetFile(AssetManager assetManager, String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private static void extractFileToHtmlDir(Context context, String fromAssetPath, String sFile, String filePathToExtract) {
        InputStream in = null;
        try {
            AssetManager assetManager = context.getAssets();
            in = assetManager.open(fromAssetPath + "/" + sFile);
            writeZipAndExctractFileToLocal(in, filePathToExtract);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void writeZipAndExctractFileToLocal(InputStream in, String path) throws Exception {

        ArrayList<String> data = new ArrayList<>();
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(in));
        ZipEntry zipEntry = null;
        try {
            /** Iterate over all the files and folders*/
            for (zipEntry = zipInputStream.getNextEntry(); zipEntry != null; zipEntry = zipInputStream.getNextEntry()) {

                String innerFileName = path + File.separator
                        + zipEntry.getName();
                File innerFile = new File(innerFileName);

                try {
                    ensureZipPathSafety(innerFile, innerFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                /** Checking for pre-existence of the file and taking* necessary actions*/
                if (innerFile.exists()) {
                    innerFile.delete();
                }

                if (zipEntry.isDirectory()) {
                    innerFile.mkdirs();
                } else {
                    FileOutputStream outputStream = new FileOutputStream(
                            innerFileName);
                    final int BUFFER_SIZE = 2048;

                    /* Get the buffered output stream*/
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                            outputStream, BUFFER_SIZE);
                    /*
                     * Write into the file's buffered output stream
                     */
                    int count = 0;
                    byte[] buffer = new byte[BUFFER_SIZE];
                    while ((count = zipInputStream.read(buffer, 0,
                            BUFFER_SIZE)) != -1) {
                        bufferedOutputStream.write(buffer, 0, count);
                    }

                    /** Handle closing of output streams..*/
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }

                /** Finish the current zipEntry*/
                zipInputStream.closeEntry();


            }
            /** Handle closing of input stream.*/
            zipInputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void ensureZipPathSafety(File innerFile, String innerFileName) throws Exception {

        String destDirCanonicalPath = (new File(innerFileName)).getCanonicalPath();
        String outputFilecanonicalPath = innerFile.getCanonicalPath();
        if (!outputFilecanonicalPath.startsWith(destDirCanonicalPath)) {
            throw new Exception(String.format("Found Zip Path Traversal Vulnerability with %s", destDirCanonicalPath));
        }
    }

    public static File getHtmlDirFromSandbox() {
        File htmlDir = new File(App.getInstance().getFilesDir() + File.separator + AppConstant.FOLDER_NAME_WEB_HTML);

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


    public static void setNativeCallbackSettingFlag() {
        try {
            String fileContents = FileUtility.readAllFileContent(
                    "appsetting".toLowerCase().replace(" ", "_") + ".json", Utility.getHtmlDirFromSandbox().getAbsolutePath());

            if (TextUtils.isEmpty(fileContents)) return;

            String SynOSUrl = Utility.getStringObjectValue(new JSONObject(fileContents),
                    "SynOSUrl");

            if (SynOSUrl != null)
                IntegrationManager.SYNC_OS_URL = SynOSUrl;

            String AppfilesUrl = Utility.getStringObjectValue(new JSONObject(fileContents),
                    "AppfilesUrl");

            if (AppfilesUrl != null)
                IntegrationManager.APP_FILE_URL = AppfilesUrl;

            String DefaultDomain = Utility.getStringObjectValue(new JSONObject(fileContents),
                    "DefaultDomain");

            if (DefaultDomain != null)
                IntegrationManager.DEFAULT_TEMPLATE_SUB_DOMAIN = DefaultDomain;

            String appFilesServer = Utility.getStringObjectValue(new JSONObject(fileContents),
                    "appFilesServer");

            if (appFilesServer != null)
                IntegrationManager.APP_FILE_HOST = appFilesServer;

            String socketHost = Utility.getStringObjectValue(new JSONObject(fileContents),
                    "SocketHost");

            if (socketHost != null)
                IntegrationManager.SOCKET_HOST = socketHost;

            String RestApiHost = Utility.getStringObjectValue(new JSONObject(fileContents),
                    "RestApiHost");

            if (RestApiHost != null)
                IntegrationManager.REST_API_HOST = RestApiHost;

            IntegrationManager.isNativeBackHandle = Utility.getJsonObjectBooleanValue(new JSONObject(fileContents),
                    "isnativecallback");

            boolean isScreenRecordingAllowed = Utility.getJsonObjectBooleanValue(new JSONObject(fileContents),
                    "isScreenRecordingAllowed");
            IntegrationManager.PREVENT_SCREEN_CAPTURE = isScreenRecordingAllowed;

            boolean isBlockRootedDeviceSupport = Utility.getJsonObjectBooleanValue(new JSONObject(fileContents),
                    "isBlockRootedDeviceSupport");
            IntegrationManager.BLOCK_ROOTED_DEVICE_SUPPORT = isBlockRootedDeviceSupport;

            boolean disableNativeBackButton = Utility.getJsonObjectBooleanValue(new JSONObject(fileContents),
                    "disableNativeBackButton");

            IntegrationManager.DISABLE_NATIVE_BACK_BUTTON = disableNativeBackButton;

        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public static String getRedirectedUrl(String filename, String queryMode, boolean isExistsCheck) {

        MybeepsPref mybeepsPref = new MybeepsPref(App.getInstance().getApplicationContext());
        //skip passkey screen
        if (!TextUtils.isEmpty(mybeepsPref.getValue("passKey"))) {
            filename = TextUtils.isEmpty(mybeepsPref.getValue("pageName"))
                    ? AppConstant.FileName.DEFAULT_FILE_NAME : mybeepsPref.getValue("pageName");
            queryMode = TextUtils.isEmpty(mybeepsPref.getValue("queryMode"))
                    ? AppConstant.FileName.DEFAULT_QUERY_MODE : mybeepsPref.getValue("queryMode");
        }

        File sandboxFile = new File(Utility.getHtmlDirFromSandbox() + File.separator + filename);

        queryMode = "queryMode=" + (TextUtils.isEmpty(queryMode) ? AppConstant.FileName.DEFAULT_QUERY_MODE : queryMode);

        if (isExistsCheck) {
            return Uri.fromFile(sandboxFile).toString() + "?" + queryMode;
        }

        if (sandboxFile.exists() && !TextUtils.isEmpty(queryMode)) {
            return Uri.fromFile(sandboxFile).toString() + "?" + queryMode;
        }

        return "";
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

    /**
     * Check string is null or empty by trim
     *
     * @param string
     * @return status
     */
    public static boolean isEmpty(String string) {
        if (string != null) {
            string = string.trim();
        }
        return TextUtils.isEmpty(string);
    }

    public static boolean isLoadFromLocalHtmlDir(String url) {

        boolean result = false;
        try {
            result = url.contains("file:///data") && url.contains("files/WebHtml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static String getResString(int resId) {
        return App.getInstance().getApplicationContext().getResources().getString(resId);
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



    public static void cancelNotification(Context context) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }



    /**
     * check and return if external cache dir is status
     *
     * @return true, if external cache is available
     * false, Otherwise
     */
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
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

    public static byte[] getBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
