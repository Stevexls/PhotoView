package com.stevexls.photoview;

import android.view.View;

public interface OnViewTapListener {

    /**
     * ImageView单击时监听
     * @param view 单击时的view
     * @param x 单击时x坐标
     * @param y 单击时y坐标
     */
    void onViewTap(View view, float x, float y);
}
