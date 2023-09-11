package com.hokuapps.loadnativefileupload.annotate;

import static com.hokuapps.loadnativefileupload.NativeFileUpload.ACTION_REQUEST_EDIT_IMAGE;
import static com.hokuapps.loadnativefileupload.annotate.TurboImageView.cirRectPoints;
import static com.hokuapps.loadnativefileupload.annotate.TurboImageView.straightLinePoints;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.View;
import android.view.WindowManager;
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
import com.hokuapps.loadnativefileupload.NativeFileUpload;
import com.hokuapps.loadnativefileupload.R;
import com.hokuapps.loadnativefileupload.annotate.adapter.Image;
import com.hokuapps.loadnativefileupload.annotate.adapter.ImageAdapter;
import com.hokuapps.loadnativefileupload.annotate.adapter.Pencil;
import com.hokuapps.loadnativefileupload.annotate.adapter.PencilAdapter;
import com.hokuapps.loadnativefileupload.imageEditor.IPRectangleAnnotationActivity;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils;
import com.xinlan.imageeditlibrary.editimage.utils.FileUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class FreeDrawingActivity extends AppCompatActivity implements TurboImageViewListener {

    public static final String FILE_PATH = "file_path";
    public static final String EXTRA_OUTPUT = "extra_output";
    public static final String SAVE_FILE_PATH = "save_file_path";
    public static final String ORIGINAL_IMAGE = "ORIGINAL_IMAGE";
    public static final String EXTRA_TOOLBAR_COLOR = "extra_toolbar_color";
    public static final String EXTRA_TOOLBAR_TITLE = "extra_toolbar_title";
    public static final String IMAGE_IS_EDIT = "image_is_edit";
    public static final String ANNOTATION_COUNT = "ANNOTATION_COUNT";
    public static final String ANNOTATION_DATA = "ANNOTATION_DATA";
    public static final String ANNOTATION_TYPE = "ANNOTATION_TYPE";

    private static final String TAG = "AnnotateActivity";
    public String filePath;
    public String originalImage;
    public String saveFilePath;
    public TextView imageTitle;
    public ImageView btnFreeDraw;
    String toolbarTitle = "";
    String itemTitle = "";
    String metaData = "";
    int drawType;
    String toolbarColor = "#FF0000";
    Bitmap dragged;
    boolean isDraw;
    private LoadImageTask mLoadImageTask;
    private int imageWidth, imageHeight;
    private TextView text_clear, text_done;
    private TextView title;
    private ImageView imgMain;
    private RelativeLayout toolbar, relMain;
    private TurboImageViewFree turboImageView;
    private RelativeLayout reldrag;
    private FrameLayout frame_annotation;
    private RecyclerView recyclerView, recyclerViewPencil;
    private ArrayList<Image> imageList = new ArrayList<>();
    private ArrayList<Pencil> pencilList = new ArrayList<>();
    private SaveImageAnnotatedImage saveImageAnnotatedImage;
    private ArrayList<Point> drawPoints = new ArrayList<>();
    private Path path = new Path();
    private Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ArrayList<Path> paths = new ArrayList<Path>();

    private static Context mContext;

    public static void start(Activity context, final String data, final String editImagePath, final String outputPath, String colorCode, String title, final int requestCode, int type) {
        mContext = context;
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, com.xinlan.imageeditlibrary.R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, FreeDrawingActivity.class);
        it.putExtra(IPRectangleAnnotationActivity.DATA, data);
        it.putExtra(IPRectangleAnnotationActivity.FILE_PATH, editImagePath);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_OUTPUT, outputPath);
        it.putExtra(IPRectangleAnnotationActivity.ANNOTATION_TYPE, type);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_TOOLBAR_COLOR, colorCode);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_TOOLBAR_TITLE, title);
        context.startActivityForResult(it, requestCode);
    }

    public static Dialog getLoadingDialog(Context context, String title, boolean canCancel) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(canCancel);
        dialog.setMessage(title);
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_drwaing);

        initView();
        initAnnotateButton();
        getData();
        setStatusBarColor();
        setToolbarTitleAndColor();
    }

    private void initAnnotateButton() {
        findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        text_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               deleteAnnotation();
            }
        });
    }

    private void deleteAnnotation() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to clear annotation.");
        builder.setPositiveButton(R.string.lbl_yes, (dialog, which) -> {
            dialog.dismiss();
            finish();
            NativeFileUpload.setFreeDrawing(getIntent().getStringExtra(IPRectangleAnnotationActivity.DATA), filePath, toolbarColor, toolbarTitle, ACTION_REQUEST_EDIT_IMAGE, drawType);
        });

        builder.setNegativeButton(R.string.lbl_no, (dialog, which) -> dialog.dismiss());
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        imageWidth = getWindowManager().getDefaultDisplay().getWidth();
        imageHeight = getWindowManager().getDefaultDisplay().getHeight();
        isDraw = false;
        btnFreeDraw = findViewById(R.id.btnFreeDraw);
        title = findViewById(R.id.title);
        relMain = findViewById(R.id.relMain);
        toolbar = findViewById(R.id.toolbar);
        text_clear = findViewById(R.id.text_clear);
        imgMain = findViewById(R.id.imgMain);
        reldrag = findViewById(R.id.reldrag);
        frame_annotation = findViewById(R.id.frame_annotation);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewPencil = findViewById(R.id.recyclerViewPencil);
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
        });

        reldrag.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                drawPoints.add(new Point(event.getX(), event.getY(), toolbarColor, itemTitle, metaData));
                addImage(dragged, event.getX(), event.getY());
            }
            return true;
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
        Log.d(TAG, "canvas touched");
    }

    public void addImage(Bitmap bitmap, float x, float y) {
    }

    private void setToolbarTitleAndColor() {
        if (!TextUtils.isEmpty(toolbarTitle)) {
            title.setText(toolbarTitle);
        }

        if (!TextUtils.isEmpty(toolbarColor))
            toolbar.setBackgroundColor(Color.parseColor(toolbarColor));
    }

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

    public void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimaryDark, outValue, true);

            getWindow().setStatusBarColor(color == 0 ? outValue.data : color);
        }
    }

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
                if (object.has("annotaionsArray")) {
                    btnFreeDraw.setVisibility(View.VISIBLE);
                    JSONArray annotaionsArray = object.getJSONArray("annotaionsArray");
                    for (int i = 0; i < annotaionsArray.length(); i++) {
                        JSONObject jsonObject = annotaionsArray.getJSONObject(i);
                        Image image = new Image(FileUploadUtility.getHtmlDirFromSandbox(mContext).getAbsolutePath() +
                                File.separator + jsonObject.getString("imageName"), FileUploadUtility.getHtmlDirFromSandbox(mContext).getAbsolutePath() +
                                File.separator + jsonObject.getString("selectedImageName"), jsonObject.getString("title"), jsonObject.getString("metadata"), false, false, false);
                        imageList.add(image);
                    }
                } else {
                    btnFreeDraw.setVisibility(View.GONE);
                    Image image1 = new Image(getResources().getDrawable(R.drawable.line_white), getResources().getDrawable(R.drawable.line_red), "Draw Line", "", false, true, true);
                    Image image2 = new Image(getResources().getDrawable(R.drawable.circle_white), getResources().getDrawable(R.drawable.circle_red), "Circle", "", false, true, false);
                    imageList.add(image1);
                    imageList.add(image2);
                }
            }

            ImageAdapter adapter = new ImageAdapter(imageList, this);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setAdapter(adapter);

            if (drawType == 5) {
                recyclerView.setVisibility(View.GONE);
                btnFreeDraw.setVisibility(View.GONE);
                recyclerViewPencil.setVisibility(View.VISIBLE);
                TurboImageView.isFreeDraw = true;
                TurboImageView.isDraw = "Path";
                pencilList.add(new Pencil("White", "#FFFFFF", getResources().getDrawable(R.drawable.eraser), false));
                pencilList.add(new Pencil("Black", "#111111", getResources().getDrawable(R.drawable.black), false));
                pencilList.add(new Pencil("Blue", "#0000FF", getResources().getDrawable(R.drawable.blue), false));
                pencilList.add(new Pencil("Brown", "#A52A2A", getResources().getDrawable(R.drawable.brown), false));
                pencilList.add(new Pencil("Dark Green", "#006400", getResources().getDrawable(R.drawable.darkgreen), false));
                pencilList.add(new Pencil("Dark Orange", "#FF8C00", getResources().getDrawable(R.drawable.darkorange), false));
                pencilList.add(new Pencil("Grey", "#808080", getResources().getDrawable(R.drawable.grey), false));
                pencilList.add(new Pencil("Light Blue", "#ADD8E6", getResources().getDrawable(R.drawable.lightblue), false));
                pencilList.add(new Pencil("Light Green", "#90EE90", getResources().getDrawable(R.drawable.lightgreen), false));
                pencilList.add(new Pencil("Red", "#FF0000", getResources().getDrawable(R.drawable.red), false));
                pencilList.add(new Pencil("Yellow", "#FFFF00", getResources().getDrawable(R.drawable.yellow), false));

                PencilAdapter pencilAdapter = new PencilAdapter(pencilList, this);
                recyclerViewPencil.setHasFixedSize(true);
                recyclerViewPencil.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                recyclerViewPencil.setAdapter(pencilAdapter);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerViewPencil.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * load image from the path
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

    private final class SaveBtnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            PermissionListener permissionListener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
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

        private FreeDrawingActivity mContext;
        private Dialog dialog;

        public SaveImageAnnotatedImage(FreeDrawingActivity context) {
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
                            point = ((TurboImageView.Point) ((ArrayList) entry.getValue()).get(0));
                            pointEnd = ((TurboImageView.Point) ((ArrayList) entry.getValue()).get(1));
                            object.put("badge", entry.getKey());
                            object.put("type", point.drawType);
                            object.put("title", point.drawType);
                            object.put("color", String.valueOf(TurboImageView.colorCode));

                            start.put("x", point.x);
                            start.put("y", point.y);

                            end.put("x", pointEnd.x);
                            end.put("y", pointEnd.y);

                            object.put("start", start);
                            object.put("end", end);

                            annotateData.put(object);
                            object = new JSONObject();
                            start = new JSONObject();
                            end = new JSONObject();
                        }
                    }
                    if (cirRectPoints.size() > 0) {
                        for (int i = 1; i <= cirRectPoints.size(); i++) {
                            TurboImageView.Point point = null;
                            object.put("badge", i);
                            object.put("type", point.drawType);
                            object.put("title", point.drawType);
                            object.put("color", String.valueOf(TurboImageView.colorCode));
                            point = cirRectPoints.get(i - 1);
                            start.put("x", point.x);
                            start.put("y", point.y);
                            object.put("start", start);

                            annotateData.put(object);
                            object = new JSONObject();
                            start = new JSONObject();
                        }

                    }
                    if (drawPoints.size() > 0) {
                        for (int i = 1; i <= drawPoints.size(); i++) {
                            Point point = drawPoints.get(i - 1);
                            object.put("badge", i);
                            object.put("type", drawType);
                            object.put("title", point.drawType);
                            object.put("metadata", point.metaData);
                            object.put("color", toolbarColor);

                            start.put("x", point.x);
                            start.put("y", point.y);
                            object.put("start", start);

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
                int annotationCount = cirRectPoints.size() + TurboImageView.badgeCount + drawPoints.size();
                returnIntent.putExtra(ANNOTATION_COUNT, "" + annotationCount);
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
                            point = ((TurboImageView.Point) ((ArrayList) entry.getValue()).get(0));
                            pointEnd = ((TurboImageView.Point) ((ArrayList) entry.getValue()).get(1));
                            object.put("badge", entry.getKey());
                            object.put("type", point.drawType);
                            object.put("title", point.drawType);
                            object.put("color", String.valueOf(TurboImageView.colorCode));

                            start.put("x", point.x);
                            start.put("y", point.y);

                            end.put("x", pointEnd.x);
                            end.put("y", pointEnd.y);

                            object.put("start", start);
                            object.put("end", end);

                            annotateData.put(object);
                            object = new JSONObject();
                            start = new JSONObject();
                            end = new JSONObject();
                        }
                    }

                    if (cirRectPoints.size() > 0) {
                        for (int i = 1; i <= cirRectPoints.size(); i++) {
                            TurboImageView.Point point = null;
                            object.put("badge", i);
                            object.put("type", point.drawType);
                            object.put("title", point.drawType);
                            object.put("color", String.valueOf(TurboImageView.colorCode));
                            point = cirRectPoints.get(i - 1);
                            start.put("x", point.x);
                            start.put("y", point.y);
                            object.put("start", start);

                            annotateData.put(object);
                            object = new JSONObject();
                            start = new JSONObject();
                        }

                    }
                    if (drawPoints.size() > 0) {
                        for (int i = 1; i <= drawPoints.size(); i++) {
                            Point point = drawPoints.get(i - 1);
                            object.put("badge", i);
                            object.put("type", drawType);
                            object.put("title", point.drawType);
                            object.put("metadata", point.metaData);
                            object.put("color", toolbarColor);

                            start.put("x", point.x);
                            start.put("y", point.y);
                            object.put("start", start);

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
                int annotationCount = cirRectPoints.size() + TurboImageView.badgeCount + drawPoints.size();
                returnIntent.putExtra(ANNOTATION_COUNT, "" + annotationCount);
                returnIntent.putExtra(ANNOTATION_DATA, annotateData.toString());
                mContext.setResult(RESULT_OK, returnIntent);
            }


            if (dialog != null) {
                dialog.dismiss();
            }
            text_done.setEnabled(true);
            TurboImageView.badgeCount = 0;
            mContext.finish();
        }
    }
}
