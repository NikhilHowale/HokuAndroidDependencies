package com.hokuapps.logoutnativecall;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.hokuapps.logoutnativecall.utils.LogoutPref;
import com.hokuapps.logoutnativecall.constants.LogoutConstant;
import com.hokuapps.logoutnativecall.models.Error;
import com.hokuapps.logoutnativecall.utils.LogoutUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LogoutRestApiClientEvent {

    private Context mContext;
    private String apiFullUrl;
    private JSONObject requestJson;
    protected Looper looper;
    IWebSocketClientEvent listener;

    public static final int STATUS_SUCCESS = 0;
    public static volatile DispatchQueue taskQueue = new DispatchQueue("taskQueue");

    public LogoutRestApiClientEvent(Context mContext, String apiName) {
        this.mContext = mContext;
        this.apiFullUrl = apiName;
        requestJson = new JSONObject();
    }

    public void setLooper(Looper looper) {
        this.looper = looper;
    }


    public JSONObject getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(JSONObject requestJson) {
        this.requestJson = requestJson;
    }

    protected String getEventName() {
        return "";
    }


    public void setListener(IWebSocketClientEvent listener) {
        this.listener = listener;
    }

    public void fire() {
        if (!LogoutUtility.isNetworkAvailable(mContext)) {
            try {

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        try {
            final JSONObject jsonObject = buildPayload();
            taskQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        Response response = fireAsyncTask(getEventName(), jsonObject);

                        if (response.isSuccessful()) {
                            parseResponse(response.body().string(), listener);
                        } else {

                            Error error = Error.createError(response.body().string(), mContext);
                            listener.onFailure(error);
                        }


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        } catch (Exception e) {


        }

    }

    protected JSONObject buildPayload() {
        JSONObject jsonObject = new JSONObject();
        try {
            LogoutPref pref = new LogoutPref(mContext);

            jsonObject = (requestJson == null ? new JSONObject() : requestJson);
            jsonObject.put("tokenKey", pref.getValue(LogoutConstant.AppPref.AUTH_TOKEN));
            jsonObject.put("secretKey", pref.getValue(LogoutConstant.AppPref.AUTH_SECRET_KEY));
            jsonObject.put("queryMode", "mylist");
            jsonObject.put("isMobile", true);
            jsonObject.put("isiPad", false);

            return jsonObject;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jsonObject;

    }

    private void parseResponse(Object arg, IWebSocketClientEvent listener) {
        try {
            JSONObject jsonObject = new JSONObject(arg.toString());
            int statusCode = 0;

            if (jsonObject.has(LogoutConstant.AuthIO.STATUS_CODE))
                statusCode = jsonObject.getInt(LogoutConstant.AuthIO.STATUS_CODE);

            if (statusCode == STATUS_SUCCESS) {
                listener.onSuccess(jsonObject);

            } else {

            }
        } catch (JSONException e) {
            e.printStackTrace();

        }

    }

    protected Response fireAsyncTask(String event, JSONObject jsonObject) throws IOException {


        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();

        //change url for app
        String url = apiFullUrl;


        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);

        String authorization = LogoutUtility.getStringObjectValue(requestJson, "Authorization");

        if (!TextUtils.isEmpty(authorization)) {
            requestBuilder.headers(getAuthorization(authorization));
        }

        Request request = requestBuilder.build();
        Response response = client.newCall(request).execute();
        return response;
    }

    private Headers getAuthorization(String authorization) {

        Headers headers = new Headers.Builder()
                .add("Authorization", authorization)
                .build();
        return headers;
    }
}
