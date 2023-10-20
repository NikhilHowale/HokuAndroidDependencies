package com.hokuapps.getbackgroundlocationupdates.callbacks;

import android.location.Location;

public interface OneTimeLocationCallback {

    void onNewUpdatedLocation(Location location);
}
