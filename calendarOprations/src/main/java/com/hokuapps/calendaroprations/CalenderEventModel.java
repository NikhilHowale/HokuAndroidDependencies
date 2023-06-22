package com.hokuapps.calendaroprations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CalenderEventModel {

    @SerializedName("tokenKey")
    @Expose
    private String tokenKey;


    @SerializedName("secretKey")
    @Expose
    private String secretKey;
    @SerializedName("queryMode")
    @Expose
    private String queryMode;
    @SerializedName("ajaXCallURL")
    @Expose
    private String ajaXCallURL;
    @SerializedName("callUrl")
    @Expose
    public String callUrl;
    @SerializedName("pageTitle")
    @Expose
    public String pageTitle;
    @SerializedName("nextButtonTitle")
    @Expose
    public String nextButtonTitle;
    @SerializedName("nextRedirectionURL")
    @Expose
    public String nextRedirectionURL;
    @SerializedName("nextButtonCallback")
    @Expose
    public String nextButtonCallback;
    @SerializedName("isReadOnly")
    @Expose
    public Boolean isReadOnly;
    @SerializedName("offlineDataID")
    @Expose
    public String offlineDataID;
    @SerializedName("callbackFunction")
    @Expose
    public String callbackFunction;
    @SerializedName("action")
    @Expose
    public String action;
    @SerializedName("notes")
    @Expose
    public String notes;
    @SerializedName("endDate")
    @Expose
    public long endDate;
    @SerializedName("startDate")
    @Expose
    public long startDate;

    @SerializedName("updatedStartDate")
    @Expose
    public long updatedStartDate;
    @SerializedName("updatedEndDate")
    @Expose
    public long updatedEndDate;
    @SerializedName("eventTitle")
    @Expose
    public String eventTitle;


    public long getUpdatedStartDate() {
        return updatedStartDate;
    }

    public void setUpdatedStartDate(long updatedStartDate) {
        this.updatedStartDate = updatedStartDate;
    }

    public long getUpdatedEndDate() {
        return updatedEndDate;
    }

    public void setUpdatedEndDate(long updatedEndDate) {
        this.updatedEndDate = updatedEndDate;
    }
    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(String tokenKey) {
        this.tokenKey = tokenKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getQueryMode() {
        return queryMode;
    }

    public void setQueryMode(String queryMode) {
        this.queryMode = queryMode;
    }

    public String getAjaXCallURL() {
        return ajaXCallURL;
    }

    public void setAjaXCallURL(String ajaXCallURL) {
        this.ajaXCallURL = ajaXCallURL;
    }

    public String getCallUrl() {
        return callUrl;
    }

    public void setCallUrl(String callUrl) {
        this.callUrl = callUrl;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getNextButtonTitle() {
        return nextButtonTitle;
    }

    public void setNextButtonTitle(String nextButtonTitle) {
        this.nextButtonTitle = nextButtonTitle;
    }

    public String getNextRedirectionURL() {
        return nextRedirectionURL;
    }

    public void setNextRedirectionURL(String nextRedirectionURL) {
        this.nextRedirectionURL = nextRedirectionURL;
    }

    public String getNextButtonCallback() {
        return nextButtonCallback;
    }

    public void setNextButtonCallback(String nextButtonCallback) {
        this.nextButtonCallback = nextButtonCallback;
    }

    public Boolean getIsReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(Boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public String getOfflineDataID() {
        return offlineDataID;
    }

    public void setOfflineDataID(String offlineDataID) {
        this.offlineDataID = offlineDataID;
    }

    public String getCallbackFunction() {
        return callbackFunction;
    }

    public void setCallbackFunction(String callbackFunction) {
        this.callbackFunction = callbackFunction;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(Integer endDate) {
        this.endDate = endDate;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(Integer startDate) {
        this.startDate = startDate;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }
}
