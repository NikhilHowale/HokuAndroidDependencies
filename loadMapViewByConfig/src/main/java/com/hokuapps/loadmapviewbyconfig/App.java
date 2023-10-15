package com.hokuapps.loadmapviewbyconfig;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

public class App extends Application {

    private static App singleTon;
    private LruCache<String, Bitmap> mContactImageMemoryCache;
    private Context mApplicationContext;

    /**
     * Constructor
     */
    private App() {
        mApplicationContext = this;
        singleTon = this;
    }




    /**
     * Get the application context
     */
    public Context getApplicationContext() {
        return mApplicationContext;
    }



    /**
     * Get the instance of the class
     */
    public static synchronized App getInstance() {
        return singleTon;
    }



    /**
     * Get bitmap from memory cache
     * @param key key to retrieve bitmap
     * @return return bitmap from cache
     */
    public Bitmap getBitmapFromMemCache(String key) {
        if (key == null) return null;
        return mContactImageMemoryCache.get(key);
    }



    /**
     * Add bitmap to memory cache
     * @param key key to add bitmap to cache
     * @param bitmap bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key == null) return;
        if (getBitmapFromMemCache(key) == null) {
            mContactImageMemoryCache.put(key, bitmap);
        }
    }
}
