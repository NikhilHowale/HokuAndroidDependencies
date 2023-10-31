package com.hokuapps.startvideocall.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hokuapps.startvideocall.R;
import com.hokuapps.startvideocall.delegate.IWebSocketClientEvent;
import com.hokuapps.startvideocall.model.Error;
import com.hokuapps.startvideocall.pref.CallPreference;
import com.hokuapps.startvideocall.utils.AppConstant;
import com.hokuapps.startvideocall.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestApiClientEvent  {

    private static final String TAG = "RestApiClientEvent";


    private Context mContext;
    private String apiFullUrl;

    public static volatile DispatchQueue taskQueue = new DispatchQueue("taskQueue");

    IWebSocketClientEvent listener;
    private JSONObject requestJson;

    public RestApiClientEvent(Context context, String apiFullUrl) {
        this.mContext = context;
        this.apiFullUrl = apiFullUrl;
        requestJson = new JSONObject();
    }

    public JSONObject getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(JSONObject requestJson) {
        this.requestJson = requestJson;
    }

    public void setListener(IWebSocketClientEvent listener) {
        this.listener = listener;
    }

    /**
     * This method build jsonObject payload for api
     */
    protected JSONObject buildPayload() {
        JSONObject jsonObject = new JSONObject();
        try {
            CallPreference pref = new CallPreference(mContext);

            jsonObject = (requestJson == null ? new JSONObject() : requestJson);
            jsonObject.put("tokenKey", pref.getValue(AppConstant.AppPref.AUTH_TOKEN));
            jsonObject.put("secretKey", pref.getValue(AppConstant.AppPref.AUTH_SECRET_KEY));
            jsonObject.put("queryMode", "mylist");
            jsonObject.put("isMobile", true);
            jsonObject.put("isiPad", false);

            return jsonObject;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonObject;
    }


    /**
     * This method call api in handler and check response is successful or failure return result using listener
     */
    public void fire() {
        if (!Utility.isNetworkAvailable(mContext)) {
            try {

                Error error = new Error(AppConstant.INVALID_ID, mContext.getString(R.string.network_connection_unavailable));
                Log.e(TAG, "fire: " + error.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            final JSONObject jsonObject = buildPayload();
            taskQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        Response response = fireAsyncTask(jsonObject);
                        if(response.body() == null) return;

                        if (response.isSuccessful()) {
                            parseResponse(response.body().string(), listener);
                        } else {
                            Error error = Error.createError(response.body().string(),mContext);
                            if(listener == null) return;

                            listener.onFinish(error);
                        }


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Headers getAuthorization(String authorization) {

        return new Headers.Builder()
                .add("Authorization", authorization)
                .build();
    }

    /**
     * This method parse object to success / fail
     * @param arg object response
     * @param listener return data using listener
     */
    private void parseResponse(Object arg, IWebSocketClientEvent listener) {
        try {
            JSONObject jsonObject = new JSONObject(arg.toString());
            int statusCode = 0;

            if(listener == null) return;

            if (jsonObject.has(AppConstant.ResponseCode.STATUS_CODE))
                statusCode = jsonObject.getInt(AppConstant.ResponseCode.STATUS_CODE);

            if (statusCode == AppConstant.ResponseCode.STATUS_SUCCESS) {
                listener.onSuccess(jsonObject);

            }else {
                listener.onFinish(new Error("Something went wrong"));
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }

    }

    /**
     * This method call api using okhttp return response
     * @param jsonObject with require parameter for api
     * @return return call response
     */
    protected Response fireAsyncTask(JSONObject jsonObject) throws IOException {


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
        return client.newCall(request).execute();
    }



}