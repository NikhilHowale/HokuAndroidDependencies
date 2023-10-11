package com.hokuapps.loadnativefileupload.imageEditor;

import static com.xinlan.imageeditlibrary.editimage.view.StickerView.bank;
import static com.xinlan.imageeditlibrary.editimage.view.StickerView.cirRectPoints;
import static com.xinlan.imageeditlibrary.editimage.view.StickerView.startEndPoints;
import static com.xinlan.imageeditlibrary.editimage.view.StickerView.straightLinePoints;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.hokuapps.loadnativefileupload.R;
import com.hokuapps.loadnativefileupload.colorseekbar.ColorSeekBar;
import com.hokuapps.loadnativefileupload.constants.KeyConstants.AnnotationData;
import com.hokuapps.loadnativefileupload.models.LocationMapModel;
import com.hokuapps.loadnativefileupload.utilities.FileUploadUtility;
import com.xinlan.imageeditlibrary.editimage.OnItemChangeListener;
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils;
import com.xinlan.imageeditlibrary.editimage.utils.FileUtil;
import com.xinlan.imageeditlibrary.editimage.view.StickerItem;
import com.xinlan.imageeditlibrary.editimage.view.StickerView;
import com.xinlan.imageeditlibrary.editimage.view.TextStickerItem;
import com.xinlan.imageeditlibrary.editimage.view.TextStickerView;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouch;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Created by user on 21/4/17.
 */
public class IPRectangleAnnotationActivity extends com.xinlan.imageeditlibrary.BaseActivity implements View.OnClickListener {

    public static final String FILE_PATH = "file_path";
    public static final String DATA = "data";
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

    private static boolean isColorPalletShowing;
    private static boolean isTextModeSelected;

    public String filePath;
    public String originalImage;
    public String saveFilePath;
    //    public Bitmap mainBitmap;
    public ImageViewTouch mainImage;
    public ViewFlipper bannerFlipper;
    public StickerView mStickerView;
    public EditText inputEditText;
    public TextStickerView mTextStickerView;
    String toolbarColor = "#3c3c3c";
    String toolbarTitle = "";
    private int imageWidth, imageHeight;
    private LoadImageTask mLoadImageTask;
    private IPRectangleAnnotationActivity mContext;
    private LinearLayout linearLayout;
    private TextView title;
    private View backBtn;
    private View saveBtn;
    private View txtClear;
    private View undoBtn;
    private ColorSeekBar colorSlider;
    private int selectedColor = Color.RED;
    private FrameLayout work_space_layout;
    private LocationMapModel locationMapModel;
    private SaveImageAnnotatedImage saveImageAnnotatedImage;
    private LinearLayout bottomLayout;
    private Object currentItemObj = null;
    private Object lastSelectedItem = null;
    private boolean isFromDeleted = false;
    private ImageView ivTextIcon;
    private ImageView ivColourPickerIcon;
    private ImageView ivUndoIcon;
    private TextView ivDrawPath, ivDrawRectangle, ivDrawCircle, tv_drawLine;

    private ColorSeekBar.OnColorChangeListener onColorChangeListener = new ColorSeekBar.OnColorChangeListener() {
        @Override
        public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
            selectedColor = color;
            if (currentItemObj != null) {
                if (currentItemObj instanceof StickerItem) {
                    StickerItem stickerItem = (StickerItem) currentItemObj;
                    stickerItem.setRectangleBorderColor(color);
                    mStickerView.invalidate();

                } else if (currentItemObj instanceof TextStickerItem) {
                    TextStickerItem textStickerItem = (TextStickerItem) currentItemObj;
                    textStickerItem.setTextColor(selectedColor);
                    mTextStickerView.invalidate();
                }
            }
        }
    };
    private Stack<Object> itemStack;
    private OnItemChangeListener onItemChangeListener = new OnItemChangeListener() {

        @Override
        public void onItemChanged(Object item, int type) {
            if (item == null) return;
            try {
                switch (type) {
                    case OnItemChangeListener.reselected:
                        if (item == null) return;
                        if (item instanceof TextStickerItem) {
                            setEdittextSelection(((TextStickerItem) item).mText, inputEditText);
                            setIsEditTextEnabled(true);
                        } else {
                            setIsEditTextEnabled(false);
                        }
                        break;

                    case OnItemChangeListener.itemChanged:
                        mTextStickerView.removePreviousIfTextEmpty(lastSelectedItem, item);
                        lastSelectedItem = currentItemObj;
                        removeLastSelected(item);
                        currentItemObj = item;
                        break;

                    case OnItemChangeListener.deleted:
                        isFromDeleted = true;
                        inputEditText.setText("");
                        hideSoftKeyboard(inputEditText.getWindowToken());
                        if (item instanceof StickerItem) {
                            StickerItem stickerItem = (StickerItem) item;
                            itemStack.remove(stickerItem);
                        } else if (item instanceof TextStickerItem) {
                            TextStickerItem textStickerItem = (TextStickerItem) item;
                            itemStack.remove(textStickerItem);
                        }

                        setIsEditTextEnabled(false);

                        break;

                    case OnItemChangeListener.pushItemInStack:
                        itemStack.push(item);
                        break;

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    };

    public static void start(Activity context, String filePath, String outputFile, String colorCode, boolean usedForAnnotation, String annotateData) {
        Intent it = new Intent(context, IPRectangleAnnotationActivity.class);
        it.putExtra(IPRectangleAnnotationActivity.FILE_PATH, filePath);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_OUTPUT, outputFile);
        it.putExtra(IPRectangleAnnotationActivity.ANNOTATION_COLOR, colorCode);
        it.putExtra(IPRectangleAnnotationActivity.IS_ANNOTATION, usedForAnnotation);
        it.putExtra(IPRectangleAnnotationActivity.ANNOTATION_DATA, annotateData);
        context.startActivityForResult(it, 9006);
    }

    /** open activity for edit image
     * @param context context
     * @param editImagePath file path to load image
     * @param outputPath path after image is edited
     * @param requestCode set code for intent to specify request
     */
    public static void start(Activity context, final String editImagePath, final String outputPath, String colorCode, String title, final int requestCode) {
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, com.xinlan.imageeditlibrary.R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, IPRectangleAnnotationActivity.class);
        it.putExtra(IPRectangleAnnotationActivity.FILE_PATH, editImagePath);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_OUTPUT, outputPath);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_TOOLBAR_COLOR, colorCode);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_TOOLBAR_TITLE, title);
        bank.clear();
        straightLinePoints.clear();
        cirRectPoints.clear();
        StickerView.badgeCount = 0;
        context.startActivityForResult(it, requestCode);
    }

    public static void start(Activity context, final String editImagePath, final String outputPath, String colorCode, String title, final int requestCode, String type, String color) {
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, com.xinlan.imageeditlibrary.R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, IPRectangleAnnotationActivity.class);
        it.putExtra(IPRectangleAnnotationActivity.FILE_PATH, editImagePath);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_OUTPUT, outputPath);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_TOOLBAR_COLOR, colorCode);
        it.putExtra(IPRectangleAnnotationActivity.EXTRA_TOOLBAR_TITLE, title);
        it.putExtra(IPRectangleAnnotationActivity.ANNOTATION_TYPE, type);
        it.putExtra(IPRectangleAnnotationActivity.ANNOTATION_COLOR, color);
        bank.clear();
        straightLinePoints.clear();
        cirRectPoints.clear();
        StickerView.badgeCount = 0;
        context.startActivityForResult(it, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkInitImageLoader();
        setContentView(R.layout.activity_ip_rectangle_annotation);
        loadBundleData();
        initView();
        getData();
        setStatusBarColor();
        setToolbarTitleAndColor();
    }

    private void setToolbarTitleAndColor() {
        if (!TextUtils.isEmpty(toolbarTitle)) {
            title.setText(toolbarTitle);
        }

        if (!TextUtils.isEmpty(toolbarColor))
            linearLayout.setBackgroundColor(Color.parseColor(toolbarColor));
    }

    private void setStatusBarColor() {
        try {
            String color = "#FFFFFF";

            if(getIntent().getExtras() != null && getIntent().getExtras().containsKey(EXTRA_TOOLBAR_COLOR)){
                color = getIntent().getExtras().getString(EXTRA_TOOLBAR_COLOR);
            }

            if (!TextUtils.isEmpty(color) && !"#FFFFFF".equalsIgnoreCase(color)) {
                setStatusBarColor(FileUploadUtility.changeColorToPrimaryHSB(color));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getData() {
        try {
            filePath = getIntent().getStringExtra(FILE_PATH);
            originalImage = getIntent().getStringExtra(FILE_PATH);
            saveFilePath = getIntent().getStringExtra(EXTRA_OUTPUT);
            toolbarColor = getIntent().getStringExtra(EXTRA_TOOLBAR_COLOR);
            toolbarTitle = getIntent().getStringExtra(EXTRA_TOOLBAR_TITLE);
            loadImage(filePath);
            StickerView.isDraw = getIntent().getStringExtra(ANNOTATION_TYPE) != null ? getIntent().getStringExtra(ANNOTATION_TYPE) : "Line";
            StickerView.colorCode =  "#000000";

            if (getIntent().getStringExtra(ANNOTATION_DATA) != null) {
                JSONArray array = new JSONArray(getIntent().getStringExtra(ANNOTATION_DATA));
                for (int i = 0; i < array.length(); i++) {

                    JSONObject object = array.getJSONObject(i);

                    if (object.getString("type").equalsIgnoreCase("Line")) {

                        JSONObject start = object.getJSONObject("start");
                        JSONObject end = object.getJSONObject("end");

                        float x1 = Float.parseFloat(start.getString("x"));
                        float y1 = Float.parseFloat(start.getString("y"));

                        float x2 = Float.parseFloat(end.getString("x"));
                        float y2 = Float.parseFloat(end.getString("y"));

                        startEndPoints.add(new StickerView.Point(x1, y1, object.getString("color"), object.getString("type")));
                        startEndPoints.add(new StickerView.Point(x2, y2, object.getString("color"), object.getString("type")));

                        straightLinePoints.put(i + 1, startEndPoints);

                        startEndPoints = new ArrayList<>();
                    } else {

                        JSONObject start = object.getJSONObject("start");

                        float x1 = Float.parseFloat(start.getString("x"));
                        float y1 = Float.parseFloat(start.getString("y"));

                        startEndPoints.add(new StickerView.Point(x1, y1, object.getString("color"), object.getString("type")));

                        straightLinePoints.put(i + 1, startEndPoints);

                        startEndPoints = new ArrayList<>();

                    }

                }
                StickerView.isDraw = "";
                mStickerView.drawData();
            }
            if (getIntent().getBooleanExtra(IPRectangleAnnotationActivity.IS_ANNOTATION, false)) {
                saveBtn.performClick();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTextChangeListener() {
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isFromDeleted) {
                    isFromDeleted = false;
                    return;
                }
                String text = s.toString().trim();
                mTextStickerView.setText(TextUtils.isEmpty(text) ? getString(com.xinlan.imageeditlibrary.R.string.add_caption_label) : text);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {

        mContext = this;
        itemStack = new Stack<>();
        imageWidth = getWindowManager().getDefaultDisplay().getWidth();
        imageHeight = getWindowManager().getDefaultDisplay().getHeight();

        inputEditText = findViewById(com.xinlan.imageeditlibrary.R.id.text_input);
        addTextChangeListener();
        bannerFlipper = findViewById(R.id.banner_flipper);
        bannerFlipper.setInAnimation(this, com.xinlan.imageeditlibrary.R.anim.in_bottom_to_top);
        bannerFlipper.setOutAnimation(this, com.xinlan.imageeditlibrary.R.anim.out_bottom_to_top);

        saveBtn = findViewById(R.id.save_btn);
        txtClear = findViewById(R.id.txtClear);
        txtClear.setOnClickListener(this);
        saveBtn.setOnClickListener(new SaveBtnClick());

        undoBtn = findViewById(R.id.undo_btn);
        undoBtn.setEnabled(false);
        undoBtn.setOnClickListener(new UndoBtnClick());

        colorSlider = findViewById(R.id.colorSlider);
        colorSlider.setOnColorChangeListener(onColorChangeListener);

        mainImage = findViewById(R.id.main_image);
        mStickerView = findViewById(R.id.sticker_panel);
        mTextStickerView = findViewById(com.xinlan.imageeditlibrary.R.id.text_sticker_panel);
        mStickerView.setOnItemChangeListener(onItemChangeListener);
        mTextStickerView.setOnItemChangeListener(onItemChangeListener);
        work_space_layout = findViewById(R.id.work_space_layout);

        //toolbar title
        title = findViewById(R.id.title);
        linearLayout = findViewById(R.id.banner);

        ivTextIcon = findViewById(R.id.iv_text);
        ivTextIcon.setOnClickListener(this);
        ivColourPickerIcon = findViewById(R.id.iv_colour_selector);
        ivColourPickerIcon.setOnClickListener(this);
        ivUndoIcon = findViewById(R.id.iv_undo);
        ivDrawPath = findViewById(R.id.tv_drawpath);
        ivDrawRectangle = findViewById(R.id.tv_drawrectangle);
        ivDrawCircle = findViewById(R.id.tv_drawcircle);
        tv_drawLine = findViewById(R.id.tv_drawline);


        ivUndoIcon.setOnClickListener(this);
        ivDrawPath.setOnClickListener(this);
        ivDrawRectangle.setOnClickListener(this);
        ivDrawCircle.setOnClickListener(this);
        tv_drawLine.setOnClickListener(this);

        ivUndoIcon.setEnabled(false);
        bottomLayout = findViewById(R.id.layoutBottom);

        mainImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        final float x = event.getX();
                        final float y = event.getY();
                        if (!isTextModeSelected) {
                            setIsEditTextEnabled(false);
                            currentItemObj = mStickerView.addBitImage(null, x, y, selectedColor);
                            ivUndoIcon.setEnabled(true);
                        } else {
                            currentItemObj = mTextStickerView.addText(inputEditText, currentItemObj, x, y, selectedColor/*Color.BLUE*/);
                            setIsEditTextEnabled(true);
                            inputEditText.setText("");
                            ivUndoIcon.setEnabled(true);
                        }
                        break;
                }
                return true;
            }
        });

        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forceReturnBack();
            }
        });

        setStickerOrTextStickerViewEnabled(true);
        setIsEditTextEnabled(false);

        mStickerView.setmTextStickerView(mTextStickerView);
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("mapLocationModel", locationMapModel);
        returnIntent.putExtras(bundle);
        setResult(RESULT_CANCELED, returnIntent);
        super.onBackPressed();
    }

    /**
     * forceReturnBack
     */
    private void forceReturnBack() {
        Intent returnIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("mapLocationModel", locationMapModel);
        returnIntent.putExtras(bundle);
        setResult(RESULT_CANCELED, returnIntent);
        this.finish();
    }

    /**
     * Load image from filepath
     *
     * @param filepath load image into imageview using file path
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
        saveImageAnnotatedImage = new SaveImageAnnotatedImage((IPRectangleAnnotationActivity.this));
        saveImageAnnotatedImage.execute(bitmap);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }
    }

    /**
     * Load bundle data
     */
    private void loadBundleData() {
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            locationMapModel = bundle.containsKey("mapLocationModel") ? (LocationMapModel) bundle.getParcelable("mapLocationModel") : new LocationMapModel();

            if (locationMapModel == null) {
                locationMapModel = new LocationMapModel();
            }
        }
    }

    @Override
    public void onClick(View v) {
        hideSoftKeyboard(inputEditText.getWindowToken());
        int id = v.getId();
        if (id == R.id.iv_text) {
            onAddTextClick();
        } else if (id == R.id.iv_colour_selector) {
            onColourSelectorClick();
        } else if (id == R.id.iv_undo) {
            onUndoClick();
        } else if (id == R.id.tv_drawpath) {
            StickerView.isDraw = "Path";
        } else if (id == R.id.tv_drawrectangle) {
            StickerView.isDraw = "Rectangle";
        } else if (id == R.id.tv_drawcircle) {
            StickerView.isDraw = "Circle";
        } else if (id == R.id.tv_drawline) {
            StickerView.isDraw = "Line";
        } else if (id == R.id.txtClear) {
            mStickerView.clearAllDrawData();
        }
    }

    public void hideSoftKeyboard(IBinder windowToken) {

        try {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(windowToken, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onColourSelectorClick() {
        if (isColorPalletShowing) {
            isColorPalletShowing = false;
            bottomLayout.setVisibility(View.GONE);
        } else {
            isColorPalletShowing = true;
            bottomLayout.setVisibility(View.VISIBLE);
        }
    }

    private void onUndoClick() {
        ivUndoIcon.setEnabled(!performUndoOperationForRecent());
    }

    private void onAddTextClick() {

        if (isTextModeSelected) {
            isTextModeSelected = false;
            ivTextIcon.setImageResource(R.drawable.text_icon);
            setStickerOrTextStickerViewEnabled(true);
            mTextStickerView.removePreviousIfTextEmpty();
        } else {
            isTextModeSelected = true;
            ivTextIcon.setImageResource(R.drawable.text_icon_selected);
            setStickerOrTextStickerViewEnabled(false);
        }

        setIsEditTextEnabled(false);
    }

    private void setStickerOrTextStickerViewEnabled(boolean isStickerEnabled) {
        //rectangle annotation is enabled or disabled
        mStickerView.setEnabled(isStickerEnabled);

        //rectangle annotation is enabled or disabled
        mTextStickerView.setEnabled(!isStickerEnabled);

    }

    private void setIsEditTextEnabled(boolean isEditTextEnabled) {
        inputEditText.setVisibility(isEditTextEnabled ? View.VISIBLE : View.INVISIBLE);
    }

    private void setEdittextSelection(String text, EditText editText) {
        if (!getString(com.xinlan.imageeditlibrary.R.string.add_caption_label).equalsIgnoreCase(text)) {
            editText.setText(text);
            int position = editText.length();
            Editable eText = editText.getText();
            Selection.setSelection(eText, position);
        }
    }

    /**
     * undo recent item from stack
     *
     * @return return true if undo operation otherwise false
     */
    public boolean performUndoOperationForRecent() {

        if (!itemStack.isEmpty()) {
            Object itemDeletedObj = itemStack.pop();

            if (itemDeletedObj instanceof StickerItem) {
                StickerItem stickerItemDeleted = (StickerItem) itemDeletedObj;
                bank.remove(stickerItemDeleted.stickerItemId);
                if (!itemStack.isEmpty() && itemStack.lastElement() != null) {
                    currentItemObj = itemStack.lastElement();
                    setCurrentItemObj();
                }

                mStickerView.invalidate();

            } else if (itemDeletedObj instanceof TextStickerItem) {
                TextStickerItem textStickerItemDeleted = (TextStickerItem) itemDeletedObj;
                mTextStickerView.bank.remove(textStickerItemDeleted.stickerItemId);
                if (!itemStack.isEmpty() && itemStack.lastElement() != null) {
                    currentItemObj = itemStack.lastElement();
                    setCurrentItemObj();
                }

                mTextStickerView.invalidate();
            }
        }


        if (itemStack.isEmpty()) setIsEditTextEnabled(false);

        return itemStack.isEmpty();
    }

    private void removeLastSelected(Object objFrom) {
        Object currentItemObj = lastSelectedItem;
        if (currentItemObj != null) {
            if (currentItemObj instanceof StickerItem) {
                int stickerIdPrev = ((StickerItem) currentItemObj).stickerItemId;
                int stickerId = -1;
                if (objFrom instanceof StickerItem) {
                    stickerId = ((StickerItem) objFrom).stickerItemId;
                }

                if (stickerIdPrev != stickerId) {
                    ((StickerItem) currentItemObj).isDrawHelpTool = false;
                    mStickerView.invalidate();
                }

            } else if (currentItemObj instanceof TextStickerItem) {
                int stickerIdPrev = ((TextStickerItem) currentItemObj).stickerItemId;
                int stickerId = -1;
                if (objFrom instanceof TextStickerItem) {
                    stickerId = ((TextStickerItem) objFrom).stickerItemId;
                }

                if (stickerIdPrev != stickerId) {
                    ((TextStickerItem) currentItemObj).isShowHelpBox = false;
                    mTextStickerView.invalidate();
                }

            }
        }
    }

    private void setCurrentItemObj() {
        if (currentItemObj != null) {
            if (currentItemObj instanceof StickerItem) {
                setStickerOrTextStickerViewEnabled(true);
                setIsEditTextEnabled(false);
                ((StickerItem) currentItemObj).isDrawHelpTool = true;
                mStickerView.invalidate();
            } else if (currentItemObj instanceof TextStickerItem) {
                setStickerOrTextStickerViewEnabled(false);
                setIsEditTextEnabled(true);
                setEdittextSelection(((TextStickerItem) currentItemObj).mText, inputEditText);
                ((TextStickerItem) currentItemObj).isShowHelpBox = true;
                mTextStickerView.invalidate();
            }
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

            mainImage.setBackground(new BitmapDrawable(mContext.getResources(), result));

        }
    }// end inner class

    private final class SaveBtnClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            PermissionListener permissionListener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    saveBtn.setEnabled(false);

                    if (currentItemObj != null) {
                        if (currentItemObj instanceof StickerItem) {
                            StickerItem stickerItem = (StickerItem) currentItemObj;
                            stickerItem.isDrawHelpTool = false;

                        } else if (currentItemObj instanceof TextStickerItem) {
                            TextStickerItem textStickerItem = (TextStickerItem) currentItemObj;

                            try {
                                //remove item if it has add caption
                                if (textStickerItem != null) {
                                    if (TextUtils.isEmpty(textStickerItem.mText)
                                            || mContext.getResources().getString(com.xinlan.imageeditlibrary.R.string.add_caption_label).equalsIgnoreCase(textStickerItem.mText)) {
                                        mTextStickerView.invalidate();
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            textStickerItem.isShowHelpBox = false;
                        }
                    }

                    mContext.work_space_layout.buildDrawingCache(true);
                    Bitmap bitmap = mContext.work_space_layout.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false);
                    mContext.work_space_layout.destroyDrawingCache();
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

    private final class UndoBtnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            undoBtn.setEnabled(!performUndoOperationForRecent());

        }
    }// end inner class

    private final class SaveImageAnnotatedImage extends AsyncTask<Bitmap, Void, Void> {

        private IPRectangleAnnotationActivity mContext;
        private Dialog dialog;

        public SaveImageAnnotatedImage(IPRectangleAnnotationActivity context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = getLoadingDialog(mContext, getString(R.string.label_saving), false);
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
            Bundle bundle = new Bundle();
            bundle.putParcelable("mapLocationModel", locationMapModel);
            returnIntent.putExtras(bundle);
            if (FileUtil.checkFileExist(saveFilePath)) {

                JSONArray annotateData = new JSONArray();
                JSONObject object = new JSONObject();
                JSONObject start = new JSONObject();
                JSONObject end = new JSONObject();

                try {
                    if (straightLinePoints != null && straightLinePoints.size() > 0) {
                        Set<Map.Entry<Integer, ArrayList<StickerView.Point>>> mappings = straightLinePoints.entrySet();
                        StickerView.Point point = null;
                        StickerView.Point pointEnd = null;
                        for (Map.Entry<Integer, ArrayList<StickerView.Point>> entry : mappings) {
                            point = entry.getValue().get(0);
                            pointEnd = entry.getValue().get(1);
                            object.put(AnnotationData.BADGE, entry.getKey());
                            object.put(AnnotationData.DRAW_TYPE, "0");
                            object.put(AnnotationData.TYPE, "Line");
                            object.put(AnnotationData.COLOR, String.valueOf(StickerView.colorCode));

                            start.put(AnnotationData.X, point.x);
                            start.put(AnnotationData.Y, point.y);

                            end.put(AnnotationData.X, pointEnd.x);
                            end.put(AnnotationData.Y, pointEnd.y);

                            object.put(AnnotationData.START, start);
                            object.put(AnnotationData.END, end);

                            annotateData.put(object);
                            object = new JSONObject();
                            start = new JSONObject();
                            end = new JSONObject();
                        }
                    } else if (cirRectPoints.size() > 0) {
                        for (int i = 1; i <= cirRectPoints.size(); i++) {
                            StickerView.Point point = null;
                            object.put(AnnotationData.BADGE, i);
                            object.put(AnnotationData.DRAW_TYPE, "1");
                            object.put(AnnotationData.TYPE, "Circle");
                            object.put(AnnotationData.COLOR, String.valueOf(StickerView.colorCode));
                            point = cirRectPoints.get(i - 1);
                            start.put(AnnotationData.X, point.x);
                            start.put(AnnotationData.Y, point.y);
                            object.put(AnnotationData.START, start);

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
                int annotationCount = cirRectPoints.size() > 0 ? cirRectPoints.size() : StickerView.badgeCount;
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
                        Set<Map.Entry<Integer, ArrayList<StickerView.Point>>> mappings = straightLinePoints.entrySet();
                        StickerView.Point point = null;
                        StickerView.Point pointEnd = null;
                        for (Map.Entry<Integer, ArrayList<StickerView.Point>> entry : mappings) {
                            point = entry.getValue().get(0);
                            pointEnd = entry.getValue().get(1);
                            object.put(AnnotationData.BADGE, entry.getKey());
                            object.put(AnnotationData.DRAW_TYPE, "0");
                            object.put(AnnotationData.TYPE, "Line");
                            object.put(AnnotationData.COLOR, String.valueOf(StickerView.colorCode));

                            start.put(AnnotationData.X, point.x);
                            start.put(AnnotationData.Y, point.y);

                            end.put(AnnotationData.X, pointEnd.x);
                            end.put(AnnotationData.Y, pointEnd.y);

                            object.put(AnnotationData.START, start);
                            object.put(AnnotationData.END, end);

                            annotateData.put(object);
                            object = new JSONObject();
                            start = new JSONObject();
                            end = new JSONObject();
                        }
                    } else if (cirRectPoints.size() > 0) {
                        for (int i = 1; i <= cirRectPoints.size(); i++) {
                            StickerView.Point point = null;
                            object.put(AnnotationData.BADGE, i);
                            object.put(AnnotationData.DRAW_TYPE, "1");
                            object.put(AnnotationData.TYPE, "Circle");
                            object.put(AnnotationData.COLOR, String.valueOf(StickerView.colorCode));
                            point = cirRectPoints.get(i - 1);
                            start.put(AnnotationData.X, point.x);
                            start.put(AnnotationData.Y, point.y);
                            object.put(AnnotationData.START, start);

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
                int annotationCount = cirRectPoints.size() > 0 ? cirRectPoints.size() : StickerView.badgeCount;
                returnIntent.putExtra(ANNOTATION_COUNT, "" + annotationCount);
                returnIntent.putExtra(ANNOTATION_DATA, annotateData.toString());
                mContext.setResult(RESULT_OK, returnIntent);
            }//end if
            mContext.finish();
            if (dialog != null) {
                dialog.dismiss();
            }

            saveBtn.setEnabled(true);
        }
    }

}