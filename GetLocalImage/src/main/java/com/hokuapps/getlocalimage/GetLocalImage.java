package com.hokuapps.getlocalimage;

import android.app.Activity;
import android.text.TextUtils;
import android.webkit.WebView;

import com.hokuapps.getlocalimage.service.DownloadManager;
import com.hokuapps.getlocalimage.utility.Utility;

import org.json.JSONObject;

public class GetLocalImage {

    final WebView mWebView;
    final Activity mActivity;


    public GetLocalImage(WebView mWebView, Activity mActivity) {
        this.mWebView = mWebView;
        this.mActivity = mActivity;
    }

    /**
     * This method download the image from url to local directory and return to callback
     * @param responseData jsonObject with file url and filename, file mime type
     */
    public void getLocalImage(final String responseData){

        try {

            JSONObject jsonObjRes = new JSONObject(responseData);
            String fileName = Utility.getStringObjectValue(jsonObjRes, "fileName");
            String fileMimeType = Utility.getStringObjectValue(jsonObjRes, "fileMimeType");
            String url = Utility.getStringObjectValue(jsonObjRes, "url");

            String nextCallBackButton = Utility.getStringObjectValue(jsonObjRes, "nextButtonCallback");

            String localFilePath = null;
            JSONObject jsonObjResNew = new JSONObject();
            jsonObjResNew.put("dataDictionay", jsonObjRes);

            if (!TextUtils.isEmpty(fileName)) {
                localFilePath = Utility.getHtmlDirFromSandbox(mActivity).getAbsolutePath() + "/" + fileName;
                if (!Utility.isFileExist(localFilePath)) {

                    startDownload((int) System.currentTimeMillis(), url,
                            fileMimeType, fileName);
                } else {
                    jsonObjResNew.put("localFilePath", localFilePath);
                }
            }

            // call next callback button
            Utility.callJavaScriptFunction(mActivity, mWebView,
                    nextCallBackButton,
                    jsonObjResNew);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This method start download file to local directory
     * @param downloadID  download ID
     * @param urlToDownload url of image
     * @param fileMimeType file mime type
     * @param originalFileName file name
     */
    private void startDownload(int downloadID, String urlToDownload, String fileMimeType, String originalFileName) {
        DownloadManager downloadManager = DownloadManager.getInstance(mActivity, downloadID);

        if (downloadManager.isRunningTask()) return;

        downloadManager.startDownload(downloadID, urlToDownload, Utility.getDownloadFileParentDir("", true, mActivity),
                false, false, fileMimeType, false, false, originalFileName,true);
    }
}
