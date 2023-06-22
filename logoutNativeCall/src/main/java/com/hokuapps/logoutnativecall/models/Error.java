package com.hokuapps.logoutnativecall.models;

import android.content.Context;

import com.hokuapps.logoutnativecall.utils.LogoutUtility;
import com.hokuappslogoutnativecall.R;

import org.json.JSONException;
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

    public static Error createError(Object arg, Context mContext) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(arg.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (jsonObject == null || jsonObject.length() == 0) {
            return null;
        }

        int statusCode = -1;
        String msg = "";
        try {
            statusCode = jsonObject.getInt("statusCode");
            msg = (String) LogoutUtility.getJsonObjectValue(jsonObject, "errorMessage");


            if (msg == null) {
                msg = getErrorMessage(statusCode,mContext);
            }

        } catch (Exception e) {

        }

        return new Error(statusCode, msg);
    }

    private static String getErrorMessage(int statusCode, Context mContext) {
        Context context = mContext;
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
