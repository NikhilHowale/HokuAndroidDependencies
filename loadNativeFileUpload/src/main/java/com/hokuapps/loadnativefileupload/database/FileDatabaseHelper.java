package com.hokuapps.loadnativefileupload.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class FileDatabaseHelper extends SQLiteOpenHelper {

    public final String TAG = FileDatabaseHelper.class.getSimpleName();

    public static final String DATABASE_NAME = "appMediaDetails"+ ".db";
    public static final int DATABASE_VERSION = 1;

    public FileDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create database with table name
     */
    interface CreateQuery {
        String APP_MEDIA_DETAILS_TABLE = "CREATE TABLE IF NOT EXISTS " + Tables.AppMediaDetails.TABLE_NAME + " ( " +
                Tables.AppMediaDetails.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Tables.AppMediaDetails.COLUMN_OFFLINE_DATA_ID + " TEXT, " +
                Tables.AppMediaDetails.COLUMN_FILE_NAME + " TEXT, " +
                Tables.AppMediaDetails.COLUMN_FILE_SIZE_BYTES + " TEXT, " +
                Tables.AppMediaDetails.COLUMN_UPLOAD_DATE + " TEXT, " +
                Tables.AppMediaDetails.COLUMN_MEDIA_ID + " TEXT, " +
                Tables.AppMediaDetails.COLUMN_S3FILE_PATH + " TEXT, " +
                Tables.AppMediaDetails.COLUMN_UPLOAD_STATUS + " INTEGER, " +
                Tables.AppMediaDetails.COLUMN_INSTRUCATION_NUMBER + " INTEGER, " +
                Tables.AppMediaDetails.COLUMN_IMAGE_TYPE + " INTEGER, " +
                Tables.AppMediaDetails.COLUMN_IMAGE_CAPTION + " TEXT " +
                " ) ";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CreateQuery.APP_MEDIA_DETAILS_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
