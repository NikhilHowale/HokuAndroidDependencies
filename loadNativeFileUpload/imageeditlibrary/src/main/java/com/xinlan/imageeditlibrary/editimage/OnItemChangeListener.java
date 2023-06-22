package com.xinlan.imageeditlibrary.editimage;

/**
 * Created by user on 7/9/17.
 */

public interface OnItemChangeListener {

    int itemChanged = 0;
    int reselected = 1;
    int deleted = 2;
    int deletedPrev = 5;
    int pushItemInStack = 3;

    void onItemChanged(Object item, int type);

}
