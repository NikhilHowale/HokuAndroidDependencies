package com.hokuapps.loadnativefileupload.utilities;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class MapWrapperLayout extends FrameLayout {

    public interface OnDragListener {
        public void onDrag(MotionEvent motionEvent);
        public void onMove(MotionEvent motionEvent);
    }

    private OnDragListener mOnDragListener;

    public MapWrapperLayout(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (mOnDragListener != null) {

            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mOnDragListener.onMove(ev);
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mOnDragListener.onDrag(ev);
                    break;
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    public void setOnDragListener(OnDragListener mOnDragListener) {
        this.mOnDragListener = mOnDragListener;
    }
}