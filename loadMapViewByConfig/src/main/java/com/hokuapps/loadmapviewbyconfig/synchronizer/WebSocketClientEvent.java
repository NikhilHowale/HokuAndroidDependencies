package com.hokuapps.loadmapviewbyconfig.synchronizer;

import android.os.Looper;

import com.hokuapps.loadmapviewbyconfig.delegate.IWebSocketClientEvent;

public abstract class WebSocketClientEvent {
    IWebSocketClientEvent listener;

    // to differentiate callback should initiate on UI thread or some other thread
    protected Looper looper;

    public void setLooper(Looper looper) {
        this.looper = looper;
    }

    public void setListener(IWebSocketClientEvent listener) {
        this.listener = listener;
    }


}
