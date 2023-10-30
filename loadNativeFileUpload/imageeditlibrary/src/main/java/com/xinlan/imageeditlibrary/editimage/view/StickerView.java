package com.xinlan.imageeditlibrary.editimage.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.xinlan.imageeditlibrary.editimage.OnItemChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class StickerView extends View {

    public static LinkedHashMap<Integer, StickerItem> bank = new LinkedHashMap<Integer, StickerItem>();
    public static String isDraw;
    public static String colorCode;
    private static int STATUS_IDLE = 0;
    private static int STATUS_MOVE = 1;
    private static int STATUS_DELETE = 2;
    private static int STATUS_ROTATE = 3;
    public static int badgeCount = 0;
    public StickerItem currentItem;
    public TextStickerView mTextStickerView;
    private int imageCount;
    private Context mContext;
    private int currentStatus;
    private float oldx, oldy;
    private OnItemChangeListener onItemChangeListener;

    // New
    public RectF dstRect;
    public Matrix matrix;
    public static ArrayList<Point> cirRectPoints = new ArrayList<>();
    public static ArrayList<Point> startEndPoints = new ArrayList<>();
    public static HashMap<Integer, ArrayList<Point>> straightLinePoints = new HashMap<>();
    private Paint brush = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint mTextPaint = new TextPaint();
    private Path path = new Path();

    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();

    public StickerView(Context context) {
        super(context);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        brush.setStyle(Paint.Style.STROKE);
        brush.setColor(Color.parseColor(colorCode));
        mTextPaint.setAntiAlias(true);
        brush.setStrokeWidth(5);

        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(30);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public OnItemChangeListener getOnItemChangeListener() {
        return onItemChangeListener;
    }

    public void setOnItemChangeListener(OnItemChangeListener onItemChangeListener) {
        this.onItemChangeListener = onItemChangeListener;
    }

    private void init(Context context) {
        this.mContext = context;
        currentStatus = STATUS_IDLE;
    }

    public void addBitImage(final Bitmap addBit) {
        StickerItem item = new StickerItem(this.getContext());
        item.init(addBit, this);
        if (currentItem != null) {
            currentItem.isDrawHelpTool = false;
        }

        if (onItemChangeListener != null) {
            onItemChangeListener.onItemChanged(item, OnItemChangeListener.pushItemInStack);
        }

        bank.put(++imageCount, item);
        this.invalidate();
    }

    public StickerItem addBitImage(final Bitmap addBit, float X, float y, int colour) {

        StickerItem item = new StickerItem(this.getContext());

        item.setCircleCounts(String.valueOf(bank.size() + 1));

        item.init(addBit, this, X, y);

        if (currentItem != null) {
            currentItem.isDrawHelpTool = false;
        }
        currentItem = item;
        item.stickerItemId = ++imageCount;

//        if (bank.size() < 4) {
        bank.put(currentItem.stickerItemId, item);
        if (onItemChangeListener != null) {
            onItemChangeListener.onItemChanged(item, OnItemChangeListener.pushItemInStack);
        }

        this.invalidate();
//        }

        return item;
    }

    public StickerItem addText(final String text, float X, float y, int colour) {

        StickerItem item = new StickerItem(this.getContext());
        item.init(text, this, X, y);

        if (currentItem != null) {
            currentItem.isDrawHelpTool = false;
        }
        currentItem = item;
        item.stickerItemId = ++imageCount;
        bank.put(currentItem.stickerItemId, item);
        //itemStack.push(item);

        if (onItemChangeListener != null) {
            onItemChangeListener.onItemChanged(currentItem, OnItemChangeListener.pushItemInStack);
        }

        this.invalidate();

        return item;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDraw.equalsIgnoreCase("Rectangle") || isDraw.equalsIgnoreCase("Circle")) {
            //For Rectangle
            for (Integer id : bank.keySet()) {
                StickerItem item = bank.get(id);
                item.draw(canvas, isDraw);
            }// end for each

           // drawCircleRect(canvas, null, isDraw);

        } else if (isDraw.equalsIgnoreCase("Path")) {
            for (Path path : paths) {
                drawStraightLine(canvas, path, isDraw);
            }
        } else if (isDraw.equalsIgnoreCase("Line")) {
            // New
            for (Path path : paths) {
                drawStraightLine(canvas, path, isDraw);
            }
        } else {
            if (straightLinePoints != null && straightLinePoints.size() > 0) {
                Set<Map.Entry<Integer, ArrayList<Point>>> mappings = straightLinePoints.entrySet();
                Point point = null;
                for (Map.Entry<Integer, ArrayList<Point>> entry : mappings) {
                    point = ((Point) ((ArrayList) entry.getValue()).get(0));
                    if (point.drawType.equalsIgnoreCase("Line") || point.drawType.equalsIgnoreCase("Path")) {
                        drawStraightLine(canvas, path, point.drawType);
                    } else {
                        cirRectPoints.add(point);
                        drawCircleRect(canvas, null, point.drawType);
                    }
                }
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean ret = super.onTouchEvent(event);
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (isDraw.equalsIgnoreCase("Line") || isDraw.equalsIgnoreCase("Path")) {
//                    if (event.getX() < getRootView().getWidth() / 2) {
//                        startEndPoints.add(new Point(x, y, "Left"));
////                    } else if (event.getY() > getRootView().getHeight() / 2) {
////                        startEndPoints.add(new Point(x, y, "Bottom"));
//                    } else {
//                        startEndPoints.add(new Point(x, y, "Right"));
//                    }
                    ret = true;
                    path.moveTo(x, y);
                    startEndPoints.add(new Point(x, y, colorCode, isDraw));
                    badgeCount++;
                    straightLinePoints.put(badgeCount, startEndPoints);

                } else {
                    cirRectPoints.add(new Point(x, y, colorCode, isDraw));
                    invalidate();
                    int deleteId = -1;
                    for (Integer id : bank.keySet()) {
                        StickerItem item = bank.get(id);
                        if (item.isSelectedControllerCircle(x, y) > 0) {
                            ret = true;
                            if (currentItem != null) {
                                currentItem.isDrawHelpTool = false;
                            }
                            currentItem = item;
                            currentItem.isDrawHelpTool = true;
                            currentStatus = STATUS_ROTATE;
                            oldx = x;
                            oldy = y;
                        } else if (item.dstRect.contains(x, y)) {

                            ret = true;
                            if (currentItem != null) {
                                currentItem.isDrawHelpTool = false;
                            }
                            currentItem = item;
                            currentItem.isDrawHelpTool = true;
                            currentStatus = STATUS_MOVE;
                            oldx = x;
                            oldy = y;
                            if (onItemChangeListener != null) {
                                onItemChangeListener.onItemChanged(currentItem, OnItemChangeListener.reselected);
                            }

                        }// end if
                    }// end for each
                    //check if any text sticker is available check for touch on same
                    if (mTextStickerView != null) {
                        for (Integer id : mTextStickerView.bank.keySet()) {
                            TextStickerItem item = mTextStickerView.bank.get(id);

                            if (item.mDeleteDstRect.contains(x, y)
                                    || item.mHelpBoxRect.contains(x, y)
                                    || item.mRotateDstRect.contains(x, y)) {

                                if (currentItem != null) {
                                    currentItem.isDrawHelpTool = false;
                                    invalidate();
                                }
                                return super.onTouchEvent(event);
                            }

                        }
                    }
                    //change current item
                    if (onItemChangeListener != null) {
                        onItemChangeListener.onItemChanged(currentItem, OnItemChangeListener.itemChanged);
                    }

                    if (!ret && currentItem != null && currentStatus == STATUS_IDLE) {
                        currentItem.isDrawHelpTool = false;
                        currentItem = null;
                        invalidate();
                    }

                    if (deleteId > 0 && currentStatus == STATUS_DELETE) {
                        bank.remove(deleteId);
                        currentStatus = STATUS_IDLE;
                        invalidate();
                        ret = true;
                    }// end if
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDraw.equalsIgnoreCase("Line") || isDraw.equalsIgnoreCase("Path")) {
                    ret = true;
                    path.lineTo(x, y);
                }
               /*  ret = true;
                if (currentStatus == STATUS_MOVE) {
                    float dx = x - oldx;
                    float dy = y - oldy;
                    if (currentItem != null) {
                        currentItem.updatePos(dx, dy);
                        invalidate();
                    }// end if
                    oldx = x;
                    oldy = y;
                } else if (currentStatus == STATUS_ROTATE) {

                    if (currentItem != null) {
                        currentItem.scaleCropController(x, y);
                        invalidate();
                    }// end if
                    oldx = x;
                    oldy = y;
                }*/
                break;
            case MotionEvent.ACTION_UP:
                if (isDraw.equalsIgnoreCase("Line") || isDraw.equalsIgnoreCase("Path")) {
                    ret = true;
                    startEndPoints.add(new Point(x, y, colorCode, isDraw));
                    straightLinePoints.put(badgeCount, startEndPoints);
                    paths.add(path);
                    invalidate();
                    path = new Path();
                    startEndPoints = new ArrayList<>();
                }
                break;
        }// end switch

        return ret;
    }

    public LinkedHashMap<Integer, StickerItem> getBank() {
        return bank;
    }

    public void clear() {
        bank.clear();
        this.invalidate();
    }

    public void clearAllDrawData() {
        straightLinePoints.clear();
        bank.clear();
        cirRectPoints.clear();
        paths.clear();
        badgeCount = 0;
        this.invalidate();
    }

    public void onClickUndo() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            badgeCount--;
            invalidate();
        } else {

        }
        //toast the user
    }

    public void onClickRedo() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        } else {

        }
        //toast the user
    }

    public TextStickerView getmTextStickerView() {
        return mTextStickerView;
    }

    public void setmTextStickerView(TextStickerView mTextStickerView) {
        this.mTextStickerView = mTextStickerView;
    }

    // New
    private void drawStraightLine(Canvas c, Path path, String isDraw) {
        try {
            if (straightLinePoints != null && straightLinePoints.size() > 0) {
                Set<Map.Entry<Integer, ArrayList<Point>>> mappings = straightLinePoints.entrySet();
                Point point = null;
                Point pointEnd = null;
                for (Map.Entry<Integer, ArrayList<Point>> entry : mappings) {
                    point = ((Point) ((ArrayList) entry.getValue()).get(0));
                    pointEnd = ((Point) ((ArrayList) entry.getValue()).get(1));
                    if (isDraw.equalsIgnoreCase("Path")) {
                        brush.setColor(Color.parseColor(point.color));
                        c.drawPath(path, brush);
                    } else {
                        brush.setColor(Color.parseColor(point.color));
                        c.drawLine(point.x, point.y, pointEnd.x, pointEnd.y, brush);
                    }

                    c.drawText("" + entry.getKey(), point.x + 35, point.y + 35, mTextPaint);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawCircleRect(Canvas canvas, Bitmap addBit, String isDraw) {

        try {

            for (int i = 1; i <= cirRectPoints.size(); i++) {

                Point point = cirRectPoints.get(i - 1);
                if (isDraw.equalsIgnoreCase("Circle")) {
                    brush.setColor(Color.parseColor(point.color));
                    canvas.drawCircle(point.x, point.y, 45, brush);
                } else {
                    int bitmapWidth = addBit == null ? Constants.getRMin100Width(getContext()) : addBit.getWidth();
                    int bitmapHeight = addBit == null ? Constants.getRMin100Height(getContext()) : addBit.getHeight();

                    int bitWidth = Math.min(bitmapWidth, this.getWidth() >> 1);
                    int bitHeight = bitWidth * bitmapHeight / bitmapWidth;
                    int left = point.x == -1 ? (this.getWidth() >> 1) - (bitWidth >> 1) : (int) point.x;
                    int top = point.y == -1 ? (this.getHeight() >> 1) - (bitHeight >> 1) : (int) point.y;

                    dstRect = new RectF(left, top, left + bitmapWidth, top + bitmapHeight);

                    matrix = new Matrix();
                    matrix.postTranslate(dstRect.left, dstRect.top);
                    matrix.postScale((float) bitWidth / bitmapWidth,
                            (float) bitHeight / bitmapHeight, dstRect.left,
                            dstRect.top);

                    RectF helpBox = new RectF(dstRect);
                    brush.setColor(Color.parseColor(point.color));
                    canvas.drawRect(helpBox, brush);
                }
                canvas.drawText("" + i, point.x + 60, point.y + 35, mTextPaint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Point {
        public float x, y;
        String color;
        String drawType;

        public Point(float x, float y, String color, String drawType) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.drawType = drawType;
        }

        @Override
        public String toString() {
            return x + ", " + y;
        }
    }

    public void drawData() {
        invalidate();
    }

}// end class
