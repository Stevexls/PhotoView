package com.stevexls.photoviewdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSimple, btnLongPic, btnVp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSimple = findViewById(R.id.simple);
        btnLongPic = findViewById(R.id.long_pic);
        btnVp = findViewById(R.id.vp);

        btnSimple.setOnClickListener(this);
        btnLongPic.setOnClickListener(this);
        btnVp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.simple:
                intent.setClass(MainActivity.this, SimpleActivity.class);
                break;
            case R.id.long_pic:
                intent.setClass(MainActivity.this, LongPictureActivity.class);
                break;
            case R.id.vp:
                intent.setClass(MainActivity.this, ViewPagerActivity.class);
                break;
        }
        startActivity(intent);
    }
}
