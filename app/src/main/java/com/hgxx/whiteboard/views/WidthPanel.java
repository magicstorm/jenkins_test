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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by ly on 22/05/2017.
 */

public class WidthPanel extends PopoutMenu {
    int widthCount = 5;
    private float[] widths = new float[]{
            1,5,10,15,20,25
    };
    private float widthRef = 529;

    float[] radius = new float[]{
            3f,
            5f,
            7f,
            9f,
            11f,
    };

    private int selectedWidth = -1;


    ArrayList<RectF> widthAreas = new ArrayList<>();
    private Paint mPaint;

    public WidthPanel(Context context) {
        this(context, null);
    }

    public WidthPanel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WidthPanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDatas();
        initPaint();
        setClickable(true);
    }

    private void initDatas(){
        for(int i=0;i<widthCount;i++){
            widthAreas.add(null);
        }

    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor("#fc7d61"));
        mPaint.setStrokeWidth(dp2px(2));
        mPaint.setStyle(Paint.Style.STROKE);
    }



    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        float leftPadding = dp2px(15);
        float endPadding = dp2px(20);
        float leftStart = viewLeft + leftPadding;
        float drawWidth = getActWidth()-leftPadding-endPadding;

        float totalOvalDiameter = 0;
        for(int i=0;i<radius.length;i++){
            totalOvalDiameter+=dp2px(radius[i])*2;
        }
        float lineLen = (drawWidth - totalOvalDiameter)/widthCount;


        float currentStart = leftStart;
        for(int i=0;i<widthCount;i++){
            float blockWidth = dp2px(radius[i])*2+lineLen;

            RectF curRect = new RectF(currentStart, viewTop, currentStart+blockWidth, viewBottom);
            currentStart += blockWidth;

            widthAreas.set(i, curRect);

            if(getSelectedWidth()==i){
                mPaint.setStyle(Paint.Style.FILL);
            }else{
                mPaint.setStyle(Paint.Style.STROKE);
            }

            RectF roundRect = new RectF(
                    curRect.centerX()-dp2px(radius[i]),
                    curRect.centerY()-dp2px(radius[i]),
                    curRect.centerX()+dp2px(radius[i]),
                    curRect.centerY()+dp2px(radius[i])
            );

            canvas.drawOval(
                    roundRect,
                    mPaint
                    );

            if(i<4){
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(roundRect.right, roundRect.centerY(), roundRect.right+lineLen, roundRect.centerY(), mPaint);
            }


        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return super.onInterceptTouchEvent(ev);
        return true;
    }


    public interface OnWidthSelected{
        void onWidthSelected(float ratio);
    }

    private OnWidthSelected onWidthSelected;

    public void setOnWidthSelected(OnWidthSelected onWidthSelected) {
        this.onWidthSelected = onWidthSelected;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if(action==MotionEvent.ACTION_DOWN){
            for(int i=0;i<widthAreas.size();i++){
                if(widthAreas.get(i).contains(event.getX(), event.getY())){
                    setSelectedWidth(i);
                    break;
                }
            }
            invalidate();
        }


        return super.onTouchEvent(event);
    }

    private float dp2px(float dp){
        return (int)(dp*getContext().getResources().getDisplayMetrics().density+0.5f);
    }

    public int getSelectedWidth() {
        return selectedWidth;
    }

    public void setSelectedWidth(int selectedWidth) {
        if(onWidthSelected!=null){
            onWidthSelected.onWidthSelected(widths[selectedWidth]/widthRef);
        }
        this.selectedWidth = selectedWidth;
    }
}
