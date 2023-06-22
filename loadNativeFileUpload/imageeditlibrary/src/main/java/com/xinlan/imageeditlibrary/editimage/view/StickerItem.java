package com.xinlan.imageeditlibrary.editimage.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.xinlan.imageeditlibrary.R;


public class StickerItem {

    public static final int PADDING = 32;
    private static final float MIN_SCALE = 0.15f;
    private static final int HELP_BOX_PAD = 0;
    private static final int BUTTON_WIDTH = Constants.STICKER_BTN_HALF_SIZE;
    public static float TEXT_SIZE_DEFAULT = 60f;
    //private static final int BUTTON_WIDTH_DELETE = 30;
    public static Context context;
    //private static Bitmap deleteBit;
    private static Bitmap rotateBit;
    public Bitmap bitmap;
    public Rect srcRect;
    public RectF dstRect;
    public RectF leftTopCircleRect;
    public RectF rightBottomCircleRect;
    public RectF leftBottomCircleRect;
    public RectF rightTopCircleRect;
    public String mText;
    public RectF helpBox;
    // public RectF deleteCircleRect;
    public Matrix matrix;
    public float rotateAngle = 0;
    public boolean isDrawHelpTool = false;
    public Paint helpBoxPaint = new Paint();
    public Paint paint = new Paint();
    public RectF detectRightTopRect;
    public RectF detectLeftBottomRect;
    public RectF detectRightBottomRect;
    public RectF detectLeftTopRect;
    public int stickerItemId = -1;
    public Path drawPath = new Path();
    private String circleCounts;
    private Rect helpToolsRect;
    private Rect mTextRect = new Rect();// warp text rect record
    private TextPaint mTextPaint = new TextPaint();
    private float circleX, circleY;
    // public RectF detectDeleteRect;

    public StickerItem(Context context) {

        //init text size from dimes

        TEXT_SIZE_DEFAULT = context.getResources().getDimension(R.dimen.text_size_caption);

        helpBoxPaint.setColor(Color.BLACK);
        helpBoxPaint.setStyle(Style.STROKE);
        helpBoxPaint.setAntiAlias(true);
        helpBoxPaint.setStrokeWidth(3);
        helpBoxPaint.setStrokeJoin(Paint.Join.ROUND);

        paint.setColor(Color.parseColor(StickerView.colorCode));
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(5);

        // end if
        if (rotateBit == null) {
            rotateBit = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.sticker_rotate);
        }// end if

        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(30);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        StickerItem.context = context;
    }

    public int getStickerItemId() {
        return stickerItemId;
    }

    public void setStickerItemId(int stickerItemId) {
        this.stickerItemId = stickerItemId;
    }

    public boolean isDrawHelpTool() {
        return isDrawHelpTool;
    }

    public void setDrawHelpTool(boolean drawHelpTool) {
        isDrawHelpTool = drawHelpTool;
    }

    public int getRectangleBorderColor() {
        return paint.getColor();
    }

    public void setRectangleBorderColor(int color) {
        this.paint.setColor(color);
        this.mTextPaint.setColor(color);
    }

    public void setTextColor(int newColor) {
        mTextPaint.setColor(newColor);
    }

    public void setText(String text) {
        this.mText = text;
    }

    public void init(Bitmap addBit, View parentView) {
        init(addBit, parentView, -1, -1);
    }

    /**
     * Initialize rectangle with rectangle bitmap or null if not
     *
     * @param addBit     - draw text on parent view
     * @param parentView
     * @param x
     * @param y
     */
    public void init(Bitmap addBit, View parentView, float x, float y) {

        circleX = x;
        circleY = y;

        this.bitmap = addBit;
        drawPath.lineTo(x, y);
        drawPath.moveTo(x, y);

        int bitmapWidth = addBit == null ? Constants.getRMin100Width(context) : addBit.getWidth();
        int bitmapHeight = addBit == null ? Constants.getRMin100Height(context) : addBit.getHeight();

        this.srcRect = new Rect(0, 0, bitmapWidth, bitmapHeight);
        int bitWidth = Math.min(bitmapWidth, parentView.getWidth() >> 1);
        int bitHeight = bitWidth * bitmapHeight / bitmapWidth;
        int left = x == -1 ? (parentView.getWidth() >> 1) - (bitWidth >> 1) : (int) x;
        int top = y == -1 ? (parentView.getHeight() >> 1) - (bitHeight >> 1) : (int) y;

        this.dstRect = new RectF(left, top, left + bitmapWidth, top + bitmapHeight);

        this.matrix = new Matrix();
        this.matrix.postTranslate(this.dstRect.left, this.dstRect.top);
        this.matrix.postScale((float) bitWidth / bitmapWidth,
                (float) bitHeight / bitmapHeight, this.dstRect.left,
                this.dstRect.top);

        this.isDrawHelpTool = true;
        this.helpBox = new RectF(this.dstRect);
        updateHelpBoxRect();

        /*helpToolsRect = new Rect(0, 0, deleteBit.getWidth(),
                deleteBit.getHeight());*/

        helpToolsRect = new Rect(0, 0, rotateBit.getWidth(),
                rotateBit.getHeight());

        leftTopCircleRect = new RectF(helpBox.left - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH, helpBox.left + BUTTON_WIDTH, helpBox.top
                + BUTTON_WIDTH);
        rightBottomCircleRect = new RectF(helpBox.right - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH, helpBox.right + BUTTON_WIDTH, helpBox.bottom
                + BUTTON_WIDTH);
        rightTopCircleRect = new RectF(helpBox.right - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH, helpBox.right + BUTTON_WIDTH, helpBox.top
                + BUTTON_WIDTH);
        leftBottomCircleRect = new RectF(helpBox.left - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH, helpBox.left + BUTTON_WIDTH, helpBox.bottom
                + BUTTON_WIDTH);

       /* deleteCircleRect = new RectF(helpBox.centerX() - BUTTON_WIDTH_DELETE,
                helpBox.centerY() - BUTTON_WIDTH_DELETE,
                helpBox.centerX() + BUTTON_WIDTH_DELETE,
               helpBox.centerY() + BUTTON_WIDTH_DELETE);*/

        detectRightBottomRect = new RectF(rightBottomCircleRect);
        detectLeftTopRect = new RectF(leftTopCircleRect);
        detectRightTopRect = new RectF(rightTopCircleRect);
        detectLeftBottomRect = new RectF(leftBottomCircleRect);

        // detectDeleteRect = new RectF(deleteCircleRect);
    }

    /**
     * Initialize rectangle with text
     *
     * @param text       - draw text on parent view
     * @param parentView
     * @param x
     * @param y
     */
    public void init(String text, View parentView, float x, float y) {
        this.mText = text;

        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextRect);

        int bitmapWidth = mTextRect.width() + Constants.getRMin40Width(context);
        int bitmapHeight = mTextRect.height() + Constants.getRMin40Width(context)/*40*/;

        this.srcRect = new Rect(0, 0, bitmapWidth, bitmapHeight);
        int bitWidth = Math.min(bitmapWidth, parentView.getWidth() >> 1);
        int bitHeight = bitWidth * bitmapHeight / bitmapWidth;
        int left = x == -1 ? (parentView.getWidth() >> 1) - (bitWidth >> 1) : (int) x;
        int top = y == -1 ? (parentView.getHeight() >> 1) - (bitHeight >> 1) : (int) y;

        this.dstRect = new RectF(left, top, left + bitmapWidth, top + bitmapHeight);

        this.matrix = new Matrix();
        this.matrix.postTranslate(this.dstRect.left, this.dstRect.top);
        this.matrix.postScale((float) bitWidth / bitmapWidth,
                (float) bitHeight / bitmapHeight, this.dstRect.left,
                this.dstRect.top);

        this.isDrawHelpTool = true;
        this.helpBox = new RectF(this.dstRect);
        updateHelpBoxRect();

        helpToolsRect = new Rect(0, 0, rotateBit.getWidth(),
                rotateBit.getHeight());

        leftTopCircleRect = new RectF(helpBox.left - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH, helpBox.left + BUTTON_WIDTH, helpBox.top
                + BUTTON_WIDTH);
        rightBottomCircleRect = new RectF(helpBox.right - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH, helpBox.right + BUTTON_WIDTH, helpBox.bottom
                + BUTTON_WIDTH);
        rightTopCircleRect = new RectF(helpBox.right - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH, helpBox.right + BUTTON_WIDTH, helpBox.top
                + BUTTON_WIDTH);
        leftBottomCircleRect = new RectF(helpBox.left - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH, helpBox.left + BUTTON_WIDTH, helpBox.bottom
                + BUTTON_WIDTH);

        paint.setColor(Color.RED);

        detectRightBottomRect = new RectF(rightBottomCircleRect);
        detectLeftTopRect = new RectF(leftTopCircleRect);
        detectRightTopRect = new RectF(rightTopCircleRect);
        detectLeftBottomRect = new RectF(leftBottomCircleRect);
    }

    private void updateHelpBoxRect() {
        this.helpBox.left -= HELP_BOX_PAD;
        this.helpBox.right += HELP_BOX_PAD;
        this.helpBox.top -= HELP_BOX_PAD;
        this.helpBox.bottom += HELP_BOX_PAD;
    }

    public void updatePos(final float dx, final float dy) {
        this.matrix.postTranslate(dx, dy);

        dstRect.offset(dx, dy);

        helpBox.offset(dx, dy);

        leftTopCircleRect.offset(dx, dy);
        rightBottomCircleRect.offset(dx, dy);
        leftBottomCircleRect.offset(dx, dy);
        rightTopCircleRect.offset(dx, dy);
        // deleteCircleRect.offset(dx,dy);

        this.detectRightBottomRect.offset(dx, dy);
        this.detectLeftTopRect.offset(dx, dy);
        this.detectLeftBottomRect.offset(dx, dy);
        this.detectRightTopRect.offset(dx, dy);
        // this.detectDeleteRect.offset(dx, dy);
    }

    public void scaleCropController(float xx, float yy) {

        switch (isSelectedControllerCircle(xx, yy)) {
        /*    case 1:
                dstRect.left +=  dx;
                dstRect.top += dy;
                break;
            case 2:
                dstRect.right += dx;
                dstRect.top += dy;
                break;
            case 3:
                dstRect.left += dx;
                dstRect.bottom += dy;
                break;
            case 4:
                dstRect.right += dx;
                dstRect.bottom += dy;
                break;*/

            case 1:
                dstRect.left = xx;
                dstRect.top = yy;
                break;
            case 2:
                dstRect.right = xx;
                dstRect.top = yy;
                break;
            case 3:
                dstRect.left = xx;
                dstRect.bottom = yy;
                break;
            case 4:
                dstRect.right = xx;
                dstRect.bottom = yy;
                break;
        }// end switch

        //handle for min width and height
        if (dstRect.width() < Constants.getRMin70Width(context)) {
            dstRect.right = helpBox.right;
            dstRect.left = helpBox.left;
        }

        if (dstRect.height() < Constants.getRMin70Height(context)) {
            dstRect.bottom = helpBox.bottom;
            dstRect.top = helpBox.top;
        }

        helpBox.set(dstRect);

        updateHelpBoxRect();

        rightBottomCircleRect.offsetTo(helpBox.right - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH);
        leftTopCircleRect.offsetTo(helpBox.left - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH);
        leftBottomCircleRect.offsetTo(helpBox.left - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH);
        rightTopCircleRect.offsetTo(helpBox.right - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH);

        /*deleteCircleRect.offsetTo(helpBox.centerX() - BUTTON_WIDTH_DELETE, helpBox.centerY()
                - BUTTON_WIDTH_DELETE);*/

        detectRightBottomRect.offsetTo(helpBox.right - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH);
        detectLeftTopRect.offsetTo(helpBox.left - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH);
        detectLeftBottomRect.offsetTo(helpBox.left - BUTTON_WIDTH, helpBox.bottom
                - BUTTON_WIDTH);
        detectRightTopRect.offsetTo(helpBox.right - BUTTON_WIDTH, helpBox.top
                - BUTTON_WIDTH);

        /*detectDeleteRect.offsetTo(helpBox.centerX() - BUTTON_WIDTH_DELETE,
                helpBox.centerY() - BUTTON_WIDTH_DELETE);*/

    }

    public int isSelectedControllerCircle(float x, float y) {
        if (detectLeftTopRect.contains(x, y))
            return 1;
        if (detectRightTopRect.contains(x, y))
            return 2;
        if (detectLeftBottomRect.contains(x, y))
            return 3;
        if (detectRightBottomRect.contains(x, y))
            return 4;
        return -1;
    }

    public void draw(Canvas canvas, String isDraw) {

        if (mText != null) {
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            drawText(canvas, (int) helpBox.centerX() + 20, (int) helpBox.centerY() + 20, 0.0f, 0.0f);
            if (this.isDrawHelpTool) {
                canvas.save();
                canvas.rotate(rotateAngle, helpBox.centerX(), helpBox.centerY());
                //canvas.drawRoundRect(helpBox, 10, 10, helpBoxPaint);
                //canvas.drawRect(helpBox, rectanglePaint);
                //canvas.drawBitmap(deleteBit, helpToolsRect, leftTopCircleRect, null);
                canvas.drawBitmap(rotateBit, helpToolsRect, leftTopCircleRect, null);
                canvas.drawBitmap(rotateBit, helpToolsRect, rightBottomCircleRect, null);
                canvas.drawBitmap(rotateBit, helpToolsRect, detectLeftBottomRect, null);
                canvas.drawBitmap(rotateBit, helpToolsRect, detectRightTopRect, null);
                // canvas.drawBitmap(deleteBit, helpToolsRect, detectDeleteRect, null);
                canvas.restore();

            }
        } else {
            if (isDraw.equalsIgnoreCase("Rectangle")) {
                // For Rectangle
                canvas.drawRect(helpBox, paint);
                canvas.drawText(circleCounts, circleX + 60, circleY + 35, mTextPaint);
                canvas.save();
            } else if (isDraw.equalsIgnoreCase("Circle")) {
                // For Circle
                canvas.drawCircle(circleX, circleY, 45, paint);
                canvas.drawText(circleCounts, circleX + 60, circleY + 35, mTextPaint);
                canvas.save();

            }
        }
        canvas.restore();

        // detectRightBottomRect
    }

    public String getCircleCounts() {
        return circleCounts;
    }

    public void setCircleCounts(String circleCounts) {
        this.circleCounts = circleCounts;
    }

    public void drawText(Canvas canvas, int _x, int _y, float scale, float rotate) {
        if (TextUtils.isEmpty(mText)) {
            return;
        }

        int x = _x;
        int y = _y;


        mTextRect.offset(x - (mTextRect.width() >> 1), y);

        canvas.save();
        canvas.drawText(mText, x, y, mTextPaint);
        canvas.restore();
    }
}// end class


