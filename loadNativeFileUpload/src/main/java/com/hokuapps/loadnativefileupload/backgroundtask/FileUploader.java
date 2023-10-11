package com.hokuapps.loadnativefileupload.backgroundtask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.hokuapps.loadnativefileupload.NativeFileUpload;
import com.hokuapps.loadnativefileupload.constants.FileUploadConstant;

import com.hokuapps.loadnativefileupload.dao.AppMediaDetailsDAO;
import com.hokuapps.loadnativefileupload.delegate.OnUploadListener;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
import com.hokuapps.loadnativefileupload.models.Error;
import com.hokuapps.loadnativefileupload.restrequest.ServiceRequest;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class FileUploader implements OnUploadListener {

    private static HashMap<String, FileUploader> profileUploaderMap = new HashMap<String, FileUploader>();

    private FileUploaderAsyncTask fileUploaderAsyncTask;
    private ArrayList<WeakReference<OnUploadListener>> listeners = new ArrayList<WeakReference<OnUploadListener>>();
    private IUICallBackRoofing uiCallBack;
    private String filePath;
    private AppMediaDetails appMediaDetails;
    private String appID;
    private String appsServerToken;

    private Context context;

    String authToken;

    private FileUploader(AppMediaDetails appMediaDetails, Context context) {
        this.appMediaDetails = appMediaDetails;
        this.context = context;
    }

    /**
     * Make singleton instance of object
     *
     * @return ProfileUploader
     */
    public static FileUploader getInstance(AppMediaDetails appMediaDetails, Context mContext) {
        FileUploader roofingUploader = null;

        try {
            if (appMediaDetails == null) {
                throw new NullPointerException("appMediaDetails object is null");
            }

            roofingUploader = getRunningRoofingUploaderRef(appMediaDetails);

            if (roofingUploader == null) {
                roofingUploader = new FileUploader(appMediaDetails, mContext);
                profileUploaderMap.put(appMediaDetails.getFileName(), roofingUploader);
            } else {
                roofingUploader.appMediaDetails = appMediaDetails;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return roofingUploader;
    }

    public static FileUploader getRunningRoofingUploaderRef(AppMediaDetails appMediaDetails) {
        return profileUploaderMap.get(appMediaDetails.getFileName());
    }

    /**
     * Perform operation for uploading profile or group photo to server
     */
    public void startUpload() {

        if (TextUtils.isEmpty(filePath)) {
            throw new NullPointerException("File path is null");
        } else if (TextUtils.isEmpty(appID)) {
            throw new NullPointerException("appID key is null");
        }

        executeTask();
    }

    private void remove(String groupId) {
        profileUploaderMap.remove(groupId);
    }

    /**
     * Stop the process to upload
     */
    public void stopUpload() {
        if (fileUploaderAsyncTask != null) {
            if (!fileUploaderAsyncTask.isCancelled()) {
                // Cancel task
                fileUploaderAsyncTask.cancel(true);
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private void executeTask() {
        if (fileUploaderAsyncTask == null) {

            ServiceRequest serviceRequest = new ServiceRequest();

            if (!TextUtils.isEmpty(NativeFileUpload.APP_FILE_URL)) {
                serviceRequest.setUrl(NativeFileUpload.APP_FILE_URL + File.separator + FileUploadConstant.REST_URLS.UPLOAD_MP_HELITRACK);
            }

            serviceRequest.setFilePath(filePath);
            serviceRequest.setAppID(appID);
            serviceRequest.setFileName(appMediaDetails.getFileName());
            serviceRequest.setAppMediaDetails(appMediaDetails);
            //set headers
            serviceRequest.addHTTPHeader(FileUploadConstant.AuthIO.AUTH_TOKEN.toLowerCase(), appsServerToken);
            //serviceRequest.addHTTPHeader("Authorization", authToken);
            serviceRequest.addHTTPHeader("key", appID);
            serviceRequest.addHTTPHeader("filename", appMediaDetails.getFileName());

            fileUploaderAsyncTask = new FileUploaderAsyncTask(300000, this, context);

            fileUploaderAsyncTask.execute(serviceRequest);
        }
    }

    @Override
    public void onUploadFinish(ServiceRequest request, Error error) {
        for (WeakReference<OnUploadListener> weakReferenceListener : listeners) {
            if (weakReferenceListener.get() != null) {
                weakReferenceListener.get().onUploadFinish(request, error);
            }
        }

        if (error != null) {

            try {
                AppMediaDetailsDAO.updateUploadedFileStatus(request.getFileName(), AppMediaDetails.UPLOAD_FAILED);
                request.setAppMediaDetails(appMediaDetails);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.uiCallBack.onFailure(request);

        } else if (request != null) {
            this.uiCallBack.onSuccess(request);
            remove(request.getFileName());
        }

        fileUploaderAsyncTask = null;
        listeners.clear();
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getAppsServerToken() {
        return appsServerToken;
    }

    public void setAppsServerToken(String appsServerToken) {
        this.appsServerToken = appsServerToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setUiCallBack(IUICallBackRoofing uiCallBack) {
        this.uiCallBack = uiCallBack;
    }

    public interface IUICallBackRoofing {

        void onSuccess(ServiceRequest serviceRequest);

        void onFailure(ServiceRequest serviceRequest);

    }
}
