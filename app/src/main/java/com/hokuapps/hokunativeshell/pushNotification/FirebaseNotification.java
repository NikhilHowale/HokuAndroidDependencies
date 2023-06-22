package com.hokuapps.hokunativeshell.pushNotification;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


import com.hokuapps.hokunativeshell.App;
import com.hokuapps.hokunativeshell.constants.AppConstant;
import com.hokuapps.hokunativeshell.pref.MybeepsPref;
import com.hokuapps.hokunativeshell.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


public class FirebaseNotification {

    private FirebaseAnalytics firebaseAnalytics;
    private Context context;

    private static final String TAG = "FirebaseNotification";

    public FirebaseNotification(Context context) {
        this.context = context;
    }


    public void configureNotification() {

        firebaseAnalytics = FirebaseAnalytics.getInstance(context);

        String googleJasonData = loadJSONFromAsset(context);

        if (googleJasonData != null) {

            try {
                JSONObject googleObject = new JSONObject(googleJasonData);
                String projectString = Utility.getStringObjectValue(googleObject,"project_info");
                JSONObject projectInfoObject = new JSONObject(projectString);
                String projectId = Utility.getStringObjectValue(projectInfoObject,"project_id");
                String project_number = Utility.getStringObjectValue(projectInfoObject,"project_number");
                String clientString = Utility.getStringObjectValue(googleObject,"client");
                JSONArray clientArray = new JSONArray(clientString);
                JSONObject clientObject = new JSONObject(String.valueOf(clientArray.get(0)));
                String clientInfo = Utility.getStringObjectValue(clientObject,"client_info");
                String applicationId = Utility.getStringObjectValue(new JSONObject(clientInfo),"mobilesdk_app_id");
                String api_key = Utility.getStringObjectValue(clientObject,"api_key");
                JSONArray apiArray = new JSONArray(api_key);
                JSONObject apiArrayObject = new JSONObject(String.valueOf(apiArray.get(0)));
                String apiKey = Utility.getStringObjectValue(new JSONObject(String.valueOf(apiArrayObject)),"current_key");

                initFCMFirebaseAcct(context, projectId, project_number,applicationId,apiKey);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public void initFCMFirebaseAcct(Context context, String projectId, String gcmSenderId, String applicationId, String apiKey) {

        FirebaseOptions options2 = new FirebaseOptions.Builder()
                .setProjectId(projectId)
                .setApiKey(apiKey)
                .setApplicationId(applicationId)
                .setGcmSenderId(gcmSenderId)
                .build();

        try {
            Log.e(TAG, "firebase fcm initialized");
            FirebaseApp firebaseApp = FirebaseApp.initializeApp(context, options2, "FCM_Notification");
            loadToken(firebaseApp);
        } catch (Exception e) {
            FirebaseApp firebaseApp = FirebaseApp.getInstance("FCM_Notification");
            loadToken(firebaseApp);
            Log.e(TAG, "App already exists");
        }


    }

    private void loadToken(FirebaseApp firebaseApp) {
        FirebaseInstanceId.getInstance(firebaseApp).getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Log.e(TAG+ "  notificationToken: ", token);
                MybeepsPref mybeepsPref = new MybeepsPref(App.getInstance().getApplicationContext());
                if(mybeepsPref != null){
                    mybeepsPref.setValue(AppConstant.NOTIFICATION_TOKEN,token);
                }


            }

        });

        FirebaseInstanceId.getInstance(firebaseApp).getInstanceId().addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("google-services.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
