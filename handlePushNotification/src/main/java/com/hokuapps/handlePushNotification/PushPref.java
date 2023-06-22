package com.hokuapps.handlePushNotification;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PushPref {
    private SharedPreferences preferences;
    private ReentrantReadWriteLock reentrantReadWriteLock;
    private Context context;
    private  String masterKeyAlias;



    /**
     * Initialize object
     *
     * @param context
     */
    public PushPref(Context context) {
        this.context = context;
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        preferences = context.getSharedPreferences("NotifyPref", Context.MODE_PRIVATE);
    }


    public String getValue(String key) {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        String userFieldTagId = preferences.getString(key, null);
        readLock.unlock();
        return userFieldTagId;
    }

    public void setValue(String key, String value) {
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        preferences.edit().putString(key, value).commit();
        writeLock.unlock();
    }
}
