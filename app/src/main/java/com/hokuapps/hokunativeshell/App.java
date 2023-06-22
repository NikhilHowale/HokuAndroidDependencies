package com.hokuapps.hokunativeshell;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;

import com.hokuapps.hokunativeshell.receivers.NotificationReceiver;
import com.hokuapps.hokunativeshell.utils.Utility;

public class App extends Application {

    public static final String TAG = App.class.getSimpleName();
    private static App singleTon;
    private Context mApplicationContext;

    public App() {
        super();
        mApplicationContext = this;
        singleTon = this;
    }

    public static synchronized App getInstance() {
        return singleTon;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(new NotificationReceiver(), new IntentFilter("android.net.conn.NOTIFICATION"));
        Utility.copyAssetDirToSandbox(this, "webapp", Utility.getHtmlDirFromSandbox().getAbsolutePath());
        Utility.setNativeCallbackSettingFlag();

    }

}
