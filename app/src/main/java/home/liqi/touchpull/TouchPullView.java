package home.liqi.touchpull;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.icu.util.Measure;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by liqi on 2017/6/6.
 */

public class TouchPullView extends View {

    private Paint mCiclePaint;
    private float mCicleRadius  = 50;
    private float mCirclePointX, mCirclePointY;

    //可拖动的高度
    private int mDragHeight = 300;

    private float mProgress;

    //目标宽度
    private int mTargetWidth = 400;

    //贝塞尔曲线的路径和画笔
    private Path mPath = new Path();
    private Paint mPathPaint;
    //重心点最终高度，决定控制点的Y坐标
    private int mTargetGravityHeight = 10;
    //角度变换 0- 135度
    private int mTargetAngle = 105;
    private Interpolator mProgressInterpolator = new DecelerateInterpolator();
    private Interpolator mTanentAngleInterpolator;

    //释放
    private ValueAnimator valueAnimator = null;


    private Drawable mContent = null;
    private int mContentMargin = 0;

    public TouchPullView(Context context) {
        super(context);
        init(null);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs){

        final Context context = getContext();
        TypedArray array = context.obtainStyledAttributes(attrs,R.styleable.TouchPullView,0,0);
        int color = array.getColor(R.styleable.TouchPullView_pColor,0x20000000);
        mCicleRadius = array.getDimension(R.styleable.TouchPullView_pRadius,mCicleRadius);
        mDragHeight = array.getDimensionPixelOffset(R.styleable.TouchPullView_pDragHeight,mDragHeight);
        mTargetAngle = array.getInt(R.styleable.TouchPullView_pTangentAngle,100);
        mTargetWidth = array.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetWidth,mTargetWidth);
        mTargetGravityHeight = array.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetGravityHeight,mTargetGravityHeight);
        mContent = array.getDrawable(R.styleable.TouchPullView_pContentDrawable);
        mContentMargin = array.getDimensionPixelOffset(R.styleable.TouchPullView_pContentDrawableMargin,0);
        array.recycle();


        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFF4081);
        mCiclePaint = p;

        //初始化路径部分画笔
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFF4081);
        mPathPaint = p;

        //切角路径插值器
        mTanentAngleInterpolator = PathInterpolatorCompat.create((mCicleRadius * 2.0f)/mDragHeight,90.0f/mTargetAngle);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //进行基础坐标参数系转变
        int count = canvas.save();
        float tranX = (getWidth() - getValueByLine(getWidth(),mTargetWidth,mProgress))/2;
        canvas.translate(tranX,0);

        //画贝塞尔曲线
        canvas.drawPath(mPath,mPathPaint);

        //画圆
        canvas.drawCircle(mCirclePointX,mCirclePointY,mCicleRadius,mCiclePaint);

        Drawable drawable = mContent;
        if(drawable != null){
            canvas.save();
            canvas.clipRect(drawable.getBounds());
            drawable.draw(canvas);
            canvas.restore();
        }

        canvas.restoreToCount(count);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //宽度的意图，宽度的类型
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int iHeight = (int)((mDragHeight * mProgress + 0.5f) + getPaddingTop() + getPaddingBottom());
        int iWidth = (int) (2 * mCicleRadius + getPaddingLeft() + getPaddingRight());

        int measureWidth,measureHeight;

        if(widthMode == MeasureSpec.EXACTLY){
            measureWidth = width;
        }else if(widthMode == MeasureSpec.AT_MOST) {
            measureWidth = Math.min(iWidth,width);
        }else {
            measureWidth = iWidth;
        }

        if(heightMode == MeasureSpec.EXACTLY){
            measureHeight = height;
        }else if(heightMode == MeasureSpec.AT_MOST) {
            measureHeight = Math.min(iHeight,height);
        }else {
            measureHeight = iHeight;
        }

        setMeasuredDimension(measureWidth,measureHeight);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updatePathLayout();
//        mCirclePointX = getWidth() >> 1;
//        mCirclePointY = getHeight() >> 1;
    }

    public void setProgress(float progress){
        mProgress = progress;
        //请求重新进行测量
        requestLayout();
    }

    //更新我们的路径相关操作
    private void updatePathLayout(){
        final  float progress = mProgressInterpolator.getInterpolation(mProgress);

        //获取可绘制区域高度和宽度
        final float w = getValueByLine(getWidth(),mTargetWidth,mProgress);
        final float h = getValueByLine(0,mDragHeight,mProgress);
        //圆的圆心X
        final float cPointx = w/2.0f;
        //圆的半径
        final float cRadius = mCicleRadius;
        //圆心Y
        final float cPointy = h - cRadius;
        //控制点结束Y的值
        final float endControlY = mTargetGravityHeight;

        //更新圆的坐标
        mCirclePointX = cPointx;
        mCirclePointY = cPointy;

        final Path path = mPath;
        path.reset();
        path.moveTo(0,0);


        //左边部分的结束点和控制点
        float lEndPointX,lEndPointY;
        float lControlPointX,lControlPointY;

        //获取当前切线的弧度
        float angle = mTargetAngle * mTanentAngleInterpolator.getInterpolation(progress);
        //***
        double radian = Math.toRadians(angle);
//        double radian = Math.toRadians(getValueByLine(0,mTargetAngle,progress));
        //***
        float x = (float) (Math.sin(radian) * cRadius);
        float y = (float) (Math.cos(radian) * cRadius);

        lEndPointX = cPointx - x;
        lEndPointY = cPointy + y;

        //控制点Y坐标变化
        lControlPointY = getValueByLine(0,endControlY,progress);
        //控制点和结束点之间的高度
        float tHeight = lEndPointY - lControlPointY;
        //控制点与X的坐标距离
        float tWidth = (float) (tHeight/Math.tan(radian));
        lControlPointX = lEndPointX - tWidth;

        path.quadTo(lControlPointX,lControlPointY,lEndPointX,lEndPointY);
        //连接到右边
        path.lineTo(cPointx + (cPointx-lEndPointX),lEndPointY);
        //画右边的贝塞尔曲线
        path.quadTo(cPointx+cPointx-lControlPointX,lControlPointY,w,0);

        //更新内容部分Drawable
        updateCOntentLayout(cPointx,cPointy,cRadius);
    }

    private void updateCOntentLayout(float cx,float cy,float radius){
        Drawable drawable = mContent;
        if(drawable != null){
            int margin = mContentMargin;
            int l = (int) (cx - radius + margin);
            int r = (int) (cx + radius - margin);
            int t = (int) (cy - radius + margin);
            int b = (int) (cy + radius - margin);
            drawable.setBounds(l,t,r,b);
        }
    }

    private float getValueByLine(float start,float end,float progress){
        return start + (end - start) * progress;
    }


    //释放动画
    public void release(){
        if(valueAnimator == null){
            ValueAnimator animator = ValueAnimator.ofFloat(mProgress,0f);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(400);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object val = animation.getAnimatedValue();
                    if(val instanceof  Float){
                        setProgress((Float) val);
                    }
                }
            });
            valueAnimator = animator;
            Log.e("liqi7", "onTouch: 3333333" );
        }else {
            valueAnimator.cancel();
            valueAnimator.setFloatValues(mProgress,0f);
            Log.e("liqi7", "onTouch: 4444444444" );
        }
        Log.e("liqi7", "onTouch: 2222222" );
        valueAnimator.start();
    }
}