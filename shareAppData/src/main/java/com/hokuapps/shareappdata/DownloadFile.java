package com.hokuapps.shareappdata;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadFile extends AsyncTask<Void, String, Boolean> {

    private static final int MEGABYTE = 1024 * 4;
    private WeakReference<DownloadCallback> downloadCallback = null;
    private File filePath = null;
    private String downloadUrl = "";
    private String downloadFolderPath = "";
    private int downloadId = 0;
    private boolean isShowDialog = false;
    private boolean isShowPreview =false;
    private boolean isShowToast =false;
    private boolean isShowNotification;
    private String fileMimeType = "text/plain";
    private boolean isRename = false;

    private String originalFileName = "";

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

    public String getDownloadFolderPath() {
        return downloadFolderPath;
    }

    public void setDownloadFolderPath(String downloadFolderPath) {
        this.downloadFolderPath = downloadFolderPath;
    }

    private File NewApifilePath = null;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public boolean isShowToast() {
        return isShowToast;
    }

    public void setShowToast(boolean showToast) {
        isShowToast = showToast;
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
            String fileNameWithoutExt = FileUtility.getFileNameWithoutExtension(fileName);
            int numOfOccurence = getFileCountByFileName(downLoadedFolderDir, fileNameWithoutExt);
            if (numOfOccurence > 0) {
                String ext = FileUtility.getExtensionWithDot(fileName);
                fileName = String.format("%s(%d)%s", fileNameWithoutExt, numOfOccurence, ext);
            }
        }

        filePath = new File(downLoadedFolderDir, fileName);

        if (downloadCallback != null && downloadCallback.get() != null) {
            downloadCallback.get().onDownloadStarted(filePath.getAbsolutePath());
        }
    }

    @Override
    protected Boolean doInBackground(Void... urls) {
        String fileUrl = downloadUrl;
        boolean isDownloaded = true;

        try {

            //check file is exist and length is not zero

            // For Api 30 change
            String destination = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

            NewApifilePath = new File(destination, filePath.getName());


            final Uri uri = Uri.parse("file://" + NewApifilePath);
            // filePath.createNewFile();
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(new File(uri.getPath()));
            int lenghtOfFile = urlConnection.getContentLength();

            byte[] buffer = new byte[MEGABYTE];
            int bufferLength = 0;
            int total = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                if (!isCancelled()) {
                    total += bufferLength;
                    fileOutputStream.write(buffer, 0, bufferLength);
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                } else {
                    isDownloaded = false;
                    break;
                }
            }

            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            isDownloaded = false;
            e.printStackTrace();
        } catch (MalformedURLException e) {
            isDownloaded = false;
            e.printStackTrace();
        } catch (IOException e) {
            isDownloaded = false;
            e.printStackTrace();
        }

        return isDownloaded;
    }

    private void deleteFile(File file) {
        if (file != null)
        file.delete();
    }

    private int getFileCountByFileName(File downLoadedFolderDir, String fileNameWithoutExtension) {

        if (downLoadedFolderDir != null) {
            File[] arrFile = downLoadedFolderDir.listFiles();

            int count = 0;
            if (arrFile != null) {
                for (File file : arrFile) {
                    if (FileUtility.getFileName(file.getAbsolutePath())
                            .startsWith(fileNameWithoutExtension)) {
                        count ++;
                    }
                }
            }

            return count;
        }
        return 0;
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
            if (downloadCallback != null && downloadCallback.get() != null && NewApifilePath != null) {
                if (!result) {
                    deleteFile(NewApifilePath);
                }
                downloadCallback.get().onDownloadStatus(result, NewApifilePath.getAbsolutePath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public interface DownloadCallback {
        void onDownloadStatus(boolean isDownloaded, String filePath);
        void onDownloadStarted(String filePath);
        void onDownloadProgressUpdate(String status);
    }

}
