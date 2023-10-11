package com.hokuapps.loadnativefileupload.annotate;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageObject extends MultiTouchObject {
    private static final double INITIAL_SCALE_FACTOR = 0.15;

    private transient Drawable drawable;

    int count =0;

    public ImageObject(int resourceId, Resources res) {
        super(res);
        drawable = res.getDrawable(resourceId);
        initPaint();
    }

    public ImageObject(Drawable drawable, Resources res) {
        super(res);
        this.drawable = drawable;
        initPaint();
    }

    public ImageObject(Bitmap bitmap, Resources res, int count) {
        super(res);
        this.drawable = new BitmapDrawable(res, bitmap);
        this.count=count;
        initPaint();
    }

    /**
     * Initialize paint object
     */
    public void initPaint() {
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(borderColor);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(3.0f);
        borderPaint.setTextSize(100);
        borderPaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));

       //Number Paint
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.RED);
        textPaint.setStrokeWidth(10.0f);
        textPaint.setTextSize(50);
    }

    /**
     * Start drawing on canvas
     * @param canvas using canvas draw a bitmap
     */
    public void draw(Canvas canvas) {
        canvas.save();

        float dx = (maxX + minX) / 2;
        float dy = (maxY + minY) / 2;

        drawable.setBounds((int) minX, (int) minY, (int) maxX, (int) maxY);
        if (flippedHorizontally) {
            canvas.scale(-1f, 1f, dx, dy);
        }
        canvas.translate(dx, dy);
        if (flippedHorizontally) {
            canvas.rotate(-angle * 180.0f / (float) Math.PI);
        } else {
            canvas.rotate(angle * 180.0f / (float) Math.PI);
        }
        canvas.translate(-dx, -dy);

        canvas.drawText(String.valueOf(count),getMaxX()+10,getMaxY()+10,textPaint); // added 100 to set the number text  to the bottom of the image

        drawable.draw(canvas);

        canvas.restore();
    }

    /**
     * Called by activity's onPause() method to free memory used for loading the images
     */
    @Override
    public void unload() {
        this.drawable = null;
    }


    /** Called by activity's onResume() method to init the images */
    @Override
    public void init(Context context, float startMidX, float startMidY) {
        Resources res = context.getResources();
        init(res);

        this.startMidX = startMidX;
        this.startMidY = startMidY;

        width = drawable.getIntrinsicWidth();
        height = drawable.getIntrinsicHeight();

        float centerX;
        float centerY;
        float scaleX;
        float scaleY;
        float angle;
        if (firstLoad) {
            centerX = startMidX;
            centerY = startMidY;

            float scaleFactor = (float) (Math.max(displayWidth, displayHeight) /
                (float) Math.max(width, height) * INITIAL_SCALE_FACTOR);
            scaleX = scaleY = scaleFactor;
            angle = 0.0f;

            firstLoad = false;
        } else {
            centerX = this.centerX;
            centerY = this.centerY;
            scaleX = this.scaleX;
            scaleY = this.scaleY;
            angle = this.angle;
        }
        setPos(centerX, centerY, scaleX, scaleY, angle);
    }
}
