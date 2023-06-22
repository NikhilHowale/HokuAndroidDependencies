package com.hokuapps.hokunativeshell.pushNotification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hokuapps.hokunativeshell.App;
import com.hokuapps.hokunativeshell.R;
import com.hokuapps.hokunativeshell.activity.WebAppActivity;
import com.hokuapps.hokunativeshell.constants.AppConstant;
import com.hokuapps.hokunativeshell.utils.Utility;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Map;

public class FCMMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMessagingService";
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e(TAG, remoteMessage.getData().toString());

        handleNotification(App.getInstance().getApplicationContext(), remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e(TAG, "onNewToken: " + token );
    }

    private void handleNotification(Context context, RemoteMessage remoteMessage) {

        try {

            Map<String, String> gcmData = remoteMessage.getData();

            if (remoteMessage.getNotification() != null) {
            }

            if (remoteMessage.getData() != null) {
            }

            if (gcmData.containsKey("msg")) {

                JSONObject jsonObject = new JSONObject(gcmData.get("msg"));

                Log.e(TAG,"Notification Data "+jsonObject.toString());

                String htmlSynchPage = Utility.getStringObjectValue(jsonObject, "htmlSynchPage");

                if (TextUtils.isEmpty(htmlSynchPage)) {
                   //handle chat notification here
                    return;
                }

                showNotification(context, jsonObject);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void showNotification(Context context, JSONObject jsonObject) {
        try {

            final int notifyID = (int) System.currentTimeMillis();
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            String messageText = Utility.getStringObjectValue(jsonObject, "messageText");
            String contentTitle = Utility.getResString(R.string.app_name);

            Intent notifyIntent = new Intent(context, WebAppActivity.class);

            String htmlSyncPageName = (String) Utility.getJsonObjectValue(jsonObject, "htmlSynchPage");
            String queryMode = (String) Utility.getJsonObjectValue(jsonObject, "qm");
            String queryString = Utility.getStringObjectValue(jsonObject, "querystring");


            String fileName = !TextUtils.isEmpty(htmlSyncPageName) ? htmlSyncPageName : AppConstant.FileName.DEFAULT_FILE_NAME;
            notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notifyIntent.putExtra("targetId", "");
            notifyIntent.putExtra(AppConstant.EXTRA_IS_HIDE_MOBILE_HEADER, true);
            notifyIntent.putExtra("isFromNotification", true);
            notifyIntent.putExtra("isIncomingCall", true);
            notifyIntent.putExtra("isLoadLocalHtml", true);
            notifyIntent.putExtra(AppConstant.EXTRA_FILENAME_URL, fileName);
            notifyIntent.putExtra("isFromAuth", true);
            notifyIntent.putExtra(AppConstant.EXTRA_LFC, true);
            notifyIntent.putExtra(AppConstant.EXTRA_IS_APP_COMMAND, true);
            notifyIntent.putExtra(AppConstant.EXTRA_QUERY_MODE, queryMode);
            notifyIntent.putExtra(AppConstant.EXTRA_QUERY_STRING, queryString);
            notifyIntent.putExtra(AppConstant.EXTRA_IS_HIDE_MOBILE_HEADER, true);

            notifyIntent.putExtra(AppConstant.EXTRA_NOTIFICATION_DATA, jsonObject.toString());

            // This is dummy data for just differentiate Pending intent
            // only set value that is check IntentFilter
            notifyIntent.addCategory("CATEGOARY" + new Date(System.currentTimeMillis()));
            notifyIntent.addFlags((int) System.currentTimeMillis());

            // Sets the Activity to start in a new, empty task
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // intent to be launched when click on notification
            PendingIntent pIntent = null;

            //PendingIntent.getActivity(App.getInstance().getApplicationContext(), 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //JSONArray jsonArray = getNotificationTextFromLocal(jsonObject);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {

                pIntent = PendingIntent.getActivity(
                        App.getInstance().getApplicationContext(),
                        0, notifyIntent,
                        PendingIntent.FLAG_IMMUTABLE);
            }
            else
            {
                pIntent = PendingIntent.getActivity(
                        App.getInstance().getApplicationContext(),
                        0, notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            //This is for android O
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        "default123", "ChannelName_" + Utility.getResString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                mChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build());
                mChannel.enableVibration(true);
                mChannel.enableLights(true);
                mNotificationManager.createNotificationChannel(mChannel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context, "default123");
            builder.setAutoCancel(true);
            builder.setContentTitle(contentTitle);
            builder.setContentText(messageText);
            builder.setTicker(messageText);
            builder.setSmallIcon(R.drawable.ic_launcher);
            builder.setNumber(1);

            boolean isPlayAudio = Utility.getJsonObjectBooleanValue(jsonObject, "isPlayAudio");
            if (isPlayAudio) {
                String notifSound = Utility.getStringObjectValue(jsonObject, "audioFileName");

                File file = new File(Utility.getHtmlDirFromSandbox().getAbsolutePath() + File.separator + notifSound);
                File fileExternal = new File(Utility.getExternalCacheDir(context).getAbsolutePath() + File.separator + notifSound);
                copyFile(file, fileExternal);
                Uri uri = Uri.fromFile(fileExternal);
                builder.setSound(uri);
            } else {

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                }

                builder.setDefaults(Notification.DEFAULT_ALL);
            }

            String notifImgUrl = Utility.getStringObjectValue(jsonObject, "media-url");

            Bitmap bmImg = null;

            if (Build.VERSION.SDK_INT >= 16) {

                NotificationCompat.Style style;

                if (TextUtils.isEmpty(notifImgUrl)) {
                    style = getNotificationStyle(contentTitle, messageText);
                } else {
                    bmImg = Ion.with(context)
                            .load(notifImgUrl).asBitmap().get();
                    style = getNotificationPictureStyle(bmImg, messageText);
                }

                if (style != null) {
                    builder.setStyle(style);
                }
            }

            builder.setLargeIcon(bmImg != null
                    ? bmImg
                    : BitmapFactory.decodeResource(App.getInstance().getApplicationContext().getResources(), R.drawable.ic_launcher));

            // will be display in notification list i.e when we pull down notification bar
            builder.setContentIntent(pIntent);

            builder.setPriority(NotificationCompat.PRIORITY_HIGH);

            mNotificationManager.notify(notifyID, builder.build());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void copyFile(File file, File newFile) throws IOException {

        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

    }

    private static NotificationCompat.Style getNotificationPictureStyle(Bitmap bitmap, String messageText) {
        try {
            NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
            bigPictureStyle.bigPicture(bitmap);
            bigPictureStyle.setBigContentTitle(messageText);
            return bigPictureStyle;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static NotificationCompat.Style getNotificationStyle(String contentTitle, String messageText) {
        try {
            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.bigText(messageText);
            bigTextStyle.setBigContentTitle(contentTitle);
            return bigTextStyle;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
