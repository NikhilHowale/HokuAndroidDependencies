package com.hokuapps.loadnativefileupload.utilities;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.hokuapps.loadnativefileupload.R;
import com.hokuapps.loadnativefileupload.models.AuthenticatedUser;

public class ThemeUtils {

    public static boolean isWhiteColor = false;

    public static Drawable getMenuIconDrawable(Activity activity, int menuIcon) {
        final AuthenticatedUser authUser = AuthenticatedUser.currentUser(activity);

        int resId = R.color.primary_text_default_material_dark;
        if (authUser != null && authUser.getThemeId() == AuthenticatedUser.THEME_GRAY) {
            resId = R.color.primary_text_default_material_light;
        }

        if (isWhiteColor)
            resId = R.color.black;

        final Drawable drawable = activity == null
                ? activity.getResources().getDrawable(menuIcon)
                : activity.getResources().getDrawable(menuIcon);

        drawable.setColorFilter(activity == null
                ? activity.getResources().getColor(resId)
                : activity.getResources().getColor(resId), PorterDuff.Mode.SRC_ATOP);

        //ToolbarColorizeHelper.setOverflowButtonColor(getActivity(), drawable);
        return drawable;
    }
}
