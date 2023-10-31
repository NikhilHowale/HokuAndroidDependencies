package com.hokuapps.startvideocall.delegate;


import com.hokuapps.startvideocall.model.Error;

import org.json.JSONObject;

public interface IWebSocketClientEvent {

    void onSuccess(JSONObject jsonObject);

    public void onFinish(Error error);

}
