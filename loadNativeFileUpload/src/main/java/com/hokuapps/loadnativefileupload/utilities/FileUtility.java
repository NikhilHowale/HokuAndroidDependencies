package com.hokuapps.loadnativefileupload.utilities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.loadnativefileupload.NativeFileUpload;
import com.hokuapps.loadnativefileupload.constants.FileUploadConstant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class FileUtility {

    /**
     *
     * @param filePath file path
     * @return return name of file
     */
    public static String getFileNameWithoutExists(String filePath) {

        File file = new File(filePath);

        return file.getName();
    }

    /**
     *
     * @return return path of media directory
     */
    public static File getMediaDirPath() {
        String path = FileUtility.getRootDir() + File.separator + FileUploadConstant.FOLDER_PARENT_NAME;
        return new File(path, FileUploadConstant.FOLDER_NAME_MEDIA);
    }

    /**
     * Return the primary external storage directory.
     *
     * @return return root( download ) directory
     */
    public static File getRootDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    /**
     * Return the MyBeepsApp external storage directory.
     *
     * @return return path of root directory
     */
    public static String getRootDirPath() {
        return getRootDir() + File.separator + FileUploadConstant.FOLDER_PARENT_NAME;
    }

    /**
     * Create the storage directory if it does not exist
     *
     * @param mediaStorageDir directory name with path
     * @return true if directory created successfully
     * false Otherwise
     */
    public static boolean makeDirectory(File mediaStorageDir) {
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return false;
            }
        }
        return true;
    }


    /**
     *
     * @param name source file path
     * @return return extension of file from name
     */
    public static String getExtensionWithDot(String name) {
        String ext;

        if (TextUtils.isEmpty(name)) return "";

        if (name.lastIndexOf(".") == -1) {
            ext = "";

        } else {
            int index = name.lastIndexOf(".");
            ext = name.substring(index);
        }
        return ext;
    }

    /**
     * Returns file name from given path
     * @param filePath file path
     * @return return file name from file path
     */
    public static String getFileName(String filePath) {

        if (!isFileExist(filePath)) return "";

        File file = new File(filePath);

        return file.getName();
    }

    /**
     * Check if  file is exist at given file location.
     *
     * @param path file path
     * @return return true if file exit otherwise false
     */
    public static boolean isFileExist(String path) {

        boolean toReturn = false;

        if (path == null || path.length() == 0) return false;

        try {
            File file = new File(path);
            if (file.exists() || file.length() > 0) toReturn = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return toReturn;
    }

    /**
     *
     * @param bitmap bitmap
     * @return return Base64 string after decoding bitmap
     */
    public static String getBase64Data(Bitmap bitmap) {
        if (bitmap == null) {
            return "";
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     *
     * @param context context
     * @return return sandbox directory path
     */
    public static File getHtmlDirFromSandbox(Context context) {
        File htmlDir = new File(context.getFilesDir() + File.separator + FileUploadConstant.FOLDER_NAME_WEB_HTML);

        if (!makeDirectory(htmlDir)) {
            return null;
        }

        return htmlDir;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }

            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                return getFilePath(context, uri);
            }

            // GoogleDrive
            else if (isGoogleDrive(uri)) {
                Toast.makeText(context, "Sorry, you can't upload files from Google Drive.", Toast.LENGTH_SHORT).show();
                return null;
            }

            // GoogleDriveDoc
            else if (isGoogleDriveDoc(uri)) {
                Toast.makeText(context, "Sorry, you can't upload files from Google Drive.", Toast.LENGTH_SHORT).show();
                return null;
            }

            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else if ("document".equals(type)) {
                    contentUri = MediaStore.Files.getContentUri("external");
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }

        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri) || isGoogleDocUri(uri))
                return uri.getLastPathSegment();
            try {
                return getDataColumn(context, uri, null, null);
            } catch (Exception ex) {
                return uri.getPath();
            }
        }

        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Returns file path from the uri
     * @param context context
     * @param uri uri of file
     * @return return real path of file
     */
    public static String getFilePath(Context context, Uri uri) {

        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getCacheDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

            inputStream.close();
            outputStream.close();
            returnCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isGoogleDocUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }

    public static boolean isGoogleDrive(Uri uri) {
        return "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }

    public static boolean isGoogleDriveDoc(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }

    @SuppressLint("SimpleDateFormat")
    public static String convertToUTCTimeZone(long milliseconds) {
        return convertToUTCTimeZone(milliseconds, FileUploadConstant.UTC_DATE_FORMAT);
    }

    /**
     * Convert provided time to UTC data
     *
     * @param milliseconds time in long format
     * @return {@link String}
     */
    @SuppressLint("SimpleDateFormat")
    public static String convertToUTCTimeZone(long milliseconds, String dateFormat) {
        String utcDateString = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            utcDateString = formatter.format(new Date(milliseconds));
        } catch (Exception e) {

            e.printStackTrace();
        }
        return utcDateString;
    }

    /**
     *
     * @param activity activity context
     * @param type set which type of file should open by intent
     */
    public static void launchIntentByFileFormat(final Activity activity, final String type) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PermissionListener permissionListener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType(type);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivityForResult(intent, NativeFileUpload.REQUEST_FILE_BROWSER);

                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {

                    }
                };
                TedPermission.create()
                        .setPermissionListener(permissionListener)
                        .setPermissions(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        .check();

            }
        });
    }

    /**
     *
     * @param uri file uri
     * @param context context
     * @return return mime type
     */
    public static String getMimeType(Uri uri,Context context) {
        String mimeType = null;
        if (uri.getScheme() != null && uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    /**
     * check's if the given file is image
     * @param type type of file
     * @return return true if image file otherwise false
     */
    public static boolean isImageFile(String type) {
        return (!TextUtils.isEmpty(type) && type.toLowerCase().contains("image"));
    }
}
