package com.hokuapps.calendaroprations;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;


import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

public class CalendarOperations {

    private final Context mContext;

    public CalendarOperations(Context context){
        this.mContext = context;
    }

    public void performCalendarOperations(Activity activity, String eventData, WebView mWebView){

        final String[] nextButtonCallBack = {" "};


        Gson gson= new Gson();
        CalenderEventModel eventModel = gson.fromJson(eventData,CalenderEventModel.class);
        //Do  validation before  asking permissions
        ArrayList<String> validationErrors = validateEventData(eventModel);

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                try {

                    JSONObject targetDataJsonObj = new JSONObject(eventData);

                    // Save the data into a Model

                    Gson gson = new Gson();
                    CalenderEventModel eventModel = gson.fromJson(targetDataJsonObj.toString(), CalenderEventModel.class);
                    nextButtonCallBack[0] = eventModel.getNextButtonCallback();


                    if (eventModel != null) {

                        // Perform the event CURD operation on Calender And get the result in an Object

                        EventResult eventResult = CalendarEventManager.Builder(activity).performEventCRUDOperations(eventModel);

                        //prepare a JasonObject to send the confirmation status back to patch
                        if (eventResult != null) {
                            JSONObject resultJason = new JSONObject();
                            resultJason.put("status", eventResult.getStatus());
                            resultJason.put("message", eventResult.getMessage());

                            //Give Result Status Back to Patch

                            callJavaScriptFunction(activity, mWebView,
                                    nextButtonCallBack[0], resultJason);
                        }
                    }


                } catch (JSONException ex) {
                    ex.printStackTrace();
                    JSONObject resulJason = new JSONObject();
                    try {
                        resulJason.put("status", "1");
                        resulJason.put("message", ex.getMessage());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Give Result Status Back to Patch
                    callJavaScriptFunction(activity, mWebView,
                            nextButtonCallBack[0], resulJason);
                }

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }


        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setPermissions(Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.READ_CALENDAR)
                .check();

    }


    public ArrayList<String> validateEventData(CalenderEventModel eventModel) {
        ArrayList<String> validationErrors = new ArrayList<String>();
        validationErrors = new CalendarEventValidator().validate( eventModel);
        return validationErrors;
    }



    public  void callJavaScriptFunction(Activity activity, final WebView webView, final String callingJavaScriptFn, final JSONObject response){
        if (activity == null) return;


        activity.runOnUiThread(() -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(String.format("javascript:" + callingJavaScriptFn + "(%s)", response), null);
                } else {
                    webView.loadUrl(String.format("javascript:" + callingJavaScriptFn + "(%s)", response));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
    }
}
