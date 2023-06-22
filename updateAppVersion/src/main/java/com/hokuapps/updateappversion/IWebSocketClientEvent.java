package com.hokuapps.updateappversion;

import org.json.JSONObject;

public interface IWebSocketClientEvent {

    void onSuccess(JSONObject jsonObject);

    void onFailure(Error error);
}
