/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hokuapps.loadmapviewbyconfig.locationProvider;

import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.API_NAME;
import static com.hokuapps.loadmapviewbyconfig.constant.MapConstant.Keys.STATUS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.hokuapps.loadmapviewbyconfig.synchronizer.UpdateCurrentLocationClientEvent;
import com.hokuapps.loadmapviewbyconfig.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Receiver for handling location updates.
 * <p>
 * For apps targeting API level O
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)} should be used when
 * requesting location updates. Due to limits on background services,
 * {@link android.app.PendingIntent#getService(Context, int, Intent, int)} should not be used.
 * <p>
 * Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 * less frequently than the interval specified in the
 * {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 * foreground.
 */
public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";

    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final String requestData = intent.getStringExtra("REQUEST_DATA");

            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();

                    Log.i(TAG, LocationResultHelper.getSavedLocationResult(context));


                    if (locations.size() > 0) {
                        parseJsonResponseRequestData(context, requestData, locations.get(0));
                    }
                }
            }
        }
    }


    /**
     * Parse request data into jsonObject
     * @param context Application context
     * @param requestData request data
     * @param location location object
     */
    private void parseJsonResponseRequestData(final Context context, String requestData, final Location location) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(requestData);

            String updateLocationApi = Utility.getStringObjectValue(jsonObject, API_NAME);
            String status = Utility.getStringObjectValue(jsonObject, STATUS);

            UpdateCurrentLocationClientEvent.callUpdateCurrentLocationAPI(context,
                    location.getLatitude(), location.getLongitude(),
                    updateLocationApi, status, (error, process, socketClientEvent) -> {
                        if (error != null) {

                            return;
                        }
                        if (process != null) {
                            Toast.makeText(context, "Location CLEARED: " + location.getLatitude() + ", Longitude: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                        }
                    }, false,jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
