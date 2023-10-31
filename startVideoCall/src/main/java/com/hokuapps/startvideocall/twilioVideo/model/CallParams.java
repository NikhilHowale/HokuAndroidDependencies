package com.hokuapps.startvideocall.twilioVideo.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CallParams implements Parcelable {

    private String roomName;

    private boolean isVideo = true;
    private String headerColor;

    private String notificationEndPoint;
    private String accessToken;
    private String tokenUrl;
    private String callerName;
    private String callerProfileImage;
    private String callUniqueId;

    private String callRejectUrl;

    private boolean isIncomingCall;
    private boolean isCallFromNotification;

    public CallParams() {
    }

    public String getCallRejectUrl() {
        return callRejectUrl;
    }

    public void setCallRejectUrl(String callRejectUrl) {
        this.callRejectUrl = callRejectUrl;
    }
    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }


    public String getHeaderColor() {
        return headerColor;
    }

    public void setHeaderColor(String headerColor) {
        this.headerColor = headerColor;
    }

    public String getNotificationEndPoint() {
        return notificationEndPoint;
    }

    public void setNotificationEndPoint(String notificationEndPoint) {
        this.notificationEndPoint = notificationEndPoint;
    }

    public String getCallUniqueId() {
        return callUniqueId;
    }

    public void setCallUniqueId(String callUniqueId) {
        this.callUniqueId = callUniqueId;
    }

    public boolean isIncomingCall() {
        return isIncomingCall;
    }

    public void setIncomingCall(boolean incomingCall) {
        isIncomingCall = incomingCall;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getCallerProfileImage() {
        return callerProfileImage;
    }

    public void setCallerProfileImage(String callerProfileImage) {
        this.callerProfileImage = callerProfileImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isCallFromNotification() {
        return isCallFromNotification;
    }

    public void setCallFromNotification(boolean callFromNotification) {
        isCallFromNotification = callFromNotification;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(roomName);
        parcel.writeInt(isVideo ? 1 : 0);
        parcel.writeString(headerColor);
        parcel.writeString(notificationEndPoint);
        parcel.writeInt(isIncomingCall ? 1 : 0);
        parcel.writeInt(isCallFromNotification ? 1 : 0);
        parcel.writeString(accessToken);
        parcel.writeString(tokenUrl);
        parcel.writeString(callerName);
        parcel.writeString(callerProfileImage);
        parcel.writeString(callUniqueId);

    }

    public static final Creator<CallParams> CREATOR = new Creator<CallParams>() {
        public CallParams createFromParcel(Parcel source) {
            CallParams callParams = new CallParams();
            callParams.roomName = source.readString();

            callParams.isVideo = source.readInt() == 1;

            callParams.headerColor = source.readString();
            callParams.notificationEndPoint = source.readString();
            callParams.isIncomingCall = source.readInt() == 1;
            callParams.isCallFromNotification = source.readInt() == 1;
            callParams.accessToken = source.readString();
            callParams.tokenUrl = source.readString();

            callParams.callerName = source.readString();
            callParams.callerProfileImage = source.readString();
            callParams.callUniqueId = source.readString();
            return callParams;
        }

        public CallParams[] newArray(int size) {
            return new CallParams[size];
        }
    };


}
