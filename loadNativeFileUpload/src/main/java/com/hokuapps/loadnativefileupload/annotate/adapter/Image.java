package com.hokuapps.loadnativefileupload.annotate.adapter;

import android.graphics.drawable.Drawable;

public class Image {
    private String imageTitle;
    private String imageName;
    private String metaData;
    private String selectedImageName;
    private Drawable oldDrawable;
    private Drawable newDrawable;
    private boolean isSelected;
    private boolean isLocal;
    private boolean isDrawLine;

    public Image(Drawable oldDrawable, Drawable newDrawable, String imageTitle, String metaData, boolean isSelected, boolean isLocal, boolean isDrawLine) {
        this.oldDrawable = oldDrawable;
        this.newDrawable = newDrawable;
        this.imageTitle = imageTitle;
        this.metaData = metaData;
        this.isSelected = isSelected;
        this.isLocal = isLocal;
        this.isDrawLine = isDrawLine;
    }

    public Image(String imageName, String selectedImageName, String title, String metaData, boolean isSelected, boolean isLocal, boolean isDrawLine) {
        this.imageName = imageName;
        this.selectedImageName = selectedImageName;
        this.imageTitle = title;
        this.metaData = metaData;
        this.isSelected = isSelected;
        this.isLocal = isLocal;
        this.isDrawLine = isDrawLine;
    }

    public String getMetaData() {
        return metaData;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public Drawable getOldDrawable() {
        return oldDrawable;
    }

    public void setOldDrawable(Drawable oldDrawable) {
        this.oldDrawable = oldDrawable;
    }

    public Drawable getNewDrawable() {
        return newDrawable;
    }

    public void setNewDrawable(Drawable newDrawable) {
        this.newDrawable = newDrawable;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getSelectedImageName() {
        return selectedImageName;
    }

    public void setSelectedImageName(String selectedImageName) {
        this.selectedImageName = selectedImageName;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public boolean isDrawLine() {
        return isDrawLine;
    }

}
