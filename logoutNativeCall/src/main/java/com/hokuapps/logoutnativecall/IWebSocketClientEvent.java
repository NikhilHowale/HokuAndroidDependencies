package com.hokuapps.logoutnativecall;

import com.hokuapps.logoutnativecall.models.Error;

import org.json.JSONObject;

public interface IWebSocketClientEvent {

    void onSuccess(JSONObject jsonObject);

    void onFailure(Error error);
}
