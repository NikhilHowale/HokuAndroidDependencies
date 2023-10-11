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
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.AUTH_TOKEN;
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
import android.annotation.SuppressLint;
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
import com.hokuapps.loadnativefileupload.backgroundtask.FileUploader;
import com.hokuapps.loadnativefileupload.backgroundtask.ImageCompression;
import com.hokuapps.loadnativefileupload.constants.FileUploadConstant;
import com.hokuapps.loadnativefileupload.database.FileContentProvider;
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

@SuppressLint("StaticFieldLeak")
public class NativeFileUpload {
    public WebView mWebView;
    public static Activity mActivity;
    public static Context mContext;
    private JSResponseData jsResponseData;
    private String appAuthToken = "";
    public String offlineID = null;
    private int instructionNumberClockIn = 0;
    public static final int ACTION_REQUEST_EDIT_IMAGE = 9006;
    public static final int ACTION_REQUEST_EDIT_IMAGE_MAP_PLAN = 9008;
    public static final int SELECT_GALLERY_IMAGE_CODE_WITHOUT_EDITOR = 7002;
    public static final int SELECT_GALLERY_IMAGE_CODE = 7000;
    public static final int REQUEST_FILE_BROWSER = 9012;
    private String caption;
    private Uri mImageCaptureUri = null;

    private String serverAuthToken;
    private static NativeFileUpload Instance;
    public static String APP_FILE_URL = "";

    public NativeFileUpload() {

    }

    /**
     * get the instance of the class
     *
     * @return instance of NativeFileUpload
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
     * @param mWebView require to call javascript function
     * @param mActivity activity context
     * @param mContext context
     * @param uploadUrl upload capture images or draw images to this url
     * @param authority access application database
     */
    public void initialization(WebView mWebView, Activity mActivity, Context mContext,String uploadUrl, String authority) {
        this.mWebView = mWebView;
        NativeFileUpload.mActivity = mActivity;
        NativeFileUpload.mContext = mContext;
        APP_FILE_URL = uploadUrl;
        FileContentProvider.getInstance().setUpDatabase(authority);
    }


    /**
     *  set data for authorization
     * @param responseData jsonObject for retrieve auth data
     */
    public void setAuthDetails(String responseData){
        try {
            JSONObject object = new JSONObject(responseData);
            this.serverAuthToken = FileUploadUtility.getStringObjectValue(object, AUTH_TOKEN);
            this.appAuthToken = "";
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Entry point of the NativeFileUpload module
     *
     * @param responseData provide option and require data for that option
     * @throws  JSONException occur when key mis match
     */
    public void loadNativeFileUpload(final String responseData) throws JSONException {

        JSResponseData jsResponseDataModel = FileUploadUtility.parseLoadNativeFileUploadJsResponseData(responseData);
        setJsResponseData(jsResponseDataModel);
        offlineID = jsResponseDataModel.getOfflineID();

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
                            jsResponseData.isCropped(),
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
     * @param responseData provide data for image annotation with previous annotations on image
     * @throws JSONException  occur when key mis match
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
     * @param jsResponseData provide image url for free drawing on image
     */
    private void startFreeDrawingActivity(JSResponseData jsResponseData) {
        File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                "draw_" + System.currentTimeMillis() + ".png");
        String filePath = null;
        try {
            filePath =jsResponseData.getImageURL();

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
     * @param jsResponseData provide draw type to draw circle, rect or line on image
     * @param filePath show image using file path
     */
    private void startAnnotationActivity(JSResponseData jsResponseData, String filePath) {
        File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                "draw_" + System.currentTimeMillis() + ".png");
        String colorCode = jsResponseData != null ? jsResponseData.getColorCode() : null;
        assert jsResponseData != null;
        int drawType = jsResponseData.getDrawType();
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
     * @param jsonObject scan image to retrieve text and add to jsonObject for callback
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                TedPermission.create()
                        .setPermissionListener(permissionlistener)
                        .setPermissions(
                                Manifest.permission.READ_MEDIA_IMAGES
                        )
                        .check();
            } else {
                TedPermission.create()
                        .setPermissionListener(permissionlistener)
                        .setPermissions(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                        .check();
            }
        });
    }


    /**
     * get the response data
     *
     * @return json response data
     */
    public JSResponseData getJsResponseData() {
        return jsResponseData != null ? jsResponseData : new JSResponseData();
    }


    /**
     * handle captured image result data
     *
     * @param intent contain annotated images path with caption as result in intent
     */
    public void handleImageResultIntent(Intent intent) {

        String filePath = intent.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
        if(filePath == null) return;

        caption = intent.getStringExtra(CameraConfiguration.Arguments.CAPTION);
        boolean isFromGallery = intent.getBooleanExtra(CameraConfiguration.Arguments.IS_FROM_GALLERY, false);

        if(isFromGallery){
            Intent previewActivityIntent = PreviewActivity.newIntent(mActivity,
                    CameraConfiguration.MEDIA_ACTION_PHOTO, filePath, false,
                    getJsResponseData().isShowCaption(), getJsResponseData().getCaption(),
                    true, true, getJsResponseData().getResponseData());

            mActivity.startActivityForResult(previewActivityIntent, CustomCameraManager.CAPTURE_MEDIA_PHOTO);
            return;
        }

        mImageCaptureUri = Uri.fromFile(new File(filePath));

        saveImageToSyncHtmlFilesDir(mImageCaptureUri, mContext, null);

    }


    /**
     * @param filename name of selected image
     * @param offlineID upload image against offlineID
     */
    private void setNativeSelectedPhotoCallbackFunction(String filename, String offlineID) {
        setNativeSelectedPhotoCallbackFunction(filename, offlineID, getJsResponseData().getCallbackFunction());
    }


    /**
     * @param filename name of selected image
     * @param offlineID upload image against offlineID
     * @param callbackName provide image data to callback
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
     * @param filename name of selected image
     * @param offlineID upload image against offlineID
     * @return jsonObject with filename and offlineID
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
     * @param filePath  filePath to upload server
     * @param offlineID upload image against offlineID
     */
    private void startImageUpload(final String filePath, String offlineID) {
        startImageUpload(filePath, offlineID, getJsResponseData().getAppID(), getJsResponseData().getSrcImageName(), AppMediaDetails.INSTRUCTION_IMAGE_TYPE);

    }


    /**
     * start uploading image
     *
     * @param filePath filePath to upload file on server
     * @param offlineID upload image against offlineID
     * @param appID ID of app
     * @param srcName set image name if provided
     * @param imageType give info of image type to upload
     */
    private void startImageUpload(final String filePath, String offlineID, String appID, String srcName, int imageType) {
        File file = new File(filePath);
        String fileName = FileUtility.getFileNameWithoutExists(filePath);
        final AppMediaDetails appMediaDetails = FileUploadUtility.saveAppMediaDetails(file, offlineID, instructionNumberClockIn,
                TextUtils.isEmpty(srcName) ? fileName : srcName, imageType, caption, mContext);

        instructionNumberClockIn = appMediaDetails.getInstructionNumber();

        if (appMediaDetails == null) {
            return;
        }
        FileUploader roofingUploader = FileUploader.getInstance(appMediaDetails, mContext);
        roofingUploader.setFilePath(file.getPath());
        roofingUploader.setAppID(appID);
        roofingUploader.setAppsServerToken(serverAuthToken);
        roofingUploader.setUiCallBack(new FileUploader.IUICallBackRoofing() {
            @Override
            public void onSuccess(final ServiceRequest serviceRequest) {
                System.out.println("UPLOAD FINISHED:" + Calendar.getInstance().getTime());
                try {
                    if (serviceRequest != null && serviceRequest.getAppMediaDetails() != null) {


                        if (jsResponseData != null && jsResponseData.isWaitForResponse()) {
                            setCallbackFunction(serviceRequest.getAppMediaDetails(), jsResponseData.getCallbackFunction());
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
     * @param appMediaDetails contain image related info like filename, status, id, uploaded path of image
     * @param callbackName provide image data to callback
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
     * @param jsResponseData provide jsonObject
     */
    public void setJsResponseData(JSResponseData jsResponseData) {
        this.jsResponseData = jsResponseData;
    }


    /**
     * show dialogue for options (gallery/camera/Document)
     *
     * @param jsResponseData provide data to open custom dialog of file selection
     */
    @SuppressLint("InflateParams")
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

            if(mBottomSheetDialog.getWindow() != null ){
                mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
            }

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
     * @param mActivity activity context
     * @param requestCode provide info of which intent requested
     * @param resultCode provide status of requested intent
     * @param intent contain data if result is success
     */
    public void handleScanTextResult(Activity mActivity, int requestCode, int resultCode, Intent intent) {
        ScanTextUtility.getInstance(mActivity).onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * handle file browsing
     *
     * @param intent contain data if result is success
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

        if (jsResponseData.getExtension() != null) {
            if (!jsResponseData.getExtension().equals(extension)) {
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
            FileUploadUtility.cancelJavaScriptCall(mActivity, mWebView, getJsResponseData().getCallbackFunction());
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
                    filepathImg, getJsResponseData().isCropped(), getJsResponseData().isShowCaption(), getJsResponseData().getCaption(), true, true);
            mActivity.startActivityForResult(intentCrop, REQUEST_CROP_IMAGE);
        } else {
            uploadFileAndCallback(filepathImg);
        }
    }

    /**
     * start image upload and set callback function
     *
     * @param filePath filePath to upload file on server
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
     * @param intent contain image path of selected image from gallery
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
     * @param stringExtra json data for activity to perform work
     * @param filePath show image with filePath to draw
     * @param toolbarColor set toolbar color
     * @param toolbarTitle title for toolbar
     * @param actionRequestEditImage action for image edit
     * @param drawType type of draw on image like circle , line
     */
    public static void setFreeDrawing(String stringExtra, String filePath, String toolbarColor, String toolbarTitle, int actionRequestEditImage, int drawType) {
        File outputFile = new File(FileUploadUtility.getHtmlDirFromSandbox(mContext) + File.separator +
                "draw_" + System.currentTimeMillis() + ".png");
        FreeDrawingActivity.start(mActivity, stringExtra, filePath, outputFile.getAbsolutePath(),
                toolbarColor, toolbarTitle, ACTION_REQUEST_EDIT_IMAGE, drawType);

    }

    /**
     * handle onActivity result of free drawing and upload it to file directory
     *
     * @param intent contain image path of selected draw image
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
     * @param imageCaptureUri file uri of capture image
     * @param context context
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
                            setNativeSelectedPhotoCallbackFunction(FileUtility.getFileName(uploadFilePath.getAbsolutePath()), offlineID, jsResponseData.getCallbackFunction());
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
     * @param filename name of file
     * @param offlineID upload image against offlineID
     * @param filePath image path after annotation
     * @param originalImagePath path of original image
     * @param badgeCount count of annotation added
     * @param annotationData provide which annotation added to image
     */
    private void setNativeSelectedPhotoCallbackFunctionV1(String filename, String offlineID, String filePath, String originalImagePath, int badgeCount, String annotationData) {
        try {
            JSONObject responseJsonObj = setFileNameAndOfflineIDToResponseData(filename, offlineID);
            responseJsonObj.put(IMAGE_PATH, filePath);
            responseJsonObj.put(ORIGINAL_IMAGE_PATH, originalImagePath);
            responseJsonObj.put(ANNOTATION_COUNT, badgeCount);
            responseJsonObj.put(ANNOTATE_DATA, new JSONArray(annotationData));
            mWebView.evaluateJavascript(String.format("javascript:" + getJsResponseData().getCallbackFunction() + "(%s)", responseJsonObj), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
