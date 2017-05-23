package com.hgxx.whiteboard.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hgxx.whiteboard.R;

/**
 * Created by ly on 21/05/2017.
 */

public class ColorPointer extends View {

    private float radius = 40;
    private int color = Color.parseColor("#ff0000");
    private int viewWidth;
    private int viewHeight;
    private Paint mPaint;
    private float arrowHeight;

    public ColorPointer(Context context) {
        this(context, null);
    }

    public ColorPointer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPointer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getAttr(context, attrs, defStyleAttr);
        initPaint();
    }
    private void getAttr(Context context, AttributeSet attributeSet, int defStyleAtt) {
        TypedArray att = context.obtainStyledAttributes(attributeSet, R.styleable.ColorPointer, defStyleAtt, 0);
        radius = att.getDimension(R.styleable.ColorPointer_radius, radius);
        this.setClickable(false);
    }

    public void setColor(String color){
        mPaint.setColor(Color.parseColor(color));
        invalidate();
    }


    private void initPaint(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path path = new Path();

        float centerX = radius;
        float centerY = radius;

        path.addCircle(centerX, centerY, radius, Path.Direction.CCW);

        float deltaX = getTanDeltaX(radius, arrowHeight);
        float deltaY = getTanDeltaY(radius, arrowHeight);


        path.moveTo(centerX-deltaX, centerY+deltaY);
        path.quadTo(centerX, 2*radius+arrowHeight*(1-0.618f), centerX, viewHeight);

//        path.moveTo(viewWidth, centerY);
        path.quadTo(centerX, 2*radius+arrowHeight*(1-0.618f), centerX+deltaX, centerY+deltaY);


        canvas.drawPath(path, mPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        viewWidth = (int)radius*2;
        arrowHeight = radius;
        viewHeight = (int)(viewWidth+ arrowHeight);

        int msw = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY);
        int msh = MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY);
        super.onMeasure(msw, msh);
    }



    private float getTanDeltaY(float radius, float arrowHeight){
        return radius*radius/(radius+arrowHeight);
    }

    private float getTanDeltaX(float radius, float arrowHeight){
          return (float)Math.sqrt(Math.pow(radius, 2)-Math.pow(getTanDeltaY(radius, arrowHeight), 2));
    }


    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getViewHeight() {
        return viewHeight;
    }

}

