package com.jdkj.customizeview.view.slidingRing;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.jdkj.customizeview.R;


/**
 * 自定义圆环进度控制view Customize
 * SlidingRing
 */

public class SlidingRingView extends View {
    private Context mContext;

    //==== paint ====//
    private Paint mCircleBgPaint;
    private Paint mBlurPaint;
    private Paint mCirqueBgPaint;
    private Paint mCirquePaint;
    private Paint mTextPaint;
    private Paint mBitmapPaint;
    private Paint mMiddleImagePaint;

    /**
     * 圆半径
     */
    private int mRadius;

    /**
     * 圆中心点x坐标
     */
    private int mCenterX;

    /**
     * 圆中心点y坐标
     */
    private int mCenterY;

    /**
     * 当前进度对应弧度
     */
    private float mCurrentAngle = 0;

    /**
     * 圆环oval
     */
    private RectF mOval;

    /**
     * 可拖动小圆点的rect
     */
    private Rect mBitmapRect;

    /**
     * progress最小值
     */
    private int mMinProgress;

    /**
     * progress最大值
     */
    private int mMaxProgress;

    //滑动圆点资源
    private int mDotResources;

    //
    private int mIntermediateImageResources;

    /**
     * 开始角度
     */
    public int mStartAngle = -225;
    //进度偏移量 开始角度+偏移量=总角度
    public int mProgressOffest = 45;

    /**
     * 总角度
     */
    public int mEndAngle = 270;
    public float mEndAnglef = 270f;

    /**
     * 上一次的弧边角
     */
    private float lastAngle;
    /**
     * 上一次点所在象限
     */
    public int lastQuadrant = 3;
    public int mTouchQuadrant = 3;

    /**
     * 当前progress
     */
    private int mCurrentProgress;

    private float mCurrentProgressPercent;

    /**
     * 线条宽度
     */
    private int mLineStrokeWidth;

    /**
     * 原始半径拓展的模糊虚影半径宽度
     */
    private int mBlurMaskRadius;

    /**
     * 虚影颜色
     */
    private int mBlurMaskColor;

    /**
     * 虚影透明度
     */
    private int mBlurMaskAlpha;

    /**
     * 最小有效点击半径
     */
    private int mMinValidateTouchArcRadius;

    /**
     * 最大有效点击半径
     */
    private int mMaxValidateTouchArcRadius;

    /**
     * 圆环进度条起始颜色
     */
    private int mTextColor;

    /**
     * 圆环背景颜色
     */
    private int mCirqueBgColor;

    /**
     * 拖动小圆点bitmap
     */
    private Bitmap mDragBitmap;

    //中间图片，替换中间的文字
    private Bitmap mMiddleImage;

    /**
     * 文本
     */
    private String mText;

    /**
     * 动画开启标志位
     */
    private boolean mIsAnim;

    private boolean mIsTouchOnArc;

    private OnCirqueProgressChangeListener mOnCirqueProgressChangeListener;

    /**
     * 渐变数组
     */
    private int[] mArcColors = new int[]{
            Color.parseColor("#CD1332"),
            Color.parseColor("#CD1332"),
            Color.parseColor("#CD1332")};

    public SlidingRingView(Context context) {
        this(context, null);
    }

    public SlidingRingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mContext = context;
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlidingRingView,
                defStyleAttr, 0);
        mMinProgress = ta.getInteger(R.styleable.SlidingRingView_min_progress, 0);
        mMaxProgress = ta.getInteger(R.styleable.SlidingRingView_max_progress, 100);
        mDotResources = ta.getResourceId(R.styleable.SlidingRingView_dot_resources, R.mipmap.ic_ring_dot);
        mIntermediateImageResources = ta.getResourceId(R.styleable.SlidingRingView_intermediate_image_resources, R.mipmap.ic_set_gill_temp_middle_image);
        mIsAnim = ta.getBoolean(R.styleable.SlidingRingView_is_anim, true);
        ta.recycle();
        initParams();
        initPaint();
    }

    /**
     * 初始化参数
     */
    private void initParams() {
        mLineStrokeWidth = DensityUtils.dip2px(mContext, 2.f);
        mBlurMaskRadius = DensityUtils.dip2px(mContext, 15.f);
        mBlurMaskColor = Color.DKGRAY;
        mBlurMaskAlpha = 60; //0-255
        mTextColor = Color.parseColor("#d7000f");
        mCirqueBgColor = Color.parseColor("#e6e6e6");
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mCircleBgPaint = new Paint();
        int parseColor = Color.parseColor("#E6E6E6");
        mCircleBgPaint.setColor(parseColor);
        mCircleBgPaint.setAntiAlias(true);

        mBlurPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBlurPaint.setColor(mBlurMaskColor);
        mBlurPaint.setAlpha(mBlurMaskAlpha);
        mBlurPaint.setMaskFilter(new BlurMaskFilter(mBlurMaskRadius, BlurMaskFilter.Blur
                .NORMAL));
        mBlurPaint.setAntiAlias(true);

        mCirqueBgPaint = new Paint();
        mCirqueBgPaint.setStrokeWidth(DensityUtils.dip2px(mContext, mLineStrokeWidth));
        mCirqueBgPaint.setStrokeCap(Paint.Cap.ROUND);
        mCirqueBgPaint.setColor(mCirqueBgColor);
        mCirqueBgPaint.setStyle(Paint.Style.STROKE);
        mCirqueBgPaint.setAntiAlias(true);
        //设置阴影 radius为阴影的角度，dx和dy为阴影在x轴和y轴上的距离，color为阴影的颜色
        int parseColor1 = Color.parseColor("#ff4d4d4d");
        mCirqueBgPaint.setShadowLayer(12f, -2f, -2f, parseColor1);
        mCircleBgPaint.setAlpha(40);


        mCirquePaint = new Paint();
        mCirquePaint.setStrokeWidth(DensityUtils.dip2px(mContext, mLineStrokeWidth));
        mCirquePaint.setStrokeCap(Paint.Cap.ROUND);
        mCirquePaint.setStyle(Paint.Style.STROKE);
        mCirquePaint.setDither(true);
        mCirquePaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DensityUtils.dip2px(mContext, 30));

        mBitmapPaint = new Paint();
        mBitmapPaint.setDither(true);
        mBitmapPaint.setFilterBitmap(true);
        mBitmapPaint.setAntiAlias(true);

        mDragBitmap = BitmapUtils.getBitmap(mContext, mDotResources);
        mDragBitmap = BitmapUtils.conversionBitmap(mDragBitmap, DensityUtils.dip2px(mContext,
                30), DensityUtils.dip2px(mContext, 30));

        mMiddleImagePaint = new Paint();
        mMiddleImagePaint.setDither(true);
        mMiddleImagePaint.setFilterBitmap(true);
        mMiddleImagePaint.setAntiAlias(true);
        mMiddleImage = BitmapUtils.getBitmap(mContext, mIntermediateImageResources);
        mMiddleImage = BitmapUtils.conversionBitmap(mMiddleImage, DensityUtils.dip2px(mContext,
                216), DensityUtils.dip2px(mContext, 216));
        mOval = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//            drawBlurBg(canvas);

        drawCircleBg(canvas);

        drawCirqueBg(canvas);

        drawCirque(canvas);

        drawText(canvas);

        drawMiddleImage(canvas);

        drawDragBitmap(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获取点击位置的坐标
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTouchArc(x, y)) {
                    mIsTouchOnArc = true;
                    updateCurrentAngle(x, y);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsTouchOnArc) {
                    updateCurrentAngle(x, y);
                    if (mOnCirqueProgressChangeListener != null)
                        mOnCirqueProgressChangeListener.onChange(mMinProgress, mMaxProgress,
                                Integer.parseInt(mText.replace("℃", "")));
                }
                break;
            case MotionEvent.ACTION_UP:
                mIsTouchOnArc = false;
                if (mOnCirqueProgressChangeListener != null)
                    mOnCirqueProgressChangeListener.onChangeEnd(mMinProgress, mMaxProgress,
                            Integer.parseInt(mText.replace("℃", "")));
                break;
        }
        if (mCurrentAngle <= mEndAngle) {
            invalidate();
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
        int minXY = Math.min(mCenterX, mCenterY);
        mRadius = minXY - mLineStrokeWidth / 2 - mDragBitmap.getWidth() / 2;

        mOval.set(w / 2 - mRadius, h / 2 - mRadius, w / 2 + mRadius, h / 2 + mRadius);

        int left = minXY - mRadius - mDragBitmap.getWidth() / 2;
        int top = minXY - mDragBitmap.getHeight() / 2;
        mBitmapRect = new Rect(left, top, left + mDragBitmap.getWidth(), top + mDragBitmap
                .getHeight());

        mCirquePaint.setShader(new SweepGradient(mCenterX, mCenterY, mArcColors, null));

        mMinValidateTouchArcRadius = (int) (mRadius - mDragBitmap.getWidth() / 2 * 1.5f);
        mMaxValidateTouchArcRadius = (int) (mRadius + mDragBitmap.getWidth() / 2 * 1.5f);
    }

    /**
     * 绘制blur背景虚化 类似于增加了android:translationZ=""
     *
     * @param canvas canvas
     */
    private void drawBlurBg(Canvas canvas) {
        canvas.drawCircle(mCenterX, mCenterY, mRadius + DensityUtils.dip2px(mContext,
                5), mBlurPaint);
    }

    /**
     * 绘制圆背景
     *
     * @param canvas canvas
     */
    private void drawCircleBg(Canvas canvas) {
        canvas.drawCircle(mCenterX, getHeight() / 2, mRadius, mCircleBgPaint);
    }

    /**
     * 绘制圆环背景
     *
     * @param canvas canvas
     */
    private void drawCirqueBg(Canvas canvas) {
//        canvas.drawArc(mOval, 0, 360, false, mCirqueBgPaint);//灰色线圈
        canvas.drawArc(mOval, mStartAngle, mEndAngle, false, mCirqueBgPaint);//灰色线圈
    }

    /**
     * 绘制当前进度圆环
     *
     * @param canvas canvas
     */
    private void drawCirque(Canvas canvas) {
        if (mCurrentAngle != 0) {
//            canvas.drawArc(mOval, 180, mCurrentAngle, false, mCirquePaint);//起始点180度
            canvas.drawArc(mOval, mStartAngle, mCurrentAngle, false, mCirquePaint);//起始点180度
        }
    }

    /**
     * 绘制Text
     *
     * @param canvas canvas
     */
    private void drawText(Canvas canvas) {
        int v = mMaxProgress - mMinProgress;
        mCurrentProgress = Math.round(mCurrentAngle / (mEndAnglef / v)) + mMinProgress;
        mCurrentProgressPercent = mCurrentProgress / ((mMaxProgress - mMinProgress) * 1.f);
        mText = mCurrentProgress + "℃";
        //中间有图片则不绘制文字
        if (mMiddleImage == null) {
            canvas.drawText(mText, getWidth() / 2 - mTextPaint.measureText(mText) / 2, getHeight() / 2 -
                    DensityUtils.dip2px(mContext, 5), mTextPaint);
        }
    }

    /**
     * h绘制中间的图片
     *
     * @param canvas
     */
    private void drawMiddleImage(Canvas canvas) {
        if (mMiddleImage != null)
            canvas.drawBitmap(mMiddleImage, getWidth() / 2 - mMiddleImage.getWidth() / 2, getHeight() / 2 -
                    mMiddleImage.getHeight() / 2, mMiddleImagePaint);
    }

    /**
     * 绘制小圆点bitmap
     *
     * @param canvas canvas
     */
    private void drawDragBitmap(Canvas canvas) {
//        PointF progressPoint = ChartUtils.calcArcEndPointXY(mCenterX, mCenterY, mRadius, mCurrentAngle, 180);
        PointF progressPoint = ChartUtils.calcArcEndPointXY(mCenterX, mCenterY, mRadius, mCurrentAngle, mStartAngle, mEndAngle);

        int left = (int) progressPoint.x - mDragBitmap.getWidth() / 2;
        int top = (int) progressPoint.y - mDragBitmap.getHeight() / 2;

        //        mBitmapRect = new Rect(left, top, left + mDragBitmap.getWidth(), top +
        //                mDragBitmap.getHeight());
        //
        //        canvas.drawBitmap(mDragBitmap,
        //                new Rect(0, 0, mDragBitmap.getWidth(), mDragBitmap.getHeight()),
        //                mBitmapRect, mBitmapPaint);
        //bitmap直接使用BitmapUtils中的缩放方法缩放，可以不用Rect进行缩放，也可以通过限定Rect来限定bitmap大小
        canvas.drawBitmap(mDragBitmap, left, top, mBitmapPaint);
    }

    /**
     * 计算宽/高值
     *
     * @param measureSpec measureSpec
     * @return measure后的宽/高值
     */
    private int measure(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            //wrap_content，最大宽/高为200dp
            result = DensityUtils.dip2px(mContext, 200);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * 更新当前进度对应弧度
     *
     * @param x 按下x坐标点
     * @param y 按下y坐标点
     */
    private void updateCurrentAngle(float x, float y) {
        //根据坐标转换成对应的角度
        float pointX = x - mCenterX;
        float pointY = y - mCenterY;
        float tan_x;//根据左边点所在象限处理过后的x值
        float tan_y;//根据左边点所在象限处理过后的y值
        double atan;//所在象限弧边angle

        //01：第一象限-右上角区域
        //保证dragBitmap在峰值的时候不会因为滑到这个象限更新currentAngle
        if (pointX >= 0 && pointY <= 0) {
            if (((lastQuadrant == 3 && lastAngle == 359.f)
                    || (lastQuadrant == 3 && lastAngle == 0.f))
                    && mTouchQuadrant != 1)
                return;
            mTouchQuadrant = 1;
            tan_x = pointX;
            tan_y = pointY * (-1);
            atan = Math.atan(tan_x / tan_y);//求弧边
            mCurrentAngle = (int) Math.toDegrees(atan) + 90.f + mProgressOffest;
            lastQuadrant = 1;
        }

        //02：第二象限-左上角区域
        if (pointX <= 0 && pointY <= 0) {
            if (((lastQuadrant == 3 && lastAngle == 359.f)
                    || (lastQuadrant == 3 && lastAngle == 0.f))
                    && mTouchQuadrant != 2) {
                return;
            }

            mTouchQuadrant = 2;
            tan_x = pointX * (-1);
            tan_y = pointY * (-1);
            atan = Math.atan(tan_y / tan_x);//求弧边
            mCurrentAngle = (int) Math.toDegrees(atan) + mProgressOffest;
            lastQuadrant = 2;
        }

        //03：第三象限-左下角区域
        if (pointX <= 0 && pointY >= 0) {
            tan_x = pointX * (-1);
            tan_y = pointY;
            atan = Math.atan(tan_x / tan_y);//求弧边
            if ((int) Math.toDegrees(atan) >= (90.f - mProgressOffest)) {
                mCurrentAngle = (int) Math.toDegrees(atan) - (90.f - mProgressOffest);
                if (lastAngle >= 270.f) {
                    mCurrentAngle = 359.f;
                }
            } else {
                mCurrentAngle = (int) Math.toDegrees(atan) + 270.f + mProgressOffest;
                if (lastAngle <= 90.f) {
                    mCurrentAngle = 0.f;
                }
            }
            lastQuadrant = 3;
        }

        //04：第四象限-右下角区域
        //保证dragBitmap在峰值的时候不会因为滑到这个象限更新currentAngle
        if (pointX >= 0 && pointY >= 0) {
            if (((lastQuadrant == 3 && lastAngle == 359.f)
                    || (lastQuadrant == 3 && lastAngle == 0.f))
                    && mTouchQuadrant != 4)
                return;
            mTouchQuadrant = 4;
            tan_x = pointX;
            tan_y = pointY;
            atan = Math.atan(tan_y / tan_x);//求弧边
            mCurrentAngle = (int) Math.toDegrees(atan) + 180.f + mProgressOffest;
            lastQuadrant = 4;
            if (mCurrentAngle >= mEndAngle) {
                return;
            }
        }
        lastAngle = mCurrentAngle;
    }

    /**
     * 按下时判断按下的点是否按在圆边范围内
     *
     * @param x x坐标点
     * @param y y坐标点
     */
    private boolean isTouchArc(float x, float y) {
        double d = getTouchRadius(x, y);
        return d >= mMinValidateTouchArcRadius && d <= mMaxValidateTouchArcRadius;
    }

    /**
     * 计算某点到圆点的距离
     *
     * @param x x坐标点
     * @param y y坐标点
     */
    private double getTouchRadius(float x, float y) {
        float cx = x - getWidth() / 2;
        float cy = y - getHeight() / 2;
        return Math.hypot(cx, cy);
    }

    /**
     * 设置进度
     *
     * @param progress 进度数据
     */
    public void setProgress(int progress) {
        setProgress(progress, mIsAnim);
    }

    /**
     * 设置进度
     *
     * @param progress 进度数据
     */
    public void setProgress(int progress, boolean isAnim) {
        if (progress < mMinProgress || progress > mMaxProgress) {
            throw new RuntimeException("set progress out of range");
        }

        float endAngle = (float) (progress - mMinProgress) / (mMaxProgress - mMinProgress) *
                mEndAnglef;

        if (isAnim) {
            startAnim(endAngle);
        } else {
            mCurrentAngle = endAngle;
            postInvalidate();
        }
        if (mOnCirqueProgressChangeListener != null)
            mOnCirqueProgressChangeListener.onChange(mMinProgress, mMaxProgress,
                    getCurrentProgress());
    }

    public void addProgress() {
        mCurrentProgress++;
        if (mCurrentProgress < mMinProgress || mCurrentProgress > mMaxProgress) {
            mCurrentProgress--;
            return;
        }
        setProgress(mCurrentProgress, false);
    }

    public void cutProgress() {
        mCurrentProgress--;
        if (mCurrentProgress < mMinProgress || mCurrentProgress > mMaxProgress) {
            mCurrentProgress++;
            return;
        }
        setProgress(mCurrentProgress, false);
    }

    /**
     * 获取当前progress具体值
     * <p>
     * 这个progress不是百分比，而是转换后具体的值
     *
     * @return 当前progress具体值
     */
    public int getCurrentProgress() {
        return mCurrentProgress;
    }

    /**
     * 获取当前progress进度百分比
     *
     * @return 当前progress进度百分比
     */
    public float getProgressPercent() {
        return mCurrentProgressPercent;
    }

    /**
     * 开启动画
     * <p>
     * 起始点向当前进度点动画绘制
     */
    private void startAnim(float endAngle) {
        ValueAnimator mAngleAnim = ValueAnimator.ofFloat(0, endAngle);
        mAngleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mAngleAnim.setDuration(1500);
        mAngleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mCurrentAngle = (float) valueAnimator.getAnimatedValue();
                postInvalidate();
            }
        });
        mAngleAnim.start();
    }

    /**
     * 设置是否开启动画
     * <p>
     * 此方法要在setProgress()方法之前执行，否则没有效果
     *
     * @param isAnim 是否开启动画
     */
    public void setIsAnim(boolean isAnim) {
        mIsAnim = isAnim;
    }

    /**
     * 设置圆环进度范围
     *
     * @param minProgress 最小进度
     * @param maxProgress 最大进度
     */
    public void setProgressRange(int minProgress, int maxProgress) {
        if (minProgress >= maxProgress) {
            throw new RuntimeException("progress range anomaly");
        }
        mMinProgress = minProgress;
        mMaxProgress = maxProgress;
    }

    /**
     * 圆环进度状态变化接口
     */
    public interface OnCirqueProgressChangeListener {
        /**
         * 圆环进度变化
         *
         * @param minProgress 整个圆环的最小progress值
         * @param maxProgress 整个圆环的最大progress值
         * @param progress    当前progress 这里的progress是根据当前进度百分比和最大最小值转换后的精确值
         */
        void onChange(int minProgress, int maxProgress, int progress);

        /**
         * 进度变化结束
         *
         * @param minProgress 整个圆环的最小progress值
         * @param maxProgress 整个圆环的总progress值
         * @param progress    当前progress 这里的progress是根据当前进度百分比和最大最小值转换后的精确值
         */
        void onChangeEnd(int minProgress, int maxProgress, int progress);
    }

    /**
     * 设置圆环进度变化监听
     *
     * @param onCirqueProgressChangeListener onCirqueProgressChangeListener
     */
    public void setOnTextFinishListener(OnCirqueProgressChangeListener
                                                onCirqueProgressChangeListener) {
        mOnCirqueProgressChangeListener = onCirqueProgressChangeListener;
    }
}
