package com.hokuapps.updateappversion;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateVersionApiClient {

    private Context mContext;
    private String apiFullUrl;

    private String tokenKey;

    private String secretKey;
    private JSONObject requestJson;
    protected Looper looper;
    public static volatile DispatchQueue taskQueue = new DispatchQueue("taskQueue");
    IWebSocketClientEvent listener;
    public static final int STATUS_SUCCESS = 0;

    public UpdateVersionApiClient(Context context, String apiFullUrl, String tokenKey, String secretKey) {
        this.tokenKey = tokenKey;
        this.secretKey = secretKey;
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


    public void setListener(IWebSocketClientEvent listener) {
        this.listener = listener;
    }


    protected JSONObject buildPayload() {
        JSONObject jsonObject = new JSONObject();
        try {


            jsonObject = (requestJson == null ? new JSONObject() : requestJson);
            jsonObject.put("tokenKey", tokenKey);
            jsonObject.put("secretKey", secretKey);
            jsonObject.put("queryMode", "mylist");
            jsonObject.put("isMobile", true);
            jsonObject.put("isiPad", false);

            return jsonObject;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return jsonObject;

    }


    public void fire() {
        if (!Utility.isNetworkAvailable(mContext)) {
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

    private void parseResponse(Object arg, IWebSocketClientEvent listener) {
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

        String authorization = Utility.getStringObjectValue(requestJson, "Authorization");

        if (!TextUtils.isEmpty(authorization)) {
            requestBuilder.headers(getAuthorization(authorization));
        }

        Request request = requestBuilder.build();
        Response response = client.newCall(request).execute();
        return response;
    }

}
