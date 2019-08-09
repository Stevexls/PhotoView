package com.stevexls.photoview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.OverScroller;

/**
 * Time：2019/6/21 11:29
 * Description:
 */
public class PhotoView extends AppCompatImageView implements View.OnTouchListener,
        View.OnLayoutChangeListener {

    private static float DEFAULT_MAX_SCALE = 4.0f;  // 缩放最大值
    private static float DEFAULT_MID_SCALE = 2.0f; // 中间状态
    private static float DEFAULT_SCALE = 1.0f;      // 正常状态
    private static float DEFAULT_MIN_SCALE = 0.75f; // 最小值
    private static int DEFAULT_ZOOM_DURATION = 200;
    private int mZoomDuration = DEFAULT_ZOOM_DURATION;    // 缩放动画时间
    private float mMinScale = DEFAULT_MIN_SCALE;
    private float mMidScale = DEFAULT_MID_SCALE;
    private float mMaxScale = DEFAULT_MAX_SCALE;
    private float defaultScale = DEFAULT_SCALE;

    // 是否滑动到边界的界定
    private static final int HORIZONTAL_EDGE_NONE = -1;
    private static final int HORIZONTAL_EDGE_LEFT = 0;
    private static final int HORIZONTAL_EDGE_RIGHT = 1;
    private static final int HORIZONTAL_EDGE_BOTH = 2;
    private static final int VERTICAL_EDGE_NONE = -1;
    private static final int VERTICAL_EDGE_TOP = 0;
    private static final int VERTICAL_EDGE_BOTTOM = 1;
    private static final int VERTICAL_EDGE_BOTH = 2;
    private int mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
    private int mVerticalScrollEdge = VERTICAL_EDGE_BOTH;

    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mSuppMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();

    private final float[] mMatrixValues = new float[9];
    private float mBaseRotation;
    private boolean inited = false;
    private ScaleType pendingScaleType;
    private boolean mZoomEnabled = true;    // 是否允许缩放
    private boolean mRotateEnable = false;  // 是否允许旋转
    private boolean mBlockParentIntercept = false;
    private ScaleType mScaleType = ScaleType.FIT_CENTER;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private OverScroller mScroller;
    private float lastFocusX = 0, lastFocusY = 0;   // 最后一次缩放中心点
    private float currentFlingX = 0, currentFlingY = 0; // 最后一次惯性滑动所在的坐标
    private boolean isViewHorizontalChanged = false, isViewVerticalChanged = false; // view是否改变过
    private boolean mAllowParentInterceptOnEdge = false;

    private OnViewChangedListener onViewChangedListener;
    private OnDrawListener onDrawListener;
    private CustomGestureDetector scaleDetector;
    private DragGestureDetector dragGestureDetector;
    private RotationGestureDetector rotateDetector;
    private OnGestureListener onGestureListener = new OnGestureListener() {
        @Override
        public void onDrag(float dx, float dy) {
            // 正在缩放时禁止拖动
            if (scaleDetector.isScaling() && rotateDetector.isRotate()) {
                return;
            }
            if (onViewChangedListener != null) {
                onViewChangedListener.onDrag(dx, dy);
            }

            mSuppMatrix.postTranslate(dx, dy);
            checkAndDisplayMatrix();

            ViewParent parent = getParent();
            if (parent == null) {
                return;
            }

            if (!mBlockParentIntercept) {
                boolean div = Math.abs(dx) > Math.abs(dy);
                if (mHorizontalScrollEdge == HORIZONTAL_EDGE_NONE && div) {
                    isViewHorizontalChanged = true;
                }
                if (mVerticalScrollEdge == VERTICAL_EDGE_NONE && !div) {
                    isViewVerticalChanged = true;
                }
                if (((mHorizontalScrollEdge == HORIZONTAL_EDGE_BOTH
                        || mHorizontalScrollEdge == HORIZONTAL_EDGE_LEFT
                        || mHorizontalScrollEdge == HORIZONTAL_EDGE_RIGHT) && div)
                        || ((mVerticalScrollEdge == VERTICAL_EDGE_BOTH
                        || mVerticalScrollEdge == VERTICAL_EDGE_TOP
                        || mVerticalScrollEdge == VERTICAL_EDGE_BOTTOM) && !div)) {
                    if (!isViewHorizontalChanged && !isViewVerticalChanged) {
                        mAllowParentInterceptOnEdge = true;
                    }
                }
                if (mAllowParentInterceptOnEdge) {  // 允许拦截
                    parent.requestDisallowInterceptTouchEvent(false);
                } else {    // 禁止拦截
                    parent.requestDisallowInterceptTouchEvent(true);
                }
            } else {
                // 禁止父控件拦截事件
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }

        @Override
        public void onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (onViewChangedListener != null) {
                onViewChangedListener.onFling(e1, e2, velocityX, velocityY);
            }
            fling(getImageViewWidth(), getImageViewHeight(), (int) velocityX, (int) velocityY);
        }

        @Override
        public void onScale(float scaleFactor, float focusX, float focusY) {
            float scale = getScale();

            if ((scale < mMaxScale && scaleFactor > 1f)
                    || (scale > mMinScale && scaleFactor < 1f)) {
                if (onViewChangedListener != null) {
                    onViewChangedListener.onScaleChange(scaleFactor, focusX, focusY);
                }
                mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                lastFocusX = focusX;
                lastFocusY = focusY;
                checkAndDisplayMatrix();
            } else if (scale >= mMaxScale && scaleFactor > 1f) {
                scaleFactor = (scaleFactor - 1) / 2 + 1;
                if (onViewChangedListener != null) {
                    onViewChangedListener.onScaleChange(scaleFactor, focusX, focusY);
                }
                mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                lastFocusX = focusX;
                lastFocusY = focusY;
                checkAndDisplayMatrix();
            }
        }

        @Override
        public void onRotation(float deltaAngle, float focusX, float focusY) {
            if (onViewChangedListener != null) {
                onViewChangedListener.onRotate(deltaAngle, focusX, focusY);
            }
            mSuppMatrix.postRotate(deltaAngle, focusX, focusY);
            checkAndDisplayMatrix();
        }
    };

    public PhotoView(Context context) {
        super(context);
        init(context);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        addOnLayoutChangeListener(this);
        setOnTouchListener(this);
        super.setScaleType(ScaleType.MATRIX);
        inited = true;
        if (pendingScaleType != null) {
            setScaleType(pendingScaleType);
            pendingScaleType = null;
        }
        dragGestureDetector = new DragGestureDetector(this, onGestureListener);
        scaleDetector = new CustomGestureDetector(getContext(), onGestureListener);
        rotateDetector = new RotationGestureDetector(this, onGestureListener);
        mScroller = new OverScroller(context);
        mBaseRotation = 0.0f;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            updateBaseMatrix(getDrawable());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean handled = false;
        if (mZoomEnabled && ((ImageView) v).getDrawable() != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ViewParent parent = v.getParent();
                    if (parent != null) {
                        // 禁止父控件拦截事件
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mAllowParentInterceptOnEdge = false;
                    isViewHorizontalChanged = false;
                    isViewVerticalChanged = false;
                    cancelFling();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    float scale = getScale();
                    if (scale < defaultScale || scale > mMaxScale) {   // 缩放值超出范围时，进行调整
                        float targetScale = scale < defaultScale ? defaultScale : mMaxScale;

                        v.post(new AnimatedZoomRunnable(scale, targetScale, lastFocusX, lastFocusY));
                        handled = true;
                    }
                    break;
            }
            boolean didntScale = false, didntDrag = false;
            if (scaleDetector != null) {
                handled = scaleDetector.onTouchEvent(event);
                didntScale = !scaleDetector.isScaling();
            }
            if (dragGestureDetector != null) {
                handled = dragGestureDetector.onTouchEvent(event);
                didntDrag = !dragGestureDetector.isDragging();
            }
            mBlockParentIntercept = didntScale && didntDrag;  // 非缩放、非拖动状态时禁止父控件拦截
            if (rotateDetector != null && mRotateEnable) {
                handled = rotateDetector.onTouchEvent(event);
            }
        }
        return handled;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (onDrawListener != null) {
            onDrawListener.onDraw(canvas, getImageViewWidth(), getImageViewHeight(), getDisplayRect());
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        //computeScrollOffset: 依据当前已经消逝的时间计算当前的坐标点，保存在mCurrX和mCurrY值中,判断滑动是否完成
        if (mScroller.computeScrollOffset()) {
            final int newX = mScroller.getCurrX();  // 获取当前应该滑动到的坐标
            final int newY = mScroller.getCurrY();
            mSuppMatrix.postTranslate(currentFlingX - newX, currentFlingY - newY);  // 计算需要移动的距离
            checkAndDisplayMatrix();
            currentFlingX = newX;
            currentFlingY = newY;
        }
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (!inited) {
            pendingScaleType = scaleType;
            return;
        }
        if (scaleType == null || scaleType == mScaleType || scaleType == ScaleType.MATRIX) {
            return;
        }
        mScaleType = scaleType;
        update();
    }

    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        update();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        update();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        update();
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            update();
        }
        return changed;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (!mZoomEnabled) {
            super.setOnClickListener(l);
        } else {
            if (!isClickable()) {
                setClickable(true);
            }
            dragGestureDetector.setOnClickListener(l);
        }
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        if (!mZoomEnabled) {
            super.setOnLongClickListener(l);
        } else {
            if (!isLongClickable()) {
                setLongClickable(true);
            }
            dragGestureDetector.setOnLongClickListener(l);
        }
    }

    private void updateBaseMatrix(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        float viewWidth = getImageViewWidth();
        float viewHeight = getImageViewHeight();
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        mBaseMatrix.reset();
        float widthScale = viewWidth / drawableWidth;
        float heightScale = viewHeight / drawableHeight;
        if (heightScale <= 0.4 && (drawableHeight / drawableWidth) > 2) {   // 增加对长图的支持
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
        } else if (mScaleType == ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F,
                    (viewHeight - drawableHeight) / 2F);
        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);
        } else if (mScaleType == ScaleType.CENTER_INSIDE) {
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);
        } else {
            RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
            RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);
            if ((int) mBaseRotation % 180 != 0) {
                mTempSrc = new RectF(0, 0, drawableHeight, drawableWidth);
            }
            mBaseMatrix.setRectToRect(mTempSrc, mTempDst, scaleTypeToScaleToFit(mScaleType));
        }
        resetMatrix();
    }

    private int getImageViewWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getImageViewHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType scaleType) {
        if (scaleType == ScaleType.FIT_CENTER) {
            return Matrix.ScaleToFit.CENTER;
        } else if (scaleType == ScaleType.FIT_END) {
            return Matrix.ScaleToFit.END;
        } else if (scaleType == ScaleType.FIT_START) {
            return Matrix.ScaleToFit.START;
        } else {    // ScaleType.FIT_XY
            return Matrix.ScaleToFit.FILL;
        }
    }

    public void resetMatrix() {
        mSuppMatrix.reset();
        setRotationBy(mBaseRotation);
        setImageViewMatrix(getDrawMatrix());
    }

    private void setImageViewMatrix(Matrix matrix) {
        setImageMatrix(matrix);
        if (onViewChangedListener != null) {
            RectF displayRect = getDisplayRect(matrix);
            if (displayRect != null) {
                onViewChangedListener.onMatrixChanged(displayRect);
            }
        }
    }

    public void update() {
        if (mZoomEnabled) {
            updateBaseMatrix(getDrawable());
        } else {
            resetMatrix();
        }
    }

    public Matrix getImageMatrix() {
        return mDrawMatrix;
    }

    // 获取需要变换的值
    public Matrix getSuppMatrix() {
        return mSuppMatrix;
    }

    public Matrix getBaseMatrix() {
        return mBaseMatrix;
    }

    // 获取当前变换后的mDrawMatrix值
    public Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);   // 每次都初始化原始状态
        mDrawMatrix.postConcat(mSuppMatrix);    // 执行变换
        return mDrawMatrix;
    }

    public boolean setDisplayMatrix(Matrix finalMatrix) {
        if (finalMatrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }
        if (getDrawable() == null) {
            return false;
        }
        mSuppMatrix.set(finalMatrix);
        checkAndDisplayMatrix();
        return true;
    }

    // 图片展示的范围
    private RectF getDisplayRect(Matrix matrix) {
        Drawable d = getDrawable();
        if (d != null) {
            RectF mDisplayRect = new RectF();
            // 这个地方还需要注意rectf.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight())这个方法，
            // 如果不写将导致结果没有将图片的宽高加进去
            mDisplayRect.set(0, 0, d.getIntrinsicWidth(),
                    d.getIntrinsicHeight());
            // 将Matrix 的值映射到RecF中
            matrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    public RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    // 获取矩阵的值
    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);  // 将matrix值赋给float数组
        return mMatrixValues[whichValue];
    }

    // 获取当前缩放值
    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow
                (getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    // 旋转至
    public void setRotationTo(float degrees) {
        setRotationTo(degrees, getWidth() / 2.0f, getHeight() / 2.0f);
    }

    public void setRotationTo(float degrees, float px, float py) {
        mSuppMatrix.setRotate(degrees % 360, px, py);
        checkAndDisplayMatrix();
    }

    // 原基础上继续旋转
    public void setRotationBy(float degrees) {
        setRotationBy(degrees, getWidth() / 2.0f, getHeight() / 2.0f);
    }

    public void setRotationBy(float degrees, float px, float py) {
        mSuppMatrix.postRotate(degrees % 360, px, py);
        checkAndDisplayMatrix();
    }

    // 是否允许长按
    public PhotoView setIsLongPressEnabled(boolean isLongPressEnabled) {
        dragGestureDetector.setIsLongPressEnabled(isLongPressEnabled);
        return this;
    }

    // 缩放动画时间
    public PhotoView setZoomTransitionDuration(int milliseconds) {
        this.mZoomDuration = milliseconds;
        return this;
    }

    public PhotoView setZoomEnabled(boolean zoomEnabled) {
        this.mZoomEnabled = zoomEnabled;
        return this;
    }

    public PhotoView setRotateEnable(boolean mRotateEnable) {
        this.mRotateEnable = mRotateEnable;
        return this;
    }

    public PhotoView setBaseRotation(float degree) {
        this.mBaseRotation = degree % 360;
        return this;
    }

    public PhotoView setScale(float scale, float focusX, float focusY, boolean animate) {
        if (scale < defaultScale || scale > mMaxScale) {
            throw new IllegalArgumentException("Scale must be within the range of defaultScale and maxScale");
        }
        if (animate) {
            post(new AnimatedZoomRunnable(getScale(), scale, focusX, focusY));
        } else {
            mSuppMatrix.setScale(scale, scale, focusX, focusY);
            checkAndDisplayMatrix();
        }
        return this;
    }

    public float getMidScale() {
        return mMidScale;
    }

    public PhotoView setMidScale(float midScale) {
        this.mMidScale = midScale;
        return this;
    }

    public float getDefaultScale() {
        return defaultScale;
    }

    public PhotoView setDefaultScale(float defaultScale) {
        this.defaultScale = defaultScale;
        return this;
    }

    public float getMaxScale() {
        return mMaxScale;
    }

    public PhotoView setMaxScale(float maxScale) {
        this.mMaxScale = maxScale;
        return this;
    }

    public float getMinScale() {
        return mMinScale;
    }

    public PhotoView setMinScale(float minScale) {
        this.mMinScale = minScale;
        return this;
    }

    public PhotoView setOnViewChangedListener(OnViewChangedListener listener) {
        this.onViewChangedListener = listener;
        return this;
    }

    public PhotoView setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
        return this;
    }

    public PhotoView setOnViewTapListener(OnViewTapListener listener) {
        dragGestureDetector.setOnViewTapListener(listener);
        return this;
    }

    public PhotoView setOnPhotoTapListener(OnPhotoTapListener listener) {
        dragGestureDetector.setOnPhotoTapListener(listener);
        return this;
    }

    public PhotoView setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener listener) {
        dragGestureDetector.setOnOutsidePhotoTapListener(listener);
        return this;
    }

    public void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(getDrawMatrix());
        }
    }

    // 检查是否超出ImageView范围,不能出现空隙
    private boolean checkMatrixBounds() {
        RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }
        float height = rect.height();
        float width = rect.width();
        float deltaX = 0, deltaY = 0;   // 需要移动的距离

        int viewHeight = getImageViewHeight();
        if (height <= viewHeight) {  // 图片在高度上属于缩小状态
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rect.top;
                    break;
                case FIT_END:
                    deltaY = viewHeight - height - rect.top;
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rect.top;  // 让图片垂直方向居中
                    break;
            }
            // 缩小状态下,垂直方向滑动都到达边界
            mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
        } else if (rect.top > 0) { // rectF高度大于ImageView本身的高度, rectF.top = 0并且rectF想继续往下移动时,限制其移动
            mVerticalScrollEdge = VERTICAL_EDGE_TOP;    // 垂直滑动边界为top边界
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) { // rectF高度大于ImageView本身的高度, rectF.bottom = viewHeight并且rectF想继续往上移动时,限制其移动
            mVerticalScrollEdge = VERTICAL_EDGE_BOTTOM;
            deltaY = viewHeight - rect.bottom;
        } else {    // 高度上属于放大状态并且滑动没有触及边界
            mVerticalScrollEdge = VERTICAL_EDGE_NONE;
        }

        int viewWidth = getImageViewWidth();
        if (width <= viewWidth) { // 当前在宽度方面属于缩小状态
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rect.left;
                    break;
                case FIT_END:
                    deltaX = viewWidth - width - rect.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;   // 水平方向居中
                    break;
            }
            mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
        } else if (rect.left > 0) {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_LEFT;
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mHorizontalScrollEdge = HORIZONTAL_EDGE_RIGHT;
        } else {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_NONE;
        }
        mSuppMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    private void fling(int viewWidth, int viewHeight, int velocityX, int velocityY) {
        final RectF rect = getDisplayRect();
        if (rect == null) {
            return;
        }
        // Math.round 四舍五入取整
        final int startX = Math.round(-rect.left);
        final int minX, maxX, minY, maxY;
        if (viewWidth < rect.width()) { // 如果当前是放大状态
            minX = 0;   // x轴最小滑动距离
            maxX = Math.round(rect.width() - viewWidth);
        } else {        // 当前是缩小状态
            minX = maxX = startX;
        }
        final int startY = Math.round(-rect.top);
        if (viewHeight < rect.height()) {   // 如果当前是放大状态
            minY = 0;   // y轴最小滑动距离
            maxY = Math.round(rect.height() - viewHeight);
        } else {
            minY = maxY = startY;
        }
        currentFlingX = startX;
        currentFlingY = startY;
        if (startX != maxX || startY != maxY) {
            mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
        }
    }

    private void cancelFling() {
        mScroller.forceFinished(true);
    }

    private class AnimatedZoomRunnable implements Runnable {

        private final float mFocalX, mFocalY;
        private final long mStartTime;
        private final float mZoomStart, mZoomEnd;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom,
                                    final float focalX, final float focalY) {
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {
            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getScale();
            onGestureListener.onScale(deltaScale, mFocalX, mFocalY);
            if (t < 1f) {
                postDelayed(this, 16);
            }
        }

        // 获取当前动画完成程度
        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            t = mInterpolator.getInterpolation(t);
            return t;
        }
    }
}
