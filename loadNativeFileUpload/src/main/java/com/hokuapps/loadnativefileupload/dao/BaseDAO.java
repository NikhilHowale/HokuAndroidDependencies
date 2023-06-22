package com.hokuapps.loadnativefileupload.dao;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public abstract class BaseDAO {

    // class member
    private Context context;

    public BaseDAO(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    abstract public int save() throws Exception;

    abstract public void update() throws Exception;

    abstract public boolean remove() throws Exception;


    /**
     * Query according to specific URI
     *
     * @param uri
     * @param projection
     * @param where
     * @param whereArgs
     * @param sortOrder
     * @return {@link Cursor}
     */
    protected Cursor query(Uri uri, String[] projection, String where, String whereArgs[], String sortOrder) {
        if (context == null) {
            throw new IllegalArgumentException("Invalid context");
        }

        return getContext().getContentResolver().query(uri, projection, where, whereArgs, sortOrder);
    }

    protected ContentResolver getContentResolver() {
        return context.getContentResolver();
    }
}
