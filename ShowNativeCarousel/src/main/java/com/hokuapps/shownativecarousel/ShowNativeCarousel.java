package com.hokuapps.shownativecarousel;


import static com.hokuapps.shownativecarousel.constants.CarouselConstant.*;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import com.hokuapps.shownativecarousel.pref.CarouselPref;
import com.hokuapps.shownativecarousel.utility.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

public class ShowNativeCarousel {

    private final Context mContext;
    private final Activity mActivity;


    /**
     * Constructor
     * @param mContext context
     * @param mActivity activity reference
     * @param loadHtml build variant
     * @param isDefaultUserLogin build variant
     * @param appId build variant
     * @param flavour build variant
     */
    public ShowNativeCarousel(Context mContext, Activity mActivity, boolean loadHtml,
                              boolean isDefaultUserLogin,String appId,String flavour) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        LOAD_HTML_DIRECTLY = loadHtml;
        IS_DEFAULT_USER_LOGIN = isDefaultUserLogin;
        APPLICATION_ID = appId;
        FLAVOR = flavour;
    }


    /**
     * Entry point of dependency
     * @param reqData response data
     */
    public void showNativeCarousel(final String reqData) {
        try {

            JSONObject jsonObj = new JSONObject(reqData);
            JSONArray imageSlideArr = (JSONArray) Utility.getJsonObjectValue(jsonObj, KeyConstants.IMAGE_LIST);
            int index = jsonObj.has(KeyConstants.INDEX) ? jsonObj.getInt(KeyConstants.INDEX) : 0;
            boolean isShowDownload = Utility.getJsonObjectBooleanValue(jsonObj, KeyConstants.IS_SHOW_DOWNLOAD);

            if (imageSlideArr != null && imageSlideArr.length() > 0) {
                new CarouselPref(mContext).setValue(KeyConstants.IMAGE_LIST, imageSlideArr.toString());
                ImageSliderActivity.startActivity(mActivity, new JSONArray(), index, isShowDownload);
            } else {
                Toast.makeText(mActivity, R.string.image_list_empty, Toast.LENGTH_LONG).show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
