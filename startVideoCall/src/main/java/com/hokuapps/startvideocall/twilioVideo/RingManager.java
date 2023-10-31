package com.hokuapps.startvideocall.twilioVideo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;




public class RingManager {
    @SuppressLint("StaticFieldLeak")
    private static RingManager ringManager;
    private final AudioManager audioManager;
    private MediaPlayer phoneRingPlayer;
    private final Context context;
    private final Vibrator v;

    public static RingManager getInstance(Context context) {
        if (ringManager == null) {
            ringManager = new RingManager(context);
        }

        return ringManager;
    }

    private RingManager(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * This method play outgoing ringtone
     * @param ringId sound uri
     */
    public synchronized void playOutgoingRing(Uri ringId) {
        stop();
        phoneRingPlayer = MediaPlayer.create(context, ringId );

        phoneRingPlayer.setLooping(true);

        if (phoneRingPlayer != null && !phoneRingPlayer.isPlaying()) {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
            switch (audioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_NORMAL:
                    try {
                        phoneRingPlayer.start();
                    } catch (Throwable t) {
                        Log.e("RingtoneManager", "Failed to start playing ring tone");
                    }

                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    vibrate();
            }
        }
    }

    public void setSpeakerPhoneOn(boolean speakerOn) {
        audioManager.setSpeakerphoneOn(speakerOn);
    }

    public synchronized void stop() {
        if (phoneRingPlayer != null) {
            if (phoneRingPlayer.isPlaying()) {
                phoneRingPlayer.stop();
            }
            phoneRingPlayer.release();
            phoneRingPlayer = null;
        }
        stopVibrate();
    }

    private void vibrate() {
        long[] pattern = {500, 300, 500};
        if (v != null) {
            v.vibrate(pattern, 0);
        }
    }

    private void stopVibrate() {
        if (v != null) {
            v.cancel();
        }
    }

    private void calculateVolume() {
        int ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        int maxRingVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int calculatedVolume = ringVolume * maxMusicVolume / maxRingVolume;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, calculatedVolume, AudioManager.FLAG_PLAY_SOUND);
    }

    public void registerAudioInOutReceiver() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(audioInOutReceiver, intentFilter);
    }

    public void unregisterAudioInOutReceiver() {
       context.unregisterReceiver(audioInOutReceiver);
    }

    public BroadcastReceiver audioInOutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if (audioManager != null) {
                            Log.d("Headset", "Headset is unplugged");
                            audioManager.setSpeakerphoneOn(true);
                        }
                        break;
                    case 1:

                        if (audioManager != null) {
                            Log.d("Headset", "Headset is plugged");
                            audioManager.setSpeakerphoneOn(false);
                        }
                        break;
                    default:
                        Log.d("Headset", "I have no idea what the headset state is");
                }
            }

        }
    };
}

