package com.hokuapps.shownativecarousel;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.hokuapps.shownativecarousel.constants.CarouselConstant;
import com.hokuapps.shownativecarousel.service.IntegrationManager;
import com.hokuapps.shownativecarousel.utility.Utility;

public class BaseCompatActivity extends AppCompatActivity {

    /**
     * Set color to status bar of screen
     * @param color color code to apply on status bar
     */
    public void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimaryDark, outValue, true);

            try {
                //Setting status bar color for sherlock.
                if (IntegrationManager.isWhiteColor) {
                    getWindow().setStatusBarColor(Color.parseColor(IntegrationManager.appStatusBarColor));
                    setTaskDescription(IntegrationManager.appStatusBarColor);
//                  Change status bar icon colors to light.
                    View decor = getWindow().getDecorView();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    } else {
                        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

                    }
                    return;
                }

                if (CarouselConstant.LOAD_HTML_DIRECTLY && !TextUtils.isEmpty(IntegrationManager.appStatusBarColor)) {
                    getWindow().setStatusBarColor(Utility.changeColorToPrimaryHSB(IntegrationManager.appStatusBarColor));
                    setTaskDescription(IntegrationManager.appStatusBarColor);
                    return;
                }

                if (Utility.isRoofingSouthwest()) {
                    getWindow().setStatusBarColor(Utility.changeColorToPrimaryHSB("#3c3c3c"));
                    return;
                } else if (CarouselConstant.IS_DEFAULT_USER_LOGIN) {
                    getWindow().setStatusBarColor(
                            Utility.changeColorToPrimaryHSB(Utility.isCampusAffairs()
                                    ? "#00000000"
                                    : "#49344F"));
                    return;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            getWindow().setStatusBarColor(color == 0 ? outValue.data : color);
        }
    }


    /**
     * set status bar color
     * @param color color code
     */
    private void setTaskDescription(String color) {
        try {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(Utility.getResString(R.string.app_name,this), bm, Color.parseColor(color));
            setTaskDescription(td);

            if (bm != null) {
                bm.recycle();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
