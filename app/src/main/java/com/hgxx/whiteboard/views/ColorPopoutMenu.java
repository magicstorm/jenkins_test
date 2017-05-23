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
    private int pointerSize=20;

    private float cpPosX = 0;
    private String cpColor;
    private float cpPosY;
    private float cpMoveEnd;
    private float cpMoveStart;


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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
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
        cpColor = colorStrings[0];

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



        for(int i=0;i<colors.size();i++){
            float blockLeft = barLeft +i*blockWidth;
            float blockRight = blockLeft+blockWidth;
            colorRects.set(i, new RectF(blockLeft, contentRect.top, blockRight, contentRect.bottom));
        }

        cpMoveStart = colorRects.get(0).left;
        cpMoveEnd = colorRects.get(colorRects.size()-1).right;

        if(cpPosX==0){
            cpPosX = colorRects.get(0).centerX();
        }
        cpPosY = barTop;


    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for(int i=0;i<colors.size();i++){
            mPaint.setColor(Color.parseColor(colors.get(i)));
            Path path = new Path();
            path.moveTo(colorRects.get(i).left, barTop);
            path.addRect(colorRects.get(i).left, barTop, colorRects.get(i).right, barBottom, Path.Direction.CCW);
            canvas.drawPath(path, mPaint);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }








    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return true;
//        return super.onInterceptHoverEvent(event);
    }

    public interface OnColorMoveListener{
        void onColorMove(float cpPosX, String color);
    }

    private OnColorMoveListener onColorMoveListener;

    public void setOnColorMoveListener(OnColorMoveListener onColorMoveListener) {
        this.onColorMoveListener = onColorMoveListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if(action==MotionEvent.ACTION_DOWN){
            float x = event.getX();
            colorMove(x);
            //TODO move pointer here && change color


        }else if (action==MotionEvent.ACTION_MOVE){
            //TODO move pointer here && change color
            float x = event.getX();
            colorMove(x);

        }else if(action==MotionEvent.ACTION_UP){

        }else if(action==MotionEvent.ACTION_CANCEL){

        }


        return true;
//        return super.onTouchEvent(event);
    }

    public void colorMove(float x) {
        if(x<=cpMoveEnd&&x>=cpMoveStart){
            setCpPosX(x);
            for(int i=0;i<colorRects.size();i++){
                if(colorRects.get(i).contains(x, colorRects.get(i).centerY())){
                    cpColor = colors.get(i);
                    break;
                }
            }

            if(onColorMoveListener!=null){
                onColorMoveListener.onColorMove(cpPosX, cpColor);
            }
        }
    }

    private float dp2px(float dp){
        return (int)(dp*getContext().getResources().getDisplayMetrics().density+0.5f);
    }

    public synchronized float getCpPosX() {
        return cpPosX;
    }

    public synchronized void setCpPosX(float cpPosX) {
        this.cpPosX = cpPosX;
    }

    public float getCpPosY() {
        return cpPosY;
    }

    public String getCpColor() {
        return cpColor;
    }
}
