package com.hokuapps.handlePushNotification;

import android.app.Activity;

public class PushNotification {

    public static Activity activity;

    private static PushNotification INSTANCE = null;

    private PushNotification() {};

    public static PushNotification getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PushNotification();
        }
        return(INSTANCE);
    }
}
