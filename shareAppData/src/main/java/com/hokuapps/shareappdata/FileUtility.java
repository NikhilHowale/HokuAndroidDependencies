package com.hokuapps.shareappdata;


import android.text.TextUtils;
import android.util.Log;

import java.io.File;

public class FileUtility {

    private static final String TAG = "FileUtility";

    // Avoid creation of object
    private FileUtility() {
    }




    public static boolean isFileExist(String path) {

        boolean toReturn = false;

        if (path == null || path.length() == 0) return false;

        try {
            File file = new File(path);
            if (file.exists() || file.length() > 0) toReturn = true;
        } catch (Exception ex) {
           Log.e(TAG , "isFileExist : Exception occurred " + ex.getMessage());
        }

        return toReturn;
    }






    public static String getFileName(String filePath) {

        if (!isFileExist(filePath)) return "";

        File file = new File(filePath);

        return file.getName();
    }



    public static String getFileNameWithoutExtension(String filename) {

        return filename.substring(0, filename.length() - getExtensionWithDot(filename).length());
    }

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








    public static String getDownloadFileParentDir(String folderName, boolean isSandboxDir) {
        File fileRootDir = new File(ShareUtility.getRootDirPath());
        if (!isSandboxDir) {

            if (!fileRootDir.exists()) {
                fileRootDir.mkdir();
            }

            File pdfFolder = new File(fileRootDir, folderName);

            if (!pdfFolder.exists()) {
                pdfFolder.mkdir();
            }

            return pdfFolder.getAbsolutePath();

        } else {
            return new ShareAppData().htmlDirectory.getAbsolutePath();
        }
    }


}

