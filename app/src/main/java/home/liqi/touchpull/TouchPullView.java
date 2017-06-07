package home.liqi.touchpull;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.icu.util.Measure;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liqi on 2017/6/6.
 */

public class TouchPullView extends View {

    private Paint mCiclePaint;
    private int mCicleRadius  =150;
    private int mCirclePointX, mCirclePointY;

    //可拖动的高度
    private int mDragHeight = 800;

    private float mProgress;

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        canvas.drawCircle(mCirclePointX,mCirclePointY,mCicleRadius,mCiclePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCirclePointX = getWidth() >> 1;
        mCirclePointY = getHeight() >> 1;
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
        int iWidth = 2 * mCicleRadius + getPaddingLeft() + getPaddingRight();

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

    public void setProgress(float progress){
        mProgress = progress;
        //请求重新进行测量
        requestLayout();
    }
}
