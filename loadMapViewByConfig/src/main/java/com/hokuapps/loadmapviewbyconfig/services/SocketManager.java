package com.hokuapps.loadmapviewbyconfig.services;

import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.AUTH_TOKEN;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.BV;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.DEVICE_ID;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.DEVICE_NAME;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.DEVICE_TYPE;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.SECRET_KEY;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.TIMESTAMP;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.hokuapps.loadmapviewbyconfig.R;
import com.hokuapps.loadmapviewbyconfig.constant.MapConstant;
import com.hokuapps.loadmapviewbyconfig.utility.Utility;

import java.util.Objects;

import okhttp3.Headers;

public class SocketManager {

    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_INVALID_NUMBER = 1000;
    public static final int STATUS_INVALID_OTP = 1001;
    public static final int STATUS_OTP_EXPIRED = 1002;
    public static final int STATUS_INVALID_COUNTRY_NAME = 1004;
    public static final int STATUS_INVALID_AUTH_TOKEN = 1008;
    //5xx Procession error
    public static final int STATUS_PROCESSING_ERROR = 500;
    public static final int STATUS_ERROR_SENDING_SMS = 200;
    public static final int STATUS_ERROR_MAKING_CALL = 2001;
    public static final int STATUS_TIMEOUT = 2002;
    //4xx Exception error
    public static final int STATUS_EXCEPTION_ERROR = 400;


    /**
     * Get error message based on status code
     * @param statusCode status code ()
     * @param context
     * @return
     */
    public static String getErrorMessage(int statusCode, Context context) {

        switch (statusCode) {
            case STATUS_INVALID_NUMBER:
                return context.getResources().getString(R.string.status_invalid_phone_number);
            case STATUS_INVALID_COUNTRY_NAME:
                return context.getResources().getString(R.string.status_invalid_country_name);
            case STATUS_INVALID_OTP:
                return context.getResources().getString(R.string.status_invalid_otp_code);
            case STATUS_OTP_EXPIRED:
                return context.getResources().getString(R.string.status_invalid_otp_expired);
            case STATUS_ERROR_SENDING_SMS:
                return context.getResources().getString(R.string.status_error_sending_sms);
            case STATUS_ERROR_MAKING_CALL:
                return context.getResources().getString(R.string.status_error_making_call);
            case STATUS_INVALID_AUTH_TOKEN:
                return context.getResources().getString(R.string.failed_match_otp_code);
            case STATUS_EXCEPTION_ERROR:
                break;
            case STATUS_PROCESSING_ERROR:
                break;
            case STATUS_TIMEOUT:
                return context.getResources().getString(R.string.status_error_connection_timeout);
            default:
                break;
        }

        return null;
    }


    /**
     * Get login headers for Api (Device details)
     * @param context
     * @return
     */
    public static Headers getLoginHeaders(Context context) {
        String deviceName = String.format("%s%s, Android SDK %s", Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK_INT);
        deviceName = deviceName.replaceAll("_", "");
        String deviceId = "`";

        if (TextUtils.isEmpty(deviceId)) {
            deviceId = !Objects.equals(Build.SERIAL, Build.UNKNOWN) ? Build.SERIAL : Utility.getRandomUUID();
        }

        return new Headers.Builder()
                .add(DEVICE_ID, deviceId)
                .add(DEVICE_TYPE, String.valueOf(MapConstant.DEVICE_TYPE))
                .add(DEVICE_NAME, deviceName)
                .add(TIMESTAMP, String.valueOf(Utility.getCurrentDateTimeInMS()))
                .add(BV, Utility.getVersionName(context))
                .build();
    }


    /**
     * returns
     * @return returns header
     */
    public static Headers getRefreshTokenHeaders() {
        return new Headers.Builder()
                .add(SECRET_KEY, MapConstant.AUTH_SECRET_KEY)
                .add(AUTH_TOKEN, AUTH_TOKEN).build();

    }


    /**
     *
     * @param context
     * @return
     */
    public static Headers getTokenAndVersionHeaders(Context context) {

        return new Headers.Builder()
                .add(MapConstant.AuthIO.AUTH_TOKEN.toLowerCase(), AUTH_TOKEN)
                .add("v", String.format("%s-%s", "a", Utility.getVersionName(context))).build();
    }


    /**
     * Interface listens for response (success, error)
     * @param <T>
     */
    public interface DataListener<T> {
        void onSuccess(T obj);

        void onError(T obj);
    }

}
