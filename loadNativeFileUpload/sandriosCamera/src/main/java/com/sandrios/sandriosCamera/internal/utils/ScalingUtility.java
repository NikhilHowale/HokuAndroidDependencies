package com.sandrios.sandriosCamera.internal.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScalingUtility {
    /**
     * ScalingLogic defines how scaling should be carried out if source and
     * destination image has different aspect ratio.
     * <p/>
     * CROP: Scales the image the minimum amount while making sure that at least
     * one of the two dimensions fit inside the requested destination area.
     * Parts of the source image will be cropped to realize this.
     * <p/>
     * FIT: Scales the image the minimum amount while making sure both
     * dimensions fit inside the requested destination area. The resulting
     * destination dimensions might be adjusted to a smaller size than
     * requested.
     */
    public enum ScalingLogic {
        CROP, FIT
    }


    /**
     * check size of image from width and height return how small image require
     * @param options bitmap factory reference
     * @param reqWidth require width
     * @param reqHeight require height
     * @return return size
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * check orientation and size of bitmap then scale according need
     * @param filePath file path of capture image
     * @return return bitmap after orientation and size change
     */
    public static Bitmap decodeFile(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        //by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        try {
            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

            //setting inSampleSize value allows to load a scaled down version of the original image
            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

            //inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;

            //this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
                //load the bitmap from its path
                bmp = BitmapFactory.decodeFile(filePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ExifInterface exifObject = null;
        int orientation = 0;
        try {
            exifObject = new ExifInterface(filePath);
            orientation = exifObject.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotateBitmap(bmp, orientation);
    }

    /**
     * rotate bitmap
     * @param bitmap bitmap
     * @param orientation orientation
     * @return return rotated bitmap other wise same bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * save bitmap into local storage
     * @param bm bitmap
     * @param filePath file path of selected or capture image
     * @return return return file path of save image
     */
    public static String saveBitmap(Bitmap bm, String filePath) {
        File f = new File(filePath);
        File croppedFile = null;
        File NewApifilePath = null;
        String newFilePath = "";

        if (!TextUtils.isEmpty(filePath)) {
            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            newFilePath = f.getParentFile().getPath() + File.separator + "IMG_" + timeStamp + ".jpeg";
            croppedFile = new File(newFilePath);
            if (croppedFile.exists()) {
                croppedFile.delete();
            } else {
                try {
                    croppedFile.createNewFile();
                    try {
                        FileOutputStream out = new FileOutputStream(croppedFile);
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } catch (FileNotFoundException e) {
                        // Do something for Api 30
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {

                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                        String destination = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                        NewApifilePath = new File(destination, "IMG_" + timeStamp + ".jpeg");
                        final Uri uri = Uri.parse("file://" + NewApifilePath);
                        newFilePath = uri.getPath();
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(uri.getPath());
                            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                    e.printStackTrace();
                }
            }
        }
        return newFilePath;
    }
}