package com.hokuapps.startvideocall.broadcastReceiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.work.WorkManager;

import com.hokuapps.startvideocall.network.RestApiClientEvent;
import com.hokuapps.startvideocall.pref.CallPreference;
import com.hokuapps.startvideocall.twilioVideo.VideoSessionActivity;
import com.hokuapps.startvideocall.twilioVideo.model.CallParams;
import com.hokuapps.startvideocall.twilioVideo.model.UserInfo;
import com.hokuapps.startvideocall.utils.AppConstant;
import com.hokuapps.startvideocall.utils.Utility;

import org.json.JSONObject;


public class CallNotificationActionReceiver extends BroadcastReceiver {


    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext=context;
        if (intent != null && intent.getExtras() != null) {

            String action ="";
            action = intent.getStringExtra(AppConstant.BroadCastCallData.ACTION_TYPE);

            String notificationData = intent.getStringExtra(AppConstant.BroadCastCallData.NOTIFICATION_DATA);
            if(notificationData == null || notificationData.length() == 0 ){
                WorkManager.getInstance(mContext).cancelAllWork();
                return;
            }

            if (action != null&& !action.equalsIgnoreCase("")) {
                performClickAction(context, action, notificationData);
            }

            WorkManager.getInstance(mContext).cancelAllWork();

        }


    }

    /**
     * This method call when user click on notification accept/ reject button
     * @param context context
     * @param action type of action - accept, reject
     * @param data json string with call details
     */
    private void performClickAction(Context context, String action, String data) {
        if(action.equalsIgnoreCase(AppConstant.BroadCastCallData.RECEIVE_CALL)) {
            try {
                Log.e("CALL", "CallNotificationActionReceiver :   " + AppConstant.BroadCastCallData.RECEIVE_CALL);
                JSONObject jsonObject = new JSONObject(data);

                String tokenUrl = Utility.getStringObjectValue(jsonObject,  AppConstant.JSONFiled.TWILIO_TOKEN_URL);
                String roomName = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.ROOM_NAME);


                CallPreference callPreference = new CallPreference(context);

                UserInfo userInfo = new UserInfo();
                userInfo.setName(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.USER_NAME));
                userInfo.setUserId(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.USER_ID));
                userInfo.setSecretKey(callPreference.getSecretKey());
                userInfo.setTokenKey(callPreference.getToken());

                CallParams callParams = new CallParams();

                callParams.setVideo(true);
                if(jsonObject.has(AppConstant.JSONFiled.IS_AUDIO_ONLY) && jsonObject.getBoolean(AppConstant.JSONFiled.IS_AUDIO_ONLY)) {
                    callParams.setVideo(false);
                }

                callParams.setRoomName(roomName);
                callParams.setTokenUrl(tokenUrl);
                callParams.setCallerName(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALLER_NAME));
                callParams.setCallerProfileImage(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.CALLER_IMAGE));
                callParams.setCallUniqueId(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.CALL_UNIQUE_ID));
                callParams.setAccessToken(null);
                callParams.setIncomingCall(true);
                callParams.setCallFromNotification(false);

                Bundle bundleData = new Bundle();
                bundleData.putParcelable(VideoSessionActivity.EXTRA_CALL_PARAMS, callParams);
                bundleData.putParcelable(AppConstant.EXTRA_USER_DETAILS, userInfo);

                Intent intent = new Intent(mContext, VideoSessionActivity.class);
                intent.putExtras(bundleData);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);

            }catch (Exception e){
                e.printStackTrace();
            }

        }
        else if(action.equalsIgnoreCase(AppConstant.BroadCastCallData.DIALOG_CALL)){
            try {
                Log.e("CALL", "CallNotificationActionReceiver :   " + AppConstant.BroadCastCallData.DIALOG_CALL);
                JSONObject jsonObject = new JSONObject(data);

                String tokenUrl = Utility.getStringObjectValue(jsonObject,  AppConstant.JSONFiled.TWILIO_TOKEN_URL);
                String roomName = Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.ROOM_NAME);

                CallPreference callPreference = new CallPreference(context);

                UserInfo userInfo = new UserInfo();
                userInfo.setName(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.USER_NAME));
                userInfo.setUserId(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.USER_ID));
                userInfo.setSecretKey(callPreference.getValue(AppConstant.AppPref.AUTH_SECRET_KEY));
                userInfo.setTokenKey(callPreference.getValue(AppConstant.AppPref.AUTH_TOKEN));

                CallParams callParams = new CallParams();

                callParams.setVideo(true);
                if(jsonObject.has(AppConstant.JSONFiled.IS_AUDIO_ONLY) && jsonObject.getBoolean(AppConstant.JSONFiled.IS_AUDIO_ONLY)) {
                    callParams.setVideo(false);
                }

                callParams.setRoomName(roomName);
                callParams.setTokenUrl(tokenUrl);
                callParams.setCallerName(Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALLER_NAME));
                callParams.setCallerProfileImage(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.CALLER_IMAGE));
                callParams.setCallUniqueId(Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.CALL_UNIQUE_ID));
                callParams.setAccessToken(null);
                callParams.setIncomingCall(true);
                callParams.setCallFromNotification(true);

                Bundle bundleData = new Bundle();

                bundleData.putParcelable(VideoSessionActivity.EXTRA_CALL_PARAMS, callParams);
                bundleData.putParcelable(AppConstant.EXTRA_USER_DETAILS, userInfo);

                Intent intent = new Intent(mContext, VideoSessionActivity.class);
                intent.putExtras(bundleData);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
                WorkManager.getInstance(mContext).cancelAllWork();
            }catch (Exception exception){
                exception.printStackTrace();
            }
         }
        else {
            try {
                Log.e("CALL", "CallNotificationActionReceiver :   " + action);
                JSONObject jsonObject = new JSONObject(data);
                String roomName = Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.ROOM_NAME);
                String mUserId = Utility.getStringObjectValue(jsonObject,AppConstant.JSONFiled.USER_ID);

                JSONObject senderObject = new JSONObject();
                senderObject.put(AppConstant.JSONFiled.ROOM_NAME,roomName);
                senderObject.put(AppConstant.JSONFiled.ACTION_STATUS,AppConstant.CALL_REJECTED);
                senderObject.put(AppConstant.JSONFiled.USER_ID,mUserId);
                senderObject.put(AppConstant.JSONFiled.CALL_UNIQUE_ID, Utility.getStringObjectValue(jsonObject, AppConstant.JSONFiled.CALL_UNIQUE_ID));

                RestApiClientEvent apiClientEvent = new RestApiClientEvent(mContext,AppConstant.CALL_STATUS_URL);
                apiClientEvent.setRequestJson(senderObject);
                apiClientEvent.fire();

                WorkManager.getInstance(mContext).cancelAllWork();

            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

}