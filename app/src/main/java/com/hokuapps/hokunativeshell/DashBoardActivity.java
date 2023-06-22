package com.hokuapps.hokunativeshell;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.hokuapps.hokunativeshell.activity.WebAppActivity;
import com.hokuapps.hokunativeshell.constants.AppConstant;
import com.hokuapps.hokunativeshell.constants.IntegrationManager;
import com.hokuapps.hokunativeshell.pref.MybeepsPref;
import com.hokuapps.hokunativeshell.pushNotification.FirebaseNotification;
import com.hokuapps.hokunativeshell.utils.Utility;

import org.json.JSONObject;

public class DashBoardActivity extends AppCompatActivity {

    private String launchPageName = "";
    public static String TAG =  "DashBoardActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(IntegrationManager.appStatusBarColor));
        }

        FirebaseNotification firebaseNotification = new FirebaseNotification(this);
        firebaseNotification.configureNotification();

        if (getIntent().getExtras() != null) {
            try {
                JSONObject jsonObject = new JSONObject();
                for (String key : getIntent().getExtras().keySet()) {
                    String value = "";
                    value = getIntent().getExtras().getString(key);
                    Log.e(TAG, "Key: " + key + " Value: " + value);
                    jsonObject.put(key, value);
                }
                Log.e(TAG, "Notification Data :" + jsonObject.toString());
                if (jsonObject.has("msg")) {
                    jsonObject.put("isRedirect", true);
                    Intent intent = new Intent("android.net.conn.NOTIFICATION");
                    intent.putExtra(AppConstant.EXTRA_NOTIFICATION_DATA, jsonObject.toString());
                    if (WebAppActivity.isOpen) {
                        sendBroadcast(intent);
                        finish();
                        return;
                    } else {
                        Intent intent1 = new Intent(this, WebAppActivity.class);
                        intent1.putExtra(AppConstant.EXTRA_NOTIFICATION_DATA, jsonObject.toString());
                        startActivity(intent1);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (BuildConfig.LOAD_HTML_DIRECTLY) {

            loadBundleData();

            WebAppActivity.loadWebPageForURLWithOrWithoutAuth(DashBoardActivity.this,
                    AppConstant.FileName.DEFAULT_START_NAME,
                    false,
                    true,
                    true,
                    !Utility.isEmpty(new MybeepsPref(DashBoardActivity.this).getValue(AppConstant.LOGGED_IN_USER_ID)),
                    null,launchPageName);

        }

        finish();
    }

    private void loadBundleData() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                launchPageName = bundle.containsKey("pageName") ? bundle.getString("pageName") : "";
            }

        }

    }
}