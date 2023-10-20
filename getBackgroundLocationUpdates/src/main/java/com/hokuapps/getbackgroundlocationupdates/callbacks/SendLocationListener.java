package com.hokuapps.getbackgroundlocationupdates.callbacks;

import org.json.JSONObject;

import okhttp3.Response;

public interface SendLocationListener {

    void onSuccess(JSONObject jsonObject);

    void onFailure(Response response);
}
