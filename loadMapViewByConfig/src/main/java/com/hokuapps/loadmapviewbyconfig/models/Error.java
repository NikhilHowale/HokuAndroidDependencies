package com.hokuapps.loadmapviewbyconfig.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hokuapps.loadmapviewbyconfig.App;
import com.hokuapps.loadmapviewbyconfig.R;
import com.hokuapps.loadmapviewbyconfig.utility.Utility;

import org.json.JSONObject;

/**
 * Class to contains details about program error/exception
 */
public class Error {

    // class member
    private int code;
    private String msg;
    private Object object;

    public interface ErrorCode {

        //Client Error
        int BAD_REQUEST = 400;
        int REQUEST_TIMEOUT = 408;
        int NOT_FOUND = 404;

        //Server Error
        int INTERNAL_SERVER_ERROR = 500;
        int BAD_GATEWAY = 502;
        int SERVICE_UNAVAILABLE = 503;
        int GATEWAY_TIMEOUT = 504;

    }

    /**
     * Initialize object with code
     */
    public Error(int code) {
        this.code = code;
    }

    /**
     * Initialize object with message
     */
    public Error(String errMsg) {
        this.msg = errMsg;
    }

    /**
     * Initialize object
     *
     * @param code code
     * @param msg error message
     */
    public Error(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    @NonNull
    @Override
    public String toString() {
        return "statusCode:" + code + ", errMsg:" + msg;
    }

    /**
     * Construct JSON object for current error object
     *
     * @return {@link JSONObject}
     */
    public JSONObject createJSONObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("statusCode", code);
            jsonObject.put("errorMessage", Utility.isEmpty(msg) ? "" : msg);
        } catch (Exception ignored) {
        }

        return jsonObject;
    }

    public static Error createError(JSONObject jsonObject) {
        if (jsonObject == null || jsonObject.length() == 0) {
            return null;
        }

        int statusCode = -1;
        String msg = "";
        try {
            statusCode = jsonObject.getInt("statusCode");
            msg = (String) Utility.getJsonObjectValue(jsonObject, "errorMessage");


            if (msg == null) {
                msg = getErrorMessage(statusCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Error(statusCode, msg);
    }

    private static String getErrorMessage(int statusCode) {
        Context context = App.getInstance().getApplicationContext();
        switch (statusCode) {
            case ErrorCode.BAD_REQUEST:
            case ErrorCode.BAD_GATEWAY:
            case ErrorCode.NOT_FOUND:
            case ErrorCode.INTERNAL_SERVER_ERROR:
            case ErrorCode.SERVICE_UNAVAILABLE:
                return context.getResources().getString(R.string.err_occurred_msg);
            case ErrorCode.REQUEST_TIMEOUT:
            case ErrorCode.GATEWAY_TIMEOUT:
                return context.getResources().getString(R.string.timeout_error_message);

        }
        return null;
    }
}
