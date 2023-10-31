package com.hokuapps.startvideocall.twilioVideo;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.hokuapps.startvideocall.delegate.IWebSocketClientEvent;
import com.hokuapps.startvideocall.model.Error;
import com.hokuapps.startvideocall.network.RestApiClientEvent;
import com.hokuapps.startvideocall.twilioVideo.audio.ServiceUtil;
import com.hokuapps.startvideocall.twilioVideo.model.CallParams;
import com.hokuapps.startvideocall.twilioVideo.model.ParticipantViewState;
import com.hokuapps.startvideocall.twilioVideo.model.UserInfo;
import com.hokuapps.startvideocall.utils.AppConstant;
import com.hokuapps.startvideocall.utils.CameraCapturerCompat;
import com.hokuapps.startvideocall.utils.Utility;
import com.twilio.video.AudioCodec;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.OpusCodec;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoCodec;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;
import com.twilio.video.Vp8Codec;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;

import tvi.webrtc.VideoSink;


public class VideoSessionManager {
    private static final String TAG = VideoSessionManager.class.getSimpleName();

    private static VideoSessionManager mVideoSessionManager;


    /*
     * Audio and video tracks can be created with names. This feature is useful for categorizing
     * tracks of participants. For example, if one participant publishes a video track with
     * ScreenCapturer and CameraCapturer with the names "screen" and "camera" respectively then
     * other participants can use RemoteVideoTrack#getName to determine which video track is
     * produced from the other participant's screen or camera.
     */
    private static final String LOCAL_AUDIO_TRACK_NAME = "mic";
    private static final String LOCAL_VIDEO_TRACK_NAME = "camera";

    /*
     * Access token used to connect. This field will be set either from the console generated token
     * or the request to the token server.
     */
    private String accessToken;

    /*
     * A Room represents communication between a local participant and one or more participants.
     */
    private Room room;
    private LocalParticipant localParticipant;

    /*
     * AudioCodec and VideoCodec represent the preferred codec for encoding and decoding audio and
     * video.
     */
    private AudioCodec audioCodec;
    private VideoCodec videoCodec;

    /*
     * Encoding parameters represent the sender side bandwidth constraints.
     */
    private EncodingParameters encodingParameters;

    private CameraCapturerCompat cameraCapturerCompat;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;

    private AudioManager audioManager;
    private String remoteParticipantIdentity;
    private int previousAudioMode;
    private boolean previousMicrophoneMute;
    private VideoSink localVideoView;

    //callback for sessions
    public IVideoSessionCallback iVideoSessionCallback;
    private CallParams callParams;
    private UserInfo callingUserInfo;
    private Context mContext;
    private VideoView primaryVideoView;
    private VideoView thumbnailVideoView;
    private HashMap<String, ParticipantViewState> participant;



    /*  avoid to create direct object */
    private VideoSessionManager(Context context) {
        this.mContext = context;
    }

    public static VideoSessionManager getInstance(Context context) {

        //if (mVideoSessionManager == null) {
            mVideoSessionManager = new VideoSessionManager(context);
       // }

        return mVideoSessionManager;
    }

    public CallParams getCallParams() {
        return callParams;
    }

    public void setCallParams(CallParams callParams) {
        this.callParams = callParams;
    }

    public UserInfo getCallingUserInfo() {
        return callingUserInfo;
    }

    public void setCallingUserInfo(UserInfo callingUserInfo) {
        this.callingUserInfo = callingUserInfo;
    }

    public void onSessionCreate() {

        /*
         * Needed for setting/abandoning audio focus during call
         */
        audioManager = ServiceUtil.getAudioManager(mContext);


        //audioManager.setSpeakerphoneOn(true);
        if(callParams.isVideo()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Utility.setCommunicationDevice(audioManager, AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
            } else {
                audioManager.setSpeakerphoneOn(true);
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Utility.setCommunicationDevice(audioManager,  AudioDeviceInfo.TYPE_BUILTIN_EARPIECE);
            } else {
                audioManager.setSpeakerphoneOn(false);
            }
        }



        createAudioAndVideoTracks();

        iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.IDLE);

        if(callParams.isIncomingCall()) {
             fetchToken(callParams.getTokenUrl());
        }

        participant = new HashMap<>();

    }

    public void onSessionResume() {

         /*
         * Update preferred audio and video codec in case changed in settings
         */
        audioCodec = new OpusCodec();
        videoCodec = new Vp8Codec(false);

        /*
         * Get latest encoding parameters
         */
        final EncodingParameters newEncodingParameters = new EncodingParameters(0, 0);

        /*
         * If the local video track was released when the app was put in the background, recreate.
         */
        if (localVideoTrack == null) {
            localVideoTrack = LocalVideoTrack.create(mContext,
                    true,
                    cameraCapturerCompat,
                    LOCAL_VIDEO_TRACK_NAME);
            localVideoTrack.addSink(localVideoView);

            /*
             * If connected to a Room then share the local video track.
             */
            if (localParticipant != null) {
                localParticipant.publishTrack(localVideoTrack);

                /*
                 * Update encoding parameters if they have changed.
                 */
                if (!newEncodingParameters.equals(encodingParameters)) {
                    localParticipant.setEncodingParameters(newEncodingParameters);
                }
            }

            setAudioVoiceCallOnly(callParams.isVideo());
        }

        /*
         * Update encoding parameters
         */
        encodingParameters = newEncodingParameters;
    }

    public void onSessionPause() {
        /*
         * Release the local video track before going in the background. This ensures that the
         * camera can be used by other applications while this app is in the background.
         */
        if (localVideoTrack != null) {
            /*
             * If this local video track is being shared in a Room, unpublish from room before
             * releasing the video track. Participants will be notified that the track has been
             * unpublished.
             */
            if (localParticipant != null) {
                localParticipant.unpublishTrack(localVideoTrack);
            }

            localVideoTrack.release();
            localVideoTrack = null;
        }
    }

    public void onSessionDestroy() {
        configureAudio(false);

        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        if (room != null && room.getState() != Room.State.DISCONNECTED) {
            disconnectConnection();
        }

        /*
         * Release the local audio and video tracks ensuring any memory allocated to audio
         * or video is freed.
         */
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
            localVideoTrack = null;
        }

    }

    /**
     * Create audio and video tracks
     */
    public void createAudioAndVideoTracks() {


        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(mContext, true, LOCAL_AUDIO_TRACK_NAME);

            // Share your camera
            cameraCapturerCompat = new CameraCapturerCompat(mContext, CameraCapturerCompat.Source.FRONT_CAMERA);
            localVideoTrack = LocalVideoTrack.create(mContext,
                    true,
                    cameraCapturerCompat,
                    LOCAL_VIDEO_TRACK_NAME);

            //update in ui
            primaryVideoView.setMirror(true);
            localVideoTrack.addSink(primaryVideoView);
            localVideoView = primaryVideoView;

    }


    boolean isCallAccepted = false;

     /**
     * connect to room using access token
     */
    public void connectToRoom() {

        try {
            if(accessToken == null){
                return;
            }

            if (callParams.isIncomingCall() && TextUtils.isEmpty(accessToken)) {
                isCallAccepted = true;
            }

            configureAudio(true);
            ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(accessToken) .roomName(callParams.getRoomName());

            /*
             * Add local audio track to connect options to share with participants.
             */
            if (localAudioTrack != null) {
                connectOptionsBuilder.audioTracks(Collections.singletonList(localAudioTrack));
            }

            /*
             * Add local video track to connect options to share with participants.
             */
            if (localVideoTrack != null ) {
                connectOptionsBuilder.videoTracks(Collections.singletonList(localVideoTrack));
            }

            /*
             * Set the preferred audio and video codec for media.
             */
            connectOptionsBuilder.preferAudioCodecs(Collections.singletonList(audioCodec));
            connectOptionsBuilder.preferVideoCodecs(Collections.singletonList(videoCodec));

            /*
             * Set the sender side encoding parameters.
             */
            connectOptionsBuilder.encodingParameters(encodingParameters);

            room = Video.connect(mContext, connectOptionsBuilder.build(), roomListener());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
     * Called when remote participant joins the room
     */
    private void addRemoteParticipant(RemoteParticipant remoteParticipant) {
        try {

            remoteParticipantIdentity = remoteParticipant.getIdentity();


            /*
             * Add remote participant renderer
             */
            if (remoteParticipant.getRemoteVideoTracks().size() > 0) {
                RemoteVideoTrackPublication remoteVideoTrackPublication = remoteParticipant.getRemoteVideoTracks().get(0);

                /*
                 * Only render video tracks that are subscribed to
                 */
                if (remoteVideoTrackPublication.isTrackSubscribed()) {
                    ParticipantViewState participantView = new ParticipantViewState();
                    participantView.setSid(remoteParticipant.getSid());
                    participantView.setIdentity(remoteParticipant.getIdentity());
                    participantView.setMuted(remoteParticipant.getRemoteAudioTracks().get(0).getAudioTrack().isEnabled());
                    participantView.setVideoTrack(remoteVideoTrackPublication.getVideoTrack());
                    participant.put(remoteParticipant.getSid(),participantView);
                    addRemoteParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
                }
            }

            /*
             * Start listening for participant events
             */
            remoteParticipant.setListener(remoteParticipantListener());

        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    /*
     * Set primary view as renderer for participant video track
     */
    private void addRemoteParticipantVideo(VideoTrack videoTrack) {
        moveLocalVideoToThumbnailView();

        //update ui
        primaryVideoView.setMirror(false);
        videoTrack.addSink(primaryVideoView);
    }

    /**
     * move local video to thumbnail view
     */
    public void moveLocalVideoToThumbnailView() {
        try {
            if(localVideoTrack.isEnabled()){
                if (thumbnailVideoView.getVisibility() == View.GONE) {
                    thumbnailVideoView.setVisibility(View.VISIBLE);
                    if(localVideoTrack !=null) {
                        localVideoTrack.removeSink(primaryVideoView);
                        localVideoTrack.addSink(thumbnailVideoView);
                        localVideoView = thumbnailVideoView;
                        thumbnailVideoView.setMirror(cameraCapturerCompat.getCameraSource() ==  CameraCapturerCompat.Source.FRONT_CAMERA);
                    }

                }
            }
            iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.CONNECTED);
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    /**
     * move local video to primary view
     */
    public void moveLocalVideoToPrimaryView() {
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            thumbnailVideoView.setVisibility(View.GONE);
            if (localVideoTrack != null) {
                localVideoTrack.removeSink(thumbnailVideoView);
                localVideoTrack.addSink(primaryVideoView);
            }
            localVideoView = primaryVideoView;
            primaryVideoView.setMirror(cameraCapturerCompat.getCameraSource() == CameraCapturerCompat.Source.FRONT_CAMERA);
        }
    }

    /*
     * Called when remote participant leaves the room
     */
    private void removeRemoteParticipant(RemoteParticipant remoteParticipant) {
        if (!remoteParticipant.getIdentity().equals(remoteParticipantIdentity)) {
            return;
        }

        /*
         * Remove remote participant renderer
         */
        if (!remoteParticipant.getRemoteVideoTracks().isEmpty()) {
            RemoteVideoTrackPublication remoteVideoTrackPublication =
                    remoteParticipant.getRemoteVideoTracks().get(0);

            /*
             * Remove video only if subscribed to participant track
             */
            if (remoteVideoTrackPublication.isTrackSubscribed()) {
                removeParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
            }
        }
        moveLocalVideoToPrimaryView();
    }

    private void removeParticipantVideo(VideoTrack videoTrack) {
        videoTrack.removeSink(primaryVideoView);
    }

    /*
     * Room events listener
     */
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(@NonNull Room room) {
                localParticipant = room.getLocalParticipant();


                for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
                    addRemoteParticipant(remoteParticipant);
                    break;
                }

                Log.e("PARTICIPANT", "onConnected: " + room.getRemoteParticipants().size() );
                if(callParams.isIncomingCall()){
                    if(room.getRemoteParticipants().size() < 1){
                        iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.HANGING);
                    }
                }

                iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.LOCAL_PARTICIPANT_CONNECTED);

            }

            @Override
            public void onConnectFailure(@NonNull Room room, @NonNull TwilioException e) {
                configureAudio(false);
                iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.HANGING);
                Log.e(TAG, "onConnectFailure: " + e.getExplanation() );
            }

            @Override
            public void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException) {

            }

            @Override
            public void onReconnected(@NonNull Room room) {

            }

            @Override
            public void onDisconnected(@NonNull Room room, TwilioException e) {
                localParticipant = null;
                VideoSessionManager.this.room = null;
                iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.HANGING);
            }

            @Override
            public void onParticipantConnected(@NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
                addRemoteParticipant(remoteParticipant);
                iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.CONNECTED);

            }

            @Override
            public void onParticipantDisconnected(@NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
                removeRemoteParticipant(remoteParticipant);
                participant.remove(remoteParticipant.getSid());
                iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.DISCONNECTED);
            }

            @Override
            public void onRecordingStarted(@NonNull Room room) {
                /*
                 * Indicates when media shared to a Room is being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStarted");
            }

            @Override
            public void onRecordingStopped(@NonNull Room room) {
                /*
                 * Indicates when media shared to a Room is no longer being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStopped");
            }
        };
    }

    private RemoteParticipant.Listener remoteParticipantListener() {
        return new RemoteParticipant.Listener() {
            @Override
            public void onAudioTrackPublished(@NonNull RemoteParticipant remoteParticipant,
                                              @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Log.i(TAG, String.format("onAudioTrackPublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(),
                        remoteAudioTrackPublication.isTrackEnabled(),
                        remoteAudioTrackPublication.isTrackSubscribed(),
                        remoteAudioTrackPublication.getTrackName()));
            }

            @Override
            public void onAudioTrackUnpublished(@NonNull RemoteParticipant remoteParticipant,
                                                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Log.i(TAG, String.format("onAudioTrackUnpublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(),
                        remoteAudioTrackPublication.isTrackEnabled(),
                        remoteAudioTrackPublication.isTrackSubscribed(),
                        remoteAudioTrackPublication.getTrackName()));
            }

            @Override
            public void onDataTrackPublished(@NonNull RemoteParticipant remoteParticipant,
                                             @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {
                Log.i(TAG, String.format("onDataTrackPublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(),
                        remoteDataTrackPublication.isTrackEnabled(),
                        remoteDataTrackPublication.isTrackSubscribed(),
                        remoteDataTrackPublication.getTrackName()));
            }

            @Override
            public void onDataTrackUnpublished(@NonNull RemoteParticipant remoteParticipant,
                                               @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {
                Log.i(TAG, String.format("onDataTrackUnpublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(),
                        remoteDataTrackPublication.isTrackEnabled(),
                        remoteDataTrackPublication.isTrackSubscribed(),
                        remoteDataTrackPublication.getTrackName()));
            }

            @Override
            public void onVideoTrackPublished(@NonNull RemoteParticipant remoteParticipant,
                                              @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Log.i(TAG, String.format("onVideoTrackPublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(),
                        remoteVideoTrackPublication.isTrackEnabled(),
                        remoteVideoTrackPublication.isTrackSubscribed(),
                        remoteVideoTrackPublication.getTrackName()));
            }

            @Override
            public void onVideoTrackUnpublished(@NonNull RemoteParticipant remoteParticipant,
                                                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Log.i(TAG, String.format("onVideoTrackUnpublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(),
                        remoteVideoTrackPublication.isTrackEnabled(),
                        remoteVideoTrackPublication.isTrackSubscribed(),
                        remoteVideoTrackPublication.getTrackName()));
            }

            @Override
            public void onAudioTrackSubscribed(@NonNull RemoteParticipant remoteParticipant,
                                               @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                                               @NonNull RemoteAudioTrack remoteAudioTrack) {
                Log.i(TAG, String.format("onAudioTrackSubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrack.isEnabled(),
                        remoteAudioTrack.isPlaybackEnabled(),
                        remoteAudioTrack.getName()));
            }

            @Override
            public void onAudioTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant,
                                                 @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                                                 @NonNull RemoteAudioTrack remoteAudioTrack) {
                Log.i(TAG, String.format("onAudioTrackUnsubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrack.isEnabled(),
                        remoteAudioTrack.isPlaybackEnabled(),
                        remoteAudioTrack.getName()));
            }

            @Override
            public void onAudioTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant,
                                                       @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                                                       @NonNull TwilioException twilioException) {
                Log.i(TAG, String.format("onAudioTrackSubscriptionFailed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrackPublication: sid=%b, name=%s]" +
                                "[TwilioException: code=%d, message=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(),
                        remoteAudioTrackPublication.getTrackName(),
                        twilioException.getCode(),
                        twilioException.getMessage()));
            }

            @Override
            public void onDataTrackSubscribed(@NonNull RemoteParticipant remoteParticipant,
                                              @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                              @NonNull RemoteDataTrack remoteDataTrack) {
                Log.i(TAG, String.format("onDataTrackSubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrack.isEnabled(),
                        remoteDataTrack.getName()));
            }

            @Override
            public void onDataTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant,
                                                @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                                @NonNull RemoteDataTrack remoteDataTrack) {
                Log.i(TAG, String.format("onDataTrackUnsubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrack.isEnabled(),
                        remoteDataTrack.getName()));
            }

            @Override
            public void onDataTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant,
                                                      @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                                                      @NonNull TwilioException twilioException) {
                Log.i(TAG, String.format("onDataTrackSubscriptionFailed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrackPublication: sid=%b, name=%s]" +
                                "[TwilioException: code=%d, message=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(),
                        remoteDataTrackPublication.getTrackName(),
                        twilioException.getCode(),
                        twilioException.getMessage()));
            }

            @Override
            public void onVideoTrackSubscribed(@NonNull RemoteParticipant remoteParticipant,
                                               @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                                               @NonNull RemoteVideoTrack remoteVideoTrack) {
                Log.i(TAG, String.format("onVideoTrackSubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrack.isEnabled(),
                        remoteVideoTrack.getName()));
                addRemoteParticipantVideo(remoteVideoTrack);
            }

            @Override
            public void onVideoTrackUnsubscribed(@NonNull RemoteParticipant remoteParticipant,
                                                 @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                                                 @NonNull RemoteVideoTrack remoteVideoTrack) {
                Log.i(TAG, String.format("onVideoTrackUnsubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrack.isEnabled(),
                        remoteVideoTrack.getName()));
                removeParticipantVideo(remoteVideoTrack);
            }

            @Override
            public void onVideoTrackSubscriptionFailed(@NonNull RemoteParticipant remoteParticipant,
                                                       @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                                                       @NonNull TwilioException twilioException) {
                Log.i(TAG, String.format("onVideoTrackSubscriptionFailed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrackPublication: sid=%b, name=%s]" +
                                "[TwilioException: code=%d, message=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(),
                        remoteVideoTrackPublication.getTrackName(),
                        twilioException.getCode(),
                        twilioException.getMessage()));

            }

            @Override
            public void onAudioTrackEnabled(@NonNull RemoteParticipant remoteParticipant,
                                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Log.i(TAG, String.format("onAudioTrackUnsubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getRemoteAudioTrack().isEnabled(),
                        remoteAudioTrackPublication.getRemoteAudioTrack().isPlaybackEnabled(),
                        remoteAudioTrackPublication.getRemoteAudioTrack().getName()));
            }

            @Override
            public void onAudioTrackDisabled(@NonNull RemoteParticipant remoteParticipant,
                                             @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Log.i(TAG, String.format("onAudioTrackDisabled: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getRemoteAudioTrack().isEnabled(),
                        remoteAudioTrackPublication.getRemoteAudioTrack().isPlaybackEnabled(),
                        remoteAudioTrackPublication.getRemoteAudioTrack().getName()));

            }

            @Override
            public void onVideoTrackEnabled(@NonNull RemoteParticipant remoteParticipant,
                                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Log.i(TAG, String.format("onVideoTrackEnabled: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getRemoteVideoTrack().isEnabled(),
                        remoteVideoTrackPublication.getRemoteVideoTrack().getName()));
                iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.PARTICIPANT_VIDEO_ON);
            }

            @Override
            public void onVideoTrackDisabled(@NonNull RemoteParticipant remoteParticipant,
                                             @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Log.i(TAG, String.format("onVideoTrackDisabled: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getRemoteVideoTrack().isEnabled(),
                        remoteVideoTrackPublication.getRemoteVideoTrack().getName()));
                iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.PARTICIPANT_VIDEO_OFF);
            }
        };
    }

    /*
     * Disconnect from connection from room
     */
    private void disconnectConnection() {

        if (room != null) {
            room.disconnect();
        }
    }

    /**
     * This method switch camera
     */
    public boolean isBackCamera() {
        if (cameraCapturerCompat != null) {
            CameraCapturerCompat.Source cameraSource = cameraCapturerCompat.getCameraSource();
            cameraCapturerCompat.switchCamera();
            if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
                thumbnailVideoView.setMirror(cameraSource == CameraCapturerCompat.Source.BACK_CAMERA);
            } else {
                primaryVideoView.setMirror(cameraSource == CameraCapturerCompat.Source.BACK_CAMERA);
            }

            return cameraSource == CameraCapturerCompat.Source.BACK_CAMERA;
        }
        return false;
    }


    /*
     * Enable/disable the local video track
     */
    public void setAudioVoiceCallOnly(boolean isEnable) {
        if (localVideoTrack != null) {
            localVideoTrack.enable(isEnable);
        }
    }



    /*
     * Enable/disable the local audio track. The results of this operation are
     * signaled to other Participants in the same Room. When an audio track is
     * disabled, the audio is muted.
     */
    public void setMuteUnMute() {
        if (localAudioTrack != null) {
            boolean enable = !localAudioTrack.isEnabled();
            localAudioTrack.enable(enable);
            iVideoSessionCallback.onVideoCallStatus(enable ? VideoCallStatus.MUTE : VideoCallStatus.UN_MUTE);
        }
    }

    /*
     * Enable/disable the local audio track. The results of this operation are
     * signaled to other Participants in the same Room. When an audio track is
     * disabled, the audio is muted.
     */
    public void onEndCall() {
        disconnectConnection();
        iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.HANGING);
    }


    /**
     * This method call api to fetch accessToken, require to connect twilio room
     * @param url access token url
     */
    public void fetchToken(String url){
        try {
            accessToken = "";
            JSONObject data = new JSONObject();

            data.put(AppConstant.JSONFiled.SECRET_KEY, AppConstant.CallData.AUTH_SECRET_KEY);
            data.put(AppConstant.JSONFiled.ROOM_NAME, callParams.getRoomName());
            data.put(AppConstant.JSONFiled.TOKEN_KEY,  AppConstant.CallData.AUTH_TOKEN);
            data.put(AppConstant.JSONFiled.IS_JOINING,  callParams.isIncomingCall());
            data.put(AppConstant.JSONFiled.CALL_UNIQUE_ID,  callParams.getCallUniqueId());

            RestApiClientEvent apiClientEvent = new RestApiClientEvent(mContext, url);
            apiClientEvent.setRequestJson(data);
            apiClientEvent.setListener(new IWebSocketClientEvent() {
                @Override
                public void onSuccess(JSONObject object) {
                    Log.e(TAG, "onSuccess: " + object );
                    try {

                        if(object == null) return;

                        if(object.has(AppConstant.JSONFiled.ACCESS_TOKEN) && object.getString(AppConstant.JSONFiled.ACCESS_TOKEN).length() > 0){
                            accessToken = object.getString(AppConstant.JSONFiled.ACCESS_TOKEN);
                        }

                        if(object.has(AppConstant.JSONFiled.STATUS) && object.getString(AppConstant.JSONFiled.STATUS).equals("0")){
                            if (callParams.isIncomingCall()) {
                                if (isCallAccepted) {
                                    connectToRoom();
                                    isCallAccepted = false;
                                } else {
                                    iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.INCOMING_RINGING);
                                }
                            }
                            else {
                                connectToRoom();
                                iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.OUTGOING_RINGING);
                            }
                        }

                        if(object.has(AppConstant.JSONFiled.ERROR) && object.getString(AppConstant.JSONFiled.ERROR).length() > 0){
                            iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.HANGING);
                        }
                    }catch (JSONException jsonException){
                        jsonException.printStackTrace();
                        iVideoSessionCallback.onVideoCallStatus(VideoCallStatus.HANGING);
                    }
                }

                @Override
                public void onFinish(Error error) {
                    Log.e(TAG, "onFinish: " + error);
                }
            });

            apiClientEvent.fire();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Configure audio
     * @param enable - true mode in communication, false otherwise (set previously set audio)
     */
    private void configureAudio(boolean enable) {
        if(audioManager != null){
            if (enable) {
                previousAudioMode = audioManager.getMode();
                requestAudioFocus();
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                previousMicrophoneMute = audioManager.isMicrophoneMute();
                audioManager.setMicrophoneMute(false);

            } else {
                audioManager.setMode(previousAudioMode);
                audioManager.abandonAudioFocus(null);
                audioManager.setMicrophoneMute(previousMicrophoneMute);
            }
        }

    }

    /**
     * This method enable / disable video input when video call
     */
    public void setLocalVideoTrack(){
        if(localVideoTrack != null) {
            boolean enable = !localVideoTrack.isEnabled();
            localVideoTrack.enable(enable);
            if (callParams.isVideo()) {
                iVideoSessionCallback.onVideoCallStatus(enable ? VideoCallStatus.VIDEO_OF : VideoCallStatus.VIDEO_ON);
            }
            if(!callParams.isVideo()){
                iVideoSessionCallback.onVideoCallStatus(enable ? VideoCallStatus.VIDEO_ON : VideoCallStatus.VIDEO_OF);
            }
        }
    }

    /**
     * This method check if speaker is on then move it earpiece or if speaker is off then turn onn
     * @return return boolean true if speaker is on otherwise false
     */
    public boolean isSpeakerOn(){
        if(audioManager != null){

            if(audioManager.isSpeakerphoneOn()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Utility.setCommunicationDevice(audioManager, AudioDeviceInfo.TYPE_BUILTIN_EARPIECE);
                } else {
                    audioManager.setSpeakerphoneOn(false);
                }

                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Utility.setCommunicationDevice(audioManager,  AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
            } else {
                audioManager.setSpeakerphoneOn(true);
            }
            return true;
        }
        return false;
    }


    /**
     *  This method set speaker Off for audio call when connected
     */
    public void setSpeakerOff(){
        if(audioManager != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Utility.setCommunicationDevice(audioManager, AudioDeviceInfo.TYPE_BUILTIN_EARPIECE);
            } else {
                audioManager.setSpeakerphoneOn(false);
            }
        }
    }


    /**
     * This method request audio focus
     */
    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();

            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener(
                                    new AudioManager.OnAudioFocusChangeListener() {
                                        @Override
                                        public void onAudioFocusChange(int i) {
                                        }
                                    })
                            .build();
            audioManager.requestAudioFocus(focusRequest);
        } else {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,  AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
    }

    
    public void setiVideoSessionCallback(IVideoSessionCallback iVideoSessionCallback) {
        this.iVideoSessionCallback = iVideoSessionCallback;
    }




    public void setPrimaryVideoView(VideoView primaryVideoView) {
        this.primaryVideoView = primaryVideoView;
    }

    public void setThumbnailVideoView(VideoView thumbnailVideoView) {
        this.thumbnailVideoView = thumbnailVideoView;
    }


    public enum VideoCallStatus {
        IDLE,
        LOCAL_PARTICIPANT_CONNECTED,
        INCOMING_RINGING,
        OUTGOING_RINGING,
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        HANGING,
        MUTE,
        UN_MUTE,
        VIDEO_ON,
        PARTICIPANT_VIDEO_OFF,
        PARTICIPANT_VIDEO_ON,
        VIDEO_OF,
    };

    public enum CallType {
        INCOMING,
        OUTGOING
    };

    public interface IVideoSessionCallback {
            void onVideoCallStatus(VideoCallStatus videoCallStatus);
    }

}
