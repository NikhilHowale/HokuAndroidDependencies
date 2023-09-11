package com.hokuapps.loadnativefileupload;

import static com.hokuapps.loadnativefileupload.constants.FileUploadConstant.Shape.CIRCLE;
import static com.hokuapps.loadnativefileupload.constants.FileUploadConstant.Shape.LINE;
import static com.hokuapps.loadnativefileupload.constants.FileUploadConstant.Shape.PATH;
import static com.hokuapps.loadnativefileupload.constants.FileUploadConstant.Shape.Rectangle;
import static com.hokuapps.loadnativefileupload.constants.FileUploadConstant.options.IS_ANNOTATION;
import static com.hokuapps.loadnativefileupload.constants.FileUploadConstant.options.IS_ANNOTATION_WITH_IMAGE_PATH;
import static com.hokuapps.loadnativefileupload.constants.FileUploadConstant.options.IS_ANNOTATION_WITH_IMAGE_URL;
import static com.hokuapps.loadnativefileupload.constants.FileUploadConstant.options.IS_ANNOTATION_WITH_LOCAL_IMAGE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.ANNOTATE_DATA;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.ANNOTATION_COUNT;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.CAPTION;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.FILE_NAME;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.FILE_NM;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IMAGE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_IMAGE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MEDIA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.NEXT_BUTTON_CALLBACK;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.OFFLINE_DATA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.ORIGINAL_IMAGE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.REQUEST_PARAMS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.S3_FILE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.SCANNED_TEXT;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.STATUS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.STEP;
import static com.hokuapps.loadnativefileupload.utilities.FileUploadUtility.moveFile;
import static com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration.REQUEST_CROP_IMAGE;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
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
import com.hokuapps.loadnativefileupload.annotate.FreeDrawingActivity;
import com.hokuapps.loadnativefileupload.backgroundtask.ImageCompression;
import com.hokuapps.loadnativefileupload.backgroundtask.FileUploader;
import com.hokuapps.loadnativefileupload.constants.FileUploadConstant;
import com.hokuapps.loadnativefileupload.imageEditor.IPRectangleAnnotationActivity;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
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
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class NativeFileUpload {
    public WebView mWebView;
    public static Activity mActivity;
    public static Context mContext;
    private String[] requiredJSONObjectKey = {};
    private JSResponseData jsResponseData;
    private String appAuthToken = "";
    public String offlineID = null;
    private int instucationNumberClockIn = 0;
    public static final int ACTION_REQUEST_EDIT_IMAGE = 9006;
    public static final int ACTION_REQUEST_EDIT_IMAGE_MAP_PLAN = 9008;
    public static final int SELECT_GALLERY_IMAGE_CODE_WITHOUT_EDITOR = 7002;
    public static final int SELECT_GALLERY_IMAGE_CODE = 7000;
    public static final int REQUEST_FILE_BROWSER = 9012;
    private String caption;
    private Uri mImageCaptureUri = null;

    private String authorizationToken;
    private static NativeFileUpload Instance;
    public static String APP_FILE_URL = "";
    public static String AUTHORITY = "com.pearlista.database";

    public NativeFileUpload() {

    }

    /**
     * get the instance of the class
     *
     * @return
     */
    public static NativeFileUpload getInstance() {
        if (Instance == null) {
            Instance = new NativeFileUpload();
        }
        return Instance;
    }

    /**
     * Initialize the given variables
     *
     * @param mWebView
     * @param mActivity
     * @param mContext
     * @param authorizationToken
     * @param appAuthToken
     * @param uploadUrl
     * @param authority
     */
    public void initialization(WebView mWebView, Activity mActivity, Context mContext,
                               String authorizationToken, String appAuthToken, String uploadUrl, String authority) {
        this.mWebView = mWebView;
        NativeFileUpload.mActivity = mActivity;
        NativeFileUpload.mContext = mContext;
        this.authorizationToken = authorizationToken;
        this.appAuthToken = appAuthToken;
        APP_FILE_URL = uploadUrl;
        AUTHORITY = authority;

    }

    /**
     * Entry point of the NativeFileUpload module
     *
     * @param responseData
     * @throws JSONException
     */
    public void loadNativeFileUpload(final String responseData) throws JSONException {

        JSResponseData jsResponseDataModel = FileUploadUtility.parseLoadNativeFileUploadJsResponseData(responseData);
        setJsResponseData(jsResponseDataModel);

        if (jsResponseData == null) return;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(responseData);
            String option = FileUploadUtility.getOption(jsResponseData);

            switch (option) {

                case FileUploadConstant.options.IS_SCAN_TEXT:
                    parseScanTextResponse(jsonObject);

                    break;
                case FileUploadConstant.options.IS_DEFAULT_CAMERA:
                    launchNativeFileUploadByOptions(jsResponseData);
                    break;
                case FileUploadConstant.options.IS_FREE_DRAW:
                    startFreeDrawingActivity(jsResponseData);
                    break;
                case IS_ANNOTATION:
                    startShapeAnnotation(responseData);
                    break;
                case IS_ANNOTATION_WITH_IMAGE_PATH:
                    String filePath = jsResponseData.getOriginalImagePath();
                    startAnnotationActivity(jsResponseData, filePath);
                    break;
                case IS_ANNOTATION_WITH_LOCAL_IMAGE:
                    String localFilePath = FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator + FileUploadUtility.getStringObjectValue(new JSONObject(responseData), "localImageName");
                    startAnnotationActivity(jsResponseData, localFilePath);
                    break;
                case IS_ANNOTATION_WITH_IMAGE_URL:
                    String urlFilePath = jsResponseData.getImageURL();
                    startAnnotationActivity(jsResponseData, urlFilePath);
                    break;
                default:
                    CustomCameraManager.launchCameraFromActivity(mActivity, !jsResponseData.isSkipLibrary(),
                            jsResponseData.getInstructionText(), false, false,
                            jsResponseData.isCroped(),
                            false, jsResponseData.isRectangle(),
                            jsResponseData.getResponseData());
                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Draw shape over image(line,circle,rectangle,path)
     *
     * @param responseData
     * @throws JSONException
     */
    private void startShapeAnnotation(String responseData) throws JSONException {
        File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                "draw_" + System.currentTimeMillis() + ".png");
        String colorCode = jsResponseData != null ? jsResponseData.getColorCode() : null;

        assert jsResponseData != null;
        String filePath = jsResponseData.getOriginalImagePath();

        boolean usedForAnnotation = jsResponseData.isUsedForAnnotation();

        JSONArray annotateData = new JSONObject(responseData).getJSONArray(ANNOTATE_DATA);
        if (annotateData != null) {
            IPRectangleAnnotationActivity.start(mActivity, filePath,
                    outputFile.getAbsolutePath(), colorCode, usedForAnnotation, annotateData.toString());
        }
    }


    /**
     * Start free drawing activity
     *
     * @param jsResponseData
     */
    private void startFreeDrawingActivity(JSResponseData jsResponseData) {
        File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                "draw_" + System.currentTimeMillis() + ".png");
        String filePath = null;
        try {
            filePath = jsResponseData.getImageURL();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String colorCode = jsResponseData.getColorCode();
        if (filePath != null && !filePath.isEmpty()) {
            FreeDrawingActivity.start(mActivity, jsResponseData.getResponseData(), filePath, outputFile.getAbsolutePath(),
                    colorCode, jsResponseData.getPageTitle(), ACTION_REQUEST_EDIT_IMAGE, 5);
        } else {
            FreeDrawingActivity.start(mActivity, jsResponseData.getResponseData(), "Drawing Board", outputFile.getAbsolutePath(),
                    colorCode, jsResponseData.getPageTitle(), ACTION_REQUEST_EDIT_IMAGE, 5);
        }
    }


    /**
     * Start annotation activity
     *
     * @param jsResponseData
     * @param filePath
     */
    private void startAnnotationActivity(JSResponseData jsResponseData, String filePath) {
        File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                "draw_" + System.currentTimeMillis() + ".png");
        String colorCode = jsResponseData != null ? jsResponseData.getColorCode() : null;
        assert jsResponseData != null;
        int drawType = jsResponseData.getDrawtype();
        String type = null;
        switch (drawType){
            case 0 :
                type = LINE;
                break;
            case 1 :
                type = CIRCLE;
                break;
            case 2 :
                type = Rectangle;
                break;
            case 3 :
                type = PATH;
                break;
            case 4 :
                AnnotateActivity.start(mActivity, jsResponseData.getResponseData(), filePath, outputFile.getAbsolutePath(),
                        colorCode, jsResponseData.getPageTitle(),
                        jsResponseData.isMapPlan() ? ACTION_REQUEST_EDIT_IMAGE_MAP_PLAN : ACTION_REQUEST_EDIT_IMAGE, drawType);
                break;

        }

        IPRectangleAnnotationActivity.start(mActivity, filePath, outputFile.getAbsolutePath(),
                colorCode, jsResponseData.getPageTitle(),
                jsResponseData.isMapPlan() ? ACTION_REQUEST_EDIT_IMAGE_MAP_PLAN : ACTION_REQUEST_EDIT_IMAGE
                , type, jsResponseData.getColor());
    }


    /**
     * parse Scanned Text Response data
     *
     * @param jsonObject
     */
    private void parseScanTextResponse(final JSONObject jsonObject) {
        mActivity.runOnUiThread(() -> {

            PermissionListener permissionlistener = new PermissionListener() {

                @Override
                public void onPermissionGranted() {
                    ScanTextUtility.getInstance(mActivity)
                            .setImageScanListener((string, jsonArrayScannedText) -> {
                                System.out.println("IMAGE SCANNED :" + string);
                                String nextButtonCallback = FileUploadUtility.getStringObjectValue(jsonObject, NEXT_BUTTON_CALLBACK);

                                try {
                                    jsonObject.put(SCANNED_TEXT, jsonArrayScannedText);
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


    /**
     * get the response data
     *
     * @return
     */
    public JSResponseData getJsResponseData() {
        return jsResponseData != null ? jsResponseData : new JSResponseData();
    }


    /**
     * handle captured image result data
     *
     * @param intent
     */
    public void handleImageResultIntent(Intent intent) {

        String filePath = intent.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
        caption = intent.getStringExtra(CameraConfiguration.Arguments.CAPTION);
        mImageCaptureUri = Uri.fromFile(new File(filePath));

        saveImageToSyncHtmlFilesDir(mImageCaptureUri, mContext, null);

    }


    /**
     * @param filename
     * @param offlineID
     */
    private void setNativeSelectedPhotoCallbackFunction(String filename, String offlineID) {
        setNativeSelectedPhotoCallbackFunction(filename, offlineID, getJsResponseData().getCallbackfunction());
    }


    /**
     * @param filename
     * @param offlineID
     * @param callbackName
     */
    private void setNativeSelectedPhotoCallbackFunction(String filename, String offlineID, String callbackName) {
        JSONObject responseJsonObj = setFileNameAndOfflineIDToResponseData(filename, offlineID);
        try {
            String captureFilePath = FileUtility.getPath(mContext, mImageCaptureUri);
            responseJsonObj.put(IMAGE_PATH, captureFilePath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mWebView.evaluateJavascript(String.format("javascript:" + callbackName + "(%s)", responseJsonObj.toString()), null);
    }


    /**
     * set file name and offline id to response data
     *
     * @param filename
     * @param offlineID
     * @return
     */
    private JSONObject setFileNameAndOfflineIDToResponseData(String filename, String offlineID) {
        try {
            JSONObject jsonObject = new JSONObject(getJsResponseData().getResponseData());
            jsonObject.put(FILE_NAME, filename);
            jsonObject.put(OFFLINE_DATA_ID, offlineID);
            jsonObject.putOpt(CAPTION, caption);
            return jsonObject;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new JSONObject();
    }


    /**
     * Start image upload to server and save to local db with its information
     *
     * @param filePath
     * @param offlineID
     */
    private void startImageUpload(final String filePath, String offlineID) {
        startImageUpload(filePath, offlineID, getJsResponseData().getAppID(), getJsResponseData().getSrcImageName(), AppMediaDetails.INSTRUCTION_IMAGE_TYPE);

    }


    /**
     * start uploading image
     *
     * @param filePath
     * @param offlineID
     * @param appID
     * @param srcName
     * @param imageType
     */
    private void startImageUpload(final String filePath, String offlineID, String appID, String srcName, int imageType) {
        File file = new File(filePath);
        String fileName = FileUtility.getFileNameWithoutExists(filePath);
        final AppMediaDetails appMediaDetails = FileUploadUtility.saveAppMediaDetails(file, offlineID, instucationNumberClockIn,
                TextUtils.isEmpty(srcName) ? fileName : srcName, imageType, caption, mContext);

        instucationNumberClockIn = appMediaDetails.getInstructionNumber();

        if (appMediaDetails == null) {
            return;
        }
        FileUploader roofingUploader = FileUploader.getInstance(appMediaDetails, mContext);
        roofingUploader.setFilePath(file.getPath());
        roofingUploader.setAppID(appID);
        roofingUploader.setAppsServerToken(appAuthToken);
        roofingUploader.setAuthToken(authorizationToken);
        roofingUploader.setUiCallBack(new FileUploader.IUICallBackRoofing() {
            @Override
            public void onSuccess(final ServiceRequest serviceRequest) {
                System.out.println("UPLOAD FINISHED:" + Calendar.getInstance().getTime());
                try {
                    if (serviceRequest != null && serviceRequest.getAppMediaDetails() != null) {


                        if (jsResponseData != null && jsResponseData.isWaitForResponse()) {
                            setCallbackFunction(serviceRequest.getAppMediaDetails(), jsResponseData.getCallbackfunction());
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

            @Override
            public void onFailure(ServiceRequest serviceRequest) {
                System.out.println("UPLOAD FAILED:" + Calendar.getInstance().getTime());
            }
        });

        roofingUploader.startUpload();
        System.out.println("UPLOAD STARTED:" + Calendar.getInstance().getTime());

    }


    /**
     * put details in json object and set callback function
     *
     * @param appMediaDetails
     * @param callbackName
     */
    private void setCallbackFunction(AppMediaDetails appMediaDetails, String callbackName) {
        try {
            JSONObject responseJsonObj = new JSONObject();
            responseJsonObj.put(STEP, appMediaDetails.getInstructionNumber());
            responseJsonObj.put(FILE_NM, appMediaDetails.getFileName());
            responseJsonObj.put(MEDIA_ID, appMediaDetails.getMediaID());
            responseJsonObj.put(IMAGE_PATH, appMediaDetails.getFilePath());
            responseJsonObj.put(S3_FILE_PATH, appMediaDetails.getS3FilePath());
            responseJsonObj.put(STATUS, appMediaDetails.getUploadStatus());
            responseJsonObj.put(IS_IMAGE, false);
            responseJsonObj.put(REQUEST_PARAMS, new JSONObject(getJsResponseData().getResponseData()));

            mWebView.evaluateJavascript(String.format("javascript:" + callbackName + "(%s)", responseJsonObj), null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * set json response data
     *
     * @param jsResponseData
     */
    public void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }


    /**
     * show dialogue for options (gallery/camera/Document)
     *
     * @param jsResponseData
     */
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
                mBottomSheetDialog.dismiss();
                openCustomCamera();

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


    /**
     * Launch in app custom camera
     */
    private void openCustomCamera() {
        try {


            PermissionListener permissionListener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    CameraManager.launchCameraFromActivity(FileUploadConstant.MessageType.TYPE_IMAGE, CameraManager.REQUEST_IMAGE_CAPTURE);

                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {

                }
            };

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                TedPermission.create()
                        .setPermissionListener(permissionListener)
                        .setPermissions(
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_MEDIA_IMAGES

                        )
                        .check();
            } else {
                TedPermission.create()
                        .setPermissionListener(permissionListener)
                        .setPermissions(Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * handle scanned text results
     *
     * @param mActivity
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void handleScanTextResult(Activity mActivity, int requestCode, int resultCode, Intent intent) {
        ScanTextUtility.getInstance(mActivity).onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * handle file browsing
     *
     * @param intent
     */
    public void handleFileBrowsing(Intent intent) {
        Uri fileUri = intent.getData();
        String filepathImg = "";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            filepathImg = FileUploadUtility.copyFileToInternalStorage(fileUri, "DOC", mContext); // for target Api 30
        } else {
            filepathImg = FileUtility.getPath(mContext, fileUri);
        }
        String extension = filepathImg.substring(filepathImg.lastIndexOf(".") + 1);

        if (jsResponseData.getExtention() != null) {
            if (!jsResponseData.getExtention().equals(extension)) {
                Toast.makeText(mContext, extension + " not supported.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (jsResponseData.getSupportedFormat() != null) {
            List<String> list = Arrays.asList(jsResponseData.getSupportedFormat());

            if (list.size() > 0) {
                if (!list.contains(extension)) {
                    Toast.makeText(mContext, extension + " not supported.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
        if (fileUri == null) {
            FileUploadUtility.cancelJavaScriptCall(mActivity, mWebView, getJsResponseData().getCallbackfunction());
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

    /**
     * start image upload and set callback function
     *
     * @param filePath
     */
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

    /**
     * handle custom image gallery
     *
     * @param intent
     */
    public void handleCustomImageGallery(Intent intent) {
        String filepath = intent.getStringExtra("imgPath");
        File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                "draw_" + System.currentTimeMillis() + ".png");

        String colorCode = jsResponseData != null ? jsResponseData.getColorCode() : null;

        IPRectangleAnnotationActivity.start(mActivity, filepath, outputFile.getAbsolutePath(),
                colorCode, jsResponseData.getPageTitle(),
                jsResponseData.isMapPlan() ? ACTION_REQUEST_EDIT_IMAGE_MAP_PLAN : ACTION_REQUEST_EDIT_IMAGE);

    }

    /**
     * takes image from file path and add drawing in canvas
     *
     * @param stringExtra
     * @param filePath
     * @param toolbarColor
     * @param toolbarTitle
     * @param actionRequestEditimage
     * @param drawType
     */
    public static void setFreeDrawing(String stringExtra, String filePath, String toolbarColor, String toolbarTitle, int actionRequestEditimage, int drawType) {
        File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                "draw_" + System.currentTimeMillis() + ".png");
        FreeDrawingActivity.start(mActivity, stringExtra, filePath, outputFile.getAbsolutePath(),
                toolbarColor, toolbarTitle, ACTION_REQUEST_EDIT_IMAGE, drawType);

    }

    /**
     * handle onActivity result of free drawing and upload it to file directory
     *
     * @param intent
     */
    public void handleFreeDrawingImage(Intent intent) {
        String newFilePath = intent.getStringExtra(IPRectangleAnnotationActivity.SAVE_FILE_PATH);
        if (newFilePath != null) {
            mImageCaptureUri = Uri.fromFile(new File(newFilePath));
            saveImageToSyncHtmlFilesDir(mImageCaptureUri, mContext, intent);
        }
    }

    /**
     * compress captured image and save captured image to web html folder
     *
     * @param imageCaptureUri
     * @param context
     */
    private void saveImageToSyncHtmlFilesDir(Uri imageCaptureUri, Context context, Intent intent) {
        try {
            if (imageCaptureUri == null) {
                Toast.makeText(context, "image capture error.", Toast.LENGTH_SHORT).show();
                return;
            }

            final String sourceFilePath = FileUtility.getPath(context,
                    imageCaptureUri);
            final String destFileName = FileUploadUtility.generateImageFileNameByAppID(FileUtility.getExtensionWithDot(sourceFilePath), getJsResponseData().getAppID());

            File to = new File(FileUtility.getHtmlDirFromSandbox(mContext) + File.separator + destFileName);
            if (to != null)
                mImageCaptureUri = Uri.fromFile(new File(to.getPath()));

            if (!TextUtils.isEmpty(sourceFilePath)) {
                File from = new File(sourceFilePath);

                //compress file image file
                int FILE_SIZE_BYTES_1_MB = jsResponseData.getMaxFileSize() == 0 ? 1024 * 1024 : jsResponseData.getMaxFileSize() * 1024 * 1024;

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
                        } else {
                            setNativeSelectedPhotoCallbackFunction(FileUtility.getFileName(uploadFilePath.getAbsolutePath()), offlineID, jsResponseData.getCallbackfunction());
                        }

                    }

                    startImageUpload(uploadFilePath.getAbsolutePath(), offlineID);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * sending image data to bridge call
     *
     * @param filename
     * @param offlineID
     * @param filePath
     * @param originalImagePath
     * @param badgeCount
     * @param annotationData
     */
    private void setNativeSelectedPhotoCallbackFunctionV1(String filename, String offlineID, String filePath, String originalImagePath, int badgeCount, String annotationData) {
        try {
            JSONObject responseJsonObj = setFileNameAndOfflineIDToResponseData(filename, offlineID);
            responseJsonObj.put(IMAGE_PATH, filePath);
            responseJsonObj.put(ORIGINAL_IMAGE_PATH, originalImagePath);
            responseJsonObj.put(ANNOTATION_COUNT, badgeCount);
            responseJsonObj.put(ANNOTATE_DATA, new JSONArray(annotationData));
            mWebView.evaluateJavascript(String.format("javascript:" + getJsResponseData().getCallbackfunction() + "(%s)", responseJsonObj), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
