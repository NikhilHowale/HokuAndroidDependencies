package com.hokuapps.loadmapviewbyconfig.utility;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;


public class KeyboardUtils {
    private static final String TAG = "KeyboardUtils";
    private KeyboardListener keyboardListener;


    public interface KeyboardListener {
         void onKeyboardOpen();

         void onKeyboardClose();

    }

    public KeyboardListener getKeyboardListener() {
        return keyboardListener;
    }

    public void setKeyboardListener(final View contentView, KeyboardListener keyboardListener) {
        this.keyboardListener = keyboardListener;

        // ContentView is the root view of the layout of this activity/fragment
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(
                () -> {

                    Rect r = new Rect();
                    contentView.getWindowVisibleDisplayFrame(r);
                    int screenHeight = contentView.getRootView().getHeight();

                    // r.bottom is the position above soft keypad or device button.
                    // if keypad is shown, the r.bottom is smaller than that before.
                    int keypadHeight = screenHeight - r.bottom;

                    Log.d(TAG, "keypadHeight = " + keypadHeight);

                    if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                        // keyboard is opened
                        if (getKeyboardListener() != null)
                            getKeyboardListener().onKeyboardOpen();

                    } else {
                        // keyboard is closed
                        if (getKeyboardListener() != null)
                            getKeyboardListener().onKeyboardClose();
                    }
                });

    }

}
