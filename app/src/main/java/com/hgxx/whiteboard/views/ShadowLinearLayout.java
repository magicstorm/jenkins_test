package com.hgxx.whiteboard.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.hgxx.whiteboard.R;

/**
 * Created by ly on 22/05/2017.
 */

public class ShadowLinearLayout extends LinearLayout {

    private static int ELEVATION_POS_LEFT = 1;
    private static int ELEVATION_POS_RIGHT = 2;
    private static int ELEVATION_POS_TOP = 3;
    private static int ELEVATION_POS_BOTTOM = 4;

    private float elevationSize;
    private int elevationPos;
    private Paint mPaint;
    private int viewWidth;
    private int viewHeight;
    private Paint shadowPaint;
    private int shadowColor = Color.parseColor("#999999");

    public ShadowLinearLayout(Context context) {
        this(context, null);
    }

    public ShadowLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getAttr(context, attrs, defStyleAttr);

    }

    private void getAttr(Context context, AttributeSet attributeSet, int defStyleAttr){
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.ShadowLinearLayout, defStyleAttr, 0);
        elevationSize = typedArray.getDimension(R.styleable.ShadowLinearLayout_elevationSize, 0);
        elevationPos = typedArray.getInt(R.styleable.ShadowLinearLayout_elevationPos, 2);
        initPaint();
    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#ffffff"));
//        mPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.transparent));



        shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.parseColor("#999999"));
        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint);
        if(elevationPos==ELEVATION_POS_RIGHT){
            mPaint.setShadowLayer(elevationSize, elevationSize*0.3f, elevationSize*0.3f, shadowColor);
        }
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        if(elevationPos==ELEVATION_POS_RIGHT){
            Path path = new Path();
            path.addRect(0, 0f, viewWidth-elevationSize, (float)viewHeight, Path.Direction.CCW);
            canvas.drawPath(path, mPaint);
        }
        super.dispatchDraw(canvas);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
    }
}
