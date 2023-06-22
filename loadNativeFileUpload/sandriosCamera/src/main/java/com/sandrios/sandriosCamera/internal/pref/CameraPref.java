package com.sandrios.sandriosCamera.internal.pref;

import android.content.Context;
import android.content.SharedPreferences;

import com.sandrios.sandriosCamera.internal.ui.view.CameraSwitchView;
import com.sandrios.sandriosCamera.internal.ui.view.FlashSwitchView;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by user on 7/4/17.
 */
public class CameraPref {

    private SharedPreferences preferences;
    private ReentrantReadWriteLock reentrantReadWriteLock;
    private Context context;
    /**
     * Initialize object
     *
     * @param context
     */
    public CameraPref(Context context) {
        this.context = context;
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        preferences = context.getSharedPreferences("cameraSettingPref", Context.MODE_PRIVATE);
    }


    public int getFlashMode() {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        int flashMode = preferences.getInt("flashMode", FlashSwitchView.FLASH_AUTO);
        readLock.unlock();
        return flashMode;
    }

    public void setFlashMode(int flashMode) {
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        preferences.edit().putInt("flashMode", flashMode).commit();
        writeLock.unlock();
    }

    public int getCameraRotation() {
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        int cameraRotation = preferences.getInt("cameraRotation", CameraSwitchView.CAMERA_TYPE_REAR);
        readLock.unlock();
        return cameraRotation;
    }

    public void setCameraRotation(int cameraRotation) {
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        preferences.edit().putInt("cameraRotation", cameraRotation).commit();
        writeLock.unlock();
    }

}
