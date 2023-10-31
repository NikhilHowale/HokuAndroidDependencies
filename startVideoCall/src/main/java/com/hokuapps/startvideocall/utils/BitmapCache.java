package com.hokuapps.startvideocall.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;

import java.io.File;

public class BitmapCache {
    private static BitmapCache cacheInstance;
    private static LruCache<String, Bitmap> mContactImageMemoryCache;

    public static BitmapCache getInstance(){
        if(cacheInstance == null){
            cacheInstance = new BitmapCache();
        }

        if (mContactImageMemoryCache == null) {
            configureLRUCache();
        }
        
        return cacheInstance;
    }

    /**
     * Configure LRU memory cache
     */
    private static void configureLRUCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mContactImageMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    /**
     * This method add bitmap to cache
     * @param key key for mContactImageMemoryCache map
     * @param bitmap bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key == null) return;
        if (getBitmapFromMemCache(key) == null) {
            mContactImageMemoryCache.put(key, bitmap);
        }
    }

    /**
     * This method retrieve bitmap or add bitmap
     * @param context context
     * @param fileName file name
     * @return return bitmap
     */
    public Bitmap addOrGetBitmapFromMemCache(Context context, String fileName) {
        Bitmap bitmap = null;

        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        bitmap = getBitmapFromMemCache(fileName);

        if (bitmap == null) {
            File file = Utility.getProfileThumbPath(context,fileName);
            if (file != null && file.exists()) {
                bitmap = ScalingUtility.decodeFile(file.getAbsolutePath(), AppConstant.IMAGE_THUMB_SIZE_128, AppConstant.IMAGE_THUMB_SIZE_128, ScalingUtility.ScalingLogic.CROP);
                if (bitmap != null) {
                    addBitmapToMemoryCache(fileName, bitmap);
                }
            }
        }

        return bitmap;
    }

    public void removeBitmapToMemoryCache(String key) {
        if (key == null) return;
        if (getBitmapFromMemCache(key) != null) {
            mContactImageMemoryCache.remove(key);
        }
    }

    /**
     *  This method return bitmap from cache
     * @param key key to retrieve bitmap from cache
     * @return return bitmap
     */
    public Bitmap getBitmapFromMemCache(String key) {
        if (key == null) return null;
        return mContactImageMemoryCache.get(key);
    }


}
