package com.xinlan.imageeditlibrary.editimage.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.widget.EditText;

import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.utils.RectUtil;


public class TextStickerItem {
    public static float TEXT_SIZE_DEFAULT = 50;
    public static float PADDING = 16/*32*/;


    public String mText;
    public TextPaint mPaint = new TextPaint();
    private Paint debugPaint = new Paint();
    private Paint mHelpPaint = new Paint();

    private Rect mTextRect = new Rect();// warp text rect record
    public RectF mHelpBoxRect = new RectF();
    private Rect mDeleteRect = new Rect();//删除按钮位置
    private Rect mRotateRect = new Rect();//旋转按钮位置

    public RectF mDeleteDstRect = new RectF();
    public RectF mRotateDstRect = new RectF();

    private Bitmap mDeleteBitmap;
    private Bitmap mRotateBitmap;

    public EditText mEditText;//输入控件
    public int stickerItemId = -1;
    public int layout_x = 0;
    public int layout_y = 0;

    public Context context;
    /*public float last_x = 0;
    public float last_y = 0;*/

    public float mRotateAngle = 0;
    public float mScale = 1;

    public boolean isShowHelpBox = true;
    public String hintText = "";

    public TextStickerItem(Context context) {
        initView(context, -1, -1);
    }

    private void initializeTextSizeAndPadding() {
        TEXT_SIZE_DEFAULT = context.getResources().getDimension(R.dimen.text_size_caption);
        PADDING = context.getResources().getDimension(R.dimen.text_padding);
        hintText = context.getResources().getString(R.string.add_caption_label);
    }

    public void initView(Context context, float x, float y) {
        //debugPaint.setColor(Color.parseColor("#66ff0000"));
        this.context = context;
        initializeTextSizeAndPadding();
        mDeleteBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.sticker_delete);
        mRotateBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.sticker_rotate);

        int left = (int)x;
        int top = (int) y;
        layout_x = left;
        layout_y = top;
        this.isShowHelpBox = true;

        mPaint.getTextBounds(hintText, 0, hintText.length(), mTextRect);
        mTextRect.offset(left - (mTextRect.width() >> 1), top);
        mHelpBoxRect.set(mTextRect.left - PADDING, mTextRect.top - PADDING
                , mTextRect.right + PADDING, mTextRect.bottom + PADDING);

        mDeleteRect.set(0, 0, mDeleteBitmap.getWidth(), mDeleteBitmap.getHeight());
        mRotateRect.set(0, 0, mRotateBitmap.getWidth(), mRotateBitmap.getHeight());

        mDeleteDstRect = new RectF(0, 0, Constants.getRMin40dp(context), Constants.getRMin40dp(context));
        mRotateDstRect = new RectF(0, 0, Constants.getRMin40dp(context), Constants.getRMin40dp(context));

        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(TEXT_SIZE_DEFAULT);
        mPaint.setAntiAlias(true);

        mHelpPaint.setColor(Color.BLACK);
        mHelpPaint.setStyle(Paint.Style.STROKE);
        mHelpPaint.setAntiAlias(true);
        mHelpPaint.setStrokeWidth(4);
    }


    public void drawContent(Canvas canvas) {
        drawText(canvas);

        //draw x and rotate button
        int offsetValue = ((int) mDeleteDstRect.width()) >> 1;
        mDeleteDstRect.offsetTo(mHelpBoxRect.left - offsetValue, mHelpBoxRect.top - offsetValue);
        mRotateDstRect.offsetTo(mHelpBoxRect.right - offsetValue, mHelpBoxRect.bottom - offsetValue);

        RectUtil.rotateRect(mDeleteDstRect, mHelpBoxRect.centerX(),
                mHelpBoxRect.centerY(), mRotateAngle);
        RectUtil.rotateRect(mRotateDstRect, mHelpBoxRect.centerX(),
                mHelpBoxRect.centerY(), mRotateAngle);

        if (!isShowHelpBox) {
            return;
        }

        canvas.save();
        canvas.rotate(mRotateAngle, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
        canvas.drawRoundRect(mHelpBoxRect, 10, 10, mHelpPaint);
        canvas.restore();


        canvas.drawBitmap(mDeleteBitmap, mDeleteRect, mDeleteDstRect, null);
        canvas.drawBitmap(mRotateBitmap, mRotateRect, mRotateDstRect, null);
        //canvas.drawRect(mRotateDstRect, debugPaint);
        //canvas.drawRect(mDeleteDstRect, debugPaint);
    }

    private void drawText(Canvas canvas) {
        drawText(canvas, layout_x, layout_y, mScale, mRotateAngle);
    }

    public void drawText(Canvas canvas, int _x, int _y, float scale, float rotate) {
        if (TextUtils.isEmpty(mText)) {
            return;
        }

        int x = _x;
        int y = _y;

        if (mText.length() > hintText.length()) {

            mPaint.getTextBounds(mText, 0, mText.length(), mTextRect);
            mTextRect.offset(x - (mTextRect.width() >> 1), y);

            mHelpBoxRect.set(mTextRect.left - PADDING, mTextRect.top - PADDING
                    , mTextRect.right + PADDING, mTextRect.bottom + PADDING);
        } else {
            mPaint.getTextBounds(hintText, 0, hintText.length(), mTextRect);
            mTextRect.offset(x - (mTextRect.width() >> 1), y);

            mHelpBoxRect.set(mTextRect.left - PADDING, mTextRect.top - PADDING
                    , mTextRect.right + PADDING, mTextRect.bottom + PADDING);
        }

        RectUtil.scaleRect(mHelpBoxRect, scale);

        canvas.save();
        canvas.scale(scale, scale, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
        canvas.rotate(rotate, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
        canvas.drawText(context.getResources().getString(R.string.add_caption_label).equalsIgnoreCase(mText) ? "" : mText, x, y, mPaint);
        canvas.restore();
    }


    public void clearTextContent() {
        if (mEditText != null) {
            mEditText.setText(null);
        }
        //setText(null);
    }


    /**
     * 旋转 缩放 更新
     *
     * @param dx
     * @param dy
     */
    public void updateRotateAndScale(final float dx, final float dy) {
        float c_x = mHelpBoxRect.centerX();
        float c_y = mHelpBoxRect.centerY();

        float x = mRotateDstRect.centerX();
        float y = mRotateDstRect.centerY();

        float n_x = x + dx;
        float n_y = y + dy;

        float xa = x - c_x;
        float ya = y - c_y;

        float xb = n_x - c_x;
        float yb = n_y - c_y;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        float scale = curLen / srcLen;// 计算缩放比

        mScale *= scale;
        float newWidth = mHelpBoxRect.width() * mScale;

        if (newWidth < 70) {
            mScale /= scale;
            return;
        }

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));
        float calMatrix = xa * yb - xb * ya;// 行列式计算 确定转动方向

        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        mRotateAngle += angle;
    }

    public void setTextColor(int newColor) {
        this.mPaint.setColor(newColor);
    }

    public void setEditText(EditText textView) {
            this.mEditText = textView;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public float getScale() {
        return mScale;
    }

    public float getRotateAngle() {
        return mRotateAngle;
    }
}// end class
