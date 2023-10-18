package com.hokuapps.getlocalimage.backgroundtask;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.hokuapps.getlocalimage.utility.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFile extends AsyncTask<Void, String, Boolean> {

    private static final int MEGABYTE = 1024 * 4;
    private File filePath = null;
    private File NewApiFilePath = null;

    private String downloadFolderPath = "";
    private String downloadUrl = "";
    private int downloadId = 0;
    private boolean isShowDialog = false;
    private boolean isShowPreview = false;
    private String fileMimeType = "text/plain";
    private boolean isRename = false;
    private boolean isShowNotification;
    private String originalFileName = "";
    private boolean isShowToast = false;
    private boolean isForceDownload = false;

    public void setForceDownload(boolean forceDownload) {
        this.isForceDownload = forceDownload;
    }
    private WeakReference<DownloadCallback> downloadCallback = null;

    public void setDownloadFolderPath(String downloadFolderPath) {
        this.downloadFolderPath = downloadFolderPath;
    }
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }
    public void setShowDialog(boolean showDialog) {
        isShowDialog = showDialog;
    }
    public void setShowPreview(boolean showPreview) {
        isShowPreview = showPreview;
    }
    public void setFileMimeType(String fileMimeType) {
        this.fileMimeType = fileMimeType;
    }
    public void setShowNotification(boolean showNotification) {
        isShowNotification = showNotification;
    }
    public void setRename(boolean rename) {
        isRename = rename;
    }
    public void setShowToast(boolean showToast) {
        isShowToast = showToast;
    }
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    public void setDownloadCallback(DownloadCallback downloadCallback) {
        this.downloadCallback = new WeakReference<>(downloadCallback);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        String fn = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
        String fileName = fn.replace(" ", "%20");

        if (!TextUtils.isEmpty(originalFileName)) {
            fileName = originalFileName;
        }


        File downLoadedFolderDir = new File(downloadFolderPath);

        if (isRename) {
            //need to change file name if already exists
            String fileNameWithoutExt = Utility.getFileNameWithoutExtension(fileName);
            int numOfOccurrence = getFileCountByFileName(downLoadedFolderDir, fileNameWithoutExt);
            if (numOfOccurrence > 0) {
                String ext = Utility.getExtensionWithDot(fileName);
                fileName = String.format("%s(%d)%s", fileNameWithoutExt, numOfOccurrence, ext);
            }
        }

        filePath = new File(downLoadedFolderDir, fileName);

        if (downloadCallback != null && downloadCallback.get() != null) {
            downloadCallback.get().onDownloadStarted(filePath.getAbsolutePath());
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        String fileUrl = downloadUrl;
        boolean isDownloaded = true;

        try {

            //check file is exist and length is not zero

            // For Api 30 change
            String destination = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

            NewApiFilePath = new File(destination, filePath.getName());

            if (!isForceDownload)
                if ((NewApiFilePath.exists() && NewApiFilePath.length() > 0)) {
                    return isDownloaded;
                }

            final Uri uri = Uri.parse("file://" + NewApiFilePath);
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(new File(uri.getPath()));
            int lengthOfFile = urlConnection.getContentLength();

            byte[] buffer = new byte[MEGABYTE];
            int bufferLength = 0;
            int total = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                if (!isCancelled()) {
                    total += bufferLength;
                    fileOutputStream.write(buffer, 0, bufferLength);
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                } else {
                    isDownloaded = false;
                    break;
                }
            }

            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            isDownloaded = false;
            e.printStackTrace();
        }

        return isDownloaded;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (downloadCallback != null && downloadCallback.get() != null) {
            downloadCallback.get().onDownloadProgressUpdate(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        try {
            if (downloadCallback != null && downloadCallback.get() != null && NewApiFilePath != null) {
                if (!result) {
                    deleteFile(NewApiFilePath);
                }
                downloadCallback.get().onDownloadStatus(result, NewApiFilePath.getAbsolutePath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteFile(File file) {
        if (file == null) ;
        file.delete();
    }


    /**
     * This method count the number of file by name
     * @param downLoadedFolderDir sandbox directory
     * @param fileNameWithoutExtension file name without extension
     * @return return count
     */
    private int getFileCountByFileName(File downLoadedFolderDir, String fileNameWithoutExtension) {

        if (downLoadedFolderDir != null) {
            File[] arrFile = downLoadedFolderDir.listFiles();

            int count = 0;
            for (File file : arrFile) {
                if (Utility.getFileName(file.getAbsolutePath())
                        .startsWith(fileNameWithoutExtension)) {
                    count++;
                }
            }

            return count;
        }
        return 0;
    }


    public interface DownloadCallback {
        void onDownloadStatus(boolean isDownloaded, String filePath);

        void onDownloadStarted(String filePath);

        void onDownloadProgressUpdate(String status);
    }
}
