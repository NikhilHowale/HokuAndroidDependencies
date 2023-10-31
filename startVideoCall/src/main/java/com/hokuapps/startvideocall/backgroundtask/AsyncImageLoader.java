package com.hokuapps.startvideocall.backgroundtask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.hokuapps.startvideocall.displayer.FadeInBitmapDisplayer;
import com.hokuapps.startvideocall.displayer.RoundedBitmapDisplayer;
import com.hokuapps.startvideocall.utils.BitmapCache;
import com.hokuapps.startvideocall.utils.Utility;
import com.hokuapps.startvideocall.widget.TextDrawable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class AsyncImageLoader {
    private static final String TAG = AsyncImageLoader.class.getSimpleName();

    private final Context context;

    public AsyncImageLoader(Context context) {
        this.context = context;
    }

    /**
     * Class that Extends AsyncTask, This is used to process bitmap
     * to create thumbnail image.
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        public String data;
        private final WeakReference<ImageView> imageViewReference;
        private RoundedBitmapDisplayer displayer;
        private List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        public BitmapWorkerTask(ImageView imageView, RoundedBitmapDisplayer displayer) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            this.imageViewReference = new WeakReference<ImageView>(imageView);
            this.displayer = displayer;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            Bitmap bitmap = null;
            try {
                //check data is null or empty
                if (TextUtils.isEmpty(data)) {
                    return bitmap;
                }

                String fileName = data;
                File file = Utility.generateLocalFilePathForThumbnail(context,fileName);

                if (!Utility.isFileExist(file)) {
                    if (downloadProfileThumb(data, file) == null) {
                        if (file != null && file.exists()) file.delete();
                        return bitmap;
                    }
                }

                bitmap = BitmapCache.getInstance().addOrGetBitmapFromMemCache(context,fileName);
            } catch (OutOfMemoryError | Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);

                    boolean firstDisplay = !displayedImages.contains(data);
                    if (firstDisplay) {
                        FadeInBitmapDisplayer.animate(imageView, 50);
                        displayedImages.add(data);
                    }
                }
            }
        }
    }

    /**
     * This method display image in imageView from cache
     * @param id key to fetch image from cache
     * @param name profile user name
     * @param imageView imageView
     */
    public void displayImage(String id, String name, ImageView imageView) {
        try {
            if (TextUtils.isEmpty(id)) {
                TextDrawable drawable = TextDrawable.getTextDrawable(name);
                imageView.setImageDrawable(drawable);
            } else {
                final Bitmap bitmap = BitmapCache.getInstance().getBitmapFromMemCache(id);

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    // read from storage with AsyncTask
                    if (cancelPotentialWork(id, imageView)) {
                        final BitmapWorkerTask task = new BitmapWorkerTask(imageView, null);
                        TextDrawable.Builder builder = TextDrawable.builder().round().builder();
                        builder.set(TextDrawable.getTwoCharFromString(name).toUpperCase());

                        final AsyncShapeDrawable asyncShapeDrawable = new AsyncShapeDrawable(task, builder);
                        imageView.setImageDrawable(asyncShapeDrawable);
                        task.execute(id);
                    }
                }
            }
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if another running task is already associated with the ImageView.
     * If so, it attempts to cancel the previous task by calling cancel().
     * In a small number of cases, the new task data matches the existing task and
     * nothing further needs to happen
     *
     * @param id key associated with each bitmap
     * @param imageView imageView
     */
    private static boolean cancelPotentialWork(String id, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            if (bitmapData == null || !bitmapData.equalsIgnoreCase(id)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    /**
     * A helper method, that is used above to retrieve the task associated with
     * a particular ImageView:
     *
     * @param imageView imageView
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
           if (drawable instanceof AsyncShapeDrawable) {
                final AsyncShapeDrawable asyncDrawable = (AsyncShapeDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static class AsyncShapeDrawable extends TextDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncShapeDrawable(BitmapWorkerTask bitmapWorkerTask, Builder builder) {
            super(builder);
            bitmapWorkerTaskReference =  new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }


        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }


    /**
     * This method call api to download profile to local storage
     * @param urlString url string
     * @param file local file path
     * @return return downloaded profile image path
     */
    private synchronized String downloadProfileThumb(String urlString, File file) {
        String response = null;
        int responseCode = -1;
        //start listening to the stream
        FileOutputStream fos = null;
        InputStream in = null;
        try {

            if (file == null) {
                return null;
            }
            Log.e(TAG, ":downloadProfileThumb : " + urlString );
            Response fileResponse = getREST(urlString);
            responseCode = fileResponse.code();

            if (fileResponse.isSuccessful()) {
                writeBitmapToFile(fileResponse.body().byteStream(), file);
            } else {
                Log.e(TAG, "AsyncImageLoader :: downloadProfileThumb : can't connect to server :: response code: " + responseCode );
                return null;
            }

            response = file.getAbsolutePath();

        } catch (IOException ex) {
            Log.e(TAG, "AsyncImageLoader :: downloadProfileThumb : IOException = " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            Log.e(TAG, "AsyncImageLoader :: downloadProfileThumb : Exception = " + ex.getMessage() );
            ex.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "AsyncImageLoader :: downloadProfileThumb : IOException = " + e.getMessage() );
            }
        }

        return response;
    }

    /**
     * This method create rest request and return response
     * @param url url
     * @return return
     */
    public static Response getREST(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        //change url for app

        Log.e(TAG, "Request URL = " + url );

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        requestBuilder.header( "gzip", "Accept-Encoding");

        Request request = requestBuilder.build();
        return client.newCall(request).execute();
    }

    private boolean copyStream(InputStream is, OutputStream os)
            throws IOException {
        int current = 0;
        int total = is.available();
        final byte[] bytes = new byte[32768];
        int count;
        try {
            while ((count = is.read(bytes, 0, 32768)) != -1) {
                os.write(bytes, 0, count);
                current += count;
            }
            os.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * THis method write input stream to file
     * @param imageStream input stream
     * @param tmpFile team file path
     */
    private boolean writeBitmapToFile(InputStream imageStream, File tmpFile) throws Exception {
        boolean loaded = false;
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile), 32768);
            try {
                loaded = copyStream(imageStream, os);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        } finally {
            if (!loaded) {
                tmpFile.delete();
            }
        }
        return loaded;
    }
}
