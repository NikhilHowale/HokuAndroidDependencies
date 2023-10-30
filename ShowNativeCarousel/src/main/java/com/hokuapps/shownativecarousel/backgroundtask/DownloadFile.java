package com.hokuapps.shownativecarousel.backgroundtask;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import com.hokuapps.shownativecarousel.utility.Utility;

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

    /**
     * invoked on the UI thread before the task is executed.
     * This step is normally used to setup the task, for instance by showing a progress bar in the user interface.
     */
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


    /**
     *  invoked on the background thread immediately after onPreExecute() finishes executing.
     *  This step is used to perform background computation that can take a long time
     * @param voids parameters sent to the task upon execution.
     */
    @Override
    protected Boolean doInBackground(Void... voids) {
        String fileUrl = downloadUrl;
        boolean isDownloaded = true;

        try {

            String destination = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

            NewApiFilePath = new File(destination, filePath.getName());

            if (!isForceDownload)
                if ((NewApiFilePath.exists() && NewApiFilePath.length() > 0)) {
                    return true;
                }

            final Uri uri = Uri.parse("file://" + NewApiFilePath);
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(uri.getPath());
            int lengthOfFile = urlConnection.getContentLength();

            byte[] buffer = new byte[MEGABYTE];
            int bufferLength;
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


    /**
     * invoked on the UI thread after a call to publishProgress(Progress).
     * The timing of the execution is undefined.
     * This method is used to display any form of progress in the user interface while the background computation is still executing.
     * @param values The progress values to update the UI with
     */
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (downloadCallback != null && downloadCallback.get() != null) {
            downloadCallback.get().onDownloadProgressUpdate(values[0]);
        }
    }


    /**
     * invoked on the UI thread after the background computation finishes.
     * The result of the background computation is passed to this step as a parameter.
     * @param result  result of the background computation
     */
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


    /**
     * Delete the given file
     * @param file file to be deleted
     */
    private void deleteFile(File file) {
        assert file != null;
        file.delete();
    }


    /**
     * get the number of file given file directory and file name
     * @param downLoadedFolderDir folder/directory path
     * @param fileNameWithoutExtension file name
     */
    private int getFileCountByFileName(File downLoadedFolderDir, String fileNameWithoutExtension) {

        if (downLoadedFolderDir != null) {
            File[] arrFile = downLoadedFolderDir.listFiles();

            int count = 0;
            assert arrFile != null;
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

    /**
     * callback interface listening to download status
     */
    public interface DownloadCallback {
        void onDownloadStatus(boolean isDownloaded, String filePath);

        void onDownloadStarted(String filePath);

        void onDownloadProgressUpdate(String status);
    }
}
