package com.hokuapps.loadmapviewbyconfig.socketio;


public class SocketWatcher {

    private long lastActiveTime;
    static SocketWatcher socketWatcher;

    public static SocketWatcher getInstance() {
        if (socketWatcher == null) {
            socketWatcher = new SocketWatcher();
            socketWatcher.lastActiveTime = System.currentTimeMillis();
        }
        return socketWatcher;
    }


    /**
     * Updates to last active time
     * @param time
     */
    public void updateLastActiveTime(long time) {
        lastActiveTime = time;
    }


}
