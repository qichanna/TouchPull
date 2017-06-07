package home.liqi.touchpull;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liqi on 2017/6/7.
 */

public class BezierView extends View {

    private Path mSrcBezier = new Path();
    private Path mBezier = new Path();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BezierView(Context context) {
        this(context,null);
    }

    public BezierView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BezierView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        Paint paint = mPaint;
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        //        mSrcBezier.quadTo();
        mSrcBezier.cubicTo(200,700,500,1200,700,200);

        new Thread(){
            @Override
            public void run() {
                initBezier();
            }
        }.start();

    }

    private void initBezier(){

        float[] xPoints = new float[]{0,200,500,700};
        float[] yPoints = new float[]{0,700,1200,200};

        Path path = mBezier;

        int fps = 1000;

        for(int i = 0; i <= fps; i++){
            float progress = i / (float)fps;
            float x = calculateBezier(progress,xPoints);
            float y = calculateBezier(progress,yPoints);
            path.lineTo(x,y);
            postInvalidate();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



    }

    private float calculateBezier(float t, float... values){
        final int len = values.length;

        for(int i = len - 1;i > 0; i--){
            for (int j = 0; j < i; j++){
                values[j] = values[j] + (values[j+1] - values[j]) * t;
            }
        }
        return values[0];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(0x40000000);
        canvas.drawPath(mSrcBezier,mPaint);
        mPaint.setColor(Color.RED);
        canvas.drawPath(mBezier,mPaint);

    }
}
