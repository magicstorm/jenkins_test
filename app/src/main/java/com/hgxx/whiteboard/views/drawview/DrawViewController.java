package com.hgxx.whiteboard.views.drawview;



import java.util.Arrays;

/**
 * Created by ly on 03/05/2017.
 */

public class DrawViewController {
    private boolean isMoving = false;
    private DrawLayout mDv;

    public DrawViewController(DrawLayout dv){
        mDv = dv;
    }

    public void clear(){
        if(mDv==null)return;
        mDv.clear();
    }

    public int getHeight(){
        if(mDv==null)return 0;
        return mDv.getCurHeight();
    }

    public int getWidth(){
        if(mDv==null)return 0;
        return mDv.getCurWidth();
    }

    public void drawEnd(){
        if(mDv==null)return;
        mDv.drawEnd();
    }

    public void setWidth(int width){
        if(mDv==null)return;
        mDv.setCurWidth(width);
        mDv.getLayoutParams().width = width;
    }

    public void setHeight(int height){
        if(mDv==null)return;
        mDv.setCurHeight(height);
        mDv.getLayoutParams().height = height;
    }

    public void setDrawType(String drawType){
        if(Arrays.asList(DrawLayout.DRAW_TYPES).contains(drawType)){
            mDv.setDrawType(drawType);
        }
        else{
            mDv.setDrawType(DrawLayout.DRAW_TYPE_LINE);
        }
    }

    public void cancel(int cancelIndex){
        mDv.touch_cancel(cancelIndex);
    }


    public void setDrawable(boolean drawable){
        mDv.setDrawable(drawable);
    }

    public void startDraw(float x, float y){
        mDv.startDraw(x, y);
    }

    public void drawMove(float x, float y){
        mDv.drawMove(x, y);
    }

    public void setStrokeWidth(float strokeWidth){
        mDv.setStrokeWidth(strokeWidth);
    }

    public void setPaintColor(String color){
        mDv.setPaintColor(color);
    }

    public synchronized boolean isMoving() {
        return isMoving;
    }

    public synchronized void setMoving(boolean moving) {
        isMoving = moving;
    }
}
