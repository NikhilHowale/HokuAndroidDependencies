package com.hokuapps.loadnativefileupload.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by user on 5/1/18.
 */

public class DownloadParams implements Parcelable  {
    public static final String TAG = DownloadParams.class.getSimpleName();

    public DownloadParams() {
    }

    public String downloadUrl = "";
    public String originalFileName = "";
    public String downloadId = "";
    public int notificationID = 0;
    public boolean isShowDialog = false;
    public boolean isShowToast = false;
    public boolean isShowPreview =false;
    public boolean isShowNotification;
    public String fileMimeType = "text/plain";
    public boolean isRename = false;
    public String downloadFolderPath = "";

    public String folderName;
    public boolean isSandBoxDir;
    public boolean saveSkipCamera;

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public boolean isRename() {
        return isRename;
    }

    public void setRename(boolean rename) {
        isRename = rename;
    }

    public boolean isShowNotification() {
        return isShowNotification;
    }

    public void setShowNotification(boolean showNotification) {
        isShowNotification = showNotification;
    }

    public String getFileMimeType() {
        return fileMimeType;
    }

    public void setFileMimeType(String fileMimeType) {
        this.fileMimeType = fileMimeType;
    }

    public boolean isShowPreview() {
        return isShowPreview;
    }

    public void setShowPreview(boolean showPreview) {
        isShowPreview = showPreview;
    }

    public boolean isShowDialog() {
        return isShowDialog;
    }

    public void setShowDialog(boolean showDialog) {
        isShowDialog = showDialog;
    }

    public boolean isShowToast() {
        return isShowToast;
    }

    public void setShowToast(boolean showToast) {
        isShowToast = showToast;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }

    public String getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(String downloadId) {
        this.downloadId = downloadId;
    }

    public String getDownloadFolderPath() {
        return downloadFolderPath;
    }

    public void setDownloadFolderPath(String downloadFolderPath) {
        this.downloadFolderPath = downloadFolderPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(downloadUrl);
        parcel.writeString(originalFileName);
        parcel.writeString(downloadId);
        parcel.writeInt(notificationID);
        parcel.writeInt(isShowDialog ? 1 : 0);
        parcel.writeInt(isShowToast ? 1 : 0);
        parcel.writeInt(isShowPreview ? 1 : 0);
        parcel.writeInt(isShowNotification ? 1 : 0);
        parcel.writeInt(isRename ? 1 : 0);
        parcel.writeString(downloadFolderPath);
        parcel.writeString(fileMimeType);

    }

    public static final Creator<DownloadParams> CREATOR = new Creator<DownloadParams>() {
        public DownloadParams createFromParcel(Parcel source) {
            DownloadParams downloadParams = new DownloadParams();
            downloadParams.downloadUrl = source.readString();
            downloadParams.originalFileName = source.readString();
            downloadParams.downloadId = source.readString();
            downloadParams.notificationID = source.readInt();

            downloadParams.isShowDialog = source.readInt() == 1 ? true : false;
            downloadParams.isShowToast = source.readInt() == 1 ? true : false;
            downloadParams.isShowPreview = source.readInt() == 1 ? true : false;
            downloadParams.isShowNotification = source.readInt() == 1 ? true : false;
            downloadParams.isRename = source.readInt() == 1 ? true : false;

            downloadParams.downloadFolderPath = source.readString();
            downloadParams.fileMimeType = source.readString();

            return downloadParams;
        }

        public DownloadParams[] newArray(int size) {
            return new DownloadParams[size];
        }
    };
}
