package com.stevexls.photoview;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.math.BigDecimal;

/**
 * Time：2019/6/21 13:59
 * Description:
 */
public class DragGestureDetector {

    private static final int INVALID_POINTER_ID = -1;

    private int mActivePointerId = INVALID_POINTER_ID;
    private int mActivePointerIndex = 0;

    private View.OnLongClickListener onLongClickListener;
    private View.OnClickListener onClickListener;
    // 单击view时监听
    private OnViewTapListener onViewTapListener;
    // 单击drawable内监听
    private OnPhotoTapListener onPhotoTapListener;
    // 单击在drawable外时监听
    private OnOutsidePhotoTapListener onOutsidePhotoTapListener;

    private PhotoView photoView;
    private OnGestureListener listener;
    private GestureDetector mGestureDetector;

    private boolean mIsDragging;    // 是否正在拖动
    private boolean singleTouch = false;

    private GestureDetector.SimpleOnGestureListener dragListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            singleTouch = true;
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (onLongClickListener != null) {
                onLongClickListener.onLongClick(photoView);
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (onClickListener != null) {
                onClickListener.onClick(photoView);
            }

            RectF displayRect = photoView.getDisplayRect();
            float x = e.getX(), y = e.getY();
            if (onViewTapListener != null) {
                onViewTapListener.onViewTap(photoView, x, y);
            }

            if (displayRect != null) {
                // 单击事件发生在displayRect内时
                if (displayRect.contains(x, y)) {
                    if (onPhotoTapListener != null) {
                        onPhotoTapListener.onPhotoTap(photoView, x, y);
                    }
                    return true;
                } else {
                    if (onOutsidePhotoTapListener != null) {
                        onOutsidePhotoTapListener.onOutsidePhotoTap(photoView, x, y);
                    }
                }
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float scale = photoView.getScale();
            BigDecimal bigDecimal = new BigDecimal(scale);
            scale = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();  // 保留两位小数并四舍五入
            float x = e.getX();
            float y = e.getY();
            if (scale < photoView.getMidScale()) {
                photoView.setScale(photoView.getMidScale(), x, y, true);
            } else {
                photoView.setScale(photoView.getDefaultScale(), x, y, true);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e2.getPointerCount() > 1) {
                singleTouch = false;
            }
            if (singleTouch) {
                mIsDragging = true;
                listener.onDrag(-distanceX, -distanceY);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            listener.onFling(e1, e2, -velocityX, -velocityY);
            return true;
        }
    };

    public DragGestureDetector(PhotoView photoView, OnGestureListener listener) {
        this.photoView = photoView;
        this.listener = listener;
        mIsDragging = false;
        singleTouch = true;
        mGestureDetector = new GestureDetector(photoView.getContext(), dragListener);
    }

    public boolean isDragging() {
        return mIsDragging;
    }

    // 是否允许长按
    public void setIsLongPressEnabled(boolean isLongPressEnabled) {
        mGestureDetector.setIsLongpressEnabled(isLongPressEnabled);
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        this.onViewTapListener = listener;
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        this.onPhotoTapListener = listener;
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener listener) {
        this.onOutsidePhotoTapListener = listener;
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        try {
            return mGestureDetector.onTouchEvent(event);
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
