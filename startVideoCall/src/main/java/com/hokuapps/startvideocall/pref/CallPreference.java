package com.hokuapps.startvideocall.pref;

import android.content.Context;
import android.content.SharedPreferences;

import com.hokuapps.startvideocall.R;
import com.hokuapps.startvideocall.utils.AppConstant;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CallPreference {
    private SharedPreferences preferences;
    private Context context;
    private ReentrantReadWriteLock reentrantReadWriteLock;

    /**
     * Initialize object
     *
     * @param context
     */
    public CallPreference(Context context) {
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

    public String getValue(String key) {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        String userFieldTagId = preferences.getString(key, null);
        readLock.unlock();
        return userFieldTagId;
    }

    public String getSecretKey() {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        String token = preferences.getString(context.getString(R.string.secret_pref_key), "");
        readLock.unlock();
        return token;
    }

    public String getToken() {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        String token = preferences.getString(context.getString(R.string.user_token_pref_key), "");
        readLock.unlock();
        return token;
    }

    public String getUserCallParams() {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        String callParams = preferences.getString(AppConstant.Call_PARAMS, "");
        readLock.unlock();
        return callParams;
    }


    public void setUserCallParams(String callParams) {
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        preferences.edit().putString(AppConstant.Call_PARAMS, callParams).commit();
        writeLock.unlock();
    }
}
