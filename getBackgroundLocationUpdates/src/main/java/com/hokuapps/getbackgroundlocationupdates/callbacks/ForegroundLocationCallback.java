package com.hokuapps.getbackgroundlocationupdates.callbacks;

import android.location.Location;

public interface ForegroundLocationCallback {

    void onNewUpdatedLocation(Location location);
}
