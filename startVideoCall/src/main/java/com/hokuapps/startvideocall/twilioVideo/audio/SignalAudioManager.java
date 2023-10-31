package com.hokuapps.startvideocall.twilioVideo.audio;


import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import androidx.annotation.NonNull;

import com.hokuapps.startvideocall.R;
import com.hokuapps.startvideocall.utils.Utility;


public class SignalAudioManager {

    private static final String TAG = SignalAudioManager.class.getSimpleName();

    private final Context context;
    private final IncomingRinger incomingRinger;
    private final OutgoingRinger outgoingRinger;

    private final SoundPool soundPool;
    private final int connectedSoundId;
    private final int disconnectedSoundId;

    public SignalAudioManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.incomingRinger = new IncomingRinger(context);
        this.outgoingRinger = new OutgoingRinger(context);
        this.soundPool = new SoundPool(1, AudioManager.STREAM_VOICE_CALL, 0);

        this.connectedSoundId = this.soundPool.load(context, R.raw.webrtc_completed, 1);
        this.disconnectedSoundId = this.soundPool.load(context, R.raw.webrtc_disconnected, 1);
    }

    public void initializeAudioManager() {
        AudioManager audioManager = ServiceUtil.getAudioManager(context);

        audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
    }

    /**
     *  This method start incoming call ring
     */
    public void startIncomingRinger() {
        AudioManager audioManager = ServiceUtil.getAudioManager(context);
        boolean speaker = !audioManager.isWiredHeadsetOn() && !audioManager.isBluetoothScoOn();

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        audioManager.setMicrophoneMute(false);
        audioManager.setSpeakerphoneOn(speaker);

        incomingRinger.start(speaker);
    }

    /**
     *  This method start outgoing call ring
     */
    public void startOutgoingRinger(OutgoingRinger.Type type) {
        AudioManager audioManager = ServiceUtil.getAudioManager(context);
        audioManager.setMicrophoneMute(false);

        if (type == OutgoingRinger.Type.SONAR) {
            audioManager.setSpeakerphoneOn(false);
        } else {
            audioManager.setSpeakerphoneOn(true);
        }

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, 0);

        outgoingRinger.start(type);
    }

    public void silenceIncomingRinger() {
        incomingRinger.stop();
    }

    /**
     * This method set speaker on or off
     * @param playDisconnected true if play sound otherwise false
     * @param isSpeakerPhoneOn flag true for speaker otherwise false
     */
    public void startCommunication(boolean playDisconnected, boolean isSpeakerPhoneOn) {
        AudioManager audioManager = ServiceUtil.getAudioManager(context);

        incomingRinger.stop();
        outgoingRinger.stop();

        if (isSpeakerPhoneOn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Utility.setCommunicationDevice(audioManager,  AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
            } else {
                audioManager.setSpeakerphoneOn(isSpeakerPhoneOn);
            }
        }

        audioManager.setMicrophoneMute(false);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        if (playDisconnected) {
            soundPool.play(connectedSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    /**
     * This method stop ringtone
     * @param playDisconnected true if ringtone keep play otherwise false
     */
    public void stop(boolean playDisconnected) {
        AudioManager audioManager = ServiceUtil.getAudioManager(context);

        incomingRinger.stop();
        outgoingRinger.stop();

        if (playDisconnected) {
            soundPool.play(disconnectedSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
        }

        if (audioManager.isBluetoothScoOn()) {
            audioManager.setBluetoothScoOn(false);
            audioManager.stopBluetoothSco();
        }

        audioManager.setSpeakerphoneOn(false);

        audioManager.setMicrophoneMute(false);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.abandonAudioFocus(null);
    }
}
