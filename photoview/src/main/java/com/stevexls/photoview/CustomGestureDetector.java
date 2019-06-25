package com.stevexls.photoview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Time: 2019/6/12 17:42
 * Description: 缩放监听
 */
public class CustomGestureDetector {

    private ScaleGestureDetector mDetector;   // 缩放手势操作
    private OnGestureListener mListener;

    public CustomGestureDetector(Context context, OnGestureListener listener) {
        mListener = listener;
        ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();  // 获取当前缩放因子,每次都是从1开始变化
                if (scaleFactor >= 0) {
                    // getFocusX(),getFocusY()：返回组成该手势的两个触点的中点在组件上的y,x轴坐标，单位为像素。
                    mListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                }
                return true;
            }
        };
        mDetector = new ScaleGestureDetector(context, mScaleListener);
    }

    // 如果缩放手势正处在进行中，返回true;否则返回false
    public boolean isScaling() {
        return mDetector.isInProgress();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return mDetector.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
