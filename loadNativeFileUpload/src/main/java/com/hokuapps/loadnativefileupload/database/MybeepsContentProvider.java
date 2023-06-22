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
import com.hokuapps.loadnativefileupload.services.IntegrationManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class MybeepsContentProvider extends ContentProvider {

    public static final String AUTHORITY = NativeFileUpload.AUTHORITY/*"com.mybeeps.database"*/;
    public static final String SYNC_MOBILE_FIRST = "smV8";
    public static final Uri CONTENT_URI = Uri.parse("content://" + NativeFileUpload.AUTHORITY);
    private static final int APP_MEDIA_DETAILS = 730;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static MybeepsContentProvider provider;

    static {

        sURIMatcher.addURI(AUTHORITY, Tables.PATH_APP_MEDIA_DETAILS, APP_MEDIA_DETAILS);

    }

    public SQLiteDatabase mdb;
    MybeepsDatabaseHelper databaseHelper;

    public static MybeepsContentProvider getInstance() {
        return provider;
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new MybeepsDatabaseHelper(getContext());
        mdb = databaseHelper.getWritableDatabase();
        provider = this;
        return true;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (mdb == null) return -1;

        final int match = sURIMatcher.match(uri);
        String tableName = null;

        // match for which table we need to perform insert operation
        switch (match) {

            case APP_MEDIA_DETAILS:
                tableName = Tables.AppMediaDetails.TABLE_NAME;
                break;


            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // add record to respective table
        int id = mdb.delete(tableName, selection, selectionArgs);
        Uri newUri = uri;
        if (id > 0) {
            newUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(newUri, null);
        }

        return id;
    }

    @Override
    public String getType(Uri arg0) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (mdb == null) return null;

        final int match = sURIMatcher.match(uri);
        String tableName = null;

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

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (mdb == null) return null;

        final int match = sURIMatcher.match(uri);
        String groupBy = null;
        String tableName = null;
        Cursor cursor = null;
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

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (mdb == null) return -1;

        final int match = sURIMatcher.match(uri);
        String tableName = null;

        // match for which table we need to perform insert operation
        switch (match) {
            case APP_MEDIA_DETAILS:
                tableName = Tables.AppMediaDetails.TABLE_NAME;
                break;



            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // update record to respective table
        int id = mdb.update(tableName, values, selection, selectionArgs);

        Uri newUri = uri;
        if (id > 0) {
            newUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(newUri, null);
        }

        // return id of updated record
        return id;
    }

    public void writeDataToDatabase(File file) {

        BufferedReader in = null;
//        String line = null;
        String query = "";
        StringBuilder sb = new StringBuilder();
        try {

            in = new BufferedReader(new FileReader(file));
            mdb.beginTransaction();

            for (String next, line = in.readLine(); line != null; line = next) {
                next = in.readLine();
                try {
                    //String query = line;
                    sb.append(line);

                    int lastIndex = sb.toString().lastIndexOf(");");

                    if (sb.toString().startsWith("INSERT OR REPLACE INTO")) {
                        if (lastIndex == -1) {
                            sb.append("\n");
                            continue;
                        } else {

                            if (next != null && !next.startsWith("INSERT OR REPLACE INTO")) {
                                sb.append("\n");
                                continue;
                            }
                            query = sb.toString();
                            sb.setLength(0);

                            if (query.startsWith("INSERT OR REPLACE INTO ChatMessageText")
                                    || query.startsWith("INSERT OR REPLACE INTO ChatMessageFile")) {
                            } else if (query.startsWith("INSERT OR REPLACE INTO ChatMessage")) { /*"_id",*/

                                if (SYNC_MOBILE_FIRST.equals("smV8")) {
//                                    syncMobileV8
                                    query = query.replace("INSERT OR REPLACE INTO ChatMessage", "INSERT OR REPLACE INTO ChatMessage(\"_id\",\"orgID\",\"sender\",\"target\",\"messageId\",\"type\",\"chatMessageType\",\"creationDate\",\"serverReceivedDate\",\"readDate\",\"readNotifiedDate\",\"deliveryDate\",\"deliveryNotifiedDate\",\"sortDate\",\"updateDate\",\"syncDate\",\"isRead\",\"isDelivered\",\"messageScore\",\"mentions\",\"updatedBy\",\"addedAction\",\"actionTypeId\",\"mapAppMessageKey\",\"IntegrationId\",\"jsonData\",\"isTask\",\"isMessageDelete\")"); // syncMobileV8
                                } else {
//                                    syncMobileV6
                                    query = query.replace("INSERT OR REPLACE INTO ChatMessage", "INSERT OR REPLACE INTO ChatMessage(\"_id\",\"orgID\",\"sender\",\"target\",\"messageId\",\"type\",\"chatMessageType\",\"creationDate\",\"serverReceivedDate\",\"readDate\",\"readNotifiedDate\",\"deliveryDate\",\"deliveryNotifiedDate\",\"sortDate\",\"updateDate\",\"syncDate\",\"isRead\",\"isDelivered\",\"messageScore\",\"mentions\",\"updatedBy\",\"addedAction\",\"actionTypeId\",\"mapAppMessageKey\",\"IntegrationId\",\"jsonData\",\"isTask\")"); // syncMobileV6
                                }

                       }
                        }
                    } else {
                        sb.setLength(0);
                        continue;
                    }

                    mdb.execSQL(query);
                    sb.setLength(0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }
            }


            mdb.setTransactionSuccessful();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mdb.endTransaction();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (file != null) {
                file.delete();
            }
        }
    }




    public void execSQL(String sql) {
        try {
            mdb.beginTransaction();
            mdb.execSQL(sql);
            mdb.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mdb.endTransaction();
        }
    }
}
