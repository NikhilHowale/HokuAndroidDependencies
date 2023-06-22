package com.hokuapps.loadnativefileupload.restrequest;

import android.text.TextUtils;

import com.hokuapps.loadnativefileupload.delegate.IUICallBack;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

/**
 * Service request wrapper class.
 */
public class ServiceRequest {

    public int tag;
    private String url;
    private String httpMethod;
    private String contentType;
    private String additionalHTTPBody;
    private Hashtable<String, String> requestParams;
    private Hashtable<String, String> additionalHTTPHeaders;

    private String userId;
    private String groupId;
    private String filePath;
    private String profileKey;

    public boolean canQueue;
    public URI uri;
    private boolean isMediaFileUploader;

    private String appID;
    private String fileName;
    private String responseString;
    private AppMediaDetails appMediaDetails;
    private IUICallBack iuiCallBack;

    public IUICallBack getIuiCallBack() {
        return iuiCallBack;
    }

    public AppMediaDetails getAppMediaDetails() {
        return appMediaDetails;
    }

    public void setAppMediaDetails(AppMediaDetails appMediaDetails) {
        this.appMediaDetails = appMediaDetails;
    }





    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getAdditionalHTTPBody() {
        return additionalHTTPBody;
    }

    public void setAdditionalHTTPBody(String additionalHTTPBody) {
        this.additionalHTTPBody = additionalHTTPBody;
    }


    public String getHTTPMethod() {
        return httpMethod;
    }

    public void setHTTPMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public ServiceRequest() {

    }

    /**
     * Parameterized Constructor
     *
     * @param url
     * @param tag
     */
    public ServiceRequest(String url, int tag) {
        this.url = url;
        this.tag = tag;
    }

    /**
     * Add request parameters
     *
     * @param key
     * @param value
     */
    public void addRequestParameter(String key, String value) {
        if (requestParams == null) {
            requestParams = new Hashtable<String, String>();
        }
        requestParams.put(key, value);
    }

    /**
     * Add Http headers
     *
     * @param key
     * @param value
     */
    public void addHTTPHeader(String key, String value) {
        if (additionalHTTPHeaders == null) {
            additionalHTTPHeaders = new Hashtable<String, String>();
        }
        additionalHTTPHeaders.put(key, value);
    }

    /**
     * Get Http headers.
     */
    public Hashtable<String, String> getHTTPHeaders() {
        return additionalHTTPHeaders;
    }

    /**
     * do request.
     */
    public void doRequest() {
        // TODO: Need to add AsyncTask to send http request
    }

    public URI getUri() {

        URI uri = null;
        try {
            if (!TextUtils.isEmpty(url)) {
                uri = new URI(url);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }



    public boolean isMediaFileUploader() {
        return isMediaFileUploader;
    }

    public void setMediaFileUploader(boolean isMediaFileUploader) {
        this.isMediaFileUploader = isMediaFileUploader;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }
}
