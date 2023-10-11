package com.hokuapps.loadnativefileupload.restrequest;

/**
 * Class contains service response details
 * for activity
 */
public class ServiceResponse {

    // class members
    private String errorMsg;
    private String responseString;
    private int responseCode;

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getResponseString() {
        return responseString;
    }

    public void setResponseString(String responseString) {
        this.responseString = responseString;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

}
