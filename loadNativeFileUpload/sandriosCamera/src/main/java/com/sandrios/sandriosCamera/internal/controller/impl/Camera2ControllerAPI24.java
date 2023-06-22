package com.sandrios.sandriosCamera.internal.controller.impl;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.configuration.ConfigurationProvider;
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
 * Created by Arpit Gandhi on 7/6/16.
 */
@TargetApi(24)
public class Camera2ControllerAPI24 implements CameraController<String>,
        CameraOpenListener<Integer, SurfaceHolder.Callback>,
        CameraPhotoListener, CameraVideoListener, CameraCloseListener<Integer> {

    private final static String TAG = "Camera2Controller";

    private String currentCameraId;
    private ConfigurationProvider configurationProvider;
    private CameraManager<Integer, SurfaceHolder.Callback> camera2Manager;
    private CameraView cameraView;

    private File outputFile;

    public Camera2ControllerAPI24(CameraView cameraView, ConfigurationProvider configurationProvider) {
        this.cameraView = cameraView;
        this.configurationProvider = configurationProvider;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        camera2Manager = Camera1Manager.getInstance();
        camera2Manager.initializeCameraManager(configurationProvider, cameraView.getActivity());
       // currentCameraId = String.valueOf(camera2Manager.getFaceBackCameraId());

        currentCameraId = new CameraPref(cameraView.getActivity()).getCameraRotation() == CameraSwitchView.CAMERA_TYPE_FRONT
                ?String.valueOf(camera2Manager.getFaceFrontCameraId()) : String.valueOf(camera2Manager.getFaceBackCameraId());


    }

    @Override
    public void onResume() {
        camera2Manager.openCamera(Integer.valueOf(currentCameraId), this);
    }

    @Override
    public void onPause() {
        camera2Manager.closeCamera(null);
        cameraView.releaseCameraPreview();
    }

    @Override
    public void onDestroy() {
        camera2Manager.releaseCameraManager();
    }

    @Override
    public void takePhoto() {
        outputFile = CameraHelper.getOutputMediaFile(cameraView.getActivity(), CameraConfiguration.MEDIA_ACTION_PHOTO);
        camera2Manager.takePhoto(outputFile, this);
    }

    @Override
    public void startVideoRecord() {
        outputFile = CameraHelper.getOutputMediaFile(cameraView.getActivity(), CameraConfiguration.MEDIA_ACTION_VIDEO);
        camera2Manager.startVideoRecord(outputFile, this);
    }

    @Override
    public void stopVideoRecord() {
        camera2Manager.stopVideoRecord();
    }

    @Override
    public boolean isVideoRecording() {
        return camera2Manager.isVideoRecording();
    }

    @Override
    public void switchCamera(final @CameraConfiguration.CameraFace int cameraFace) {
        currentCameraId = String.valueOf(camera2Manager.getCurrentCameraId().equals(camera2Manager.getFaceFrontCameraId()) ?
                camera2Manager.getFaceBackCameraId() : camera2Manager.getFaceFrontCameraId());

        camera2Manager.closeCamera(this);
    }

    @Override
    public void setFlashMode(@CameraConfiguration.FlashMode int flashMode) {
        camera2Manager.setFlashMode(flashMode);
    }

    @Override
    public void switchQuality() {
        camera2Manager.closeCamera(this);
    }

    @Override
    public int getNumberOfCameras() {
        return camera2Manager.getNumberOfCameras();
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
    public String getCurrentCameraId() {
        return currentCameraId;
    }

    @Override
    public void onCameraOpened(Integer openedCameraId, Size previewSize, SurfaceHolder.Callback surfaceTextureListener) {
        cameraView.updateUiForMediaAction(CameraConfiguration.MEDIA_ACTION_BOTH);
        AutoFitSurfaceView autoFitSurfaceView = new AutoFitSurfaceView(cameraView.getActivity(), surfaceTextureListener);
        autoFitSurfaceView.setOnTouchListener(onTouchListener);
        cameraView.updateCameraPreview(previewSize, autoFitSurfaceView);
        cameraView.updateCameraSwitcher(camera2Manager.getNumberOfCameras());
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

                cameraView.onZoomInOrOut(zoom);

            }
        } else if (newDist < dist) {
            //zoom out
            if (zoom > 0) {
                zoom = zoom - 2;
                if (zoom < 0)
                    zoom = 0;
                cameraView.onZoomInOrOut(zoom);
            }
        }
        dist = newDist;

        params.setZoom(zoom);
        camera2Manager.getCamera().setParameters(params);
    }

    public void handleFocus(Camera.Parameters params) {
        /*int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);*/

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            camera2Manager.getCamera().autoFocus(new Camera.AutoFocusCallback() {
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
            Camera.Parameters params = camera2Manager.getCamera().getParameters();
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

        camera2Manager.openCamera(Integer.valueOf(currentCameraId), this);
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
        return camera2Manager;
    }
}
