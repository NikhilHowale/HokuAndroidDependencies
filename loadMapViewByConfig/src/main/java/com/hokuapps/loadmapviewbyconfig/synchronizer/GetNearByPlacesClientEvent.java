package com.hokuapps.loadmapviewbyconfig.synchronizer;

import static com.hokuapps.loadmapviewbyconfig.services.SocketManager.*;
import static com.hokuapps.loadmapviewbyconfig.services.SocketManager.STATUS_TIMEOUT;
import static com.hokuapps.loadmapviewbyconfig.services.SocketManager.getErrorMessage;
import static com.hokuapps.loadmapviewbyconfig.services.SocketManager.getLoginHeaders;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hokuapps.loadmapviewbyconfig.R;
import com.hokuapps.loadmapviewbyconfig.backgroundTask.DispatchQueue;
import com.hokuapps.loadmapviewbyconfig.constant.MapConstant;
import com.hokuapps.loadmapviewbyconfig.models.Error;
import com.hokuapps.loadmapviewbyconfig.socketio.SocketWatcher;
import com.hokuapps.loadmapviewbyconfig.utility.Utility;

import org.json.JSONObject;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetNearByPlacesClientEvent extends WebSocketClientEvent implements DataListener<JSONObject> {
    private static final String TAG = "GetNearByPlacesClientEv";
    private final Context mContext;

    private final String apiFullUrl;
    private JSONObject requestJson;
    private JSONObject mapRefreshJson;
    private JSONObject jsonObjectOtherRequestData;
    public static volatile DispatchQueue taskQueue = new DispatchQueue("taskQueue");

    public GetNearByPlacesClientEvent(Context context, String apiFullUrl) {

        this.mContext = context;
        this.apiFullUrl = apiFullUrl;
        requestJson = new JSONObject();
    }

    public void setRequestJson(JSONObject requestJson) {
        this.requestJson = requestJson;
    }

    public void setOtherRequestData(JSONObject jsonObjectOtherRequestData) {
        this.jsonObjectOtherRequestData = jsonObjectOtherRequestData;

    }

    public void setMapRefreshJson(JSONObject mapRefreshJson) {
        this.mapRefreshJson = mapRefreshJson;
    }


    public void fire() {
        try {
            final JSONObject jsonObject = new JSONObject();
            taskQueue.postRunnable(() -> fireAsyncTask("getEventName()", jsonObject, GetNearByPlacesClientEvent.this));

        } catch (Exception e) {
            Log.e(TAG, "fire: " +e.getMessage());

        }
    }

    @Override
    public void onSuccess(JSONObject obj) {
        try {

            if (looper != null) {
                new Handler(looper).post(() -> listener.onFinish(null, obj, GetNearByPlacesClientEvent.this));
            } else {
                listener.onFinish(null, obj, GetNearByPlacesClientEvent.this);
            }

        } catch (Exception e) {
            // instead of again handling error here we passing control to error callback
            Error error = new Error(MapConstant.INVALID_ID, e.getMessage());
            onError(error.createJSONObject());

        }
    }

    @Override
    public void onError(JSONObject obj) {

        final Error error = Error.createError(obj);

        if (looper != null) {
            new Handler(Looper.getMainLooper()).post(() -> listener.onFinish(error, null, GetNearByPlacesClientEvent.this));
        } else {
            listener.onFinish(error, null, GetNearByPlacesClientEvent.this);
        }
    }

    /**
     *
     * @param event
     * @param jsonObject
     * @param listener
     */
    protected void fireAsyncTask(final String event, final JSONObject jsonObject, final DataListener<JSONObject> listener) {

        try {
            Response response = fireAsyncTask(event, jsonObject);
            Log.e("Event =>", "Event =>" + event + "Request Obj =>" + jsonObject);
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    Utility.parseResponse(response.body().string(), listener);
                }
            } else {
                Error error = new Error(MapConstant.INVALID_ID, mContext.getResources().getString(R.string.err_occurred_msg));
                listener.onError(error.createJSONObject());
            }

        } catch (IOException ex) {

            ex.printStackTrace();

            if (listener != null) {
                Error error = new Error(MapConstant.INVALID_ID, getErrorMessage(STATUS_TIMEOUT, mContext));
                listener.onError(error.createJSONObject());
            }
        } catch (Exception ex) {

            ex.printStackTrace();
        }

        SocketWatcher.getInstance().updateLastActiveTime(System.currentTimeMillis());
    }


    /**
     *
     * @param event
     * @param jsonObject
     * @return return the response got from api call
     * @throws IOException
     */
    protected Response fireAsyncTask(final String event, final JSONObject jsonObject) throws IOException {

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();
        String url = apiFullUrl;

        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);

        //add headers for login or sign up api
        if (event.equalsIgnoreCase(MapConstant.AuthorizationParams.EVENT_LOGIN)
                || event.equalsIgnoreCase(MapConstant.AuthorizationParams.EVENT_UPDATE_PROFILE_V2)
                || event.equalsIgnoreCase(MapConstant.ChatIO.EVENT_REGISTER_APP_TOKEN)) {
            requestBuilder.headers(getLoginHeaders(mContext));
            Log.e("HEADER =>", "HEADER =>" + getLoginHeaders(mContext));
        } else if (event.equalsIgnoreCase(MapConstant.AuthorizationParams.EVENT_REFRESH_TOKEN_V2)) {
            requestBuilder.headers(getRefreshTokenHeaders());
            Log.e("HEADER =>", "HEADER =>" + getRefreshTokenHeaders());
        } else {
            requestBuilder.headers(getTokenAndVersionHeaders(mContext));
            Log.e("HEADER =>", "HEADER =>" + getTokenAndVersionHeaders(mContext));
        }

        requestBuilder.header("gzip", "Accept-Encoding");

        Request request = requestBuilder.build();
        Response response = client.newCall(request).execute();

        Log.e("URL =>", "URL =>" + url);
        return response;
    }
}
