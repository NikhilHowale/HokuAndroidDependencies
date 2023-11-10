package com.hokuapps.loadnativefileupload.backgroundtask;

import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.ANNOTATE_DATA;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.ANNOTATION_COUNT;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.CAPTION;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.FILE_NAME;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.FILE_NM;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IMAGE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IS_IMAGE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.MEDIA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.OFFLINE_DATA_ID;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.ORIGINAL_IMAGE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.REQUEST_PARAMS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.S3_FILE_PATH;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.STATUS;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.STEP;
import static com.hokuapps.loadnativefileupload.utilities.FileUploadUtility.moveFile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebView;
import android.widget.Toast;

import com.hokuapps.loadnativefileupload.annotate.AnnotateActivity;
import com.hokuapps.loadnativefileupload.imageEditor.IPRectangleAnnotationActivity;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
import com.hokuapps.loadnativefileupload.models.JSResponseData;
import com.hokuapps.loadnativefileupload.restrequest.ServiceRequest;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;
import com.hokuapps.loadnativefileupload.utilities.FileUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;

public class ImageUpload {
    @SuppressLint("StaticFieldLeak")
    private static ImageUpload instance;

    private Activity mActivity;
    private WebView mWebview;
    private JSResponseData jsResponseData;
    private final String caption = "";
    private int instructionNumberClockIn = 0;
    public String offlineID = null;

    private String serverAuthToken;

    public static ImageUpload getInstance(){
        if(instance == null){
            instance = new ImageUpload();
        }

        return instance;
    }

    public void initUpload(Activity context, WebView mWebview, JSResponseData mJSResponseData, String serverAuthToken){
        this.mActivity = context;
        this.mWebview = mWebview;
        this.jsResponseData = mJSResponseData;
        this.serverAuthToken = serverAuthToken;
    }


    /**
     * @param filePath path of selected image
     * @param offlineID upload image against offlineID
     */
    private void setNativeSelectedPhotoCallbackFunction(String filePath, String offlineID) {
        setNativeSelectedPhotoCallbackFunction(filePath, offlineID, jsResponseData.getCallbackFunction());
    }

    /**
     * @param filePath path of selected image
     * @param offlineID upload image against offlineID
     * @param callbackName provide image data to callback
     */
    public void setNativeSelectedPhotoCallbackFunction(String filePath, String offlineID, String callbackName) {
        String filename = FileUtility.getFileName(filePath);
        JSONObject responseJsonObj = setFileNameAndOfflineIDToResponseData(filename, offlineID);
        try {
            String captureFilePath = FileUtility.getPath(mActivity, Uri.fromFile(new File(filePath)));
            responseJsonObj.put(IMAGE_PATH, captureFilePath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mWebview.evaluateJavascript(String.format("javascript:" + callbackName + "(%s)", responseJsonObj), null);
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
            mWebview.evaluateJavascript(String.format("javascript:" + jsResponseData.getCallbackFunction() + "(%s)", responseJsonObj), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            responseJsonObj.put(REQUEST_PARAMS, new JSONObject(jsResponseData.getResponseData()));

            mWebview.evaluateJavascript(String.format("javascript:" + callbackName + "(%s)", responseJsonObj), null);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * compress captured image and save captured image to web html folder
     *
     * @param imageCaptureUri file uri of capture image
     * @param context context
     */
    public void saveImageToSyncHtmlFilesDir(Uri imageCaptureUri, Intent intent) {
        try {
            if (imageCaptureUri == null) {
                Toast.makeText(mActivity, "image capture error.", Toast.LENGTH_SHORT).show();
                return;
            }

            final String sourceFilePath = FileUtility.getPath(mActivity,imageCaptureUri);
            final String destFileName = FileUploadUtility.generateImageFileNameByAppID(FileUtility.getExtensionWithDot(sourceFilePath), jsResponseData.getAppID());

            File to = new File(FileUtility.getHtmlDirFromSandbox(mActivity) + File.separator + destFileName);

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
                            setNativeSelectedPhotoCallbackFunction(compressedPath, offlineID);
                            System.out.println("compressed size : " + new File(compressedPath).length());

                            startImageUpload(compressedPath, offlineID);
                        }
                    });

                } else {

                    boolean isMoved;
                    File uploadFilePath;
                    isMoved = moveFile(from, to);
                    uploadFilePath = isMoved ? to : from;

                    if (!jsResponseData.isWaitForResponse()) {

                        if (intent != null) {
                            final String originalImage = FileUtility.getPath(mActivity,
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
                            setNativeSelectedPhotoCallbackFunction(uploadFilePath.getAbsolutePath(), offlineID, jsResponseData.getCallbackFunction());
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
     * set file name and offline id to response data
     *
     * @param filename name of selected image
     * @param offlineID upload image against offlineID
     * @return jsonObject with filename and offlineID
     */
    private JSONObject setFileNameAndOfflineIDToResponseData(String filename, String offlineID) {
        try {
            JSONObject jsonObject = new JSONObject(jsResponseData.getResponseData());
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
        startImageUpload(filePath, offlineID, jsResponseData.getAppID(), jsResponseData.getSrcImageName(), AppMediaDetails.INSTRUCTION_IMAGE_TYPE);
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
    public void startImageUpload(final String filePath, String offlineID, String appID, String srcName, int imageType) {
        File file = new File(filePath);
        String fileName = FileUtility.getFileNameWithoutExists(filePath);
        final AppMediaDetails appMediaDetails = FileUploadUtility.saveAppMediaDetails(file, offlineID, instructionNumberClockIn,
                TextUtils.isEmpty(srcName) ? fileName : srcName, imageType, caption, mActivity);

        instructionNumberClockIn = appMediaDetails.getInstructionNumber();

        if (appMediaDetails == null) {
            return;
        }
        FileUploader roofingUploader = FileUploader.getInstance(appMediaDetails, mActivity);
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
}
