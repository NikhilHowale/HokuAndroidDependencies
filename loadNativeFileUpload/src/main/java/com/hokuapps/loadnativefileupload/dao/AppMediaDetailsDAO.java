package com.hokuapps.loadnativefileupload.dao;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import androidx.loader.content.CursorLoader;

import com.hokuapps.loadnativefileupload.database.MybeepsDatabaseHelper;
import com.hokuapps.loadnativefileupload.database.Tables;
import com.hokuapps.loadnativefileupload.models.AppMediaDetails;


import java.util.ArrayList;

public class AppMediaDetailsDAO extends BaseDAO {

    AppMediaDetails appMediaDetails;
    public SQLiteDatabase mdb;
    MybeepsDatabaseHelper databaseHelper;
    static Context mContext;

    public AppMediaDetailsDAO(Context context) {
        super(context);
    }

    public AppMediaDetailsDAO(Context context, AppMediaDetails appMediaDetails) {
        super(context);
        this.mContext = context;
        databaseHelper = new MybeepsDatabaseHelper(getContext());
        mdb = databaseHelper.getWritableDatabase();
        this.appMediaDetails = appMediaDetails;
    }

    public static void updateUploadedFileStatus(String fileName, int uploadStatus) {
        AppMediaDetailsDAO appMediaDetailsDAO = new AppMediaDetailsDAO(mContext);
        AppMediaDetails appMediaDetails = appMediaDetailsDAO.getStoredAppMediaDetails(mContext, fileName);
        appMediaDetails.setUploadStatus(uploadStatus);
        appMediaDetails.save(mContext);
    }

    public boolean deleteAppMediaDeatailsByFileName(String filename) {
        if (TextUtils.isEmpty(filename)) {
            return false;
        }

        //delete a record
        return mContext.getContentResolver().delete(
                Tables.AppMediaDetailsTable.CONTENT_URI,
                Tables.AppMediaDetailsTable.COLUMN_FILE_NAME + " =?",
                new String[]{filename}) > 0;
    }

    private static AppMediaDetails buildAppMediaDetailsFromCursor(Cursor cursor) {
        AppMediaDetails appMediaDetailsModel = null;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                appMediaDetailsModel = getAppMediaDetailsFromCursor(cursor);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return appMediaDetailsModel;
    }

    public static ArrayList<AppMediaDetails> getAppMediaDetailsListByOfflineID(Context context, String offlineId) {
        String sort_order = Tables.AppMediaDetailsTable.COLUMN_INSTRUCATION_NUMBER + " ASC"; //LIMIT 2
        CursorLoader loader = new CursorLoader(context,
                Tables.AppMediaDetailsTable.CONTENT_URI,
                null,
                Tables.AppMediaDetailsTable.COLUMN_OFFLINE_DATA_ID + " =? ",
                new String[]{offlineId},
                sort_order);

        ArrayList<AppMediaDetails> appMediaDetailsList = buildAppMediaDetailsList(loader.loadInBackground());
        return appMediaDetailsList;
    }

    public static AppMediaDetails getAppMediaDetailsByFileName(Context context, String offlineId, String filename) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(
                Tables.AppMediaDetailsTable.CONTENT_URI,
                null,
                Tables.AppMediaDetailsTable.COLUMN_OFFLINE_DATA_ID + " =? AND " +
                        Tables.AppMediaDetailsTable.COLUMN_FILE_NAME + " =? ",
                new String[]{offlineId, filename},
                null);

        AppMediaDetails appMediaDetailsList = buildAppMediaDetailsFromCursor(cursor);

        return appMediaDetailsList;
    }

    public static AppMediaDetails getAppMediaDetailsListByOfflineIDWithUploadedFile(Context context, String offlineId,
                                                                                    boolean isUploaded, String filename) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(
                Tables.AppMediaDetailsTable.CONTENT_URI,
                null,
                Tables.AppMediaDetailsTable.COLUMN_OFFLINE_DATA_ID + " =? AND " +
                        "(" + Tables.AppMediaDetailsTable.COLUMN_UPLOAD_STATUS + "=? OR " +
                        Tables.AppMediaDetailsTable.COLUMN_UPLOAD_STATUS + " =?) AND " +
                        Tables.AppMediaDetailsTable.COLUMN_FILE_NAME + " =? ",
                new String[]{offlineId, String.valueOf(isUploaded ? 1 : 0), String.valueOf(isUploaded ? 1 : 2), filename},
                null);

        AppMediaDetails appMediaDetailsList = buildAppMediaDetailsFromCursor(cursor);

        return appMediaDetailsList;
    }

    public boolean deleteAppMediaDeatailsByInstructionNumber(String offlineId, int number) {
        if (TextUtils.isEmpty(offlineId)) {
            return false;
        }

        //delete a record
        mContext.getContentResolver().delete(
                Tables.AppMediaDetailsTable.CONTENT_URI,
                Tables.AppMediaDetailsTable.COLUMN_OFFLINE_DATA_ID + " = \"" + offlineId + "\" AND " +
                        Tables.AppMediaDetailsTable.COLUMN_INSTRUCATION_NUMBER + " = " + number, null);
        return true;
    }

    public static ArrayList<AppMediaDetails>
    getAppMediaDetailsListByOfflineIDWithUploadedFile(Context context,
                                                      String offlineId,
                                                      boolean isUploaded) {

        CursorLoader loader = new CursorLoader(context,
                Tables.AppMediaDetailsTable.CONTENT_URI,
                null,
                Tables.AppMediaDetailsTable.COLUMN_OFFLINE_DATA_ID + " =? AND " +
                        "(" + Tables.AppMediaDetailsTable.COLUMN_UPLOAD_STATUS + "=? OR " +
                        Tables.AppMediaDetailsTable.COLUMN_UPLOAD_STATUS + " =?)",
                new String[]{offlineId, String.valueOf(isUploaded ? 1 : 0), String.valueOf(isUploaded ? 1 : 2)},
                null);

        ArrayList<AppMediaDetails> appMediaDetailsList = buildAppMediaDetailsList(loader.loadInBackground());

        return appMediaDetailsList;
    }

    private static ArrayList<AppMediaDetails> buildAppMediaDetailsList(Cursor cursor) {
        ArrayList<AppMediaDetails> appMediaDetailsList = new ArrayList<>();
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    appMediaDetailsList.add(getAppMediaDetailsFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return appMediaDetailsList;
    }

    @SuppressLint("Range")
    public static AppMediaDetails getAppMediaDetailsFromCursor(Cursor cursor) {

        AppMediaDetails appMediaDetailsModel = new AppMediaDetails();
        appMediaDetailsModel.setRow_id(cursor.getLong(cursor.getColumnIndex(Tables.AppMediaDetailsTable.COLUMN_ID)));
        appMediaDetailsModel.setOfflineDataID(cursor.getString(cursor.getColumnIndex(Tables.AppMediaDetailsTable.COLUMN_OFFLINE_DATA_ID)));
        appMediaDetailsModel.setFileName(cursor.getString(cursor.getColumnIndex(Tables.AppMediaDetailsTable.COLUMN_FILE_NAME)));
        appMediaDetailsModel.setFileSizeBytes(cursor.getString(cursor.getColumnIndex(Tables.AppMediaDetailsTable.COLUMN_FILE_SIZE_BYTES)));
        appMediaDetailsModel.setUploadDate(cursor.getString(cursor.getColumnIndex(Tables.AppMediaDetailsTable.COLUMN_UPLOAD_DATE)));
        appMediaDetailsModel.setMediaID(cursor.getString(cursor.getColumnIndex(Tables.AppMediaDetailsTable.COLUMN_MEDIA_ID)));

        appMediaDetailsModel.setS3FilePath(cursor.getString(cursor.getColumnIndex(Tables.AppMediaDetailsTable.COLUMN_S3FILE_PATH)));
        appMediaDetailsModel.setUploadStatus(cursor.getInt(cursor.getColumnIndex(Tables.AppMediaDetailsTable.COLUMN_UPLOAD_STATUS)));
        appMediaDetailsModel.setInstructionNumber(cursor.getInt(cursor.getColumnIndex(Tables.AppMediaDetailsTable.COLUMN_INSTRUCATION_NUMBER)));
        appMediaDetailsModel.setImageType(cursor.getInt(cursor.getColumnIndex(Tables.AppMediaDetailsTable.COLUMN_IMAGE_TYPE)));
        appMediaDetailsModel.setImageCaption(cursor.getString(cursor.getColumnIndexOrThrow(Tables.AppMediaDetailsTable.COLUMN_IMAGE_CAPTION)));

        return appMediaDetailsModel;
    }


    @Override
    public int save() throws Exception {
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = contentResolver.insert(Tables.AppMediaDetailsTable.CONTENT_URI, getContentValues());
        return (int) ContentUris.parseId(uri);
    }

    @Override
    public void update() throws Exception {
        if (appMediaDetails == null) {
            return;
        }

        mdb.update(Tables.PATH_APP_MEDIA_DETAILS, getContentValues(), Tables.AppMediaDetailsTable.COLUMN_ID + "=" + appMediaDetails.getRow_id(), null);
    }

    @Override
    public boolean remove() throws Exception {
        if (appMediaDetails == null) {
            return false;
        }

        //delete a record
        return getContext().getContentResolver().delete(
                Tables.AppMediaDetailsTable.CONTENT_URI,
                Tables.AppMediaDetailsTable.COLUMN_ID + " =?",
                new String[]{String.valueOf(appMediaDetails.getRow_id())}) > 0;
    }

    protected ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(Tables.AppMediaDetailsTable.COLUMN_OFFLINE_DATA_ID, this.appMediaDetails.getOfflineDataID());
        values.put(Tables.AppMediaDetailsTable.COLUMN_FILE_NAME, this.appMediaDetails.getFileName());
        values.put(Tables.AppMediaDetailsTable.COLUMN_FILE_SIZE_BYTES, this.appMediaDetails.getFileSizeBytes());
        values.put(Tables.AppMediaDetailsTable.COLUMN_UPLOAD_DATE, this.appMediaDetails.getUploadDate());
        values.put(Tables.AppMediaDetailsTable.COLUMN_MEDIA_ID, this.appMediaDetails.getMediaID());
        values.put(Tables.AppMediaDetailsTable.COLUMN_S3FILE_PATH, this.appMediaDetails.getS3FilePath());
        values.put(Tables.AppMediaDetailsTable.COLUMN_UPLOAD_STATUS, this.appMediaDetails.getUploadStatus());
        values.put(Tables.AppMediaDetailsTable.COLUMN_INSTRUCATION_NUMBER, this.appMediaDetails.getInstructionNumber());
        values.put(Tables.AppMediaDetailsTable.COLUMN_IMAGE_TYPE, this.appMediaDetails.getImageType());
        values.put(Tables.AppMediaDetailsTable.COLUMN_IMAGE_CAPTION, this.appMediaDetails.getImageCaption());
        return values;
    }

    public AppMediaDetails getStoredAppMediaDetails(Context context, String filename) {
        ContentResolver cr = context.getContentResolver();

        return buildAppMediaDetailsFromCursor(cr.query(
                Tables.AppMediaDetailsTable.CONTENT_URI,
                null,
                Tables.AppMediaDetailsTable.COLUMN_FILE_NAME + " =? ",
                new String[]{filename},
                null));
    }

    public AppMediaDetails getStoredAppMediaDetails(Context context, int instructionNumber) {
        ContentResolver cr = context.getContentResolver();

        return buildAppMediaDetailsFromCursor(cr.query(
                Tables.AppMediaDetailsTable.CONTENT_URI,
                null,
                Tables.AppMediaDetailsTable.COLUMN_INSTRUCATION_NUMBER + " =? ",
                new String[]{String.valueOf(instructionNumber)},
                null));
    }

}

