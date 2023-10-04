package com.hokuapps.loadnativefileupload.annotate;



import static com.hokuapps.loadnativefileupload.annotate.TurboImageView.cirRectPoints;
import static com.hokuapps.loadnativefileupload.annotate.TurboImageView.straightLinePoints;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.ANNOTATIONS_ARRAY;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.BADGE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.COLOR;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.END;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.IMAGE_NAME;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.METADATA;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.SELECTED_IMAGE_NAME;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.START;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.TITLE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.TOTAL_COUNT_EDIT;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.TYPE;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.X;
import static com.hokuapps.loadnativefileupload.constants.KeyConstants.keyConstants.Y;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.loadnativefileupload.R;
import com.hokuapps.loadnativefileupload.annotate.adapter.Image;
import com.hokuapps.loadnativefileupload.annotate.adapter.ImageAdapter;
import com.hokuapps.loadnativefileupload.imageEditor.IPRectangleAnnotationActivity;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils;
import com.xinlan.imageeditlibrary.editimage.utils.FileUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressLint("Registered")
public class AnnotateActivity extends AppCompatActivity implements TurboImageViewListener {

    public static final String FILE_PATH = "file_path";
    public static final String EXTRA_OUTPUT = "extra_output";
    public static final String SAVE_FILE_PATH = "save_file_path";
    public static final String ORIGINAL_IMAGE = "ORIGINAL_IMAGE";
    public static final String EXTRA_TOOLBAR_COLOR = "extra_toolbar_color";
    public static final String EXTRA_TOOLBAR_TITLE = "extra_toolbar_title";
    public static final String IMAGE_IS_EDIT = "image_is_edit";
    public static final String ANNOTATION_COUNT = "ANNOTATION_COUNT";
    public static final String ANNOTATION_DATA = "ANNOTATION_DATA";
    public static final String IS_ANNOTATION = "IS_ANNOTATION";
    public static final String ANNOTATION_TYPE = "ANNOTATION_TYPE";
    public static final String ANNOTATION_COLOR = "ANNOTATION_COLOR";
    public static final String ANNOTATION_DRAW_TYPE = "ANNOTATION_DRAW_TYPE";
    private static final String TAG = "AnnotateActivity";
    public static int totalcountedit = 0;
    static boolean isDelete = false;
    public String filePath;
    public String originalImage;
    public String saveFilePath;
    public TextView imageTitle;
    public ImageView btnFreeDraw;
    boolean isSelect = false;
    String toolbarTitle = "";
    String itemTitle = "";
    String metaData = "";
    int drawType;
    int linePathCount = 0;
    String toolbarColor = "#FF0000";
    Bitmap dragged;
    boolean isDraw;
    private TextPaint mTextPaint = new TextPaint();
    private LoadImageTask mLoadImageTask;
    private int imageWidth, imageHeight;
    private TextView text_clear, text_done;
    private TextView title;
    private RelativeLayout toolbar;
    private TurboImageView turboImageView;
    private RelativeLayout reldrag;
    private ImageView imgMain;
    private Button iv_undo;
    private FrameLayout frame_annotation;
    private RecyclerView recyclerView;
    private ArrayList<Image> imageList = new ArrayList<>();
    private SaveImageAnnotatedImage saveImageAnnotatedImage;
    private ArrayList<Point> drawPoints = new ArrayList<>();
    private static Context mContext;

    public static void start(Activity context, final String jsResponseData, final String editImagePath, final String outputPath, String colorCode, String title, final int requestCode, int type) {
        mContext = context;
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, com.xinlan.imageeditlibrary.R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, AnnotateActivity.class);
        it.putExtra(IPRectangleAnnotationActivity.DATA, jsResponseData);
        it.putExtra(IPRectangleAnnotationActivity.FILE_PATH, editImagePath);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_OUTPUT, outputPath);
        it.putExtra(IPRectangleAnnotationActivity.ANNOTATION_TYPE, type);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_TOOLBAR_COLOR, colorCode);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_TOOLBAR_TITLE, title);
        context.startActivityForResult(it, requestCode);
    }


    /**
     * Get progress dialog instance with given title string
     * @param context
     * @param title title to show on dialog
     * @param canCancel
     * @return
     */
    public static Dialog getLoadingDialog(Context context, String title, boolean canCancel) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(canCancel);
        dialog.setMessage(title);
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotate);
        initView();
        initAnnotateButton();
        getData();
        setStatusBarColor();
        setToolbarTitleAndColor();

    }


    /**
     * Initialize buttons and set click listeners
     */
    private void initAnnotateButton() {
        findViewById(R.id.addButton).setOnClickListener(view -> {

        });

        findViewById(R.id.removeButton).setOnClickListener(v -> {
            boolean deleted = turboImageView.removeSelectedObject();
            Log.d(TAG, "deleted: " + deleted);
        });

        text_clear.setOnClickListener(v -> deleteAnnotation());


        iv_undo.setOnClickListener(v -> {
            if (drawPoints.size() > 0) {
                drawPoints.remove(drawPoints.size() - 1);
                turboImageView.undoSelectedObject();
            }

        });

        findViewById(R.id.removeAllButton).setOnClickListener(v -> turboImageView.removeAllObjects());

        findViewById(R.id.deselectButton).setOnClickListener(v -> turboImageView.deselectAll());

        findViewById(R.id.flipButton).setOnClickListener(v -> turboImageView.toggleFlippedHorizontallySelectedObject());
    }


    /**
     * Initialize views(imageview,button,toolbar,recyclerview)
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        imageWidth = getWindowManager().getDefaultDisplay().getWidth();
        imageHeight = getWindowManager().getDefaultDisplay().getHeight();
        isDraw = false;
        btnFreeDraw = findViewById(R.id.btnFreeDraw);
        title = findViewById(R.id.title);
        toolbar = findViewById(R.id.toolbar);
        text_clear = findViewById(R.id.text_clear);
        reldrag = findViewById(R.id.reldrag);
        imgMain = findViewById(R.id.imgMain);
        iv_undo = findViewById(R.id.iv_undo);
        frame_annotation = findViewById(R.id.frame_annotation);
        recyclerView = findViewById(R.id.recyclerView);
        imageTitle = findViewById(R.id.imgTitle);
        turboImageView = findViewById(R.id.turboImageView);
        turboImageView.setListener(this);
        text_done = findViewById(R.id.text_done);
        text_done.setOnClickListener(new SaveBtnClick());

        btnFreeDraw.setOnClickListener(v -> {
            isDraw = !isDraw;
            if (isDraw) {
                btnFreeDraw.setImageResource(R.drawable.ic_cancel);
                TurboImageView.isFreeDraw = true;
                TurboImageView.isDraw = "Path";
            } else {
                TurboImageView.isFreeDraw = false;
                btnFreeDraw.setImageResource(R.drawable.ic_edit);
            }
            turboImageView.deselectAll();
        });

        reldrag.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                drawPoints.add(new Point(event.getX(), event.getY(), toolbarColor, itemTitle, metaData));
                addImage(dragged, event.getX(), event.getY());
            }
            return true;
        });

        turboImageView.setOnLongClickListener(v -> {
            try {
                if (new JSONObject(getIntent().getStringExtra(IPRectangleAnnotationActivity.DATA)).has("annotaionsArray")) {
                    if (!turboImageView.check()) {
                        for (MultiTouchObject object : turboImageView.mImages) {
                            if (object.isSelected()) {
                                isSelect = true;
                                break;
                            }
                        }

                        if (isSelect) {
                            isSelect = false;
                            final AlertDialog.Builder builder = new AlertDialog.Builder(AnnotateActivity.this);
                            builder.setMessage("Are you sure want to delete this annotation.");
                            builder.setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    isDelete = true;
                                    float x = 0, y = 0;
                                    for (MultiTouchObject object : turboImageView.mImages) {
                                        if (object.isSelected()) {
                                            x = object.startMidX;
                                            y = object.startMidY;
                                        }
                                    }

                                    turboImageView.removeLongSelectedObject();
                                    for (int i = 0; i < drawPoints.size(); i++) {
                                        Point point = drawPoints.get(i);
                                        if (point.x == x && point.y == y) {
                                            drawPoints.remove(i);
                                            break;
                                        }
                                    }
                                }
                            });

                            builder.setNegativeButton(R.string.lbl_no, (dialog, which) -> dialog.dismiss());
                            final AlertDialog dialog = builder.create();
                            dialog.setCancelable(false);
                            dialog.show();
                        }
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    @Override
    public void onImageObjectSelected(MultiTouchObject multiTouchObject) {
        Log.d(TAG, "image object selected");
    }

    @Override
    public void onImageObjectDropped() {
        Log.d(TAG, "image object dropped");
    }

    @Override
    public void onCanvasTouched() {
        turboImageView.deselectAll();
        Log.d(TAG, "canvas touched");
    }


    /**
     * Add bitmap image to imageview
     * @param bitmap bitmaps
     * @param x
     * @param y
     */
    public void addImage(Bitmap bitmap, float x, float y) {
        turboImageView.addObject(AnnotateActivity.this, bitmap, x, y);
        turboImageView.deselectAll();
    }


    /**
     *
     * @param dragged
     * @param title
     * @param metaData
     */
    public void getDraggedBitMap(Bitmap dragged, String title, String metaData) {
        this.dragged = dragged;
        this.itemTitle = title;
        this.metaData = metaData;
    }


    /**
     * Set title and color to toolbar
     */
    private void setToolbarTitleAndColor() {
        if (!TextUtils.isEmpty(toolbarTitle)) {
            title.setText(toolbarTitle);
        }

        if (!TextUtils.isEmpty(toolbarColor))
            toolbar.setBackgroundColor(Color.parseColor(toolbarColor));
    }


    /**
     * Set color to status bar
     */
    private void setStatusBarColor() {
        try {
            String color = getIntent().getExtras().containsKey(EXTRA_TOOLBAR_COLOR)
                    ? getIntent().getExtras().getString(EXTRA_TOOLBAR_COLOR)
                    : "#FFFFFF";
            if (!TextUtils.isEmpty(color) && !"#FFFFFF".equalsIgnoreCase(color)) {
                setStatusBarColor(FileUploadUtility.changeColorToPrimaryHSB(color));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Set given color to status bar
     * @param color color need to set to status bar
     */
    public void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimaryDark, outValue, true);

            getWindow().setStatusBarColor(color == 0 ? outValue.data : color);
        }
    }


    /**
     * Fetch data from intent and set to variables
     */
    private void getData() {
        try {
            filePath = getIntent().getStringExtra(FILE_PATH);
            originalImage = getIntent().getStringExtra(FILE_PATH);
            saveFilePath = getIntent().getStringExtra(EXTRA_OUTPUT);
            drawType = getIntent().getIntExtra(ANNOTATION_TYPE, 0);
            toolbarColor = getIntent().getStringExtra(EXTRA_TOOLBAR_COLOR);
            toolbarTitle = getIntent().getStringExtra(EXTRA_TOOLBAR_TITLE);
            if (filePath.contains("https")) {
                Glide.with(this)
                        .load(filePath)
                        .thumbnail(0.1f)
                        .into(imgMain);
            } else {
                loadImage(filePath);
            }
            if (!TextUtils.isEmpty(getIntent().getStringExtra(IPRectangleAnnotationActivity.DATA))) {
                JSONObject object = new JSONObject(getIntent().getStringExtra(IPRectangleAnnotationActivity.DATA));
                totalcountedit = FileUploadUtility.getJsonObjectIntValue(object, TOTAL_COUNT_EDIT);
                turboImageView.imageCount = totalcountedit;
                if (object.has(ANNOTATIONS_ARRAY)) {
                    btnFreeDraw.setVisibility(View.VISIBLE);
                    iv_undo.setVisibility(View.GONE);
                    JSONArray annotationsArray = object.getJSONArray(ANNOTATIONS_ARRAY);
                    for (int i = 0; i < annotationsArray.length(); i++) {
                        JSONObject jsonObject = annotationsArray.getJSONObject(i);
                        Image image = new Image(FileUploadUtility.getHtmlDirFromSandbox(mContext).getAbsolutePath() +
                                File.separator + FileUploadUtility.getStringObjectValue(jsonObject, IMAGE_NAME),
                                FileUploadUtility.getHtmlDirFromSandbox(mContext).getAbsolutePath() +
                                        File.separator + FileUploadUtility.getStringObjectValue(jsonObject, SELECTED_IMAGE_NAME)
                                , FileUploadUtility.getStringObjectValue(jsonObject, TITLE),
                                FileUploadUtility.getStringObjectValue(jsonObject, METADATA), false, false, false);
                        imageList.add(image);
                    }
                } else {
                    btnFreeDraw.setVisibility(View.GONE);
                    iv_undo.setVisibility(View.GONE);
                    Image image1 = new Image(getResources().getDrawable(R.drawable.line_white), getResources().getDrawable(R.drawable.line_red), "Draw Line", "", false, true, true);
                    Image image2 = new Image(getResources().getDrawable(R.drawable.circle_white), getResources().getDrawable(R.drawable.circle_red), "Circle", "", false, true, false);
                    imageList.add(image1);
                    imageList.add(image2);
                }
            }

            ImageAdapter adapter = new ImageAdapter(imageList, AnnotateActivity.this);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Load image from given file path
     * @param filepath
     */
    public void loadImage(String filepath) {
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }
        mLoadImageTask = new LoadImageTask();
        mLoadImageTask.execute(filepath);
    }


    /**
     * Show alert dialog to delete or cancel annotation
     */
    private void deleteAnnotation() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to clear annotation.");
        builder.setPositiveButton(R.string.lbl_yes, (dialog, which) -> {
            dialog.dismiss();
            drawPoints.clear();
            turboImageView.removeAllObjects();
            turboImageView.clearAllDrawData();
            turboImageView.imageCount = totalcountedit;
        });

        builder.setNegativeButton(R.string.lbl_no, (dialog, which) -> dialog.dismiss());
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * Save Stickers
     */
    public void saveStickers(Bitmap bitmap) {
        if (saveImageAnnotatedImage != null) {
            saveImageAnnotatedImage.cancel(true);
        }
        saveImageAnnotatedImage = new SaveImageAnnotatedImage(this);
        saveImageAnnotatedImage.execute(bitmap);
    }

    public static class Point {
        public float x, y;
        String color;
        String drawType;
        String metaData;

        public Point(float x, float y, String color, String drawType, String metaData) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.drawType = drawType;
            this.metaData = metaData;
        }

        @Override
        public String toString() {
            return x + ", " + y;
        }
    }

    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            return BitmapUtils.getSampledBitmap(params[0], imageWidth, imageHeight);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (result != null) {
                frame_annotation.setBackground(new BitmapDrawable(result));
            } else {
                frame_annotation.setBackgroundColor(getResources().getColor(R.color.white));
            }

        }
    }// end inner class

    /**
     *
     */
    private final class SaveBtnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            PermissionListener permissionListener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    iv_undo.setVisibility(View.GONE);
                    text_done.setEnabled(false);
                    frame_annotation.setDrawingCacheEnabled(true);
                    Bitmap bitmap = frame_annotation.getDrawingCache();
                    saveStickers(bitmap);
                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {

                }
            };
            TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setPermissions(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    .check();

        }
    }// end inner class

    @SuppressLint("StaticFieldLeak")
    private final class SaveImageAnnotatedImage extends AsyncTask<Bitmap, Void, Void> {

        private AnnotateActivity mContext;
        private Dialog dialog;

        public SaveImageAnnotatedImage(AnnotateActivity context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = getLoadingDialog(mContext, "Saving...", false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Bitmap... params) {
            BitmapUtils.saveBitmap(params[0], mContext.saveFilePath);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent returnIntent = new Intent();
            if (FileUtil.checkFileExist(saveFilePath)) {
                JSONArray annotateData = new JSONArray();
                JSONObject object = new JSONObject();
                JSONObject start = new JSONObject();
                JSONObject end = new JSONObject();
                try {
                    if (straightLinePoints != null && straightLinePoints.size() > 0) {
                        Set<Map.Entry<Integer, ArrayList<TurboImageView.Point>>> mappings = straightLinePoints.entrySet();
                        TurboImageView.Point point = null;
                        TurboImageView.Point pointEnd = null;
                        for (Map.Entry<Integer, ArrayList<TurboImageView.Point>> entry : mappings) {
                            if (entry.getValue().size() > 1) {
                                linePathCount++;
                                point = ((TurboImageView.Point) ((ArrayList) entry.getValue()).get(0));
                                pointEnd = ((TurboImageView.Point) ((ArrayList) entry.getValue()).get(1));
                                object.put(BADGE, entry.getKey());
                                object.put(TYPE, point.drawType);
                                object.put(TITLE, point.drawType);
                                object.put(COLOR, String.valueOf(TurboImageView.colorCode));

                                start.put(X, point.x);
                                start.put(Y, point.y);

                                end.put(X, pointEnd.x);
                                end.put(Y, pointEnd.y);

                                object.put(START, start);
                                object.put(END, end);

                                annotateData.put(object);
                                object = new JSONObject();
                                start = new JSONObject();
                                end = new JSONObject();
                            }
                        }
                    }
                    if (cirRectPoints.size() > 0) {
                        for (int i = 1; i <= cirRectPoints.size(); i++) {
                            TurboImageView.Point point = null;
                            object.put(BADGE, i);
                            object.put(TYPE, point.drawType);
                            object.put(TITLE, point.drawType);
                            object.put(COLOR, String.valueOf(TurboImageView.colorCode));
                            point = cirRectPoints.get(i - 1);
                            start.put(X, point.x);
                            start.put(Y, point.y);
                            object.put(START, start);

                            annotateData.put(object);
                            object = new JSONObject();
                            start = new JSONObject();
                        }

                    }
                    if (drawPoints.size() > 0) {
                        for (int i = 1; i <= drawPoints.size(); i++) {
                            Point point = drawPoints.get(i - 1);
                            object.put(BADGE, i);
                            object.put(TYPE, drawType);
                            object.put(TITLE, point.drawType);
                            object.put(METADATA, point.metaData);
                            object.put(COLOR, toolbarColor);

                            start.put(X, point.x);
                            start.put(Y, point.y);
                            object.put(START, start);

                            annotateData.put(object);
                            object = new JSONObject();
                            start = new JSONObject();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                returnIntent.putExtra(SAVE_FILE_PATH, saveFilePath);
                returnIntent.putExtra(IMAGE_IS_EDIT, true);
                FileUtil.ablumUpdate(mContext, saveFilePath);
                returnIntent.putExtra(ORIGINAL_IMAGE, originalImage);
                int annotationCount = cirRectPoints.size() + linePathCount + drawPoints.size();
                returnIntent.putExtra(ANNOTATION_COUNT, annotationCount + totalcountedit);
                returnIntent.putExtra(ANNOTATION_DATA, annotateData.toString());
                mContext.setResult(RESULT_OK, returnIntent);

            } else {
                JSONArray annotateData = new JSONArray();
                JSONObject object = new JSONObject();
                JSONObject start = new JSONObject();
                JSONObject end = new JSONObject();
                try {
                    if (straightLinePoints != null && straightLinePoints.size() > 0) {
                        Set<Map.Entry<Integer, ArrayList<TurboImageView.Point>>> mappings = straightLinePoints.entrySet();
                        TurboImageView.Point point = null;
                        TurboImageView.Point pointEnd = null;
                        for (Map.Entry<Integer, ArrayList<TurboImageView.Point>> entry : mappings) {
                            if (entry.getValue().size() > 1) {
                                linePathCount++;
                                point = ((TurboImageView.Point) ((ArrayList) entry.getValue()).get(0));
                                pointEnd = ((TurboImageView.Point) ((ArrayList) entry.getValue()).get(1));
                                object.put(BADGE, entry.getKey());
                                object.put(TYPE, point.drawType);
                                object.put(TITLE, point.drawType);
                                object.put(COLOR, String.valueOf(TurboImageView.colorCode));

                                start.put(X, point.x);
                                start.put(Y, point.y);

                                end.put(X, pointEnd.x);
                                end.put(Y, pointEnd.y);

                                object.put(START, start);
                                object.put(END, end);

                                annotateData.put(object);
                                object = new JSONObject();
                                start = new JSONObject();
                                end = new JSONObject();
                            }
                        }
                    }

                    if (cirRectPoints.size() > 0) {
                        for (int i = 1; i <= cirRectPoints.size(); i++) {
                            TurboImageView.Point point = null;
                            object.put(BADGE, i);
                            object.put(TYPE, point.drawType);
                            object.put(TITLE, point.drawType);
                            object.put(COLOR, String.valueOf(TurboImageView.colorCode));
                            point = cirRectPoints.get(i - 1);
                            start.put(X, point.x);
                            start.put(Y, point.y);
                            object.put(START, start);

                            annotateData.put(object);
                            object = new JSONObject();
                            start = new JSONObject();
                        }

                    }
                    if (drawPoints.size() > 0) {
                        for (int i = 1; i <= drawPoints.size(); i++) {
                            Point point = drawPoints.get(i - 1);
                            object.put(BADGE, i);
                            object.put(TYPE, drawType);
                            object.put(TITLE, point.drawType);
                            object.put(METADATA, point.metaData);
                            object.put(COLOR, toolbarColor);

                            start.put(X, point.x);
                            start.put(Y, point.y);
                            object.put(START, start);

                            annotateData.put(object);
                            object = new JSONObject();
                            start = new JSONObject();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                returnIntent.putExtra(SAVE_FILE_PATH, filePath);
                returnIntent.putExtra(ORIGINAL_IMAGE, originalImage);
                returnIntent.putExtra(IMAGE_IS_EDIT, false);
                int annotationCount = cirRectPoints.size() + linePathCount + drawPoints.size();
                returnIntent.putExtra(ANNOTATION_COUNT, annotationCount + totalcountedit);
                returnIntent.putExtra(ANNOTATION_DATA, annotateData.toString());
                mContext.setResult(RESULT_OK, returnIntent);
            }

            if (dialog != null) {
                dialog.dismiss();
            }
            text_done.setEnabled(true);
            straightLinePoints.clear();
            cirRectPoints.clear();
            drawPoints.clear();
            TurboImageView.badgeCount = 0;
            linePathCount = 0;
            mContext.finish();
        }
    }
}
