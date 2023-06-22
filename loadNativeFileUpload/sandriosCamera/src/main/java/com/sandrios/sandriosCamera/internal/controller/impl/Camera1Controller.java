package com.sandrios.sandriosCamera.internal.controller.impl;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.sandrios.sandriosCamera.internal.configuration.ConfigurationProvider;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.controller.CameraController;
import com.sandrios.sandriosCamera.internal.controller.view.CameraView;
import com.sandrios.sandriosCamera.internal.manager.CameraManager;
import com.sandrios.sandriosCamera.internal.manager.impl.Camera1Manager;
import com.sandrios.sandriosCamera.internal.manager.listener.CameraCloseListener;
import com.sandrios.sandriosCamera.internal.manager.listener.CameraOpenListener;
import com.sandrios.sandriosCamera.internal.manager.listener.CameraPhotoListener;
import com.sandrios.sandriosCamera.internal.manager.listener.CameraVideoListener;
import com.sandrios.sandriosCamera.internal.pref.CameraPref;
import com.sandrios.sandriosCamera.internal.ui.view.AutoFitSurfaceView;
import com.sandrios.sandriosCamera.internal.ui.view.CameraSwitchView;
import com.sandrios.sandriosCamera.internal.utils.CameraHelper;
import com.sandrios.sandriosCamera.internal.utils.Size;

import java.io.File;
import java.util.List;

/**
 * Created by Arpit Gandhi on 7/7/16.
 */

@SuppressWarnings("deprecation")
public class Camera1Controller implements CameraController<Integer>,
        CameraOpenListener<Integer, SurfaceHolder.Callback>, CameraPhotoListener, CameraCloseListener<Integer>, CameraVideoListener {

    private final static String TAG = "Camera1Controller";

    private Integer currentCameraId;
    private ConfigurationProvider configurationProvider;
    private CameraManager<Integer, SurfaceHolder.Callback> cameraManager;
    private CameraView cameraView;

    private File outputFile;

    public Camera1Controller(CameraView cameraView, ConfigurationProvider configurationProvider) {
        this.cameraView = cameraView;
        this.configurationProvider = configurationProvider;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        cameraManager = Camera1Manager.getInstance();
        cameraManager.initializeCameraManager(configurationProvider, cameraView.getActivity());
//        currentCameraId = cameraManager.getFaceBackCameraId();

        currentCameraId = new CameraPref(cameraView.getActivity()).getCameraRotation() == CameraSwitchView.CAMERA_TYPE_FRONT
                ? cameraManager.getFaceFrontCameraId() : cameraManager.getFaceBackCameraId();

    }

    @Override
    public void onResume() {
        cameraManager.openCamera(currentCameraId, this);

    }

    @Override
    public void onPause() {
        cameraManager.closeCamera(null);
    }

    @Override
    public void onDestroy() {
        cameraManager.releaseCameraManager();
    }

    @Override
    public void takePhoto() {
        outputFile = CameraHelper.getOutputMediaFile(cameraView.getActivity(), CameraConfiguration.MEDIA_ACTION_PHOTO);
        cameraManager.takePhoto(outputFile, this);
    }

    @Override
    public void startVideoRecord() {
        outputFile = CameraHelper.getOutputMediaFile(cameraView.getActivity(), CameraConfiguration.MEDIA_ACTION_VIDEO);
        cameraManager.startVideoRecord(outputFile, this);
    }

    @Override
    public void setFlashMode(@CameraConfiguration.FlashMode int flashMode) {
        cameraManager.setFlashMode(flashMode);
    }

    @Override
    public void stopVideoRecord() {
        cameraManager.stopVideoRecord();
    }

    @Override
    public boolean isVideoRecording() {
        return cameraManager.isVideoRecording();
    }

    @Override
    public void switchCamera(@CameraConfiguration.CameraFace final int cameraFace) {
        currentCameraId = cameraManager.getCurrentCameraId().equals(cameraManager.getFaceFrontCameraId()) ?
                cameraManager.getFaceBackCameraId() : cameraManager.getFaceFrontCameraId();

        cameraManager.closeCamera(this);
    }

    @Override
    public void switchQuality() {
        cameraManager.closeCamera(this);
    }

    @Override
    public int getNumberOfCameras() {
        return cameraManager.getNumberOfCameras();
    }

    @Override
    public int getMediaAction() {
        return configurationProvider.getMediaAction();
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public Integer getCurrentCameraId() {
        return currentCameraId;
    }


    @Override
    public void onCameraOpened(Integer cameraId, Size previewSize, SurfaceHolder.Callback surfaceCallback) {
        cameraView.updateUiForMediaAction(configurationProvider.getMediaAction());
        AutoFitSurfaceView autoFitSurfaceView = new AutoFitSurfaceView(cameraView.getActivity(), surfaceCallback);
        autoFitSurfaceView.setOnTouchListener(onTouchListener);
        cameraView.updateCameraPreview(previewSize, autoFitSurfaceView);
        cameraView.updateCameraSwitcher(getNumberOfCameras());
    }

    /**
     * Determine the space between the first two fingers
     */
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > dist) {
            //zoom in
            if (zoom < maxZoom) {
                zoom = zoom + 2;
                if (zoom > maxZoom)
                    zoom = maxZoom;

            }
        } else if (newDist < dist) {
            //zoom out
            if (zoom > 0) {
                zoom = zoom - 2;
                if (zoom < 0)
                    zoom = 0;
            }
        }
        dist = newDist;
        cameraView.onZoomInOrOut(zoom);
        params.setZoom(zoom);
        cameraManager.getCamera().setParameters(params);
    }

    public void handleFocus(Camera.Parameters params) {
        /*int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);*/

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            cameraManager.getCamera().autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
                    // currently set to auto-focus on single touch
                }
            });

        }
    }

    private float dist;
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // Get the pointer ID
            Camera.Parameters params = cameraManager.getCamera().getParameters();
            int action = event.getAction();

            if (event.getPointerCount() > 1) {
                // handle multi-touch events
                if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    dist = getFingerSpacing(event);
                } else if (action == MotionEvent.ACTION_MOVE) {
                    handleZoom(event, params);
                } else {
                    handleFocus(params);
                }
            } else {
                // handle single touch events
                if (action == MotionEvent.ACTION_UP) {
                    handleFocus(params);
                }
            }
            return true;
        }
    };

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(Integer closedCameraId) {
        cameraView.releaseCameraPreview();

        cameraManager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPhotoTaken(File photoFile) {
        cameraView.onPhotoTaken();
    }

    @Override
    public void onPhotoTakeError() {
    }

    @Override
    public void onVideoRecordStarted(Size videoSize) {
        cameraView.onVideoRecordStart(videoSize.getWidth(), videoSize.getHeight());
    }

    @Override
    public void onVideoRecordStopped(File videoFile) {
        cameraView.onVideoRecordStop();
    }

    @Override
    public void onVideoRecordError() {

    }

    @Override
    public CameraManager getCameraManager() {
        return cameraManager;
    }
}
