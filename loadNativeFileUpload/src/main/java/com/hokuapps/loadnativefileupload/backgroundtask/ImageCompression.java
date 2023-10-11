package com.hokuapps.loadnativefileupload.backgroundtask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Looper;


import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public  class ImageCompression {
    private Context context;

    public ImageCompression(Context context) {
        this.context = context;
    }

    private static final int maxWidth = 1920/*720*/;
    private static final int maxHeight = 1080/*1280*/;
    Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
    Handler handler = new Handler(Looper.getMainLooper());


    /**
     * Compress image asynchronously
     * @param from image path for compress
     * @param to save compress image to this location
     * @param callback return compress image path
     */
    public void executeAsync(String from,String to, OnCompressedListener callback) {
        executor.execute(() -> {
            final String result;
            try {
                result = compressImageV3(from,to,maxWidth,maxHeight);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            handler.post(() -> {
                callback.onImageCompressed(result);
            });
        });
    }

    /**
     * Compress image according to given width and height and returns compressed image path
     * @param originalPath original path for image compress
     * @param compressedPath compress image path
     * @param reqWidth require compress image width
     * @param reqHeight require compress image height
     * @return return compress image path
     */
    static String compressImageV3(String originalPath, String compressedPath, int reqWidth, int reqHeight) {
        try {
            FileOutputStream fileOutputStream = null;
            File imageFile = new File(originalPath);
            File file = new File(compressedPath).getParentFile();
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                fileOutputStream = new FileOutputStream(compressedPath);
                // write the compressed bitmap at the destination specified by destinationPath.
                decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight).compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            } finally {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return compressedPath;
    }

    /**
     *
     * @param imageFile image file
     * @param reqWidth require compress width
     * @param reqHeight require compress height
     * @return return scale bitmap
     * @throws IOException occur when file decoding failed
     */
    static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap scaledBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        //check the rotation of the image and display it properly
        ExifInterface exif;
        exif = new ExifInterface(imageFile.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
        Matrix matrix = new Matrix();
        if (orientation == 6) {
            matrix.postRotate(90);
        } else if (orientation == 3) {
            matrix.postRotate(180);
        } else if (orientation == 8) {
            matrix.postRotate(270);
        }
        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return scaledBitmap;
    }

    /**
     *
     * resizing image according to width and height
     * @param options bitmap factory reference
     * @param reqWidth require compress width
     * @param reqHeight require compress height
     * @return return how small image you want
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /**
     * Setting listener for image compression success
     */
    public interface OnCompressedListener {
        void onImageCompressed(String compressedPath);
    }
}
