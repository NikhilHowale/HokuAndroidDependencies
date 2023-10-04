package com.hokuapps.loadmapviewbyconfig.models;

/**
 * Created by user on 6/4/18.
 */

public class PlaceModel {


    public String description;
    public String placeId;
    public String titleAddress;


    public PlaceModel(String description, String placeId, String titleAddress) {
        this.description = description;
        this.placeId = placeId;
        this.titleAddress = titleAddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getTitleAddress(){
        return titleAddress;
    }

    public void setTitleAddress(String titleAddress){
        this.titleAddress = titleAddress;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Override
    public String toString() {
        return getTitleAddress() + ", " + getDescription();
    }
}
