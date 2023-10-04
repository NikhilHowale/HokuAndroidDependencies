package com.hokuapps.loadmapviewbyconfig.synchronizer;

import android.content.Context;
import android.os.Looper;

import com.hokuapps.loadmapviewbyconfig.delegate.IWebSocketClientEvent;

import org.json.JSONObject;

public class UpdateCurrentLocationClientEvent extends WebSocketClientEvent{
    private Context mContext;
    private double latitude;
    private double longitude;
    private String apiFullUrl;
    private String status;
    private boolean sendPastLocations = false;
    private JSONObject jsonObject;

    public UpdateCurrentLocationClientEvent(Context context, double latitude, double longitude, String apiFullUrl, String status, boolean sendPastLocations, JSONObject jsonObject) {

        this.mContext = context;
        this.latitude = latitude;
        this.longitude = longitude;
        this.apiFullUrl = apiFullUrl;
        this.status = status;
        this.sendPastLocations = sendPastLocations;
        this.jsonObject = jsonObject;
    }


    /**
     * Call api for current location
     * @param context
     * @param latitude
     * @param longitude
     * @param apiFullUrl
     * @param status
     * @param iWebSocketClientEvent
     * @param sendPastLocations
     * @param jsonObject
     */
    public static void callUpdateCurrentLocationAPI(Context context, double latitude, double longitude, String apiFullUrl, String status, IWebSocketClientEvent iWebSocketClientEvent, boolean sendPastLocations, JSONObject jsonObject) {
        UpdateCurrentLocationClientEvent updateCurrentLocationClientEvent =
                new UpdateCurrentLocationClientEvent(context, latitude, longitude, apiFullUrl, status, sendPastLocations, jsonObject);
        updateCurrentLocationClientEvent.setLooper(Looper.getMainLooper());
        updateCurrentLocationClientEvent.setListener(iWebSocketClientEvent);
        updateCurrentLocationClientEvent.fire();
    }


    public void fire() {

    }
}
