package com.hokuapps.loadnativefileupload.models;

import android.content.Context;
import android.util.Log;


import com.hokuapps.loadnativefileupload.constants.FileUploadConstant;
import com.hokuapps.loadnativefileupload.dao.AppMediaDetailsDAO;

/**
 * Created by user on 29/12/16.
 */
public class AppMediaDetails {
    private static final String TAG = AppMediaDetails.class.getSimpleName();
    public static final int FILE_TYPE = 4;
    private long row_id;
    private String offlineDataID;
    private String fileName;
    private String fileSizeBytes;
    private String uploadDate;
    private String mediaID;
    private String s3FilePath;
    private int uploadStatus;
    private int instructionNumber;
    private int imageType;

    private String filePath;
    private String appID;
    private String imageCaption;
    public static final int UPLOAD_SUCCESS = 1;
    public static final int UPLOAD_FAILED = 0;

    public static final int INSTRUCTION_IMAGE_TYPE = 1;
    public static final int MAP_IMAGE_TYPE = 2;
    public static final int MAP_PLAN_IMAGE_TYPE = 3;
    public static final int UPLOAD_INPROGRESS = 2;

    public AppMediaDetails() {
        row_id = -1;
    }

    public long getRow_id() {
        return row_id;
    }

    public void setRow_id(long row_id) {
        this.row_id = row_id;
    }

    public String getOfflineDataID() {
        return offlineDataID;
    }

    public void setOfflineDataID(String offlineDataID) {
        this.offlineDataID = offlineDataID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(String fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getMediaID() {
        return mediaID;
    }

    public void setMediaID(String mediaID) {
        this.mediaID = mediaID;
    }

    public String getS3FilePath() {
        return s3FilePath;
    }

    public void setS3FilePath(String s3FilePath) {
        this.s3FilePath = s3FilePath;
    }

    public int getUploadStatus() {
        return uploadStatus;
    }

    public boolean isUploadStatus() {
        return uploadStatus == 1 ? true : false;
    }

    public void setUploadStatus(int uploadStatus) {
        this.uploadStatus = uploadStatus;
    }

    public int getInstructionNumber() {
        return instructionNumber;
    }

    public void setInstructionNumber(int instructionNumber) {
        this.instructionNumber = instructionNumber;
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

    public int getImageType() {
        return imageType;
    }

    public void setImageType(int imageType) {
        this.imageType = imageType;
    }

    public String getImageCaption() {
        return imageCaption;
    }

    public void setImageCaption(String imageCaption) {
        this.imageCaption = imageCaption;
    }

    public void save(Context context) {
        AppMediaDetailsDAO appMediaDetailsDAO = null;

        try {
            appMediaDetailsDAO = new AppMediaDetailsDAO(context, this);

            //check object is null
            if (appMediaDetailsDAO == null) return;

            // insert new record if rowId is -1
            if (this.row_id == FileUploadConstant.INVALID_ID) {
                this.row_id = appMediaDetailsDAO.save();
            } else { // update existing record
                appMediaDetailsDAO.update();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}