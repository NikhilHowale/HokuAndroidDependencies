package com.hokuapps.loadmapviewbyconfig.delegate;

import com.hokuapps.loadmapviewbyconfig.models.Error;
import com.hokuapps.loadmapviewbyconfig.synchronizer.WebSocketClientEvent;


public interface IWebSocketClientEvent {

     void onFinish(Error error, Object process, WebSocketClientEvent socketClientEvent);

}
