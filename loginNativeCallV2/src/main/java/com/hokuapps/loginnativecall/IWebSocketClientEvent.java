package com.hokuapps.loginnativecall;

import com.hokuapps.loginnativecall.model.Error;

import org.json.JSONObject;

public interface IWebSocketClientEvent {

    void onSuccess(JSONObject jsonObject);

    void onFailure(Error error);
}
