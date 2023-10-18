package com.hokuapps.getlocalimage.service;

import android.content.Context;
import android.os.AsyncTask;

import com.hokuapps.getlocalimage.backgroundtask.DownloadFile;

import java.util.HashMap;

public class DownloadManager implements DownloadFile.DownloadCallback{

    private static DownloadManager downloadManager;
    private static HashMap<Integer, DownloadManager> downloaderMap = new HashMap<Integer, DownloadManager>();
    private static Context mContext;
    private DownloadFile downloadFile;

    public static DownloadManager getInstance(Context context, int downloadID) {
        downloadManager = getRunningDownloaderRef(downloadID);
        if (downloadManager == null) {
            downloadManager = new DownloadManager();
            downloaderMap.put(downloadID, downloadManager);
        }
        mContext = context;
        return downloadManager;
    }

    public static DownloadManager getRunningDownloaderRef(int downloadID) {
        return downloaderMap.get(downloadID);
    }

    public boolean isRunningTask() {
        if (downloadFile != null
                && !downloadFile.isCancelled()) {
            return true;
        }

        return false;
    }


    /**
     * This method download the image from url
     * @param downloadID download id
     * @param mUrl image url
     * @param folderPath sandbox folder path
     * @param isShowDialog show dialog
     * @param isShowPreview show preview
     * @param fileMimeType file mim type
     * @param isShowNotification flag to show notification
     * @param isRename flag to change name
     * @param originalFileName original file name
     * @param isShowToast flag to show toast
     */
    public void startDownload(int downloadID, String mUrl, String folderPath,
                              boolean isShowDialog, boolean isShowPreview, String fileMimeType,
                              boolean isShowNotification, boolean isRename, String originalFileName,
                              boolean isShowToast) {

        try {
            if (isRunningTask()) return;

            if (downloadFile == null) {
                downloadFile = new DownloadFile();
                downloadFile.setDownloadFolderPath(folderPath);
                downloadFile.setDownloadUrl(mUrl);
                downloadFile.setDownloadId(downloadID);
                downloadFile.setShowDialog(isShowDialog);
                downloadFile.setShowPreview(isShowPreview);
                downloadFile.setFileMimeType(fileMimeType);
                downloadFile.setShowNotification(isShowNotification);
                downloadFile.setRename(isRename);
                downloadFile.setShowToast(isShowToast);
                downloadFile.setOriginalFileName(originalFileName);
                downloadFile.setDownloadCallback(this);

                downloadFile.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDownloadStatus(boolean isDownloaded, String filePath) {

    }

    @Override
    public void onDownloadStarted(String filePath) {

    }

    @Override
    public void onDownloadProgressUpdate(String status) {

    }
}
