package com.hokuapps.loadmapviewbyconfig.utility;

import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.HEADER_TEXT_COLOR;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.hokuapps.loadmapviewbyconfig.R;
import com.hokuapps.loadmapviewbyconfig.constant.MapConstant;


public class ThemeUtils {

    /**
     *
     * @param context
     * @param colorCode
     * @return
     */
    public static ShapeDrawable getBgRoundedRectByTheme(Context context, String colorCode) {

        ShapeDrawable rndrect = new ShapeDrawable(
                new RoundRectShape(new float[]{70, 70, 70, 70, 70, 70, 70, 70},
                        null, null));
        rndrect.setIntrinsicHeight(50);
        rndrect.setIntrinsicWidth(100);
        rndrect.getPaint().setColor(Color.GRAY);
        try {
            if (MapConstant.LOAD_HTML_DIRECTLY && !TextUtils.isEmpty(colorCode)) {
                rndrect.getPaint().setColor(Color.parseColor(colorCode));
            } else {
                rndrect.getPaint().setColor(context.getResources().getColor(getPrimaryColor()));
            }
            return rndrect;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rndrect;
    }


    /**
     * Returns the theme primary color according to theme
     * @return
     */
    public static int getPrimaryColor() {
        int curThemeId;

            curThemeId = MapConstant.THEME_ID;

        int primaryColor = R.color.color_primary;
        switch (curThemeId) {
            case MapConstant.themeId.THEME_DEFAULT:
                break;
            case MapConstant.themeId.THEME_GRAY:
                primaryColor = R.color.color_gray_primary;
                break;
            case MapConstant.themeId.THEME_DARK_BLUE:
                primaryColor = R.color.color_dark_blue_primary;
                break;
            case MapConstant.themeId.THEME_RED:
                primaryColor = R.color.color_red_primary;
                break;

            case MapConstant.themeId.THEME_PURPLE:
                primaryColor = R.color.color_purple_primary;
                break;
            case MapConstant.themeId.THEME_CYNA:
                primaryColor = R.color.color_cyna_primary;
                break;
            case MapConstant.themeId.THEME_GREEN:
                primaryColor = R.color.color_green_primary;
                break;
            case MapConstant.themeId.THEME_BLUE:
                primaryColor = R.color.color_blue_primary;
                break;
            case MapConstant.themeId.THEME_PINK:
                primaryColor = R.color.color_pink_primary;
                break;

            case MapConstant.themeId.THEME_YELLOW:
                primaryColor = R.color.color_yellow_primary;
                break;
            case MapConstant.themeId.THEME_DARK:
                primaryColor = R.color.color_dark_primary;
                break;
        }
        return primaryColor;
    }


    /**
     * this function changes the toolbar color according to theme
     * @param toolbar toolbar instance
     */
    public static void changedToolbarTextColorByTheme(View toolbar) {
        if (toolbar != null) {
            try {
                String headerTextColor = Utility.getStringObjectValue(Utility.configJson, HEADER_TEXT_COLOR);
                if (TextUtils.isEmpty(headerTextColor)) {
                    headerTextColor = "#000000";
                }
                if (MapConstant.LOAD_HTML_DIRECTLY && !TextUtils.isEmpty(headerTextColor)) {
                    if (toolbar instanceof TextView) {
                        ((TextView) toolbar).setTextColor(Color.parseColor(headerTextColor));
                    } else if (toolbar instanceof Toolbar) {
                        ((Toolbar) toolbar).setTitleTextColor(Color.parseColor(headerTextColor));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
