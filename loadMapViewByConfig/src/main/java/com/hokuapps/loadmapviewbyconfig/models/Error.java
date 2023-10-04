package com.hokuapps.loadmapviewbyconfig.models;

import android.content.Context;


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
        //token
        int INVALID_TOKEN = 1101;
        int INVALID_SECRET_KEY = 1100;

        //Client Error
        int BAD_REQUEST = 400;
        int REQUEST_TIMEOUT = 408;
        int NOT_FOUND = 404;
        int SOCKET_READ_FAILED = 23;

        //Server Error
        int INTERNAL_SERVER_ERROR = 500;
        int BAD_GATEWAY = 502;
        int SERVICE_UNAVAILABLE = 503;
        int GATEWAY_TIMEOUT = 504;

        //local error
        int NO_INTERNET = 2001;
        int UNKNOWN_ERROR = 520;

        //Cancel from user
        int CANCEL_BY_USER = 2003;
    }

    /**
     * Initialize object
     *
     * @param code
     */
    public Error(int code) {
        this.code = code;
    }

    /**
     * Initialize object
     *
     * @param errMsg
     */
    public Error(String errMsg) {
        this.msg = errMsg;
    }

    /**
     * Initialize object
     *
     * @param code
     * @param msg
     */
    public Error(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }




    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("statusCode:").append(code).append(", errMsg:").append(msg);
        return builder.toString();
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
        } catch (Exception e) {
//            LogUtility.printErrorMessage(Error.class.getSimpleName(), e);
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

        }

        return new Error(statusCode, msg);
    }

    private static String getErrorMessage(int statusCode) {
        Context context = App.getInstance().getApplicationContext();
        switch (statusCode) {
            case ErrorCode.BAD_REQUEST:
            case ErrorCode.BAD_GATEWAY:
                return context.getResources().getString(R.string.err_occurred_msg);
            case ErrorCode.NOT_FOUND:
                return context.getResources().getString(R.string.err_occurred_msg);
            case ErrorCode.REQUEST_TIMEOUT:
            case ErrorCode.GATEWAY_TIMEOUT:
                return context.getResources().getString(R.string.timeout_error_message);
            case ErrorCode.INTERNAL_SERVER_ERROR:
            case ErrorCode.SERVICE_UNAVAILABLE:
                return context.getResources().getString(R.string.err_occurred_msg);

        }
        return null;
    }
}
