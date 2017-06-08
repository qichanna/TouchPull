package home.liqi.touchpull;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.icu.util.Measure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liqi on 2017/6/6.
 */

public class TouchPullView extends View {

    private Paint mCiclePaint;
    private float mCicleRadius  = 40;
    private float mCirclePointX, mCirclePointY;

    //可拖动的高度
    private int mDragHeight = 600;

    private float mProgress;

    //目标宽度
    private int mTargetWidth = 200;

    //贝塞尔曲线的路径和画笔
    private Path mPath = new Path();
    private Paint mPathPaint;
    //重心点最终高度，决定控制点的Y坐标
    private int mTargetGravityHeight;
    //角度变换 0- 135度
    private int mTargetAngle = 120;

    public TouchPullView(Context context) {
        super(context);
        init();
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF000000);
        mCiclePaint = p;

        //初始化路径部分画笔
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF000000);
        mPathPaint = p;



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
        final  float progress = mProgress;

        //获取可绘制区域高度和宽度
        final float w = getValueByLine(getWidth(),mTargetWidth,progress);
        final float h = getValueByLine(0,mDragHeight,progress);
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

        double radian = Math.toRadians(getValueByLine(0,mTargetAngle,progress));
        float x = (float) (Math.sin(radian) * cRadius);
        float y = (float) (Math.cos(radian) * cRadius);

        lEndPointX = cPointx - x;
        lEndPointY = cPointy + y;

        //控制点Y坐标变化
        lControlPointY = getValueByLine(0,endControlY,progress);
        //控制点和结束点之间的高度
        float tHeight = lControlPointY - lControlPointY;
        //控制点与X的坐标距离
        float tWidth = (float) (tHeight/Math.tan(radian));
        lControlPointX = lEndPointX - tWidth;

        path.quadTo(lControlPointX,lControlPointY,lEndPointX,lEndPointY);
        //连接到右边
        path.lineTo(cPointx + (cPointx-lEndPointX),lEndPointY);
        //画右边的贝塞尔曲线
        path.quadTo(cPointx+cPointx-lControlPointX,lControlPointY,w,0);
    }

    private float getValueByLine(float start,float end,float progress){
        return start + (end - start) * progress;
    }
}
