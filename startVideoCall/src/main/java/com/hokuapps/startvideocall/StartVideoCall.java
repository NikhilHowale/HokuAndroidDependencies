package com.hokuapps.startvideocall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.startvideocall.delegate.IWebSocketClientEvent;
import com.hokuapps.startvideocall.model.Error;
import com.hokuapps.startvideocall.network.RestApiClientEvent;
import com.hokuapps.startvideocall.pref.CallPreference;
import com.hokuapps.startvideocall.service.CallManager;
import com.hokuapps.startvideocall.twilioVideo.VideoSessionActivity;
import com.hokuapps.startvideocall.twilioVideo.model.CallParams;
import com.hokuapps.startvideocall.twilioVideo.model.UserInfo;
import com.hokuapps.startvideocall.utils.AppConstant;
import com.hokuapps.startvideocall.utils.Utility;

import org.json.JSONObject;

import java.util.List;

public class StartVideoCall {

    private static final String TAG = "StartVideoCall";

    private Activity mActivity;
    @SuppressLint("StaticFieldLeak")
    private static StartVideoCall mInstance = null;

    private static WorkManager mWorkManager;
    private static String callUniqueId = "";

    public static StartVideoCall getInstance() {
        if(mInstance == null){
            mInstance = new StartVideoCall();
        }
        return mInstance;
    }


    /**
     * This function use for extract required data from responseData for video call
     *
     * @param activity     context for launch activity
     * @param responseData contain caller and receiver information which going to join room
     *                     isAudioOnly - provide is it video call or audio call
     *                     token url - it contain token url for fetch authToken to authentication
     */
    public void start(Activity activity, String responseData, String appSecretKey, String appAuthToken){
        try {
            mActivity = activity;
            JSONObject jsonObject = new JSONObject(responseData);
            CallParams callParams = new CallParams();

            callParams.setVideo(true);
            if(jsonObject.has(AppConstant.JSONFiled.IS_AUDIO_ONLY) && jsonObject.getBoolean(AppConstant.JSONFiled.IS_AUDIO_ONLY)){
                callParams.setVideo(false);
            }

            callParams.setRoomName(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.ROOM_NAME));
            callParams.setCallerName(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALLER_NAME));
            callParams.setCallerProfileImage(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALLER_IMAGE));
            callParams.setTokenUrl(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.TWILIO_TOKEN_URL));
            callParams.setCallUniqueId(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.CALL_UNIQUE_ID));
            callParams.setCallRejectUrl(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.CALL_DENIED_URL));
            callParams.setIncomingCall(false);
            callParams.setCallFromNotification(false);


            UserInfo userInfo = new UserInfo();
            userInfo.setName(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.USER_NAME));
            userInfo.setUserId(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.USER_ID));
            userInfo.setSecretKey(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.SECRET_KEY));
            userInfo.setTokenKey(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.TOKEN_KEY));

            AppConstant.CallData.AUTH_SECRET_KEY = appSecretKey;
            AppConstant.CallData.AUTH_TOKEN = appAuthToken;

            Bundle bundleData = new Bundle();
            bundleData.putParcelable(VideoSessionActivity.EXTRA_CALL_PARAMS, callParams);
            bundleData.putParcelable(AppConstant.EXTRA_USER_DETAILS, userInfo);

            startVideoCalling(bundleData);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method launch Activity with required permission
     * @param bundleData contain caller and receiver information which going to join room
     */
    private void startVideoCalling(Bundle bundleData){

        try {

            PermissionListener permissionListener =new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    VideoSessionActivity.startActivity(mActivity, bundleData);
                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {

                }
            };

            TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setPermissions( Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO)
                    .check();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This function call when receiver get notification for call
     * @param context context for create notification
     * @param jsonObject JSON data contain information of caller and tokenUrl
     *                   ACTION_STATUS - provide call status  ACCEPT, REJECT , RINGING
     *                   IS_VIDEO_CALL - Check for is it calling notification
     */
    public boolean handleCallNotification(Context context, JSONObject jsonObject, String secretKey, String token){
        try {
            String messageText = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.MESSAGE_TEXT);
            String callerName = Utility.getStringObjectValue(jsonObject,  AppConstant.JSONFiled.CALLER_NAME);
            AppConstant.CallData.AUTH_SECRET_KEY = secretKey;
            AppConstant.CallData.AUTH_TOKEN = token;

            if(jsonObject.has(AppConstant.JSONFiled.ACTION_STATUS)){

                int mAction = Utility.getJsonObjectIntValue(jsonObject, AppConstant.JSONFiled.ACTION_STATUS);

                if(mAction == AppConstant.RINGING){
                    Intent ringing = new Intent(AppConstant.TWILIO_CALL_ACTION);
                    ringing.putExtra(AppConstant.CALL_ACTION, mAction);
                    context.sendBroadcast(ringing);
                }else if(mAction == AppConstant.CALL_REJECTED){


                    String serverUniqueId = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALL_UNIQUE_ID);

                    if(callUniqueId.length() > 0 && callUniqueId.equals(serverUniqueId)){
                        callUniqueId = "";
                        return true;
                    }

                    callUniqueId = serverUniqueId;

                    //CallPreference callPreference = new CallPreference(context);
                    String CallingData = AppConstant.CallData.NOTIFICATION_DATA;

                    Intent hangUp = new Intent(AppConstant.TWILIO_CALL_ACTION);
                    hangUp.putExtra(AppConstant.CALL_ACTION, mAction);

                    if(CallingData.length() == 0){
                        context.sendBroadcast(hangUp);
                        return true;
                    }

                    context.sendBroadcast(hangUp);

                    if(mWorkManager != null) {
                        mWorkManager.cancelAllWork();
                    }

                    showMissCallNotification(context);
                }

                return true;
            }


            if(jsonObject.has(AppConstant.JSONFiled.IS_VIDEO_CALL)){


                AppConstant.CallData.CALL_UNIQUE_ID = "";
                if(jsonObject.has(AppConstant.JSONFiled.CALL_UNIQUE_ID)) {
                    AppConstant.CallData.CALL_UNIQUE_ID = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALL_UNIQUE_ID);
                }

                AppConstant.CallData.NOTIFICATION_DATA = jsonObject.toString();
                String profileImage = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALLER_IMAGE);
                String callUniqueId = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALL_UNIQUE_ID);

                boolean isAudio = false;

                if(jsonObject.has(AppConstant.JSONFiled.IS_AUDIO_ONLY) && jsonObject.getBoolean(AppConstant.JSONFiled.IS_AUDIO_ONLY)) {
                    isAudio = Utility.getJsonObjectBooleanValue(jsonObject, AppConstant.JSONFiled.IS_AUDIO_ONLY);
                }

                if(jsonObject.has(AppConstant.JSONFiled.CALL_DENIED_URL) && jsonObject.getString(AppConstant.JSONFiled.CALL_DENIED_URL).length() > 0){
                    AppConstant.CALL_STATUS_URL = jsonObject.getString(AppConstant.JSONFiled.CALL_DENIED_URL);
                }

                Data.Builder builder = new Data.Builder();
                builder.putString(AppConstant.ManagerData.MESSAGE_TEXT, messageText);
                builder.putString(AppConstant.ManagerData.UNIQUE_ID, callUniqueId);
                builder.putString(AppConstant.ManagerData.CALLER_NAME, callerName);
                builder.putString(AppConstant.ManagerData.PROFILE_IMAGE, profileImage);
                builder.putBoolean(AppConstant.ManagerData.CALL_TYPE, isAudio);
                builder.putString(AppConstant.ManagerData.NOTIFICATION_DATA, jsonObject.toString());

                createCallWorker(context,builder.build());

                return true;
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Create notification with accept and reject option and play ring for user notification
     * @param context create work manager with context
     * @param callData JSON data contain information of caller and tokenUrl
     */
    static void createCallWorker(Context context, Data callData) {

        mWorkManager = WorkManager.getInstance(context);
        OneTimeWorkRequest workRequest =  new OneTimeWorkRequest.Builder(CallManager.class)
                .setInputData(callData)
                .build();

        mWorkManager.enqueue(workRequest);

    }

    /**
     * This method when user did not accept call or reject incoming call
     * @param context context for showing miss call notification
     */
    private static void showMissCallNotification(Context context) {
        try {

            CallPreference callPreference = new CallPreference(context);
            String CallingData = AppConstant.CallData.NOTIFICATION_DATA;
                    //callPreference.getUserCallParams();
            if(CallingData.length() == 0){
                return;
            }
            JSONObject jsonObject = new JSONObject(CallingData);
            JSONObject voipPayload = new JSONObject();
            voipPayload.put(AppConstant.JSONFiled.VOIP_PAYLOAD, jsonObject);

            RestApiClientEvent apiClientEvent = new RestApiClientEvent( context, AppConstant.MISSED_CALL_INFO_URL);
            apiClientEvent.setRequestJson(voipPayload);
            apiClientEvent.setListener(new IWebSocketClientEvent() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    Log.e(TAG, "onSuccess: " + jsonObject );
                }

                @Override
                public void onFinish(Error error) {
                    Log.e(TAG, "onFinish: " + error );
                }
            });
            apiClientEvent.fire();


            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(CallManager.CHANNEL_ID, CallManager.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription(context.getString(R.string.call_channel_name));
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Intent missCallIntent = new Intent(context, VideoSessionActivity.class);

            String tokenUrl = Utility.getStringObjectValue(jsonObject,  AppConstant.JSONFiled.TWILIO_TOKEN_URL);
            String roomName = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.ROOM_NAME);

            UserInfo userInfo = new UserInfo();
            userInfo.setName(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.USER_NAME));
            userInfo.setUserId(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.USER_ID));
            userInfo.setSecretKey(callPreference.getValue(AppConstant.AppPref.AUTH_SECRET_KEY));
            userInfo.setTokenKey(callPreference.getValue(AppConstant.AppPref.AUTH_TOKEN));

            CallParams callParams = new CallParams();


            String callType = AppConstant.CallType.VIDEO;
            callParams.setVideo(true);
            if(jsonObject.has(AppConstant.JSONFiled.IS_AUDIO_ONLY) && jsonObject.getBoolean(AppConstant.JSONFiled.IS_AUDIO_ONLY)) {
                callType = AppConstant.CallType.AUDIO;
                callParams.setVideo(false);
            }

            callParams.setRoomName(roomName);
            callParams.setTokenUrl(tokenUrl);
            String callerName = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALLER_NAME);
            callParams.setCallerName(callerName);
            callParams.setCallerProfileImage(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.CALLER_IMAGE));
            callParams.setCallUniqueId(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.CALL_UNIQUE_ID));
            callParams.setAccessToken(null);
            callParams.setIncomingCall(false);
            callParams.setCallFromNotification(false);

            Bundle bundleData = new Bundle();
            bundleData.putParcelable(VideoSessionActivity.EXTRA_CALL_PARAMS, callParams);
            bundleData.putParcelable(AppConstant.EXTRA_USER_DETAILS, userInfo);
            missCallIntent.putExtras(bundleData);
            missCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pIntent = PendingIntent.getActivity(context,0, missCallIntent, PendingIntent.FLAG_IMMUTABLE);
            } else  {
                pIntent = PendingIntent.getActivity(context,0, missCallIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }


            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CallManager.CHANNEL_ID);
            builder.setContentTitle(callerName)
                    .setContentText(context.getString(R.string.call_notification_type,callType ))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setAutoCancel(true)
                    .setContentIntent(pIntent);

            mNotificationManager.notify( 11, builder.build() );
            //callPreference.setUserCallParams("");
            AppConstant.CallData.NOTIFICATION_DATA = "";
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
