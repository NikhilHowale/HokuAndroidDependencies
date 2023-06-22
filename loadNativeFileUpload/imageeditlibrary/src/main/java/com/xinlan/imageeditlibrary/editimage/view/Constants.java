package com.xinlan.imageeditlibrary.editimage.view;

import android.content.Context;

import com.xinlan.imageeditlibrary.editimage.utils.DensityUtil;

/**
 * Created by panyi on 2016/8/6.
 */
public class Constants {
    public static final int STICKER_BTN_HALF_SIZE = 50;

    public static int getRMin100Height(Context context) {
        return DensityUtil.dip2px(context, (float) 100);
    }

    public static int getRMin100Width(Context context) {
        return DensityUtil.dip2px(context, (float) 100);
    }

    public static int getRMin70Height(Context context) {
        return DensityUtil.dip2px(context, (float) 70);
    }

    public static int getRMin70Width(Context context) {
        return DensityUtil.dip2px(context, (float) 70);
    }

    public static int getRMin40Height(Context context) {
        return DensityUtil.dip2px(context, (float) 50);
    }

    public static int getRMin40Width(Context context) {
        return DensityUtil.dip2px(context, (float) 50);
    }

    public static int getRMin40dp(Context context) {
        return DensityUtil.dip2px(context, (float) 40);
    }
}
