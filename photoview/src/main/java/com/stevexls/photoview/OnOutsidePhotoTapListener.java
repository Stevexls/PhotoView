package com.stevexls.photoview;

import android.widget.ImageView;

/**
 * Callback when the user tapped outside of the photo
 */
public interface OnOutsidePhotoTapListener {

    /**
     * 当单击事件发生在drawable外时
     * @param imageView 单击的View
     * @param x 单击时x坐标
     * @param y y坐标
     */
    void onOutsidePhotoTap(ImageView imageView, float x, float y);
}
