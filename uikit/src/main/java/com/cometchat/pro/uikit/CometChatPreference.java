package com.cometchat.pro.uikit;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CometChatPreference {

    private SharedPreferences preferences;
    private ReentrantReadWriteLock reentrantReadWriteLock;

    public CometChatPreference(Context context) {
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        preferences = context.getSharedPreferences(context.getString(R.string.preferences), Context.MODE_PRIVATE);
    }

    /**
     * This method store data into sharedPreference using key
     * @param key key
     * @param value value
     */
    public void setValue(String key, String value) {
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        preferences.edit().putString(key, value).apply();
        writeLock.unlock();
    }

    /**
     * This method retrieve data into sharedPreference using key
     * @param key key
     * @return return string value from preference
     */
    public String getValue(String key) {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        String userFieldTagId = preferences.getString(key, null);
        readLock.unlock();
        return userFieldTagId;
    }
}
