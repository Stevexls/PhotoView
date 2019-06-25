package com.stevexls.photoviewdemo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.stevexls.photoview.PhotoView;

/**
 * Time：2019/6/21 16:17
 * Description:
 * Author:592172833@qq.com
 */
public class LongPictureActivity extends AppCompatActivity {

    private PhotoView photoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_pic);
        photoView = findViewById(R.id.photo_view);
        photoView.setZoomEnabled(true);     // 允许缩放
        Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.bigimg);
        photoView.setImageDrawable(drawable);
    }
}
