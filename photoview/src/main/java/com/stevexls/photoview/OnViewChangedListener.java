package com.stevexls.photoview;

import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * Time：2019/6/21 10:31
 * Description:Interface definition for a callback to be invoked when PhotoView is changed.
 */
public interface OnViewChangedListener {

    /**
     * Notified of a fling event
     * @param e1 The first down motion event that started the fling.
     * @param e2 The move motion event that triggered the current onFling.
     * @param velocityX The velocity of this fling measured in pixels per second along the x axis.
     * @param velocityY The velocity of this fling measured in pixels per second along the y axis.
     */
    void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);

    /**
     * Callback for when the photo is experiencing a drag event. This cannot be invoked when the user is scaling or rotating.
     * @param dx The change of the coordinates in the x-direction
     * @param dy The change of the coordinates in the y-direction
     */
    void onDrag(float dx, float dy);

    /**
     * Callback for when the scale changes
     * @param scaleFactor the scale factor (less than 1 for zoom out, greater than 1 for zoom in)
     * @param focusX focal point X position
     * @param focusY focal point Y position
     */
    void onScaleChange(float scaleFactor, float focusX, float focusY);

    /**
     * Callback for when the photo is rotating
     * @param deltaAngle The change of the angle
     * @param focusX focal point X position
     * @param focusY focal point Y position
     */
    void onRotate(float deltaAngle, float focusX, float focusY);

    /**
     * Callback for when the matrix of a view changes
     * @param rect 改变后的RectF
     */
    void onMatrixChanged(RectF rect);
}
