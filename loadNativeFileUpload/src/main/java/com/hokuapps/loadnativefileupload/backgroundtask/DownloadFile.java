package com.hokuapps.loadnativefileupload.backgroundtask;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.hokuapps.loadnativefileupload.models.DownloadParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 11/4/17.
 */
public class DownloadFile extends AsyncTask<Void, String, Boolean> {

    private static final int MEGABYTE = 1024 * 4;
    private WeakReference<DownloadCallback> downloadCallback = null;
    private File filePath = null;
    private File NewApiFilePath = null;
    private DownloadParams downloadParams;

    private String originalFileName = "";
    private String downloadUrl = "";

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public DownloadParams getDownloadParams() {
        return downloadParams;
    }

    public void setDownloadParams(DownloadParams downloadParams) {
        this.downloadParams = downloadParams;
    }

    public void setDownloadCallback(DownloadCallback downloadCallback) {
        this.downloadCallback = new WeakReference<DownloadCallback>(downloadCallback);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        String fn = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
        String fileName = fn.replace(" ", "%20");

        if (!TextUtils.isEmpty(originalFileName)) {
            fileName = originalFileName;
        }

        File downLoadedFolderDir = new File(downloadParams.downloadFolderPath);


        filePath = new File(downLoadedFolderDir, fileName);

        if (downloadCallback != null && downloadCallback.get() != null) {
            downloadCallback.get().onDownloadStarted(filePath.getAbsolutePath());
        }

    }

    @Override
    protected Boolean doInBackground(Void... urls) {
        String fileUrl = downloadParams.downloadUrl;
        boolean isDownloaded = true;

        try {

            String destination = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

            NewApiFilePath = new File(destination, filePath.getName());


            if ((NewApiFilePath.exists() && NewApiFilePath.length() > 0)) {
                return true;
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
                    publishProgress(""+(int)((total * 100)/lengthOfFile));
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

    private void deleteFile(File file) {
        if (file == null) return;

        file.delete();
    }

    /**
     * This method update downloaded file using its name
     * @param isDownloaded if true then rename other wise false
     * @param downloadedFilePath new downloaded file path
     * @param originalFilepath original File path
     */
    public static void updateDownloadedFileByName(boolean isDownloaded, String downloadedFilePath, String originalFilepath) {

        File file = new File(downloadedFilePath);

        if (isDownloaded) {
            file.renameTo(new File(originalFilepath));
        } else {
            file.delete();
        }
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
                downloadCallback.get().onDownloadStatus(result, NewApiFilePath.getAbsolutePath(), NewApiFilePath.getAbsolutePath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public interface DownloadCallback {
        void onDownloadStatus(boolean isDownloaded, String downloadedFilePath, String originalFilePath);
        void onDownloadStarted(String filePath);
        void onDownloadProgressUpdate(String status);
    }

}