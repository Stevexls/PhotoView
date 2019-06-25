package com.stevexls.photoviewdemo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.stevexls.photoview.OnPhotoTapListener;
import com.stevexls.photoview.PhotoView;

public class SimpleActivity extends AppCompatActivity {

    private PhotoView photoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        photoView = findViewById(R.id.photo_view);
        photoView.setZoomEnabled(true);     // 允许缩放
        photoView.setRotateEnable(false);   // 允许旋转
//        photoView.setBaseRotation(90);      // 基础旋转角度
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SimpleActivity.this,"onClick", Toast.LENGTH_SHORT).show();
            }
        });
        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                Toast.makeText(SimpleActivity.this,"onPhotoTap x = " + x + ", y = " + y, Toast.LENGTH_SHORT).show();
            }
        });
//        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.test);
        photoView.setImageDrawable(drawable);
    }
}
