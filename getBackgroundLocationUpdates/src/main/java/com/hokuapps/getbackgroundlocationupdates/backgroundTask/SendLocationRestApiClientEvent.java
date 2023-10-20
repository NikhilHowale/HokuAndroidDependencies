package com.hokuapps.getbackgroundlocationupdates.backgroundTask;


import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;


import com.hokuapps.getbackgroundlocationupdates.Utility.LocUtility;
import com.hokuapps.getbackgroundlocationupdates.callbacks.SendLocationListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SendLocationRestApiClientEvent {
    private Context mContext;
    private String apiFullUrl;
    private JSONObject requestJson;
    protected Looper looper;
    public static volatile DispatchQueue taskQueue = new DispatchQueue("taskQueue");
    SendLocationListener listener;
    public static final int STATUS_SUCCESS = 0;

    public SendLocationRestApiClientEvent(Context context, String apiFullUrl) {
        this.mContext = context;
        this.apiFullUrl = apiFullUrl;
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


    public void setListener(SendLocationListener listener) {
        this.listener = listener;
    }


    protected JSONObject buildPayload() {
        JSONObject jsonObject = new JSONObject();

        jsonObject = this.requestJson;

        return jsonObject;

    }


    public void fire() {
        if (!LocUtility.isNetworkAvailable(mContext)) {
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
                            listener.onFailure(response);
                        }


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        } catch (Exception e) {


        }

    }

    private void parseResponse(Object arg, SendLocationListener listener) {
        try {
            JSONObject jsonObject = new JSONObject(arg.toString());
            int statusCode = 0;

            if (jsonObject.has("statusCode"))
                statusCode = jsonObject.getInt("statusCode");

            if (statusCode == STATUS_SUCCESS) {
                listener.onSuccess(jsonObject);

            } else {

            }
        } catch (JSONException e) {
            e.printStackTrace();

        }

    }


    private Headers getAuthorization(String authorization) {

        Headers headers = new Headers.Builder()
                .add("Authorization", authorization)
                .build();
        return headers;
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

        String authorization = LocUtility.getStringObjectValue(requestJson, "Authorization");

        if (!TextUtils.isEmpty(authorization)) {
            requestBuilder.headers(getAuthorization(authorization));
        }

        Request request = requestBuilder.build();
        Response response = client.newCall(request).execute();
        return response;
    }

}