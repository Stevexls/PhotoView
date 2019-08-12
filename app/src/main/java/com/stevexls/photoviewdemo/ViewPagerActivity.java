package com.stevexls.photoviewdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.stevexls.photoview.PhotoView;

/**
 * Time：2019/6/21 16:36
 * Description:
 * Author:592172833@qq.com
 */
public class ViewPagerActivity extends AppCompatActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewpager);
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new PhotoAdapter());
    }

    static class PhotoAdapter extends PagerAdapter {
        private static final int[] sDrawables = {R.mipmap.test, R.mipmap.test, R.mipmap.bigimg, R.mipmap.test, R.mipmap.bigimg, R.mipmap.test,};

        @Override
        public int getCount() {
            return sDrawables.length;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setImageResource(sDrawables[position]);
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return photoView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }
    }
}
