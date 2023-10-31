package com.hokuapps.startvideocall.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.hokuapps.startvideocall.R;
import com.hokuapps.startvideocall.broadcastReceiver.CallNotificationActionReceiver;
import com.hokuapps.startvideocall.network.RestApiClientEvent;
import com.hokuapps.startvideocall.twilioVideo.RingManager;
import com.hokuapps.startvideocall.twilioVideo.VideoSessionActivity;
import com.hokuapps.startvideocall.utils.AppConstant;
import com.hokuapps.startvideocall.utils.Utility;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class CallManager extends Worker {

    public static final int NOTIFICATION_ID = 10;
    public static String CHANNEL_ID = "";
    public static String CHANNEL_NAME = "";
    private Context mContext;
    private Handler handler;
    private Runnable delayRunnable = null;

    public CallManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            CHANNEL_ID = getInputData().getString("app_name") + "CallChannel";
            CHANNEL_NAME = getInputData().getString("app_name") + "Call Channel";
            String callerName = getInputData().getString(AppConstant.ManagerData.CALLER_NAME);
            String profileImage = getInputData().getString(AppConstant.ManagerData.PROFILE_IMAGE);
            String notificationData = getInputData().getString(AppConstant.ManagerData.NOTIFICATION_DATA);

            String callTypeString = AppConstant.CallType.VIDEO;
            boolean callType = getInputData().getBoolean(AppConstant.ManagerData.CALL_TYPE, false);
            if (callType) {
                callTypeString = AppConstant.CallType.AUDIO;
            }

            if (notificationData == null) {
                return Result.failure();
            }

            JSONObject jsonObject = new JSONObject(notificationData);
            String roomName = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.ROOM_NAME);
            String mUserId = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.USER_ID);
            JSONObject senderObject = new JSONObject();
            senderObject.put(AppConstant.JSONFiled.ROOM_NAME, roomName);
            senderObject.put(AppConstant.JSONFiled.ACTION_STATUS, AppConstant.RINGING);
            senderObject.put(AppConstant.JSONFiled.USER_ID, mUserId);
            senderObject.put(AppConstant.JSONFiled.CALL_UNIQUE_ID, Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALL_UNIQUE_ID));
            //Utility.callReject(AppConstant.CALL_REJECT_URL, senderObject);

            RestApiClientEvent apiClientEvent = new RestApiClientEvent(mContext,AppConstant.CALL_STATUS_URL);
            apiClientEvent.setRequestJson(senderObject);
            apiClientEvent.fire();

            RingManager.getInstance(mContext).playOutgoingRing(Settings.System.DEFAULT_RINGTONE_URI);

            setForegroundAsync(createForeGroundInfo(callerName, callTypeString, profileImage, notificationData));
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
        RingManager.getInstance(mContext).stop();
        if (handler != null && delayRunnable != null) {
            handler.removeCallbacks(delayRunnable);
        }
    }



    private ForegroundInfo createForeGroundInfo(String callerName, String callTypeString, String profileImage, String notificationData) throws ExecutionException, InterruptedException {

        handler = new Handler(Looper.getMainLooper());

        Intent cancelCallAction = new Intent(mContext, CallNotificationActionReceiver.class);
        cancelCallAction.putExtra("ConstantApp.CALL_RESPONSE_ACTION_KEY", "ConstantApp.CALL_CANCEL_ACTION");
        cancelCallAction.putExtra(AppConstant.BroadCastCallData.ACTION_TYPE, AppConstant.BroadCastCallData.CANCEL_CALL);
        cancelCallAction.putExtra(AppConstant.BroadCastCallData.NOTIFICATION_ID, NOTIFICATION_ID);
        cancelCallAction.putExtra(AppConstant.BroadCastCallData.NOTIFICATION_DATA, notificationData);
        cancelCallAction.setAction(AppConstant.BroadCastCallData.CANCEL_CALL);


        Intent callDialogAction = new Intent(mContext, CallNotificationActionReceiver.class);
        //cancelCallAction.putExtra("ConstantApp.CALL_RESPONSE_ACTION_KEY", "ConstantApp.DIALOG_CALL");
        callDialogAction.putExtra(AppConstant.BroadCastCallData.ACTION_TYPE, AppConstant.BroadCastCallData.DIALOG_CALL);
        callDialogAction.putExtra(AppConstant.BroadCastCallData.NOTIFICATION_ID, NOTIFICATION_ID);
        callDialogAction.putExtra(AppConstant.BroadCastCallData.NOTIFICATION_DATA, notificationData);
        callDialogAction.setAction(AppConstant.BroadCastCallData.DIALOG_CALL);

        PendingIntent receiveCallPendingIntent = null;
        PendingIntent cancelCallPendingIntent = null;
        PendingIntent callDialogPendingIntent = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            receiveCallPendingIntent = PendingIntent.getActivity( mContext, 1200, createVideoSessionActivity(notificationData), PendingIntent.FLAG_IMMUTABLE);
            cancelCallPendingIntent = PendingIntent.getBroadcast(mContext, 1201, cancelCallAction, PendingIntent.FLAG_IMMUTABLE);
            callDialogPendingIntent = PendingIntent.getBroadcast(mContext, 1202, createVideoSessionActivity(notificationData), PendingIntent.FLAG_IMMUTABLE);

        } else {
            receiveCallPendingIntent = PendingIntent.getActivity(mContext, 1200, createVideoSessionActivity(notificationData), PendingIntent.FLAG_UPDATE_CURRENT);
            cancelCallPendingIntent = PendingIntent.getBroadcast(mContext, 1201, cancelCallAction, PendingIntent.FLAG_UPDATE_CURRENT);
            callDialogPendingIntent = PendingIntent.getBroadcast(mContext, 1202, createVideoSessionActivity(notificationData), PendingIntent.FLAG_UPDATE_CURRENT);
        }

        registerNotificationChannelChannel(CHANNEL_ID, CHANNEL_NAME, mContext.getString(R.string.call_channel_name));

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.custom_notification);

        remoteViews.setTextViewText(R.id.callerName, callerName);
        remoteViews.setTextViewText(R.id.callType, mContext.getString(R.string.Incoming_call_notification,callTypeString));

        Bitmap profileImageBitmap = null;
        if(profileImage != null && profileImage.length() > 0){
            profileImageBitmap = Ion.with(mContext)
                    .load(profileImage).asBitmap().get();
            Bitmap resized = Bitmap.createScaledBitmap(profileImageBitmap, 80, 80, true);
            remoteViews.setImageViewBitmap(R.id.photo, resized);
        }else {
            remoteViews.setImageViewResource(R.id.photo, R.drawable.ic_launcher);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID);

            builder.setContentTitle(callerName)
                    .setContentText(mContext.getString(R.string.Incoming_call_notification,callTypeString))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setAutoCancel(false)
                    .setSound(null)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(remoteViews)
                    .setCustomBigContentView(remoteViews)
                    .setFullScreenIntent(callDialogPendingIntent, true);

        remoteViews.setOnClickPendingIntent(R.id.btnAnswer, receiveCallPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.btnDecline, cancelCallPendingIntent);

        delayRunnable = new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(notificationData);
                    String roomName = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.ROOM_NAME);
                    String mUserId = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.USER_ID);
                    JSONObject senderObject = new JSONObject();
                    senderObject.put(AppConstant.JSONFiled.ROOM_NAME, roomName);
                    senderObject.put(AppConstant.JSONFiled.ACTION_STATUS, AppConstant.CALL_REJECTED);
                    senderObject.put(AppConstant.JSONFiled.USER_ID, mUserId);
                    senderObject.put(AppConstant.JSONFiled.CALL_UNIQUE_ID, Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALL_UNIQUE_ID));
                    //Utility.callReject(AppConstant.CALL_REJECT_URL, senderObject);
                    RestApiClientEvent apiClientEvent = new RestApiClientEvent(mContext,AppConstant.CALL_STATUS_URL);
                    apiClientEvent.setRequestJson(senderObject);
                    apiClientEvent.fire();

                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
                WorkManager.getInstance(mContext).cancelAllWork();

            }
        };
        handler.postDelayed(delayRunnable, AppConstant.CALL_END_DELAY);
        return new ForegroundInfo(NOTIFICATION_ID, builder.build());
    }

    private Intent createVideoSessionActivity(  String notificationData){
        Intent receiveCallAction = new Intent(mContext, VideoSessionActivity.class);
        try {
            Bundle bundleData = new Bundle();
            receiveCallAction.putExtras(bundleData);
            receiveCallAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return receiveCallAction;
    }

    public void registerNotificationChannelChannel(String channelId, String channelName, String channelDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(channelDescription);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
