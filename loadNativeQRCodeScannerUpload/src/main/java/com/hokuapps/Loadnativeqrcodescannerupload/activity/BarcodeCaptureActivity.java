package com.hokuapps.Loadnativeqrcodescannerupload.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.snackbar.Snackbar;
import com.hokuapps.Loadnativeqrcodescannerupload.R;
import com.hokuapps.Loadnativeqrcodescannerupload.barcodereader.BarcodeGraphic;
import com.hokuapps.Loadnativeqrcodescannerupload.barcodereader.BarcodeGraphicTracker;
import com.hokuapps.Loadnativeqrcodescannerupload.barcodereader.BarcodeTrackerFactory;
import com.hokuapps.Loadnativeqrcodescannerupload.barcodereader.ui.camera.CameraSource;
import com.hokuapps.Loadnativeqrcodescannerupload.barcodereader.ui.camera.CameraSourcePreview;
import com.hokuapps.Loadnativeqrcodescannerupload.barcodereader.ui.camera.GraphicOverlay;
import com.hokuapps.Loadnativeqrcodescannerupload.barcodereader.ui.camera.ScannerOverlay;
import com.hokuapps.Loadnativeqrcodescannerupload.utils.AppConstant;
import com.hokuapps.Loadnativeqrcodescannerupload.utils.BarcodeConstant;
import com.hokuapps.Loadnativeqrcodescannerupload.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeCaptureActivity extends AppCompatActivity  implements BarcodeGraphicTracker.BarcodeUpdateListener{

    private static final String TAG = "BarcodeCaptureActivity";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    // helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private ScannerOverlay scannerOverlay;
    private WebView headerWebview;
    private String reqData = "";
    //    private Toolbar mToolbar;
    private JSONObject mRequestData;
    private ImageView imgBack;
    private boolean isShowOverlay = true;

    private TextView descriptionTv;
    private Button btnDownload;
    private EditText edtText;
    private TextView textViewDone, imageManualEntry;
    private Set<String> barcodes;
    private boolean isContinueScanning = false;
    private boolean isManualEntry = false;
    private static String BASE_URL = "";
    private ToneGenerator toneGen1;


    private final CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == R.id.use_flash) {
                mCameraSource.setFlashMode(isChecked ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            }
        }
    };

    /**
     * This method launch barcode scan activity
     * @param activity activity
     * @param isAutoFocus flag for auto focus
     * @param isUseFlash flag for set flash on otherwise turn off
     * @param reqJson json string with extra data
     */
    public static void launchBarcodeReaderActivity(Activity activity, boolean isAutoFocus, boolean isUseFlash, String reqJson) {
        // launch barcode activity.
        Intent intent = new Intent(activity, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeConstant.IntentExtras.AUTO_FOCUS, isAutoFocus);
        intent.putExtra(BarcodeConstant.IntentExtras.USE_FLASH, isUseFlash);
        intent.putExtra(BarcodeConstant.IntentExtras.REQUEST_JSON_OBJ_STR, reqJson);

        activity.startActivityForResult(intent, BarcodeConstant.RequestCode.RC_BARCODE_CAPTURE);
    }

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.barcode_capture);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPreview = findViewById(R.id.preview);
        Utility.hideKeyboard(this);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);

        configureLayoutVisibility();

        // read parameters from the intent used to launch the activity.
        boolean autoFocus = getIntent().getBooleanExtra(BarcodeConstant.IntentExtras.AUTO_FOCUS, false);
        boolean useFlash = getIntent().getBooleanExtra(BarcodeConstant.IntentExtras.USE_FLASH, false);
        reqData = getIntent().getStringExtra(BarcodeConstant.IntentExtras.REQUEST_JSON_OBJ_STR);
        bindJsonObjFromReq();
        initView();
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            //Focusing
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        ((CompoundButton) findViewById(R.id.use_flash)).setOnCheckedChangeListener(onCheckedChangeListener);

        setupToolbar();

        barcodes = new HashSet<>();
    }

    /**
     * Set up the mToolbar component
     */
    private void setupToolbar() {
        String mTitle = Utility.getStringObjectValue(mRequestData, "title");
        descriptionTv.setText(TextUtils.isEmpty(mTitle) ? "Scan QR" : mTitle);

    }

    private void configureLayoutVisibility() {
        scannerOverlay = findViewById(R.id.scanner_overlay);
        headerWebview = findViewById(R.id.header_webview);
        scannerOverlay.setVisibility(isShowOverlay ? View.GONE : View.GONE);
        headerWebview.setVisibility(View.GONE);

        //load header web view
        setWebView();
        loadUrlHeaderPage("scanqr_5b6ea2241803f21165b8b7b1.html");
    }

    private void bindJsonObjFromReq() {
        try {
            mRequestData = new JSONObject(reqData);
        } catch (Exception ex) {
            mRequestData = new JSONObject();
            ex.printStackTrace();
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snack bar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, BarcodeConstant.RequestCode.RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        BarcodeConstant.RequestCode.RC_HANDLE_CAMERA_PERM);
            }
        };

        findViewById(R.id.topLayout).setOnClickListener(listener);
        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    private void initView() {

        String description = Utility.getStringObjectValue(mRequestData, "description");
        isContinueScanning = Utility.getJsonObjectBooleanValue(mRequestData,"isContinueScanning");
        BASE_URL = Utility.getStringObjectValue(mRequestData,"scanApi");


        imgBack = findViewById(R.id.imgBack);
        descriptionTv = findViewById(R.id.description_tv);
        descriptionTv.setText("Scan QR");
        textViewDone = findViewById(R.id.textDone);
        imageManualEntry = findViewById(R.id.imageManualEntry);

        if(!isContinueScanning){
            textViewDone.setVisibility(View.GONE);
            imageManualEntry.setVisibility(View.GONE);
            scannerOverlay.setVisibility(View.VISIBLE);
            mGraphicOverlay.setVisibility(View.GONE);
        }else {
            scannerOverlay.setVisibility(View.GONE);
            mGraphicOverlay.setVisibility(View.VISIBLE);

        }

        btnDownload = findViewById(R.id.btnDownload);
        edtText = findViewById(R.id.edtText);

        btnDownload.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra(BarcodeConstant.IntentExtras.BARCODE_OBJECT, edtText.getText().toString().trim());
            data.putExtra(BarcodeConstant.IntentExtras.REQUEST_JSON_OBJ_STR, edtText.getText().toString().trim());
            setResult(CommonStatusCodes.SUCCESS, data);
            finish();
        });

        imgBack.setVisibility(View.VISIBLE);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        textViewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcodes != null) {
                    Intent data = new Intent();
                    ArrayList<String> barcodeArrayList = new ArrayList<>(barcodes);
                    data.putExtra(BarcodeConstant.IntentExtras.MULTIPLE_BARCODE_OBJECT, barcodeArrayList);
                    data.putExtra(BarcodeConstant.IntentExtras.REQUEST_JSON_OBJ_STR, reqData);
                    setResult(CommonStatusCodes.SUCCESS, data);
                    finish();
                }
            }
        });

        imageManualEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isManualEntry = true;
                openManualDialog();
            }
        });
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, this);
        barcodeDetector.setProcessor( new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
            }
        }


        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();


        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(screenWidth, screenHeight)
                .setRequestedFps(15.0f);

        // make sure that auto focus is an available option
        builder = builder.setFocusMode( autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);

        mCameraSource = builder.setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null).build();

    }

    @Override
    public void onBarcodeDetected(final Barcode barcode) {
        //do something with barcode data returned

        runOnUiThread(() -> {
            if (barcode != null) {
                if(isManualEntry){
                    return;
                }

                if(isContinueScanning) {
                    if(!(barcodes.contains(barcode.displayValue))) {

                        Utility.vibrateDevice(BarcodeCaptureActivity.this, 800);
                        toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
                        checkBarcodeValidity(barcode.displayValue);

                    } else {
                        Toast.makeText(BarcodeCaptureActivity.this, "Parcel " + barcode.displayValue +" scanned before", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Intent data = new Intent();
                    data.putExtra(BarcodeConstant.IntentExtras.BARCODE_OBJECT, barcode);
                    data.putExtra(BarcodeConstant.IntentExtras.REQUEST_JSON_OBJ_STR, reqData);
                    setResult(CommonStatusCodes.SUCCESS, data);
                    finish();
                }

            }
        });

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }
    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, BarcodeConstant.RequestCode.RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private static class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            //mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != BarcodeConstant.RequestCode.RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // we have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(BarcodeConstant.IntentExtras.AUTO_FOCUS, false);
            boolean useFlash = getIntent().getBooleanExtra(BarcodeConstant.IntentExtras.USE_FLASH, false);
            reqData = getIntent().getStringExtra(BarcodeConstant.IntentExtras.REQUEST_JSON_OBJ_STR);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multi tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     *  Open dialog for Manual entry for barcode
     */
    private void openManualDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_manual_entry);
        dialog.setCanceledOnTouchOutside(false);

        EditText etText = dialog.findViewById(R.id.etText);
        Button btnCancel = dialog.findViewById(R.id.dialogButtonCancle);
        Button btnOk = dialog.findViewById(R.id.dialogButtonOK);

        btnOk.setOnClickListener(v1 -> {
            String manualData = etText.getText().toString().trim();

            if(manualData.length() == 0){
                Toast.makeText(this, "Enter manual entry ", Toast.LENGTH_SHORT).show();
                return;
            }
            checkBarcodeValidity(manualData);
            dialog.dismiss();
        });
        btnCancel.setOnClickListener(v2 -> {
            isManualEntry = false;
            dialog.dismiss();
        });
        dialog.show();
    }

    /**
     * onTap returns the tapped barcode result to the calling Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        // Find tap point in preview frame coordinates.
        int[] location = new int[2];
        mGraphicOverlay.getLocationOnScreen(location);
        float x = (rawX - location[0]) / mGraphicOverlay.getWidthScaleFactor();
        float y = (rawY - location[1]) / mGraphicOverlay.getHeightScaleFactor();

        // Find the barcode whose center is closest to the tapped point.
        Barcode best = null;
        float bestDistance = Float.MAX_VALUE;
        for (BarcodeGraphic graphic : mGraphicOverlay.getGraphics()) {
            Barcode barcode = graphic.getBarcode();
            if (barcode.getBoundingBox().contains((int) x, (int) y)) {
                // Exact hit, no need to keep looking.
                best = barcode;
                break;
            }
            float dx = x - barcode.getBoundingBox().centerX();
            float dy = y - barcode.getBoundingBox().centerY();
            float distance = (dx * dx) + (dy * dy);  // actually squared distance
            if (distance < bestDistance) {
                bestDistance = distance;
            }
        }

        return false;
    }

    /**
     * set webview.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void setWebView() {


        WebSettings settings = headerWebview.getSettings();

        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        settings.setJavaScriptEnabled(true);

        headerWebview.addJavascriptInterface(new WebAppJavascriptInterface(this), "Android");

        // By using this method together with the overridden method onReceivedSslError()
        // you will avoid the "WebView Blank Page" problem to appear. This might happen if you
        // use a "https" url!
        settings.setDomStorageEnabled(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        headerWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        headerWebview.setWebChromeClient(new WebChromeClient() {
        });

        // chromium, enable hardware acceleration
        headerWebview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    private void loadUrlHeaderPage(String pageName) {
        if (!TextUtils.isEmpty(pageName)) {
            headerWebview.loadUrl(Uri.decode(Utility.getCompleteUrl(this,pageName)));
        }
    }


    /**
     * This function check for barcode validating with API Call
     * @param barcode barcode from scan
     */
    private  void checkBarcodeValidity(String barcode){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String validateData = validateBarcodeValidateFromServer(barcode);
            handler.post(() -> {
                if(validateData != null && validateData.length()>0){
                    try {
                        final JSONObject object = new JSONObject(validateData);
                        if(object.has("status") && object.getString("status").length()>0){

                            if(object.getString("status").equals("0")){
                                Toast.makeText(BarcodeCaptureActivity.this,object.getString("message"),Toast.LENGTH_SHORT).show();
                                barcodes.add(barcode);
                                isManualEntry = false;

                            }
                            if(object.getString("status").equals("1")){
                                Toast.makeText(BarcodeCaptureActivity.this,object.getString("message"),Toast.LENGTH_SHORT).show();
                                isManualEntry = false;
                            }

                        }
                    }catch (JSONException jsonException){
                        jsonException.printStackTrace();
                        isManualEntry = false;
                    }

                }else {
                    isManualEntry = false;
                }
            });
        });
    }


    /**
     * This function call API using BASE_URL
     * @param barcode send scan barcode with other data
     * @return if barcode scan success send return result otherwise through exception1
     */
    private String  validateBarcodeValidateFromServer(String barcode){
        String tokenData = "";
        try {
            long date = System.currentTimeMillis();
            URL url = new URL(BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject data = new JSONObject();
            data.put("userName", AppConstant.USERConstant.USER_NAME);
            data.put("userID", AppConstant.USERConstant.USER_ID);
            data.put("parcelnumber", barcode);
            data.put("date", date);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(data.toString());
            writer.flush();


            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                tokenData = line;
            }
            writer.close();
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "ERROR  : " + e.getMessage() );
        }
        return tokenData;
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    public class WebAppJavascriptInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppJavascriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void redirectPage(final String callRequestObj) {

            runOnUiThread(() -> {
                try {
                    Intent data = new Intent();
                    data.putExtra(BarcodeConstant.IntentExtras.REQUEST_JSON_OBJ_STR, reqData);
                    data.putExtra(BarcodeConstant.IntentExtras.REQUEST_CALL_BACK_OBJ, callRequestObj);
                    data.putExtra("isFromHeaderClick", true);
                    setResult(CommonStatusCodes.SUCCESS, data);
                    finish();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        }
    }

}
