package com.hokuapps.hokunativeshell.pref;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.hokuapps.hokunativeshell.R;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MybeepsPref {

    private SharedPreferences preferences;
    private ReentrantReadWriteLock reentrantReadWriteLock;
    private Context context;
    private  String masterKeyAlias;



    /**
     * Initialize object
     *
     * @param context
     */
    public MybeepsPref(Context context) {
        this.context = context;
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        preferences = context.getSharedPreferences("nativePreference", Context.MODE_PRIVATE);

//        try {
//            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
//            preferences = EncryptedSharedPreferences.create(
//                    context.getString(R.string.pref_name),
//                    masterKeyAlias,
//                    context,
//                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//            );
//        } catch (GeneralSecurityException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
