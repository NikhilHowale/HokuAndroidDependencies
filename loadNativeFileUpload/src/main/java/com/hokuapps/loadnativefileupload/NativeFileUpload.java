package com.hokuapps.loadnativefileupload;

import static com.hokuapps.loadnativefileupload.utilities.FileUploadUtility.getCurrentDateTimeInMS;
import static com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration.REQUEST_CROP_IMAGE;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.loadnativefileupload.annotate.AnnotateActivity;
import com.hokuapps.loadnativefileupload.annotate.FreeDrwaingActivity;
import com.hokuapps.loadnativefileupload.backgroundtask.ImageCompression;

import com.hokuapps.loadnativefileupload.backgroundtask.RoofingUploader;

import com.hokuapps.loadnativefileupload.constants.FileUploadConstant;
import com.hokuapps.loadnativefileupload.dao.AppMediaDetailsDAO;
import com.hokuapps.loadnativefileupload.delegate.IUICallBack;
import com.hokuapps.loadnativefileupload.imageEditor.IPRectangleAnnotationActivity;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
import com.hokuapps.loadnativefileupload.models.AuthenticatedUser;
import com.hokuapps.loadnativefileupload.models.JSResponseData;
import com.hokuapps.loadnativefileupload.restrequest.ServiceRequest;
import com.hokuapps.loadnativefileupload.scantext.ScanTextUtility;
import com.hokuapps.loadnativefileupload.utilities.CameraManager;
import com.hokuapps.loadnativefileupload.utilities.CustomCameraManager;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;
import com.hokuapps.loadnativefileupload.utilities.FileUtility;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.preview.PreviewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class NativeFileUpload {

    public WebView mWebView;
    public static Activity mActivity;
    public static Context mContext;
    private LruCache<String, Bitmap> mContactImageMemoryCache;

    private String[] requiredJSONObjectKey = {};
    private JSResponseData jsResponseData;
    private String appAuthToken = "";
    public String offlineID = null;
    private int instucationNumberClockIn = 0;
    public static final int ACTION_REQUEST_EDITIMAGE = 9006;
    public static final int ACTION_REQUEST_EDITIMAGE_MAP_PLAN = 9008;
    public static final int SELECT_GALLERY_IMAGE_CODE_WITHOUT_EDITOR = 7002;

    public static final int REQUEST_FILE_BROWSER = 9012;
    private String caption;
    private String profileImagePath = null;
    private Uri mImageCaptureUri = null;
    private boolean isGetAllFileStatusCalled = false;
    private String fileStatusCallBackFunction = "";
    private String syncOfflineNextButtonCallBack = "";
    private String objParams = "";

    private String authorizationToken;

    private static NativeFileUpload Instance;

    public static String APP_FILE_URL = "";
    public static String AUTHORITY = "com.pearlista.database";

    public NativeFileUpload() {

    }


    public static NativeFileUpload getInstance() {
        if (Instance == null) {
            Instance = new NativeFileUpload();
        }
        return Instance;
    }

    public void initialization(WebView mWebView, Activity mActivity, Context mContext,
                               String authorizationToken, String appAuthToken, String uploadUrl, String authority) {
        this.mWebView = mWebView;
        this.mActivity = mActivity;
        this.mContext = mContext;
        this.authorizationToken = authorizationToken;
        this.appAuthToken = appAuthToken;
        this.APP_FILE_URL = uploadUrl;
        this.AUTHORITY = authority;


    }

    public void loadNativeFileUpload(final String responseData) throws JSONException {

        parseLoadNativeFileUploadJsResponseData(responseData);
        if (jsResponseData == null) return;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(responseData);
            if (FileUploadUtility.getJsonObjectBooleanValue(jsonObject, "isScanText")) {
                parseScanTextResponse(jsonObject);
                return;
            }
            if (FileUploadUtility.getJsonObjectBooleanValue(jsonObject, "isScanDocument")) {
                parseScanDocumentResponse(jsonObject);
                return;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsResponseData.isDefaultCamera()) {
            launchNativeFileUploadByOptions(jsResponseData);
            return;
        }

        if (jsResponseData.isSkipCamera()) {
            if (jsResponseData.isLoadPhotoEditor()) {
                CustomCameraManager.launchCustomImageGallery(mActivity, null, jsResponseData.getColorCode(),
                        SelectPictureActivity.SELECT_GALLERY_IMAGE_CODE);
            } else {
                CustomCameraManager.launchCustomImageGallery(mActivity, null, jsResponseData.getColorCode(),
                        SELECT_GALLERY_IMAGE_CODE_WITHOUT_EDITOR);
            }
        } else {
            if (jsResponseData.getDrawtype() == 5) {
                File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                        "draw_" + System.currentTimeMillis() + ".png");
                String filePath = null;
                try {
                    filePath = FileUploadUtility.getStringObjectValue(new JSONObject(responseData), "imageURL");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                String colorCode = jsResponseData != null ? jsResponseData.getColorCode() : null;
                if (filePath != null && !filePath.isEmpty()) {
                    FreeDrwaingActivity.start(mActivity, jsResponseData.getResponseData(), filePath, outputFile.getAbsolutePath(),
                            colorCode, jsResponseData.getPageTitle(), ACTION_REQUEST_EDITIMAGE, 5);
                } else {
                    FreeDrwaingActivity.start(mActivity, jsResponseData.getResponseData(), "Drwaing Board", outputFile.getAbsolutePath(),
                            colorCode, jsResponseData.getPageTitle(), ACTION_REQUEST_EDITIMAGE, 5);
                }

                return;
            } else if (new JSONObject(responseData).has("usedForAnnotation") &&
                    new JSONObject(responseData).has("annotateData")) {
                File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                        "draw_" + System.currentTimeMillis() + ".png");
                String colorCode = jsResponseData != null ? jsResponseData.getColorCode() : null;

                String filePath = FileUploadUtility.getStringObjectValue(new JSONObject(responseData), "originalImagePath");
                boolean usedForAnnotation = FileUploadUtility.getJsonObjectBooleanValue(new JSONObject(responseData), "usedForAnnotation");
                JSONArray annotateData = new JSONObject(responseData).getJSONArray("annotateData");
                IPRectangleAnnotationActivity.start(mActivity, filePath,
                        outputFile.getAbsolutePath(), colorCode, usedForAnnotation, annotateData.toString());

            } else if (new JSONObject(responseData).has("originalImagePath")) {
                File outputFile = new File(FileUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                        "draw_" + System.currentTimeMillis() + ".png");
                String filePath = FileUploadUtility.getStringObjectValue(new JSONObject(responseData), "originalImagePath");
                String colorCode = jsResponseData != null ? jsResponseData.getColorCode() : null;
                int drawType = jsResponseData.getDrawtype();
                String type = null;
                if (drawType == 0) {
                    type = "Line";
                } else if (drawType == 1) {
                    type = "Circle";
                } else if (drawType == 2) {
                    type = "Rectangle";
                } else if (drawType == 3) {
                    type = "Path";
                } else if (drawType == 4) {
                    AnnotateActivity.start(mActivity, jsResponseData.getResponseData(), filePath, outputFile.getAbsolutePath(),
                            colorCode, jsResponseData.getPageTitle(),
                            jsResponseData.isMapPlan() ? ACTION_REQUEST_EDITIMAGE_MAP_PLAN : ACTION_REQUEST_EDITIMAGE, drawType);
                    return;
                }
                IPRectangleAnnotationActivity.start(mActivity, filePath, outputFile.getAbsolutePath(),
                        colorCode, jsResponseData.getPageTitle(),
                        jsResponseData.isMapPlan() ? ACTION_REQUEST_EDITIMAGE_MAP_PLAN : ACTION_REQUEST_EDITIMAGE
                        , type, jsResponseData.getColor());
            } else if (new JSONObject(responseData).has("localImageName")) {
                File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                        "draw_" + System.currentTimeMillis() + ".png");
                String filePath = FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator + FileUploadUtility.getStringObjectValue(new JSONObject(responseData), "localImageName");
                String colorCode = jsResponseData != null ? jsResponseData.getColorCode() : null;
                int drawType = jsResponseData.getDrawtype();
                String type = null;
                if (drawType == 0) {
                    type = "Line";
                } else if (drawType == 1) {
                    type = "Circle";
                } else if (drawType == 2) {
                    type = "Rectangle";
                } else if (drawType == 3) {
                    type = "Path";
                } else if (drawType == 4) {
                    AnnotateActivity.start(mActivity, jsResponseData.getResponseData(), filePath, outputFile.getAbsolutePath(),
                            colorCode, jsResponseData.getPageTitle(),
                            jsResponseData.isMapPlan() ? ACTION_REQUEST_EDITIMAGE_MAP_PLAN : ACTION_REQUEST_EDITIMAGE, drawType);
                    return;
                }
                IPRectangleAnnotationActivity.start(mActivity, filePath, outputFile.getAbsolutePath(),
                        colorCode, jsResponseData.getPageTitle(),
                        jsResponseData.isMapPlan() ? ACTION_REQUEST_EDITIMAGE_MAP_PLAN : ACTION_REQUEST_EDITIMAGE
                        , type, jsResponseData.getColor());
            } else if (new JSONObject(responseData).has("imageURL")) {
                File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                        "draw_" + System.currentTimeMillis() + ".png");
                String filePath = FileUploadUtility.getStringObjectValue(new JSONObject(responseData), "imageURL");
                String colorCode = jsResponseData != null ? jsResponseData.getColorCode() : null;
                int drawType = jsResponseData.getDrawtype();
                String type = null;
                if (drawType == 0) {
                    type = "Line";
                } else if (drawType == 1) {
                    type = "Circle";
                } else if (drawType == 2) {
                    type = "Rectangle";
                } else if (drawType == 3) {
                    type = "Path";
                } else if (drawType == 4) {
                    AnnotateActivity.start(mActivity, jsResponseData.getResponseData(), filePath, outputFile.getAbsolutePath(),
                            colorCode, jsResponseData.getPageTitle(),
                            jsResponseData.isMapPlan() ? ACTION_REQUEST_EDITIMAGE_MAP_PLAN : ACTION_REQUEST_EDITIMAGE, drawType);
                    return;
                }
                IPRectangleAnnotationActivity.start(mActivity, filePath, outputFile.getAbsolutePath(),
                        colorCode, jsResponseData.getPageTitle(),
                        jsResponseData.isMapPlan() ? ACTION_REQUEST_EDITIMAGE_MAP_PLAN : ACTION_REQUEST_EDITIMAGE
                        , type, jsResponseData.getColor());
            }else {
                CustomCameraManager.launchCameraFromActivity(mActivity, !jsResponseData.isSkipLibrary(),
                        jsResponseData.getInstructionText(), false, false,
                        jsResponseData.isCroped(),
                        false, jsResponseData.isRectangle(),
                        jsResponseData.getResponseData());
            }
        }
    }


    private void parseLoadNativeFileUploadJsResponseData(String responseData) {
        try {
            JSONObject responseJsonObj = new JSONObject(responseData);
            JSResponseData jsResponseDataModel = new JSResponseData();

            jsResponseDataModel.setLanguagePref((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "languagePref"));
            jsResponseDataModel.setType((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "type"));
            jsResponseDataModel.setColor((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "color"));

            jsResponseDataModel.setDrawtype(FileUploadUtility.getJsonObjectIntValue(responseJsonObj, "drawType"));
            jsResponseDataModel.setMaxFileSize(FileUploadUtility.getJsonObjectIntValue(responseJsonObj, "maxFileSize"));


            jsResponseDataModel.setAppID((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "appID"));
            jsResponseDataModel.setCallbackfunction((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "callbackFunction"));
            jsResponseDataModel.setSrcImageName((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "src"));
            jsResponseDataModel.setInstructionText((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "instructionText"));

            jsResponseDataModel.setSkipCamera(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "skipCamera"));
            jsResponseDataModel.setSelectVideo(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isSelectVideo"));
            jsResponseDataModel.setSkipLibrary(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "skipLibrary"));

            jsResponseDataModel.setLoadPhotoEditor(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "LoadPhotoEditor"));
            jsResponseDataModel.setDrawing(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isDrawing"));

            jsResponseDataModel.setProfileImage(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isProfileImage"));
            jsResponseDataModel.setProfileUploadStart(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isProfileUploadStart"));
            jsResponseDataModel.setColorCode(responseJsonObj.has("colorCode") ? responseJsonObj.getString("colorCode") : "#448aff");
            jsResponseDataModel.setMapPlan(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isMapPlan"));
            jsResponseDataModel.setCroped(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "canCropImage"));
            jsResponseDataModel.setPageTitle((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "pageTitle"));
            jsResponseDataModel.setExtention((String) FileUploadUtility.getJsonObjectValue(responseJsonObj, "extention"));
            jsResponseDataModel.setShowCaption(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "showCaption"));
            jsResponseDataModel.setCaption(FileUploadUtility.getStringObjectValue(responseJsonObj, "caption"));

            String offlineDataID = FileUploadUtility.getStringObjectValue(responseJsonObj, "offlineDataID");

            jsResponseDataModel.setWaitForResponse(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isWaitForResponse"));
            jsResponseDataModel.setDefaultCamera(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isDefaultCamera"));
            jsResponseDataModel.setDocumentsOnly(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isDocumentsOnly"));
            jsResponseDataModel.setSupportedFormat((String[]) FileUploadUtility.getJsonObjectValue(responseJsonObj, "supportedFormat"));
            jsResponseDataModel.setDocumentsUpload(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isDocumentsUpload"));
            jsResponseDataModel.setBase64Data(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isBase64Data"));
            jsResponseDataModel.setAudioRecording(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isAudioRecording"));
            jsResponseDataModel.setRectangle(FileUploadUtility.getJsonObjectBooleanValue(responseJsonObj, "isRectangle"));

            String step = FileUploadUtility.getStringObjectValue(responseJsonObj, "step");

            if (!TextUtils.isEmpty(offlineDataID)) {
                offlineID = offlineDataID;
            }

            if (!TextUtils.isEmpty(step) && !step.equalsIgnoreCase("null")) {
                instucationNumberClockIn = Integer.parseInt(step);
            }

            jsResponseDataModel.setOfflineID(offlineID);

            jsResponseDataModel.setResponseData(responseData);

            setJsResponseData(jsResponseDataModel);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void parseScanDocumentResponse(JSONObject jsonObject) {
//        startScan();
    }


    private void parseScanTextResponse(final JSONObject jsonObject) {
        mActivity.runOnUiThread(() -> {

            PermissionListener permissionlistener = new PermissionListener() {

                @Override
                public void onPermissionGranted() {
                    ScanTextUtility.getInstance(mActivity)
                            .setImageScanListener((string, jsonArrayScannedText) -> {
                                System.out.println("IMAGE SCANNED :" + string);
                                String nextButtonCallback = FileUploadUtility.getStringObjectValue(jsonObject, "nextButtonCallback");
                                JSONObject jsonObjectData = new JSONObject();
                                try {

                                    jsonObject.put("scannedText", jsonArrayScannedText);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, nextButtonCallback, jsonObject);
                            }).showPicker();
                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {
                    Toast.makeText(mActivity, "Permission required to select image.", Toast.LENGTH_SHORT).show();

                }
            };

            TedPermission.create()
                    .setPermissionListener(permissionlistener)
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .check();
        });
    }

    public JSResponseData getJsResponseData() {
        return jsResponseData != null ? jsResponseData : new JSResponseData();
    }


    private void handleMultipleImagesHere(List<Image> images, Uri mImageCaptureUri, Context context) {

//        if (images != null && images.size() > 0) {
//            String filePath = "";
//            for (int index = 0; index < images.size(); index++) {
//                filePath = images.get(index).getPath();
//                if (filePath != null) {
//                    mImageCaptureUri = Uri.fromFile(new File(filePath));
//
//                    saveImageToSyncHtmlFilesDir(mImageCaptureUri, context);
//
//                } else {
//                    Toast.makeText(mContext, "error while capture photo.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
    }

    /**
     * Create image file name by app id and current system time
     *
     * @param extension
     * @return
     */
    private String generateImageFileNameByAppID(String extension) {
        try {
            if (!TextUtils.isEmpty(getJsResponseData().getAppID())) {
                return getJsResponseData().getAppID() + "_" + getCurrentDateTimeInMS() + extension;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "" + System.currentTimeMillis() + extension;
    }


    public void handleImageResultIntent(Intent intent) {

        String filePath = intent.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
        caption = intent.getStringExtra(CameraConfiguration.Arguments.CAPTION);
        mImageCaptureUri = Uri.fromFile(new File(filePath));

        saveImageToSyncHtmlFilesDir(mImageCaptureUri, mContext);

    }


    private void setNativeSelectedPhotoCallbackFunction(String filename, String offlineID) {
        setNativeSelectedPhotoCallbackFunction(filename, offlineID, getJsResponseData().getCallbackfunction());
    }

    private void setNativeSelectedPhotoCallbackFunction(String filename, String offlineID, String callbackName) {
        JSONObject responseJsonObj = setFileNameAndOfflineIDToResponseData(filename, offlineID);
        try {
            String catureFilePath = FileUtility.getPath(mContext, mImageCaptureUri);
            responseJsonObj.put("imagePath", catureFilePath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(String.format("javascript:" + callbackName + "(%s)", responseJsonObj.toString()), null);
        } else {
            mWebView.loadUrl(String.format("javascript:" + callbackName + "(%s)", responseJsonObj));
        }
    }

    private void updateAuthUserProfileStatus(boolean isSuccess) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("isSuccess", isSuccess);
            FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, "uploadProfileStatus", jsonObject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key == null) return;
        if (getBitmapFromMemCache(key) == null) {
            mContactImageMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        if (key == null) return null;
        return mContactImageMemoryCache.get(key);
    }

    private void holdProfileImage(String sourceFilePath) {
        try {
            JSONObject jsonObject = new JSONObject(jsResponseData.getResponseData());
            if (FileUploadUtility.getJsonObjectBooleanValue(jsonObject, "editProfile")) {
                profileImagePath = sourceFilePath;

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean moveFile(File file, File newFile) throws IOException {

        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        boolean isMoved = false;
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

        //return moveFileV2(file, newFile);
    }

    private void setNativeBase64DataResponse(String filePath, String filename, String offlineID, String base64Data) {
        try {
            JSONObject responseJsonObj = setFileNameAndOfflineIDToResponseData(filename, offlineID);
            responseJsonObj.put("imageData", base64Data);
            responseJsonObj.put("filePath", filePath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(String.format("javascript:" + getJsResponseData().getCallbackfunction() + "(%s)", responseJsonObj), null);
            } else {
                mWebView.loadUrl(String.format("javascript:" + getJsResponseData().getCallbackfunction() + "(%s)", responseJsonObj));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * set file name and offline id to resposne data
     *
     * @param filename
     * @param offlineID
     * @return
     */
    private JSONObject setFileNameAndOfflineIDToResponseData(String filename, String offlineID) {
        try {
            JSONObject jsonObject = new JSONObject(getJsResponseData().getResponseData());
            jsonObject.put("fileName", filename);
            jsonObject.put("offlineDataID", offlineID);
            jsonObject.putOpt("caption", caption);
            return jsonObject;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject();
    }

    /**
     * Start image upload to server and save to lacal db with its information
     *
     * @param filePath
     * @param offlineID
     */
    private void startImageUpload(final String filePath, String offlineID) {
        startImageUpload(filePath, offlineID, getJsResponseData().getAppID(), getJsResponseData().getSrcImageName(), AppMediaDetails.INSTRUCTION_IMAGE_TYPE);
    }

    private void startImageUpload(final String filePath, String offlineID, String appID, String srcName, int imageType) {
        File file = new File(filePath/*Utility.getHtmlDirFromSandbox() + File.separator + fileName*/);
        String fileName = FileUtility.getFileNameWithoutExists(filePath);
        //saveAppOfflineData(offlineID, appID); //  no need
        final AppMediaDetails appMediaDetails = saveAppMediaDetails(file, offlineID, instucationNumberClockIn,
                TextUtils.isEmpty(srcName) ? fileName : srcName, imageType, caption);

        instucationNumberClockIn = appMediaDetails.getInstructionNumber();

        if (appMediaDetails == null) {
            return;
        }
        RoofingUploader roofingUploader = RoofingUploader.getInstance(appMediaDetails, mContext);
        roofingUploader.setFilePath(file.getPath());
        roofingUploader.setAppID(appID);
        roofingUploader.setAppsServerToken(appAuthToken);
        roofingUploader.setAuthToken(authorizationToken);
        roofingUploader.setUiCallBack(new RoofingUploader.IUICallBackRoofing() {
            @Override
            public void onSuccess(final ServiceRequest serviceRequest) {
                System.out.println("UPLOAD FINISHED:" + Calendar.getInstance().getTime());
                try {
                    if (serviceRequest != null && serviceRequest.getAppMediaDetails() != null) {
                        updateFileStatus(serviceRequest.getAppMediaDetails().getOfflineDataID());

                        if (jsResponseData != null && jsResponseData.isWaitForResponse()) {
                            setCallbackFunction(serviceRequest.getAppMediaDetails(), jsResponseData.getCallbackfunction());
                        }

                        if (!TextUtils.isEmpty(profileImagePath) && jsResponseData.isProfileImage()) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setCallbackFunction(serviceRequest.getAppMediaDetails(), jsResponseData.getCallbackfunction());
                                    profileImagePath = "";
                                }
                            });

                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

            @Override
            public void onFailure(ServiceRequest serviceRequest) {
                if (serviceRequest != null && serviceRequest.getAppMediaDetails() != null) {
                    updateFileStatus(serviceRequest.getAppMediaDetails().getOfflineDataID());
                }
            }
        });

        roofingUploader.startUpload();
        System.out.println("UPLOAD STARTED:" + Calendar.getInstance().getTime());

    }

    private void updateFileStatus(String offlineDataID) {
        if (isGetAllFileStatusCalled && !TextUtils.isEmpty(fileStatusCallBackFunction)) {
            JSONObject jsonObjectFileStatus = getAllFileStatusList(offlineDataID);

            FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, fileStatusCallBackFunction, jsonObjectFileStatus);
        } else if (!TextUtils.isEmpty(syncOfflineNextButtonCallBack)) {
            JSONObject jsonObjectFileStatus = getAllFileStatusList(offlineDataID);
            if (objParams != null && !objParams.isEmpty()) {
                try {
                    jsonObjectFileStatus.put("objParams", objParams);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, syncOfflineNextButtonCallBack, jsonObjectFileStatus);
        }
    }

    private JSONObject getAllFileStatusList(String offlineDataID) {

        try {
            JSONObject jsonObjectResponse = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            ArrayList<AppMediaDetails> appMediaDetailsArrayList = AppMediaDetailsDAO.getAppMediaDetailsListByOfflineID(
                    mContext, offlineDataID);
            String mapFileMediaID = "";
            String mapFileName = "";
            int mapFileStatus = 0;
            String caption = "";
            String mapPlanFileMediaID = "";
            String mapPlanFileName = "";
            String mapPlanS3FilePath = "";
            int mapPlanStatus = 0;

            for (AppMediaDetails appMediaDetails : appMediaDetailsArrayList) {
                if (appMediaDetails.getImageType() == AppMediaDetails.MAP_PLAN_IMAGE_TYPE) {
                    mapPlanFileMediaID = appMediaDetails.getMediaID();
                    mapPlanFileName = appMediaDetails.getFileName();
                    mapPlanS3FilePath = appMediaDetails.getS3FilePath();
                    mapPlanStatus = appMediaDetails.getUploadStatus();
                } else if (appMediaDetails.getImageType() == AppMediaDetails.MAP_IMAGE_TYPE) {
                    mapFileMediaID = appMediaDetails.getMediaID();
                    mapFileName = appMediaDetails.getFileName();
                    mapFileStatus = appMediaDetails.getUploadStatus();
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("step", appMediaDetails.getInstructionNumber());
                    jsonObject.put("fileNm", appMediaDetails.getFileName());
                    jsonObject.put("mediaID", appMediaDetails.getMediaID());
                    jsonObject.put("S3FilePath", appMediaDetails.getS3FilePath());
                    jsonObject.put("status", appMediaDetails.getUploadStatus());
                    jsonObject.putOpt("caption", appMediaDetails.getImageCaption());
                    jsonArray.put(jsonObject);
                }
                caption = appMediaDetails.getImageCaption();

            }

            jsonObjectResponse.put("appMediaArrayForUploadArray", jsonArray);

            if (!TextUtils.isEmpty(mapFileMediaID)) {
                jsonObjectResponse.put("mapFileMediaID", mapFileMediaID);
            }

            if (!TextUtils.isEmpty(mapFileName)) {
                jsonObjectResponse.put("mapFileName", mapFileName);
            }

            if (!TextUtils.isEmpty(mapPlanFileMediaID)) {
                jsonObjectResponse.put("mapPlanMediaID", mapPlanFileMediaID);
            }

            if (!TextUtils.isEmpty(mapPlanFileName)) {
                jsonObjectResponse.put("mapPlanFileNm", mapPlanFileName);
            }

            if (!TextUtils.isEmpty(mapPlanS3FilePath)) {
                jsonObjectResponse.put("mapPlanS3FilePath", mapPlanS3FilePath);
            }

            jsonObjectResponse.put("mapPlanStatus", mapPlanStatus);
            jsonObjectResponse.put("mapFileStatus", mapFileStatus);


            return jsonObjectResponse;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject();
    }


    private void setCallbackFunction(AppMediaDetails appMediaDetails, String callbackName) {
        try {
            JSONObject responseJsonObj = new JSONObject();
            responseJsonObj.put("step", appMediaDetails.getInstructionNumber());
            responseJsonObj.put("fileNm", appMediaDetails.getFileName());
            responseJsonObj.put("mediaID", appMediaDetails.getMediaID());
            responseJsonObj.put("imagePath", appMediaDetails.getFilePath());
            responseJsonObj.put("S3FilePath", appMediaDetails.getS3FilePath());
            responseJsonObj.put("status", appMediaDetails.getUploadStatus());
            responseJsonObj.put("IsImage", false);
            responseJsonObj.put("requestParams", new JSONObject(getJsResponseData().getResponseData()));


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(String.format("javascript:" + callbackName + "(%s)", responseJsonObj), null);
            } else {
                mWebView.loadUrl(String.format("javascript:" + callbackName + "(%s)", responseJsonObj));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public AppMediaDetails saveAppMediaDetails(File file, String offlineID, int instucationNumberClockIn, String srcFileName, int imageType, String caption) {
        AppMediaDetails appMediaDetails = null;
        try {

            AppMediaDetailsDAO appMediaDetailsDAO = new AppMediaDetailsDAO(mContext);
            appMediaDetails = appMediaDetailsDAO.getStoredAppMediaDetails(mContext,
                    file.getName());

            if (appMediaDetails == null) {
                appMediaDetails = new AppMediaDetails();

                appMediaDetails.setInstructionNumber(instucationNumberClockIn);

            } else {
                stopUploadingAndDeletedOldImageFile(appMediaDetails);
            }
            appMediaDetails.setOfflineDataID(offlineID);
            appMediaDetails.setFileName(file.getName());
            appMediaDetails.setImageType(imageType);
            appMediaDetails.setFileSizeBytes(String.valueOf(file.length()));
            appMediaDetails.setInstructionNumber(appMediaDetails.getInstructionNumber());
            appMediaDetails.setUploadStatus(AppMediaDetails.UPLOAD_INPROGRESS);
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
    private void stopUploadingAndDeletedOldImageFile(AppMediaDetails appMediaDetails) {
        try {
            RoofingUploader roofingUploader = RoofingUploader.getInstance(appMediaDetails, mContext);
            File fileOldDelete = new File(FileUtility.getHtmlDirFromSandbox(mContext) + File.separator + appMediaDetails.getFileName());
            fileOldDelete.delete();
            roofingUploader.stopUpload();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }

    private void launchNativeFileUploadByOptions(final JSResponseData jsResponseData) {

        try {

            final Dialog mBottomSheetDialog = new Dialog(mActivity,
                    R.style.MaterialDialogSheet);
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.popup_load_native_upload_type_window_layout, null);
            Button takePhoto = view.findViewById(R.id.btn_take_photo);
            Button fromGallery = view.findViewById(R.id.btn_from_gallery);
            Button documents = view.findViewById(R.id.btn_documents);
            Button cancel = view.findViewById(R.id.btn_cancel);

            if (jsResponseData.getLanguagePref() != null && jsResponseData.getLanguagePref().equalsIgnoreCase("ch")) {
                takePhoto.setText("拍照");
                fromGallery.setText("选择库");
                documents.setText("文件");
                cancel.setText("取消");
            }

            if (jsResponseData.isDocumentsOnly()) {
                documents.setVisibility(View.VISIBLE);
            } else {
                takePhoto.setVisibility(jsResponseData.isSkipCamera() ? View.GONE : View.VISIBLE);
                fromGallery.setVisibility(jsResponseData.isSkipLibrary() ? View.GONE : View.VISIBLE);
                documents.setVisibility(jsResponseData.isDocumentsUpload() ? View.VISIBLE : View.GONE);
            }

            mBottomSheetDialog.setContentView(view);
            mBottomSheetDialog.setCancelable(true);
            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
            mBottomSheetDialog.show();
            takePhoto.setOnClickListener(v -> {
                try {
                    mBottomSheetDialog.dismiss();

                    PermissionListener permissionListener = new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            CameraManager.launchCameraFromActivity(FileUploadConstant.MessageType.TYPE_IMAGE, CameraManager.REQUEST_IMAGE_CAPTURE);

                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {

                        }
                    };
                    TedPermission.create()
                            .setPermissionListener(permissionListener)
                            .setPermissions(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                            .check();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            });

            fromGallery.setOnClickListener(v -> {
                try {
                    mBottomSheetDialog.dismiss();
                    CustomCameraManager.launchCustomImageGallery(mActivity, null, jsResponseData.getColorCode(),
                            SELECT_GALLERY_IMAGE_CODE_WITHOUT_EDITOR);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            documents.setOnClickListener(v -> {
                try {
                    mBottomSheetDialog.dismiss();
                    String type = jsResponseData.getFileMimeType();
                    FileUtility.launchIntentByFileFormat(mActivity, TextUtils.isEmpty(type) ? "*/*" : type);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            cancel.setOnClickListener(v -> mBottomSheetDialog.dismiss());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void handleScanTextResult(Activity mActivity, int requestCode, int resultCode, Intent intent) {
        ScanTextUtility.getInstance(mActivity).onActivityResult(requestCode, resultCode, intent);
    }

    public void handleFileBrowsing(Intent intent) {


        Uri fileUri = intent.getData();
        String filepathImg = "";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            filepathImg = copyFileToInternalStorage(fileUri, "DOC"); // for target Api 30
        } else {
            filepathImg = FileUtility.getPath(mContext, fileUri);
        }
        String extention = filepathImg.substring(filepathImg.lastIndexOf(".") + 1);

        if (jsResponseData.getExtention() != null) {
            if (!jsResponseData.getExtention().equals(extention)) {
                Toast.makeText(mContext, extention + " not supported.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (jsResponseData.getSupportedFormat() != null) {
            List<String> list = Arrays.asList(jsResponseData.getSupportedFormat());

            if (list.size() > 0) {
                if (!list.contains(extention)) {
                    Toast.makeText(mContext, extention + " not supported.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        if (fileUri == null) {
            cancelJavaScriptCall();
            return;
        }

        mImageCaptureUri = Uri.fromFile(new File(filepathImg));
        String mimeType = FileUtility.getMimeType(mImageCaptureUri, mContext);

        if (FileUtility.isImageFile(mimeType)) {
            File to = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator + "Image_crop_" + System.currentTimeMillis() + ".png");
            try {
                moveFile(new File(filepathImg), to);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Intent intentCrop = PreviewActivity.newIntent(mContext,
                    CameraConfiguration.MEDIA_ACTION_PHOTO,
                    filepathImg, getJsResponseData().isCroped(), getJsResponseData().isShowCaption(), getJsResponseData().getCaption(), true, true);
            mActivity.startActivityForResult(intentCrop, REQUEST_CROP_IMAGE);
        } else {
            uploadFileAndCallback(filepathImg);
        }
    }

    private void uploadFileAndCallback(String filePath) {
        try {
            if (!jsResponseData.isWaitForResponse()) {
                setNativeSelectedPhotoCallbackFunction(FileUtility.getFileNameWithoutExists(filePath), offlineID);
            }
            startImageUpload(filePath, getJsResponseData().getOfflineID(), getJsResponseData().getAppID(),
                    FileUtility.getFileNameWithoutExists(filePath), AppMediaDetails.FILE_TYPE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cancelJavaScriptCall() {
        try {
            //call java script if loader is shown
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("isCancel", true);
            FileUploadUtility.callJavaScriptFunction(mActivity, mWebView, getJsResponseData().getCallbackfunction(), jsonObject);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String copyFileToInternalStorage(Uri uri, String newDirName) {
        Uri returnUri = uri;

        Cursor returnCursor = mContext.getContentResolver().query(returnUri, new String[]{
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

        } catch (Exception e) {

            Log.e("Exception", e.getMessage());
        }

        return output.getPath();
    }

    public void handleCustomImageGallery(Intent intent) {
        String filepath = intent.getStringExtra("imgPath");
        File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                "draw_" + System.currentTimeMillis() + ".png");

        String colorCode = jsResponseData != null ? jsResponseData.getColorCode() : null;

        IPRectangleAnnotationActivity.start(mActivity, filepath, outputFile.getAbsolutePath(),
                colorCode, jsResponseData.getPageTitle(),
                jsResponseData.isMapPlan() ? ACTION_REQUEST_EDITIMAGE_MAP_PLAN : ACTION_REQUEST_EDITIMAGE);

    }

    public static void setFreedrawing(String stringExtra, String filePath, String toolbarColor, String toolbarTitle, int actionRequestEditimage, int drawType) {
        File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                "draw_" + System.currentTimeMillis() + ".png");
        FreeDrwaingActivity.start(mActivity, stringExtra, filePath, outputFile.getAbsolutePath(),
                toolbarColor, toolbarTitle, ACTION_REQUEST_EDITIMAGE, drawType);

    }

    public void handleFreeDrawingImage(Intent intent) {
        String newFilePath = intent.getStringExtra(IPRectangleAnnotationActivity.SAVE_FILE_PATH);
        if (newFilePath != null) {
            mImageCaptureUri = Uri.fromFile(new File(newFilePath));
            saveImageToSyncHtmlFilesDir(mImageCaptureUri, intent);
        }
    }

    private void saveImageToSyncHtmlFilesDir(Uri imageCaptureUri, Context context) {
        try {
            if (imageCaptureUri == null) {
                Toast.makeText(context, "image capture error.", Toast.LENGTH_SHORT).show();
                return;
            }

            final String sourceFilePath = FileUtility.getPath(context,
                    imageCaptureUri);
            final String destFileName = generateImageFileNameByAppID(FileUtility.getExtensionWithDot(sourceFilePath));

            File to = new File(FileUtility.getHtmlDirFromSandbox(mContext) + File.separator + destFileName);
            if (to != null)
                mImageCaptureUri = Uri.fromFile(new File(to.getPath()));

            if (!TextUtils.isEmpty(sourceFilePath)) {
                File from = new File(sourceFilePath);

                //compress file image file
                int FILE_SIZE_BYTES_1_MB = jsResponseData.getMaxFileSize() == 0 ? 1 * 1024 * 1024 : jsResponseData.getMaxFileSize() * 1024 * 1024;

                //file size should be in between 1.5mb > -- no compression
                if (from.length() >= FILE_SIZE_BYTES_1_MB && !jsResponseData.isSelectVideo()) {

                    ImageCompression imageCompression = new ImageCompression(mActivity);
                    imageCompression.executeAsync(from.getAbsolutePath(), to.getAbsolutePath(), new ImageCompression.OnCompressedListener() {
                        @Override
                        public void onImageCompressed(String compressedPath) {
                            setNativeSelectedPhotoCallbackFunction(destFileName, offlineID);
                            System.out.println("compressed size : " + new File(compressedPath).length());

                            startImageUpload(compressedPath, offlineID);

                        }
                    });

                } else {

                    boolean isMoved = false;

                    File uploadFilePath = null;


                    isMoved = moveFile(from, to);


                    uploadFilePath = isMoved ? to : from;

                    if (!jsResponseData.isWaitForResponse()) {
                        setNativeSelectedPhotoCallbackFunction(FileUtility.getFileName(uploadFilePath.getAbsolutePath()), offlineID, jsResponseData.getCallbackfunction());
                    }

                    startImageUpload(uploadFilePath.getAbsolutePath(), offlineID);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void saveImageToSyncHtmlFilesDir(Uri imageCaptureUri, Intent intent) {
        try {
            if (imageCaptureUri == null) {
                Toast.makeText(mContext, "image capture error.", Toast.LENGTH_SHORT).show();
                return;
            }

            final String sourceFilePath = FileUtility.getPath(mContext,
                    imageCaptureUri);
            final String destFileName = generateImageFileNameByAppID(FileUtility.getExtensionWithDot(sourceFilePath));

            File to = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator + destFileName);

            if (!TextUtils.isEmpty(sourceFilePath)) {
                File from = new File(sourceFilePath);

                if (jsResponseData.isBase64Data()) {
                    setNativeBase64DataResponse(from.getAbsolutePath(), FileUtility.getFileName(from.getAbsolutePath()),
                            offlineID, FileUtility.getBase64Data(BitmapFactory.decodeFile(sourceFilePath)));
                    return;
                }

                //compress file image file
                int FILE_SIZE_BYTES_1_MB = 1 * 1024 * 1024;

                //file size should be in between 1.5mb > -- no compression
                if (from.length() >= FILE_SIZE_BYTES_1_MB) {

                    ImageCompression imageCompression = new ImageCompression(mContext);
                    imageCompression.executeAsync(from.getAbsolutePath(), to.getAbsolutePath(), new ImageCompression.OnCompressedListener() {
                        @Override
                        public void onImageCompressed(String compressedPath) {
                            setNativeSelectedPhotoCallbackFunction(destFileName, offlineID);
                            System.out.println("compressed size : " + new File(compressedPath).length());

                            startImageUpload(compressedPath, offlineID);

                        }
                    });
                } else {

                    boolean isMoved = false;

                    File uploadFilePath = null;

                    if (!FileUtility.isHelitrack()) {
                        isMoved = moveFile(from, to);
                    }

                    uploadFilePath = isMoved ? to : from;

                    if (!jsResponseData.isWaitForResponse()) {
                        if (intent != null) {
                            final String originalImage = FileUtility.getPath(mContext,
                                    Uri.fromFile(new File(intent.getStringExtra(IPRectangleAnnotationActivity.ORIGINAL_IMAGE))));

                            String annotationData = intent.getStringExtra("ANNOTATION_DATA");
                            setNativeSelectedPhotoCallbackFunctionV1(
                                    FileUtility.getFileName(uploadFilePath.getAbsolutePath()),
                                    offlineID,
                                    uploadFilePath.getPath(),
                                    originalImage,
                                    intent.getIntExtra(AnnotateActivity.ANNOTATION_COUNT, 0),
                                    annotationData);
                        }
                    }

                    startImageUpload(uploadFilePath.getAbsolutePath(), offlineID);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void setNativeSelectedPhotoCallbackFunctionV1(String filename, String offlineID, String filePath, String originalImagePath, int badgeCount, String annotationData) {
        setNativeSelectedPhotoCallbackFunctionV2(filename, offlineID, getJsResponseData().getCallbackfunction(), filePath, originalImagePath, badgeCount, annotationData);
    }

    private void setNativeSelectedPhotoCallbackFunctionV2(String filename, String offlineID, String callbackName, String filePath, String originalImagePath, int badgeCount, String annotationData) {
        try {
            JSONObject responseJsonObj = setFileNameAndOfflineIDToResponseData(filename, offlineID);
            responseJsonObj.put("imagePath", filePath);
            responseJsonObj.put("originalImagePath", originalImagePath);
            responseJsonObj.put("annotationCount", badgeCount);
            responseJsonObj.put("annotateData", new JSONArray(annotationData));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.evaluateJavascript(String.format("javascript:" + callbackName + "(%s)", responseJsonObj), null);
            } else {
                mWebView.loadUrl(String.format("javascript:" + callbackName + "(%s)", responseJsonObj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
