package com.hokuapps.loadnativefileupload.annotate.adapter;

import android.graphics.drawable.Drawable;

public class Pencil {
    private String imageTitle;
    private String colorCode;
    private Drawable drawable;
    private boolean isSelected;

    public Pencil(String imageTitle, String colorCode, Drawable drawable, boolean isSelected) {
        this.imageTitle = imageTitle;
        this.colorCode = colorCode;
        this.drawable = drawable;
        this.isSelected = isSelected;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public boolean isSelected() {
        return isSelected;
    }

}
