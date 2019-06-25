package com.stevexls.photoview;

import android.view.MotionEvent;

interface OnGestureListener {

    void onDrag(float dx, float dy);

    void onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                 float velocityY);

    void onScale(float scaleFactor, float focusX, float focusY);

    void onRotation(float deltaAngle, float focusX, float focusY);
}