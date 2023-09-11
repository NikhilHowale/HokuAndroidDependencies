package com.hokuapps.loadnativefileupload.backgroundtask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;


import com.hokuapps.loadnativefileupload.constants.FileUploadConstant;
import com.hokuapps.loadnativefileupload.dao.AppMediaDetailsDAO;
import com.hokuapps.loadnativefileupload.delegate.OnUploadListener;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;
import com.hokuapps.loadnativefileupload.models.Error;

import com.hokuapps.loadnativefileupload.restrequest.ServiceRequest;
import com.hokuapps.loadnativefileupload.restrequest.ServiceResponse;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;
import com.hokuapps.loadnativefileupload.utilities.FileUtility;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Created by user on 30/12/16.
 */
public class FileUploaderAsyncTask extends AsyncTask<ServiceRequest, Integer, ServiceResponse> {

    private final static int EXCEPTION_CODE = 400;
    public ServiceRequest serviceRequest;
    private String TAG = FileUploaderAsyncTask.class.getSimpleName();
    private int requestTimeout;
    private WeakReference<OnUploadListener> uploadListenerWeakReference;
    private Context context;

    public FileUploaderAsyncTask(int requestTimeout, OnUploadListener uploadListenerWeakReference, Context context) {

        this.requestTimeout = requestTimeout;
        this.uploadListenerWeakReference = new WeakReference<OnUploadListener>(uploadListenerWeakReference);
        this.context = context;
    }

    @SuppressLint("NewApi")
    @Override
    protected ServiceResponse doInBackground(ServiceRequest... params) {
        this.serviceRequest = params[0];
        ServiceResponse serviceResponse = new ServiceResponse();

        if (this.serviceRequest == null) {
            return serviceResponse;
        }

        printRequestDetails();

        try {
           Log.e(TAG , " :: doInBackground : Request URL : " + this.serviceRequest.getUrl());
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = null;

            HttpPost httpPost = null;
            String responseString = "";

            URL url = new URL(this.serviceRequest.getUrl());
            URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
            Hashtable<String, String> httpHeaders = this.serviceRequest.getHTTPHeaders();
            httpPost = new HttpPost(serviceRequest.getUrl());

            addRequestHeaders(httpPost, httpHeaders);

            if (serviceRequest.getFilePath() != null) {
                String filePath = serviceRequest.getFilePath();

                FileEntity fileEntity = new FileEntity(new File(filePath), "application/octet-stream") {
                    @Override
                    public void writeTo(OutputStream outstream) throws IOException {
                        super.writeTo(new ProgressiveOutputStream(outstream, getContentLength()));
                    }

                    @Override
                    public long getContentLength() {
                        return super.getContentLength();
                    }
                };
                fileEntity.setChunked(true);
                fileEntity.setContentType("application/octet-stream");
                httpPost.setEntity(fileEntity);
                response = httpclient.execute(httpPost);
            }

            // check the response.
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
              Log.e(TAG ,"Status Ok : " + HttpStatus.SC_OK);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                serviceResponse.setResponseString(responseString);
                out.flush();
                out.close();
                out = null;

            } else {
                //Closes the connection.
                Log.e(TAG , "Problem : ");
                response.getEntity().getContent().close();
                serviceResponse.setErrorMsg(statusLine.getReasonPhrase());
                serviceResponse.setResponseCode(statusLine.getStatusCode());
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception e) {
            serviceResponse.setErrorMsg(e.getMessage());
            serviceResponse.setResponseCode(EXCEPTION_CODE);
         Log.e(TAG , "Exception : " + e.getMessage());
        }
        return serviceResponse;
    }

    private void printRequestDetails() {
        Log.e(TAG ,"Request URL : " + serviceRequest.getUrl());
        Log.e(TAG ,"Body : " + serviceRequest.getAdditionalHTTPBody());
        System.out.println(TAG + " Request URL : " + serviceRequest.getUrl() + "\nBody : " + serviceRequest.getAdditionalHTTPBody());
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPostExecute(ServiceResponse result) {
        if (isCancelled()
                || TextUtils.isEmpty(result.getResponseString())
                || !TextUtils.isEmpty(result.getErrorMsg())) {
            Log.e(TAG ,"::onPostExecute - " + result.getErrorMsg());
           Log.e(TAG ," :: onPostExecute : roofing Uploader failed error : " + result.getErrorMsg());
            System.out.println(TAG + " onPostExecute : roofing Uploader failed error : " + result.getErrorMsg());
            Error error = new Error(result.getResponseCode(), result.getErrorMsg());

            if (uploadListenerWeakReference.get() != null) {
                uploadListenerWeakReference.get().onUploadFinish(this.serviceRequest, error);
            }

            return;
        }

        //save app media details
        parseRoofingPhotoResponse(result);

    }

    /**
     * Parse the roofing media photo response
     *
     * @param result, response of service
     */
    private void parseRoofingPhotoResponse(ServiceResponse result) {
        try {
            System.out.println(TAG + " :: upload Completed " + result.getResponseString());
            Error error = null;
            JSONObject jsonObject = new JSONObject(result.getResponseString());
            int statusCode = jsonObject.getInt(FileUploadConstant.AuthIO.STATUS_CODE);
            AppMediaDetailsDAO appMediaDetailsDAO = new AppMediaDetailsDAO(context);

            if (statusCode == 0) {

                String filename = (String) FileUploadUtility.getJsonObjectValue(jsonObject, "originalFileName");
                String mediaID = (String) FileUploadUtility.getJsonObjectValue(jsonObject, "mediaID");
                String s3FilePath = (String) FileUploadUtility.getJsonObjectValue(jsonObject, "s3FilePath");

                if (TextUtils.isEmpty(filename)) return;

                AppMediaDetails appMediaDetails = appMediaDetailsDAO.getStoredAppMediaDetails(context, filename);

                if (appMediaDetails != null) {
                    appMediaDetails.setMediaID(mediaID);
                    appMediaDetails.setS3FilePath(s3FilePath);
                    appMediaDetails.setUploadDate(FileUtility.convertToUTCTimeZone(FileUploadUtility.getCurrentDateTimeInMS()));
                    appMediaDetails.setUploadStatus(AppMediaDetails.UPLOAD_SUCCESS);
                    appMediaDetails.save(context);
                    this.serviceRequest.setAppMediaDetails(appMediaDetails);
                }

            } else {
                error = Error.createError(jsonObject);
                AppMediaDetails appMediaDetails = appMediaDetailsDAO.getStoredAppMediaDetails(context, this.serviceRequest.getFileName());
                appMediaDetails.setUploadStatus(AppMediaDetails.UPLOAD_FAILED);
                appMediaDetails.save(context);
                this.serviceRequest.setAppMediaDetails(appMediaDetails);
            }

            if (this.uploadListenerWeakReference.get() != null) {
                this.uploadListenerWeakReference.get().onUploadFinish(this.serviceRequest, error);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void addRequestHeaders(HttpPost httpPost, Hashtable<String, String> httpHeaders) {
        // add the headers
        if (httpHeaders != null) {
            Enumeration<String> allHeaders = httpHeaders.keys();
            while (allHeaders.hasMoreElements()) {
                String key = allHeaders.nextElement().trim();
                String value = httpHeaders.get(key).trim();
                httpPost.addHeader(key, value);
            }

            httpPost.addHeader("gzip", "Accept-Encoding");
        }
    }

    /**
     * Calculate progress update
     *
     * @param byteUploaded
     * @param fileLength
     * @return progress
     */
    private int getProgressUpdate(long byteUploaded, long fileLength) {
        // circle.
        int per = Math.round(((float) byteUploaded / fileLength) * 100);
        //		return (int)((byteUploaded/fileLength)*100);
        return per;
    }

    class ProgressiveOutputStream extends FilterOutputStream {
        int totalByteWrite = 0;
        long totalSize = 0;

        public ProgressiveOutputStream(final OutputStream out, final long totalSize) {
            super(out);
            this.totalSize = totalSize;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            System.out.println("Written 1 byte");
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
            System.out.println("Written " + b.length + " bytes");
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            System.out.println("Written " + len + " bytes");
            totalByteWrite += len;
            publishProgress(getProgressUpdate(totalByteWrite, totalSize));
        }
    }
}
