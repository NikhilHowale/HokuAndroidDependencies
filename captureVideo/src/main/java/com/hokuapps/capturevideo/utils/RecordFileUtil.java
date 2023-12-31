package com.hokuapps.capturevideo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class RecordFileUtil {

    public static final String TAG = "RecordFileUtil";
    private static final Object mLock = new Object();

    public static String DEFAULT_DIR;
    protected static String mTmpFileSubFix = "";
    protected static String mTmpFilePreFix = "";

    public static String getCreateFileDir(String name) {
        File file = new File(DEFAULT_DIR + name);
        if (!file.exists()) {
            file.mkdirs();
        }
        return DEFAULT_DIR + name;
    }

    public static void setFileDir(String dir) {
        DEFAULT_DIR = dir;
        getCreateFileDir("");
    }


    public static String createFile(String dir, String suffix) {
        synchronized (mLock) {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            int second = c.get(Calendar.SECOND);
            int millisecond = c.get(Calendar.MILLISECOND);
            year = year - 2000;

            String dirPath = dir;
            File d = new File(dirPath);
            if (!d.exists())
                d.mkdirs();

            if (dirPath.endsWith("/") == false) {
                dirPath += "/";
            }

            String name = mTmpFilePreFix;
            name += String.valueOf(year);
            name += String.valueOf(month);
            name += String.valueOf(day);
            name += String.valueOf(hour);
            name += String.valueOf(minute);
            name += String.valueOf(second);
            name += String.valueOf(millisecond);
            name += mTmpFileSubFix;
            if (!suffix.startsWith(".")) {
                name += ".";
            }
            name += suffix;


            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String retPath = dirPath + name;
            File file = new File(retPath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return retPath;
        }
    }


    public static String createMp4FileInBox() {
        return createFile(DEFAULT_DIR, ".mov");
    }


    public static String createFileInBox(String suffix) {
        return createFile(DEFAULT_DIR, suffix);
    }


    public static void deleteFile(String path) {
        if (path != null) {
            if(path.contains("file:")){
                path = path.replace("file:","");
            }

            File file = new File(path);
            if (file.exists()) {
                file.delete();

            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * LSNEW
     *
     * @param bmp
     */
    public static String saveBitmap(Bitmap bmp) {
        if (bmp != null) {
            try {
                BufferedOutputStream bos;
                String name = createFileInBox("png");
                bos = new BufferedOutputStream(new FileOutputStream(name));
                bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
                bos.close();
                return name;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("saveBitmap", "error  bmp  is null");
        }
        return "save Bitmap ERROR";
    }

    /**
     * LSNEW
     *
     * @return
     */
    public static boolean deleteDefaultDir() {
        File file = new File(DEFAULT_DIR);
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(file, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    public static File getHtmlDirFromSandbox(Context context) {
        File htmlDir = new File(context.getFilesDir() + File.separator + AppConstant.FOLDER_NAME_WEB_HTML);

        if (!makeDirectory(htmlDir)) {
            return null;
        }

        return htmlDir;
    }

    /**
     * Create the storage directory if it does not exist
     *
     * @param mediaStorageDir
     * @return true if directory created successfully
     * false Otherwise
     */
    public static boolean makeDirectory(File mediaStorageDir) {
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return false;
            }
        }
        return true;
    }

    public static String getFileName(String filePath) {

        if (!isFileExist(filePath)) return "";

        File file = new File(filePath);

        return file.getName();
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
            ex.printStackTrace();
        }

        return toReturn;
    }


}
