package com.hokuapps.loadnativefileupload.utilities;

import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.APP_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.CALLBACK_FUNCTION;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.CAN_CROP_IMAGE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.CAPTION;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.COLOR;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.COLOR_CODE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.DRAW_TYPE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.EXTENSION;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IMAGE_URL;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.INSTRUCTION_TEXT;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_AUDIO_RECORDING;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_BASE_64_DATA;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_CANCEL;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_DEFAULT_CAMERA;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_DOCUMENTS_ONLY;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_DOCUMENTS_UPLOAD;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_DRAWING;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_MAP_PLAN;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_PROFILE_IMAGE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_PROFILE_UPLOAD_START;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_RECTANGLE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_SCAN_DOCUMENT;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_SCAN_TEXT;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_SELECT_VIDEO;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_WAIT_FOR_RESPONSE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.LANGUAGE_PREF;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.LOAD_PHOTO_EDITOR;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.LOCAL_IMAGE_NAME;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MAX_FILE_SIZE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.OFFLINE_DATA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.ORIGINAL_IMAGE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.PAGE_TITLE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.SHOW_CAPTION;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.SKIP_CAMERA;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.SKIP_LIBRARY;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.SRC;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.SUPPORTED_FORMAT;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.TYPE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.USED_FOR_ANNOTATION;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.hokuapps.loadnativefileupload.backgroundtask.FileUploader;
import com.hokuapps.loadnativefileupload.constants.FileUploadConstant;
import com.hokuapps.loadnativefileupload.dao.AppMediaDetailsDAO;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
import com.hokuapps.loadnativefileupload.models.JSResponseData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;


public class FileUploadUtility {


    /**
     * check if the string is empty
     *
     * @param string value for check
     * @return true if string value is empty other wise false
     */
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
     * @return return parse color in int format
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
     * returns current date and time in milli seconds
     *
     * @return return time in long
     */
    public static long getCurrentDateTimeInMS() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime().getTime();
    }

    /**
     * return json object value
     *
     * @param obj jsonObject response
     * @param fieldName key for retrieve JSONObject value
     * @return JSONObject value for filedName
     * @throws JSONException if jsonObject is invalid
     */
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
     * returns boolean value from the json object
     *
     * @param obj jsonObject response
     * @param fieldName key for retrieve boolean value
     * @return boolean value for filedName
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
     * returns string value from the json object
     *
     * @param obj jsonObject response
     * @param fieldName key for retrieve String value
     * @return String value for filedName
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
     * returns integer value from the json object
     *
     * @param obj jsonObject response
     * @param fieldName key for retrieve int value
     * @return int value for filedName
     * @throws JSONException if jsonObject is invalid
     */
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

    /**
     * calls function
     *
     * @param activity context of activity
     * @param webView reference of webView
     * @param callingJavaScriptFn function name of java script
     * @param response jsonObject for javascript function
     */
    public static void callJavaScriptFunction(Activity activity, final WebView webView, final String callingJavaScriptFn, final JSONObject response) {

        if (activity == null) return;
        activity.runOnUiThread(() -> {
            try {
                webView.evaluateJavascript(String.format("javascript:" + callingJavaScriptFn + "(%s)", response), null);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
    }

    /**
     * @param mContext context
     * @return return sandbox path of file
     */
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
     * @param mediaStorageDir file directory with associate path
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
     * Move file to new file
     *
     * @param file for move to new location
     * @param newFile new location for file move
     * @return status of file transfer
     * @throws IOException when error in file transfer
     */
    public static boolean moveFile(File file, File newFile) throws IOException {

        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        boolean isMoved;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            isMoved = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            isMoved = false;
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

        return isMoved;

    }

    /**
     * copy file to internal storage
     *
     * @param uri file uri to copy
     * @param newDirName directory to store file
     * @return path after file is save to directory
     */
    public static String copyFileToInternalStorage(Uri uri, String newDirName, Context mContext) {

        Cursor returnCursor = mContext.getContentResolver().query(uri, new String[]{
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
        }, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));

        File output;
        if (!newDirName.equals("")) {
            File dir = new File(mContext.getFilesDir() + "/" + newDirName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            output = new File(mContext.getFilesDir() + "/" + newDirName + "/" + name);
        } else {
            output = new File(mContext.getFilesDir() + "/" + name);
        }
        try {
            InputStream inputStream = mContext.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(output);
            int read = 0;
            int bufferSize = 1024;
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

            inputStream.close();
            outputStream.close();
            returnCursor.close();
        } catch (Exception e) {
            returnCursor.close();
            Log.e("Exception", Objects.requireNonNull(e.getLocalizedMessage()));
        }

        return output.getPath();
    }

    /**
     * save the media details to local database
     *
     * @param file contain detail related file
     * @param offlineID save file against offlineID
     * @param instructionNumberClockIn count for number of save against offlineID
     * @param srcFileName add file name if available
     * @param imageType type of image
     * @param caption add additional text with image
     * @return all detail about image in local database
     */
    public static AppMediaDetails saveAppMediaDetails(File file, String offlineID, int instructionNumberClockIn,
                                                      String srcFileName, int imageType, String caption, Context mContext) {
        AppMediaDetails appMediaDetails = null;
        try {

            AppMediaDetailsDAO appMediaDetailsDAO = new AppMediaDetailsDAO(mContext);
            appMediaDetails = appMediaDetailsDAO.getStoredAppMediaDetails(mContext,
                    file.getName());

            if (appMediaDetails == null) {
                appMediaDetails = new AppMediaDetails();

                appMediaDetails.setInstructionNumber(instructionNumberClockIn);

            } else {
                stopUploadingAndDeletedOldImageFile(appMediaDetails, mContext);
            }
            appMediaDetails.setOfflineDataID(offlineID);
            appMediaDetails.setFileName(file.getName());
            appMediaDetails.setImageType(imageType);
            appMediaDetails.setFileSizeBytes(String.valueOf(file.length()));
            appMediaDetails.setInstructionNumber(appMediaDetails.getInstructionNumber());
            appMediaDetails.setUploadStatus(AppMediaDetails.UPLOAD_IN_PROGRESS);
            appMediaDetails.setImageCaption(caption);
            appMediaDetails.save(mContext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return appMediaDetails;
    }

    /**
     * stop uploading previous file if file is replaced and delete from location
     *
     * @param appMediaDetails - this object contains information of file
     */
    public static void stopUploadingAndDeletedOldImageFile(AppMediaDetails appMediaDetails, Context mContext) {
        try {
            FileUploader roofingUploader = FileUploader.getInstance(appMediaDetails, mContext);
            File fileOldDelete = new File(FileUtility.getHtmlDirFromSandbox(mContext) + File.separator + appMediaDetails.getFileName());
            fileOldDelete.delete();
            roofingUploader.stopUpload();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Create image file name by app id and current system time
     *
     * @param extension extension for image file
     * @return create image name with app ID and add extension
     */
    public static String generateImageFileNameByAppID(String extension, String appId) {
        try {
            if (!TextUtils.isEmpty(appId)) {
                return appId + "_" + getCurrentDateTimeInMS() + extension;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "" + System.currentTimeMillis() + extension;
    }

    /**
     * checks if any file is remained to upload
     *
     * @return remaining file upload status
     */
    public static boolean isAnyFileRenamingToUploadV2(Context mContext, String offlineId) {
        ArrayList<AppMediaDetails> appMediaDetailsArrayList = AppMediaDetailsDAO.getAppMediaDetailsListByOfflineIDWithUploadedFile(mContext,
                offlineId, false);
        for (AppMediaDetails appMediaDetails : appMediaDetailsArrayList) {
            try {
                while (!appMediaDetails.isUploadStatus()) {
                    AppMediaDetails amd = AppMediaDetailsDAO.getAppMediaDetailsListByOfflineIDWithUploadedFile(mContext,
                            offlineId, !appMediaDetails.isUploadStatus(), appMediaDetails.getFileName());
                    if (amd != null) {
                        appMediaDetails = amd;
                    }
                    Thread.sleep(2000);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return true;
    }

    /**
     * cancel javascript call
     */
    public static void cancelJavaScriptCall(Activity mActivity, WebView mWebView, String callBackFunction) {
        try {
            //call java script if loader is shown
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(IS_CANCEL, true);
            FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, callBackFunction, jsonObject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Parse the response data and put it in json object
     *
     * @param responseData parsing json object to JSResponseData
     */
    public static JSResponseData parseLoadNativeFileUploadJsResponseData(String responseData) {

        JSResponseData jsResponseDataModel = new JSResponseData();
        try {
            JSONObject responseJsonObj = new JSONObject(responseData);

            jsResponseDataModel.setLanguagePref((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, LANGUAGE_PREF));
            jsResponseDataModel.setType((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, TYPE));
            jsResponseDataModel.setColor((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, COLOR));
            jsResponseDataModel.setDrawType(FileUploadUtility.getJsonObjectIntValue(responseJsonObj, DRAW_TYPE));
            jsResponseDataModel.setMaxFileSize(FileUploadUtility.getJsonObjectIntValue(responseJsonObj, MAX_FILE_SIZE));
            jsResponseDataModel.setAppID((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, APP_ID));
            jsResponseDataModel.setCallbackFunction((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, CALLBACK_FUNCTION));
            jsResponseDataModel.setSrcImageName((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, SRC));
            jsResponseDataModel.setInstructionText((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, INSTRUCTION_TEXT));
            jsResponseDataModel.setSkipCamera(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, SKIP_CAMERA));
            jsResponseDataModel.setSelectVideo(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_SELECT_VIDEO));
            jsResponseDataModel.setSkipLibrary(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, SKIP_LIBRARY));
            jsResponseDataModel.setLoadPhotoEditor(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, LOAD_PHOTO_EDITOR));
            jsResponseDataModel.setDrawing(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_DRAWING));
            jsResponseDataModel.setProfileImage(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_PROFILE_IMAGE));
            jsResponseDataModel.setProfileUploadStart(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_PROFILE_UPLOAD_START));
            jsResponseDataModel.setColorCode(responseJsonObj.has(COLOR_CODE) ? responseJsonObj.getString(COLOR_CODE) : "#448aff");
            jsResponseDataModel.setMapPlan(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_MAP_PLAN));
            jsResponseDataModel.setCropped(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, CAN_CROP_IMAGE));
            jsResponseDataModel.setPageTitle((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, PAGE_TITLE));
            jsResponseDataModel.setExtension((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, EXTENSION));
            jsResponseDataModel.setShowCaption(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, SHOW_CAPTION));
            jsResponseDataModel.setCaption(FileUploadUtility.getStringObjectValue(responseJsonObj, CAPTION));
            String offlineDataID = FileUploadUtility.getStringObjectValue(responseJsonObj, OFFLINE_DATA_ID);
            jsResponseDataModel.setWaitForResponse(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_WAIT_FOR_RESPONSE));
            jsResponseDataModel.setDefaultCamera(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_DEFAULT_CAMERA));
            jsResponseDataModel.setDocumentsOnly(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_DOCUMENTS_ONLY));
            jsResponseDataModel.setSupportedFormat((String[]) FileUploadUtility.getJsonObjectValue(responseJsonObj, SUPPORTED_FORMAT));
            jsResponseDataModel.setDocumentsUpload(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_DOCUMENTS_UPLOAD));
            jsResponseDataModel.setBase64Data(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_BASE_64_DATA));
            jsResponseDataModel.setAudioRecording(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_AUDIO_RECORDING));
            jsResponseDataModel.setRectangle(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_RECTANGLE));
            jsResponseDataModel.setScanText(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_SCAN_TEXT));
            jsResponseDataModel.setScanDocument(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, IS_SCAN_DOCUMENT));
            jsResponseDataModel.setImageURL(FileUploadUtility.getStringObjectValue(responseJsonObj, IMAGE_URL));
            jsResponseDataModel.setOriginalImagePath(FileUploadUtility.getStringObjectValue(responseJsonObj, ORIGINAL_IMAGE_PATH));
            jsResponseDataModel.setUsedForAnnotation(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, USED_FOR_ANNOTATION));
            jsResponseDataModel.setLocalImageName(FileUploadUtility.getStringObjectValue(responseJsonObj, LOCAL_IMAGE_NAME));
            jsResponseDataModel.setOfflineID(offlineDataID);
            jsResponseDataModel.setResponseData(responseData);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsResponseDataModel;
    }

    public static String getOption(JSResponseData jsResponseData) {
        if (jsResponseData.isScanText()) {
            return FileUploadConstant.options.IS_SCAN_TEXT;
        }

        if (jsResponseData.isDefaultCamera()) {

            return FileUploadConstant.options.IS_DEFAULT_CAMERA;
        }

        if (jsResponseData.getDrawType() == 5) {
            return FileUploadConstant.options.IS_FREE_DRAW;
        }
        if (jsResponseData.isUsedForAnnotation()) {
            return FileUploadConstant.options.IS_ANNOTATION;


        }
        if (jsResponseData.getOriginalImagePath() != null) {
            return FileUploadConstant.options.IS_ANNOTATION_WITH_IMAGE_PATH;


        }
        if (jsResponseData.getLocalImageName() != null) {

            return FileUploadConstant.options.IS_ANNOTATION_WITH_LOCAL_IMAGE;

        }
        if (jsResponseData.getImageURL() != null) {

            return FileUploadConstant.options.IS_ANNOTATION_WITH_IMAGE_URL;

        } else {
            return "Default";
        }

    }
}
