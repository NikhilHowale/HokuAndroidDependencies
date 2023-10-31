package com.hokuapps.startvideocall.twilioVideo;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.WorkManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.hokuapps.startvideocall.R;
import com.hokuapps.startvideocall.backgroundtask.AsyncImageLoader;
import com.hokuapps.startvideocall.delegate.IWebSocketClientEvent;
import com.hokuapps.startvideocall.model.Error;
import com.hokuapps.startvideocall.network.RestApiClientEvent;
import com.hokuapps.startvideocall.pref.CallPreference;
import com.hokuapps.startvideocall.twilioVideo.audio.OutgoingRinger;
import com.hokuapps.startvideocall.twilioVideo.audio.SignalAudioManager;
import com.hokuapps.startvideocall.twilioVideo.model.CallParams;
import com.hokuapps.startvideocall.twilioVideo.model.UserInfo;
import com.hokuapps.startvideocall.utils.AppConstant;
import com.hokuapps.startvideocall.utils.Utility;
import com.twilio.video.VideoView;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class VideoSessionActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VideoSessionActivity";


    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
    private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE_FROM_ON_RESUME = 2;
    public static final String EXTRA_CALL_PARAMS = "extra_call_params";

    /*
     * A VideoView receives frames from a local or remote video track and renders them
     * to an associated view.
     */
    private VideoView primaryVideoView;
    private VideoView thumbnailVideoView;


    //Header UI components
    private TextView videoStatusTv;
    private TextView videoCallerNameTv;
    private View headerLayout;
    private ImageView profileImageIv;

    //Button UI components
    private TextView callTimerText;

    private CallParams callParams;
    private UserInfo callingUserInfo;

    private FrameLayout mVideoContainer;

    //session managers
    private VideoSessionManager videoSessionManager;
    private static SignalAudioManager signalAudioManager;

    private Handler layoutHandler;
    private Runnable layoutRunnable;
    private Handler callHandler;
    private Runnable callRunnable;


    private AppCompatImageView mLocal_Camera_Btn, mLocal_Volume_Btn, mLocal_Video_Btn, mLocal_Mic_Btn;
    private AppCompatImageButton mLocal_CallEnd_Btn;
    private BottomSheetBehavior behavior;
    private ConstraintLayout bottomSheet;
    private final String btn_background_transparent_color = "#00FFFFFF" ;
    private String btn_background_selected = "#FFFFFFF";

    private CardView mCenterCardView;
    private CardView  mCallingCardView;
    private ImageView mCenterImageView;
    private AppCompatImageView mCallAcceptImageView,mCallRejectImageView,mCallAudioAcceptImageview;
    private ConstraintLayout mCallReceiveRejectContainer;
    private LinearLayout callRejectLayout;
    private LinearLayout acceptVideoLayout;
    private LinearLayout acceptAudioLayout;

    private ImageView redDoteImageView;
    private ConstraintLayout recordingContainer;
    private Animation animBlink;

    private AlertDialog dialog;


    /**
     * This broadcasterReceiver receive call action like call accepted, rejected or ringing
     */
    public  BroadcastReceiver callHandlerBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstant.TWILIO_CALL_ACTION)){
                int call_action = intent.getIntExtra(AppConstant.CALL_ACTION,1);

                if(call_action == AppConstant.CALL_ACCEPTED){

                }

                if(call_action == AppConstant.CALL_REJECTED) {
                    videoStatusTv.setText(R.string.status_dis_connected);
                    if(videoSessionManager != null) {
                        Log.e(TAG, "onClick: CALL_END"  );
                        videoSessionManager.onEndCall();
                    }else {
                        finishAndRemoveTask();
                    }
                }

                if(call_action == AppConstant.RINGING){
                    if(signalAudioManager != null){
                        startRinger(VideoSessionManager.CallType.OUTGOING);
                    }
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // to wake up the screen
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "myapp:mywaking");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        // to release the screen lock
        KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("myapp:mywaking");
        keyguardLock.disableKeyguard();

        super.onCreate(savedInstanceState);
        //handleStatusBar();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_video);

        WorkManager.getInstance(this).cancelAllWork();
        loadBundleData();
        layoutHandler = new Handler();

        initHeaderLayout();
        initVideoView();
        initView();
        initBottomSheetDialog();

        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        initSessionManagers();

        initializeCallSession();


        if(callParams.isVideo() ){
            if(callParams.isIncomingCall()){
                if(callParams.isCallFromNotification()){
                    showHideView(bottomSheet, !callParams.isCallFromNotification());
                    showHideView(mCallReceiveRejectContainer, callParams.isIncomingCall());

                }else {
                    showHideView(bottomSheet, !callParams.isIncomingCall());
                    showHideView(mCallReceiveRejectContainer, !callParams.isIncomingCall());
                }
            }else {
                showHideView(mCallReceiveRejectContainer, callParams.isIncomingCall());
                showHideView(bottomSheet, !callParams.isIncomingCall());
            }

            showHideView(findViewById(R.id.frameoverlay), !callParams.isVideo());

        }

        if(!callParams.isVideo()){
            showHideView(findViewById(R.id.frameoverlay), !callParams.isVideo());
            thumbnailVideoView.setVisibility(View.GONE);
            primaryVideoView.setVisibility(View.GONE);
            if(callParams.isIncomingCall()){
                if(callParams.isCallFromNotification()){
                    showHideView(bottomSheet, !callParams.isCallFromNotification());
                    showHideView(mCallAudioAcceptImageview, !callParams.isVideo());
                    showHideView(mCallAcceptImageView, callParams.isVideo());
                    showHideView(mCallReceiveRejectContainer, callParams.isIncomingCall());

                }else {
                    //showHideView(bottomSheet, callParams.isCallFromNotification());
                    showHideView(mCallReceiveRejectContainer, !callParams.isIncomingCall());
                    showHideView(mCallAcceptImageView, callParams.isVideo());
                }
            }else {
                showHideView(bottomSheet, !callParams.isIncomingCall());
                showHideView(mCallAudioAcceptImageview, !callParams.isVideo());
                showHideView(mCallAcceptImageView, callParams.isVideo());

            }
        }

        registerBroadCastReceiver();

    }

    private Subscription timerSubscribe;

    /**
     *  This method show call timer when call connected
     */
    private void startCallTimer() {
        if (timerSubscribe == null) {
            timerSubscribe = Observable.interval(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(timeInSecond -> {
                        callTimerText.setText(hmsTimeFormatter(timeInSecond * 1000));
                        System.out.println("Timer : " + timeInSecond);
                    });
        }
    }

    /**
     * method to convert millisecond to time format
     *
     * @param milliSeconds
     * @return HH:mm:ss time formatted string
     */
    private String hmsTimeFormatter(long milliSeconds) {

        @SuppressLint("DefaultLocale") String hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return hms;
    }

    private void cancelCallTimer() {
        if (timerSubscribe != null) {
            timerSubscribe.unsubscribe();
        }
        timerSubscribe = null;
    }

    public void registerBroadCastReceiver(){
        registerReceiver(callHandlerBroadCast, new IntentFilter(AppConstant.TWILIO_CALL_ACTION));
    }

    public void unRegisterBroadcastReceiver(){
        unregisterReceiver(callHandlerBroadCast);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoSessionManager != null && !callParams.isIncomingCall()) {
            videoSessionManager.onSessionResume();
        }

        if(videoSessionManager != null && callParams.isIncomingCall() && !callParams.isCallFromNotification()){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!checkPermissionForCameraAndMicrophone()) {
                        requestPermissionForCameraAndMicrophoneFromOnResume();
                    }else {
                        if(callParams.isVideo()){
                            showRecordingDialog(getString(R.string.video_message),getString(R.string.video_message_warning));
                        }else {
                            showRecordingDialog(getString(R.string.voice_message),getString(R.string.voice_message_warning));
                        }
                    }
                }
            },100);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoSessionManager != null) {
            videoSessionManager.onSessionPause();
        }
    }

    @Override
    protected void onDestroy() {

        if (videoSessionManager != null) {
            videoSessionManager.onSessionDestroy();
        }

       signalAudioManager.stop(false);

        //stop timer
        cancelCallTimer();

        if(layoutHandler != null){
            layoutHandler.removeCallbacks(layoutRunnable);
            layoutHandler = null;
        }

        if(callHandler != null && callRunnable != null){
            callHandler.removeCallbacks(callRunnable);
            callHandler = null;
        }

        unRegisterBroadcastReceiver();


        if(dialog != null){
            dialog.dismiss();
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.local_call_reject){
            Log.e(TAG, "onClick: CALL_END"  );
            videoSessionManager.onEndCall();
        }else if(id == R.id.local_call_accept || id == R.id.local_audio_call_accept){

            if(callParams.isVideo()){
                showRecordingDialog(getString(R.string.video_message),getString(R.string.video_message_warning));
            }else {
                showRecordingDialog(getString(R.string.voice_message),getString(R.string.voice_message_warning));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            boolean cameraAndMicPermissionGranted = true;

            for (int grantResult : grantResults) {
                cameraAndMicPermissionGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
            }

            if (cameraAndMicPermissionGranted) {

                if(requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE){
                    videoSessionManager.setPrimaryVideoView(primaryVideoView);
                    videoSessionManager.setThumbnailVideoView(thumbnailVideoView);
                    videoSessionManager.setiVideoSessionCallback(iVideoSessionCallback);
                    videoSessionManager.setCallParams(callParams);
                    videoSessionManager.onSessionCreate();
                    videoSessionManager.setAudioVoiceCallOnly(callParams.isVideo());
                }

                if(requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE_FROM_ON_RESUME){

                    if (checkPermissionForCameraAndMicrophone()) {
                        videoSessionManager.setPrimaryVideoView(primaryVideoView);
                        videoSessionManager.setThumbnailVideoView(thumbnailVideoView);
                        videoSessionManager.setiVideoSessionCallback(iVideoSessionCallback);
                        videoSessionManager.setCallParams(callParams);
                        videoSessionManager.onSessionCreate();
                        videoSessionManager.setAudioVoiceCallOnly(callParams.isVideo());

                        if(callParams.isVideo()){
                            showRecordingDialog(getString(R.string.video_message),getString(R.string.video_message_warning));
                        }else {
                            showRecordingDialog(getString(R.string.voice_message),getString(R.string.voice_message_warning));
                        }
                    }

                }


            } else {
                Toast.makeText(this,"permission needed", Toast.LENGTH_LONG).show();
                finishAndRemoveTask();
            }

    }


    private boolean checkPermissionForCameraAndMicrophone() {
        int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return resultCamera == PackageManager.PERMISSION_GRANTED && resultMic == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissionForCameraAndMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            //Toast.makeText(this,"Permission needed", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_MIC_PERMISSION_REQUEST_CODE );
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_MIC_PERMISSION_REQUEST_CODE );
        }
    }

    private void requestPermissionForCameraAndMicrophoneFromOnResume() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            //Toast.makeText(this,"Permission needed", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_MIC_PERMISSION_REQUEST_CODE_FROM_ON_RESUME );
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    CAMERA_MIC_PERMISSION_REQUEST_CODE_FROM_ON_RESUME );
        }
    }

    /*
     * Set the initial UI components
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {

        callTimerText = findViewById(R.id.text_timer);
        mVideoContainer = findViewById(R.id.video_container);
        mCallReceiveRejectContainer = findViewById(R.id.layout_incomming);
        mCallAcceptImageView = findViewById(R.id.local_call_accept);
        mCallRejectImageView = findViewById(R.id.local_call_reject);
        mCallAudioAcceptImageview = findViewById(R.id.local_audio_call_accept);
        redDoteImageView = findViewById(R.id.redDot);
        recordingContainer = findViewById(R.id.recording_container);

        mCallAcceptImageView.setOnClickListener(this);
        mCallRejectImageView.setOnClickListener(this);
        mCallAudioAcceptImageview.setOnClickListener(this);

        callRejectLayout = findViewById(R.id.animReject);
        acceptVideoLayout = findViewById(R.id.animVideoAccept);
        acceptAudioLayout = findViewById(R.id.animAudioAccept);

        animBlink = AnimationUtils.loadAnimation(this, R.anim.anim_blink);

        redDoteImageView.startAnimation(animBlink);


        mVideoContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(!callParams.isVideo()){
                    behavior.setDraggable(false);
                    return true;
                }

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_UP:
                        behavior.setDraggable(true);
                        if(behavior.getState() == BottomSheetBehavior.STATE_HIDDEN){
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }else if(behavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }else if(behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                            behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }
                        break;
                }
                return true;
            }
        });

        AsyncImageLoader asyncImageLoader = new AsyncImageLoader(this);
        asyncImageLoader.displayImage(callParams.getCallerProfileImage(), callParams.getCallerName(), profileImageIv);
    }

    private void initVideoView() {
        primaryVideoView = findViewById(R.id.primary_video_view);
        thumbnailVideoView = findViewById(R.id.thumbnail_video_view);
    }

    private void initHeaderLayout() {
        videoStatusTv = findViewById(R.id.tvCallState);
        videoCallerNameTv = findViewById(R.id.tvCallerName);
        headerLayout = findViewById(R.id.headerLayout);
        profileImageIv = findViewById(R.id.profile_image_iv);
        mCallingCardView = findViewById(R.id.calling_image_container);
    }

    private void initSessionManagers() {
        videoSessionManager = VideoSessionManager.getInstance(this);
        signalAudioManager = new SignalAudioManager(this);
        signalAudioManager.initializeAudioManager();
    }

    /**
     * This method add listener to bottom sheet
     */
    private void initBottomSheetDialog(){
        bottomSheet = findViewById(R.id.bot_container);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {

                }
                if(newState == BottomSheetBehavior.STATE_EXPANDED){

                    if(layoutRunnable != null){
                        layoutHandler.removeCallbacks(layoutRunnable);
                    }
                   startBottomLayoutTimer();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                float yTranslation = slideOffset <=0 ? bottomSheet.getY() + behavior.getPeekHeight() - (thumbnailVideoView.getHeight() + 170)
                        : bottomSheet.getHeight() - (thumbnailVideoView.getHeight()+ 170);
                thumbnailVideoView.animate().y(yTranslation).setDuration(0).start();
            }
        });

        mLocal_Camera_Btn = findViewById(R.id.local_switch_camera);
        mLocal_Volume_Btn = findViewById(R.id.local_volume_control);
        mLocal_Video_Btn = findViewById(R.id.local_video_control);
        mLocal_Mic_Btn = findViewById(R.id.local_mic_control);
        mLocal_CallEnd_Btn = findViewById(R.id.local_call_end_control);

        if(callParams.isVideo()){
            mLocal_Volume_Btn.setVisibility(View.GONE);
            mLocal_Camera_Btn.setVisibility(View.VISIBLE);
            btn_background_selected = "#FFFFFF";
            mLocal_Video_Btn.setImageResource(R.drawable.video_not_connected);
            mLocal_Video_Btn.setEnabled(false);
        }

        if(!callParams.isVideo()){
            mLocal_Volume_Btn.setVisibility(View.VISIBLE);
            mLocal_Camera_Btn.setVisibility(View.GONE);
            mLocal_Video_Btn.setImageResource(R.drawable.video_not_connected);
            mLocal_Video_Btn.setEnabled(false);
            btn_background_selected = "#6D6C6C";
        }

        setUpListener();

    }



    private void initCenterImage(){
        mCenterCardView = findViewById(R.id.image_center_container);
        mCenterImageView = findViewById(R.id.caller_image);
        showHideView(mCenterCardView,!callParams.isVideo());
        AsyncImageLoader asyncImageLoader = new AsyncImageLoader(this);
        asyncImageLoader.displayImage(callParams.getCallerProfileImage(), callParams.getCallerName(), mCenterImageView);
    }

    /**
     * Initialize call session
     */
    private void initializeCallSession() {

        videoSessionManager.setCallingUserInfo(callingUserInfo);

        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraAndMicrophone();
        } else {
            videoSessionManager.setPrimaryVideoView(primaryVideoView);
            videoSessionManager.setThumbnailVideoView(thumbnailVideoView);
            videoSessionManager.setiVideoSessionCallback(iVideoSessionCallback);
            videoSessionManager.setCallParams(callParams);
            videoSessionManager.onSessionCreate();
            videoSessionManager.setAudioVoiceCallOnly(callParams.isVideo());
        }


    }


    private void startRinger(VideoSessionManager.CallType type) {
        videoStatusTv.setText(R.string.status_ringing);
        videoCallerNameTv.setText(callParams.getCallerName());
        switch (type) {
            case INCOMING:
                signalAudioManager.startIncomingRinger();
                break;
            case OUTGOING:
                if(callParams.isVideo()){
                    signalAudioManager.startOutgoingRinger(OutgoingRinger.Type.RINGING);
                }else {
                    signalAudioManager.startOutgoingRinger(OutgoingRinger.Type.SONAR);
                }

                break;
        }
    }

    private void showHideView(View view, boolean isShow) {
        view.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    private VideoSessionManager.IVideoSessionCallback iVideoSessionCallback = new VideoSessionManager.IVideoSessionCallback() {
        @Override
        public void onVideoCallStatus(VideoSessionManager.VideoCallStatus videoCallStatus) {

            if (!Utility.isActivityLive(VideoSessionActivity.this)) return;

            switch (videoCallStatus) {
                case IDLE:

                    videoCallerNameTv.setText(callParams.getCallerName());
                    videoStatusTv.setText(R.string.status_connecting);
                    if(!callParams.isIncomingCall()) {
                        if (callParams.isVideo()) {
                            showRecordingDialog(getString(R.string.video_message), getString(R.string.video_message_warning));
                        } else {
                            showRecordingDialog(getString(R.string.voice_message), getString(R.string.voice_message_warning));
                        }
                    }

                    break;

                case  LOCAL_PARTICIPANT_CONNECTED:
                    callHandler = new Handler();
                    startCallHandler();
                    break;

                case DISCONNECTED :
                    videoStatusTv.setText(R.string.status_cut);
                    finishAndRemoveTask();
                    break;

                case CONNECTING:
                    videoStatusTv.setText(R.string.status_connecting);
                    videoCallerNameTv.setText(callParams.getCallerName());
                    break;

                case CONNECTED:
                   /* CallPreference callPreference = new CallPreference(VideoSessionActivity.this);
                    callPreference.setUserCallParams("");*/
                    AppConstant.CallData.NOTIFICATION_DATA = "";
                    if(!callParams.isVideo()){
                        videoSessionManager.setSpeakerOff();
                        volume_on_off_State(false);
                    }

                    if(callHandler != null && callRunnable != null){
                        callHandler.removeCallbacks(callRunnable);
                        callHandler =null;
                        Log.e("CALL_DISCONNECT", "CALL CONNECTED" );
                    }

                    videoStatusTv.setText(R.string.status_connected);
                    videoCallerNameTv.setText(callParams.getCallerName());
                    showHideView(mCallingCardView, callParams.isVideo());
                    showHideView(recordingContainer, true);
                    initCenterImage();
                    if(callParams.isVideo()) {
                        showHideView(headerLayout,!callParams.isVideo());
                        mLocal_Video_Btn.setImageResource(R.drawable.video_on);
                        mLocal_Video_Btn.setEnabled(true);
                        behavior.setDraggable(true);
                        startBottomLayoutTimer();
                        showHideView(findViewById(R.id.frameoverlay), false);
                    }
                    
                    if(!callParams.isVideo()){
                        startCallTimer();
                        showHideView(videoStatusTv, callParams.isVideo());
                        callTimerText.setVisibility(View.VISIBLE);
                        showHideView(findViewById(R.id.frameoverlay), true);
                    }

                    signalAudioManager.startCommunication(false, false);

                    break;

                case INCOMING_RINGING:
                    //startRinger(VideoSessionManager.CallType.INCOMING);
                   /*new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Log.e("CALL CANCEL", "OUTGOING CALL CANCEL" );
                        iVideoSessionCallback.onVideoCallStatus(VideoSessionManager.VideoCallStatus.HANGING);
                    }, 30000);*/
                    break;

                case OUTGOING_RINGING:
                    //startRinger(VideoSessionManager.CallType.OUTGOING);
                    break;

                case HANGING:
                    signalAudioManager.stop(false);

                    if(callHandler != null && callRunnable != null){
                        Log.e("CALL_DISCONNECT", "HANGING " );
                        callHandler.removeCallbacks(callRunnable);
                        callHandler = null;
                    }

                    try {
                        JSONObject senderObject = new JSONObject();
                        senderObject.put(AppConstant.JSONFiled.ROOM_NAME,callParams.getRoomName());
                        senderObject.put(AppConstant.JSONFiled.ACTION_STATUS, AppConstant.CALL_REJECTED);
                        senderObject.put(AppConstant.JSONFiled.USER_ID, callingUserInfo.getUserId());
                        senderObject.put(AppConstant.JSONFiled.CALL_UNIQUE_ID,callParams.getCallUniqueId());
                       // Utility.callReject(AppConstant.CALL_REJECT_URL,senderObject);
                        RestApiClientEvent apiClientEvent = new RestApiClientEvent(VideoSessionActivity.this,/*"https://console.restoration-os.org/api/customsnippet_silentcallnotifications_hiddenfield_62321e0005023a1884c1d19c"*/AppConstant.CALL_STATUS_URL);
                        apiClientEvent.setRequestJson(senderObject);
                        apiClientEvent.setListener(new IWebSocketClientEvent() {
                            @Override
                            public void onSuccess(JSONObject jsonObject) {
                                Log.e(TAG, "onSuccess: " + jsonObject );
                            }

                            @Override
                            public void onFinish(Error error) {
                                Log.e(TAG, "onFinish: " + error);
                            }
                        });
                        apiClientEvent.fire();

                    }catch (Exception exception){
                        exception.printStackTrace();
                    }
                    finishAndRemoveTask();

                    break;

                case MUTE:
                    mic_Mute_UnMute_State(false);
                    break;
                case UN_MUTE:
                    mic_Mute_UnMute_State(true);
                    break;

                case VIDEO_OF:
                    video_on_off_state(false);
                    break;

                case VIDEO_ON:
                    video_on_off_state(true);
                    break;

                case PARTICIPANT_VIDEO_OFF:
                    showHideView(headerLayout, true);
                    videoStatusTv.setText(R.string.participant_video_off);
                    showHideView(mCenterCardView,true);
                    showHideView(mCallingCardView, false);
                    break;

                case PARTICIPANT_VIDEO_ON:
                    showHideView(headerLayout, false);
                    showHideView(mCenterCardView,false);
                    showHideView(mCallingCardView, true);
                    break;
            }
        }
    };


    private void setUpListener(){

        mLocal_Camera_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoSessionManager.isBackCamera();
            }
        });

        mLocal_Volume_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isSpeakerOn = videoSessionManager.isSpeakerOn();
                if(isSpeakerOn){
                    volume_on_off_State(true);
                }else {
                    volume_on_off_State(false);
                }

            }
        });

        mLocal_Video_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoSessionManager.setLocalVideoTrack();
            }
        });

        mLocal_Mic_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoSessionManager.setMuteUnMute();
            }
        });

        mLocal_CallEnd_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoSessionManager != null) {
                    Log.e(TAG, "onClick: CALL_END"  );
                    videoSessionManager.onEndCall();
                }
            }
        });
    }

    /**
     * This method enable/disable volume state
     * @param isVolume if flag is true sound is enable otherwise sound is off
     */
    private void volume_on_off_State(boolean isVolume){
        if (isVolume) {
            mLocal_Volume_Btn.setImageResource(R.drawable.volume_on);
            mLocal_Volume_Btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(btn_background_selected)));
            return;
        }
        mLocal_Volume_Btn.setImageResource(R.drawable.volume_on);
        mLocal_Volume_Btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(btn_background_transparent_color)));
    }

    /**
     * This method enable/disable mic state
     * @param isMute if flag is true user is unMute otherwise unMute
     */
    public void mic_Mute_UnMute_State(boolean isMute) {
        if(isMute){
            if(callParams.isVideo()) {
                mLocal_Mic_Btn.setImageResource(R.drawable.mic_off);
            }
            mLocal_Mic_Btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(btn_background_selected)));
            return;
        }
        mLocal_Mic_Btn.setImageResource(R.drawable.mic_on);
        mLocal_Mic_Btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(btn_background_transparent_color)));
    }

    /**
     * This method enable/disable video view
     * @param isVideo_Of_On if true then video otherwise off
     */
    private void video_on_off_state(boolean isVideo_Of_On){
        if(isVideo_Of_On){
            if(callParams.isVideo()) {
                mLocal_Video_Btn.setImageResource(R.drawable.video_off);
            }
            mLocal_Video_Btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(btn_background_selected)));
            return;
        }

        mLocal_Video_Btn.setImageResource(R.drawable.video_on);
        mLocal_Video_Btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(btn_background_transparent_color)));
    }

    /**
     *  This method extract bundle data from intent and add to model
     */
    public void loadBundleData() {
        CallPreference callPreference = new CallPreference(VideoSessionActivity.this);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            callParams = bundle.getParcelable(VideoSessionActivity.EXTRA_CALL_PARAMS);
            callingUserInfo = bundle.getParcelable(AppConstant.EXTRA_USER_DETAILS);
        }

        if(callParams == null || callingUserInfo == null) {
            String CallingData = AppConstant.CallData.NOTIFICATION_DATA;
                    //callPreference.getUserCallParams();
            try {
                JSONObject jsonObject = new JSONObject(CallingData);

                String tokenUrl = Utility.getStringObjectValue(jsonObject,  AppConstant.JSONFiled.TWILIO_TOKEN_URL);
                String roomName = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.ROOM_NAME);

                CallParams callParams = new CallParams();

                callParams.setVideo(true);
                if (jsonObject.has(AppConstant.JSONFiled.IS_AUDIO_ONLY) && jsonObject.getBoolean(AppConstant.JSONFiled.IS_AUDIO_ONLY)) {
                    callParams.setVideo(false);
                }

                callParams.setRoomName(roomName);
                callParams.setTokenUrl(tokenUrl);
                callParams.setCallerName(Utility.getStringObjectValue(jsonObject,  AppConstant.JSONFiled.CALLER_NAME));
                callParams.setCallerProfileImage(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALLER_IMAGE));
                callParams.setCallUniqueId(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALL_UNIQUE_ID));
                callParams.setAccessToken(null);
                callParams.setIncomingCall(true);
                callParams.setCallFromNotification(false);

                UserInfo userInfo = new UserInfo();
                userInfo.setName(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.USER_NAME));
                userInfo.setUserId(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.USER_ID));
                userInfo.setSecretKey(AppConstant.CallData.AUTH_SECRET_KEY);
                userInfo.setTokenKey(AppConstant.CallData.AUTH_TOKEN);

                this.callParams = callParams;
                this.callingUserInfo = userInfo;


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        AppConstant.CallData.CALL_UNIQUE_ID = callParams.getCallUniqueId();
        //callPreference.setValue("callUniqueID", callParams.getCallUniqueId());
    }

    /**
     *  This method open call window activity
     * @param context context
     * @param bundle bundle extra data
     */
    public static void startActivity(Activity context, Bundle bundle) {

        Intent intent = new Intent(context, VideoSessionActivity.class);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }


    /**
     *  This method hide bottom sheet by specified time
     */
    public void startBottomLayoutTimer(){
        layoutRunnable = new Runnable() {
            @Override
            public void run() {
                behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        };
        layoutHandler.postDelayed(layoutRunnable,AppConstant.BOTTOM_MENU_COLLAPSE_DELAY);
    }


    /**
     * This method show dialog before connect the call for accept record call
     * @param message message for dialog to display message
     * @param warning message for dialog to display warning
     */
    private void showRecordingDialog(String message, String warning){
        if(dialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);


            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_screen_recording, null, false);

            builder.setView(view);
            TextView textMessage = view.findViewById(R.id.label_recording_message);
            TextView textWarning = view.findViewById(R.id.label_recording_warning);
            Button btnLeave = view.findViewById(R.id.leaveMeeting);
            Button btnAccept = view.findViewById(R.id.acceptRecording);
            textMessage.setText(message);
            textWarning.setText(warning);



            btnLeave.setOnClickListener( v -> {
                iVideoSessionCallback.onVideoCallStatus(VideoSessionManager.VideoCallStatus.HANGING);
                dialog.dismiss();
            });

            btnAccept.setOnClickListener( v -> {

                playRecordingAudio(R.raw.recording_start);
                if(callParams.isIncomingCall() && !callParams.isCallFromNotification()){
                    bottomSheet.setVisibility(View.VISIBLE);
                }
                dialog.dismiss();
            });

            dialog = builder.show();
            if(dialog.getWindow() != null)
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    /**
     * This method play sound to notify user about recording has been started
     * @param soundId sound resource id
     */
    private void playRecordingAudio(int soundId){
        MediaPlayer mediaPlayer = MediaPlayer.create(this,soundId);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e(TAG, "onCompletion: "  );
                if(mp != null){
                   /* if(soundId == R.raw.recording_start){
                        //startRinger(VideoSessionManager.CallType.INCOMING);
                        if(callParams.isIncomingCall()){
                                mCallReceiveRejectContainer.setVisibility(View.GONE);
                                bottomSheet.setVisibility(View.VISIBLE);
                                videoStatusTv.setText(R.string.status_connecting);
                                videoCallerNameTv.setText(callParams.getCallerName());
                                signalAudioManager.startCommunication(false, true);
                                videoSessionManager.onSessionResume();
                                videoSessionManager.connectToRoom();
                        }else {
                           // startRinger(VideoSessionManager.CallType.OUTGOING);
                            videoSessionManager.fetchToken(callParams.getTokenUrl());

                        }
                    }

                    if(soundId == R.raw.recordin_stop){
                        videoSessionManager.onEndCall();
                    }
*/
                    mp.release();
                }
            }
        });

        if(callParams.isIncomingCall()){
            mCallReceiveRejectContainer.setVisibility(View.GONE);
            bottomSheet.setVisibility(View.VISIBLE);
            videoStatusTv.setText(R.string.status_connecting);
            videoCallerNameTv.setText(callParams.getCallerName());
            signalAudioManager.startCommunication(false, true);
            videoSessionManager.onSessionResume();
            videoSessionManager.connectToRoom();
        }
        else {
            // startRinger(VideoSessionManager.CallType.OUTGOING);
            videoSessionManager.fetchToken(callParams.getTokenUrl());

        }
    }

    /**
     *  This method start handler for specified time when user connected to room and waiting for participant to connect
     *  if participant connected to room handler remove callback other wise call disconnect after specified
     */
    private void startCallHandler(){
        if(callRunnable == null){
            callRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "onClick: CALL_END"  );
                    videoStatusTv.setText(R.string.status_cut);
                    videoSessionManager.onEndCall();
                }
            };
            callHandler.postDelayed(callRunnable,AppConstant.CALL_END_DELAY);
        }
    }


}
