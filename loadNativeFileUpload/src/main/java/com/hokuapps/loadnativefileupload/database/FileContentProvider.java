package com.hokuapps.loadnativefileupload.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;



public class FileContentProvider extends ContentProvider {

    public String AUTHORITY = "";
    public  Uri CONTENT_URI = Uri.parse("");
    private static final int APP_MEDIA_DETAILS = 730;
    private UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static FileContentProvider provider;

    public SQLiteDatabase mdb;
    FileDatabaseHelper databaseHelper;

    public static FileContentProvider getInstance() {
        return provider;
    }

    public void setUpDatabase(String authority){
        AUTHORITY = authority;
        sURIMatcher.addURI(AUTHORITY, Tables.PATH_APP_MEDIA_DETAILS, APP_MEDIA_DETAILS);
        CONTENT_URI = Uri.parse("content://" + AUTHORITY);
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
     * @param uri query
     * @param selection condition for filter records
     * @param selectionArgs argument on which records will filter
     * @return return id of deleted row
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
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

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    /**
     * Insert values into database
     * @param uri query
     * @param values values for insert into table
     * @return return uri with inserted record
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (mdb == null) return null;

        final int match = sURIMatcher.match(uri);
        String tableName;

        // match for which table we need to perform insert operation
        if (match == APP_MEDIA_DETAILS) {
            tableName = Tables.AppMediaDetails.TABLE_NAME;
        } else {
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
     *  perform custom query on table
     * @param uri query
     * @param projection selecting specific column from table
     * @param selection condition for filter records
     * @param selectionArgs argument on which records will filter
     * @param sortOrder order of records
     * @return return cursor with records
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (mdb == null) return null;

        final int match = sURIMatcher.match(uri);
        String groupBy = null;
        String tableName;
        Cursor cursor;
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        // match for which table we need to perform insert operation
        try {
            if (match == APP_MEDIA_DETAILS) {
                tableName = Tables.AppMediaDetails.TABLE_NAME;
            } else {
                throw new IllegalArgumentException("Unsupported URI: " + uri);
            }

            //select record from respective table
            builder.setTables(tableName);
            cursor = builder.query(mdb, projection, selection, selectionArgs, null, null, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

            return cursor;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Update data in database
     * @param uri query
     * @param values values for update into table
     * @param selection condition for filter records
     * @param selectionArgs argument on which records will filter
     * @return return id of updated record
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
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
