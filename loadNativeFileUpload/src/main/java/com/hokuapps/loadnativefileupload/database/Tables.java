package com.hokuapps.loadnativefileupload.database;

import android.net.Uri;

public class Tables {
    public static String PATH_APP_MEDIA_DETAILS = "AppMediaDetails";
    public static String PATH_AUTHENTICATE_USER = "AuthenticateUser";
    public static class AuthenticatedUserTableModel implements AuthenticateUser {
        public static Uri CONTENT_URI = getContentUri(PATH_AUTHENTICATE_USER);
    }

    interface AuthenticateUser {
        String COLUMN_ID = "_id";//
        String COLUMN_IS_ACTIVE = "isActive";//
        String COLUMN_PROFILE_PHOTO_UPDATE_DATE = "profilePhotoUpdateDate";//
        String COLUMN_USER_ID = "userID";//
        String COLUMN_PROFILE_PHOTO_DOWNLOAD_DATE = "profilePhotoDownloadDate";//
        String COLUMN_PHOTO_PATH = "profilePhotoPath";
        String COLUMN_ROLE = User.COLUMN_ROLE;
    }

    interface User {
        String COLUMN_ROLE = "roleId";
    }
    interface AppMediaDetails {
        String TABLE_NAME = "AppMediaDetails";
        String COLUMN_ID = "_id";
        String COLUMN_OFFLINE_DATA_ID = "offlineDataID";
        String COLUMN_FILE_NAME = "fileName";
        String COLUMN_FILE_SIZE_BYTES = "fileSizeBytes";
        String COLUMN_UPLOAD_DATE = "uploadDate";
        String COLUMN_MEDIA_ID = "mediaID";
        String COLUMN_S3FILE_PATH = "s3FilePath";
        String COLUMN_UPLOAD_STATUS = "uploadStatus";
        String COLUMN_INSTRUCATION_NUMBER = "instructionNumber";
        String COLUMN_IMAGE_TYPE = "imageType";
        String COLUMN_IMAGE_CAPTION = "imageCaption";
    }
    public static class AppMediaDetailsTable implements AppMediaDetails {
        public static Uri CONTENT_URI = getContentUri(PATH_APP_MEDIA_DETAILS);
    }
    private static Uri getContentUri(String path) {
        return MybeepsContentProvider.CONTENT_URI
                .buildUpon().appendPath(path).build();
    }
}
