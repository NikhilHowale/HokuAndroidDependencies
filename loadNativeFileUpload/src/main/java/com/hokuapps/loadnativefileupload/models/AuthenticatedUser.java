package com.hokuapps.loadnativefileupload.models;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.hokuapps.loadnativefileupload.database.Tables.AuthenticatedUserTableModel;

import java.util.ArrayList;

public class AuthenticatedUser {
    private int roleId;
    private String userId;
    private long rowId;
    public Context context;
    private static AuthenticatedUser currentUser;
    private String photoPath;
    private String profilePhotoDownloadDate;
    public static final int ROLE_ID_ADMIN = 1;
    public static final int ROLE_ID_FULL_USER = 20;
    public static final int ROLE_ID_RESTRICTED_USER = 50;
    public static final int ROLE_ID_GUEST = 100;
    public static final int ROLE_ID_MODERATOR = 150;
    public static final int ACTION_CREATE_GROUP = 1001;
    public static final int ACTION_CREATE_BROADCAST = 1002;
    public static final int ACTION_EDIT_GROUP = 1003;
    public static final int THEME_GRAY = 2;
    private static ArrayList<Integer> permissionList;
    private int themeId;
    private String profilePhotoUpdateDate;
    private String profileThumbURL;

    public String getUserId() {
        return TextUtils.isEmpty(userId) ? "" : userId;
    }
    public String getProfileThumbURL() {
        return profileThumbURL;
    }


    public AuthenticatedUser(Context context) {
        this.context = context;
    }

    public AuthenticatedUser() {

    }

    public int getThemeId() {
        return themeId;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    public String getProfilePhotoUpdateDate() {
        return profilePhotoUpdateDate;
    }

    public void setProfilePhotoUpdateDate(String profilePhotoUpdateDate) {
        this.profilePhotoUpdateDate = profilePhotoUpdateDate;
    }

    public static AuthenticatedUser currentUser(Context mContext) {
        if (currentUser == null) {
            currentUser = getActiveUserFromDB(mContext);
        } else {
            getRolePermissions(currentUser.getRoleId());
        }
        return currentUser;
    }

    private static void getRolePermissions(int roleId) {
        getPermissionsForRole(roleId);
    }

    public static void getPermissionsForRole(int roleId) {

        if (permissionList == null) {
            permissionList = new ArrayList<Integer>();
        }

        permissionList.clear();

        switch (roleId) {
            case ROLE_ID_ADMIN:
                break;
            case ROLE_ID_FULL_USER:
            case ROLE_ID_MODERATOR:
                break;
            case ROLE_ID_RESTRICTED_USER:
                addPermissionForRestrictedUser();
                break;
            case ROLE_ID_GUEST:
                addPermissionForGuestUser();
                break;
            default:
        }
    }

    private static void addPermissionForRestrictedUser() {
        permissionList.add(ACTION_CREATE_GROUP);
        permissionList.add(ACTION_CREATE_BROADCAST);
        //permissionList.add(ACTION_EDIT_GROUP);
    }

    /**
     * Add permission for guest user
     */
    private static void addPermissionForGuestUser() {
        permissionList.add(ACTION_CREATE_GROUP);
        permissionList.add(ACTION_CREATE_BROADCAST);
        permissionList.add(ACTION_EDIT_GROUP);
    }

    private static AuthenticatedUser getActiveUserFromDB(Context mContext) {
        String selection = AuthenticatedUserTableModel.COLUMN_IS_ACTIVE + " =?";
        String selectionArgs[] = new String[]{Boolean.toString(true)};


        Cursor cursor = mContext.getContentResolver().query(AuthenticatedUserTableModel.CONTENT_URI, null, selection, selectionArgs, null);
        return assignValue(cursor);
    }

    public void saveProfilePhotoUpdateDate(String value) {
        ContentValues contentValues = new ContentValues();
        String where;
        String selectionArgs[];
        if (TextUtils.isEmpty(userId)) {
            where = AuthenticatedUserTableModel.COLUMN_ID + "=?";
            selectionArgs = new String[]{String.valueOf(rowId)};
        } else {
            contentValues.put(AuthenticatedUserTableModel.COLUMN_USER_ID, userId);
            where = AuthenticatedUserTableModel.COLUMN_USER_ID + "=?";
            selectionArgs = new String[]{userId};
        }
        contentValues.put(AuthenticatedUserTableModel.COLUMN_PROFILE_PHOTO_UPDATE_DATE, value);
        context.getContentResolver().update(AuthenticatedUserTableModel.CONTENT_URI, contentValues, where, selectionArgs);
        profilePhotoUpdateDate = value;
    }

    public int getRoleId() {
        return this.roleId;
    }

    @SuppressLint("Range")
    private static AuthenticatedUser assignValue(Cursor cursor) {
        AuthenticatedUser authenticatedUser = null;
        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    authenticatedUser = new AuthenticatedUser();
                    authenticatedUser.rowId = cursor.getInt(cursor.getColumnIndex(AuthenticatedUserTableModel.COLUMN_ID));
                    authenticatedUser.userId = cursor.getString(cursor.getColumnIndex(AuthenticatedUserTableModel.COLUMN_USER_ID));
                authenticatedUser.photoPath = cursor.getString(cursor.getColumnIndex(AuthenticatedUserTableModel.COLUMN_PHOTO_PATH));
                 authenticatedUser.profilePhotoDownloadDate = cursor.getString(cursor.getColumnIndex(AuthenticatedUserTableModel.COLUMN_PROFILE_PHOTO_UPDATE_DATE));
                 authenticatedUser.roleId = cursor.getInt(cursor.getColumnIndex(AuthenticatedUserTableModel.COLUMN_ROLE));

               }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        return authenticatedUser;
    }

    public static boolean updateAuthenticateUserPhotoAndDownloadDate(Context context, String userId, String photoUri, String photoDownloadDate) {
        try {
            ContentValues values = new ContentValues();
            String selectCriteria = AuthenticatedUserTableModel.COLUMN_USER_ID + "= ?";
            String[] selectionArgs = new String[]{userId};

            if (photoDownloadDate != null) {
                values.put(AuthenticatedUserTableModel.COLUMN_PROFILE_PHOTO_DOWNLOAD_DATE, photoDownloadDate);
            }

            if (photoUri != null) {
                values.put(AuthenticatedUserTableModel.COLUMN_PHOTO_PATH, photoUri);
            }

            int id = context.getContentResolver().update(AuthenticatedUserTableModel.CONTENT_URI, values, selectCriteria, selectionArgs);
            if (id > 0) {
                AuthenticatedUser user = AuthenticatedUser.currentUser(context);
                user.setProfilePhotoDownloadDate(photoDownloadDate);
                user.setPhotoPath(photoUri);
                return true;
            }
        } catch (Exception e) {

        }

        return false;
    }

    public void setProfilePhotoDownloadDate(String profilePhotoDownloadDate) {
        this.profilePhotoDownloadDate = profilePhotoDownloadDate;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
