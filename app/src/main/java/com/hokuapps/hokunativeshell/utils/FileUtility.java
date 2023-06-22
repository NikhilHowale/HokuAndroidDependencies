package com.hokuapps.hokunativeshell.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileUtility {

    private static final String TAG = "FileUtility";

    // Avoid creation of object
    private FileUtility() {
    }

    /**
     * Check if  file is exist at given file location.
     *
     * @param path
     * @return
     */
    public static boolean isFileExist(String path) {

        boolean toReturn = false;

        if (path == null || path.length() == 0) return toReturn;

        try {
            File file = new File(path);
            if (file.exists() || file.length() > 0) toReturn = true;
        } catch (Exception ex) {
            Log.e(TAG , "isFileExist : Exception occured " + ex.getMessage());
        }

        return toReturn;
    }

    public static String readAllFileContent(String filename, String fileDir) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;

        File fileToRead = new File(fileDir, filename);

        if (!fileToRead.exists()) return sb.toString();

        try {
            br = new BufferedReader(new FileReader(fileToRead));
            String temp;
            while ((temp = br.readLine()) != null)
                sb.append(temp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close(); // stop reading
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
