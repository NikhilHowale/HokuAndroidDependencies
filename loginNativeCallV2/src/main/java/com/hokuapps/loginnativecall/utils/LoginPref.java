package com.hokuapps.loginnativecall.utils;

import android.content.Context;
import android.content.SharedPreferences;


import com.hokuappsloginnativecall.R;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LoginPref {

    private ReentrantReadWriteLock reentrantReadWriteLock;
    private SharedPreferences preferences;
    private Context context;

    public LoginPref(Context context) {
        this.context = context;
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        preferences = context.getSharedPreferences(context.getString(R.string.pref_name), Context.MODE_PRIVATE);
    }

    public void setValue(String key, String value) {
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        preferences.edit().putString(key, value).commit();
        writeLock.unlock();
    }

    public void setLongValue(String key, long longVal) {
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        preferences.edit().putLong(key, longVal).commit();
        writeLock.unlock();
    }

    public String getValue(String key) {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        String userFieldTagId = preferences.getString(key, null);
        readLock.unlock();
        return userFieldTagId;
    }

    public long getLongValue(String key) {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        long longVal = preferences.getLong(key, 0);
        readLock.unlock();
        return longVal;
    }

    public void setBooleanValue(String key, boolean value) {
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        preferences.edit().putBoolean(key, value).commit();
        writeLock.unlock();
    }
}
