package com.xinlan.imageeditlibrary.editimage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.xinlan.imageeditlibrary.BaseActivity;
import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.fragment.AddTextFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.CropFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.FliterListFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.MainMenuFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.PaintFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.RotateFragment;
import com.xinlan.imageeditlibrary.editimage.fragment.StirckerFragment;
import com.xinlan.imageeditlibrary.editimage.utils.FileUtil;
import com.xinlan.imageeditlibrary.editimage.view.CropImageView;
import com.xinlan.imageeditlibrary.editimage.view.CustomPaintView;
import com.xinlan.imageeditlibrary.editimage.view.CustomViewPager;
import com.xinlan.imageeditlibrary.editimage.view.RotateImageView;
import com.xinlan.imageeditlibrary.editimage.view.StickerView;
import com.xinlan.imageeditlibrary.editimage.view.TextStickerView;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouch;
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils;
import com.xinlan.imageeditlibrary.editimage.view.imagezoom.ImageViewTouchBase;

public class EditImageActivity extends BaseActivity {
    public static final String FILE_PATH = "file_path";
    public static final String EXTRA_OUTPUT = "extra_output";
    public static final String EXTRA_TOOLBAR_COLOR = "extra_toolbar_color";

    public static final String SAVE_FILE_PATH = "save_file_path";

    public static final String IMAGE_IS_EDIT = "image_is_edit";

    public static final int MODE_NONE = 0;
    public static final int MODE_STICKERS = 1;
    public static final int MODE_FILTER = 2;
    public static final int MODE_CROP = 3;
    public static final int MODE_ROTATE = 4;
    public static final int MODE_TEXT = 5;
    public static final int MODE_PAINT = 6;

    public String filePath;
    public String saveFilePath;
    public String toolbarColor = "#333333";
    private int imageWidth, imageHeight;
    private LoadImageTask mLoadImageTask;

    public int mode = MODE_NONE;
    private EditImageActivity mContext;
    public Bitmap mainBitmap;
    public ImageViewTouch mainImage;
    private View backBtn;

    public ViewFlipper bannerFlipper;
    public FrameLayout banner;

    private View applyBtn;
    private View saveBtn;

    public StickerView mStickerView;
    public CropImageView mCropPanel;
    public RotateImageView mRotatePanel;
    public TextStickerView mTextStickerView;
    public CustomPaintView mPaintView;

    public CustomViewPager bottomGallery;
    private BottomGalleryAdapter mBottomGalleryAdapter;
    private MainMenuFragment mMainMenuFragment;
    public StirckerFragment mStirckerFragment;
    public FliterListFragment mFliterListFragment;
    public CropFragment mCropFragment;
    public RotateFragment mRotateFragment;
    public AddTextFragment mAddTextFragment;
    public PaintFragment mPaintFragment;

    /**
     *
     * @param context
     * @param editImagePath
     * @param outputPath
     * @param requestCode
     */
    public static void start(Activity context,final String editImagePath,final String outputPath,final int requestCode){
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, EditImageActivity.class);
        it.putExtra(EditImageActivity.FILE_PATH, editImagePath);
        it.putExtra(EditImageActivity.EXTRA_OUTPUT, outputPath);
        context.startActivityForResult(it, requestCode);
    }

    /**
     *
     * @param context
     * @param editImagePath
     * @param outputPath
     * @param requestCode
     */
    public static void start(Activity context,final String editImagePath,final String outputPath, String toolbarColor ,final int requestCode){
        if (TextUtils.isEmpty(editImagePath)) {
            Toast.makeText(context, R.string.no_choose, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent it = new Intent(context, EditImageActivity.class);
        it.putExtra(EditImageActivity.FILE_PATH, editImagePath);
        it.putExtra(EditImageActivity.EXTRA_OUTPUT, outputPath);
        it.putExtra(EditImageActivity.EXTRA_TOOLBAR_COLOR, toolbarColor);
        context.startActivityForResult(it, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkInitImageLoader();
        setContentView(R.layout.activity_image_edit);
        initView();
        getData();
        //setToolbarBackgroundColor();
    }

    private void getData() {
        filePath = getIntent().getStringExtra(FILE_PATH);
        saveFilePath = getIntent().getStringExtra(EXTRA_OUTPUT);

        /*if (getIntent().getExtras().containsKey(EXTRA_TOOLBAR_COLOR)) {
            toolbarColor = getIntent().getStringExtra(EXTRA_TOOLBAR_COLOR);
        }*/

        loadImage(filePath);
    }

    private void setToolbarBackgroundColor() {
        try {
            if (banner != null) {
                banner.setBackgroundColor(Color.parseColor(toolbarColor));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initView() {
        mContext = this;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels / 2;
        imageHeight = metrics.heightPixels / 2;

        bannerFlipper = (ViewFlipper) findViewById(R.id.banner_flipper);
        bannerFlipper.setInAnimation(this, R.anim.in_bottom_to_top);
        bannerFlipper.setOutAnimation(this, R.anim.out_bottom_to_top);
        banner = (FrameLayout) findViewById(R.id.banner);

        applyBtn = findViewById(R.id.apply);
        applyBtn.setOnClickListener(new ApplyBtnClick());
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new SaveBtnClick());

        mainImage = (ImageViewTouch) findViewById(R.id.main_image);
        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                forceReturnBack();
            }
        });

        mStickerView = (StickerView) findViewById(R.id.sticker_panel);
        mCropPanel = (CropImageView) findViewById(R.id.crop_panel);
        mRotatePanel = (RotateImageView) findViewById(R.id.rotate_panel);
        mTextStickerView = (TextStickerView) findViewById(R.id.text_sticker_panel);
        mPaintView = (CustomPaintView) findViewById(R.id.custom_paint_view);

        // gallery
        bottomGallery = (CustomViewPager) findViewById(R.id.bottom_gallery);
        //bottomGallery.setOffscreenPageLimit(5);
        mMainMenuFragment = MainMenuFragment.newInstance(this);
        mBottomGalleryAdapter = new BottomGalleryAdapter(
                this.getSupportFragmentManager());
        mStirckerFragment = StirckerFragment.newInstance(this);
        mFliterListFragment = FliterListFragment.newInstance(this);
        mCropFragment = CropFragment.newInstance(this);
        mRotateFragment = RotateFragment.newInstance(this);
        mAddTextFragment = AddTextFragment.newInstance(this);
        mPaintFragment = PaintFragment.newInstance(this);

        bottomGallery.setAdapter(mBottomGalleryAdapter);


        mainImage.setFlingListener(new ImageViewTouch.OnImageFlingListener() {
            @Override
            public void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //System.out.println(e1.getAction() + " " + e2.getAction() + " " + velocityX + "  " + velocityY);
                if (velocityY > 1) {
                    closeInputMethod();
                }
            }
        });
    }

    /**
     *
     */
    private void closeInputMethod() {
        if (mAddTextFragment.isAdded()) {
            mAddTextFragment.hideInput();
        }
    }

    /**
     *
     */
    private final class BottomGalleryAdapter extends FragmentPagerAdapter {
        public BottomGalleryAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int index) {
            // System.out.println("createFragment-->"+index);
            switch (index) {
                case MainMenuFragment.INDEX:
                    return mMainMenuFragment;
                case StirckerFragment.INDEX:
                    return mStirckerFragment;
                case FliterListFragment.INDEX:
                    return mFliterListFragment;
                case CropFragment.INDEX:
                    return mCropFragment;
                case RotateFragment.INDEX:
                    return mRotateFragment;
                case AddTextFragment.INDEX:
                    return mAddTextFragment;
                case PaintFragment.INDEX:
                    return mPaintFragment;
            }//end switch
            return MainMenuFragment.newInstance(mContext);
        }

        @Override
        public int getCount() {
            return 7;
        }
    }// end inner class

    /**
     * Load image from filepath
     *
     * @param filepath
     */
    public void loadImage(String filepath) {
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }
        mLoadImageTask = new LoadImageTask();
        mLoadImageTask.execute(filepath);
    }

    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {

            return BitmapUtils.getSampledBitmap(params[0], imageWidth,
                    imageHeight);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mainBitmap != null) {
                mainBitmap.recycle();
                mainBitmap = null;
                System.gc();
            }
            mainBitmap = result;
            mainImage.setImageBitmap(result);
            mainImage.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
            // mainImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
        }
    }// end inner class

    /**
     * on key down event
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (mode) {
                case MODE_STICKERS:
                    mStirckerFragment.backToMain();
                    return true;
                case MODE_FILTER:
                    mFliterListFragment.backToMain();
                    return true;
                case MODE_CROP:
                    mCropFragment.backToMain();
                    return true;
                case MODE_ROTATE:
                    mRotateFragment.backToMain();
                    return true;
            }// end switch

            forceReturnBack();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * forceReturnBack
     */
    private void forceReturnBack() {
        setResult(RESULT_CANCELED);
        this.finish();
    }

    /**
     ApplyBtnClick
     */
    private final class ApplyBtnClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (mode) {
                case MODE_STICKERS:
                    mStirckerFragment.saveStickers();// sticker
                    break;
                case MODE_FILTER:// mode filter
                    mFliterListFragment.saveFilterImage();
                    break;
                case MODE_CROP:
                    mCropFragment.saveCropImage();
                    break;
                case MODE_ROTATE:
                    mRotateFragment.saveRotateImage();
                    break;
                case MODE_TEXT:
                    mAddTextFragment.saveTextImage();
                    break;
                case MODE_PAINT:
                    mPaintFragment.savePaintImage();
                    break;
                default:
                    break;
            }// end switch
        }
    }// end inner class

    /**
     * SaveBtnClick
     *
     * @author panyi
     */
    private final class SaveBtnClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            Intent returnIntent = new Intent();
            if(FileUtil.checkFileExist(saveFilePath)){
                returnIntent.putExtra(SAVE_FILE_PATH, saveFilePath);
                returnIntent.putExtra(IMAGE_IS_EDIT, true);

                FileUtil.ablumUpdate(mContext,saveFilePath);

                mContext.setResult(RESULT_OK, returnIntent);
            }else{
                returnIntent.putExtra(SAVE_FILE_PATH, filePath);
                returnIntent.putExtra(IMAGE_IS_EDIT, false);
                mContext.setResult(RESULT_OK, returnIntent);
            }//end if
            mContext.finish();
        }
    }// end inner class

    /**
     * change Bitmap
     *
     * @param newBit
     */
    public void changeMainBitmap(Bitmap newBit) {
        if (mainBitmap != null) {
            if (!mainBitmap.isRecycled()) {// 回收
                mainBitmap.recycle();
            }
            mainBitmap = newBit;
        } else {
            mainBitmap = newBit;
        }// end if
        mainImage.setImageBitmap(mainBitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }
    }
}// end class
