package com.hokuapps.hokunativeshell.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    public interface NotificationListener {
        void onReceive(Intent intent);
    }

    public static NotificationListener notificationListener = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (notificationListener != null) {
            notificationListener.onReceive(intent);
        }

    }

    public static void setNotificationListener(NotificationListener notificationListener){
        NotificationReceiver.notificationListener = notificationListener;

    }
}
