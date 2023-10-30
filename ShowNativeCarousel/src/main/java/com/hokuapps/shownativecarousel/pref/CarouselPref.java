package com.hokuapps.shownativecarousel.pref;

import android.content.Context;
import android.content.SharedPreferences;

import com.hokuapps.shownativecarousel.R;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CarouselPref {

    private final SharedPreferences preferences;
    private final ReentrantReadWriteLock reentrantReadWriteLock;

    public CarouselPref(Context context) {
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        preferences = context.getSharedPreferences(context.getString(R.string.pref_name), Context.MODE_PRIVATE);
    }

    public void setValue(String key, String value) {
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        preferences.edit().putString(key, value).apply();
        writeLock.unlock();
    }

    public String getValue(String key) {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        String userFieldTagId = preferences.getString(key, null);
        readLock.unlock();
        return userFieldTagId;
    }
}
