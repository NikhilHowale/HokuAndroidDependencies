package com.hokuapps.loadnativefileupload.restrequest;

import android.text.TextUtils;

import com.hokuapps.loadnativefileupload.delegate.IUICallBack;

import org.w3c.dom.Element;

/**
 * Class contains service response details
 * for activity
 */
public class ServiceResponse {

    // class members
    private String errorMsg;
    private int serviceRequestTag;
    private String responseString;
    private IUICallBack iuiCallBack;
//    private Message message;
    private int responseCode;




    public IUICallBack getIuiCallBack() {
        return iuiCallBack;
    }

    public void setIuiCallBack(IUICallBack iuiCallBack) {
        this.iuiCallBack = iuiCallBack;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public int getServiceRequestTag() {
        return serviceRequestTag;
    }

    public void setServiceRequestTag(int serviceRequestTag) {
        this.serviceRequestTag = serviceRequestTag;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }

    public Element getElement() {

        Element element = null;
        if (!TextUtils.isEmpty(responseString)) {
            element = ServiceParser.getDocumentElement(responseString);
        }
        return element;
    }



    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }



}
