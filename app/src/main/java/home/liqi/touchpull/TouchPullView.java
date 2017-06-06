package home.liqi.touchpull;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by liqi on 2017/6/6.
 */

public class TouchPullView extends View {

    private Paint mCiclePaint;
    private int mCicleRadius  =150;

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
        float x = getWidth() >> 1;
        float y = getHeight() >> 1;
        canvas.drawCircle(x,y,mCicleRadius,mCiclePaint);
    }
}
