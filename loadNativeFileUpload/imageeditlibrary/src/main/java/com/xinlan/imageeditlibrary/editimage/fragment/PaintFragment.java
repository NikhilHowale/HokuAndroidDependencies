package com.xinlan.imageeditlibrary.editimage.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.EditImageActivity;
import com.xinlan.imageeditlibrary.editimage.adapter.ColorListAdapter;
import com.xinlan.imageeditlibrary.editimage.task.StickerTask;
import com.xinlan.imageeditlibrary.editimage.ui.ColorPicker;
import com.xinlan.imageeditlibrary.editimage.view.CustomPaintView;
import com.xinlan.imageeditlibrary.editimage.view.PaintModeView;

public class PaintFragment extends Fragment implements View.OnClickListener, ColorListAdapter.IColorListAction {
    public static final int INDEX = 6;
    public static final String TAG = PaintFragment.class.getName();

    private View mainView;
    private EditImageActivity activity;
    private View backToMenu;
    private PaintModeView mPaintModeView;
    private RecyclerView mColorListView;
    private ColorListAdapter mColorAdapter;
    private View popView;

    private CustomPaintView mPaintView;

    private ColorPicker mColorPicker;

    private PopupWindow setStokenWidthWindow;
    private SeekBar mStokenWidthSeekBar;

    private ImageView mEraserView;

    public boolean isEraser = false;

    private SaveCustomPaintTask mSavePaintImageTask;

    public int[] mPaintColors = {Color.BLACK,
            Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.WHITE,
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};

    public static PaintFragment newInstance(EditImageActivity activity) {
        PaintFragment fragment = new PaintFragment();
        fragment.activity = activity;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPaintView = activity.mPaintView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_edit_paint, null);
        backToMenu = mainView.findViewById(R.id.back_to_main);
        mPaintModeView = (PaintModeView) mainView.findViewById(R.id.paint_thumb);
        mColorListView = (RecyclerView) mainView.findViewById(R.id.paint_color_list);
        mEraserView = (ImageView) mainView.findViewById(R.id.paint_eraser);
        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        backToMenu.setOnClickListener(this);// 返回主菜单

        mColorPicker = new ColorPicker(getActivity(), 255, 0, 0);
        initColorListView();
        mPaintModeView.setOnClickListener(this);

        initStokeWidthPopWindow();

        mEraserView.setOnClickListener(this);
        updateEraserView();
    }

    /**
     * initColorListView
     */
    private void initColorListView() {

        mColorListView.setHasFixedSize(false);

        LinearLayoutManager stickerListLayoutManager = new LinearLayoutManager(activity);
        stickerListLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        mColorListView.setLayoutManager(stickerListLayoutManager);
        mColorAdapter = new ColorListAdapter(this, mPaintColors, this);
        mColorListView.setAdapter(mColorAdapter);


    }

    @Override
    public void onClick(View v) {
        if (v == backToMenu) {//back button click
            backToMain();
        } else if (v == mPaintModeView) {
            setStokeWidth();
        } else if (v == mEraserView) {
            toggleEraserView();
        }//end if
    }

    /**
     * backToMain
     */
    public void backToMain() {
        activity.mode = EditImageActivity.MODE_NONE;
        activity.bottomGallery.setCurrentItem(MainMenuFragment.INDEX);
        activity.mainImage.setVisibility(View.VISIBLE);
        activity.bannerFlipper.showPrevious();

        this.mPaintView.setVisibility(View.GONE);
    }

    public void onShow() {
        activity.mode = EditImageActivity.MODE_PAINT;
        activity.bottomGallery.setCurrentItem(PaintFragment.INDEX);
        activity.mainImage.setImageBitmap(activity.mainBitmap);
        activity.bannerFlipper.showNext();

        this.mPaintView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onColorSelected(int position, int color) {
        setPaintColor(color);
    }

    @Override
    public void onMoreSelected(int position) {
        mColorPicker.show();
        Button okColor = (Button) mColorPicker.findViewById(R.id.okColorButton);
        okColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPaintColor(mColorPicker.getColor());
                mColorPicker.dismiss();
            }
        });
    }

    /**
     * setPaintColor
     *
     * @param paintColor
     */
    protected void setPaintColor(final int paintColor) {
        mPaintModeView.setPaintStrokeColor(paintColor);

        updatePaintView();
    }

    private void updatePaintView() {
        isEraser = false;
        updateEraserView();

        this.mPaintView.setColor(mPaintModeView.getStokenColor());
        this.mPaintView.setWidth(mPaintModeView.getStokenWidth());
    }

    /**
     * setStokeWidth
     * show popwidnow to set paint width
     */
    protected void setStokeWidth() {
        if (popView.getMeasuredHeight() == 0) {
            popView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        }

        mStokenWidthSeekBar.setMax(mPaintModeView.getMeasuredHeight() / 2);

        mStokenWidthSeekBar.setProgress((int) mPaintModeView.getStokenWidth());

        mStokenWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPaintModeView.setPaintStrokeWidth(progress);
                updatePaintView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        int[] locations = new int[2];
        activity.bottomGallery.getLocationOnScreen(locations);
        setStokenWidthWindow.showAtLocation(activity.bottomGallery,
                Gravity.NO_GRAVITY, 0, locations[1] - popView.getMeasuredHeight());
    }

    private void initStokeWidthPopWindow() {
        popView = LayoutInflater.from(activity).
                inflate(R.layout.view_set_stoke_width, null);
        setStokenWidthWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.WRAP_CONTENT);

        mStokenWidthSeekBar = (SeekBar) popView.findViewById(R.id.stoke_width_seekbar);

        setStokenWidthWindow.setFocusable(true);
        setStokenWidthWindow.setOutsideTouchable(true);
        setStokenWidthWindow.setBackgroundDrawable(new BitmapDrawable());
        setStokenWidthWindow.setAnimationStyle(R.style.popwin_anim_style);


        mPaintModeView.setPaintStrokeColor(Color.RED);
        mPaintModeView.setPaintStrokeWidth(10);

        updatePaintView();
    }

    private void toggleEraserView() {
        isEraser = !isEraser;
        updateEraserView();
    }

    private void updateEraserView() {
        mEraserView.setImageResource(isEraser ? R.drawable.eraser_seleced : R.drawable.eraser_normal);
        mPaintView.setEraser(isEraser);
    }

    /**
     * savePaintImage
     */
    public void savePaintImage() {
        if (mSavePaintImageTask != null && !mSavePaintImageTask.isCancelled()) {
            mSavePaintImageTask.cancel(true);
        }

        mSavePaintImageTask = new SaveCustomPaintTask(activity);
        mSavePaintImageTask.execute(activity.mainBitmap);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSavePaintImageTask != null && !mSavePaintImageTask.isCancelled()) {
            mSavePaintImageTask.cancel(true);
        }
    }

    /**
     * 文字合成任务
     * 合成最终图片
     */
    private final class SaveCustomPaintTask extends StickerTask {

        public SaveCustomPaintTask(EditImageActivity activity) {
            super(activity);
        }

        @Override
        public void handleImage(Canvas canvas, Matrix m) {
            float[] f = new float[9];
            m.getValues(f);
            int dx = (int) f[Matrix.MTRANS_X];
            int dy = (int) f[Matrix.MTRANS_Y];
            float scale_x = f[Matrix.MSCALE_X];
            float scale_y = f[Matrix.MSCALE_Y];
            canvas.save();
            canvas.translate(dx, dy);
            canvas.scale(scale_x, scale_y);

            if (mPaintView.getPaintBit() != null) {
                canvas.drawBitmap(mPaintView.getPaintBit(), 0, 0, null);
            }
            canvas.restore();
        }

        @Override
        public void onPostResult(Bitmap result) {
            mPaintView.reset();

            activity.changeMainBitmap(result);

            backToMain();
        }
    }//end inner class

}// end class
