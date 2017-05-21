package com.hgxx.whiteboard.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ly on 20/05/2017.
 */

public class ColorPopoutMenu extends PopoutMenu {
    private List<String> colors = new ArrayList<>();
    private ArrayList<RectF> colorRects = new ArrayList<>();

    private float colorBarWidth;
    private float barLeft;
    private float colorBarHeight;
    private float barTop;
    private float blockWidth;
    private float barBottom;
    private Paint mPaint;
    private RectF contentRect;
    private float barRight;

    public ColorPopoutMenu(Context context) {
        this(context, null);
    }

    public ColorPopoutMenu(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPopoutMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDefaultColors();
        initPaint();
    }

    private void setDefaultColors(){
        String[] colorStrings  = new String[]{
                "#999999",
                "#ff0033",
                "#00cc00",
                "#0000cc",
                "#ffcc00",
                "#ff6633",
                "#990099",
                "#663300",
                "#000000",
        };
        setColors(colorStrings);

        for(int i=0;i<colorStrings.length;i++){
            colorRects.add(null);
        }
    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setColors(String[] colors){
        this.colors = Arrays.asList(colors);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        colorBarWidth = getActWidth() *0.80f;
        colorBarHeight = getActHeight() * 0.2f;


        contentRect = getContentRect();

        float verticalMargin = (getActHeight()-colorBarHeight)/2f;
        float horizontalMargin = (getActWidth()-colorBarWidth)/2f;

        barLeft =  horizontalMargin + contentRect.left;
        barTop = verticalMargin + contentRect.top;
        barBottom = contentRect.bottom - verticalMargin;
        barRight = contentRect.right - horizontalMargin;

        blockWidth = colorBarWidth/colors.size();

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for(int i=0;i<colors.size();i++){
            mPaint.setColor(Color.parseColor(colors.get(i)));
            float blockLeft = barLeft +i*blockWidth;
            float blockRight = blockLeft+blockWidth;
            Path path = new Path();
            path.moveTo(blockLeft, barTop);
            path.addRect(blockLeft, barTop, blockRight, barBottom, Path.Direction.CCW);


            colorRects.set(i, new RectF(blockLeft, contentRect.top, blockRight, contentRect.bottom));

            canvas.drawPath(path, mPaint);
        }
    }

    private void initPointer(){

    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return true;
//        return super.onInterceptHoverEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if(action==MotionEvent.ACTION_DOWN){
            float x = event.getX();
            float y = event.getY();
            //TODO move pointer here && change color


        }else if (action==MotionEvent.ACTION_MOVE){
            //TODO move pointer here && change color

        }else if(action==MotionEvent.ACTION_UP){

        }else if(action==MotionEvent.ACTION_CANCEL){

        }


        return super.onTouchEvent(event);
    }
}
