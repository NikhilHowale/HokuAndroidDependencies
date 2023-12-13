package com.hokuapps.capturevideo.activity;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.hokuapps.capturevideo.R;
import com.hokuapps.capturevideo.camera.CameraLogicActivity;
import com.hokuapps.capturevideo.utils.ProgressUpdate;
import com.hokuapps.capturevideo.utils.RecordFileUtil;

import java.io.File;
import java.util.TimerTask;

public class CameraActivity extends CameraLogicActivity {

    public static final String INTENT_PATH = "intent_path";
    public static final String INTENT_DATA_TYPE = "result_data_type";
    public static final String INTENT_FILE_CAPTION = "file_caption";

    public static final int RESULT_TYPE_VIDEO = 1;
    public static final int REQUEST_CODE_VIDEO = 100;
    public static final int REQUEST_CODE_PHOTO = 101;
    private static final int INTERVAL_UPDATE = 100;
    public static final float MAX_VIDEO_TIME = 16f * 1000;

    private ImageView ivSwitchFlash;
    private TextView  tv_RecTime;
    private ImageView recordView;
    private boolean isCapturePhoto = false;

    private String mOutputFilePath;
    private ProgressUpdate progressUpdate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setUpView();

        File tempPath = RecordFileUtil.getHtmlDirFromSandbox(this);
        RecordFileUtil.setFileDir(tempPath.getPath());

    }

    @Override
    public void onPause() {
        super.onPause();
        cleanRecord();
    }

    @Override
    public int getTextureResource() {
        return R.id.mTextureView;
    }

    @Override
    public void onCameraPreview(SurfaceTexture surfaceTexture) {

    }

    @Override
    public Size maxCameraSize() {
        return new Size(1920, 1080);
    }

    @Override
    protected void setUpView() {
        super.setUpView();
        recordView = findViewById(R.id.recordView);
        ivSwitchFlash = findViewById(R.id.iv_flash_video);
        tv_RecTime = findViewById(R.id.tvRecTime);

        recordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsRecordingVideo) {
                    try {
                      upEvent();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {

                    startRecordingVideo();
                }
            }
        });



       /* ivSwitchCamera.setOnClickListener(v -> {
            switchCamera();
            if (isFlashSupported) {
                ivSwitchFlash.setImageResource(R.drawable.ic_flash_off);
                ivSwitchFlash.setClickable(true);
                ivSwitchFlash.setColorFilter(Color.WHITE);
            } else {
                ivSwitchFlash.setImageResource(R.drawable.ic_flash_off);
                ivSwitchFlash.setClickable(false);
                ivSwitchFlash.setColorFilter(getResources().getColor(R.color.colorDisable));
            }
        });*/

        ivSwitchFlash.setOnClickListener(v -> {
            boolean flash = switchFlash();
            ivSwitchFlash.setImageResource(flash ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        });

        findViewById(R.id.iv_back).setOnClickListener(v -> cancelRecord());
    }

    private void upEvent() {
        runOnUiThread(this::finishVideo);
    }

    /**
     * This method start recording video
     */
    @Override
    public void startRecordingVideo() {
        super.startRecordingVideo();

        //Receive out put file here
        mOutputFilePath = getCurrentFile();

        videoDuration = 0;
        recordTime = System.currentTimeMillis();
        progressUpdate = new ProgressUpdate();
        recordView.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.stop_recording));
        progressUpdate.startUpdate(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                videoDuration += currentTime - recordTime;
                recordTime = currentTime;
                if (videoDuration < MAX_VIDEO_TIME) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_RecTime.setVisibility(View.VISIBLE);
                            tv_RecTime.setText("00 : "+videoDuration/1000);
                        }
                    });
                } else {
                    upEvent();
                }
            }
        }, INTERVAL_UPDATE);
    }

    /**
     * This method stop recording
     */
    @Override
    public void stopRecordingVideo() {
        if (null != progressUpdate) {
            progressUpdate.stopUpdate();
        }

        videoDuration = 0;
        recordTime = System.currentTimeMillis();
        tv_RecTime.setVisibility(View.GONE);
        recordView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.start_recording));
        super.stopRecordingVideo();
    }

    private long videoDuration;
    private long recordTime;


    /**
     * This method clean up when activity open
     */
    private void cleanRecord() {

        mOutputFilePath = null;
        isCapturePhoto = false;
        ivSwitchFlash.setVisibility(View.VISIBLE);
        tv_RecTime.setVisibility(View.GONE);
    }

    /**
     * This method called when video recording finish
     * open new activity to preview recorded video
     */
    public void finishVideo() {
        stopRecordingVideo();
        Intent intent = new Intent(CameraActivity.this, ViewVideoActivity.class);
        intent.putExtra(ViewVideoActivity.INTENT_PATH, Uri.fromFile(new File(mOutputFilePath)));
        intent.putExtra(ViewVideoActivity.INTENT_RESULT, true);
        startActivityForResult(intent, REQUEST_CODE_VIDEO);

    }


    /**
     * Create directory and return file
     * returning video file
     */
    protected String getOutputMediaFile() {
        return RecordFileUtil.createMp4FileInBox();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelRecord();
    }

    private void cancelRecord() {
        setResult(RESULT_CANCELED);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_VIDEO) {
                Intent intent = new Intent(data);
                setResult(RESULT_OK, intent);
                finish();
            } else if (requestCode == REQUEST_CODE_PHOTO) {
                Intent intent = new Intent(data);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else {
            cleanRecord();
        }
    }

}