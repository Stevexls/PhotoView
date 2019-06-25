package com.stevexls.photoview;

import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * Time：2019/6/18 10:31
 * Description: 绘制监听
 */
public interface OnDrawListener {

    /**
     * 绘制动作
     * @param canvas 绘制的canvas
     * @param viewWidth ImageView宽度
     * @param viewHeight ImageView高度
     * @param displayRect 展示Rect
     */
    void onDraw(Canvas canvas, int viewWidth, int viewHeight, RectF displayRect);
}
