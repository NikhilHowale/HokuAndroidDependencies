/*
 * Copyright (C) The Android Open Source Project
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
package com.hokuapps.Loadnativeqrcodescannerupload.barcodereader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.hokuapps.Loadnativeqrcodescannerupload.R;
import com.hokuapps.Loadnativeqrcodescannerupload.barcodereader.ui.camera.GraphicOverlay;


/**
 * Factory for creating a tracker and associated graphic to be associated with a new barcode.  The
 * multi-processor uses this factory to create barcode trackers as needed -- one for each barcode.
 */
public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private final GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    private final Context mContext;
    private final Bitmap bitmap;

    public BarcodeTrackerFactory(GraphicOverlay<BarcodeGraphic> mGraphicOverlay, Context mContext) {
        this.mGraphicOverlay = mGraphicOverlay;
        this.mContext = mContext;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.scanner);
    }

    @Override
    public Tracker<Barcode> create(@NonNull Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay,bitmap);
        return new BarcodeGraphicTracker(mGraphicOverlay, graphic, mContext);
    }

}

