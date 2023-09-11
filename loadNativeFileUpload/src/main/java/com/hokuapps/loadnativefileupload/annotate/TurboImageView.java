package com.hokuapps.loadnativefileupload.annotate;


import static com.hokuapps.loadnativefileupload.annotate.AnnotateActivity.isDelete;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.xinlan.imageeditlibrary.editimage.OnItemChangeListener;
import com.xinlan.imageeditlibrary.editimage.view.Constants;
import com.xinlan.imageeditlibrary.editimage.view.StickerItem;
import com.xinlan.imageeditlibrary.editimage.view.TextStickerItem;
import com.xinlan.imageeditlibrary.editimage.view.TextStickerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TurboImageView extends View implements MultiTouchObjectCanvas<MultiTouchObject> {
    private static final String TAG = "TurboImageView";
    private static final int UI_MODE_ROTATE = 1;
    private static final int UI_MODE_ANISOTROPIC_SCALE = 2;
    private static final int mUIMode = UI_MODE_ROTATE;
    public static LinkedHashMap<Integer, StickerItem> bank = new LinkedHashMap<Integer, StickerItem>();
    public static String isDraw = "Path";
    public static boolean isFreeDraw = false;
    public static String colorCode = "#FF0000";
    public static int badgeCount = 0;
    public static ArrayList<Point> cirRectPoints = new ArrayList<>();
    public static ArrayList<Point> startEndPoints = new ArrayList<>();
    public static HashMap<Integer, ArrayList<Point>> straightLinePoints = new HashMap<>();
    private static int STATUS_IDLE = 0;
    private static int STATUS_MOVE = 1;
    private static int STATUS_DELETE = 2;
    private static int STATUS_ROTATE = 3;
    private final PointInfo currTouchPoint = new PointInfo();
    public ArrayList<MultiTouchObject> mImages = new ArrayList<>();
    public StickerItem currentItem;
    public TextStickerView mTextStickerView;
    // New
    public RectF dstRect;
    public Matrix matrix;
    int imageCount = 0;
    private MultiTouchController<MultiTouchObject> multiTouchController = new MultiTouchController<>(
            this);
    private Context mContext;
    private int currentStatus;
    private float oldx, oldy;
    private OnItemChangeListener onItemChangeListener;
    private Paint brush = new Paint();
    private TextPaint mTextPaint = new TextPaint();
    private Path path = new Path();
    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();
    private TurboImageViewListener listener;
    private int objectBorderColor = MultiTouchObject.DEFAULT_BORDER_COLOR;

    private boolean selectOnObjectAdded = true;
    private Paint paint;

    public TurboImageView(Context context) {
        this(context, null);
    }

    public TurboImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TurboImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        paint = new Paint();
        paint.setColor(Color.GRAY);
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
    }

    public void addObject(Context context, int resourceId) {
        ImageObject imageObject = new ImageObject(resourceId, context.getResources());
        //addObject(context, imageObject);
    }

    public void addObject(Context context, Drawable drawable) {
        ImageObject imageObject = new ImageObject(drawable, context.getResources());
        // addObject(context, imageObject);
    }

    public void addObject(Context context, Bitmap bitmap, float x, float y) {
        imageCount++;
        ImageObject imageObject = new ImageObject(bitmap, context.getResources(), imageCount);
        addObject(context, imageObject, x, y);
    }

    private void addObject(Context context, ImageObject imageObject, float cx, float cy) {
        deselectAll();

        imageObject.setSelected(selectOnObjectAdded);
        imageObject.setBorderColor(objectBorderColor);
        mImages.add(imageObject);



        mImages.get(mImages.size() - 1).init(context, cx, cy);

        invalidate();
    }

    public int getObjectSelectedBorderColor() {
        return this.objectBorderColor;
    }

    public void setObjectSelectedBorderColor(int borderColor) {
        this.objectBorderColor = borderColor;
        for (MultiTouchObject imageObject : mImages) {
            imageObject.setBorderColor(borderColor);
        }
        invalidate();
    }

    protected void onDraw(Canvas canvas) {

        if (isDelete) {
            isDelete=false;
            for (int i = 1; i <= mImages.size(); i++) {
                MultiTouchObject imageObject = mImages.get(i - 1);
                ((ImageObject) imageObject).count = AnnotateActivity.totalcountedit + i;
                imageObject.draw(canvas);
            }
        } else {
            for (MultiTouchObject imageObject : mImages) {
                imageObject.draw(canvas);
            }
        }

        if (isDraw.equalsIgnoreCase("Rectangle") || isDraw.equalsIgnoreCase("Circle")) {
            //For Rectangle
            for (Integer id : bank.keySet()) {
                StickerItem item = bank.get(id);
                item.draw(canvas, isDraw);
            }// end for each
            drawCircleRect(canvas, null, isDraw);
        } else if (isDraw.equalsIgnoreCase("Path")) {
            for (Path path : paths) {

                drawPath(canvas, path, isDraw);
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
        super.onDraw(canvas);
    }
    private void drawPath(Canvas c, Path path, String isDraw) {
        try {
            if (straightLinePoints != null && straightLinePoints.size() > 0) {
                Set<Map.Entry<Integer, ArrayList<Point>>> mappings = straightLinePoints.entrySet();
                Point point = null;
                Point pointEnd = null;
                for (Map.Entry<Integer, ArrayList<Point>> entry : mappings) {
                    point = ((Point) ((ArrayList) entry.getValue()).get(0));
                    pointEnd = ((Point) ((ArrayList) entry.getValue()).get(1));
                    brush.setColor(Color.parseColor(point.color));
                    c.drawPath(path, brush);
                    c.drawText("" + entry.getKey(), point.x + 35, point.y + 35, mTextPaint);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawStraightLine(Canvas c, Path path, String isDraw) {
        try {
            if (straightLinePoints != null && straightLinePoints.size() > 0) {
                Set<Map.Entry<Integer, ArrayList<Point>>> mappings = straightLinePoints.entrySet();
                Point point = null;
                Point pointEnd = null;
                for (Map.Entry<Integer, ArrayList<Point>> entry : mappings) {
                    point = ((Point) ((ArrayList) entry.getValue()).get(0));
                    pointEnd = ((Point) ((ArrayList) entry.getValue()).get(1));
                    brush.setColor(Color.parseColor(point.color));
                    if (isDraw.equalsIgnoreCase("Path")) {
                        c.drawPath(path, brush);
                    } else {
                        c.drawLine(point.x, point.y, pointEnd.x, pointEnd.y, brush);
                    }
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
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        if (isFreeDraw) {
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (isDraw.equalsIgnoreCase("Line") || isDraw.equalsIgnoreCase("Path")) {
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
                        path.lineTo(x,y);
                        paths.add(path);
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isDraw.equalsIgnoreCase("Line") || isDraw.equalsIgnoreCase("Path")) {
                        ret = true;
                        startEndPoints.add(new Point(x, y, colorCode, isDraw));
                        straightLinePoints.put(badgeCount, startEndPoints);

                        path.lineTo(x,y);
                        paths.add(path);
                        invalidate();

                        path = new Path();
                        startEndPoints = new ArrayList<>();
                    }
                    break;
            }
        }
//        return ret;
        return multiTouchController.onTouchEvent(event);
    }

    /**
     * Get the image that is under the single-touch point, or return null
     * (canceling the drag op) if none
     */
    public MultiTouchObject getDraggableObjectAtPoint(PointInfo touchPoint) {
        float x = touchPoint.getX();
        float y = touchPoint.getY();

        for (int i = mImages.size() - 1; i >= 0; i--) {
            ImageObject imageObject = (ImageObject) mImages.get(i);
            if (imageObject.containsPoint(x, y)) {
                return imageObject;
            }
        }
        return null;
    }

    /**
     * Select an object for dragging. Called whenever an object is found to be
     * under the point (non-null is returned by getDraggableObjectAtPoint()) and
     * a drag operation is starting. Called with null when drag op ends.
     */
    public void selectObject(MultiTouchObject multiTouchObject, PointInfo touchPoint) {
        isFreeDraw = false;
        currTouchPoint.set(touchPoint);
        if (multiTouchObject != null) {
            // Move image to the top of the stack when selected
            mImages.remove(multiTouchObject);
            mImages.add(multiTouchObject);
            if (listener != null) {
                listener.onImageObjectSelected(multiTouchObject);
            }
        } else {
            // Called with multiTouchObject == null when drag stops.
            if (listener != null) {
                listener.onImageObjectDropped();
            }
        }
        invalidate();
    }

    @Override
    public void deselectAll() {
        for (MultiTouchObject imageObject : mImages) {
            imageObject.setSelected(false);
        }
        invalidate();
    }

    @Override
    public void canvasTouched() {
        if (listener != null) {
            listener.onCanvasTouched();
        }
    }

    public boolean removeSelectedObject() {
        boolean deleted = false;
        Iterator<MultiTouchObject> iterator = mImages.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isSelected()) {
                iterator.remove();
                deleted = true;
            }
        }

        invalidate();
        return deleted;
    }

    public boolean removeLongSelectedObject() {
        boolean deleted = false;
        Iterator<MultiTouchObject> iterator = mImages.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isSelected()) {
                imageCount--;
                iterator.remove();
                deleted = true;
            }
        }
        invalidate();
        return deleted;
    }

    public boolean undoSelectedObject() {
        boolean deleted = false;
        mImages.get(mImages.size() - 1).setSelected(true);
        Iterator<MultiTouchObject> iterator = mImages.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isSelected()) {
                imageCount--;
                iterator.remove();
                deleted = true;
            }
        }

        invalidate();
        return deleted;
    }

    public void removeAllObjects() {
        mImages.clear();
        imageCount = 0;
        invalidate();
    }

    public void clearAllDrawData() {
        straightLinePoints.clear();
        bank.clear();
        cirRectPoints.clear();
        paths.clear();
        badgeCount = 0;
        this.invalidate();
    }

    public int getSelectedObjectCount() {
        int count = 0;
        for (MultiTouchObject imageObject : mImages) {
            if (imageObject.isSelected()) {
                count++;
            }
        }
        return count;
    }

    public int getObjectCount() {
        return mImages.size();
    }

    public void setListener(TurboImageViewListener turboImageViewListener) {
        this.listener = turboImageViewListener;
        brush.setStyle(Paint.Style.STROKE);
        brush.setColor(Color.RED);
        brush.setStrokeWidth(10);

        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextSize(30);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }


    /**
     * Get the current position and scale of the selected image. Called whenever
     * a drag starts or is reset.
     */
    public void getPositionAndScale(MultiTouchObject multiTouchObject, PositionAndScale objPosAndScaleOut) {
        objPosAndScaleOut.set(multiTouchObject.getCenterX(), multiTouchObject.getCenterY(),
                (mUIMode & UI_MODE_ANISOTROPIC_SCALE) == 0,
                (multiTouchObject.getScaleX() + multiTouchObject.getScaleY()) / 2,
                (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0, multiTouchObject.getScaleX(),
                multiTouchObject.getScaleY(), (mUIMode & UI_MODE_ROTATE) != 0,
                multiTouchObject.getAngle());
    }

    /**
     * Set the position and scale of the dragged/stretched image.
     */

    public boolean check() {
        return multiTouchController.checkDelete();
    }

    public boolean setPositionAndScale(MultiTouchObject multiTouchObject,
                                       PositionAndScale newImgPosAndScale, PointInfo touchPoint) {
        currTouchPoint.set(touchPoint);
        boolean moved = multiTouchObject.setPos(newImgPosAndScale);

        if (moved) {
            invalidate();
        }
        return moved;
    }

    public boolean pointInObjectGrabArea(PointInfo touchPoint, MultiTouchObject multiTouchObject) {
        return false;
    }

    public boolean isSelectOnObjectAdded() {
        return selectOnObjectAdded;
    }

    public void setSelectOnObjectAdded(boolean selectOnObjectAdded) {
        this.selectOnObjectAdded = selectOnObjectAdded;
    }

    public boolean isFlippedHorizontallySelectedObject() {
        for (MultiTouchObject imageObject : mImages) {
            if (imageObject.isSelected()) {
                return imageObject.isFlippedHorizontally();
            }
        }
        return false;
    }

    public void setFlippedHorizontallySelectedObject(boolean flipped) {
        for (MultiTouchObject imageObject : mImages) {
            if (imageObject.isSelected()) {
                imageObject.setFlippedHorizontally(flipped);
            }
        }
        invalidate();
    }

    public void toggleFlippedHorizontallySelectedObject() {
        for (MultiTouchObject imageObject : mImages) {
            if (imageObject.isSelected()) {
                imageObject.setFlippedHorizontally(!imageObject.isFlippedHorizontally());
            }
        }
        invalidate();
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
}
