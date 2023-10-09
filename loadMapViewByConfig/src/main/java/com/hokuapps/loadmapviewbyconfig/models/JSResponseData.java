package com.hokuapps.loadmapviewbyconfig.models;

/**
 * Created by user on 30/12/16.
 */
public class JSResponseData {

    private String appID;
    private String responseData;

    private LocationMapModel locationMapModel;
    private String color;

    public JSResponseData() {
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getAppID() {
        return appID;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public void setLocationMapModel(LocationMapModel locationMapModel) {
        this.locationMapModel = locationMapModel;
    }

    public LocationMapModel getLocationMapModel() {
        return locationMapModel;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


}
