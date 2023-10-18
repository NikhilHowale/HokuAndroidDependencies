package com.cometchat.pro.uikit;

import static com.cometchat.pro.uikit.AppConfig.FcmConstant;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import androidx.core.app.NotificationCompat;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.helpers.CometChatHelper;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;
import com.cometchat.pro.uikit.ui_components.cometchat_ui.CometChatUI;
import com.cometchat.pro.uikit.ui_components.messages.message_list.CometChatMessageListActivity;
import com.cometchat.pro.uikit.ui_resources.constants.UIKitConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CometChatStart {

    private static final String TAG = "CometChatInitialization";

    @SuppressLint("StaticFieldLeak")
    public static CometChatStart instance;
    private Context context;
    private boolean isDashBoard = false;
    private boolean isCometChatInit = false;
    private boolean isWebNotification = false;
    private String mNotificationData = "";

    private WebView mWebView;


    public static synchronized CometChatStart getInstance(){
        if(instance == null ){
            instance = new CometChatStart();
        }
        return instance;
    }

    /**
     * This method initialize comet chat
     * @param context context
     * @param data app data
     * @param pushToken push token
     * @param isNotification flag decide is from notification call
     */
    public void startCometChatInit(Context context,String data, String pushToken, boolean isNotification){
        try {

            CometChatPreference pref = new CometChatPreference(context);
            pref.setValue("appIdData",data);

            JSONObject jsonObject = new JSONObject(data);

            String appID = "";

            if(jsonObject.has("appID")){
                appID = Utility.getStringObjectValue(jsonObject,"appID");
            }
            if(jsonObject.has("appId")){
                appID= Utility.getStringObjectValue(jsonObject,"appId");
            }

            String uid = Utility.getStringObjectValue(jsonObject,"uid");
            String authKey = Utility.getStringObjectValue(jsonObject,"authKey");

            AppSettings appSettings = new AppSettings.AppSettingsBuilder()
                    .subscribePresenceForFriends()
                    .autoEstablishSocketConnection(true)
                    .setRegion(AppConfig.REGION).build();

            if(appID == null || authKey == null){
                Log.e(TAG, "startCometChatInit: appID or uId may be null"  );
                return;
            }

            AppConfig.APP_ID = appID;
            AppConfig.COMET_CHAT_USER_UID = uid;
            AppConfig.AUTH_KEY = authKey;
            AppConfig.PUSH_TOKEN = pushToken;


            CometChat.init(context, appID, appSettings, new CometChat.CallbackListener<String>() {
                @Override
                public void onSuccess(String successMessage) {
                    isCometChatInit = true;
                    if(isNotification){
                        cometChatNotificationNavigation(context);
                        return;
                    }

                    if(isWebNotification){
                        cometChatSetup(context,mNotificationData,"",false,null);
                    }

                    cometChatLogin(false);

                }
                @Override
                public void onError(CometChatException e) {
                    Log.d(TAG, "Initialization failed with exception: " + e.getMessage());
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method setup the comet chat variable
     * @param mContext context
     * @param data json data in string
     * @param htmlDirectory html page to show medical details
     * @param isDashBoardOpen flag to open dash board or individual chat
     * @param mWebView webView reference
     */
    public void cometChatSetup( Context mContext, String data, String htmlDirectory, boolean isDashBoardOpen, WebView mWebView ){
        try{
            JSONObject jsonObject = new JSONObject(data);
            context = mContext;
            this.mWebView = mWebView;
            AppConfig.APP_ID = Utility.getStringObjectValue(jsonObject,"appID");
            AppConfig.COMET_CHAT_USER_UID = Utility.getStringObjectValue(jsonObject,"uid");
            AppConfig.GROUP_ID = Utility.getStringObjectValue(jsonObject,"groupid");
            AppConfig.USER_ID = Utility.getStringObjectValue(jsonObject,"userid");
            AppConfig.AUTH_KEY = Utility.getStringObjectValue(jsonObject,"authKey");
            AppConfig.SECRET_KEY = Utility.getStringObjectValue(jsonObject,"secretKey");
            AppConfig.TOKEN_KEY = Utility.getStringObjectValue(jsonObject,"tokenKey");
            AppConfig.ENABLE_DELETE_MESSAGE = Utility.getJsonObjectBooleanValue(jsonObject,"enableDeleteMessage");
            AppConfig.DISABLE_LOCATION_MESSAGE = Utility.getJsonObjectBooleanValue(jsonObject,"disableLocationMessage");
            AppConfig.ENABLE_PIN_MESSAGE = Utility.getJsonObjectBooleanValue(jsonObject,"enablePinMessage");
            AppConfig.HTML_DIRECTORY = htmlDirectory;

            AppConfig.CALLBACK_FUNCTION = Utility.getStringObjectValue(jsonObject,"backButtonCallback");

            AppConfig.SHOW_DEFAULT_GALLERY = Utility.getJsonObjectBooleanValue(jsonObject,"showDefaultGallery");

            if(jsonObject.has("groupDetailsPage")){
                AppConfig.MEDICAL_DETAILS = Utility.getStringObjectValue(jsonObject,"groupDetailsPage");
            }

           if(jsonObject.has("isChats")){
                AppConfig.DashBoardTabs.IS_CHATS = Utility.getJsonObjectBooleanValue(jsonObject,"isChats");
            }



            if(jsonObject.has("isUsers")){
                AppConfig.DashBoardTabs.IS_USERS = Utility.getJsonObjectBooleanValue(jsonObject,"isUsers");
            }


            if(jsonObject.has("isGroup")){
                AppConfig.DashBoardTabs.IS_GROUPS = Utility.getJsonObjectBooleanValue(jsonObject,"isGroup");
            }

            List<String> userRoles = null;
            if(jsonObject.has("roles")){
                userRoles = new ArrayList<>();
                JSONArray jsonArray = jsonObject.getJSONArray("roles");
                for (int i = 0; i < jsonArray.length(); i++){
                    userRoles.add(jsonArray.getString(i));
                }
            }

            isDashBoard = isDashBoardOpen;
            initializeCometChat();
            AppConfig.getInstance().setRoles(userRoles);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *  chat navigation if comet Initialize
     */
    public void initializeCometChat(){

        if(isCometChatInit) {
            if (CometChat.getLoggedInUser() != null) {
                if (isDashBoard) {
                    navigateToDashBoard(null);
                } else {
                    getUserAndGroupDetails();
                }
            }
            else
                cometChatLogin(true);
        }
    }

    /**
     * This method use to login to cometchat
     * @param isRedirect decide to open chat
     */
    public void cometChatLogin(boolean isRedirect){

        if(AppConfig.COMET_CHAT_USER_UID == null){
            return;
        }

        CometChat.login(AppConfig.COMET_CHAT_USER_UID, AppConfig.AUTH_KEY, new CometChat.CallbackListener<User>() {
            @Override
            public void onSuccess(User user) {
                if(isRedirect) {
                    if (isDashBoard) {
                        navigateToDashBoard(null);
                    } else {
                        getUserAndGroupDetails();
                    }
                }
                sendPushNotificationToken(AppConfig.PUSH_TOKEN);
            }

            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }
        });
    }

    /**
     *  This method use to logout from cometchat
     */
    public void cometChatLogout(){
        CometChat.logout(new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
                Log.d(TAG, "Logout completed successfully");
            }
            @Override
            public void onError(CometChatException e) {
                Log.d(TAG, "Logout failed with exception: " + e.getMessage());
            }
        });
    }

    /**
     *  Call comet chat method to fetch group or user details
     */
    private void getUserAndGroupDetails(){
        if(AppConfig.GROUP_ID != null){
            CometChat.getGroup(AppConfig.GROUP_ID, new CometChat.CallbackListener<Group>() {
                @Override
                public void onSuccess(Group group) {
                    if(group != null  && group.isJoined()) {
                        navigateToGroupChat(getInstance().context, group);
                    }
                }

                @Override
                public void onError(CometChatException e) {
                    Log.d(TAG, "Group details fetching failed with exception: " + e.getMessage());

                }
            });
        }

        if(AppConfig.USER_ID != null){
            CometChat.getUser(AppConfig.USER_ID, new CometChat.CallbackListener<User>() {
                @Override
                public void onSuccess(User user) {
                    navigateToUserChat(getInstance().context,user);
                }

                @Override
                public void onError(CometChatException e) {
                    Log.e(TAG, "onError: single user " + e.getMessage() );
                }
            });
        }
    }


    /**
     * This method register push token to comet chat
     * @param token push token
     */
    public static void sendPushNotificationToken(String token){

        if(token == null || token.length() == 0){
            return;
        }

        CometChat.registerTokenForPushNotification(token, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String s) {
                Log.e( TAG,"pushToken Registration : for comet"  );
            }
            @Override
            public void onError(CometChatException e) {
                Log.e(TAG, Objects.requireNonNull(e.getLocalizedMessage()));
            }
        });
    }


    public void createCometChatNotification(Context context, String title, JSONObject jsonObject, Class<? extends Activity> activity){
        try {

            JSONObject dataObject = null;
            if(jsonObject.has(FcmConstant.NOTIFICATION_DATA) && jsonObject.getString(FcmConstant.NOTIFICATION_DATA).length() > 0) {

                dataObject = jsonObject.getJSONObject(FcmConstant.NOTIFICATION_DATA);

                final int notifyID = (int) System.currentTimeMillis();
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                //This is for android O
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(
                            AppConfig.FcmConstant.CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                    mChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .build());
                    mChannel.enableVibration(true);
                    mChannel.enableLights(true);
                    mNotificationManager.createNotificationChannel(mChannel);
                }

                Intent notifyIntent = new Intent(context, activity);

                String type = jsonObject.getString(FcmConstant.MESSAGE_TYPE);
                String messageText = "";
                String url = "";
                String htmlSyncPageName = "";
                String queryMode = "";
                String  queryString = "";

                if(dataObject.has(FcmConstant.MESSAGE_TEXT) && dataObject.getString(FcmConstant.MESSAGE_TEXT).length() > 0){
                    messageText = dataObject.getString(FcmConstant.MESSAGE_TEXT);
                }

                String fileName = !TextUtils.isEmpty(htmlSyncPageName) ? htmlSyncPageName : FcmConstant.DEFAULT_FILE_NAME;

                notifyIntent.putExtra(FcmConstant.EXTRA_NOTIFICATION_DATA, jsonObject.toString());
                notifyIntent.addCategory("CATEGOARY" + new Date(System.currentTimeMillis()));
                notifyIntent.addFlags((int) System.currentTimeMillis());
                notifyIntent.putExtra(FcmConstant.IS_FROM_NOTIFICATION, true);
                notifyIntent.putExtra(FcmConstant.IS_COMET_CHAT, true);
                notifyIntent.putExtra(FcmConstant.IS_LOCAL_HTML, true);
                notifyIntent.putExtra(FcmConstant.EXTRA_FILENAME_URL, fileName);
                notifyIntent.putExtra(FcmConstant.IS_FROM_AUTH, true);
                notifyIntent.putExtra(FcmConstant.EXTRA_LFC, true);
                notifyIntent.putExtra(FcmConstant.EXTRA_IS_APP_COMMAND, true);
                notifyIntent.putExtra(FcmConstant.EXTRA_QUERY_MODE, queryMode);
                notifyIntent.putExtra(FcmConstant.EXTRA_QUERY_STRING, queryString);
                notifyIntent.putExtra(FcmConstant.EXTRA_IS_HIDE_MOBILE_HEADER, true);

                // Sets the Activity to start in a new, empty task
                notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                PendingIntent pIntent = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    pIntent = PendingIntent.getActivity( context,0, notifyIntent, PendingIntent.FLAG_IMMUTABLE);
                } else {
                    pIntent = PendingIntent.getActivity( context,0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder( context, AppConfig.FcmConstant.CHANNEL_ID);
                builder.setAutoCancel(true);
                builder.setContentTitle(title);
                builder.setContentText(Utility.fromHtml(messageText));
                builder.setTicker(Utility.fromHtml(messageText));
                builder.setSmallIcon(R.drawable.ic_launcher);

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                }

                builder.setDefaults(Notification.DEFAULT_ALL);

                NotificationCompat.Style style = null;

                if (type.length() > 0 && type.equals(FcmConstant.MESSAGE_TEXT)) {
                    style = Utility.getNotificationStyle(title, messageText);
                } else if (type.length() > 0 && type.equals(FcmConstant.MESSAGE_IMAGE)){
                    builder.setContentText(context.getString(R.string.sent_image));
                } else if (type.length() > 0 && type.equals(FcmConstant.MESSAGE_FILE)){
                    builder.setContentText(context.getString(R.string.sent_file));
                }

                if (style != null) {
                    builder.setStyle(style);
                }

                builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));

                builder.setContentIntent(pIntent);

                builder.setPriority(NotificationCompat.PRIORITY_HIGH);

                mNotificationManager.notify(notifyID, builder.build());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method handle notification from web
     * @param context context
     * @param notificationData json data is in string
     * @param isCometChat flag to decide is comet notification
     */
    public void handleWebChatNotification(Context context, String notificationData, boolean isCometChat){
        try {
            JSONObject jsonObject = new JSONObject(notificationData);
            if(!jsonObject.has("cometchatobj")){
                return;
            }
            mNotificationData = jsonObject.getString("cometchatobj");;
            Handler cometHandler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    isWebNotification = true;

                    if(!isCometChatInit) {
                        CometChatPreference pref = new CometChatPreference(context);
                        String appIdData = pref.getValue("appIdData");
                        if (appIdData != null && appIdData.length() >0) {
                            startCometChatInit(context,appIdData,"",false);
                        }
                        if(isCometChat)
                            return;
                    }
                    if(isCometChat) {
                        cometChatSetup(context,mNotificationData,"",false,null);
                    }

                }
            };

            cometHandler.postDelayed(runnable, 0);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * This method handle comet chat notification
     * @param context context
     * @param notificationData json data in string
     * @param pushToken push token
     * @param isCometChat flag to decide is comet notification
     */
    public void handleCometChatNotification(Context context, String notificationData, String pushToken, boolean isCometChat){

        if(!isCometChat){
            return;
        }

        Handler cometHandler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if(notificationData == null || notificationData.length() == 0)
                    return;

                mNotificationData = notificationData;

                AppConfig.PUSH_TOKEN = pushToken;

                if(!isCometChatInit) {
                    CometChatPreference pref = new CometChatPreference(context);
                    String appIdData = pref.getValue("appIdData");
                    if (appIdData != null && appIdData.length() >0) {
                        startCometChatInit(context, appIdData, pushToken,true);
                    }

                    if(isCometChat)
                        return;
                }

                if(isCometChat) {
                    cometChatNotificationNavigation(context);
                }
            }
        };

        cometHandler.postDelayed(runnable, 500);

    }

    /**
     * This method handle comet chat redirection notification
     * @param mContext context
     */
    public void cometChatNotificationNavigation(Context mContext) {
        try {
            context = mContext;
            BaseMessage baseMessage = CometChatHelper.processMessage(new JSONObject(mNotificationData));
            if (CometChat.getLoggedInUser() == null) return;
            if (baseMessage == null) return;

            if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_GROUP)) {
                Group group = (Group) baseMessage.getReceiver();
                AppConfig.GROUP_ID = group.getGuid();
                AppConfig.USER_ID = null;
                getUserAndGroupDetails();
            } else if (baseMessage.getReceiverType().equals(CometChatConstants.RECEIVER_TYPE_USER)) {
                User user = (User) baseMessage.getReceiver();
                navigateToDashBoard(mNotificationData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method navigate to group chat screen
     * @param context context
     * @param group group data model
     */
    private static void navigateToGroupChat(Context context,Group group){
        Intent intent = new Intent(context, CometChatMessageListActivity.class);
        intent.putExtra(UIKitConstants.IntentStrings.GUID, group.getGuid());
        intent.putExtra(UIKitConstants.IntentStrings.AVATAR, group.getIcon());
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_OWNER,group.getOwner());
        intent.putExtra(UIKitConstants.IntentStrings.NAME, group.getName());
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_TYPE,group.getGroupType());
        intent.putExtra(UIKitConstants.IntentStrings.TYPE, CometChatConstants.RECEIVER_TYPE_GROUP);
        intent.putExtra(UIKitConstants.IntentStrings.MEMBER_COUNT,group.getMembersCount());
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_DESC,group.getDescription());
        intent.putExtra(UIKitConstants.IntentStrings.GROUP_PASSWORD,group.getPassword());
        context.startActivity(intent);
    }


    /**
     * This method navigate to one to one chat screen
     * @param context context
     * @param user other user data model
     */
    private static void navigateToUserChat(Context context,User user){
        Intent intent = new Intent(context, CometChatMessageListActivity.class);
        intent.putExtra(UIKitConstants.IntentStrings.UID, user.getUid());
        intent.putExtra(UIKitConstants.IntentStrings.AVATAR, user.getAvatar());
        intent.putExtra(UIKitConstants.IntentStrings.STATUS, user.getStatus());
        intent.putExtra(UIKitConstants.IntentStrings.NAME, user.getName());
        intent.putExtra(UIKitConstants.IntentStrings.LINK,user.getLink());
        intent.putExtra(UIKitConstants.IntentStrings.TYPE, com.cometchat.pro.constants.CometChatConstants.RECEIVER_TYPE_USER);
        context.startActivity(intent);
    }

    /**
     *  This method use to open comet chat dashboard
     * @param notificationData notification data
     */
    private void navigateToDashBoard(String notificationData){
        CometChatUI.StartCometChatActivity(context, notificationData);
    }

    public WebView getmWebView(){
        return mWebView;
    }


}
