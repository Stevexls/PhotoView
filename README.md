# PhotoView
放大缩小旋转View

## Dependency
- Add the library to your module `build.gradle`
```gradle
dependencies {
	implementation 'com.stevexls.widget:photoview:1.0.5'
}
```

## Usage
- Step 1.
```xml
<com.stevexls.photoview.PhotoView
    android:id="@+id/photo_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

- Step 2.
```java
PhotoView photoView = findViewById(R.id.photo_view);
photoView.setImageResource(R.mipmap.image);
photoView.setZoomEnabled(true);     // 允许缩放
photoView.setRotateEnable(true);   // 允许旋转
photoView.setOnClickListener(new View.OnClickListener() { // 点击事件
    @Override
    public void onClick(View v) {
        Toast.makeText(SimpleActivity.this,"onClick", Toast.LENGTH_SHORT).show();
    }
});
photoView.setOnDrawListener(new OnDrawListener() {
    @Override
    public void onDraw(Canvas canvas, int viewWidth, int viewHeight, RectF displayRect) {
        // draw something
    }
});
```

## Thanks
- https://github.com/chrisbanes/PhotoView

## License

    Copyright 2019 Stevexls. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
