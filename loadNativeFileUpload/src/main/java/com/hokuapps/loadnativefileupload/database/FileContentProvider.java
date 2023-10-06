package com.hokuapps.loadnativefileupload.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.hokuapps.loadnativefileupload.NativeFileUpload;



public class FileContentProvider extends ContentProvider {

    public static final String AUTHORITY = NativeFileUpload.AUTHORITY;
    public static final Uri CONTENT_URI = Uri.parse("content://" + NativeFileUpload.AUTHORITY);
    private static final int APP_MEDIA_DETAILS = 730;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static FileContentProvider provider;

    static {

        sURIMatcher.addURI(AUTHORITY, Tables.PATH_APP_MEDIA_DETAILS, APP_MEDIA_DETAILS);

    }

    public SQLiteDatabase mdb;
    FileDatabaseHelper databaseHelper;

    public static FileContentProvider getInstance() {
        return provider;
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new FileDatabaseHelper(getContext());
        mdb = databaseHelper.getWritableDatabase();
        provider = this;
        return true;
    }

    /**
     * Delete values from database
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (mdb == null) return -1;

        final int match = sURIMatcher.match(uri);
        String tableName;

        // match for which table we need to perform insert operation
        if (match == APP_MEDIA_DETAILS) {
            tableName = Tables.AppMediaDetails.TABLE_NAME;
        } else {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // add record to respective table
        int id = mdb.delete(tableName, selection, selectionArgs);
        Uri newUri;
        if (id > 0) {
            newUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(newUri, null);
        }

        return id;
    }

    /**
     *
     * @param arg0
     * @return
     */
    @Override
    public String getType(Uri arg0) {
        return null;
    }

    /**
     * Insert values into database
     * @param uri
     * @param values
     * @return
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (mdb == null) return null;

        final int match = sURIMatcher.match(uri);
        String tableName;

        // match for which table we need to perform insert operation
        switch (match) {

            case APP_MEDIA_DETAILS:
                tableName = Tables.AppMediaDetails.TABLE_NAME;
                break;


            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // add record to respective table
        long id = mdb.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        Uri newUri = uri;
        if (id > 0) {
            newUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(newUri, null);
        }
        // return uri with inserted record
        return newUri;
    }

    /**
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (mdb == null) return null;

        final int match = sURIMatcher.match(uri);
        String groupBy = null;
        String tableName;
        Cursor cursor;
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        // match for which table we need to perform insert operation
        try {
            switch (match) {

                case APP_MEDIA_DETAILS:
                    tableName = Tables.AppMediaDetails.TABLE_NAME;
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported URI: " + uri);
            }

            //select record from respective table
            builder.setTables(tableName);
            cursor = builder.query(mdb, projection, selection, selectionArgs, groupBy, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

            return cursor;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Update data in database
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (mdb == null) return -1;

        final int match = sURIMatcher.match(uri);
        String tableName;

        // match for which table we need to perform insert operation
        if (match == APP_MEDIA_DETAILS) {
            tableName = Tables.AppMediaDetails.TABLE_NAME;
        } else {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // update record to respective table
        int id = mdb.update(tableName, values, selection, selectionArgs);

        Uri newUri;
        if (id > 0) {
            newUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(newUri, null);
        }

        // return id of updated record
        return id;
    }

}
