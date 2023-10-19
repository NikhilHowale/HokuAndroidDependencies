package com.hokuapps.Loadnativeqrcodescannerupload.barcodereader.ui.camera;


import static com.hokuapps.Loadnativeqrcodescannerupload.utils.AppConstant.headerButtonColor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.hokuapps.Loadnativeqrcodescannerupload.R;


/**
 * Created by ravi on 04/05/17.
 */

public class ScannerOverlay extends ViewGroup {
    private float left, top, endY, centerBoxLeft;
    private int rectWidth, rectHeight;
    private int frames;
    private boolean revAnimation;
    private int lineColor, lineWidth;
    private Paint eraser;
    private Paint paintWhiteLine;
    private Paint paintBoxCorners;
    private final float boxCornerWidth = 10.0f;
    private final float whiteLineStroke = 2.0f;
    private float centerLineHeight = 60;
    private Paint paintShader;
    private int boxLine = 40;


    public ScannerOverlay(Context context) {
        super(context);
    }

    public ScannerOverlay(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initView(context,attrs);

    }

    public ScannerOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initView(context,attrs);
    }

    private void initView(Context context,AttributeSet attrs) {
//        Init typed array.
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScannerOverlay,
                0, 0);
        rectWidth = (int) a.getDimension(R.styleable.ScannerOverlay_square_width, getResources().getDimension(R.dimen.scanner_rect_width));
        rectHeight = (int) a.getDimension(R.styleable.ScannerOverlay_square_height, getResources().getDimension(R.dimen.scanner_rect_height));
        lineColor = a.getColor(R.styleable.ScannerOverlay_line_color, ContextCompat.getColor(context, R.color.color_accent_700));

        lineWidth = a.getInteger(R.styleable.ScannerOverlay_line_width, getResources().getInteger(R.integer.line_width));
        frames = a.getInteger(R.styleable.ScannerOverlay_line_speed, getResources().getInteger(R.integer.line_width));
        centerLineHeight = lineWidth;


        eraser = new Paint();
        eraser.setAntiAlias(true);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

//        White line for inner box line
        paintWhiteLine = new Paint();
        paintWhiteLine.setAntiAlias(true);
        paintWhiteLine.setStrokeWidth(whiteLineStroke);
        paintWhiteLine.setColor(Color.WHITE);

//        Paint for drawing box corners
        paintBoxCorners = new Paint();
        paintBoxCorners.setAntiAlias(true);
        paintBoxCorners.setStrokeCap(Paint.Cap.SQUARE);
//        paintBoxCorners.setColor(lineColor);

        if (!TextUtils.isEmpty(headerButtonColor)) {
            paintBoxCorners.setColor(Color.parseColor(headerButtonColor));
        } else {
            paintBoxCorners.setColor(Color.RED);
        }

        paintBoxCorners.setStrokeWidth(boxCornerWidth);

//        Shader for center line/rect.
        Shader shader = new LinearGradient(0, 0, left + dpToPx(rectWidth), 0, Color.RED, Color.RED, Shader.TileMode.MIRROR);
        paintShader = new Paint();
        paintShader.setShader(shader);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        left = (w - dpToPx(rectWidth)) / 2;
        top = (h - dpToPx(rectHeight)) / 2;
        endY = top;
        centerBoxLeft = left;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw transparent rect
        int cornerRadius = 0;


        RectF rect = new RectF(left, top, dpToPx(rectWidth) + left, dpToPx(rectHeight) + top);
        canvas.drawRoundRect(rect, (float) cornerRadius, (float) cornerRadius, eraser);


        // draw horizontal line
        Paint line = new Paint();
        line.setColor(lineColor);
        line.setStrokeWidth((float) lineWidth);

        // draw the line to product animation
//        subtract centerLineHeight so that, line won't goes out of bounds of scanning box.

        if (endY + frames + centerLineHeight >= top + dpToPx(rectHeight)) {
//            revAnimation = true;
            revAnimation = false;
            endY = top;
        } else if (endY == top + frames + centerLineHeight) {
            revAnimation = false;
        }

        // check if the line has reached to bottom
        if (revAnimation) {
            endY -= frames;
            centerBoxLeft -= frames;

        } else {
            endY += frames;
            centerBoxLeft += frames;

        }
//        Draw center horizontal animating line.
//        canvas.drawLine(left, endY, left + dpToPx(rectWidth), endY, line);
//        Shader shader = new LinearGradient(left, top, left + dpToPx(rectWidth), top + frames, Color.RED, Color.RED, Shader.TileMode.CLAMP);
         Shader shader = new LinearGradient(0, 0, left + dpToPx(rectWidth), 0, Color.RED, Color.GRAY, Shader.TileMode.MIRROR);

        Paint paint = new Paint();
        paint.setShader(shader);
        canvas.drawRect(new RectF(left, endY + frames, left + dpToPx(rectWidth), endY + frames + centerLineHeight), paintShader);

//        Draw white lines.
        drawBoxWhiteLines(canvas, boxLine);

        //        Draw box corners.
//        a (Top left) -----
        //- (boxCornerWidth / 2) + whiteLineStroke
        canvas.drawLine(left, top , left + dpToPx(boxLine), top, paintBoxCorners);


//        b |
//          |
        canvas.drawLine(left, top, left, top + dpToPx(boxLine), paintBoxCorners);

//        c ----- (Top right)
        canvas.drawLine(left + dpToPx(rectHeight), top, (left + dpToPx(rectHeight)) - dpToPx(boxLine), top, paintBoxCorners);

//        d |
//          |
        canvas.drawLine(left + dpToPx(rectHeight), top, (left + dpToPx(rectHeight)), top + dpToPx(boxLine), paintBoxCorners);


//        e ----- (bottom right)

        canvas.drawLine(left + dpToPx(rectWidth), top + dpToPx(rectHeight), left + dpToPx(rectWidth) - dpToPx(boxLine), top + dpToPx(rectHeight), paintBoxCorners);
//        f |
//          |
        canvas.drawLine(left + dpToPx(rectHeight), top + dpToPx(rectHeight), left + dpToPx(rectHeight), top + dpToPx(rectHeight) - dpToPx(boxLine), paintBoxCorners);


//        g (bottom left) -----
        canvas.drawLine(left, top + dpToPx(rectHeight), left + dpToPx(boxLine), (top + dpToPx(rectHeight)), paintBoxCorners);

//        h |
//          |
        canvas.drawLine(left, top + dpToPx(rectHeight), left, (top + dpToPx(rectHeight)) - dpToPx(boxLine), paintBoxCorners);


        invalidate();
    }

    /**
     * This will draw white lines for the inner box.
     *
     * @param canvas draw rect with canvas that pass
     * @param boxLine border of rect shape
     */
    private void drawBoxWhiteLines(Canvas canvas, int boxLine) {
//        ========================== Draw white lines ===================

//        Top white line.
        canvas.drawLine(left + dpToPx(boxLine), top, (left + dpToPx(rectHeight)) - dpToPx(boxLine), top, paintWhiteLine);
//        Right vertical white line.
        canvas.drawLine(left + dpToPx(rectWidth), top + dpToPx(boxLine), (left + dpToPx(rectWidth)), top + dpToPx(rectHeight) - dpToPx(boxLine), paintWhiteLine);
//        Bottom horizontal white line.
        canvas.drawLine(left + dpToPx(rectWidth) - dpToPx(boxLine), top + dpToPx(rectHeight), left + dpToPx(boxLine), top + dpToPx(rectHeight), paintWhiteLine);
//        Left vertical white line.
        canvas.drawLine(left, (top + dpToPx(rectHeight)) - dpToPx(boxLine), left, (top + dpToPx(boxLine)), paintWhiteLine);
    }
}