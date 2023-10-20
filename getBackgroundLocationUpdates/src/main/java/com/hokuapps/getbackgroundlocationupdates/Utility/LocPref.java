package com.hokuapps.getbackgroundlocationupdates.Utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.hokuapps.getbackgroundlocationupdates.R;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LocPref {

    private ReentrantReadWriteLock reentrantReadWriteLock;
    private SharedPreferences preferences;
    private Context context;

    public LocPref(Context context) {
        this.context = context;
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        preferences = context.getSharedPreferences(context.getString(R.string.pref_name), Context.MODE_PRIVATE);
    }

    /**
     * set the lat/long value in shared preference
     * @param key
     * @param doubleVal
     */
    public void setDoubleValue(String key, double doubleVal) {
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        preferences.edit().putLong(key, Double.doubleToRawLongBits(doubleVal)).apply();
        writeLock.unlock();
    }

    /**
     * get the lat/long value from shared preferences
     * @param key
     * @return
     */
    public double getDoubleValue(String key) {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        double doubleVal = Double.longBitsToDouble(preferences.getLong(key, 0));
        readLock.unlock();
        return doubleVal;
    }
}
