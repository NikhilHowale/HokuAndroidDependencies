package com.hokuapps.loadnativefileupload.annotate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class TurboImageViewFree extends View {

    private static final float TOUCH_TOLERANCE = 4;
    public static String colorCode = "#FF0000";
    public int width;
    public int height;
    Context context;
    private Path path = new Path();
    private ArrayList<Path> paths = new ArrayList<Path>();
    private Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint circlePaint;
    private Path circlePath;
    private float mX, mY;

    public TurboImageViewFree(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TurboImageViewFree(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TurboImageViewFree(Context c) {
        super(c);
        context = c;
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setListener(TurboImageViewListener TurboImageViewFreeListener) {
        brush.setStyle(Paint.Style.STROKE);
        brush.setColor(Color.RED);
        brush.setStrokeWidth(10);

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);
    }

    public void clearAllDrawData() {
        paths.clear();
        mPath.reset();
        this.invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            for (Path path : paths) {
                canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                canvas.drawPath(path, brush);
                canvas.drawPath(circlePath, circlePaint);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            mPath.lineTo(x,y);
            paths.add(mPath);
            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        paths.add(mPath);
        invalidate();
        circlePath.reset();
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, brush);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        brush.setColor(Color.parseColor(colorCode));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                mPath = new Path();
                break;
        }
        return true;
    }
}
