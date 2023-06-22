package com.xinlan.imageeditlibrary.editimage.view;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.xinlan.imageeditlibrary.R;
import com.xinlan.imageeditlibrary.editimage.OnItemChangeListener;

import java.util.LinkedHashMap;

public class TextStickerView extends View {
    private static final int IDLE_MODE = 2;
    private static final int MOVE_MODE = 3;
    private static final int ROTATE_MODE = 4;
    private static final int DELETE_MODE = 5;

    private int imageCount;
    private Context mContext;
    private int currentStatus;
    private TextStickerItem currentItem;

    private float last_x = 0;
    private float last_y = 0;

    private OnItemChangeListener onItemChangeListener;

    public LinkedHashMap<Integer, TextStickerItem> bank = new LinkedHashMap<Integer, TextStickerItem>();

    //private Stack<TextStickerItem> itemStack;

    public TextStickerView(Context context) {
        super(context);
        init(context);
    }

    public TextStickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextStickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
       // itemStack = new Stack<>();
        currentStatus = IDLE_MODE;
    }

    public TextStickerItem addText(final EditText text, Object prevItemObj, float x, float y, int color) {
        TextStickerItem item = new TextStickerItem(this.getContext());
        item.initView(this.getContext(), x, y);

        if (prevItemObj instanceof TextStickerItem) {
            removePreviousIfTextEmpty((TextStickerItem) prevItemObj);
        }

        if (currentItem != null) {
            currentItem.isShowHelpBox = false;
        }

        item.stickerItemId = ++imageCount;
        currentItem = item;
        last_x = 0;
        last_y = 0;
        bank.put(currentItem.stickerItemId, item);
        setEditText(text);
        setTextColor(color);
       // itemStack.push(item);

        if (onItemChangeListener != null) {
            onItemChangeListener.onItemChanged(currentItem, OnItemChangeListener.pushItemInStack);
        }

        this.invalidate();
        return item;
    }

    public OnItemChangeListener getOnItemChangeListener() {
        return onItemChangeListener;
    }

    public void setOnItemChangeListener(OnItemChangeListener onItemChangeListener) {
        this.onItemChangeListener = onItemChangeListener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = super.onTouchEvent(event);

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
//        if (currentItem == null) return ret;

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                int deleteId = -1;
                for (Integer id : bank.keySet()) {
                    TextStickerItem item = bank.get(id);

                    if (item.mDeleteDstRect.contains(x, y)) {
                        ret = true;
                        deleteId = id;
                        currentStatus = DELETE_MODE;

                    } else if (item.mHelpBoxRect.contains(x, y)) {

                            ret = true;
                            if (currentItem != null) {
                                currentItem.isShowHelpBox = false;
                            }
                            currentItem = item;
                            currentItem.isShowHelpBox = true;

                            if (onItemChangeListener != null) {
                                onItemChangeListener.onItemChanged(currentItem, OnItemChangeListener.reselected);
                            }

                            // invalidate();
                            currentStatus = MOVE_MODE;
                            last_x = x;
                            last_y = y;

                        } else if (item.mRotateDstRect.contains(x, y)) {
                            ret = true;
                            if (currentItem != null) {
                                currentItem.isShowHelpBox = false;
                            }
                            currentItem = item;
                            currentItem.isShowHelpBox = true;
                            currentStatus = ROTATE_MODE;
                            last_x = x;
                            last_y = y;

                        }// end if
                }// end for each

                //change current item
                if (onItemChangeListener != null && deleteId == -1) {
                    onItemChangeListener.onItemChanged(currentItem, OnItemChangeListener.itemChanged);
                }

                if (!ret && currentItem != null && currentStatus == IDLE_MODE) {
                    currentItem.isShowHelpBox = false;
                    currentItem = null;
                    invalidate();
                }

                if (deleteId > 0 && currentStatus == DELETE_MODE) {
                    TextStickerItem deletedTextSticker = bank.remove(deleteId);
                    currentStatus = IDLE_MODE;
                    invalidate();
                    ret = true;

                    if (onItemChangeListener != null) {
                        onItemChangeListener.onItemChanged(deletedTextSticker, OnItemChangeListener.deleted);
                    }

                }// end if

                /*if (currentStatus == DELETE_MODE) {
                    ret = true;
                    currentStatus = IDLE_MODE;
                    currentItem.clearTextContent();
                    invalidate();
                }*/// end if
                break;
            case MotionEvent.ACTION_MOVE:
                ret = true;
                if (currentStatus == MOVE_MODE) {
                    currentStatus = MOVE_MODE;
                    float dx = x - last_x;
                    float dy = y - last_y;

                    currentItem.layout_x += dx;
                    currentItem.layout_y += dy;

                    invalidate();

                    last_x = x;
                    last_y = y;
                } else if (currentStatus == ROTATE_MODE) {
                    currentStatus = ROTATE_MODE;
                    float dx = x - last_x;
                    float dy = y - last_y;

                    currentItem.updateRotateAndScale(dx, dy);

                    invalidate();
                    last_x = x;
                    last_y = y;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                ret = false;
                currentStatus = IDLE_MODE;
                break;
        }// end switch

        return ret;
    }

    public void removePreviousIfTextEmpty() {
        try {
            if (currentItem != null) {
                if (TextUtils.isEmpty(currentItem.mText)
                        || mContext.getResources().getString(R.string.add_caption_label).equalsIgnoreCase(currentItem.mText)) {
                    bank.remove(currentItem.stickerItemId);
                    if (onItemChangeListener != null) {
                        onItemChangeListener.onItemChanged(currentItem, OnItemChangeListener.deleted);
                    }
                    invalidate();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void removePreviousIfTextEmpty(TextStickerItem previousItem) {
        try {
            if (previousItem != null) {
                if (TextUtils.isEmpty(previousItem.mText)
                        || mContext.getResources().getString(R.string.add_caption_label).equalsIgnoreCase(previousItem.mText)) {
                    bank.remove(previousItem.stickerItemId);
                    if (onItemChangeListener != null) {
                        onItemChangeListener.onItemChanged(previousItem, OnItemChangeListener.deleted);
                    }
                    invalidate();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void removePreviousIfTextEmpty(Object lastSelectedItem, Object objFrom) {
        try {
            if (lastSelectedItem != null) {

                if (lastSelectedItem instanceof TextStickerItem) {

                    TextStickerItem currentItem = (TextStickerItem) lastSelectedItem;
                    if (objFrom instanceof TextStickerItem) {
                        if (currentItem.stickerItemId != ((TextStickerItem) objFrom).stickerItemId) {
                            if (TextUtils.isEmpty(currentItem.mText)
                                    || mContext.getResources().getString(R.string.add_caption_label).equalsIgnoreCase(currentItem.mText)) {
                                bank.remove(currentItem.stickerItemId);
                                if (onItemChangeListener != null) {
                                    onItemChangeListener.onItemChanged(currentItem, OnItemChangeListener.deleted);
                                }
                                invalidate();
                            }
                        }
                    } else if (objFrom instanceof StickerItem) {
                        if (TextUtils.isEmpty(currentItem.mText)
                                || mContext.getResources().getString(R.string.add_caption_label).equalsIgnoreCase(currentItem.mText)) {
                            bank.remove(currentItem.stickerItemId);
                            if (onItemChangeListener != null) {
                                onItemChangeListener.onItemChanged(currentItem, OnItemChangeListener.deleted);
                            }
                            invalidate();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
       /* if (isInitLayout) {
            isInitLayout = false;
            resetView();
        }*/
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Integer id : bank.keySet()) {
            TextStickerItem item = bank.get(id);
            item.drawContent(canvas);

        }// end for each

    }

    /*public void resetView() {
        layout_x = getMeasuredWidth() / 2;
        layout_y = getMeasuredHeight() / 2;
        mRotateAngle = 0;
        mScale = 1;
    }*/

    public void setEditText(EditText textView) {
        if (currentItem != null) {
            currentItem.setEditText(textView);
        }
        invalidate();
    }

    public void setText(String text) {
        if (currentItem != null) {
            currentItem.setText(text);
            invalidate();
        }
    }

    public void setTextColor(int newColor) {
        if (currentItem != null) {
            this.currentItem.setTextColor(newColor);
        }
        invalidate();
    }

}//end class
