package com.hgxx.whiteboard.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ly on 27/04/2017.
 */

public class WhiteBoardView extends View {

    private String backgroundColor;
    private DisplayMetrics dm;
    private float density;
    private int paintWidth;
    private float lastx;
    private float lasty;
    private String paintColor;
    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;


    public WhiteBoardView(Context context) {
        this(context, null);
    }

    public WhiteBoardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhiteBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDatas();
        initPaint();
        initBoard();
    }

    private void initDatas(){
        dm = getResources().getDisplayMetrics();
        density = dm.density;
        backgroundColor =  "#eeeeee";
        lastx = 0;
        lasty = 0;
        paintWidth = dp2px(1);
        paintColor = "#000000";
    }

    private void initPaint(){
        //draw paint
        paint = new Paint();
        paint.setStrokeWidth(paintWidth);
        paint.setAntiAlias(true);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor(paintColor));
    }

    private void initBoard(){
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        //draw background
        paint.setColor(Color.parseColor(backgroundColor));
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }



    private void drawLine(float startx, float starty, float endx, float endy){

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                lastx = event.getRawX();
                lasty = event.getRawY();

                break;
            case MotionEvent.ACTION_MOVE:
//                float deltax = event.getRawX()-lastx;
//                float deltay = event.getRawY()-lasty;
                drawLine(lastx, lasty, event.getRawX(), event.getRawY());
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }



        return super.onTouchEvent(event);
    }

    /**
     * helpers
     */

    //get px from dp
    private int dp2px(float dp){
        return (int)(dp*density+0.5f);
    }


    /**
     * getters & setters
     */

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getPaintColor() {
        return paintColor;
    }

    public void setPaintColor(String paintColor) {
        this.paintColor = paintColor;
    }
}
