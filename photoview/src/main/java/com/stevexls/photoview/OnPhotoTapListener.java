package com.stevexls.photoview;

import android.widget.ImageView;

/**
 * A callback to be invoked when the Photo is tapped with a single
 * tap.
 */
public interface OnPhotoTapListener {

    /**
     * 当单击事件发生在真实drawable内时触发
     * @param view  单击的View
     * @param x 单击时x坐标
     * @param y 单击时y坐标
     */
    void onPhotoTap(ImageView view, float x, float y);
}
