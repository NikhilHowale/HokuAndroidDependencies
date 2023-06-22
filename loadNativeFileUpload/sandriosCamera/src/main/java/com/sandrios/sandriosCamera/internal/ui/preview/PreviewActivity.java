package com.sandrios.sandriosCamera.internal.ui.preview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;

import com.bumptech.glide.Glide;

import com.bumptech.glide.request.target.SimpleTarget;
import com.sandrios.sandriosCamera.R;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.BaseSandriosActivity;
import com.sandrios.sandriosCamera.internal.ui.view.AspectFrameLayout;
import com.sandrios.sandriosCamera.internal.utils.ScalingUtility;
import com.sandrios.sandriosCamera.internal.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouch;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouchBase;
import com.bumptech.glide.request.transition.Transition;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/*import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.view.UCropView;*/

/**
 * Created by Arpit Gandhi on 7/6/16.
 */
public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PreviewActivity";

    private final static String SHOW_CROP = CameraConfiguration.Arguments.ENABLE_CROP;
    private final static String ISFROMGALLERY = "isFromGallery";
    private final static String IS_RECTANGLE = "isRectangle";
    private final static String HIDE_RETAKE = "hide_retake";
    private final static String MEDIA_ACTION_ARG = "media_action_arg";
    private final static String FILE_PATH_ARG = CameraConfiguration.Arguments.FILE_PATH;
    private final static String SHOW_CAPTION_ARG = CameraConfiguration.Arguments.SHOW_CAPTION;
    private final static String CAPTION_ARG = CameraConfiguration.Arguments.CAPTION;
    private final static String RESPONSE_DATA = CameraConfiguration.Arguments.RESPONSE_DATA;

    private final static String RESPONSE_CODE_ARG = "response_code_arg";
    private final static String VIDEO_POSITION_ARG = "current_video_position";
    private final static String VIDEO_IS_PLAYED_ARG = "is_played";
    private final static String MIME_TYPE_VIDEO = "video";
    private final static String MIME_TYPE_IMAGE = "image";


    private int mediaAction;
    private String previewFilePath;
    private PreviewActivity mContext;
    private SurfaceView surfaceView;
    private FrameLayout photoPreviewContainer;
    private ImageViewTouch imagePreview;
    private CropImageView cropImageView;
    private AspectFrameLayout videoPreviewContainer;

    private MediaController mediaController;
    private MediaPlayer mediaPlayer;

    private int currentPlaybackPosition = 0;
    private boolean isVideoPlaying = true;
    private boolean showCrop = false;
    private boolean hideRetake = false;

    private Bitmap mainBitmap = null;
    private int imageWidth, imageHeight;

    private boolean showCaption;
    private String caption;

    //bottom layouts
    private LinearLayout captionView;
    private LinearLayout buttonPanel;
    private EditText captionText;

    private boolean isFromGallery;
    private boolean isRectangle;
    private String responseData = "";

    private MediaController.MediaPlayerControl MediaPlayerControlImpl = new MediaController.MediaPlayerControl() {
        @Override
        public void start() {
            mediaPlayer.start();
        }

        @Override
        public void pause() {
            mediaPlayer.pause();
        }

        @Override
        public int getDuration() {
            return mediaPlayer.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            return mediaPlayer.getCurrentPosition();
        }

        @Override
        public void seekTo(int pos) {
            mediaPlayer.seekTo(pos);
        }

        @Override
        public boolean isPlaying() {
            return mediaPlayer.isPlaying();
        }

        @Override
        public int getBufferPercentage() {
            return 0;
        }

        @Override
        public boolean canPause() {
            return true;
        }

        @Override
        public boolean canSeekBackward() {
            return true;
        }

        @Override
        public boolean canSeekForward() {
            return true;
        }

        @Override
        public int getAudioSessionId() {
            return mediaPlayer.getAudioSessionId();
        }
    };

    private SurfaceHolder.Callback surfaceCallbacks = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            showVideoPreview(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    public static Intent newIntent(Context context, @CameraConfiguration.MediaAction int mediaAction, String filePath, boolean showImageCrop, boolean showCaption, String caption, boolean isHideRetake, boolean isFromGallery) {

        return new Intent(context, PreviewActivity.class)
                .putExtra(MEDIA_ACTION_ARG, mediaAction)
                .putExtra(SHOW_CROP, showImageCrop)
                .putExtra(ISFROMGALLERY, isFromGallery)
                .putExtra(HIDE_RETAKE, isHideRetake)
                .putExtra(FILE_PATH_ARG, filePath)
                .putExtra(SHOW_CAPTION_ARG, showCaption)
                .putExtra(CAPTION_ARG, caption);
    }

    public static Intent newIntent(Context context, @CameraConfiguration.MediaAction int mediaAction, String filePath, boolean showImageCrop, boolean showCaption, String caption, boolean isHideRetake, boolean isFromGallery, boolean isRectangle, String responseData) {

        return new Intent(context, PreviewActivity.class)
                .putExtra(MEDIA_ACTION_ARG, mediaAction)
                .putExtra(SHOW_CROP, showImageCrop)
                .putExtra(ISFROMGALLERY, isFromGallery)
                .putExtra(HIDE_RETAKE, isHideRetake)
                .putExtra(FILE_PATH_ARG, filePath)
                .putExtra(SHOW_CAPTION_ARG, showCaption)
                .putExtra(CAPTION_ARG, caption)
                .putExtra(IS_RECTANGLE, isRectangle)
                .putExtra(RESPONSE_DATA, responseData);
    }

    public static Intent newIntent(Context context, @CameraConfiguration.MediaAction int mediaAction, String filePath, boolean showImageCrop, boolean showCaption, String caption, boolean isHideRetake) {

        return new Intent(context, PreviewActivity.class)
                .putExtra(MEDIA_ACTION_ARG, mediaAction)
                .putExtra(SHOW_CROP, showImageCrop)
                .putExtra(ISFROMGALLERY, false)
                .putExtra(HIDE_RETAKE, isHideRetake)
                .putExtra(FILE_PATH_ARG, filePath)
                .putExtra(SHOW_CAPTION_ARG, showCaption)
                .putExtra(CAPTION_ARG, caption);
    }


    public static boolean isResultConfirm(@NonNull Intent resultIntent) {
        return BaseSandriosActivity.ACTION_CONFIRM == resultIntent.getIntExtra(RESPONSE_CODE_ARG, -1);
    }

    public static String getMediaFilePatch(@NonNull Intent resultIntent) {
        return resultIntent.getStringExtra(FILE_PATH_ARG);
    }

    public static String getCaptionPatch(@NonNull Intent resultIntent) {
        return resultIntent.getStringExtra(CAPTION_ARG);
    }

    public static boolean isResultRetake(@NonNull Intent resultIntent) {
        return BaseSandriosActivity.ACTION_RETAKE == resultIntent.getIntExtra(RESPONSE_CODE_ARG, -1);
    }

    public static boolean isResultCancel(@NonNull Intent resultIntent) {
        return BaseSandriosActivity.ACTION_CANCEL == resultIntent.getIntExtra(RESPONSE_CODE_ARG, -1);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        showCaption = getIntent().getBooleanExtra(SHOW_CAPTION_ARG, false);
        caption = getIntent().getStringExtra(CAPTION_ARG);
        isFromGallery = getIntent().getBooleanExtra(ISFROMGALLERY, false);
        isRectangle = getIntent().getBooleanExtra(IS_RECTANGLE, false);
        responseData = getIntent().getStringExtra(CameraConfiguration.Arguments.RESPONSE_DATA);
        initView();
        initData(savedInstanceState);

    }

    private void initData(Bundle savedInstanceState) {
        mContext = this;

        Bundle args = getIntent().getExtras();

        mediaAction = args.getInt(MEDIA_ACTION_ARG);
        previewFilePath = args.getString(FILE_PATH_ARG);
        showCrop = args.getBoolean(SHOW_CROP);
        hideRetake = args.getBoolean(HIDE_RETAKE);

        if (mediaAction == CameraConfiguration.MEDIA_ACTION_VIDEO) {
            displayVideo(savedInstanceState);
        } else if (mediaAction == CameraConfiguration.MEDIA_ACTION_PHOTO) {
            displayImage();
        } else {
            String mimeType = Utils.getMimeType(previewFilePath);
            if (mimeType.contains(MIME_TYPE_VIDEO)) {
                displayVideo(savedInstanceState);
            } else if (mimeType.contains(MIME_TYPE_IMAGE)) {
                displayImage();
            } else finish();
        }

        initHeightAndWidth();
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mediaController == null) return false;
                if (mediaController.isShowing()) {
                    mediaController.hide();
                    showButtonPanel(true);
                } else {
                    showButtonPanel(false);
                    mediaController.show();
                }
                return false;
            }
        });

    }

    private void initPreviewImageView() {
        imagePreview.setDoubleTapEnabled(true);
        imagePreview.setScaleEnabled(true);
        imagePreview.setScrollEnabled(true);

        imagePreview.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
    }

    private void initView() {
        captionText = findViewById(R.id.caption_text);

        if (caption != null) {
            captionText.setText(caption);
        }

        surfaceView = findViewById(R.id.video_preview);
        videoPreviewContainer = findViewById(R.id.previewAspectFrameLayout);
        photoPreviewContainer = findViewById(R.id.photo_preview_container);
        imagePreview = findViewById(R.id.image_view);
        cropImageView = findViewById(R.id.cropImageView);
        buttonPanel = findViewById(R.id.preview_control_panel);
        captionView = findViewById(R.id.caption_root_container);

        //set oval shape for crop
        cropImageView .setCropShape(Build.VERSION.SDK_INT >= 28 ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL);
        cropImageView.setAspectRatio(2, 2);
        cropImageView.setFixedAspectRatio(false);

        JSONObject jsonObjectResponseData = null;
        try {
            if (!TextUtils.isEmpty(responseData)) {

                jsonObjectResponseData = new JSONObject(responseData);
                if (jsonObjectResponseData != null) {

                    if (jsonObjectResponseData.optBoolean("isRectangle")) {
//                Rectangle
                        cropImageView.setAspectRatio(3, 2);
                        cropImageView.setCropShape(CropImageView.CropShape.RECTANGLE);
                    } else if (jsonObjectResponseData.optBoolean("isSquare")) {
//                Square
                        cropImageView.setAspectRatio(1, 1);
                        cropImageView.setCropShape(CropImageView.CropShape.RECTANGLE);
                        cropImageView.setFixedAspectRatio(true);
                    }
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
//        if (!isRectangle) {
//        cropImageView.setFixedAspectRatio(false);
//        }

        initPreviewImageView();

        Button saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(this);

        View confirmMediaResult = findViewById(R.id.confirm_media_result);
        View reTakeMedia = findViewById(R.id.re_take_media);
        View cancelMediaAction = findViewById(R.id.cancel_media_action);

        findViewById(R.id.crop_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //UCrop.Options options = new UCrop.Options();
                //options.setToolbarColor(ContextCompat.getColor(mContext, android.R.color.black));
                //options.setStatusBarColor(ContextCompat.getColor(mContext, android.R.color.black));
                //Uri uri = Uri.fromFile(new File(previewFilePath));
                // UCrop.of(uri, uri)
                //        .withOptions(options)
                //        .start(mContext);
            }
        });

        if (confirmMediaResult != null)
            confirmMediaResult.setOnClickListener(this);

        if (reTakeMedia != null)
            reTakeMedia.setOnClickListener(this);

        if (cancelMediaAction != null)
            cancelMediaAction.setOnClickListener(this);

        updateBottomView();
    }

    private void updateBottomView() {
        if (showCaption) {
            buttonPanel.setVisibility(View.GONE);
            captionView.setVisibility(View.VISIBLE);
        } else {
            buttonPanel.setVisibility(View.VISIBLE);
            captionView.setVisibility(View.GONE);
        }
    }

    private void initHeightAndWidth() {
        try {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            imageWidth = metrics.widthPixels;
            imageHeight = metrics.heightPixels;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveVideoParams(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       /* if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            showImagePreview();
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaController != null) {
            mediaController.hide();
            mediaController = null;
        }
    }

    private void displayImage() {
        imagePreview.setVisibility(showCrop ? View.GONE : View.VISIBLE);
        cropImageView.setVisibility(showCrop ? View.VISIBLE : View.GONE);

        findViewById(R.id.re_take_media).setVisibility(hideRetake ? View.GONE : View.VISIBLE);
        findViewById(R.id.crop_image).setVisibility(View.GONE);

        videoPreviewContainer.setVisibility(View.GONE);
        surfaceView.setVisibility(View.GONE);
        showImagePreview();
    }

    private void showImagePreview() {
        try {
            //startLoadTask(previewFilePath);
            Bitmap myBitmap = null;
            if (showCrop) {
                cropImageView.setImageUriAsync(Uri.fromFile(new File(previewFilePath)));
            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // For Android 11 and more
                    Glide.with(this)
                            .asBitmap()
                            .load(previewFilePath)
                            
                            .thumbnail(0.1f)
                            .into(imagePreview);
                }else{
                    Glide.with(this)
                        .load(Uri.fromFile(new File(previewFilePath)))
                        .thumbnail(0.1f)
                        .into(imagePreview);
                }

            }

        } catch (OutOfMemoryError outOfMemoryError) {
            outOfMemoryError.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    private void startLoadTask(String previewFilePath) {
        if (showCrop) {
            cropImageView.setImageUriAsync(Uri.fromFile(new File(previewFilePath)));
        } else {
            LoadImageTask task = new LoadImageTask();
            task.execute(previewFilePath);
        }
    }

    private void displayVideo(Bundle savedInstanceState) {
        findViewById(R.id.crop_image).setVisibility(View.GONE);
        if (savedInstanceState != null) {
            loadVideoParams(savedInstanceState);
        }
        photoPreviewContainer.setVisibility(View.GONE);
        surfaceView.getHolder().addCallback(surfaceCallbacks);
    }

    private void showVideoPreview(SurfaceHolder holder) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(previewFilePath);
            mediaPlayer.setDisplay(holder);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaController = new MediaController(mContext);
                    mediaController.setAnchorView(surfaceView);
                    mediaController.setMediaPlayer(MediaPlayerControlImpl);

                    int videoWidth = mp.getVideoWidth();
                    int videoHeight = mp.getVideoHeight();

                    videoPreviewContainer.setAspectRatio((double) videoWidth / videoHeight);

                    mediaPlayer.start();
                    mediaPlayer.seekTo(currentPlaybackPosition);

                    if (!isVideoPlaying)
                        mediaPlayer.pause();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    finish();
                    return true;
                }
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, "Error media player playing video.");
            finish();
        }
    }

    private void saveCroppedImage(Uri croppedFileUri) {
        try {
            File saveFile = new File(previewFilePath);
            FileInputStream inStream = new FileInputStream(new File(croppedFileUri.getPath()));
            FileOutputStream outStream = new FileOutputStream(saveFile);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveVideoParams(Bundle outState) {
        if (mediaPlayer != null) {
            outState.putInt(VIDEO_POSITION_ARG, mediaPlayer.getCurrentPosition());
            outState.putBoolean(VIDEO_IS_PLAYED_ARG, mediaPlayer.isPlaying());
        }
    }

    private void loadVideoParams(Bundle savedInstanceState) {
        currentPlaybackPosition = savedInstanceState.getInt(VIDEO_POSITION_ARG, 0);
        isVideoPlaying = savedInstanceState.getBoolean(VIDEO_IS_PLAYED_ARG, true);
    }

    private void showButtonPanel(boolean show) {
        if (show) {
            buttonPanel.setVisibility(View.VISIBLE);
        } else {
            buttonPanel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        final Intent resultIntent = new Intent();
        if (view.getId() == R.id.confirm_media_result || view.getId() == R.id.button_save) {
            try {
                if (showCrop) {
                    previewFilePath = ScalingUtility.saveBitmap(cropImageView.getCroppedImage(), previewFilePath);
                    setResultDate(resultIntent);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    try {
                       Glide.with(PreviewActivity.this).asBitmap()
                               .load(previewFilePath)
				.into(new SimpleTarget<Bitmap>() {
				    @Override
				    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
					previewFilePath = ScalingUtility.saveBitmap(resource, previewFilePath);
					setResultDate(resultIntent);
					setResult(RESULT_OK, resultIntent);
					finish();
				    }
				});
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (view.getId() == R.id.re_take_media) {
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE_ARG, BaseSandriosActivity.ACTION_RETAKE);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if (view.getId() == R.id.cancel_media_action) {
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE_ARG, BaseSandriosActivity.ACTION_CANCEL);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private void setResultDate(Intent resultIntent) {
        resultIntent.putExtra(RESPONSE_CODE_ARG, BaseSandriosActivity.ACTION_CONFIRM);
        resultIntent.putExtra(FILE_PATH_ARG, previewFilePath);
        resultIntent.putExtra(CAPTION_ARG, captionText.getText().toString().trim());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteMediaFile();
    }

    private boolean deleteMediaFile() {
        if (isFromGallery) return false;

        File mediaFile = new File(previewFilePath);
        boolean isDeleted = mediaFile.delete();
        Utils.galleryAddPic(this, previewFilePath);
        return isDeleted;
    }

    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {

            return ScalingUtility.getSampledBitmap(params[0], imageWidth,
                    imageHeight);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mainBitmap != null) {
                if (!mainBitmap.isRecycled()) {
                    mainBitmap.recycle();
                    System.gc();
                }
                mainBitmap = null;

            }
            mainBitmap = result;
            imagePreview.setImageBitmap(result);
        }
    }// end inner class
}
